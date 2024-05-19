package com.julius.julius.service.Scraper.awin;

import java.net.ConnectException;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import com.julius.julius.DTO.response.ProdutoResponseDto;
import com.julius.julius.DTO.response.ProdutoScraperDTO;
import com.julius.julius.service.Scraper.ScraperService;

@Service
public class ScraperLojasAwin {

    private static final Logger logger = Logger.getLogger(ScraperService.class.getName());

    public ProdutoScraperDTO pegarDadosDoProdutoAwin(String urlShort, String nomeLoja) {
        
        try {
            Document response = getConnect(urlShort);
            
            if (response == null) {
                return new ProdutoScraperDTO("", "", "", urlShort, "");
            }
            
            return new ProdutoScraperDTO("", "", "", urlShort, "");
        } catch (ConnectException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return new ProdutoScraperDTO("", "", "", urlShort, "");
        }
        // return null;
    }
    
    private Document getConnect(String url) throws ConnectException {
        try {
            logger.info("Trying to connect to URL: " + url);
            Document response = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .referrer("https://www.google.com/")
                    .timeout(5000)
                    .cookie("PIM-SESSION-ID", "xtNeQ1oT77boxl64")
                    .get();
            logger.info("Successfully connected to URL: " + url);
            return response;
        } catch (Exception e) {
            logger.severe("Failed to connect to URL: " + url + ". Error: " + e.getMessage());
            throw new ConnectException("Failed to connect to URL: " + url);
        }
    }

    private ProdutoScraperDTO infoProdutoFerreiraCosta(Document doc, String url){

        String cssXPath = "//*[@id=\"__next\"]/main/div[1]/div/section[1]/section[2]";

        Elements elements = doc.selectXpath(cssXPath);
        System.out.println(doc.title());

        // String titulo = elements.select("data-testid=\"box-product-title\"").text();

        return new ProdutoScraperDTO("titulo", "", "", url, "");
    }
}
