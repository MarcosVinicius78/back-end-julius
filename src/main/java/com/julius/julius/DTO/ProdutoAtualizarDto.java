package com.julius.julius.DTO;

public record ProdutoAtualizarDto(
    
    Long id,
    String titulo,
    String preco,
    String precoParcelado,
    String descricao,
    String link,
    String cupom,
    String copy,
    String freteVariacoes,
    String mensagemAdicional,
    Long  id_categoria,
    Long id_loja

) {
    
}
