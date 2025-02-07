package com.julius.julius.service.Scraper.awin;

import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.logging.Logger;

import com.julius.julius.service.Scraper.JsoupConexaoService;
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

    private final BoticarioScrapper boticarioScrapper;

    private final JsoupConexaoService jsoupConexaoService;

    public ProdutoScraperDTO pegarDadosDoProdutoAwin(String urlSe,String urlOmc, String nomeLoja) {

//            if (nomeLoja.equals("boti")) {
//                return boticarioScrapper.pegarInformacoes(urlSe, urlOmc);
//            }

            return new ProdutoScraperDTO("", "", "", urlSe, urlOmc, "");
    }
}
