package com.julius.julius.service.Scraper;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.julius.julius.DTO.response.ProdutoScraperDTO;
import com.julius.julius.service.Scraper.Amazon.AmazonService;
import com.julius.julius.service.Scraper.awin.GerarLinkAwin;
import com.julius.julius.service.Scraper.awin.ScraperLojasAwin;
import com.julius.julius.service.Scraper.magazine.MagazineService;
import com.julius.julius.service.Scraper.mercadolivre.MercadoLivre;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScraperService {

    private final GerarLinkAwin gerarLinkAwin;
    private final ShopeeService shopeeService;
    private final AmazonService amazonService;
    private final MagazineService magazineService;
    private final ScraperLojasAwin scraperLojasAwin;
    
    private final MercadoLivre mercadoLivre;

    private static final Map<String, Integer> AWIN_LINKS = new HashMap<>();

    static {
        AWIN_LINKS.put("extra", 17874);
        AWIN_LINKS.put("pague", 17806);
        AWIN_LINKS.put("biscuit", 24620);
        AWIN_LINKS.put("carrefour", 17665);
        AWIN_LINKS.put("boti", 17659);
        AWIN_LINKS.put("nike", 17652);
        AWIN_LINKS.put("costa", 65618);
        AWIN_LINKS.put("ponto", 17621);
        AWIN_LINKS.put("centauro", 17806);
    }

    public ProdutoScraperDTO scraperProduto(String url) {
        return descobrirLoja(url);
    }

    private ProdutoScraperDTO descobrirLoja(String url) {
        for (Map.Entry<String, Integer> entry : AWIN_LINKS.entrySet()) {
            if (url.contains(entry.getKey())) {
                String urlAwin = gerarLinkAwin.gerarLink(url, entry.getValue());
                return handleAwin(urlAwin, entry.getKey());
            }
        }
        
        if (url.contains("amzn") || url.contains("amazon")) {
            return handleAmazon(url);
        } else if (url.contains("shopee")) {
            return handleShopee();
        } else if (url.contains("mercado")) {
            return mercadoLivre.getDadosProdutos(url);
        }else{
            return magazineService.getProdutoMagazine(url);
        }
        
        // Default case for magazine if no keyword matches
        // return null;
    }

    private ProdutoScraperDTO handleAmazon(String url) {
        String codigo = amazonService.pegarCodigoProdutoAmazon(url);
        String response = amazonService.getProdutoAmazon(codigo);
        return amazonService.montarProdutoAmazon(response, url);
    }

    private ProdutoScraperDTO handleAwin(String urlShort, String nomeLoja){
        return scraperLojasAwin.pegarDadosDoProdutoAwin(urlShort, nomeLoja);
    }

    private ProdutoScraperDTO handleShopee() {
        shopeeService.getAuthShopee();
        return null; // Adjust as necessary
    }
}
