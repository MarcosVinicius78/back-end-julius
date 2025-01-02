package com.julius.julius.service.Scraper.awin;

import com.julius.julius.service.Scraper.JsoupConexaoService;
import lombok.AllArgsConstructor;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import com.julius.julius.DTO.response.ProdutoScraperDTO;

@Service
@AllArgsConstructor
public class BoticarioScrapper {

    private final JsoupConexaoService jsoupConexaoService;

    public ProdutoScraperDTO pegarInformacoes(String urlSe, String urlOmc) {

        Document doc = null;

        try {
            doc = jsoupConexaoService.getConnect(urlSe);
        }catch (Exception e) {
            e.printStackTrace();
        }

        Elements elements = null;

        elements = doc.getElementsByClass("nproduct-title");
        String titulo = "";
        if (elements != null) {
            titulo = elements.text();
        }

        elements = doc.getElementsByClass("nproduct-price-value");
        String preco = "";
        if (elements != null) {
            preco = elements.text();
        }

        elements = doc.getElementsByClass("product-image");
        String imagem = "";
        if (elements.first() != null) {
            imagem = elements.first().attr("src");
        }

        elements = doc.getElementsByClass("nproduct-price-installments");
        String precoParcelado = "";
        System.out.println(elements.text());
        if (elements.text() != "") {
            precoParcelado = "Ou " + elements.text();
        }

        return new ProdutoScraperDTO(titulo, preco, imagem, urlSe, urlOmc, precoParcelado);
    }

}
