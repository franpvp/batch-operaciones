package com.example.bff_operaciones;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class BffOperacionesApplication {

	public static void main(String[] args) {
		SpringApplication.run(BffOperacionesApplication.class, args);
	}

}
