package com.julius.julius.DTO.response;

import java.time.LocalDateTime;

import com.julius.julius.models.Produto;

public record ProdutoResponseDto(

        Long id,
        String titulo,
        String preco,
        String descricao,
        String cupom,
        String tituloPequeno,
        String link,
        LocalDateTime dataCriacao,
        byte[] imagem,
        LojaResponseDto loja

) {

    public static ProdutoResponseDto toResonse(Produto produto) {
        return new ProdutoResponseDto(
            produto.getId(),
            produto.getTitulo(),
            produto.getPreco(),
            produto.getDescricao(),
            produto.getCupom(),
            produto.getTituloPequeno(),
            produto.getLink(),
            produto.getDataCriacao(),
            produto.getImagem(),
            LojaResponseDto.toResonse(produto.getLojas().get(0))
        );
    }

}
