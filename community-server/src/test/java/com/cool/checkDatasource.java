package com.cool;



import jakarta.annotation.PostConstruct;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.sql.DataSource;

@SpringBootTest
public class checkDatasource{
    @Autowired
    private DataSource dataSource;

    @Test
    public void checkDataSource() {
        // 控制台打印数据源类型，必须是 com.alibaba.druid.pool.DruidDataSource
        System.out.println("当前数据源类型：" + dataSource.getClass().getName());
    }
}
