package com.julius.julius.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.julius.julius.DTO.CategoriaSalvar;
import com.julius.julius.DTO.response.CategoriaResponseDto;
import com.julius.julius.models.Categoria;
import com.julius.julius.repository.CategoriaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoriaService {
    
    public final CategoriaRepository categoriaRepository;

    public CategoriaResponseDto salvarCategoria(String categoriaSalvar){

        Categoria categoria = Categoria.builder()
                                .nome_categoria(categoriaSalvar)
                                .build();
        return CategoriaResponseDto.toResonse(categoriaRepository.save(categoria));
    }

    public List<CategoriaResponseDto> listarCategoria(){
        return this.categoriaRepository.findAll().stream().map(CategoriaResponseDto::toResonse).toList();
    }

    public void apagarCategoria(Long id){
        categoriaRepository.deleteById(id);
    }

    public CategoriaResponseDto pegarCategoria(Long id){

        Optional<Categoria> categoria = categoriaRepository.findById(id);

        return CategoriaResponseDto.toResonse(categoria.get());
    }

    public void atualizarCategoria(CategoriaResponseDto categoriaResponseDto) {

        Categoria categoria = Categoria.builder()
                                .categoria_id(categoriaResponseDto.categoria_id())
                                .nome_categoria(categoriaResponseDto.nome_categoria())
                                .build();
        this.categoriaRepository.save(categoria);
    }
}
