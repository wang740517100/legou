package cn.wangkf.common.parameter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BrandQueryByPageParameter {

    /*
    *   - page：当前页，int
        - rows：每页大小，int
        - sortBy：排序字段，String
        - desc：是否为降序，boolean
        - key：搜索关键词，String
    * */

    private Integer page;
    private Integer rows;
    private String sortBy;
    private Boolean desc;
    private String key;


}
