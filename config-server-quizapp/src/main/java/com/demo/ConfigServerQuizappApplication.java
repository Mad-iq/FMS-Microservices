package com.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@SpringBootApplication
@EnableConfigServer
public class ConfigServerQuizappApplication {

	public static void main(String[] args) {
		SpringApplication.run(ConfigServerQuizappApplication.class, args);
	}

}
