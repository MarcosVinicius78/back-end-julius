package com.julius.julius.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.julius.julius.models.Promo;

public interface PromoRepository extends JpaRepository<Promo, Long>{
   
    @Modifying
    @Query(value = "DELETE FROM produtos_promo WHERE produto_id = :id AND promo_id = :idEditar", nativeQuery = true)
    void apagarPromoProduto(Long id, Long idEditar);

    @Query(value = "SELECT * FROM promo ORDER BY data_Criacao DESC", nativeQuery = true)
    Page<Promo> lsitarPromos(Pageable pageable);
}
