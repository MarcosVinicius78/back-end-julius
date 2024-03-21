package com.julius.julius.DTO.response;

public record ProdutoScraperDTO(

    String nomeProduto,
    String precoProduto,
    String urlImagem

) {
    static ProdutoScraperDTO toResponse(String nome, String preco,String urlImagem){
        return new ProdutoScraperDTO(nome, preco,urlImagem);
    }
}
