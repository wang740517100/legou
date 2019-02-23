package cn.wangkf.item.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;


@RequestMapping("spec")
public interface SpecApi {
    /**
     * 查询商品分类对应的规格参数模板
     * @param id
     * @return
     */
    @GetMapping("{id}")
    ResponseEntity<String> querySpecificationByCategoryId(@PathVariable("id") Long id);
}
