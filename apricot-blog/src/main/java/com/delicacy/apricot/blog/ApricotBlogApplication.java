package com.delicacy.apricot.blog;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApricotBlogApplication {

	public static void main(String[] args) {
		System.setProperty("server.port","8083");
		SpringApplication.run(ApricotBlogApplication.class, args);
	}

}

