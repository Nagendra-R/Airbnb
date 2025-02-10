package com.codingshuttle.project.airnb.repository;

import com.codingshuttle.project.airnb.entites.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface BookingRepository extends JpaRepository<Booking,Long> {

}
