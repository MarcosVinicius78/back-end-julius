package com.julius.julius.service.Scraper;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Service
@Getter
@Setter
@RequiredArgsConstructor
public class RoboConfigService {

    private Long tempoRobo = 300000L;

    public void mudarTempoRobo(Long tempo) {
        this.tempoRobo = tempo * 60 * 1000;
    }

    public Long buscarTempoRobo(){
        return tempoRobo/1000/60;
    }
}
