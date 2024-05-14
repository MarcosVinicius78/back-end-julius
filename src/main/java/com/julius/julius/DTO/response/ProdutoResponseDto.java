package com.julius.julius.DTO.response;

import java.util.Date;
import java.util.List;

import com.julius.julius.models.Loja;
import com.julius.julius.models.Produto;

public record ProdutoResponseDto(

        Long id,
        String titulo,
        String preco,
        String descricao,
        String cupom,
        String link,
        String freteVariacoes,
        Date dataCriacao,
        String imagem,
        LojaResponseDto loja

) {

    public static ProdutoResponseDto toResonse(Produto produto) {
        
         LojaResponseDto lojaDto = null;
        
        List<Loja> lojas = produto.getLojas();
        if (lojas != null && !lojas.isEmpty()) {
            // Se houver lojas associadas ao produto, cria o LojaResponseDto
            lojaDto = LojaResponseDto.toResonse(lojas.get(0));
        }

        return new ProdutoResponseDto(
            produto.getId(),
            produto.getTitulo(),
            produto.getPreco(),
            produto.getDescricao(),
            produto.getCupom(),
            produto.getLink(),
            produto.getFreteVariacoes(),
            produto.getDataCriacao(),
            produto.getUrlImagem(),
            lojaDto
        );
    }

}
