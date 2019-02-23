package cn.wangkf.item;


import tk.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;


@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("cn.wangkf.item.mapper")
public class LgItemService {
    public static void main(String[] args) {
        SpringApplication.run(LgItemService.class,args);
    }
}
