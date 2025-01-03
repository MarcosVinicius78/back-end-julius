package com.julius.julius.DTO.response;

import java.util.Date;

import org.hibernate.annotations.NotFound;

import com.julius.julius.models.Produto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@NotBlank
@NotNull
@NotEmpty
public record ProdutoDto(

    @NotNull
    @NotBlank
    @NotEmpty
    @NotFound
    Long id,
    String titulo,
    String preco,
    String parcelado,
    String descricao,
    String link_se,
    String link_ofm,
    Date dataCriacao,
    String cupom,
    String mensagemAdicional,
    String freteVariacoes,
    LojaResponseDto lojaResponseDto,
    CategoriaResponseDto categoriaDto,
    String imagem,
    String imagemSocial,
    String copy,
    Boolean promocaoEncerrada,
    String nomeColaborador

) {
     public static ProdutoDto toResonse(Produto produto, LojaResponseDto lojaResponseDto, CategoriaResponseDto categoriaResponseDto, String urlSe, String urlOfm){
        return new ProdutoDto(
            produto.getId(),
            produto.getTitulo(),
            produto.getPreco(),
            produto.getPrecoParcelado(),
            produto.getDescricao(),
            produto.getLink(),
            urlOfm,
            produto.getDataCriacao(),
            produto.getCupom(),
            produto.getMensagemAdicional(),
            produto.getFreteVariacoes(),
            lojaResponseDto,
            categoriaResponseDto,
            produto.getUrlImagem(),
            produto.getImagemSocial(),
            produto.getCopy(),
            produto.getPromocaoEncerrada(),
                produto.getNomeColaborador()
        );
    }
}
