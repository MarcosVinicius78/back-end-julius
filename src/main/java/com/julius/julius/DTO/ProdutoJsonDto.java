package com.julius.julius.DTO;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProdutoJsonDto {
    private List<String> coupons;
    private String title;
    private String slug;
    private String price;
    private String image;
    @JsonProperty("short_url")
    private String shortUrl;
}
