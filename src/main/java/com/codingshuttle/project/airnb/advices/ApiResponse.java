package com.codingshuttle.project.airnb.advices;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.processing.Pattern;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter

public class ApiResponse<T>{

    @JsonFormat(pattern = "dd-MM-yyyy hh:mm:ss ")
    private LocalDateTime localDateTime;

    private T data;

    private ApiError error;

    public ApiResponse() {
        this.localDateTime = LocalDateTime.now();
    }

    public ApiResponse(T data) {
        this();
        this.data = data;
    }

    public ApiResponse(ApiError error) {
        this();
        this.error = error;
    }


}
