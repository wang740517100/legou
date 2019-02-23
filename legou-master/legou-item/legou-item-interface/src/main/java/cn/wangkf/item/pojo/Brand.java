package cn.wangkf.item.pojo;

import lombok.Data;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by wangk on 2019-01-05.
 */
@Data
@Table(name="tb_brand")
public class Brand {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY) //自增celue
    private Long id;
    private String name;
    private String image;
    private Character letter;



}
