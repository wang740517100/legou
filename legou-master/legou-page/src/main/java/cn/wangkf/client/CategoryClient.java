package cn.wangkf.client;

import cn.wangkf.item.api.CategoryApi;
import org.springframework.cloud.openfeign.FeignClient;


@FeignClient(value = "item-service")
public interface CategoryClient extends CategoryApi {
}
