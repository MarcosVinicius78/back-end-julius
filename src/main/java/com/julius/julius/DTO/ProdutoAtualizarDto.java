package com.julius.julius.DTO;

public record ProdutoAtualizarDto(
    
    Long id,
    String titulo,
    String preco,
    String precoParcelado,
    String descricao,
    String link_se,
    String link_ofm,
    String cupom,
    String copy,
    String freteVariacoes,
    String mensagemAdicional,
    Long  id_categoria,
    Long id_loja

) {
    
}
