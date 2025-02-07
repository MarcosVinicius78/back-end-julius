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
        int attempts = 3;
        for (int i = 0; i < attempts; i++) {
            try {
                logger.info("Trying to connect to URL (attempt " + (i + 1) + "): " + url);
                return Jsoup.connect(url)
                        .header("Accept", "text/html")
                        .userAgent(
                                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36")
                        .referrer("https://www.google.com.br")
                        .timeout(15000) // 15 segundos
                        .followRedirects(true)
                        .get();
            } catch (Exception e) {
                logger.warning("Attempt " + (i + 1) + " failed. Error: " + e.getMessage());
                if (i == attempts - 1) {
                    logger.severe("All connection attempts failed for URL: " + url);
                    throw new ConnectException("Failed to connect to URL: " + url);
                }
            }
        }
        throw new ConnectException("Unexpected error during connection attempts.");
    }
}
