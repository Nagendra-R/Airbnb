package com.codingshuttle.project.airnb.repository;

import com.codingshuttle.project.airnb.entites.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}