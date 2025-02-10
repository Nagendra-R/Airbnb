package com.codingshuttle.project.airnb.repository;

import com.codingshuttle.project.airnb.entites.Hotel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


public interface HotelRepository extends JpaRepository<Hotel,Long> {

}
