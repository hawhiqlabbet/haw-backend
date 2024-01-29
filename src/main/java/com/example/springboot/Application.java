package com.example.springboot;

import com.example.springboot.cors.CorsConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.filter.CorsFilter;

@EnableScheduling
@EnableAsync
@SpringBootApplication
@ComponentScan(basePackages = "com.example.springboot.socketIO")
public class Application {

	@Bean
	public CorsFilter corsFilter(){
		return new CorsConfig().customCorsFilter();
	}
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

}
