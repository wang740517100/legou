package cn.wangkf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Created by wangk on 2019-01-16.
 */

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class LgSearchService {
    public static void main(String[] args) {
        SpringApplication.run(LgSearchService.class, args);
    }

}
