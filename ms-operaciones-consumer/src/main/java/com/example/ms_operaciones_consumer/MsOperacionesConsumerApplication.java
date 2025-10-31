package com.example.ms_operaciones_consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MsOperacionesConsumerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsOperacionesConsumerApplication.class, args);
	}

}
