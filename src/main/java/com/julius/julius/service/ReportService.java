package com.julius.julius.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.julius.julius.DTO.response.ReportsResponseDto;
import com.julius.julius.models.Report;
import com.julius.julius.repository.ProdutoRepository;
import com.julius.julius.repository.ReportRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportRepository reportRepository;

    private final ProdutoRepository produtoRepository;

    private final ProdutoService produtoService;

    @Transactional
    public void saveOrUpdateReport(Long productId, String reportType) {
        Report existingReport = reportRepository.findByProdutoIdAndTipo(productId, reportType);


        if (existingReport != null) {
            if (existingReport.getTotal() > 3){
                produtoService.encerrarPromocao(true, existingReport.getProduto().getId());
            }else{
                // Se já existe, atualize o total
                reportRepository.updateTotal(productId, reportType);
            }
        } else {
            // Se não existe, faça um insert
            Report newReport = new Report();
            newReport.setProduto(produtoRepository.findById(productId).orElseThrow(() -> new RuntimeException("Produto não encontrado")));
            newReport.setTipo(reportType);
            newReport.setTotal(1L);
            reportRepository.save(newReport);
        }
    }

    public Page<ReportsResponseDto> listarReports(Pageable pageable) {
        return reportRepository.findAllByOrderByIdDesc(pageable).map(ReportsResponseDto::toResonse);
        //        return reportRepository.findAll(pageable).map(ReportsResponseDto::toResonse);
    }

    public void apagarReport(Long id) {
        reportRepository.deleteById(id);
    }

    @Transactional
    public Boolean apagarVariosReports(List<ReportsResponseDto> reportsSelecionados) {

        List<Long> ids = reportsSelecionados.stream().map(ReportsResponseDto::id).toList();

        
        if (reportRepository.deleteByIdIn(ids)) {
            return true;
        }

        return false;
    }
}