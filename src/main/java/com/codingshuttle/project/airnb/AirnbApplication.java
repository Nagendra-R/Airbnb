package com.codingshuttle.project.airnb;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AirnbApplication {

	public static void main(String[] args) {
		SpringApplication.run(AirnbApplication.class, args);
	}

}
