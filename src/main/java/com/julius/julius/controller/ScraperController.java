package com.julius.julius.controller;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.julius.julius.DTO.response.ProdutoScraperDTO;
import com.julius.julius.service.Scraper.ScraperService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/scraper")
@RequiredArgsConstructor
public class ScraperController {

    private final String baseUrl = "https://api.shopee.com";
    private final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36";

    private final ScraperService scraperService;

    @GetMapping("/teste")
    public ResponseEntity<ProdutoScraperDTO> getProduto(@RequestParam String url) {

        Response response;
        try {
            response = Jsoup.connect(url)
                    .ignoreContentType(false)
                    .userAgent(
                            "Mozilla/5.0 (compatible; 008/0.83; http://www.80legs.com/webcrawler.html) Gecko/2008032620")
                    .referrer("https://www.amazon.com.br/")
                    .timeout(12000)
                    .followRedirects(true)
                    .execute();

            Document doc = response.parse();

            String titulo = doc.getElementById("productTitle").text();

            String precoSalvo = "R$ ";

            String preco = doc.getElementsByClass("a-price-whole").first().text();
            precoSalvo += preco;
            preco = doc.getElementsByClass("a-price-fraction").first().text();
            precoSalvo += preco;

            Element imgElement = doc.select("div#imgTagWrapperId img[src]").first();
            String urlImagem = imgElement.attr("src");

            return ResponseEntity.ok().body(new ProdutoScraperDTO(titulo, precoSalvo, urlImagem,url, ""));

        } catch (Exception e) {
            e.printStackTrace();
        }

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<ProdutoScraperDTO> getProductDetails(@RequestParam String url) throws Exception {
        ProdutoScraperDTO produtoScraperDTO = scraperService.scraperProduto(url);
        if (produtoScraperDTO != null) {
            return ResponseEntity.ok().body(produtoScraperDTO);
        }

        return ResponseEntity.notFound().build();
    }

}
