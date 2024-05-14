package com.julius.julius.DTO.response.postDTO;

import java.util.Date;

import com.julius.julius.models.Post;

public record PostResponseDTO(
    Long id,
    String titulo,
    String conteudo,
    String urlImagem,
    Date dataAtualizacao,
    Date dataCriacao
) {
    public static PostResponseDTO toResponse(Post post){
        return new PostResponseDTO(
            post.getId(), 
            post.getTitulo(),
            post.getConteudo(), 
            post.getUrlImagem(),
            post.getDataAtualizacao(),
            post.getDataCriacao()
            );
    }
}
