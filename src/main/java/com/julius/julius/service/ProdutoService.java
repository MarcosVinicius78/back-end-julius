package com.julius.julius.service;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import javax.imageio.ImageIO;

import com.julius.julius.DTO.response.*;
import com.julius.julius.service.Scraper.Amazon.ImageProcessingService;
import org.apache.commons.io.FileExistsException;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.github.dockerjava.api.exception.NotFoundException;
import com.julius.julius.DTO.ProdutoAtualizarDto;
import com.julius.julius.DTO.ProdutoSalvarDto;
import com.julius.julius.models.Categoria;
import com.julius.julius.models.LinksProdutos;
import com.julius.julius.models.Loja;
import com.julius.julius.models.Produto;
import com.julius.julius.models.Promo;
import com.julius.julius.repository.CategoriaRepository;
import com.julius.julius.repository.LinkProdutoRepository;
import com.julius.julius.repository.LojaRepository;
import com.julius.julius.repository.ProdutoRepository;
import com.julius.julius.repository.ReportRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    private final LojaRepository lojaRepository;

    private final ReportRepository reportRepository;

    private final CategoriaRepository categoriaRepository;

    private final LinkProdutoRepository linkProdutoRepository;

    private final PromoService promoService;

    private final ImageProcessingService imageProcessingService;

    private final ImagemService imagemService;

    private final List<String> colaboradores = Arrays.asList("Alex", "Maria", "Amanda", "Erick");

    private static final String UPLOAD_DIR = "/uploads";

    public LinksProdutos salvarLinkProduto(String url, Long site) {

        LinksProdutos linkProduto = LinksProdutos.builder()
                .url(url)
                .site(site)
                .build();

        return this.linkProdutoRepository.save(linkProduto);
    }

    public ProdutoResponseDto salvarProduto(ProdutoSalvarDto produtoSalvarDto) {
        Random random = new Random();

        Categoria categoria = categoriaRepository.findById(produtoSalvarDto.idCategoria())
                .orElseThrow(() -> new RuntimeException("Categoria inexistente"));
        Loja loja = lojaRepository.findById(produtoSalvarDto.idLoja())
                .orElseThrow(() -> new RuntimeException("Loja inexistente"));

        String colaborador = colaboradores.get(random.nextInt(colaboradores.size()));

        // Verifica se deve criar dois produtos
        boolean criarDoisProdutos =
                !produtoSalvarDto.linkSe().isEmpty() &&
                !produtoSalvarDto.linkOmc().isEmpty() &&
                loja.getNomeLoja().equalsIgnoreCase("magazine luiza") &&
                !produtoSalvarDto.cupomSe().isEmpty() &&
                produtoSalvarDto.cupomSe().toLowerCase().contains("sergipe");

        // Criar e salvar produto principal (SERGIPE ou único)
        Produto produtoPrincipal = criarProduto(produtoSalvarDto, categoria, loja, colaborador, true);

        if (criarDoisProdutos) {
            adicionarLinks(produtoPrincipal, produtoSalvarDto.linkSe(), produtoSalvarDto.linkSeApp(), "", "", true);
            produtoRepository.save(produtoPrincipal);
            produtoPrincipal = criarProduto(produtoSalvarDto, categoria, loja, colaborador, false);
            adicionarLinks(produtoPrincipal, "", "", produtoSalvarDto.linkOmcApp(), produtoSalvarDto.linkOmc(), criarDoisProdutos);
            produtoRepository.save(produtoPrincipal);
        } else {
            adicionarLinks(produtoPrincipal, produtoSalvarDto.linkSe(), produtoSalvarDto.linkSeApp(), produtoSalvarDto.linkOmcApp(), produtoSalvarDto.linkOmc(), criarDoisProdutos);
            produtoRepository.save(produtoPrincipal);
        }

        return ProdutoResponseDto.toResonse(produtoPrincipal);
    }


    /**
     * Cria um novo produto com base no DTO.
     *
     * @param isSergipe Indica se é o produto principal (SERGIPE) ou o duplicado (OMC)
     */
    private Produto criarProduto(ProdutoSalvarDto dto, Categoria categoria, Loja loja, String colaborador, boolean isSergipe) {
        Produto produto = new Produto();

        if (!dto.urlImagem().isEmpty()) {
            produto.setUrlImagem(imagemService.salvarImagemUrl(dto.urlImagem(), "produtos"));
        }

        produto.setTitulo(dto.titulo());
        produto.setPreco(dto.preco());
        produto.setPrecoParcelado(dto.precoParcelado());
//        produto.setDescricao(isSergipe ? dto.link() : dto.descricao());
        produto.setCupom(isSergipe ? dto.cupomSe() : dto.cupomOmc());
        produto.setFreteVariacoes(dto.freteVariacoes());
        produto.setMensagemAdicional(dto.mensagemAdicional());
        produto.setCategoria(categoria);
        produto.setLoja(loja);
        produto.setNomeColaborador(colaborador);
        produto.setCopy(dto.copy());

        try {
            if (!dto.urlImagem().isEmpty()) {
                produto.setImagemSocial(imageProcessingService.processImageFromUrl(dto.urlImagem()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return produto;
    }

    private void adicionarLinks(Produto produto, String linkSe, String linkSeApp, String linkOmcApp, String linkOmc, boolean doisProdutos) {
        adicionarLinkSe(produto, linkSe, linkSeApp, doisProdutos);
        adicionarLinkOmc(produto, linkOmc, linkOmcApp, doisProdutos);
    }

    private void adicionarLinkSe(Produto produto, String linkSe, String linkSeApp, boolean doisProdutos) {
        if (!doisProdutos || !linkSe.isEmpty()) {
            produto.getLinksProdutos().add(salvarLinkProduto(linkSe.isEmpty() ? linkSeApp : linkSe, 1L));
            if (linkSeApp.contains("onelink")) {
                produto.getLinksProdutos().add(salvarLinkProduto(linkSeApp, 1L));
            }
        }
    }

    private void adicionarLinkOmc(Produto produto, String linkOmc, String linkOmcApp, boolean doisProdutos) {
        if (!doisProdutos || !linkOmc.isEmpty()) {
            produto.getLinksProdutos().add(salvarLinkProduto(linkOmc == null || linkOmc.isEmpty() ? linkOmcApp : linkOmc, 2L));
            if (linkOmcApp.contains("onelink")) {
                produto.getLinksProdutos().add(salvarLinkProduto(linkOmcApp, 2L));
            }
        }
    }


    public Page<IProdutoResponseDto> getProdutosPaginados(Long site, Pageable pageable) {
        return produtoRepository.listarProdutos("", site, pageable);
    }

    public ProdutoDto pegarProduto(Long id) {
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Produto não encontrado"));

        List<LinksProdutos> urlSe = produto.getLinksProdutos().stream()
                .filter(lp -> lp.getSite().equals(1L))
                .collect(Collectors.toList());

        List<LinksProdutos> urlOfm = produto.getLinksProdutos().stream()
                .filter(lp -> lp.getSite().equals(2L))
                .collect(Collectors.toList());

        // Obtém os links de forma segura
        String linkAppSe = urlSe.stream()
                .filter(se -> se.getUrl().contains("onelink"))
                .map(LinksProdutos::getUrl)
                .findFirst()
                .orElse("");

        String linkSiteSe = urlSe.stream()
                .filter(se -> !se.getUrl().contains("onelink"))
                .map(LinksProdutos::getUrl)
                .findFirst()
                .orElse("");

        String linkAppOmc = urlOfm.stream()
                .filter(omc -> omc.getUrl().contains("onelink"))
                .map(LinksProdutos::getUrl)
                .findFirst()
                .orElse("");

        String linkSiteOfm = urlOfm.stream()
                .filter(omc -> !omc.getUrl().contains("onelink"))
                .map(LinksProdutos::getUrl)
                .findFirst()
                .orElse("");

        LojaResponseDto lojaResponseDto = LojaResponseDto.toResonse(produto.getLoja());
        CategoriaResponseDto categoriaDto = CategoriaResponseDto.toResonse(produto.getCategoria());

        return ProdutoDto.toResonse(produto, lojaResponseDto, categoriaDto, linkAppSe, linkSiteSe, linkAppOmc, linkSiteOfm);
    }

    @Transactional
    public Boolean apagarProduto(Long id) throws FileExistsException {

        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto não encontrado com ID: " + id));

        // Excluir todos os relatórios relacionados ao produto
        reportRepository.deleteByProdutoReport(id);

        // Excluir relacionamentos do produto com promoções
        produtoRepository.deleteByProdutoPromos(id);

        // Apagar links de produtos associados
        apagarLinksProduto(produto);

        produtoRepository.deleteById(id);
        // Verificar e apagar promoções associadas ao produto
        apagarPromocoesSemProdutos(produto);

        // Apagar imagens associadas ao produto
        imagemService.apagarImagem(UPLOAD_DIR.concat("/produtos/" + produto.getUrlImagem()));
        imagemService.apagarImagem(UPLOAD_DIR.concat("/produtos-real/" + produto.getImagemSocial()));

        return true;
    }

    private void apagarLinksProduto(Produto produto) {
        produto.getLinksProdutos().forEach(link -> {
            linkProdutoRepository.deleteById(link.getId());
        });
    }

    private void apagarPromocoesSemProdutos(Produto produto) {
        System.out.println("Número de promoções associadas: " + produto.getPromos().size());

        for (Promo promo : produto.getPromos()) {
            // Verifique se a promoção está sem produtos
            long produtosCount = produtoRepository.countByPromosId(promo.getId());
            System.out.println("Promo ID " + promo.getId() + " tem produtos? " + (produtosCount > 0));

            if (produtosCount == 0) {
                try {
                    System.out.println("Chamando apagarPromo para promo ID: " + promo.getId());
                    promoService.apagarPromo(promo.getId(), promo.getUrlImagem());
                } catch (Exception e) {
                    System.err.println("Erro ao tentar apagar promo ID " + promo.getId() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

    }

    public void encerrarPromocao(Boolean status, Long id) {

        Produto produto = produtoRepository.findById(id).orElseThrow(() -> new RuntimeException("Produto nao encontrado"));

        if (produto.getCupom() == null || produto.getCupom().isEmpty()) {
            produto.setPromocaoEncerrada(status);
            produtoRepository.save(produto);
            return;
        }

        List<Produto> produtos = produtoRepository.buscarProdutosComCupom(produto.getCupom());

        produtos.forEach(item -> {
            item.setPromocaoEncerrada(status);
        });

        produtoRepository.saveAll(produtos);
    }

    @Transactional
    public ProdutoResponseDto atualizarProduto(ProdutoAtualizarDto produtoAtualizarDto) {
        // Buscar entidades associadas
        Categoria categoria = categoriaRepository.findById(produtoAtualizarDto.idCategoria())
                .orElseThrow(() -> new EntityNotFoundException("Categoria não encontrada com ID " + produtoAtualizarDto.idCategoria()));
        Loja loja = lojaRepository.findById(produtoAtualizarDto.idLoja())
                .orElseThrow(() -> new EntityNotFoundException("Loja não encontrada com ID " + produtoAtualizarDto.idLoja()));
        Produto produto = produtoRepository.findById(produtoAtualizarDto.id())
                .orElseThrow(() -> new EntityNotFoundException("Produto não encontrado com ID " + produtoAtualizarDto.id()));

        // Atualizar os campos do produto
        produto.setTitulo(produtoAtualizarDto.titulo());
        produto.setPreco(produtoAtualizarDto.preco());
        produto.setPrecoParcelado(produtoAtualizarDto.precoParcelado());
        produto.setCupom(produtoAtualizarDto.cupomSe());
        produto.setFreteVariacoes(produtoAtualizarDto.freteVariacoes());
        produto.setMensagemAdicional(produtoAtualizarDto.mensagemAdicional());
        produto.setCategoria(categoria);
        produto.setLoja(loja);
        produto.setCopy(produtoAtualizarDto.copy());

        // Remover links existentes e adicionar os novos
//        linkProdutoRepository.deleteAll(produto.getLinksProdutos());
        produto.setLinksProdutos(new ArrayList<>());
        adicionarLinks(produto, produtoAtualizarDto.linkSe(), produtoAtualizarDto.linkSeApp(), produtoAtualizarDto.linkOmcApp(), produtoAtualizarDto.linkOmc(), true);

        // Salvar e retornar
        return ProdutoResponseDto.toResonse(produtoRepository.save(produto));
    }

    /**
     * Método para adicionar links a um produto.
     */
//    private void adicionarLinks(Produto produto, String linkSe, String linkOmc) {
//        if (linkSe != null && !linkSe.isEmpty()) {
//            produto.getLinksProdutos().add(salvarLinkProduto(linkSe, 1L));
//        }
//        if (linkOmc != null && !linkOmc.isEmpty()) {
//            produto.getLinksProdutos().add(salvarLinkProduto(linkOmc, 2L));
//        }
//    }


    public Page<IProdutoResponseDto> obterProdutosPorCategoria(Long site, Long categoriaId, Pageable pageable) {
        return produtoRepository.buscarProdutosPorCategoria(site, categoriaId, pageable);
    }

    public Page<ProdutoResponseDto> obterProdutosPorLoja(Long site, Long lojaId, Pageable pageable) {
        return produtoRepository.buscarProdutosPorLoja(lojaId, site, pageable).map(ProdutoResponseDto::toResonse);
    }

    @Transactional
    public void apagarVariosProdutos(List<ProdutoDto> produtosSelecionados) {
        List<Long> ids = produtosSelecionados.stream().map(ProdutoDto::id).toList();

        produtoRepository.deleteByIdIn(ids);
    }

    public Page<IProdutoResponseDto> pesquisarProdutos(Long site, String termoPesquisa, int pagina, int tamanho) {

        Pageable pageable = PageRequest.of(pagina, tamanho);

        return produtoRepository.listarProdutos(termoPesquisa, site, pageable);
    }

    public Resource loadImagemAResourceReal(String imagemNome) {
        if (!imagemNome.equals("null")) {

            try {
                File uploadDir = new File(UPLOAD_DIR + "-real");

                Path imagemPath = Paths.get(uploadDir.getAbsolutePath()).resolve(imagemNome);
                Resource resource = new UrlResource(imagemPath.toUri());

                if (resource.exists() || resource.isReadable()) {
                    return resource;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        return null;
    }

    public Page<IProdutoResponseDto> listarProdutosDestaque(Long site, int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        return produtoRepository.listarProdutosDestaque(site, pageable);
    }

    public byte[] gerarStory(String preco, String titulo, String urlImagem, String frete, String cupom)
            throws FileExistsException, FontFormatException {

        try {
            // Carregar a imagem
            BufferedImage image = ImageIO.read(new File(UPLOAD_DIR + "/produtos/story.jpeg"));

            Image foto = ImageIO.read(new File(UPLOAD_DIR + "/produtos-real/" + urlImagem));

            // Carregar a fonte personalizada
            // Carregar a fonte personalizada usando class loader
            InputStream is = getClass().getClassLoader().getResourceAsStream("fonts/Chonky_Cat.ttf");
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, is);
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            ge.registerFont(customFont);

            // Desenhar texto na imagem
            Graphics2D g = image.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g.setColor(Color.BLACK);
            RoundRectangle2D.Float roundedRect = new RoundRectangle2D.Float(65, 150, 950, 950,40, 40);
            // Recortar a imagem no formato arredondado
            Shape originalClip = g.getClip();
            g.setClip(roundedRect);

            g.drawImage(foto, 65, 150, 950, 950, null);

            g.setClip(originalClip);

            // Configurar fonte para o título
            // Font fonteNegrito = new Font(Font.SANS_SERIF, Font.BOLD, 45);
            Font fonteNegrito = customFont.deriveFont(Font.BOLD, 55);
            g.setFont(fonteNegrito);
            FontMetrics fm = g.getFontMetrics();
            int imageWidth = image.getWidth();

            int titleYPosition = 1180;

            // Quebrar o título em múltiplas linhas
            List<String> lines = new ArrayList<>();
            StringBuilder line = new StringBuilder();
            for (String word : titulo.split(" ")) {
                if (fm.stringWidth(line + word + " ") > imageWidth - 180) {
                    lines.add(line.toString());
                    line = new StringBuilder();
                }
                line.append(word).append(" ");
            }
            if (line.length() > 0) {
                lines.add(line.toString());
            }

            // Ajustar e desenhar as linhas do título
            if (lines.size() > 1) {
                // Centralizar a primeira linha
                String titleLine1 = lines.get(0);
                int titleXPosition1 = (imageWidth - fm.stringWidth(titleLine1)) / 2;
                g.drawString(titleLine1, titleXPosition1, titleYPosition);
                titleYPosition += fm.getHeight();

                // Ajustar e desenhar a segunda linha com reticências
                String titleLine2 = lines.get(1).trim();
                int maxWidth = (int) (imageWidth * 0.8);
                while (fm.stringWidth(titleLine2 + "...") > maxWidth && titleLine2.length() > 0) {
                    titleLine2 = titleLine2.substring(0, titleLine2.length() - 1);
                }
                titleLine2 += "...";
                int titleXPosition2 = (imageWidth - fm.stringWidth(titleLine2)) / 2;
                g.drawString(titleLine2, titleXPosition2, titleYPosition);
            } else {
                // Centralizar e desenhar a única linha
                String titleLine = lines.get(0);
                int titleXPosition = (imageWidth - fm.stringWidth(titleLine)) / 2;
                g.drawString(titleLine, titleXPosition, titleYPosition);
            }

            // Definir as coordenadas e dimensões do retângulo do cupom
            int rectX = 430; // exemplo de coordenada X do retângulo
            int rectY = 1290; // exemplo de coordenada Y do retângulo
            int rectWidth = 600; // exemplo de largura do retângulo
            int rectHeight = 100; // exemplo de altura do retângulo

            // g.fillRect(rectX, rectY, rectWidth, rectHeight);

            // Definir fontes com base no tamanho do texto
            Font smallFont = customFont.deriveFont(Font.BOLD, 40);
            Font mediumFont = customFont.deriveFont(Font.BOLD, 40);
            Font largeFont = customFont.deriveFont(Font.BOLD, 40);

            FontMetrics couponFm = g.getFontMetrics();
            int yPosition = rectY + (rectHeight + couponFm.getAscent()) / 2 - 3;
            int maxLines = 33; // Limite de linhas para textos longos

            if (!cupom.isEmpty() && !cupom.equals("null")) {

                // Verificar se o texto é curto (como "TOMA100") ou longo
                if (cupom.length() <= 17) { // Texto curto, ajuste o retângulo ao tamanho do texto
                    g.setFont(largeFont); // Fonte maior para textos curtos
                    FontMetrics metrics = g.getFontMetrics();

                    // Calcular largura e altura do texto
                    int textWidth = metrics.stringWidth("Cupom: " + cupom);
                    int textHeight = metrics.getHeight();

                    // Ajustar o tamanho do retângulo com base no tamanho do texto
                    int dynamicRectWidth = textWidth + 20; // Adicionar margem de 20
                    int dynamicRectHeight = textHeight + 20;

                    // Desenhar retângulo ajustado
                    g.setColor(Color.decode("#9b111e"));
                    g.fillRoundRect(rectX, rectY, dynamicRectWidth, dynamicRectHeight, 30, 30);
                    g.drawRoundRect(rectX, rectY, dynamicRectWidth, dynamicRectHeight, 30, 30);
                    g.setColor(Color.white);

                    // Centralizar o texto no retângulo
                    int xPosition = rectX + (dynamicRectWidth - textWidth) / 2;
                    int yPositionAdjusted = rectY + ((dynamicRectHeight - textHeight) / 2) + metrics.getAscent();
                    g.drawString("Cupom: " + cupom, xPosition, yPositionAdjusted);

                } else { // Texto longo, aplique quebra de linha e limite de 33 linhas
                    g.setFont(smallFont); // Fonte menor para textos longos
                    FontMetrics metrics = g.getFontMetrics();

                    // Dividir o texto em várias linhas
                    List<String> textoQuebrado = wrapTextToRectangle(cupom,
                            metrics, rectWidth);

                    g.setColor(Color.decode("#9b111e"));
                    g.fillRoundRect(rectX, rectY, rectWidth, rectHeight, 30, 30);
                    g.drawRoundRect(rectX, rectY, rectWidth, rectHeight, 30, 30);
                    g.setColor(Color.white);

                    // Se o número de linhas for maior que 33, cortar e adicionar reticências
                    if (textoQuebrado.size() > maxLines) {
                        textoQuebrado = textoQuebrado.subList(0, maxLines); // Manter apenas as primeiras 33 linhas
                        String ultimaLinha = textoQuebrado.get(maxLines - 1); // Última linha
                        ultimaLinha = ultimaLinha.substring(0, Math.min(ultimaLinha.length(), ultimaLinha.length() - 3))
                                      + "..."; // Adicionar reticências
                        textoQuebrado.set(maxLines - 1, ultimaLinha); // Atualizar a última linha
                    }

                    // Calcular a altura total do texto (considerando várias linhas)
                    int totalTextHeight = textoQuebrado.size() * metrics.getHeight();
                    int startY = rectY + (rectHeight - totalTextHeight) / 2 + metrics.getAscent(); // Início para
                    // centralizar

                    // Desenhar o texto linha por linha
                    for (String linha : textoQuebrado) {
                        int textWidth = metrics.stringWidth(linha);
                        int xPosition = rectX + (rectWidth - textWidth) / 2; // Centralizar horizontalmente
                        g.drawString(linha, xPosition, startY);
                        startY += metrics.getHeight(); // Mover para a próxima linha
                    }
                }
            }

            g.setColor(Color.black);

            // estilização do preço nos stories
            Font fonteNegritoPreco = customFont.deriveFont(Font.BOLD, 99);
            if (preco.length() > 19) {
                fonteNegritoPreco = customFont.deriveFont(Font.BOLD, 85);
                g.setFont(fonteNegritoPreco);
            } else if (preco.length() > 14) {
                fonteNegritoPreco = customFont.deriveFont(Font.BOLD, 70);
                g.setFont(fonteNegritoPreco);
            }
            g.setFont(fonteNegritoPreco);
            FontMetrics priceFm = g.getFontMetrics();
            int priceXPosition = (imageWidth - priceFm.stringWidth(preco)) / 2;
            g.drawString(preco, priceXPosition, 1781);

            g.dispose();
            // Converter a imagem para um array de bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);
            byte[] bytes = baos.toByteArray();

            // Retornar a imagem gerada
            return bytes;
        } catch (IOException e) {
            e.printStackTrace();
            throw new FileExistsException();
        }
    }

    private List<String> wrapTextToRectangle(String text, FontMetrics fm, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String lineWithWord = currentLine + (currentLine.length() > 0 ? " " : "") + word;
            if (fm.stringWidth(lineWithWord) > maxWidth) {
                lines.add(currentLine.toString());
                currentLine = new StringBuilder(word);
            } else {
                currentLine.append(currentLine.length() > 0 ? " " : "").append(word);
            }
        }
        lines.add(currentLine.toString()); // Adicionar a última linha

        return lines;
    }

    public void salvarStory(MultipartFile file) throws FileUploadException {
        salvarStories(file);
    }

    private void salvarStories(MultipartFile file) throws FileUploadException {

        try {
            File uploadsDir = new File(UPLOAD_DIR);
            if (!uploadsDir.exists()) {
                uploadsDir.mkdirs();
            }

            String fileName = file.getOriginalFilename();
            Path filePath = Path.of(uploadsDir.getAbsolutePath(), fileName);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String imagemUrl = uploadsDir.getAbsolutePath() + fileName;

        } catch (Exception e) {
            throw new FileUploadException();
        }

    }

    public byte[] gerarFeed(Long id) throws IOException, FontFormatException {
        BufferedImage image = ImageIO.read(new File(UPLOAD_DIR + "/produtos" + "/feed.jpeg"));

        Produto produto = produtoRepository.findById(id).orElseThrow();

        File imageFile = new File(UPLOAD_DIR + "/produtos-real/" + produto.getImagemSocial());
        Image foto = ImageIO.read(imageFile);

        // Carregar a fonte personalizada
        InputStream is = getClass().getClassLoader().getResourceAsStream("fonts/Chonky_Cat.ttf");
        Font customFont = Font.createFont(Font.TRUETYPE_FONT, is);
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        ge.registerFont(customFont);

        // Desenhar a imagem e o texto na imagem
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.BLACK);
        RoundRectangle2D.Float roundedRect = new RoundRectangle2D.Float(86, 225, 910, 930,40, 40);
        // Recortar a imagem no formato arredondado
        Shape originalClip = g.getClip();
        g.setClip(roundedRect);
        g.drawImage(foto, 86, 225, 910, 930, null);

        g.setClip(originalClip);
        // Desenhar o preço no retângulo
        int fontSize = 50;
        Font priceFont = customFont.deriveFont(Font.BOLD, fontSize);
        int yPreco = 1180;
        int xPreco = 740;
        int width = 220;
        int height = 90;
        FontMetrics metrics;
        int textWidth;
        int textHeight;
        g.setColor(Color.white);

        // Ajustar o tamanho da fonte do preço para caber no retângulo
        do {
            priceFont = customFont.deriveFont(Font.BOLD, fontSize);
            g.setFont(priceFont);
            metrics = g.getFontMetrics(priceFont);
            textWidth = metrics.stringWidth(produto.getPreco());
            textHeight = metrics.getHeight();
            fontSize--;
        } while (textWidth > width - 20 || textHeight > height - 20); // Deixar 20px de margem

        // Centralizar o preço dentro do retângulo
        int x = xPreco + (width - textWidth) / 2;
        int y = yPreco + ((height - textHeight) / 2) + metrics.getAscent();

        // Desenhar o preço
        g.drawString(produto.getPreco(), x, y);

        // Configuração da cor de fundo (preto)
        g.setColor(Color.BLACK);
        // g.fillRect(130, 960, 530, 90);

        // Configuração da fonte e cor do texto
        // g.setColor(Color.white);
        g.setFont(customFont.deriveFont(Font.BOLD, 30));

        // Retângulo de exemplo (posição e tamanho)
        int rectX = 130;
        int rectY = 1180;
        int rectWidth = 540; // Largura do retângulo
        int rectHeight = 90; // Altura do retângulo (aproximadamente 2 linhas de texto)

        // Desenhar o texto no retângulo
        drawTextInRectangle2(g, produto.getTitulo(), rectX, rectY, rectWidth, rectHeight);

        g.dispose();

        // Converter a imagem para um array de bytes
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos);
        byte[] bytes = baos.toByteArray();

        // Retornar a imagem gerada
        return bytes;
    }

    private static void drawTextInRectangle2(Graphics2D g2d, String text, int x, int y, int width, int height) {
        FontMetrics fm = g2d.getFontMetrics();
        int lineHeight = fm.getHeight(); // Altura de cada linha de texto

        // Lista para armazenar as linhas de texto
        List<String> lines = new ArrayList<>();

        // Divide o texto em palavras
        String[] words = text.split(" ");
        StringBuilder currentLine = new StringBuilder();

        for (String word : words) {
            String testLine = currentLine + word + " ";
            int lineWidth = fm.stringWidth(testLine);

            // Se a largura da linha for menor que a largura do retângulo, adiciona a
            // palavra à linha atual
            if (lineWidth < width) {
                currentLine.append(word).append(" ");
            } else {
                // Se a linha estiver cheia, adiciona a linha à lista de linhas e começa uma
                // nova
                lines.add(currentLine.toString().trim());
                currentLine = new StringBuilder(word).append(" ");
            }

            // Se já tiver duas linhas, para de adicionar palavras
            if (lines.size() == 2) {
                break;
            }
        }

        // Adiciona a última linha (caso ela não tenha sido adicionada ainda)
        if (!currentLine.toString().isEmpty() && lines.size() < 2) {
            lines.add(currentLine.toString().trim());
        }

        // Se houver mais de duas linhas, corta a segunda linha e adiciona "..."
        if (lines.size() == 2 && fm.stringWidth(lines.get(1)) > width) {
            String truncatedLine = truncateToFit(g2d, lines.get(1), width);
            lines.set(1, truncatedLine);
        }

        // Centralização vertical: calcula o ponto de partida para que o texto fique
        // centralizado verticalmente
        int totalTextHeight = lines.size() * lineHeight; // Altura total do texto
        int startY = y + (height - totalTextHeight) / 2 + fm.getAscent(); // Posiciona a primeira linha

        // Desenha as linhas no retângulo com centralização horizontal
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            int lineWidth = fm.stringWidth(line);
            int startX = x + (width - lineWidth) / 2; // Calcula o ponto de partida para centralizar horizontalmente
            g2d.drawString(line, startX, startY + (i * lineHeight));
        }
    }

    private static String truncateToFit(Graphics2D g2d, String text, int maxWidth) {
        FontMetrics fm = g2d.getFontMetrics();
        String ellipsis = "...";
        int ellipsisWidth = fm.stringWidth(ellipsis);

        // Trunca a linha até que ela caiba no espaço disponível
        for (int i = text.length() - 1; i > 0; i--) {
            String testLine = text.substring(0, i) + ellipsis;
            if (fm.stringWidth(testLine) <= maxWidth) {
                return testLine;
            }
        }

        return ellipsis; // Se não couber nada, retorna apenas "..."
    }

    // Método para desenhar texto com quebra de linha e reticências
    private void drawTextInRectangle(Graphics2D g, String text, int x, int y, int width, int height, Font font) {
        FontMetrics metrics = g.getFontMetrics(font);
        int lineHeight = metrics.getHeight();
        String line1 = "", line2 = "";
        boolean fits = false;

        // Ajustar o texto em até duas linhas
        for (int i = 0; i < text.length(); i++) {
            String testLine = text.substring(0, i + 1);
            int testLineWidth = metrics.stringWidth(testLine);
            if (testLineWidth > width) {
                line1 = text.substring(0, i);
                String remainingText = text.substring(i);

                // Ajustar a segunda linha com reticências, se necessário
                for (int j = 0; j < remainingText.length(); j++) {
                    String testLine2 = remainingText.substring(0, j + 1);
                    int testLine2Width = metrics.stringWidth(testLine2 + "...");
                    if (testLine2Width > width) {
                        line2 = remainingText.substring(0, j) + "...";
                        fits = true;
                        break;
                    }
                }
                if (!fits) {
                    line2 = remainingText;
                }
                break;
            }
        }

        if (line1.isEmpty()) {
            line1 = text;
        }

        // Desenhar as duas linhas dentro do retângulo
        g.drawString(line1, x, y + metrics.getAscent());
        if (!line2.isEmpty()) {
            g.drawString(line2, x, y + lineHeight + metrics.getAscent());
        }
    }

    @Transactional
    @Scheduled(cron = "0 0 23 * * ?")
    public void deletarProdutosAntigos() throws FileExistsException {
        LocalDateTime dataLimite = LocalDateTime.now().minusDays(7);
        List<Produto> produtosAntigos = produtoRepository.findProdutosComMaisDe7Dias(dataLimite);

        for (Produto produto : produtosAntigos) {
            // 🔹 Remove referências em outras tabelas primeiro
            reportRepository.deleteByProdutoReport(produto.getId());
            produtoRepository.deleteByProdutoPromos(produto.getId());

            // 🔹 Remove links associados ao produto
            if (produto.getLinksProdutos() != null && !produto.getLinksProdutos().isEmpty()) {
                linkProdutoRepository.deleteAll(produto.getLinksProdutos());
            }

            // 🔹 Remove promoções associadas
            apagarPromocoesSemProdutos(produto);

            // 🔹 Exclui as imagens associadas
            if (produto.getUrlImagem() != null) {
                imagemService.apagarImagem(UPLOAD_DIR.concat("/produtos/" + produto.getUrlImagem()));
            }
            if (produto.getImagemSocial() != null) {
                imagemService.apagarImagem(UPLOAD_DIR.concat("/produtos-real/" + produto.getImagemSocial()));
            }
        }

        // 🔹 Agora, excluímos os produtos SEM referências ativas
        produtoRepository.deleteAll(produtosAntigos);
    }
}
