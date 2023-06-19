package com.my.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationPropertiesScan
@EntityScan
@SpringBootApplication
public class Application {

	public static final String BASE_PACKAGE_MYAPP = "com.my.api";

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}
}
