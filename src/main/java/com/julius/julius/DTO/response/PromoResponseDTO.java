package com.julius.julius.DTO.response;

import java.util.List;

import com.julius.julius.models.Promo;

public record PromoResponseDTO(

    Long idPromo,
    String copyPromo,
    String urlImagem,
    List<ProdutoPromoDTO> produtoResponseDto
) {
    
    public static PromoResponseDTO toResponse(Promo promo){
        
        return new PromoResponseDTO(
            promo.getId(), 
            promo.getCopyPromo(), 
            promo.getUrlImagem(),
            promo.getProdutos().stream().map(ProdutoPromoDTO::toResponse).toList());
    }
}
