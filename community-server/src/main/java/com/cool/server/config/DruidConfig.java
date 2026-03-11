package com.cool.server.config;

import com.alibaba.druid.filter.stat.StatFilter;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot3.autoconfigure.DruidDataSourceBuilder;
import com.alibaba.druid.support.jakarta.StatViewServlet;
import com.alibaba.druid.support.jakarta.WebStatFilter;
import com.alibaba.druid.wall.WallConfig;
import com.alibaba.druid.wall.WallFilter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class DruidConfig {

    // 1. 修复数据源配置：先初始化 WallConfig，再绑定到 WallFilter
    @Primary
    @Bean
    @ConfigurationProperties("spring.datasource.druid")
    public DataSource druidDataSource() {
        DruidDataSource dataSource = (DruidDataSource) DruidDataSourceBuilder.create().build();

        // ====== 修复 WallFilter 空指针核心：手动创建 WallConfig 并绑定 ======
        WallConfig wallConfig = new WallConfig(); // 手动初始化 Config
        wallConfig.setMultiStatementAllow(false); // 设置防注入规则
        wallConfig.setDropTableAllow(false);
        wallConfig.setAlterTableAllow(false);

        WallFilter wallFilter = new WallFilter();
        wallFilter.setConfig(wallConfig); // 将 Config 绑定到 Filter（关键！）

        // StatFilter 配置（保留）
        StatFilter statFilter = new StatFilter();
        statFilter.setSlowSqlMillis(500);
        statFilter.setLogSlowSql(true);
        statFilter.setMergeSql(true);

        // 注册过滤器
        dataSource.setProxyFilters(Arrays.asList(statFilter, wallFilter));
        return dataSource;
    }

    // 2. 监控页面 Servlet 配置（保留）
    @Bean
    public ServletRegistrationBean<StatViewServlet> statViewServlet() {
        ServletRegistrationBean<StatViewServlet> bean = new ServletRegistrationBean<>(new StatViewServlet(), "/druid/*");
        Map<String, String> initParams = new HashMap<>();
        initParams.put("loginUsername", "admin");
        initParams.put("loginPassword", "admin123");
        initParams.put("allow", "127.0.0.1");
        initParams.put("resetEnable", "false");
        bean.setInitParameters(initParams);
        return bean;
    }

    // 3. Web 监控 Filter 配置（保留）
    @Bean
    public FilterRegistrationBean<WebStatFilter> webStatFilter() {
        FilterRegistrationBean<WebStatFilter> bean = new FilterRegistrationBean<>(new WebStatFilter());
        bean.setUrlPatterns(Arrays.asList("/*"));
        Map<String, String> initParams = new HashMap<>();
        initParams.put("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid/*");
        bean.setInitParameters(initParams);
        return bean;
    }
}
