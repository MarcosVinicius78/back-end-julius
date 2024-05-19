package com.julius.julius.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileExistsException;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
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
import com.julius.julius.DTO.response.ProdutoResponseDto;
import com.julius.julius.models.Categoria;
import com.julius.julius.models.Loja;
import com.julius.julius.models.Produto;
import com.julius.julius.repository.CategoriaRepository;
import com.julius.julius.repository.LojaRepository;
import com.julius.julius.repository.ProdutoRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import java.awt.*;

@Service
@RequiredArgsConstructor
public class ProdutoService {

    private final ProdutoRepository produtoRepository;

    private final LojaRepository lojaRepository;

    private final CategoriaRepository categoriaRepository;

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
        produto.setLink(produtoSalvarDto.link());
        produto.setCopy(produtoSalvarDto.copy());

        System.out.println(produto.toString());
        System.out.println(produto.getUrlImagem());

        loja.get().getProdutos().add(produto);

        return ProdutoResponseDto.toResonse(produtoRepository.save(produto));
    }

    public Page<ProdutoResponseDto> getProdutosPaginados(Pageable pageable) {

        Page<ProdutoResponseDto> produtoTeste = produtoRepository.findFirstByOrderByDataCadastroDesc(pageable)
                .map(ProdutoResponseDto::toResonse);

        if (produtoTeste.isEmpty()) {
            return Page.empty();
        }

        return produtoTeste;
    }

    // return
    // produtoRepository.findAll(pageable).map(ProdutoResponseDto::toResonse);
    public ProdutoDto pegarProduto(Long id) {

        Optional<Produto> produto = produtoRepository.findById(id);

        if (!produto.isPresent()) {
            return null;
        }

        LojaResponseDto lojaResponseDto = LojaResponseDto.toResonse(produto.get().getLoja());
        CategoriaResponseDto categoriaDto = CategoriaResponseDto.toResonse(produto.get().getCategoria());

        return ProdutoDto.toResonse(produto.get(), lojaResponseDto, categoriaDto);
    }

    public Boolean apagarProduto(Long id, String urlImagem) throws FileExistsException {

        String caminhoImagem = UPLOAD_DIR + "/" + urlImagem;
        this.produtoRepository.deleteById(id);

        apagarImagem(urlImagem);

        return true;
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

    public ProdutoResponseDto atualizarProduto(ProdutoAtualizarDto produtoAtualizarDto) {

        Optional<Categoria> categoria = categoriaRepository.findById(produtoAtualizarDto.id_categoria());
        Optional<Loja> loja = lojaRepository.findById(produtoAtualizarDto.id_loja());

        Optional<Produto> produto = produtoRepository.findById(produtoAtualizarDto.id());

        produto.get().setId(produtoAtualizarDto.id());
        produto.get().setTitulo(produtoAtualizarDto.titulo());
        produto.get().setPreco(produtoAtualizarDto.preco());
        produto.get().setPrecoParcelado(produtoAtualizarDto.precoParcelado());
        produto.get().setDescricao(produtoAtualizarDto.descricao());
        produto.get().setLink(produtoAtualizarDto.link());
        produto.get().setCupom(produtoAtualizarDto.cupom());
        produto.get().setFreteVariacoes(produtoAtualizarDto.freteVariacoes());
        produto.get().setMensagemAdicional(produtoAtualizarDto.mensagemAdicional());
        produto.get().setCategoria(categoria.get());
        produto.get().setLoja(loja.get());
        produto.get().setCopy(produtoAtualizarDto.copy());

        return ProdutoResponseDto.toResonse(this.produtoRepository.save(produto.get()));
    }

    public Page<ProdutoResponseDto> obterProdutosPorCategoria(Long categoriaId, Pageable pageable) {
        return produtoRepository.findByCategoriIdOrderByDataCriacaoDesc(categoriaId, pageable)
                .map(ProdutoResponseDto::toResonse);
    }

    @Transactional
    public void apagarVariosProdutos(List<ProdutoDto> produtosSelecionados) {
        List<Long> ids = produtosSelecionados.stream().map(ProdutoDto::id).toList();

        produtoRepository.deleteByIdIn(ids);
    }

    public List<ProdutoResponseDto> pesquisarProdutos(String termoPesquisa) {
        // Implemente a lógica de pesquisa no repositório
        return produtoRepository.findByTituloContainingIgnoreCase(termoPesquisa).stream()
                .map(ProdutoResponseDto::toResonse).toList();
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
        return null;
    }

    public byte[] gerarStory(String preco, String titulo, String urlImagem, String frete, String cupom)
            throws FileExistsException {

        try {
            // Carregar a imagem
            BufferedImage image = ImageIO.read(new File(UPLOAD_DIR + "/story.jpeg"));

            Image foto = ImageIO.read(new File(UPLOAD_DIR + "-real" + "/" + urlImagem));

            int x = (image.getWidth() - foto.getWidth(null)) / 15;

            // Desenhar texto na imagem
            Graphics2D g = image.createGraphics();
            g.setColor(Color.BLACK);
            g.drawImage(foto, 53, 130, 800, 750, null);

            // Configurar fonte para o título
            g.setFont(new Font("SansSerif", Font.BOLD, 45));
            FontMetrics fm = g.getFontMetrics();
            int imageWidth = image.getWidth();
            int titleYPosition = 970;

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

            g.setFont(new Font("SansSerif", Font.BOLD, 40));
            if (!cupom.isEmpty() && cupom.length() <= 6) {
                // g.setFont(new Font("Arial", Font.BOLD, 40));
                g.drawString("Cupom:" + cupom, 455, 1122);
            } else if (!cupom.isEmpty() && cupom.length() <= 16) {
                g.setFont(new Font("SansSerif", Font.BOLD, 35));
                g.drawString("Cupom: " + cupom, 355, 1122);
            } else if (!cupom.isEmpty() && cupom.length() >= 17) {
                g.setFont(new Font("SansSerif", Font.BOLD, 33));
                g.drawString("Cupom: " + cupom, 353, 1122);
            } else if (!frete.isEmpty() && frete.length() == 18) {
                // frete grátis prime
                g.drawString(frete, 430, 1122);
            } else if (!frete.isEmpty() && frete.length() == 12) {
                // frete grátis
                g.drawString(frete, 490, 1122);
            } else if (!frete.isEmpty() && frete.length() == 15) {
                g.drawString(frete, 450, 1122);
                // frete econômico
            } else if (!frete.isEmpty() && frete.length() == 30) {
                g.setFont(new Font("SansSerif", Font.BOLD, 35));
                // frete grátis algumas regioes
                g.drawString(frete, 350, 1122);
            }

            // g.setFont(new Font("Arial", Font.BOLD, 40));
            // g.drawString(titulo1, 90, 970);

            // g.setFont(new Font("Arial", Font.BOLD, 40));
            // g.drawString(titulo2 + "...", 80, 1040);

            g.setFont(new Font("SansSerif", Font.BOLD, 90));
            FontMetrics priceFm = g.getFontMetrics();
            int priceXPosition = (imageWidth - priceFm.stringWidth(preco)) / 2;
            g.drawString(preco, priceXPosition, 1277);

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

    @Scheduled(cron = "0 0 0 * * ?") // Executa diariamente à meia-noite
    public void deletarProdutosAntigos() throws FileExistsException {
        LocalDateTime dataLimite = LocalDateTime.now().minusDays(7);
        List<Produto> produtosAntigos = produtoRepository.findProdutosComMaisDe7Dias(dataLimite);

        for (Produto produto : produtosAntigos) {
            apagarImagem(produto.getUrlImagem());
            apagarImagemReal(produto.getImagemSocial());
        }

        produtoRepository.deleteAll(produtosAntigos);
    }
}
