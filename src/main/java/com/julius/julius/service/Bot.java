// package com.julius.julius.service;

// import java.io.IOException;
// import java.net.URL;
// import java.util.Scanner;

// import org.telegram.telegrambots.bots.TelegramLongPollingBot;
// import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
// import org.telegram.telegrambots.meta.api.objects.Update;

// import com.julius.julius.constants.DadosDoBot;

// public class Bot extends TelegramLongPollingBot {

//     @Override
//     public void onUpdateReceived(Update update) {

//         if (update.hasMessage() && update.getMessage().hasText()) {
//             SendMessage message = funcionalidade(update);

//             try {
//                 execute(message);
//             } catch (Exception e) {
//                 e.printStackTrace();
//             }
//         }
//     }

//     private SendMessage funcionalidade(Update update) {
//         SendMessage message = new SendMessage();

//         String menssagemTexto = update.getMessage().getText();

//         System.out.println(update.getMessage().getChatId());

//         double dolar = getDollarValue();

//         if (menssagemTexto.equals("/dolar")) {

//             String responseDolar = "O valor do dolar é: " + dolar;
//             message.setChatId(update.getMessage().getChatId().toString());
//             message.setText(responseDolar);
//         } else {

//             int numero = Integer.parseInt(menssagemTexto);

//             dolar = numero * dolar;

//             String responseTexto = String.format("A conversão é: %.2f ", dolar);

//             message.setChatId(update.getMessage().getChatId().toString());
//             message.setText(responseTexto);
//         }

//         return message;
//     }

//     // Método para obter o valor atual do dólar a partir de uma API externa
//     private double getDollarValue() {
//         String apiUrl = "https://economia.awesomeapi.com.br/last/USD-BRL";
//         try (Scanner scanner = new Scanner(new URL(apiUrl).openStream(), "UTF-8").useDelimiter("\\A")) {
//             String json = scanner.next();
//             json = json.split(":")[5].split(",")[0];
//             json = json.replaceAll("\"", " ");
//             return Double.parseDouble(json);
//         } catch (IOException e) {
//             e.printStackTrace();
//         }
//         return -1;
//     }

//     @Override
//     public String getBotUsername() {
//         return DadosDoBot.BOT_USER_NAME;
//     }

//     @Override
//     public String getBotToken() {
//         return DadosDoBot.BOT_TOKEN;
//     }

// }
