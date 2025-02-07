package com.julius.julius.DTO.produtoScrapperPeDto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ResponseScrapper(
        Props props
) {
}
