package com.enterprise.ong_pet2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class OngPet2Application {

	public static void main(String[] args) {
		SpringApplication.run(OngPet2Application.class, args);
	}

}
