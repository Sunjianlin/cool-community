package com.cool.server.security.config;

import com.cool.common.utils.JwtUtil;
import com.cool.server.context.BaseContext;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ContextConfig {
    @Autowired
    private JwtUtil jwtUtil;

    @PostConstruct
    public void initBaseContext() {
        BaseContext.setJwtUtil(jwtUtil);
    }
}