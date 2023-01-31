package com.jvc.interconnectingflights.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.jvc.interconnectingflights")
public class InterconnectingFlightsApplication {
	public static void main(String[] args) {
		SpringApplication.run(InterconnectingFlightsApplication.class, args);
	}

}
