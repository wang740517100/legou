package cn.wangkf.item.controller;

import cn.wangkf.common.vo.PageResult;
import cn.wangkf.item.request.GoodsRefreshCacheRequest;
import cn.wangkf.item.request.GoodsUpdateCacheRequest;
import cn.wangkf.item.request.Request;
import cn.wangkf.item.service.GoodsService;
import cn.wangkf.item.service.RequestRouteService;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import cn.wangkf.item.bo.*;
import cn.wangkf.common.parameter.SpuQueryByPageParameter;
import org.springframework.http.HttpStatus;
import cn.wangkf.item.pojo.SpuDetail;
import cn.wangkf.item.pojo.Sku;

import java.util.List;

@RestController
@RequestMapping("goods")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;

    @Autowired
    private RequestRouteService requestRouteService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 分页查询
     * @param page
     * @param rows
     * @param sortBy
     * @param desc
     * @param key
     * @param saleable
     * @return
     */
    @GetMapping("/spu/page")
    public ResponseEntity<PageResult<SpuBo>> querySpuByPage(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "rows", defaultValue = "5") Integer rows,
            @RequestParam(value = "sortBy", required = false) String sortBy,
            @RequestParam(value = "desc", defaultValue = "false") Boolean desc,
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "saleable",defaultValue = "true") Boolean saleable){
        SpuQueryByPageParameter spuQueryByPageParameter = new SpuQueryByPageParameter(page,rows,sortBy,desc,key,saleable);
        //分页查询spu信息
        PageResult<SpuBo> result = this.goodsService.queryGoodsWithPage(spuQueryByPageParameter);
        return ResponseEntity.ok(result);
    }

    /**
     * 保存商品
     * @param spu
     * @return
     */
    @PostMapping
    public ResponseEntity<Void> saveGoods(@RequestBody SpuBo spu){
        this.goodsService.saveGoods(spu);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 根据id查询商品
     * @param id
     * @return
     */
    @GetMapping("/spu/{id}")
    public ResponseEntity<SpuBo> queryGoodsById(@PathVariable("id") Long id) {
        SpuBo spuBo = this.goodsService.queryGoodsById(id);
        if (spuBo == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } else {
            return ResponseEntity.ok(spuBo);
        }

    }


    /**
     * 修改商品
     * @param spuBo
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updateGoods(@RequestBody SpuBo spuBo){
        this.goodsService.updateGoods(spuBo);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();

    }


    /**
     * 删除商品
     * @param ids
     * @return
     */
    @DeleteMapping("/spu/{id}")
    public ResponseEntity<Void> deleteGoods(@PathVariable("id") String ids){
        String separator="-";
        if (ids.contains(separator)){
            String[] goodsId = ids.split(separator);
            for (String id:goodsId){
                this.goodsService.deleteGoods(Long.parseLong(id));
            }
        }
        else {
            this.goodsService.deleteGoods(Long.parseLong(ids));
        }
        return ResponseEntity.status(HttpStatus.OK).build();
    }

    /**
     * 根据Spu的id查询其下所有的sku
     * @param id
     * @return
     */
    @GetMapping("/sku/list/{id}")
    public ResponseEntity<List<Sku>> querySkuBySpuId(@PathVariable("id") Long id){
        List<Sku> list = this.goodsService.querySkuBySpuId(id);
        return ResponseEntity.ok(list);
    }

    /**
     * 根据spu商品id查询详情
     * @param id
     * @return
     */
    @GetMapping("/spu/detail/{id}")
    public ResponseEntity<SpuDetail> querySpuDetailBySpuId(@PathVariable("id") Long id){
        SpuDetail spuDetail = this.goodsService.querySpuDetailBySpuId(id);
        if (spuDetail == null){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }else {
            return ResponseEntity.ok(spuDetail);
        }
    }








    /**
     * 根据id查询商品，回写 redis cluster
     * @param id
     * @return
     */
    @GetMapping("/cache/spu/{id}")
    public ResponseEntity<SpuBo> queryGoodsByCache(@PathVariable("id") Long id){
        SpuBo spuBo = null;
        try{

            // 添加到 queue 里面等待执行查询
            Request request = new GoodsRefreshCacheRequest(id, goodsService, false);
            requestRouteService.process(request);

            long startTime = System.currentTimeMillis();
            long endTime = 0L, waitTime = 0L;
            while (true) {

                // 等待200ms
                if(waitTime > 200) {
                    break;
                }

                // 去缓存中查询信息
                spuBo = goodsService.getSpuBoCasche(id);

                if (spuBo != null) {
                    return ResponseEntity.ok(spuBo);
                } else {
                    Thread.sleep(20);
                    endTime = System.currentTimeMillis();
                    waitTime = endTime - startTime;
                }
            }

            // 当 redis 没有该商品时：1、mysql 里面有就更新到 redis  2、mysql 里面没有就直接返回
            spuBo = this.goodsService.queryGoodsById(id);
            if (spuBo != null) {
                // 刷新到
                request = new GoodsRefreshCacheRequest(id, goodsService, true);
                requestRouteService.process(request);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.ok(spuBo);
    }

    /**
     * 修改商品和删除缓存
     * @param spuBo
     * @return
     */
    @PutMapping
    public ResponseEntity<Void> updateGoodsByCache(@RequestBody SpuBo spuBo){
        Request request = new GoodsUpdateCacheRequest(spuBo, goodsService, false);
        requestRouteService.process(request);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }


}
