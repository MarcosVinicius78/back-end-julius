package com.julius.julius.controller;

import com.julius.julius.service.Scraper.ConfigSiteService;
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

    private final ScraperService scraperService;

    private final ConfigSiteService configSiteService;

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
        return ResponseEntity.ok().build();
    }

    @GetMapping("/statusBot")
    public ResponseEntity<Boolean> statusBot(){
        return ResponseEntity.ok().body(scraperService.statusBot());
    }

    @GetMapping("/mudar-tempo-do-robo")
    public ResponseEntity<Void> mudarTempoDoRobo(@RequestParam Long tempo){

        configSiteService.mudarTempoRobo(tempo);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/buscar-tempo-do-robo")
    public ResponseEntity<Long> mudarTempoDoRobo(){
        return ResponseEntity.ok().body(configSiteService.buscarTempoRobo());
    }

    @GetMapping("/ativar-link-curto")
    public ResponseEntity<Void> ativarLinkCurto(@RequestParam Boolean valor){
        configSiteService.mudarLinkCurto(valor);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status-link_curto")
    public ResponseEntity<Boolean> statusLinkCurto(){
        return ResponseEntity.ok().body(configSiteService.buscarLinkCurto());
    }
}