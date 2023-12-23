package com.julius.julius.DTO.response;

import com.julius.julius.models.Produto;

public record ProdutoDto(

    Long id,
    String titulo,
    String preco,
    String descricao,
    String link,
    String cupom,
    String tituloPequeno,
    LojaResponseDto lojaResponseDto,
    CategoriaResponseDto categoriaDto,
    byte[] imagem

) {
     public static ProdutoDto toResonse(Produto produto, LojaResponseDto lojaResponseDto, CategoriaResponseDto categoriaResponseDto){
        return new ProdutoDto(
            produto.getId(),
            produto.getTitulo(),
            produto.getPreco(),
            produto.getDescricao(),
            produto.getLink(),
            produto.getCupom(),
            produto.getTituloPequeno(),
            lojaResponseDto,
            categoriaResponseDto,
            produto.getImagem()
        );
    }
}
