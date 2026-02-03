package com.benny.board_mate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class BoardMateApplication {

	public static void main(String[] args) {
		SpringApplication.run(BoardMateApplication.class, args);
	}
}