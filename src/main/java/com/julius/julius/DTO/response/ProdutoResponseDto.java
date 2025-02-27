package com.julius.julius.DTO.response;

import java.util.Date;

import com.julius.julius.models.Loja;
import com.julius.julius.models.Produto;

public record ProdutoResponseDto(

        Long id,
        String titulo,
        String preco,
        String parcelado,
        String descricao,
        String cupom,
        String link,
        String freteVariacoes,
        Date dataCriacao,
        String imagem,
        LojaResponseDto loja,
        String imagemSocial,
        String copy,
        String mensagemAdicional,
        Boolean promocaoEncerrada
) {

    public static ProdutoResponseDto toResonse(Produto produto) {
        
         LojaResponseDto lojaDto = null;     
        
        Loja loja = produto.getLoja();
        if (loja != null) {
            // Se houver lojas associadas ao produto, cria o LojaResponseDto
            lojaDto = LojaResponseDto.toResonse(loja);
        }

        return new ProdutoResponseDto(
            produto.getId(),
            produto.getTitulo(),
            produto.getPreco(),
            produto.getPrecoParcelado(),
            produto.getDescricao(),
            produto.getCupom(),
            produto.getLink(),
            produto.getFreteVariacoes(),
            produto.getDataCriacao(),
            produto.getUrlImagem(),
            lojaDto,
            produto.getImagemSocial(),
            produto.getCopy(),
            produto.getMensagemAdicional(),
            produto.getPromocaoEncerrada()
            // produto.getLinksProdutos()
        );
    }

}
