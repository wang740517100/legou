package cn.wangkf.pojo;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.sql.DataSourceDefinition;

@Data
@ConfigurationProperties(prefix = "legou.sms")
public class SmsProperties {

    private String accessKeyId;

    private String accessKeySecret;

    private String signName;

    private String verifyCodeTemplate;


}
