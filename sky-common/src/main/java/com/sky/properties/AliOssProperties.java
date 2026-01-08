package com.sky.properties;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@ConfigurationProperties(prefix = "sky.alioss")//配置读取类
@Data
@Slf4j
public class AliOssProperties {

    private String endpoint;
    private String accessKeyId;
    private String accessKeySecret;
    private String bucketName;
    @PostConstruct
    public void init() {
        log.info("阿里云配置类加载完成，配置信息：endpoint={}, bucketName={}", endpoint, bucketName);
    }
}


