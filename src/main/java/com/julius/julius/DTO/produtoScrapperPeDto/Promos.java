package com.julius.julius.DTO.produtoScrapperPeDto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.julius.julius.DTO.ProdutoJsonDto;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Promos(
        @JsonProperty("results")
        List<ProdutoJsonDto> results
) {
}
