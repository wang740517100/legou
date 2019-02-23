package cn.wangkf.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 *
 * @Feature: 过滤白名单
 *
 */
@Data
@ConfigurationProperties(prefix = "legou.filter")
public class FilterProperties {

    private List<String> allowPaths;


}
