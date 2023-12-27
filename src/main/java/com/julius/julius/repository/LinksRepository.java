package com.julius.julius.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.julius.julius.models.Link;

public interface LinksRepository extends JpaRepository<Link, Long> {
    
}
