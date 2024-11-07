package com.julius.julius;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JuliusApplication {

	public static void main(String[] args) {
		SpringApplication.run(JuliusApplication.class, args);
	}
}