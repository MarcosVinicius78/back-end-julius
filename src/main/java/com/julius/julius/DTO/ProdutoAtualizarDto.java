package com.julius.julius.DTO;

public record ProdutoAtualizarDto(
    
    Long id,
    String titulo,
    String preco,
    String precoParcelado,
    String descricao,
    String linkSe,
    String linkSeApp,
    String linkOmc,
    String linkOmcApp,
    String cupomSe,
    String copy,
    String freteVariacoes,
    String mensagemAdicional,
    Long  idCategoria,
    Long idLoja

) {
    
}
