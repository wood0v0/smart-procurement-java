package com.example.procurement;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
@MapperScan(basePackages = "com.example.procurement", annotationClass = Mapper.class)
public class ProcurementApplication {

  public static void main(String[] args) {
    SpringApplication.run(ProcurementApplication.class, args);
  }
}
