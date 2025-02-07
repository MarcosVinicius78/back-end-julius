package com.julius.julius.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProdutoSalvarDto(

        @NotBlank(message = "O campo 'Titulo' é obrigatório") 
        String titulo,

        @NotBlank(message = "O campo 'Preço' é obrigatório")
        String preco,

        String precoParcelado,
        String descricao,
        String linkSe,
        String linkOmc,
        String cupomOmc,
        String cupomSe,
        String mensagemAdicional,
        String link,
        String freteVariacoes,
        String urlImagem,

        @NotNull(message = "O Campo 'id_categoria' é obrigatorio")
        Long idCategoria,

        @NotNull(message = "O Campo 'id_loja' é obrigatorio")
        Long idLoja,

        String copy
){}
