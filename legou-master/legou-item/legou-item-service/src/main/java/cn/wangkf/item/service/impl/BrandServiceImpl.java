package cn.wangkf.item.service.impl;

import cn.wangkf.common.enums.ExceptionEnum;
import cn.wangkf.common.exception.LgException;
import cn.wangkf.common.vo.PageResult;
import cn.wangkf.item.mapper.BrandMapper;
import cn.wangkf.item.pojo.Brand;
import cn.wangkf.item.service.BrandService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * Created by wangk on 2019-01-05.
 */
@Service
public class BrandServiceImpl implements BrandService {

    @Autowired
    private BrandMapper brandMapper;

    //通用Mapper和分页助手pagehelper共同实现分页查询
    @Override
    public PageResult<Brand> queryBrandByPage(Integer page, Integer rows, String sortBy, Boolean desc, String key) {
        //1、分页
        PageHelper.startPage(page, rows);
        //2、过滤
        Example example = new Example(Brand.class);
        if (StringUtils.isNotBlank(key)) {
            //过滤条件
            example.createCriteria().orLike("name", "%"+key+"%").orEqualTo("letter", key.toUpperCase());
        }
        //3、排序
        if (StringUtils.isNotBlank(sortBy)) {
            String orderByClause = sortBy + (desc ? " DESC" : " ASC");
            example.setOrderByClause(orderByClause);
        }
        //4、查询（
        List<Brand> list = brandMapper.selectByExample(example);

        //5、分页助手解析list
        PageInfo<Brand> info = new PageInfo<>(list);
        return new PageResult<>(info.getTotal(), list);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)  //springboot事务管理
    public void saveBrand(Brand brand, List<Long> categories) {
        //新增品牌信息(通用Mapper单表插入) 成功后才有brandid
        int n = brandMapper.insertSelective(brand);
        if (n != 1) {
            throw new LgException(ExceptionEnum.BRAND_ADD_FAIL);
        }
        //新增品牌和分类中间表
        for (Long cid : categories) {
            n = brandMapper.insertCategoryBrand(cid, brand.getId());
            if (n != 1) {
                throw new LgException(ExceptionEnum.BRAND_ADD_FAIL);
            }
        }

    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBrand(Long id) {
        //删除品牌信息
        this.brandMapper.deleteByPrimaryKey(id);

        //维护中间表
        this.brandMapper.deleteByBrandIdInCategoryBrand(id);
    }


    @Override
    public void deleteByBrandIdInCategoryBrand(Long bid) {
        this.brandMapper.deleteByBrandIdInCategoryBrand(bid);
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public void updateBrand(Brand brand,List<Long> categories) {
        // 修改品牌信息
        this.brandMapper.updateByPrimaryKeySelective(brand);

        //维护品牌和分类中间表
        for (Long cid : categories) {
            this.brandMapper.insertCategoryBrand(cid, brand.getId());
        }
    }

    /**
     * 根据category id查询brand
     * @param cid
     * @return
     */
    @Override
    public List<Brand> queryBrandByCategoryId(Long cid) {
        return this.brandMapper.queryBrandByCategoryId(cid);
    }

    /**
     * 根据品牌id集合查询品牌信息
     * @param ids
     * @return
     */
    @Override
    public List<Brand> queryBrandByBrandIds(List<Long> ids) {
        return this.brandMapper.selectByIdList(ids);
    }

}
