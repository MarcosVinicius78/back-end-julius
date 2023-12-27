package com.julius.julius.DTO.response;

import java.util.List;

import com.julius.julius.models.Banner;
import com.julius.julius.models.Link;
import com.julius.julius.models.Loja;

public record LinksBannersDto(

    List<Link> links,

    List<Banner> banners

) {

     public static LinksBannersDto toResonse(List<Link> link, List<Banner> banners){
        return new LinksBannersDto(
            link,
            banners
        );
    }
    
}
