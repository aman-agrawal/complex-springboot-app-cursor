package com.example.complexapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "com.example.complexapp.repository")
@EnableElasticsearchRepositories(basePackages = "com.example.complexapp.search")
@EnableFeignClients(basePackages = "com.example.complexapp.client")
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
public class ComplexSpringBootApplication {

    public static void main(String[] args) {
        SpringApplication.run(ComplexSpringBootApplication.class, args);
    }
}
