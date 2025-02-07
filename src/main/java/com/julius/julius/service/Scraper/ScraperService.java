package com.julius.julius.service.Scraper;

import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.julius.julius.DTO.produtoScrapperPeDto.ResponseScrapper;
import com.julius.julius.models.Loja;
import com.julius.julius.repository.LinkProdutoRepository;
import com.julius.julius.service.ImagemService;
import lombok.Getter;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.julius.julius.DTO.ProdutoJsonDto;
import com.julius.julius.DTO.response.ProdutoScraperDTO;
import com.julius.julius.models.Categoria;
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
@Getter
public class ScraperService {

    private final GerarLinkAwin gerarLinkAwin;
    private final ShopeeService shopeeService;
    private final AmazonService amazonService;
    private final MagazineService magazineService;
    private final ScraperLojasAwin scraperLojasAwin;

    private final ImagemService imagemService;

    private final MercadoLivre mercadoLivre;

    private final ProdutoRepository produtoRepository;
    private final CategoriaRepository categoriaRepository;
    private final LojaRepository lojaRepository;
    private final ProdutoService produtoService;

    private final JsoupConexaoService jsoupConexaoService;

    private final String API_OMC = "d8d2cc65-dd41-4f9d-853d-172fae7dcdfe";

    private final String API_SE = "ec0428ab-5f35-4ac8-86e7-6573bbb26570";
    private final String ID_AFILIADO_SE = "1397427";
    private final String ID_AFILIADO_OMC = "1619304";

    private final LinkProdutoRepository linkProdutoRepository;

    private boolean ativar = false;

    private static final Map<String, Integer> AWIN_LINKS = new HashMap<>();

