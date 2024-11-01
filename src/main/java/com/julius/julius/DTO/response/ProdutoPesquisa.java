package com.julius.julius.DTO.response;

import java.util.Date;

public record ProdutoPesquisa(

        Long id,
        String titulo,
        String copy,
        String preco,
        String parcelado,
        String cupom,
        String link,
        String freteVariacoes,
        String mensagemAdicional,
        Boolean promocaoEncerrada,
        Date dataCriacao,
        String imagem,
        String imagemSocial,
        String imagemLoja,
        String nomeLoja,
        String descricao
) {

}
