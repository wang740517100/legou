package cn.wangkf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 *文件服务入口
 */
@SpringBootApplication
@EnableDiscoveryClient
public class FileService {
    public static void main(String[] args) {
        SpringApplication.run(FileService.class, args);
    }
}
