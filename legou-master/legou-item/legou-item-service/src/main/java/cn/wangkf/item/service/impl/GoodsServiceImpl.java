package cn.wangkf.item.service.impl;

import cn.wangkf.common.parameter.SpuQueryByPageParameter;
import cn.wangkf.common.utils.DozerUtils;
import cn.wangkf.item.mapper.BrandMapper;
import cn.wangkf.item.mapper.SkuMapper;
import cn.wangkf.item.mapper.StockMapper;
import cn.wangkf.item.mapper.CategoryMapper;
import cn.wangkf.item.mapper.SpuDetailMapper;
import cn.wangkf.item.mapper.SpuMapper;
import cn.wangkf.item.service.CategoryService;
import cn.wangkf.item.service.GoodsService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import com.github.pagehelper.Page;
import cn.wangkf.common.vo.PageResult;
import cn.wangkf.item.pojo.*;
import cn.wangkf.item.bo.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.Date;

@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    SpuMapper spuMapper;

    @Autowired
    BrandMapper brandMapper;

    @Autowired
    CategoryMapper categoryMapper;

    @Autowired
    CategoryService categoryService;

    @Autowired
    private SpuDetailMapper spuDetailMapper;

    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private StockMapper stockMapper;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    private ListOperations listRedisTemplate;


    private static final String REDIS_SPU_KEY_PRE = "item-service_spu_id_";

    private static final String REDIS_SPU_DETAIL_KEY_PRE = "item-service_spu_detail_spuid_";

    private static final String REDIS_SKU_KEY_PRE = "item-service_sku_spuid_";

    @PostConstruct
    public void init() {
        listRedisTemplate = redisTemplate.opsForList();
    }

    @Override
    public PageResult<SpuBo> queryGoodsWithPage(SpuQueryByPageParameter spuQueryByPageParameter) {
        //1.查询spu，分页查询，最多查询100条
        PageHelper.startPage(spuQueryByPageParameter.getPage(),
                Math.min(spuQueryByPageParameter.getRows(),100));

        //2.创建查询条件
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();

        //3.条件过滤
        //3.1 是否过滤上下架
        if (spuQueryByPageParameter.getSaleable() != null){
            criteria.orEqualTo("saleable",spuQueryByPageParameter.getSaleable());
        }
        //3.2 是否模糊查询
        if (StringUtils.isNotBlank(spuQueryByPageParameter.getKey())){
            criteria.andLike("title", "%"+spuQueryByPageParameter.getKey()+"%");
        }
        //3.3 是否排序
        if (StringUtils.isNotBlank(spuQueryByPageParameter.getSortBy())){
            example.setOrderByClause(spuQueryByPageParameter.getSortBy()
                    + (spuQueryByPageParameter.getDesc() ? " DESC":" ASC"));
        }
        Page<Spu> pageInfo = (Page<Spu>)spuMapper.selectByExample(example);


        //将spu变为spubo
        List<SpuBo> list = pageInfo.getResult().stream().map(spu -> {
            SpuBo spuBo = new SpuBo();
            //1.属性拷贝
            BeanUtils.copyProperties(spu, spuBo);

            //2.查询spu的商品分类名称，各级分类
            List<String> nameList = categoryService.queryNameByIds(Arrays.asList(spu.getCid1(),spu.getCid2(),spu.getCid3()));
            //3.拼接名字,并存入
            spuBo.setCname(StringUtils.join(nameList, "/"));
            //4.查询品牌名称
            Brand brand = brandMapper.selectByPrimaryKey(spu.getBrandId());
            spuBo.setBname(brand.getName());
            return spuBo;
        }).collect(Collectors.toList());

        return new PageResult<>(pageInfo.getTotal(),list);

    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveGoods(SpuBo spu) {
        //保存spu
        spu.setSaleable(true);
        spu.setValid(true);
        spu.setCreateTime(new Date());
        spu.setLastUpdateTime(spu.getCreateTime());
        this.spuMapper.insert(spu);

        //保存spu详情
        SpuDetail spuDetail = spu.getSpuDetail();
        spuDetail.setSpuId(spu.getId());
        this.spuDetailMapper.insert(spuDetail);

        //保存sku和库存信息
        saveSkuAndStock(spu.getSkus(),spu.getId());

        //发送消息到mq
        amqpTemplate.convertAndSend("item.insert", spu.getId());
    }

    private void saveSkuAndStock(List<Sku> skus, Long id) {
        for (Sku sku : skus){
            if (!sku.getEnable()){
                continue;
            }
            //保存sku
            sku.setSpuId(id);
            //默认不参加任何促销
            sku.setCreateTime(new Date());
            sku.setLastUpdateTime(sku.getCreateTime());
            this.skuMapper.insert(sku);

            //保存库存信息
            Stock stock = new Stock();
            stock.setSkuId(sku.getId());
            stock.setStock(sku.getStock());
            this.stockMapper.insert(stock);
        }
    }

    @Override
    public SpuBo queryGoodsById(Long id) {
        /**
         * 第一页所需信息如下：
         * 1.商品的分类信息、所属品牌、商品标题、商品卖点（子标题）
         * 2.商品的包装清单、售后服务
         */
        Spu spu=this.spuMapper.selectByPrimaryKey(id);
        SpuDetail spuDetail = this.spuDetailMapper.selectByPrimaryKey(spu.getId());

        Example example = new Example(Sku.class);
        example.createCriteria().andEqualTo("spuId",spu.getId());
        List<Sku> skuList = this.skuMapper.selectByExample(example);
        List<Long> skuIdList = new ArrayList<>();
        for (Sku sku : skuList){
            skuIdList.add(sku.getId());
        }

        List<Stock> stocks = this.stockMapper.selectByIdList(skuIdList);

        for (Sku sku:skuList){
            for (Stock stock : stocks){
                if (sku.getId().equals(stock.getSkuId())){
                    sku.setStock(stock.getStock());
                }
            }
        }

        SpuBo spuBo = new SpuBo(spu.getBrandId(),spu.getCid1(),spu.getCid2(),spu.getCid3(),spu.getTitle(),
                spu.getSubTitle(),spu.getSaleable(),spu.getValid(),spu.getCreateTime(),spu.getLastUpdateTime());
        spuBo.setSpuDetail(spuDetail);
        spuBo.setSkus(skuList);
        return spuBo;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateGoods(SpuBo spuBo) {
        /**
         * 更新策略：
         *      1.判断tb_spu_detail中的spec_template字段新旧是否一致
         *      2.如果一致说明修改的只是库存、价格和是否启用，那么就使用update
         *      3.如果不一致，说明修改了特有属性，那么需要把原来的sku全部删除，然后添加新的sku
         */

        //更新spu
        spuBo.setSaleable(true);
        spuBo.setValid(true);
        spuBo.setLastUpdateTime(new Date());
        this.spuMapper.updateByPrimaryKeySelective(spuBo);

        //更新spu详情
        SpuDetail spuDetail = spuBo.getSpuDetail();
        String oldTemp = this.spuDetailMapper.selectByPrimaryKey(spuBo.getId()).getGenericSpec();
        if (spuDetail.getGenericSpec().equals(oldTemp)){
            //相等，sku update
            //更新sku和库存信息
            updateSkuAndStock(spuBo.getSkus(),spuBo.getId(),true);
        }else {
            //不等，sku insert
            //更新sku和库存信息
            updateSkuAndStock(spuBo.getSkus(),spuBo.getId(),false);
        }
        spuDetail.setSpuId(spuBo.getId());
        this.spuDetailMapper.updateByPrimaryKeySelective(spuDetail);

        //发送消息到mq
        amqpTemplate.convertAndSend("item.update", spuBo.getId());
    }

    private void updateSkuAndStock(List<Sku> skus,Long id,boolean tag) {
        //通过tag判断是insert还是update
        //获取当前数据库中spu_id = id的sku信息
        Example e = new Example(Sku.class);
        e.createCriteria().andEqualTo("spuId",id);
        //oldList中保存数据库中spu_id = id 的全部sku
        List<Sku> oldList = this.skuMapper.selectByExample(e);
        if (tag){
            /**
             * 判断是更新时是否有新的sku被添加：如果对已有数据更新的话，则此时oldList中的数据和skus中的ownSpec是相同的，否则则需要新增
             */
            int count = 0;
            for (Sku sku : skus){
                if (!sku.getEnable()){
                    continue;
                }
                for (Sku old : oldList){
                    if (sku.getOwnSpec().equals(old.getOwnSpec())){
                        System.out.println("更新");
                        //更新
                        List<Sku> list = this.skuMapper.select(old);
                        if (sku.getPrice() == null){
                            sku.setPrice(0L);
                        }
                        if (sku.getStock() == null){
                            sku.setStock(0L);
                        }
                        sku.setId(list.get(0).getId());
                        sku.setCreateTime(list.get(0).getCreateTime());
                        sku.setSpuId(list.get(0).getSpuId());
                        sku.setLastUpdateTime(new Date());
                        this.skuMapper.updateByPrimaryKey(sku);
                        //更新库存信息
                        Stock stock = new Stock();
                        stock.setSkuId(sku.getId());
                        stock.setStock(sku.getStock());
                        this.stockMapper.updateByPrimaryKeySelective(stock);
                        //从oldList中将更新完的数据删除
                        oldList.remove(old);
                        break;
                    }else{
                        //新增
                        count ++ ;
                    }
                }
                if (count == oldList.size() && count != 0){
                    //当只有一个sku时，更新完因为从oldList中将其移除，所以长度变为0，所以要需要加不为0的条件
                    List<Sku> addSku = new ArrayList<>();
                    addSku.add(sku);
                    saveSkuAndStock(addSku,id);
                    count = 0;
                }else {
                    count =0;
                }
            }
            //处理脏数据
            if (oldList.size() != 0){
                for (Sku sku : oldList){
                    this.skuMapper.deleteByPrimaryKey(sku.getId());
                    Example example = new Example(Stock.class);
                    example.createCriteria().andEqualTo("skuId",sku.getId());
                    this.stockMapper.deleteByExample(example);
                }
            }
        }else {
            List<Long> ids = oldList.stream().map(Sku::getId).collect(Collectors.toList());
            //删除以前的库存
            Example example = new Example(Stock.class);
            example.createCriteria().andIn("skuId",ids);
            this.stockMapper.deleteByExample(example);
            //删除以前的sku
            Example example1 = new Example(Sku.class);
            example1.createCriteria().andEqualTo("spuId",id);
            this.skuMapper.deleteByExample(example1);
            //新增sku和库存
            saveSkuAndStock(skus,id);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteGoods(long id) {
        //删除spu表中的数据
        this.spuMapper.deleteByPrimaryKey(id);

        //删除spu_detail中的数据
        Example example = new Example(SpuDetail.class);
        example.createCriteria().andEqualTo("spuId",id);
        this.spuDetailMapper.deleteByExample(example);


        List<Sku> skuList = this.skuMapper.selectByExample(example);
        for (Sku sku : skuList){
            //删除sku中的数据
            this.skuMapper.deleteByPrimaryKey(sku.getId());
            //删除stock中的数据
            this.stockMapper.deleteByPrimaryKey(sku.getId());
        }

        //发送消息到mq
        amqpTemplate.convertAndSend("item.delete", id);
    }

    @Override
    public SpuDetail querySpuDetailBySpuId(long id) {
        return this.spuDetailMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<Sku> querySkuBySpuId(Long id) {
        Example example = new Example(Sku.class);
        example.createCriteria().andEqualTo("spuId", id);
        return this.skuMapper.selectByExample(example);
    }

    @Override
    public SpuBo getSpuBoCasche(Long id) {
        // 缓存维度化解决方案将 spuBo 拆开存储避免浪费缓存资源
        // 查询 spu 信息
        String spukey = REDIS_SPU_KEY_PRE + id;
        String spuStr = stringRedisTemplate.opsForValue().get(spukey);
        Spu spu = JSONObject.toJavaObject(JSON.parseObject(spuStr), SpuBo.class);

        // 查询 spuDetail 信息
        String spuDetailKey = REDIS_SPU_DETAIL_KEY_PRE + id;
        String spuDetailStr = stringRedisTemplate.opsForValue().get(spuDetailKey);
        SpuDetail spuDetail = JSONObject.toJavaObject(JSON.parseObject(spuDetailStr), SpuDetail.class);

        // 查询 skuLis 信息
        String skuListKey = REDIS_SKU_KEY_PRE + id;
        Long size = listRedisTemplate.size(skuListKey);
        List<Sku> skuList = listRedisTemplate.range(skuListKey, 0, size-1);

        SpuBo spuBo = new SpuBo(spu.getBrandId(),spu.getCid1(),spu.getCid2(),spu.getCid3(),spu.getTitle(),
                spu.getSubTitle(),spu.getSaleable(),spu.getValid(),spu.getCreateTime(),spu.getLastUpdateTime());
        spuBo.setSpuDetail(spuDetail);
        spuBo.setSkus(skuList);
        return spuBo;
    }

    @Override
    public void updateSpuBoCasche(SpuBo spuBo) {
        Spu spu = DozerUtils.map(spuBo, Spu.class);
        String spuStr = JSONObject.toJSONString(spu);
        String spuKey = REDIS_SPU_KEY_PRE + spuBo.getId();
        stringRedisTemplate.opsForValue().set(spuKey, spuStr);

        String spuDetailStr = JSONObject.toJSONString(spuBo.getSpuDetail());
        String spuDetailKey = REDIS_SPU_DETAIL_KEY_PRE + spuBo.getId();
        stringRedisTemplate.opsForValue().set(spuDetailKey, spuDetailStr);

        String skuListKey = REDIS_SKU_KEY_PRE + spuBo.getId();
        listRedisTemplate.leftPushAll(skuListKey, spuBo.getSkus());
    }

    @Override
    public Boolean deleteSpuBoCasche(Long id) {
        String spuKey = REDIS_SPU_KEY_PRE + id;
        Boolean spuBoDelete = stringRedisTemplate.delete(spuKey);

        String spuDetailKey = REDIS_SPU_DETAIL_KEY_PRE + id;
        stringRedisTemplate.delete(spuDetailKey);

        String skuListKey = REDIS_SKU_KEY_PRE + id;
        listRedisTemplate.leftPop(skuListKey);

        return spuBoDelete;
    }





}

