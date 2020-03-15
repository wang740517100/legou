package cn.wangkf.client;

import cn.wangkf.item.api.GoodsApi;
import org.springframework.cloud.openfeign.FeignClient;


@FeignClient(value = "item-service")
public interface GoodsClient extends GoodsApi {
}
