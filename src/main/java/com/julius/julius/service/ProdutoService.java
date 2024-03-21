package com.julius.julius.service;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    public String salvarImagem(String url) throws IOException {

        URL file = new URL(url);

        File uploadsDir = new File(UPLOAD_DIR);
        if (uploadsDir.exists()) {
            uploadsDir.mkdirs();
        }

        Date data = new Date();

        String fileName = url.toString().substring(url.lastIndexOf("/") + 1);
        String nomeImagem = data.getTime() + fileName;
        Path filePath = Path.of(uploadsDir.getAbsolutePath(), nomeImagem);

        Files.copy(file.openStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return nomeImagem;
    }

    public ProdutoResponseDto salvarProduto(ProdutoSalvarDto produtoSalvarDto) throws IOException {

        Optional<Categoria> categoria = categoriaRepository.findById(produtoSalvarDto.id_categoria());
        Optional<Loja> loja = lojaRepository.findById(produtoSalvarDto.id_loja());

        Produto produto = new Produto();

        if (!produtoSalvarDto.urlImagem().equals("")) {
            produto.setUrlImagem(salvarImagem(produtoSalvarDto.urlImagem()));
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

    public void apagarProduto(Long id,String urlImagem) throws FileNotFoundException {

        String caminhoImagem = UPLOAD_DIR +"/"+ urlImagem;

        File arquivoImagem = new File(caminhoImagem);
        if (arquivoImagem.exists()) {
            arquivoImagem.delete();
        }else{
            throw new FileNotFoundException("arquivo não encontrado");
        }
        
        this.produtoRepository.deleteById(id);
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
        // produto.setImagem(converterBase64(produtoAtualizarDto.imagemUrl()));
        produto.setCategoria(categoria.get());
        produto.getLojas().add(loja.get());

        return ProdutoResponseDto.toResonse(this.produtoRepository.save(produto));
    }

    public Page<ProdutoResponseDto> obterProdutosPorCategoria(Long categoriaId, Pageable pageable) {
        return produtoRepository.findByCategoriIdOrderByDataCriacaoDesc(categoriaId, pageable)
                .map(ProdutoResponseDto::toResonse);
    }

    @Transactional
    public void apagarVariosProdutos(List<Long> produtosSelecionados) {
        produtoRepository.deleteByIdIn(produtosSelecionados);
    }

    public List<ProdutoResponseDto> pesquisarProdutos(String termoPesquisa) {
        // Implemente a lógica de pesquisa no repositório
        return produtoRepository.findByTituloContainingIgnoreCase(termoPesquisa).stream()
                .map(ProdutoResponseDto::toResonse).toList();
    }

    public Resource loadImagemAResource(String imagemNome) throws FileNotFoundException {

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
}
