package cn.wangkf.item.service.impl;

import cn.wangkf.item.service.SpecificationService;
import cn.wangkf.item.mapper.SpecificationMapper;
import cn.wangkf.item.pojo.Specification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class SpecificationServiceImpl implements SpecificationService {

    @Autowired
    private SpecificationMapper specificationMapper;

    @Override
    public Specification queryById(Long id) {
        return this.specificationMapper.selectByPrimaryKey(id);
    }

    @Override
    public void saveSpecification(Specification specification) {
        this.specificationMapper.insert(specification);
    }

    @Override
    public void updateSpecification(Specification specification) {
        this.specificationMapper.updateByPrimaryKeySelective(specification);
    }

    @Override
    public void deleteSpecification(Specification specification) {
        this.specificationMapper.deleteByPrimaryKey(specification);
    }
}
