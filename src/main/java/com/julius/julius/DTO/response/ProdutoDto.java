package com.julius.julius.DTO.response;

import com.julius.julius.models.Produto;

public record ProdutoDto(

    Long id,
    String titulo,
    String preco,
    String parcelado,
    String descricao,
    String link,
    String cupom,
    String mensagemAdicional,
    String freteVariacoes,
    LojaResponseDto lojaResponseDto,
    CategoriaResponseDto categoriaDto,
    String imagem

) {
     public static ProdutoDto toResonse(Produto produto, LojaResponseDto lojaResponseDto, CategoriaResponseDto categoriaResponseDto){
        return new ProdutoDto(
            produto.getId(),
            produto.getTitulo(),
            produto.getPreco(),
            produto.getPrecoParcelado(),
            produto.getDescricao(),
            produto.getLink(),
            produto.getCupom(),
            produto.getMensagemAdicional(),
            produto.getFreteVariacoes(),
            lojaResponseDto,
            categoriaResponseDto,
            produto.getUrlImagem()
        );
    }
}
