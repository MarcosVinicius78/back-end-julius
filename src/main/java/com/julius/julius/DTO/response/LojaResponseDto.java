package com.julius.julius.DTO.response;

import com.julius.julius.models.Loja;

public record LojaResponseDto(
    Long id,
    String nome_loja,
    String urlImagem
) {

    public static LojaResponseDto toResonse(Loja loja){
        return new LojaResponseDto(
            loja.getId(),
            loja.getNomeLoja(),
            loja.getUrlImagem()
        );
    }

}