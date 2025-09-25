package com.example.oyl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@SpringBootApplication
@EnableScheduling  // 스케줄러 동작
@EnableMethodSecurity
public class MySiteApplication {

	public static void main(String[] args) {

		SpringApplication.run(MySiteApplication.class, args);
	}

}
