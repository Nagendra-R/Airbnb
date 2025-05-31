package com.codingshuttle.project.airnb.entites;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id",
            referencedColumnName = "id",
            nullable = false
    )
    private Hotel hotel;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @Column(columnDefinition = "TEXT[]")        //array of text
    private String[] photos;

    @Column(columnDefinition = "TEXT[]")        // Wi-Fi,swimming pool
    private String[] amenities;

    @Column(nullable = false)    // total no of rooms in this type
    private Integer totalCount;

    @Column(nullable = false)
    private Integer capacity;           //2,3,4

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updateAt;

}

