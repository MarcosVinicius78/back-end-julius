package com.julius.julius.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.julius.julius.DTO.request.MensagemRequest;

import org.springframework.http.ResponseEntity;

@Service
@AllArgsConstructor
public class TelegramService {

    private final String TELEGRAM_BOT_TOKEN = "7889228882:AAEZSaj2jveJvmC1vwkZEN1-fOdQgboFkm8";
    // private final String TELEGRAM_CHAT_ID = "-1002452348897"; // Chat ID do canal/grupo
    // private final String TELEGRAM_CHAT_ID = "-1002452348897"; // Chat ID do canal/grupo OMC
    // private final String TELEGRAM_CHAT_ID = "-1002017124017"; // Chat ID do canal/grupo julius
    private final String TELEGRAM_CHAT_ID_SE = "-1001724288449"; // Chat ID do canal/grupo Sergipe Ofertas
    private final String TELEGRAM_CHAT_ID_OMC = "-1002108490823"; // Chat ID do canal/grupo Sergipe Ofertas
    private final String API_IMAGEM = "https://sergipeofertas.com.br/api/";
    // private final String API_IMAGEM = "https://lojawap.vteximg.com.br/arquivos/AIRFRYOVEN_fogaca_apontando_dedo_mobile.png";

    public void enviarProdutoParaTelegram(MensagemRequest mensagem) {
        String apiUrl = "https://api.telegram.org/bot" + TELEGRAM_BOT_TOKEN;

        // URL para enviar a foto com a mensagem

        String photoUrl = "";

        if (mensagem.mensagem().contains("sergipeofertas")) {
            photoUrl = apiUrl + "/sendPhoto?chat_id=" + TELEGRAM_CHAT_ID_SE + "&photo="
                       + API_IMAGEM + "download/" + mensagem.url() + "/produtos-real"
                       + "&caption=" + mensagem.mensagem() + "&parse_mode=Markdown";
        } else if (mensagem.mensagem().contains("ofertasmaiscupons")) {
            photoUrl = apiUrl + "/sendPhoto?chat_id=" + TELEGRAM_CHAT_ID_OMC + "&photo="
                       + API_IMAGEM + "download/" + mensagem.url() + "/produtos-real"
                       + "&caption=" + mensagem.mensagem() + "&parse_mode=Markdown";
        }

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(photoUrl, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            System.out.println("Produto e imagem enviados com sucesso para o Telegram.");
        } else {
            System.out.println("Falha ao enviar o produto e a imagem para o Telegram.");
        }
    }

}
