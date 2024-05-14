package com.julius.julius.service.Scraper;

import org.springframework.stereotype.Service;

import com.julius.julius.DTO.response.ProdutoScraperDTO;
import com.julius.julius.service.Scraper.Amazon.AmazonService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScraperService {

    private final ShopeeService shopeeService;

    private final AmazonService amazonService;

    private final MagazineService magazineService;

    public ProdutoScraperDTO scraperProduto(String url) {

        return descobrirLoja(url);
    }

    private ProdutoScraperDTO descobrirLoja(String url) {

        String codigo = "";

        if (url.contains("amzn") || url.contains("amazon")) {
            codigo = amazonService.pegarCodigoProdutoAmazon(url);

            String response = amazonService.getProdutoAmazon(codigo);

            return amazonService.montarProdutoAmazon(response, url);
            
        } else if (url.contains("shopee")) {
            shopeeService.getAuthShopee();
        }else if(url.contains("magazine")){
            magazineService.getProdutoMagazine(url);
        }else{
            return magazineService.getProdutoMagazine(url);
        }

        return null;
    }
}
