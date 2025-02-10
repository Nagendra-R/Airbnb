package com.codingshuttle.project.airnb.dto;

import com.codingshuttle.project.airnb.entites.User;
import com.codingshuttle.project.airnb.entites.enums.Gender;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GuestDto {

    private Long id;
    @JsonIgnore
    private User user;
    private String name;
    private Gender gender;
    private Integer age;

}
