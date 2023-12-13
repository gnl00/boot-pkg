package com.demo.lite;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Map;

/**
 * PgkLiteTest
 *
 * @author gnl
 * @since 2023/5/10
 */
@SpringBootTest
public class PgkLiteTest {
    @Autowired
    private ConfigurableApplicationContext ac;

    @Test
    public void test() {
        Map<String, Object> properties = ac.getEnvironment().getSystemProperties();
        System.out.println(properties.get("java.class.path"));
    }
}
