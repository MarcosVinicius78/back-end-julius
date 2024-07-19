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

        String link_se,
        String link_ofm,
        
        String cupom,
        String mensagemAdicional,
        
        String freteVariacoes,
        
        // @NotBlank(message = "O campo 'imagem' é obrigatório")
        String urlImagem,

        @NotNull(message = "O Campo 'id_categoria' é obrigatorio")
        Long id_categoria,

        @NotNull(message = "O Campo 'id_loja' é obrigatorio")
        Long id_loja,

        String copy
){}
