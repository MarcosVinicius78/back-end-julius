package com.julius.julius.service.Scraper;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.julius.julius.DTO.response.ProdutoScraperDTO;
import com.julius.julius.models.LinksProdutos;
import com.julius.julius.models.Produto;
import com.julius.julius.repository.CategoriaRepository;
import com.julius.julius.repository.LojaRepository;
import com.julius.julius.repository.ProdutoRepository;
import com.julius.julius.service.ProdutoService;
import com.julius.julius.service.Scraper.Amazon.AmazonService;
import com.julius.julius.service.Scraper.awin.GerarLinkAwin;
import com.julius.julius.service.Scraper.awin.ScraperLojasAwin;
import com.julius.julius.service.Scraper.magazine.MagazineService;
import com.julius.julius.service.Scraper.mercadolivre.MercadoLivre;

import io.jsonwebtoken.io.IOException;
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

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;
    private final LojaRepository lojaRepository;
    private final ProdutoService produtoService;

    private boolean ativar = false;

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
        AWIN_LINKS.put("casas", 17629);
        AWIN_LINKS.put("beleza", 29407);
        AWIN_LINKS.put("cobasi", 17870);
        AWIN_LINKS.put("tok", 36382);
        AWIN_LINKS.put("eudora", 17837);
        AWIN_LINKS.put("madeira", 17762);
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
        } else if (url.contains("mercado")) {
            return mercadoLivre.getDadosProdutos(url);
        } else if (url.contains("shopee")) {
            return handleShopee(url);
        } else {
            return magazineService.getProdutoMagazine(url);
        }

        // Default case for magazine if no keyword matches
        // return null;
    }

    private ProdutoScraperDTO handleAmazon(String url) {
        String codigo = amazonService.pegarCodigoProdutoAmazon(url);
        String responseSe = amazonService.getProdutoAmazon(codigo, 1);
        String responseOmc = amazonService.getProdutoAmazon(codigo, 2);
        ProdutoScraperDTO produtoScraperDTO = amazonService.montarProdutoAmazon(responseOmc, url);
        return amazonService.montarProdutoAmazon(responseSe, produtoScraperDTO.urlProdutoSe());
    }

    private ProdutoScraperDTO handleAwin(String urlShort, String nomeLoja) {
        return scraperLojasAwin.pegarDadosDoProdutoAwin(urlShort, nomeLoja);
    }

    private ProdutoScraperDTO handleShopee(String url) {
        String responseSe = shopeeService.fetchProductOffers(url, 1L);

        ProdutoScraperDTO produtoSe = shopeeService.pegarInfoProdutosShopee(responseSe);
        
        String responseOmc = shopeeService.fetchProductOffers(url, 2L);
        ProdutoScraperDTO produtoOmc = shopeeService.pegarInfoProdutosShopee(responseOmc);

        return new ProdutoScraperDTO(produtoSe.nomeProduto(), produtoSe.precoProduto(), produtoSe.urlImagem(), produtoSe.urlProdutoSe(), produtoOmc.urlProdutoOfm(), produtoSe.precoParcelado());
    }

    @Scheduled(fixedRate = 300000)
    public void checkForNewProducts() {

        if (ativar) {
            raspagemDadosEconomizando();
            raspagemDadosPechinchou();
        }
    }

    private void raspagemDadosEconomizando() {
        try {
            Document doc = Jsoup.connect("https://economizandu.com.br/").get();
            Element scriptTag = doc.selectFirst("script#__NEXT_DATA__");

            if (scriptTag != null) {
                String jsonData = scriptTag.html();
                JsonObject jsonObject = JsonParser.parseString(jsonData).getAsJsonObject();
                JsonArray produtos = jsonObject.getAsJsonObject("props")
                        .getAsJsonObject("pageProps").getAsJsonArray("products");
                // JsonArray results = promos.getAsJsonArray("results");

                for (int i = 0; i < produtos.size(); i++) {
                    JsonObject promo = produtos.get(i).getAsJsonObject();
                    JsonObject atributos = promo.getAsJsonObject("attributes");
                
                    // String productId = promo.get("id").getAsString();
                    String preco = atributos.get("price").getAsString();
                    String titulo = atributos.get("title").getAsString();
                    String image = atributos.get("image").getAsString();
                    String link = atributos.get("link").getAsString();
                    // String slug = promo.get("slug").getAsString();

                    
                    NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
                    
                    // System.out.println("produtos encontrados mas nenhum é shopee");
                    // Verifica se o produto já foi processado
                    if (!produtoRepository.existsByTitulo(titulo)
                    && (link.contains("shopee"))) {
                        String imagemSocial = extractImageReal(image);
                        Produto produto = new Produto();
                        // produto.setProductId(productId);
                        produto.setPreco(formatter.format(Double.parseDouble(preco)));
                        produto.setUrlImagem(produtoService.salvarImagem(image));
                        produto.setTitulo(titulo);
                        produto.setCategoria(categoriaRepository.getById(10L));

                        produto.setMensagemAdicional("Promoção sujeita a alteração a qualquer momento");

                        if (imagemSocial != null) {
                            produto.setImagemSocial(produtoService.salvarImagemRealUrl(imagemSocial));
                        }

                        String linkSe = handleShopee(link).urlProdutoSe();
                        // String linkOmc = handleAmazon(link).urlProdutoOfm();

                        produto.setLoja(lojaRepository.findByNomeLojaContainingIgnoreCase("amazon"));

                        LinksProdutos linksProdutosSe = produtoService.salvarLinkProduto(linkSe, 1L);
                        // LinksProdutos linksProdutosOmc = produtoService.salvarLinkProduto(linkOmc,
                        // 2L);

                        produto.getLinksProdutos().add(linksProdutosSe);
                        // produto.getLinksProdutos().add(linksProdutosOmc);

                        produto.setLink(linkSe);
                        // System.out.println("amazon");

                        produtoRepository.save(produto);

                        System.out.printf("Novo Produto Encontrado");
                    }
                }
            } else {
                System.out.println("Script com JSON não encontrado.");
            }
        } catch (

        Exception e) {
            e.printStackTrace();
        }
    }

    private void raspagemDadosPechinchou() {
        try {
            Document doc = Jsoup.connect("https://pechinchou.com.br/").get();
            Element scriptTag = doc.selectFirst("script#__NEXT_DATA__");

            if (scriptTag != null) {
                String jsonData = scriptTag.html();
                JsonObject jsonObject = JsonParser.parseString(jsonData).getAsJsonObject();
                JsonObject pageProps = jsonObject.getAsJsonObject("props").getAsJsonObject("pageProps");
                JsonObject promos = pageProps.getAsJsonObject("promos");
                JsonArray results = promos.getAsJsonArray("results");

                for (int i = 0; i < results.size(); i++) {
                    JsonObject promo = results.get(i).getAsJsonObject();
                    // String productId = promo.get("id").getAsString();
                    String preco = promo.get("price").getAsString();
                    String titulo = promo.get("title").getAsString();
                    String image = promo.get("image").getAsString();
                    String link = promo.get("long_url").getAsString();
                    String slug = promo.get("slug").getAsString();

                    String imagemSocial = extractImageReal("https://pechinchou.com.br/oferta/" + slug);

                    JsonArray couponsArray = promo.getAsJsonArray("coupons");
                    String firstCoupon = "";
                    if (couponsArray != null && couponsArray.size() > 0) {
                        firstCoupon = couponsArray.get(0).getAsString();
                    }

                    NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

                    // Verifica se o produto já foi processado
                    if (!produtoRepository.existsByTitulo(titulo)
                            && (link.contains("amazon") || link.contains("maga")
                                    || !firstCoupon.contains("PECHINCHOU"))) {
                        Produto produto = new Produto();
                        // produto.setProductId(productId);
                        produto.setPreco(formatter.format(Double.parseDouble(preco)));
                        produto.setUrlImagem(produtoService.salvarImagem(image));
                        produto.setTitulo(titulo);
                        produto.setCategoria(categoriaRepository.getById(10L));

                        produto.setCupom(firstCoupon);
                        produto.setMensagemAdicional("Promoção sujeita a alteração a qualquer momento");

                        if (imagemSocial != null) {
                            produto.setImagemSocial(produtoService.salvarImagemRealUrl(imagemSocial));
                            // System.out.println("iamgem soscial: "+
                            // produtoService.salvarImagem(imagemSocial));
                        }
                        if (link.contains("amazon")) {
                            String linkSe = handleAmazon(link).urlProdutoSe();
                            String linkOmc = handleAmazon(link).urlProdutoOfm();

                            produto.setLoja(lojaRepository.findByNomeLojaContainingIgnoreCase("amazon"));

                            LinksProdutos linksProdutosSe = produtoService.salvarLinkProduto(linkSe, 1L);
                            LinksProdutos linksProdutosOmc = produtoService.salvarLinkProduto(linkOmc, 2L);

                            produto.getLinksProdutos().add(linksProdutosSe);
                            produto.getLinksProdutos().add(linksProdutosOmc);

                            produto.setLink(linkSe);
                            // System.out.println("amazon");
                        } else if (link.contains("mercado")) {
                            produto.setLoja(lojaRepository.findByNomeLojaContainingIgnoreCase("mercado"));
                            System.out.println("mercado");
                        } else if (link.contains("maga")) {
                            produto.setLoja(lojaRepository.findByNomeLojaContainingIgnoreCase("magazine"));
                            LinksProdutos linksProdutosSe = produtoService
                                    .salvarLinkProduto(link.replace("magazinedopechinchou", "sergipeeofertas"), 1L);
                            LinksProdutos linksProdutosOmc = produtoService
                                    .salvarLinkProduto(
                                            link.replace("magazinedopechinchou", "magazineofertasmaiscupom"), 2L);
                            produto.getLinksProdutos().add(linksProdutosSe);
                            produto.getLinksProdutos().add(linksProdutosOmc);
                            produto.setLink(link.replace("magazinedopechinchou", "sergipeeofertas"));
                        }

                        produtoRepository.save(produto);

                        System.out.printf("Novo Produto Encontrado");
                    }
                }
            } else {
                System.out.println("Script com JSON não encontrado.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String extractImageReal(String url) throws java.io.IOException {

        // Conecte-se ao site e obtenha o conteúdo HTML
        Document docProduto = Jsoup.connect(url).get();

        Element scriptTagProduto = docProduto.selectFirst("script#__NEXT_DATA__");

        if (scriptTagProduto != null) {
            String jsonData = scriptTagProduto.html();
            JsonObject jsonObject = JsonParser.parseString(jsonData).getAsJsonObject();
            JsonObject pageProps = jsonObject.getAsJsonObject("props").getAsJsonObject("pageProps");
            JsonObject promo = pageProps.getAsJsonObject("promo");
            if (promo.get("image_social") != null) {
                return promo.get("image_social").getAsString();
            } else {
                System.out.println("A imagem social e null");
            }

        }
        return "";
    }

    public void ativarBot(Boolean valor) {
        ativar = valor;
    }

    public Boolean statusBot() {
        return ativar;
    }

}
