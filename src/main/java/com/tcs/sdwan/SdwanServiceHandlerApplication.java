package com.tcs.sdwan;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.tcs")
public class SdwanServiceHandlerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SdwanServiceHandlerApplication.class, args);
	}

}
