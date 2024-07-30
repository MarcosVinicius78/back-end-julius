package com.julius.julius.service.Scraper.mercadolivre;

import java.net.ConnectException;
import java.util.logging.Logger;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import com.julius.julius.DTO.response.ProdutoScraperDTO;
import com.julius.julius.service.Scraper.ScraperService;

@Service
public class MercadoLivre {

    private static final Logger logger = Logger.getLogger(ScraperService.class.getName());

    public ProdutoScraperDTO getDadosProdutos(String url) {
        try {
            Document doc = getConnect(url);

            Elements titulo = doc.getElementsByClass("poly-component__title");

            Element precoElement = doc.select(".poly-price__current").first();
            String preco = "";
            if (precoElement != null) {
                preco = precoElement.getElementsByClass("andes-money-amount__fraction").text();
                preco = "R$ " + formatPrice(preco);
            } else {
                System.out.println("Preço não encontrado.");
            }

            Element urlImagem = doc.select("div.poly-card__portada img").first();
            String img = "";
            if (urlImagem != null) {
                img = urlImagem.attr("data-src");
            }

            Elements precoParceladoElement = doc.getElementsByClass("poly-price__installments");
            String precoParcelado = "";
            if (precoParceladoElement != null) {
                precoParcelado = precoParceladoElement.text();
            }

            return new ProdutoScraperDTO(titulo.text(), preco, img, url,"", precoParcelado);
        } catch (ConnectException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static String formatPrice(String price) {
        // Verifica se o preço tem duas casas decimais, caso contrário, adiciona "00"
        if (!price.contains(",")) {
            price += ",00";
        } else {
            int index = price.indexOf(",");
            if (price.substring(index + 1).length() == 1) {
                price += "0";
            }
        }
        return price;
    }

    private Document getConnect(String url) throws ConnectException {
        try {
            logger.info("Trying to connect to URL: " + url);
            Document response = Jsoup.connect(url)
                    .userAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
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
}
