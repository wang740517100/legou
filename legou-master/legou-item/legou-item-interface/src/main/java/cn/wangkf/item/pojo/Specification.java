package cn.wangkf.item.pojo;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Table(name = "tb_specification")
public class Specification {

    @Id
    private Long categoryId;

    private String specifications;


}