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

    private static final String UPLOAD_DIR = "uploads/produtos";

    public String salvarImagemProduto(MultipartFile file, Long id) throws FileUploadException {

        Optional<Produto> produto = produtoRepository.findById(id);

        try {
            File uploadsDir = new File(UPLOAD_DIR);
            if (!uploadsDir.exists()) {
                uploadsDir.mkdirs();
            }

            Date data = new Date();

            String fileName = file.getOriginalFilename();
            String nomeImagem = data.getTime() + fileName;
            Path filePath = Path.of(uploadsDir.getAbsolutePath(), nomeImagem);

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String imagemUrl = uploadsDir.getAbsolutePath() + fileName;

            produto.get().setUrlImagem(nomeImagem);

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
        }else{
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
        produto.getLojas().add(loja.get());
        produto.setLink(produtoSalvarDto.link());

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

        LojaResponseDto lojaResponseDto = LojaResponseDto.toResonse(produto.get().getLojas().get(0));
        CategoriaResponseDto categoriaDto = CategoriaResponseDto.toResonse(produto.get().getCategoria());

        return ProdutoDto.toResonse(produto.get(), lojaResponseDto, categoriaDto);
    }

    public Boolean apagarProduto(Long id, String urlImagem) throws FileExistsException {

        String caminhoImagem = UPLOAD_DIR + "/" + urlImagem;
        this.produtoRepository.deleteById(id);
        
        if (!urlImagem.isEmpty()) {
            System.out.println(urlImagem);
            File arquivoImagem = new File(caminhoImagem);
            if (arquivoImagem.exists()) {
                arquivoImagem.delete();
            } else {
                throw new FileExistsException("Imagem não existe");
            }
        }

        return true;
    }

    public ProdutoResponseDto atualizarProduto(ProdutoAtualizarDto produtoAtualizarDto) {

        Optional<Categoria> categoria = categoriaRepository.findById(produtoAtualizarDto.id_categoria());
        Optional<Loja> loja = lojaRepository.findById(produtoAtualizarDto.id_loja());

        Produto produto = new Produto();
        produto.setId(produtoAtualizarDto.id());
        produto.setTitulo(produtoAtualizarDto.titulo());
        produto.setPreco(produtoAtualizarDto.preco());
        produto.setDescricao(produtoAtualizarDto.descricao());
        produto.setLink(produtoAtualizarDto.link());
        produto.setCupom(produtoAtualizarDto.cupom());
        // produto.setTituloPequeno(produtoAtualizarDto.tituloPequeno());
        produto.setUrlImagem(produtoAtualizarDto.imagemUrl());
        produto.setCategoria(categoria.get());
        produto.getLojas().add(loja.get());

        return ProdutoResponseDto.toResonse(this.produtoRepository.save(produto));
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

    public byte[] gerarStory(String preco, String titulo, String urlImagem, String frete, String cupom) {

        try {
            // Carregar a imagem
            BufferedImage image = ImageIO.read(new File(UPLOAD_DIR + "/WhatsApp Image 2024-05-07 at 09.16.20.jpeg"));

            Image foto = ImageIO.read(new File(UPLOAD_DIR + "/" + urlImagem));

            int x = (image.getWidth() - foto.getWidth(null)) / 15;

            // Desenhar texto na imagem
            Graphics2D g = image.createGraphics();
            g.setColor(Color.BLACK);
            g.drawImage(foto, 53, 130, 800, 750, null);

            g.setFont(new Font("Arial", Font.BOLD, 90));
            g.drawString(preco, 280, 1280);

            String titulo1 = "";
            String titulo2 = titulo;
            int cortar = 0;
            System.out.println(titulo);
            for (int i = 0; i < titulo.length() ; i++) {
                if (titulo.charAt(i) == ' ') {
                    cortar = i;
                }
                
                if (i == 23) {
                    titulo1 = titulo.substring(0, cortar);
                    System.out.println(cortar);
                    if (titulo.length() >= 47) {
                        titulo2 = titulo.substring(cortar, 40);
                        System.out.println(titulo2);
                        break;
                    }else{
                        titulo2 = titulo.substring(cortar, titulo.length());
                        break;
                    }
                }
            }

            if (!cupom.isEmpty()) {
                g.setFont(new Font("Arial", Font.BOLD, 40));
                g.drawString("Cupom:"+cupom, 395, 1122);
            }else if (!frete.isEmpty()) {
                g.setFont(new Font("Arial", Font.BOLD, 40));
                g.drawString(frete, 500, 1122);
            }

            
            g.setFont(new Font("Arial", Font.BOLD, 60));
            g.drawString(titulo1, 90, 970);
            
            g.setFont(new Font("Arial", Font.BOLD, 60));
            g.drawString(titulo2+"...", 80, 1040);
            g.dispose();

            // Converter a imagem para um array de bytes
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);
            byte[] bytes = baos.toByteArray();

            // Retornar a imagem gerada
            return bytes;
        } catch (IOException e) {
            e.printStackTrace();
            throw new InternalError();
        }
    }
    

    // public byte[] loadImagemAResource(String imagemNome) throws
    // FileNotFoundException {

    // try{
    // File uploadDir = new File(UPLOAD_DIR);

    // Path imagemPath = Paths.get(uploadDir.getAbsolutePath()).resolve(imagemNome);

    // System.out.println(imagemPath.toString());

    // byte[] images;

    // images = Files.readAllBytes(new File(imagemPath.toString()).toPath());
    // return images;
    // } catch (IOException e) {
    // e.printStackTrace();
    // }
    // return null;
    // }
}
