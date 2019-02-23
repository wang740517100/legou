package cn.wangkf.client;

import cn.wangkf.item.api.BrandApi;
import org.springframework.cloud.openfeign.FeignClient;


@FeignClient(value = "item-service")
public interface BrandClient extends BrandApi {
}
