package cn.wangkf.item.pojo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Table(name="tb_spu_detail")
public class SpuDetail {
    @Id
    private Long spuId;

    private String description;

    //商品特殊规格的名称及可选值模板
    private String genericSpec;

    //商品的全局规格属性
    private String specialSpec;

    //包装清单
    private String packingList;

    //售后服务
    private String afterService;

}