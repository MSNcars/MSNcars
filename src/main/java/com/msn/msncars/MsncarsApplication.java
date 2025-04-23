package com.msn.msncars;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MsncarsApplication {

	public static void main(String[] args) {
		SpringApplication.run(MsncarsApplication.class, args);
	}

}
