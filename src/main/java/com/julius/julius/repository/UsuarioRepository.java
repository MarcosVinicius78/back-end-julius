package com.julius.julius.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.julius.julius.models.auth.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, Long>{
    
}
