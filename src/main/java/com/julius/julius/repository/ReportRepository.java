package com.julius.julius.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.julius.julius.models.Produto;
import com.julius.julius.models.Report;

public interface ReportRepository extends JpaRepository<Report, Long> {

    @Query("SELECT r FROM Report r WHERE r.produto.id = :productId AND r.tipo = :reportType")
    Report findByProdutoIdAndTipo(@Param("productId") Long productId, @Param("reportType") String reportType);

    @Modifying
    @Query("UPDATE Report r SET r.total = r.total + 1 WHERE r.produto.id = :productId AND r.tipo = :reportType")
    void updateTotal(@Param("productId") Long productId, @Param("reportType") String reportType);

    @Query("SELECT r FROM Report r ORDER BY r.dataCriacao DESC")
    Page<Produto> findFirstByOrderByDataCadastroDesc(Pageable pageable);

    Boolean deleteByIdIn(List<Long> reports);
}