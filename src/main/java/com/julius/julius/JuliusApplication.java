package com.julius.julius;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.telegram.telegrambots.meta.TelegramBotsApi;
// import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
// import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import org.springframework.scheduling.annotation.EnableScheduling;

// import com.julius.julius.service.Bot;

@SpringBootApplication
@EnableScheduling
public class JuliusApplication {

	public static void main(String[] args) {
		SpringApplication.run(JuliusApplication.class, args);
	}
}