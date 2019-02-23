package cn.wangkf.client;

import cn.wangkf.item.api.SpecApi;
import org.springframework.cloud.openfeign.FeignClient;


@FeignClient(value = "item-service")
public interface SpecClient extends SpecApi {
}
