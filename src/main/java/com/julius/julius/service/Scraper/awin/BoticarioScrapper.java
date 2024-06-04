package com.julius.julius.service.Scraper.awin;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import com.julius.julius.DTO.response.ProdutoScraperDTO;

@Service
public class BoticarioScrapper {
    
    public ProdutoScraperDTO pegarInformacoes(Document doc, String urlProduto){

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
            precoParcelado = "Ou "+ elements.text();
        }

        return new ProdutoScraperDTO(titulo, preco, imagem, urlProduto, precoParcelado);
    }

}
