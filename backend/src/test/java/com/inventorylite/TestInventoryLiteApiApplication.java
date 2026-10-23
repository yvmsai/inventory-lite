package com.inventorylite;

import org.springframework.boot.SpringApplication;

public class TestInventoryLiteApiApplication {

	public static void main(String[] args) {
		SpringApplication.from(InventoryLiteApiApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
