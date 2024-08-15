package com.julius.julius.DTO.request.promosDTO;

import java.util.List;

public record PromosSalvarDTO(

    Long id,
    String copyPromo,
    List<Long> idProdutos

) {
    
}
