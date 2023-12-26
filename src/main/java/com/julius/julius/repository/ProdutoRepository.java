package com.julius.julius.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.julius.julius.models.Produto;

@Repository
public interface ProdutoRepository extends JpaRepository<Produto, Long>{

    void deleteByIdIn(List<Long> prod);

    @Query("SELECT p FROM Produto p ORDER BY p.dataCriacao DESC")
    Page<Produto> findFirstByOrderByDataCadastroDesc(Pageable pageable);

    @Query("SELECT p FROM Produto p WHERE p.categoria.id = :categoriaId ORDER BY p.dataCriacao DESC")
    Page<Produto> findByCategoriIdOrderByDataCriacaoDesc(Long categoriaId, Pageable pageable);

    List<Produto> findByTituloContainingIgnoreCase(String termoPesquisa);   
}
