package com.akshadip.flowguard.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

/**
 * FlowGuard Application - Request rate limiting service
 * Author: Akshadip
 */
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class }) //just to disable the default basic security of Spring
public class FlowGuardApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlowGuardApplication.class, args);
	}

}
