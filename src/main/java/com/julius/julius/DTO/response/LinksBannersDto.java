package com.julius.julius.DTO.response;

import java.util.List;

import com.julius.julius.models.Banner;
import com.julius.julius.models.Link;

public record LinksBannersDto(

    Link links,

    List<Banner> banners

) {

     public static LinksBannersDto toResonse(Link link, List<Banner> banners){
        return new LinksBannersDto(
            link,
            banners
        );
    }
    
}