    private final ConfigSiteService configSiteService;

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
        AWIN_LINKS.put("natura", 17658);
    }

    public ProdutoScraperDTO scraperProduto(String url) {
        return descobrirLoja(url);
    }

    private ProdutoScraperDTO descobrirLoja(String url) {
        for (Map.Entry<String, Integer> entry : AWIN_LINKS.entrySet()) {
            if (url.contains(entry.getKey())) {
                String urlSe = gerarLinkAwin.gerarLink(url, entry.getValue(), API_SE, ID_AFILIADO_SE);
                String urlOmc = gerarLinkAwin.gerarLink(url, entry.getValue(), API_OMC, ID_AFILIADO_OMC);
                return scraperLojasAwin.pegarDadosDoProdutoAwin(urlSe, urlOmc, entry.getKey());
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

    public ProdutoScraperDTO handleAmazon(String url) {
        String codigo = amazonService.pegarCodigoProdutoAmazon(url);

        if (codigo == null) {
            return null;
        }
        String responseSe = amazonService.getProdutoAmazon(codigo, 1);
        String responseOmc = amazonService.getProdutoAmazon(codigo, 2);

        if (responseOmc.isEmpty()) {
            return null;
        }
        ProdutoScraperDTO produtoScraperDTO = amazonService.montarProdutoAmazon(responseOmc, url);
        return amazonService.montarProdutoAmazon(responseSe, produtoScraperDTO.urlProdutoSe());
    }

    private ProdutoScraperDTO handleShopee(String url) {
        String responseSe = shopeeService.fetchProductOffers(url, 1L);

        ProdutoScraperDTO produtoSe = shopeeService.pegarInfoProdutosShopee(responseSe);

        String responseOmc = shopeeService.fetchProductOffers(url, 2L);
        ProdutoScraperDTO produtoOmc = shopeeService.pegarInfoProdutosShopee(responseOmc);

        return new ProdutoScraperDTO(produtoSe.nomeProduto(), produtoSe.precoProduto(), produtoSe.urlImagem(),
                produtoSe.urlProdutoSe(), produtoOmc.urlProdutoOfm(), produtoSe.precoParcelado());
    }

    @Scheduled(fixedRateString = "#{@configSiteService.getTEMPO_ROBO()}")
    public void checkForNewProducts() {

        if (ativar) {
//            raspagemDadosEconomizando();
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

                    System.out.println("produto: " + produtoRepository.existsByTitulo(titulo));

                    // NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("pt",
                    // "BR"));

                    // System.out.println("produtos encontrados mas nenhum é shopee");
                    // Verifica se o produto já foi processado
                    if (!produtoRepository.existsByTitulo(titulo)
                        && link.contains("shopee")) {
                        // String imagemSocial = extractImageReal(image);
                        Produto produto = new Produto();
                        // produto.setProductId(productId);
                        produto.setPreco(preco);
//                        produto.setUrlImagem(produtoService.salvarImagem(image));
//                        produto.setImagemSocial(produtoService.salvarImagemRealUrl(image));
                        produto.setTitulo(titulo);
                        produto.setCategoria(categoriaRepository.getById(18L));

                        produto.setMensagemAdicional("Promoção sujeita a alteração a qualquer momento");

                        ProdutoScraperDTO produtoScraperDTO = handleShopee(link);

                        String linkSe = produtoScraperDTO.urlProdutoSe();
                        String linkOmc = produtoScraperDTO.urlProdutoOfm();

                        produto.setLoja(lojaRepository.findByNomeLojaContainingIgnoreCase("shopee"));

                        LinksProdutos linksProdutosOmc = produtoService.salvarLinkProduto(linkOmc, 2L);
                        LinksProdutos linksProdutosSe = produtoService.salvarLinkProduto(linkSe, 1L);

                        produto.getLinksProdutos().add(linksProdutosSe);
                        produto.getLinksProdutos().add(linksProdutosOmc);

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

//    private void raspagemDadosPechinchou() {
//        try {
//            Document doc = jsoupConexaoService.getConnect("https://pechinchou.com.br/");
//            Element scriptTag = doc.selectFirst("script#__NEXT_DATA__");
//
//            if (scriptTag == null) {
//                System.out.println("Script com JSON não encontrado.");
//                return;
//            }
//
//            String jsonData = scriptTag.html();
//
//            ObjectMapper mapper = new ObjectMapper();
//            ResponseScrapper response = mapper.readValue(jsonData, ResponseScrapper.class);
//
//            // Acesse os dados simplificados
//            for (ProdutoJsonDto promo : response.props().pageProps().promos().results()) {
//
//                Double preco = Double.parseDouble(promo.getPrice());
//
//                boolean isAmazon = promo.getShortUrl().contains("amzn");
//
//                boolean isMagazine = promo.getShortUrl().contains("maga");
//
//                boolean tituloUnico = produtoRepository.existsByTitulo(promo.getTitle());
//
//                if (preco > 750 && !tituloUnico) {
//
//                    Categoria categoria = categoriaRepository.findById(3L).orElseThrow(() -> new RuntimeException("Categoria inexistente"));
//
//                    String imagemSite = imagemService.salvarImagemUrl(promo.getImage(), "produtos");
//                    String imagemSocial = "";
//
//                    try {
//                        imagemSocial = extractImageReal("https://pechinchou.com.br/oferta/" + promo.getSlug());
//                        if (imagemSocial != null) {
////                    produto.setImagemSocial(produtoService.salvarImagemRealUrl(imagemSocial));
//                        } else {
//                            System.out.println("Nenhuma imagem social foi encontrada para o slug: " + promo.getSlug());
//                        }
//                    } catch (Exception e) {
//                        System.err.println("Erro ao extrair imagem social: " + e.getMessage());
//                        e.printStackTrace();
//                    }
//
//                    if (isMagazine) {
//                        Pattern pattern = Pattern.compile("/p/([^/]+)");
//                        Matcher matcher = pattern.matcher(promo.getShortUrl());
//
//                        String url = "";
//
//                        if (matcher.find()) {
//                            url = matcher.group(1);
//                        }
//
//                        ProdutoScraperDTO produto = magazineService.getProdutoMagazine(url);
//
//                        LinksProdutos linksProdutosSe = new LinksProdutos();
//                        linksProdutosSe.setSite(1L);
//                        linksProdutosSe.setUrl(produto.urlProdutoSe());
//
//                        LinksProdutos linksProdutosOmc = new LinksProdutos();
//                        linksProdutosOmc.setSite(2L);
//                        linksProdutosOmc.setUrl(produto.urlProdutoOfm());
//
//                        List<LinksProdutos> linksSalvos = new ArrayList<>();
//                        linksSalvos.add(linksProdutosSe);
//                        linksSalvos.add(linksProdutosOmc);
//
//                        Produto produtoSalvar = Produto.builder()
//                                .titulo(produto.nomeProduto())
//                                .preco(promo.getPrice())
//                                .linksProdutos(linksSalvos)
//                                .urlImagem(imagemSite)
//                                .imagemSocial(imagemSocial)
//                                .categoria(categoria)
//                                .mensagemAdicional("Promoção sujeita a alteração a qualquer momento.")
//                                .build();
//
//                        produtoRepository.save(produtoSalvar);
//
//                    } else if (isAmazon) {
//                        ProdutoScraperDTO produto = handleAmazon(promo.getShortUrl());
//
//                        LinksProdutos linksProdutosSe = new LinksProdutos();
//                        linksProdutosSe.setSite(1L);
//                        linksProdutosSe.setUrl(produto.urlProdutoSe());
//
//                        LinksProdutos linksProdutosOmc = new LinksProdutos();
//                        linksProdutosOmc.setSite(2L);
//                        linksProdutosOmc.setUrl(produto.urlProdutoOfm());
//
//                        List<LinksProdutos> linksSalvos = new ArrayList<>();
//                        linksSalvos.add(linksProdutosSe);
//                        linksSalvos.add(linksProdutosOmc);
//
//                        NumberFormat precoFormatado = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
//
//                        String valroFormatado = precoFormatado.format(promo.getPrice());
//
//                        Produto produtoSalvar = Produto.builder()
//                                .titulo(produto.nomeProduto())
//                                .preco(valroFormatado)
//                                .linksProdutos(linksSalvos)
//                                .urlImagem(imagemSite)
//                                .imagemSocial(imagemSocial)
//                                .mensagemAdicional("Promoção sujeita a alteração a qualquer momento.")
//                                .build();
//
//                        produtoRepository.save(produtoSalvar);
//                    }
//                }
//            }
//
//        } catch (Exception e) {
//            System.err.println("Erro ao realizar raspagem: ");
//            e.printStackTrace();
//        }
//    }

    public String extractImageReal(String url) {

        // Conecte-se ao site e obtenha o conteúdo HTML
        try {
            Document docProduto = jsoupConexaoService.getConnect(url);

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
        } catch (Exception e) {

        }
        return "";

    }

    public void ativarBot(Boolean valor) {
        ativar = valor;
    }

    public Boolean statusBot() {
        return ativar;
    }

    /// pechinchou

    public void raspagemDadosPechinchou() {
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
            String imagem = extractImageReal("https://pechinchou.com.br/oferta/" + slug);
            return imagem != null ? imagemService.salvarImagemUrl(imagem, "produtos-real") : null;
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
        ProdutoScraperDTO produto = handleAmazon(promo.getShortUrl());
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
                    .freteVariacoes("Frete Grátis Algumas Regiões")
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
