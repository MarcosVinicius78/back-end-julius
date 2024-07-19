package com.julius.julius.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.julius.julius.models.Link;

public interface LinksRepository extends JpaRepository<Link, Long> {

    @Query(value = "SELECT * FROM links WHERE site_id = :siteId", nativeQuery = true)
    Link pegarLinkeBannersSiteId(@Param("siteId") Long siteId);
    
}
