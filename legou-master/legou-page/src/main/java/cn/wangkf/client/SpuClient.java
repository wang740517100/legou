package cn.wangkf.client;

import cn.wangkf.item.api.SpuApi;
import org.springframework.cloud.openfeign.FeignClient;


@FeignClient(value = "item-service")
public interface SpuClient extends SpuApi {
}
