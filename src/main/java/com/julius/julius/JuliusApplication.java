package com.julius.julius;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
// import org.telegram.telegrambots.meta.TelegramBotsApi;
// import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
// import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

// import com.julius.julius.service.Bot;

@SpringBootApplication
public class JuliusApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(JuliusApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// try {
        //     TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        //     telegramBotsApi.registerBot(new Bot());
        // } catch (TelegramApiException e) {
        //     e.printStackTrace();
        // }
	}
}