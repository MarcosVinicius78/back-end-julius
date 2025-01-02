package com.julius.julius.service.Scraper;

import lombok.AllArgsConstructor;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.net.ConnectException;
import java.util.logging.Logger;

@Service
@AllArgsConstructor
public class JsoupConexaoService {

    private static final Logger logger = Logger.getLogger(ScraperService.class.getName());

    public Document getConnect(String url) throws ConnectException {
        try {
            logger.info("Trying to connect to URL: " + url);
            Document response = Jsoup.connect(url)
                    .userAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
                    .referrer("https://www.google.com.br")
                    .timeout(5000)
                    .cookie("PIM-SESSION-ID", "xtNeQ1oT77boxl64")
                    .followRedirects(true)
                    .get();
            logger.info("Successfully connected to URL: " + url);
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            logger.severe("Failed to connect to URL: " + url + ". Error: " + e.getMessage());
            throw new ConnectException("Failed to connect to URL: " + url);
        }
    }
}
