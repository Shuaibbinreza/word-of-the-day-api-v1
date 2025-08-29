package com.wordOfTheDayApi.word_of_the_day_api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class WordOfTheDayApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(WordOfTheDayApiApplication.class, args);
	}

}
