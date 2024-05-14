package com.julius.julius.DTO.request.postDTO;

public record PostRequestDTO(
    Long id,
    String titulo,
    String conteudo,
    String urlImagem
) {
    
}
