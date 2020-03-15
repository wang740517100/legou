package cn.wangkf.common.vo;

import lombok.Data;

/**
 * Created by wangk on 2019-01-05.
 */
@Data
public class PageParam {
    private Integer page;
    private Integer rows;
    private String sortBy;
    private Boolean desc;
    private String key;
}
