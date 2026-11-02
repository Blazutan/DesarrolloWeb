package com.poli.polirestaurante;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class PolirestauranteApplication {

	public static void main(String[] args) {
		SpringApplication.run(PolirestauranteApplication.class, args);
	}

}
