package cn.wangkf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;


/**
 * thymeleaf
 * 商品详情页（小型电商）模板引擎静态化渲染服务
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
public class LgPageApplication {
    public static void main(String[] args) {
        SpringApplication.run(LgPageApplication.class,args);
    }
}
