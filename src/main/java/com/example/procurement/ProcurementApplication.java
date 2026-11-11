package com.example.procurement;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.procurement")
public class ProcurementApplication {

  public static void main(String[] args) {
    SpringApplication.run(ProcurementApplication.class, args);
  }
}
