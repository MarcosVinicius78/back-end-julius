package com.julius.julius.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.julius.julius.models.Loja;

public interface LojaRepository extends JpaRepository<Loja, Long>{
    
    List<Loja> findAllByOrderByNomeLojaAsc();

}
