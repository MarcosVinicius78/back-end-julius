package com.julius.julius.service.Scraper.awin;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import com.julius.julius.DTO.response.ProdutoResponseDto;
import com.julius.julius.DTO.response.ProdutoScraperDTO;
import com.julius.julius.service.Scraper.ScraperService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScraperLojasAwin {

    private static final Logger logger = Logger.getLogger(ScraperService.class.getName());

    private final BoticarioScrapper boticarioScrapper;

    public ProdutoScraperDTO pegarDadosDoProdutoAwin(String urlShort, String nomeLoja) {
        
        try {
            Document response = getConnect(urlShort);
            
            System.out.println(nomeLoja);
            if (response == null) {
                return new ProdutoScraperDTO("", "", "","", urlShort, "");
            }

            switch (nomeLoja) {
                case "boti":
                    return boticarioScrapper.pegarInformacoes(response, urlShort);
                case "extra":
                    infoProdutoFerreiraCosta(response, urlShort);
                    break;
                default:
                    break;
            }
            
            
            return new ProdutoScraperDTO("", "", "","", urlShort, "");
        } catch (ConnectException e) {
            e.printStackTrace();
            return new ProdutoScraperDTO("", "", "","", urlShort, "");
        }
        // return null;
    }
    
    private Document getConnect(String url) throws ConnectException {
        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("https://www.geosurf.com", 8080));
        try {
            logger.info("Trying to connect to URL: " + url);
            Document response = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
                    .referrer("https://www.google.com.br")
                    .timeout(10000)
                    .cookie("PIM-SESSION-ID", "xtNeQ1oT77boxl64")
                    .followRedirects(true)
                    .get();
            logger.info("Successfully connected to URL: " + url);
            return response;
        } catch (Exception e) {
            logger.severe("Failed to connect to URL: " + url + ". Error: " + e.getMessage());
            throw new ConnectException("Failed to connect to URL: " + url);
        }
    }

    private ProdutoScraperDTO infoProdutoFerreiraCosta(Document doc, String url){

        System.out.println(doc.title());

        // String titulo = elements.select("data-testid=\"box-product-title\"").text();

        return new ProdutoScraperDTO("titulo", "", "",url,"", "");
    }
}
