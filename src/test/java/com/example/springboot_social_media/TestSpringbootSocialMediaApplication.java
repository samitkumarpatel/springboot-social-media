package com.example.springboot_social_media;

import org.springframework.boot.SpringApplication;

public class TestSpringbootSocialMediaApplication {

	public static void main(String[] args) {
		SpringApplication.from(SpringbootSocialMediaApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
