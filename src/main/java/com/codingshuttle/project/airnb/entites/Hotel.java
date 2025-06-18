package com.codingshuttle.project.airnb.entites;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String city;

    @Column(columnDefinition = "TEXT[]")        //array of text
    private String[] photos;

    @Column(columnDefinition = "TEXT[]")        // Wi-Fi,swimming pool
    private String[] amenities;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updateAt;

    @Embedded
    private HotelContactInfo hotelContactInfo;


    private Boolean active;

    @OneToMany(mappedBy = "hotel",fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Room> rooms;


    @ManyToOne(fetch = FetchType.LAZY,optional = false)
    private User owner;

}
