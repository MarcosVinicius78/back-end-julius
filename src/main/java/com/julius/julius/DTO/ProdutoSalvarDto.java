package com.julius.julius.DTO;

public record ProdutoSalvarDto(
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
