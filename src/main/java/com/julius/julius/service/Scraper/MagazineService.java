package com.julius.julius.service.Scraper;

import java.net.ConnectException;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import com.julius.julius.DTO.response.ProdutoScraperDTO;

@Service
public class MagazineService {

    private final String URL_BASE_MAGAZINE = "https://www.magazinevoce.com.br";

    private final String URL_BUSCA_MAGAZINE = "https://www.magazinevoce.com.br/sergipeeofertas/busca/";

    public ProdutoScraperDTO getProdutoMagazine(String url) {

        Document response = null;
        String codigo = "";
        try {
            if (url.length() > 12) {
                response = getConnect(url);
            }else{
                codigo = url;
                url = URL_BUSCA_MAGAZINE + url;
                response = getConnect(url);
            }

            Document doc = response;

            String cssSelector = "//*[@id=\"__next\"]/div/main/section[4]/div[3]/div/ul";

            // Seleciona o segundo elemento 'a'
            Elements secondAnchor = doc.selectXpath(cssSelector);

            for (Element element : secondAnchor.select("a")) {
                if (element.attr("href").contains(codigo)) {

                    String href = this.URL_BASE_MAGAZINE + element.attr("href");
                    String title = element.select("[data-testid=product-title]").text();
                    String price = element.select("[data-testid=price-value]").text();
                    String precoParcelado = element.select("[data-testid=installment]").text();
                    String imagem = element.select("[data-testid=image]").attr("src");
                    
                    return new ProdutoScraperDTO(title, price, imagem ,href, precoParcelado);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    private Document getConnect(String url) throws ConnectException{

        try {
            Document response = Jsoup.connect(url)
            .userAgent(
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
            .get();
            return response;
        } catch (Exception e) {
            throw new ConnectException();
        }
    }

    // private Response getConnect(String url) throws ConnectException{

    //     try {
    //         Response response = Jsoup.connect(url)
    //         .ignoreContentType(false)
    //         .userAgent(
    //                 "Mozilla/5.0 (compatible; 008/0.83; http://www.80legs.com/webcrawler.html) Gecko/2008032620")
    //         .referrer("https://www.magazineluiza.com.br")
    //         .timeout(12000)
    //         .followRedirects(true)
    //         .execute();
    //         return response;
    //     } catch (Exception e) {
    //         throw new ConnectException();
    //     }
    // }
}
