package com.codingshuttle.project.airnb.repository;

import com.codingshuttle.project.airnb.entites.Hotel;
import com.codingshuttle.project.airnb.entites.Inventory;
import com.codingshuttle.project.airnb.entites.Room;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {


//    @Modifying
//    @Transactional
//    @Query("DELETE FROM Inventory i WHERE i.room = :room AND i.date >= CURRENT_DATE")
//    void deleteFutureInventories(@Param("room") Room room);

    void deleteByRoom(Room room);


    @Query("""
                SELECT DISTINCT i.hotel FROM Inventory i
                WHERE i.city = :city
                AND i.date BETWEEN :startDate AND :endDate
                AND (i.totalCount - i.bookedCount) >= :roomsCount
                AND i.closed = false
                GROUP BY i.hotel
                HAVING COUNT(i.date) = :dateCount
            """)
    Page<Hotel> findHotelsWithAvailableInventories(
            @Param("city") String city,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Integer roomsCount,
            @Param("dateCount") Long dateCount,
            Pageable pageable
    );


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
               Select i from Inventory i
               WHERE i.room.id = :roomId
                     AND i.closed = false
                     AND i.date BETWEEN :startDate AND :endDate
                     AND (i.totalCount - i.bookedCount - i.reservedCount) >= :roomsCount
            """)
    List<Inventory> findAndLockAvailableInventory(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("roomsCount") Integer roomsCount,
            @Param("dateCount") Long dateCount
    );


}
