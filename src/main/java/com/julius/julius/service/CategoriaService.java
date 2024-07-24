package com.julius.julius.service;

import java.util.List;
import java.util.Optional;

import org.apache.commons.io.FileExistsException;
import org.springframework.stereotype.Service;

import com.julius.julius.DTO.CategoriaSalvar;
import com.julius.julius.DTO.response.CategoriaResponseDto;
import com.julius.julius.models.Categoria;
import com.julius.julius.models.Produto;
import com.julius.julius.repository.CategoriaRepository;
import com.julius.julius.repository.ProdutoRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    public final CategoriaRepository categoriaRepository;

    public final ProdutoService produtoService;

    public final ProdutoRepository produtoRepository;

    public CategoriaResponseDto salvarCategoria(String categoriaSalvar) {

        Categoria categoria = Categoria.builder()
                .nomeCategoria(categoriaSalvar)
                .build();
        return CategoriaResponseDto.toResonse(categoriaRepository.save(categoria));
    }

    public List<CategoriaResponseDto> listarCategoria() {
        return this.categoriaRepository.findAllByOrderByNomeCategoriaAsc().stream().map(CategoriaResponseDto::toResonse)
                .toList();
    }

    @Transactional
    public void apagarCategoria(Long id) {
        
        List<Produto> produtos = produtoRepository.findByProdutoPorCategoriaId(id);

        for (Produto i : produtos) {
            try {
                produtoService.apagarProduto(i.getId(), i.getUrlImagem(), i.getImagemSocial());
            } catch (FileExistsException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        categoriaRepository.deleteById(id);
    }

    public CategoriaResponseDto pegarCategoria(Long id) {

        Optional<Categoria> categoria = categoriaRepository.findById(id);

        return CategoriaResponseDto.toResonse(categoria.get());
    }

    public void atualizarCategoria(CategoriaResponseDto categoriaResponseDto) {

        Categoria categoria = categoriaRepository.findById(categoriaResponseDto.categoria_id()).orElse(null);

        if (categoria != null) {
            categoria.setNomeCategoria(categoriaResponseDto.nomeCategoria());
            this.categoriaRepository.save(categoria);
        }

    }
}
