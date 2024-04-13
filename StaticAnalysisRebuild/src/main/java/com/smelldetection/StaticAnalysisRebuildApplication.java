package com.smelldetection;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.smelldetection.mapper")
public class StaticAnalysisRebuildApplication {

    public static void main(String[] args) {
        SpringApplication.run(StaticAnalysisRebuildApplication.class, args);
    }

}
