package com.julius.julius.service.Scraper.awin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GerarLinkAwin {

    private final String HOST = "https://api.awin.com/publishers/1397427/linkbuilder/generate";

    public String gerarLink(String url, long idLoja) {
        try {
            String requestPayload = """
                        {
                          "advertiserId": %d,
                          "destinationUrl": "%s",
                          "parameters": {
                            "campaign": "",
                            "clickref": "",
                            "clickref2": "",
                            "clickref3": "",
                            "clickref4": "",
                            "clickref5": "",
                            "clickref6": ""
                          },
                          "shorten": true
                        }
                    """.formatted(idLoja, url);

            // Criar um cliente HTTP
            HttpClient client = HttpClientBuilder.create().build();

            // Criar o objeto HttpPost com a URL do endpoint
            HttpPost httpPost = new HttpPost(HOST);

            // Configurar o payload da solicitação
            httpPost.setEntity(new StringEntity(requestPayload));

            // Configurar o cabeçalho de autorização
            httpPost.setHeader("Authorization", "Bearer ec0428ab-5f35-4ac8-86e7-6573bbb26570");
            httpPost.setHeader("Content-Type", "application/json");
            // Executar a solicitação e obter a resposta
            HttpResponse response = client.execute(httpPost);

            // Obter o corpo da resposta
            HttpEntity entity = response.getEntity();
            String jsonResponse = EntityUtils.toString(entity, StandardCharsets.UTF_8);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(jsonResponse);
            String shortUrl = jsonNode.get("shortUrl").asText();
            
            return shortUrl;
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return null;
    }
}
