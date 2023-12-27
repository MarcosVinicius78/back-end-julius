package com.julius.julius.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.julius.julius.models.Banner;

public interface BannerRepository extends JpaRepository<Banner, Long>{
    
}
