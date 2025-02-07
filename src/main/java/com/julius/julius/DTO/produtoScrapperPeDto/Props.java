package com.julius.julius.DTO.produtoScrapperPeDto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Props(
        @JsonProperty("pageProps")
        PageProps pageProps
) {
}
