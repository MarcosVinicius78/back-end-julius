package com.julius.julius.DTO.response;

import java.util.Date;

public interface IProdutoResponseDto {
        Long getId();
        String getTitulo();
        String getCopy();
        String getPreco();
        String getParcelado();
        String getCupom();
        String getLink();
        String getFreteVariacoes();
        String getMensagemAdicional();
        Boolean getPromocaoEncerrada();
        Date getDataCriacao();
        String getImagem();
        String getImagemSocial();
        String getImagemLoja();
        String getNomeLoja();
        String getDescricao();
}
