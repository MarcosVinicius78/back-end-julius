package com.julius.julius.DTO.response;

import com.julius.julius.models.Categoria;

public record CategoriaResponseDto(
    Long categoria_id,
    String nome_categoria
) {

     public static CategoriaResponseDto toResonse(Categoria categoria){
        return new CategoriaResponseDto(
            categoria.getCategoria_id(),
            categoria.getNome_categoria()
        );
    }
    
}
