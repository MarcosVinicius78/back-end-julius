package com.julius.julius.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.julius.julius.DTO.ProdutoJsonDto;
import com.julius.julius.DTO.produtoScrapperPeDto.ResponseScrapper;
import com.julius.julius.models.Categoria;
import com.julius.julius.models.LinksProdutos;
import com.julius.julius.models.Loja;
import com.julius.julius.models.Produto;
import com.julius.julius.repository.CategoriaRepository;
import com.julius.julius.repository.LinkProdutoRepository;
import com.julius.julius.repository.LojaRepository;
import com.julius.julius.repository.ProdutoRepository;
import com.julius.julius.service.ImagemService;
import com.julius.julius.service.ConfigSiteService;
import com.julius.julius.service.Scraper.JsoupConexaoService;
import com.julius.julius.service.Scraper.magazine.MagazineService;
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

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/scraper")
@RequiredArgsConstructor
public class ScraperController {

    private final ScraperService scraperService;

    private final ConfigSiteService configSiteService;

    private final JsoupConexaoService jsoupConexaoService;

    private final ProdutoRepository produtoRepository;

    private final CategoriaRepository categoriaRepository;

    private final LojaRepository lojaRepository;

    private final ImagemService imagemService;

    private final MagazineService magazineService;

    private final LinkProdutoRepository linkProdutoRepository;

