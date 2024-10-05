package com.julius.julius.service;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.RenderingHints;
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
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;

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
import com.julius.julius.DTO.response.CategoriaResponseDto;
import com.julius.julius.DTO.response.LojaResponseDto;
import com.julius.julius.DTO.response.ProdutoDto;
import com.julius.julius.DTO.response.ProdutoPesquisa;
import com.julius.julius.DTO.response.ProdutoResponseDto;
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

    private static final String UPLOAD_DIR = "/uploads/produtos";

    private String salvarImagemReal(MultipartFile fileSocial) {
        File uploadsDir = new File(UPLOAD_DIR + "-real");
        if (!uploadsDir.exists()) {
            uploadsDir.mkdirs();
        }

        Date data = new Date();

        String fileName = fileSocial.getOriginalFilename();
        String nomeImagem = data.getTime() + fileName;
        Path filePath = Path.of(uploadsDir.getAbsolutePath(), nomeImagem);

        try {
            Files.copy(fileSocial.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {

            e.printStackTrace();
        }

        return nomeImagem;
    }

    public String salvarImagemProduto(MultipartFile file, Long id, String urlImagem, MultipartFile fileSocial,
            String urlImagemReal)
            throws FileUploadException, FileExistsException {

        Optional<Produto> produto = produtoRepository.findById(id);

        String nomeImagem = "";
        String imagemUrl = "";

        try {
            if (file != null) {
                if (urlImagem != null) {
                    apagarImagem(urlImagem);
                }
                File uploadsDir = new File(UPLOAD_DIR);
                if (!uploadsDir.exists()) {
                    uploadsDir.mkdirs();
                }

                Date data = new Date();

                String fileName = file.getOriginalFilename();
                nomeImagem = data.getTime() + fileName;
                Path filePath = Path.of(uploadsDir.getAbsolutePath(), nomeImagem);

                Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                imagemUrl = uploadsDir.getAbsolutePath() + fileName;

                produto.get().setUrlImagem(nomeImagem);

                if (fileSocial != null) {
                    nomeImagem = salvarImagemReal(fileSocial);
                    produto.get().setImagemSocial(nomeImagem);
                }
            }

            if (fileSocial != null) {
                if (urlImagemReal != null) {
                    apagarImagemReal(urlImagemReal);
                }
                nomeImagem = salvarImagemReal(fileSocial);
                produto.get().setImagemSocial(nomeImagem);
            }

            produtoRepository.save(produto.get());

            return imagemUrl;
        } catch (Exception e) {
            throw new FileUploadException();
        }

    }

    public String salvarImagem(String url) {

        try {

            URL file = new URL(url);

            File uploadsDir = new File(UPLOAD_DIR);
            if (!uploadsDir.exists()) {
                uploadsDir.mkdirs();
            }

            Date data = new Date();

            String fileName = url.toString().substring(url.lastIndexOf("/") + 1);

            String nomeImagem = data.getTime() + fileName;
            Path filePath = Path.of(uploadsDir.getAbsolutePath(), nomeImagem);

            Files.copy(file.openStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            return nomeImagem;

        } catch (Exception e) {
            throw new NotFoundException("Imagem não foi salva");
        }
    }

    private LinksProdutos salvarLinkProduto(String url, Long site) {

        LinksProdutos linkProduto = LinksProdutos.builder()
                .url(url)
                .site(site)
                .build();

        return this.linkProdutoRepository.save(linkProduto);
    }

    public ProdutoResponseDto salvarProduto(ProdutoSalvarDto produtoSalvarDto) {

        Optional<Categoria> categoria = categoriaRepository.findById(produtoSalvarDto.id_categoria());
        Optional<Loja> loja = lojaRepository.findById(produtoSalvarDto.id_loja());

        Produto produto = new Produto();

        if (!produtoSalvarDto.urlImagem().equals("")) {
            produto.setUrlImagem(salvarImagem(produtoSalvarDto.urlImagem()));
        } else {
            produto.setUrlImagem("");
        }

        produto.setTitulo(produtoSalvarDto.titulo());
        produto.setPreco(produtoSalvarDto.preco());
        produto.setPrecoParcelado(produtoSalvarDto.precoParcelado());
        produto.setDescricao(produtoSalvarDto.descricao());
        produto.setCupom(produtoSalvarDto.cupom());
        produto.setFreteVariacoes(produtoSalvarDto.freteVariacoes());
        produto.setMensagemAdicional(produtoSalvarDto.mensagemAdicional());
        produto.setCategoria(categoria.get());
        produto.setLoja(loja.get());

        if (!produtoSalvarDto.link_se().isEmpty()) {
            LinksProdutos linksProdutosSe = salvarLinkProduto(produtoSalvarDto.link_se(), 1L);
            produto.getLinksProdutos().add(linksProdutosSe);
        }
        if (!produtoSalvarDto.link_ofm().isEmpty() && produtoSalvarDto.link_ofm() != null) {
            LinksProdutos linksProdutosOfm = salvarLinkProduto(produtoSalvarDto.link_ofm(), 2L);
            produto.getLinksProdutos().add(linksProdutosOfm);
        }
        produto.setLink(produtoSalvarDto.link_se());
        produto.setCopy(produtoSalvarDto.copy());

        loja.get().getProdutos().add(produto);

        return ProdutoResponseDto.toResonse(produtoRepository.save(produto), "");
    }

    public Page<ProdutoResponseDto> getProdutosPaginados(Long site, Pageable pageable) {

        Page<ProdutoResponseDto> produtoTeste = null;

        produtoTeste = produtoRepository.listarProdutos(site, pageable)
                .map(produto -> {
                    Loja loja = new Loja();
                    loja.setUrlImagem(produto.imagemLoja());
                    loja.setNomeLoja(produto.nomeLoja());
                    ProdutoResponseDto produtoResponseDto = new ProdutoResponseDto(
                            produto.id(),
                            produto.titulo(),
                            produto.preco(), produto.parcelado(), null, produto.cupom(), produto.link(),
                            null, produto.freteVariacoes(), produto.dataCriacao(),
                            produto.imagem(), LojaResponseDto.toResonse(loja), produto.imagemSocial(), produto.copy(),
                            produto.mensagemAdicional(), produto.promocaoEncerrada());
                    return produtoResponseDto;
                });

        if (produtoTeste.isEmpty()) {
            return Page.empty();
        }

        return produtoTeste;
    }

    // public Page<ProdutoResponseDto> getProdutosPaginados(Long site, Pageable
    // pageable) {

    // Page<ProdutoResponseDto> produtoTeste = null;

    // if (site == 1) {
    // produtoTeste = produtoRepository.findProdutosSe(2L,pageable)
    // .map(produto -> ProdutoResponseDto.toResonse(produto,
    // produtoRepository.sfindByProdutoBySite(produto.getId(), 1L)));
    // } else {
    // produtoTeste = produtoRepository.findProdutosOfm(site, pageable)
    // .map(produto -> ProdutoResponseDto.toResonse(produto,
    // produtoRepository.sfindByProdutoBySite(produto.getId(), 2L)));
    // }

    // if (produtoTeste.isEmpty()) {
    // return Page.empty();
    // }

    // return produtoTeste;
    // }

    public ProdutoDto pegarProduto(Long id) {

        // Optional<Produto> produto = produtoRepository.findById(id);
        Optional<Produto> produto = produtoRepository.findById(id);
        String urlSe = produtoRepository.sfindByProdutoBySite(id, 1L);
        String urlOfm = produtoRepository.sfindByProdutoBySite(id, 2L);

        if (!produto.isPresent()) {
            return null;
        }

        LojaResponseDto lojaResponseDto = LojaResponseDto.toResonse(produto.get().getLoja());
        CategoriaResponseDto categoriaDto = CategoriaResponseDto.toResonse(produto.get().getCategoria());

        return ProdutoDto.toResonse(produto.get(), lojaResponseDto, categoriaDto, urlSe, urlOfm);
    }

    @Transactional
    public Boolean apagarProduto(Long id, String urlImagem, String imagemSocial) throws FileExistsException {

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
        apagarImagensProduto(urlImagem, imagemSocial);

        // Por fim, excluir o próprio produto

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

    private void apagarImagensProduto(String urlImagem, String imagemSocial) throws FileExistsException {
        if (urlImagem != null && !urlImagem.isEmpty()) {
            apagarImagem(urlImagem);
        }

        if (imagemSocial != null && !imagemSocial.equals("null") && !imagemSocial.isEmpty()) {
            apagarImagemReal(imagemSocial);
        }
    }

    private void apagarImagem(String urlImagem) throws FileExistsException {

        String caminhoImagem = UPLOAD_DIR + "/" + urlImagem;

        if (!urlImagem.isEmpty()) {
            File arquivoImagem = new File(caminhoImagem);
            if (arquivoImagem.exists()) {
                arquivoImagem.delete();
            } else {
                throw new FileExistsException("Imagem não existe");
            }
        }

    }

    private void apagarImagemReal(String urlImagem) throws FileExistsException {

        String caminhoImagem = UPLOAD_DIR + "-real" + "/" + urlImagem;

        if (!urlImagem.isEmpty()) {
            File arquivoImagem = new File(caminhoImagem);
            if (arquivoImagem.exists()) {
                arquivoImagem.delete();
            } else {
                throw new FileExistsException("Imagem não existe");
            }
        }

    }

    public Boolean encerrarPromocao(Boolean status, Long id) {

        Optional<Produto> produto = produtoRepository.findById(id);

        produto.get().setPromocaoEncerrada(status);

        if (produtoRepository.save(produto.get()) != null) {
            return true;
        }

        return false;
    }

    @Transactional
    public ProdutoResponseDto atualizarProduto(ProdutoAtualizarDto produtoAtualizarDto) {

        Categoria categoria = categoriaRepository.findById(produtoAtualizarDto.id_categoria())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Categoria not found with id " + produtoAtualizarDto.id_categoria()));
        Loja loja = lojaRepository.findById(produtoAtualizarDto.id_loja())
                .orElseThrow(
                        () -> new EntityNotFoundException("Loja not found with id " + produtoAtualizarDto.id_loja()));
        Produto produto = produtoRepository.findById(produtoAtualizarDto.id())
                .orElseThrow(
                        () -> new EntityNotFoundException("Produto not found with id " + produtoAtualizarDto.id()));

        List<LinksProdutos> linksProdutos = produto.getLinksProdutos();

        // Atualização ou remoção de links existentes
        Iterator<LinksProdutos> iterator = linksProdutos.iterator();
        while (iterator.hasNext()) {
            LinksProdutos linkProduto = iterator.next();
            if (linkProduto.getSite() == 1 && produtoAtualizarDto.link_se() != null
                    && !produtoAtualizarDto.link_se().isEmpty()) {
                linkProdutoRepository.atualizarUrlSe(produtoAtualizarDto.link_se(), 1L, linkProduto.getId());
            } else if (linkProduto.getSite() == 2 && produtoAtualizarDto.link_ofm() != null
                    && !produtoAtualizarDto.link_ofm().isEmpty()) {
                linkProdutoRepository.atualizarUrlSe(produtoAtualizarDto.link_ofm(), 2L, linkProduto.getId());
            } else {
                iterator.remove();
                linkProdutoRepository.deletarChaveEstrangeiraLink(linkProduto.getId());
                linkProdutoRepository.deleteById(linkProduto.getId());
            }
        }

        // Adicionando novos links, se necessário
        if (produtoAtualizarDto.link_se() != null && !produtoAtualizarDto.link_se().isEmpty()
                && linksProdutos.stream().noneMatch(lp -> lp.getSite() == 1)) {
            LinksProdutos novoLinkSe = salvarLinkProduto(produtoAtualizarDto.link_se(), 1L);
            produto.getLinksProdutos().add(novoLinkSe);
        }
        if (produtoAtualizarDto.link_ofm() != null && !produtoAtualizarDto.link_ofm().isEmpty()
                && linksProdutos.stream().noneMatch(lp -> lp.getSite() == 2)) {
            LinksProdutos novoLinkOfm = salvarLinkProduto(produtoAtualizarDto.link_ofm(), 2L);
            produto.getLinksProdutos().add(novoLinkOfm);
        }

        produto.setId(produtoAtualizarDto.id());
        produto.setTitulo(produtoAtualizarDto.titulo());
        produto.setPreco(produtoAtualizarDto.preco());
        produto.setPrecoParcelado(produtoAtualizarDto.precoParcelado());
        produto.setDescricao(produtoAtualizarDto.descricao());
        produto.setLink(produtoAtualizarDto.link_se());
        // linkProdutoRepository.atualizarUrlsPorProduto(produtoAtualizarDto.id(),
        produto.setCupom(produtoAtualizarDto.cupom());
        produto.setFreteVariacoes(produtoAtualizarDto.freteVariacoes());
        produto.setMensagemAdicional(produtoAtualizarDto.mensagemAdicional());
        produto.setCategoria(categoria);
        produto.setLoja(loja);
        produto.setCopy(produtoAtualizarDto.copy());

        return ProdutoResponseDto.toResonse(this.produtoRepository.save(produto), "");
    }

    public Page<ProdutoResponseDto> obterProdutosPorCategoria(Long site, Long categoriaId, Pageable pageable) {
        if (site == 1) {
            return produtoRepository.findByCategoriIdOrderByDataCriacaoDesc(categoriaId, pageable)
                    .map(produto -> ProdutoResponseDto.toResonse(produto,
                            produtoRepository.sfindByProdutoBySite(produto.getId(), 1L)));
        }

        return produtoRepository.findCategoriIdOrderByDataCriacaoDesc(categoriaId, pageable)
                .map(produto -> ProdutoResponseDto.toResonse(produto,
                        produtoRepository.sfindByProdutoBySite(produto.getId(), 2L)));
    }

    // public Page<ProdutoResponseDto> obterProdutosPorCategoria(Long site,Long
    // categoriaId, Pageable pageable) {
    // if (site == 1) {
    // return
    // produtoRepository.findByCategoriIdOrderByDataCriacaoDesc(categoriaId,pageable)
    // .map(produto -> ProdutoResponseDto.toResonse(produto,
    // produtoRepository.sfindByProdutoBySite(produto.getId(), 1L)));
    // }

    // return
    // produtoRepository.findCategoriIdOrderByDataCriacaoDesc(categoriaId,pageable)
    // .map(produto -> ProdutoResponseDto.toResonse(produto,
    // produtoRepository.sfindByProdutoBySite(produto.getId(), 2L)));
    // }

    @Transactional
    public void apagarVariosProdutos(List<ProdutoDto> produtosSelecionados) {
        List<Long> ids = produtosSelecionados.stream().map(ProdutoDto::id).toList();

        produtoRepository.deleteByIdIn(ids);
    }

    public Page<ProdutoResponseDto> pesquisarProdutos(Long site, String termoPesquisa, int pagina, int tamanho) {

        Pageable pageable = PageRequest.of(pagina, tamanho);

        Page<ProdutoResponseDto> produtosResponse = produtoRepository.procurarProdutos(termoPesquisa, site, pageable)
                .map(produtoPesquisa -> {
                    Loja loja = new Loja();
                    loja.setUrlImagem(produtoPesquisa.imagemLoja());
                    ProdutoResponseDto produtoResponseDto = new ProdutoResponseDto(
                            produtoPesquisa.id(),
                            produtoPesquisa.titulo(),
                            produtoPesquisa.preco(), produtoPesquisa.parcelado(),
                            "", produtoPesquisa.cupom(),
                            produtoPesquisa.link(), "",
                            produtoPesquisa.freteVariacoes(), produtoPesquisa.dataCriacao(),
                            produtoPesquisa.imagem(),
                            LojaResponseDto.toResonse(loja),
                            produtoPesquisa.imagemSocial(), "",
                            "", false);
                    return produtoResponseDto;
                });

        return produtosResponse;

        // return produtoRepository.procurarProdutos(termoPesquisa, site, pageable)
        // .map(produto -> ProdutoResponseDto.toResonse(produto, ""));

    }

    public Resource loadImagemAResource(String imagemNome) {

        try {
            File uploadDir = new File(UPLOAD_DIR);

            Path imagemPath = Paths.get(uploadDir.getAbsolutePath()).resolve(imagemNome);
            Resource resource = new UrlResource(imagemPath.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Resource loadImagemAResourceReal(String imagemNome) {
        if (!imagemNome.equals("null")) {
            System.out.println(imagemNome);
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

    public Page<ProdutoResponseDto> listarProdutosDestaque(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

        Page<ProdutoResponseDto> produtosPage = produtoRepository.listarProdutosDestaque(pageable)
                .map(produto -> ProdutoResponseDto.toResonse(produto,
                        produtoRepository.sfindByProdutoBySite(produto.getId(), 1L)));
        System.out.println(produtosPage.getNumber());
        return produtosPage;
    }

    public byte[] gerarStory(String preco, String titulo, String urlImagem, String frete, String cupom)
            throws FileExistsException, FontFormatException {

        try {
            // Carregar a imagem
            BufferedImage image = ImageIO.read(new File(UPLOAD_DIR + "/story.jpeg"));

            Image foto = ImageIO.read(new File(UPLOAD_DIR + "-real" + "/" + urlImagem));

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
            g.drawImage(foto, 69, 130, 950, 880, null);

            // Configurar fonte para o título
            // Font fonteNegrito = new Font(Font.SANS_SERIF, Font.BOLD, 45);
            Font fonteNegrito = customFont.deriveFont(Font.BOLD, 55);
            g.setFont(fonteNegrito);
            FontMetrics fm = g.getFontMetrics();
            int imageWidth = image.getWidth();
            int titleYPosition = 1080;

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
            int rectX = 380; // exemplo de coordenada X do retângulo
            int rectY = 1180; // exemplo de coordenada Y do retângulo
            int rectWidth = 580; // exemplo de largura do retângulo
            int rectHeight = 120; // exemplo de altura do retângulo

            // g.fillRect(rectX, rectY, rectWidth, rectHeight);

            // Fontes baseadas no tamanho do texto
            Font smallFont = customFont.deriveFont(Font.BOLD, 38);
            Font mediumFont = customFont.deriveFont(Font.BOLD, 42);
            Font largeFont = customFont.deriveFont(Font.BOLD, 55);

            // Font font = customFont.deriveFont(Font.BOLD, 55);
            // g.setFont(font);
            FontMetrics couponFm = g.getFontMetrics();
            // int couponXPosition = rectX + (rectWidth - couponFm.stringWidth("Cupom: " +
            // cupom)) / 3;
            int xPosition;
            FontMetrics metrics = g.getFontMetrics();
            int yPosition = rectY + (rectHeight + couponFm.getAscent()) / 2 - 3;

            if (!cupom.isEmpty() && !cupom.equals("null")) {
                if (cupom.length() <= 6) {
                    g.setFont(largeFont);
                    // g.drawString("Cupom: " + cupom, couponXPosition, couponYPosition);
                } else if (cupom.length() <= 16) {
                    g.setFont(mediumFont);
                    // g.drawString("Cupom: " + cupom, couponXPosition, couponYPosition);
                } else if (cupom.length() == 17) {
                    g.setFont(smallFont);
                } else {

                    int fontSize = 120;
                    Font font = customFont.deriveFont(Font.BOLD, fontSize);

                    List<String> textoQuebrado = wrapTextToRectangle(cupom, metrics, rectWidth);

                    while ((textoQuebrado.size() * metrics.getHeight()) > rectHeight && fontSize > 1) {
                        fontSize--;
                        font = customFont.deriveFont(Font.BOLD, fontSize);
                        g.setFont(font);
                        metrics = g.getFontMetrics();
                        textoQuebrado = wrapTextToRectangle(cupom, metrics, rectWidth);
                    }

                    // Calcular a posição Y inicial para centralizar verticalmente o bloco de texto
                    int totalTextHeight = textoQuebrado.size() * metrics.getHeight();
                    int startY = rectY + (rectHeight - totalTextHeight) / 2 + fm.getAscent();

                    // Desenhar cada linha de text
                    int lineY = startY;
                    for (String i : textoQuebrado) {

                        xPosition = rectX + (rectWidth - metrics.stringWidth(i)) / 2;
                        g.drawString(i, xPosition, lineY);
                        lineY += metrics.getHeight();
                    }

                }

                if (cupom.length() <= 17) {
                    metrics = g.getFontMetrics();
                    xPosition = rectX + (rectWidth - metrics.stringWidth("Cupom: " + cupom)) / 2;

                    g.drawString("Cupom: " + cupom, xPosition, yPosition);
                } else {

                }
            } else if (!frete.isEmpty() && frete.length() == 18) {
                g.setFont(mediumFont);
                metrics = g.getFontMetrics();
                xPosition = rectX + (rectWidth - metrics.stringWidth(frete)) / 2;
                // frete grátis prime
                g.drawString(frete, xPosition, yPosition);
                // g.drawString(frete, couponXPosition, couponYPosition);
            } else if (!frete.isEmpty() && frete.length() == 12 || frete.length() > 45) {
                g.setFont(largeFont);
                metrics = g.getFontMetrics();
                xPosition = rectX + (rectWidth - metrics.stringWidth(frete)) / 2;
                // frete grátis
                g.drawString("Frete Grátis", xPosition, yPosition);
            } else if (!frete.isEmpty() && frete.length() == 15) {
                // frete econômico
                g.setFont(largeFont);
                metrics = g.getFontMetrics();
                xPosition = rectX + (rectWidth - metrics.stringWidth(frete)) / 2;
                g.drawString(frete, xPosition, yPosition);
            } else if (!frete.isEmpty() && frete.length() == 30) {
                // Font font = customFont.deriveFont(Font.BOLD, 45);
                // g.setFont(font);

                // g.setFont(mediumFont);
                // metrics = g.getFontMetrics();
                int fontSize = 120;
                Font font = customFont.deriveFont(Font.BOLD, fontSize);

                frete = frete.replace("*", "");

                List<String> textoQuebrado = wrapTextToRectangle(frete, metrics, rectWidth);

                while ((textoQuebrado.size() * metrics.getHeight()) > rectHeight && fontSize > 1) {
                    fontSize--;
                    font = customFont.deriveFont(Font.BOLD, fontSize);
                    g.setFont(font);
                    metrics = g.getFontMetrics();
                    textoQuebrado = wrapTextToRectangle(frete, metrics, rectWidth);
                }

                // Calcular a posição Y inicial para centralizar verticalmente o bloco de texto
                int totalTextHeight = textoQuebrado.size() * metrics.getHeight();
                int startY = rectY + (rectHeight - totalTextHeight) / 2 + fm.getAscent();

                // Desenhar cada linha de texto
                int lineY = startY;
                for (String i : textoQuebrado) {

                    // g.setFont(mediumFont);
                    // metrics = g.getFontMetrics();
                    xPosition = rectX + (rectWidth - metrics.stringWidth(i)) / 2;
                    g.drawString(i, xPosition, lineY);
                    lineY += metrics.getHeight();

                }

                // frete grátis algumas regioes
                // g.drawString("Frete Grátis", 540, 1232);
                // g.drawString("Algumas Regiões", 460, 1282);
            } else if (!frete.isEmpty() && frete.length() == 24) {
                // gratis retirando na loja
                // Font font = customFont.deriveFont(Font.BOLD, 45);
                // g.setFont(font);
                // // g.setFont(font);
                // g.drawString("Grátis Retirando", 477, 1232);
                // g.drawString("Na Loja", 590, 1282);

                int fontSize = 120;
                Font font = customFont.deriveFont(Font.BOLD, fontSize);

                List<String> textoQuebrado = wrapTextToRectangle(frete, metrics, rectWidth);

                while ((textoQuebrado.size() * metrics.getHeight()) > rectHeight && fontSize > 1) {
                    fontSize--;
                    font = customFont.deriveFont(Font.BOLD, fontSize);
                    g.setFont(font);
                    metrics = g.getFontMetrics();
                    textoQuebrado = wrapTextToRectangle(frete, metrics, rectWidth);
                }

                int totalTextHeight = textoQuebrado.size() * metrics.getHeight();
                int startY = rectY + (rectHeight - totalTextHeight) / 2 + fm.getAscent();

                // Desenhar cada linha de texto
                int lineY = startY;
                for (String i : textoQuebrado) {

                    // g.setFont(mediumFont);
                    // metrics = g.getFontMetrics();
                    xPosition = rectX + (rectWidth - metrics.stringWidth(i)) / 2;
                    g.drawString(i, xPosition, lineY);
                    lineY += metrics.getHeight();

                }

            } else if (!frete.isEmpty() && frete.length() > 31) {
                int fontSize = 60;
                Font font = customFont.deriveFont(Font.BOLD, fontSize);

                List<String> textoQuebrado = wrapTextToRectangle(frete, metrics, rectWidth);

                while ((textoQuebrado.size() * metrics.getHeight()) > rectHeight && fontSize > 1) {
                    fontSize--;
                    font = customFont.deriveFont(Font.BOLD, fontSize);
                    g.setFont(font);
                    metrics = g.getFontMetrics();
                    textoQuebrado = wrapTextToRectangle(frete, metrics, rectWidth);
                }

                int totalTextHeight = textoQuebrado.size() * metrics.getHeight();
                int startY = rectY + (rectHeight - totalTextHeight) / 2 + fm.getAscent();

                // Desenhar cada linha de texto
                int lineY = startY;
                for (String i : textoQuebrado) {

                    // g.setFont(mediumFont);
                    // metrics = g.getFontMetrics();
                    xPosition = rectX + (rectWidth - metrics.stringWidth(i)) / 2;
                    g.drawString(i, xPosition, lineY);
                    lineY += metrics.getHeight();

                }
            }

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
            g.drawString(preco, priceXPosition, 1531);

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

    @Transactional
    @Scheduled(cron = "0 0 23 * * ?")
    public void deletarProdutosAntigos() throws FileExistsException {
        LocalDateTime dataLimite = LocalDateTime.now().minusDays(7);
        List<Produto> produtosAntigos = produtoRepository.findProdutosComMaisDe7Dias(dataLimite);

        produtosAntigos.stream().forEach(item -> {
            reportRepository.deleteByProdutoReport(item.getId());
            produtoRepository.deleteByProdutoPromos(item.getId());
        });

        produtoRepository.deleteAll(produtosAntigos);

        for (Produto produto : produtosAntigos) {

            apagarPromocoesSemProdutos(produto);

            produto.getLinksProdutos().stream().forEach(item -> {
                linkProdutoRepository.deleteById(item.getId());
            });

            if (produto.getUrlImagem() != null) {
                apagarImagem(produto.getUrlImagem());
            }

            if (produto.getImagemSocial() != null) {
                apagarImagemReal(produto.getImagemSocial());
            }
        }

    }
}
