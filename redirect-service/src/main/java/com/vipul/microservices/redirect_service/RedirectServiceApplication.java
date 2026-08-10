package com.vipul.microservices.redirect_service;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableFeignClients
@SpringBootApplication
public class RedirectServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(RedirectServiceApplication.class, args);
	}

}
