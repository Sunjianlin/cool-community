package com.cool.server.config;

import com.cool.common.properties.AliOssProperties;
import com.cool.common.properties.JwtProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({JwtProperties.class, AliOssProperties.class})
public class PropertiesConfig {
}
