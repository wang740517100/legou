package cn.wangkf.user;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import tk.mybatis.spring.annotation.MapperScan;

/**
 * Created by wangk on 2019-01-20.
 */
@SpringBootApplication
@EnableDiscoveryClient
@MapperScan("cn.wangkf.user.mapper")
public class LgUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(LgUserApplication.class, args);
    }
}
