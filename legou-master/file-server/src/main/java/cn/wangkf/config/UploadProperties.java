package cn.wangkf.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Created by wangk on 2019-01-06.
 */
@Data
@ConfigurationProperties("lg.upload")
public class UploadProperties {
    private String baseUrl;
    private String filePath;
    private List<String> allowTypes;

}
