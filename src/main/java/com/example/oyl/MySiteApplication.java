package com.example.oyl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling  // 스케줄러 동작
public class MySiteApplication {

	public static void main(String[] args) {

		SpringApplication.run(MySiteApplication.class, args);
	}

}
