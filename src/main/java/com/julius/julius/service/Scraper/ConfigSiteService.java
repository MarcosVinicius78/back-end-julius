package com.julius.julius.service.Scraper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Service
public class ConfigSiteService {

    private Long TEMPO_ROBO = 300000L;

    private Boolean LINK_CURTO = Boolean.FALSE;

    public void mudarTempoRobo(Long tempo) {
        this.TEMPO_ROBO = tempo * 60 * 1000;
    }

    public Long buscarTempoRobo(){
        return TEMPO_ROBO/1000/60;
    }

    public void mudarLinkCurto(Boolean valor) {
        LINK_CURTO = valor;
    }

    public Boolean buscarLinkCurto(){
        return LINK_CURTO;
    }
}
