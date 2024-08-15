package com.julius.julius.DTO.response;

import com.julius.julius.models.Produto;

public record ProdutoPromoDTO(

        Long id,
        String titulo,
        String preco,
        String urlImagem,
        String link,
        String freteVariacoes,
        String cupom,
        String nomeLoja,
        String urlLoja

) {
    public static ProdutoPromoDTO toResponse(Produto produto) {
        return new ProdutoPromoDTO(
                produto.getId(),
                produto.getTitulo(),
                produto.getPreco(),
                produto.getUrlImagem(),
                produto.getLink(),
                produto.getFreteVariacoes(),
                produto.getCupom(),
                produto.getLoja().getNomeLoja(),
                produto.getLoja().getUrlImagem()
                );
    }
}
