package com.codingshuttle.project.airnb.repository;

import com.codingshuttle.project.airnb.entites.Guest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GuestRepository extends JpaRepository<Guest,Long> {
}
