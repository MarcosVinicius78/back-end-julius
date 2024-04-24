package com.julius.julius.DTO.response;

import java.util.Date;

import com.julius.julius.models.Produto;

public record ProdutoResponseDto(

        Long id,
        String titulo,
        String preco,
        String descricao,
        String cupom,
        String link,
        String freteVariacoes,
        Date dataCriacao,
        String imagem,
        LojaResponseDto loja

) {

    public static ProdutoResponseDto toResonse(Produto produto) {
        return new ProdutoResponseDto(
            produto.getId(),
            produto.getTitulo(),
            produto.getPreco(),
            produto.getDescricao(),
            produto.getCupom(),
            produto.getLink(),
            produto.getFreteVariacoes(),
            produto.getDataCriacao(),
            produto.getUrlImagem(),
            LojaResponseDto.toResonse(produto.getLojas().get(0))
        );
    }

}
