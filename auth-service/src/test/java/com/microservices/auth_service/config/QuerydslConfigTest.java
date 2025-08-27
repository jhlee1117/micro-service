package com.microservices.auth_service.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import com.querydsl.jpa.impl.JPAQueryFactory;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.datasource.driverClassName=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class QuerydslConfigTest {

    @Autowired
    private JPAQueryFactory jpaQueryFactory;

    @Test
    void testJpaQueryFactoryBean() {
        // JPAQueryFactory가 제대로 주입되는지 확인
        assertNotNull(jpaQueryFactory, "JPAQueryFactory should not be null");
    }
} 