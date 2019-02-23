package cn.wangkf.item.api;

import cn.wangkf.item.pojo.Brand;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;


@RequestMapping("brand")
public interface BrandApi {
    /**
     * 根据品牌id结合，查询品牌信息
     * @param ids
     * @return
     */
    @GetMapping("list")
    List<Brand> queryBrandByIds(@RequestParam("ids") List<Long> ids);
}
