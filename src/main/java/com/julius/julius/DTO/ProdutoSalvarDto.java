package com.julius.julius.DTO;

public record ProdutoSalvarDto(
    String titulo,
    String preco,
    String precoParcelado,
    String descricao,
    String link,
    String cupom,
    String mensagemAdicional,
    String freteVariacoes,
    String urlImagem,
    Long  id_categoria,
    Long id_loja
) {
    
}
