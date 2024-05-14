package com.julius.julius.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.julius.julius.models.Post;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
}