package com.julius.julius.DTO.response;

public record ProdutoScraperDTO(

    String nomeProduto,
    String precoProduto,
    String urlImagem,
    String urlProdutoSe,
    String precoParcelado

) {
    static ProdutoScraperDTO toResponse(String nome, String preco,String urlImagem, String urlProduto, String precoParcelado){
        return new ProdutoScraperDTO(nome, preco,urlImagem, urlProduto, precoParcelado);
    }
}
