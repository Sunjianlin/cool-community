package com.cool.server.config;

import org.springframework.context.annotation.Configuration;
import org.mybatis.spring.annotation.MapperScan;

@Configuration
@MapperScan("com.cool.server.mapper")
public class MybatisConfig {
}