    @GetMapping
    public ResponseEntity<ProdutoScraperDTO> getProductDetails(@RequestParam String url) throws Exception {
        ProdutoScraperDTO produtoScraperDTO = scraperService.scraperProduto(url);
        if (produtoScraperDTO != null) {
            return ResponseEntity.ok().body(produtoScraperDTO);
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/ativarBot")
    public ResponseEntity<Void> ativarBot(@RequestParam Boolean ativar) {
        scraperService.ativarBot(ativar);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/statusBot")
    public ResponseEntity<Boolean> statusBot() {
        return ResponseEntity.ok().body(scraperService.statusBot());
    }

    @GetMapping("/mudar-tempo-do-robo")
    public ResponseEntity<Void> mudarTempoDoRobo(@RequestParam Long tempo) {

        configSiteService.mudarTempoRobo(tempo);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/buscar-tempo-do-robo")
    public ResponseEntity<Long> mudarTempoDoRobo() {
        return ResponseEntity.ok().body(configSiteService.buscarTempoRobo());
    }

    @GetMapping("/ativar-link-curto")
    public ResponseEntity<Void> ativarLinkCurto(@RequestParam Boolean valor) {
        configSiteService.mudarLinkCurto(valor);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status-link_curto")
    public ResponseEntity<Boolean> statusLinkCurto() {
        return ResponseEntity.ok().body(configSiteService.buscarLinkCurto());
    }

    @GetMapping("/ativar-link-sem-dominio")
    public ResponseEntity<Void> ativarLinkSemDominio(@RequestParam Boolean valor) {
        configSiteService.mudarSemDominioSe(valor);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/status-link-sem-dominio")
    public ResponseEntity<Boolean> statusLinkSemDominio() {
        return ResponseEntity.ok().body(configSiteService.buscarLinkSemDominioSe());
    }

    @GetMapping("/teste")
    public void processarPromocoes() {
        try {
            Document doc = jsoupConexaoService.getConnect("https://pechinchou.com.br/");
            Element scriptTag = doc.selectFirst("script#__NEXT_DATA__");

            if (scriptTag == null) {
                System.out.println("Script com JSON não encontrado.");
                return;
            }

            String jsonData = scriptTag.html();
            ObjectMapper mapper = new ObjectMapper();
            ResponseScrapper response = mapper.readValue(jsonData, ResponseScrapper.class);

            for (ProdutoJsonDto promo : response.props().pageProps().promos().results()) {
                processarProduto(promo);
            }

        } catch (Exception e) {
            System.err.println("Erro ao realizar raspagem: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void processarProduto(ProdutoJsonDto promo) {
        try {
            Double preco = converterPreco(promo.getPrice());
            if (preco == null || preco <= 750) return;

            boolean isAmazon = promo.getShortUrl().contains("amzn");
            boolean isMagazine = promo.getShortUrl().contains("maga");
            boolean tituloUnico = produtoRepository.existsByTitulo(promo.getTitle());

            if (tituloUnico || !(isMagazine || isAmazon)) return;

            Categoria categoria = categoriaRepository.findById(3L)
                    .orElseThrow(() -> new RuntimeException("Categoria inexistente"));

            String imagemSite = imagemService.salvarImagemUrl(promo.getImage(), "produtos");
            String imagemSocial = obterImagemSocial(promo.getSlug());

            List<LinksProdutos> linksSalvos = new ArrayList<>();
            ProdutoScraperDTO produto = null;

            if (isMagazine) {
                produto = processarMagazine(promo, linksSalvos);
            } else if (isAmazon) {
                produto = processarAmazon(promo, linksSalvos);
            }

            if (produto != null) {
                salvarProduto(promo, categoria, imagemSite, imagemSocial, linksSalvos);
            }

        } catch (Exception e) {
            System.err.println("Erro ao processar produto: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Double converterPreco(String precoStr) {
        try {
            return Double.parseDouble(precoStr);
        } catch (NumberFormatException e) {
            System.err.println("Erro ao converter preço: " + precoStr);
            return null;
        }
    }

    private String obterImagemSocial(String slug) {
        try {
            String imagem = scraperService.extractImageReal("https://pechinchou.com.br/oferta/" + slug);
            return imagem != null ? imagemService.salvarImagemUrl(imagem, "produtos") : null;
        } catch (Exception e) {
            System.err.println("Erro ao extrair imagem social para slug: " + slug);
            return null;
        }
    }

    private ProdutoScraperDTO processarMagazine(ProdutoJsonDto promo, List<LinksProdutos> linksSalvos) {
        Pattern pattern = Pattern.compile("/p/([^/]+)");
        Matcher matcher = pattern.matcher(promo.getShortUrl());

        if (matcher.find()) {
            String url = matcher.group(1);
            ProdutoScraperDTO produto = magazineService.getProdutoMagazine(url);

            adicionarLinks(linksSalvos, produto);
            return produto;
        }

        return null;
    }

    private ProdutoScraperDTO processarAmazon(ProdutoJsonDto promo, List<LinksProdutos> linksSalvos) {
        ProdutoScraperDTO produto = scraperService.handleAmazon(promo.getShortUrl());
        adicionarLinks(linksSalvos, produto);
        return produto;
    }

    private void adicionarLinks(List<LinksProdutos> linksSalvos, ProdutoScraperDTO produto) {
        linksSalvos.add(new LinksProdutos(null, produto.urlProdutoSe(), 1L, null));
        linksSalvos.add(new LinksProdutos(null, produto.urlProdutoOfm(), 2L, null));
        linkProdutoRepository.saveAll(linksSalvos);
    }

    private void salvarProduto(ProdutoJsonDto produto, Categoria categoria, String imagemSite, String imagemSocial, List<LinksProdutos> linksSalvos) {
        try {

            Loja loja = null;

            if (produto.getShortUrl().contains("magazine")) {
                loja = lojaRepository.findByNomeLojaContainingIgnoreCase("magazine");
            }else {
                loja = lojaRepository.findByNomeLojaContainingIgnoreCase("amazon");
            }

            NumberFormat precoFormatado = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

            Double preco = converterPreco(produto.getPrice());
            String valroFormatado = precoFormatado.format(preco);

            Produto produtoSalvar = Produto.builder()
                    .titulo(produto.getTitle())
                    .preco(valroFormatado)
                    .linksProdutos(linksSalvos)
                    .urlImagem(imagemSite)
                    .imagemSocial(imagemSocial)
                    .categoria(categoria)
                    .loja(loja)
                    .mensagemAdicional("Promoção sujeita a alteração a qualquer momento.")
                    .build();

            produtoRepository.save(produtoSalvar);
        } catch (Exception e) {
            System.err.println("Erro ao salvar produto: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


