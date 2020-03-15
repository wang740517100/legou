package cn.wangkf.config;

import cn.wangkf.auth.utils.RsaUtils;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.annotation.PostConstruct;
import java.security.PublicKey;

/**
 * @Feature: jwt属性
 *
 */
@Data
@ConfigurationProperties(prefix = "legou.jwt")
public class JwtProperties {
    /**
     * 公钥
     */
    private PublicKey publicKey;

    /**
     * 公钥地址
     */
    private String pubKeyPath;

    /**
     * cookie名字
     */
    private String cookieName;

    private static final Logger logger = LoggerFactory.getLogger(JwtProperties.class);



    public static Logger getLogger() {
        return logger;
    }

    /**
     * @PostConstruct :在构造方法执行之后执行该方法
     */
    @PostConstruct
    public void init(){
        try {
            // 获取公钥
            this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        } catch (Exception e) {
            logger.error("获取公钥失败！", e);
            throw new RuntimeException();
        }
    }
}
