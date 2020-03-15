package cn.wangkf.auth.client;

import cn.wangkf.user.api.UserApi;
import org.springframework.cloud.openfeign.FeignClient;


@FeignClient(value = "user-service")
public interface UserClient extends UserApi {
}
