package com.dwp.rest.webservices.DWPrestfulwebservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class DwpRestfulWebServicesApplication {
	
	@Bean
	public RestTemplate getTemplate() {
		return new RestTemplate();
	}
	
	public static void main(String[] args) throws Exception  {
		SpringApplication.run(DwpRestfulWebServicesApplication.class, args);
	}
	
}
