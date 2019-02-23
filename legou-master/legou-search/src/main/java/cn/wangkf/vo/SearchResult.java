package cn.wangkf.vo;

import cn.wangkf.common.vo.PageResult;
import cn.wangkf.item.pojo.Category;
import cn.wangkf.item.pojo.Brand;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class SearchResult<Goods> extends PageResult<Goods> {

    /**
     * 分类的集合
     */
    private List<Category> categories;

    /**
     * 品牌的集合
     */
    private List<Brand> brands;

    /**
     * 规格参数的过滤条件
     */
    private List<Map<String,Object>> specs;


    public SearchResult(List<Category> categories, List<Brand> brands, List<Map<String,Object>> specs){
        this.categories = categories;
        this.brands = brands;
        this.specs = specs;
    }

    public SearchResult(Long total, List<Goods> item,List<Category> categories, List<Brand> brands, List<Map<String,Object>> specs){
        super(total,item);
        this.categories = categories;
        this.brands = brands;
        this.specs = specs;
    }

    public SearchResult(Long total,Long totalPage, List<Goods> item,List<Category> categories, List<Brand> brands){
        super(total,totalPage,item);
        this.categories = categories;
        this.brands = brands;
    }

    public SearchResult(Long total,Long totalPage, List<Goods> item,List<Category> categories,
                        List<Brand> brands, List<Map<String,Object>> specs){
        super(total,totalPage,item);
        this.categories = categories;
        this.brands = brands;
        this.specs = specs;
    }
}
