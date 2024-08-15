package com.julius.julius.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.julius.julius.models.Promo;

public interface PromoRepository extends JpaRepository<Promo, Long>{
   
    @Modifying
    @Query(value = "DELETE FROM produtos_promo WHERE produto_id = :id AND promo_id = :idEditar", nativeQuery = true)
    void apagarPromoProduto(Long id, Long idEditar);
}
