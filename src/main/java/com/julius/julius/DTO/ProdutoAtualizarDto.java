package com.julius.julius.DTO;

public record ProdutoAtualizarDto(
    
    Long id,
    String titulo,
    String preco,
    String descricao,
    String link,
    String cupom,
    String tituloPequeno,
    String imagemUrl,
    Long  id_categoria,
    Long id_loja

) {
    
}
