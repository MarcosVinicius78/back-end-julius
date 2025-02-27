package com.julius.julius.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.julius.julius.models.Categoria;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long>{

    List<Categoria> findAllByOrderByNomeCategoriaAsc();
}
