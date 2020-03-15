package cn.wangkf.item.service;

import cn.wangkf.item.pojo.Specification;


public interface SpecificationService {
    /**
     * 根据category id查询规格参数模板
     * @param id
     * @return
     */
    Specification queryById(Long id);

    /**
     * 添加规格参数模板
     * @param specification
     */
    void saveSpecification(Specification specification);

    /**
     * 修改规格参数模板
     * @param specification
     */
    void updateSpecification(Specification specification);

    /**
     * 删除规格参数模板
     * @param specification
     */
    void deleteSpecification(Specification specification);
}
