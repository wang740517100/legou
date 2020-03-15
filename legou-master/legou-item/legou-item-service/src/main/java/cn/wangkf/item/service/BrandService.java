package cn.wangkf.item.service;

import cn.wangkf.common.vo.PageResult;
import cn.wangkf.item.pojo.Brand;
import java.util.List;

/**
 * Created by wangk on 2019-01-05.
 */
public interface BrandService {

    /**
     * 分页查询品牌
     * @param page
     * @param rows
     * @param sortBy
     * @param desc
     * @param key
     * @return
     */
    public PageResult<Brand> queryBrandByPage(Integer page, Integer rows, String sortBy, Boolean desc, String key);

    /**
     * 新增brand,并且维护中间表
     * @param brand
     * @param cids
     */
    void saveBrand(Brand brand, List<Long> cids);

    /**
     * 删除brand,并且维护中间表
     * @param id
     */
    void deleteBrand(Long id);

    /**
     * 删除中间表
     * @param bid
     */
    void deleteByBrandIdInCategoryBrand(Long bid);

    /**
     * 品牌更新
     * @param brand
     * @param categories
     */
    void updateBrand(Brand brand,List<Long> categories);

    /**
     * 根据category id查询brand
     * @param cid
     * @return
     */
    List<Brand> queryBrandByCategoryId(Long cid);

    /**
     * 根据品牌id集合查询品牌信息
     * @param ids
     * @return
     */
    List<Brand> queryBrandByBrandIds(List<Long> ids);
}
