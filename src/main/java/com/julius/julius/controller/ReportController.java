package com.julius.julius.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.julius.julius.DTO.response.ReportsResponseDto;
import com.julius.julius.service.ReportService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping("/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping
    public ResponseEntity<?> reportar(@RequestParam("productId") Long productId, @RequestParam("reportType") String reportType) {
        reportService.saveOrUpdateReport(productId, reportType);
        return ResponseEntity.ok().build();
    }

    @GetMapping("listar-reports")
    public ResponseEntity<Page<ReportsResponseDto>> listarReports(@RequestParam(defaultValue = "0") int page,
    @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);

        return ResponseEntity.ok().body(reportService.listarReports(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> apagarReport(@PathVariable Long id){
        
        reportService.apagarReport(id);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/apagar-varios")
    public ResponseEntity<Integer> apagarVariosProdutos(@RequestBody List<ReportsResponseDto> reportsSelecionados){

        if (reportsSelecionados != null) {
            reportService.apagarVariosReports(reportsSelecionados);
            return ResponseEntity.ok().body(reportsSelecionados.size());
        }

        return ResponseEntity.notFound().build();

    }
    
}
