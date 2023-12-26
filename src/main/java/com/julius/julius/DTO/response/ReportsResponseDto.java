package com.julius.julius.DTO.response;

import java.util.Date;

import com.julius.julius.models.Report;

public record ReportsResponseDto(
        Long id,
        String tituloProduto,
        Long total,
        Date dataCriacao,
        String tipo
        
) {
    public static ReportsResponseDto toResonse(Report report) {
        return new ReportsResponseDto(
                report.getId(),
                report.getProduto().getTitulo(),
                report.getTotal(),
                report.getDataCriacao(),
                report.getTipo());
    }
}
