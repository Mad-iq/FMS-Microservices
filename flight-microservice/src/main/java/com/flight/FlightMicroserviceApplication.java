package com.flight;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class FlightMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlightMicroserviceApplication.class, args);
	}

}
