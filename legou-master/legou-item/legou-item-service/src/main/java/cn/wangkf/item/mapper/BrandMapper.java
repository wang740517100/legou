package cn.wangkf.item.mapper;

import cn.wangkf.item.pojo.Brand;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.mybatis.spring.annotation.MapperScan;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * Created by wangk on 2019-01-05.
 */
public interface BrandMapper extends Mapper<Brand>, SelectByIdListMapper<Brand,Long> {

    @Insert("INSERT INTO tb_category_brand (category_id, brand_id) values (#{cid}, #{bid})")
    public int insertCategoryBrand(@Param("cid") Long cid, @Param("bid") Long bid);

    /**
     * 根据brand id删除中间表相关数据
     * @param bid
     */
    @Delete("DELETE FROM tb_category_brand WHERE brand_id = #{bid}")
    void deleteByBrandIdInCategoryBrand(@Param("bid") Long bid);

    /**
     * 根据category id查询brand,左连接
     * @param cid
     * @return
     */
    @Select("SELECT b.* FROM tb_brand b LEFT JOIN tb_category_brand cb ON b.id=cb.brand_id WHERE cb.category_id=#{cid}")
    List<Brand> queryBrandByCategoryId(Long cid);
}
