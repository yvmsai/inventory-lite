package com.inventorylite;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class InventoryLiteApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventoryLiteApiApplication.class, args);
	}

}
