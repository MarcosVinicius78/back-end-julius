package com.julius.julius.service;

import java.util.Base64;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

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

    public ProdutoResponseDto salvarProduto(ProdutoSalvarDto produtoSalvarDto) {

        Optional<Categoria> categoria = categoriaRepository.findById(produtoSalvarDto.id_categoria());
        Optional<Loja> loja = lojaRepository.findById(produtoSalvarDto.id_loja());

        Produto produto = new Produto();

        produto.setTitulo(produtoSalvarDto.titulo());
        produto.setPreco(produtoSalvarDto.preco());
        produto.setDescricao(produtoSalvarDto.descricao());
        produto.setCupom(produtoSalvarDto.cupom());
        produto.setTituloPequeno(produtoSalvarDto.tituloPequeno());
        produto.setImagem(converterBase64(produtoSalvarDto.imagemUrl()));
        produto.setCategoria(categoria.get());
        produto.getLojas().add(loja.get());
        produto.setLink(produtoSalvarDto.link());

        loja.get().getProdutos().add(produto);

        return ProdutoResponseDto.toResonse(produtoRepository.save(produto));
    }

    private byte[] converterBase64(String imagemBase64) {
        byte[] imageByte = Base64.getDecoder().decode(imagemBase64);
        return imageByte;
    }

    public Page<ProdutoResponseDto> getProdutosPaginados(Pageable pageable) {

        return produtoRepository.findFirstByOrderByDataCadastroDesc(pageable).map(ProdutoResponseDto::toResonse);

        // return produtoRepository.findAll(pageable).map(ProdutoResponseDto::toResonse);
    }

    public ProdutoDto pegarProduto(Long id) {

        Optional<Produto> produto = produtoRepository.findById(id);

        if (!produto.isPresent()) {
            return null;
        }

        LojaResponseDto lojaResponseDto = LojaResponseDto.toResonse(produto.get().getLojas().get(0));
        CategoriaResponseDto categoriaDto = CategoriaResponseDto.toResonse(produto.get().getCategoria());

        return ProdutoDto.toResonse(produto.get(), lojaResponseDto, categoriaDto);
    }

    public void apagarProduto(Long id) {
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
        produto.setTituloPequeno(produtoAtualizarDto.tituloPequeno());
        produto.setImagem(converterBase64(produtoAtualizarDto.imagemUrl()));
        produto.setCategoria(categoria.get());
        produto.getLojas().add(loja.get());

        return ProdutoResponseDto.toResonse(this.produtoRepository.save(produto));
    }

    public Page<ProdutoResponseDto> obterProdutosPorCategoria(Long categoriaId, Pageable pageable) {
        return produtoRepository.findByCategoriIdOrderByDataCriacaoDesc(categoriaId, pageable).map(ProdutoResponseDto::toResonse);
    }

    @Transactional
    public void apagarVariosProdutos(List<Long> produtosSelecionados) {
        produtoRepository.deleteByIdIn(produtosSelecionados);
    }

    public List<ProdutoResponseDto> pesquisarProdutos(String termoPesquisa) {
        // Implemente a lógica de pesquisa no repositório
        return produtoRepository.findByTituloContainingIgnoreCase(termoPesquisa).stream().map(ProdutoResponseDto::toResonse).toList();
    }
}
