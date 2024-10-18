package com.julius.julius.controller;

import java.io.UnsupportedEncodingException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.julius.julius.DTO.response.ProdutoScraperDTO;
import com.julius.julius.service.TelegramService;
import com.julius.julius.service.Scraper.ScraperService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/scraper")
@RequiredArgsConstructor
public class ScraperController {

    private final String baseUrl = "https://api.shopee.com";
    private final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/42.0.2311.90 Safari/537.36";

    private final ScraperService scraperService;
    private final TelegramService telegramService;

    @GetMapping("/teste")
    public ResponseEntity<ProdutoScraperDTO> getProduto() throws UnsupportedEncodingException {

        // telegramService.enviarProdutoParaTelegram("teste", "teste", "teste", "https://bugoumods.com/fortaleza-x-atletico-mg/", "");

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

    @GetMapping("/ativarBot")
    public ResponseEntity<Void> ativarBot(@RequestParam Boolean ativar){
        scraperService.ativarBot(ativar);
        System.out.println(scraperService.statusBot());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/statusBot")
    public ResponseEntity<Boolean> statusBot(){
        return ResponseEntity.ok().body(scraperService.statusBot());
    }
}