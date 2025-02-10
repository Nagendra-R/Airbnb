package com.codingshuttle.project.airnb.advices;


import com.codingshuttle.project.airnb.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<?>> resourceNotFoundException(ResourceNotFoundException exception){

        ApiError apiError = ApiError.builder()
                .message(exception.getMessage())
                .status(HttpStatus.NOT_FOUND)
                .build();

        return buildErrorResponseEntity(apiError);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException exception){
        List<String> subErrors = exception.getBindingResult()
                .getAllErrors()
                .stream().map(error-> error.getDefaultMessage())
                .toList();

        ApiError apiError = ApiError.builder().message("given parameters are not valid")
                .subErrors(subErrors).status(HttpStatus.BAD_REQUEST).build();

        return buildErrorResponseEntity(apiError);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<?>> httpMessageNotReadableExceptionHandler(HttpMessageNotReadableException exception){
        List<String> subErrors = List.of(exception.getLocalizedMessage());

        ApiError apiError = ApiError.builder()
                .message("given json format is not valid format")
                .subErrors(subErrors)
                .status(HttpStatus.BAD_REQUEST)
                .build();
        return buildErrorResponseEntity(apiError);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> anyException(Exception exception){
        List<String> subErrors = List.of(exception.getLocalizedMessage());
        ApiError apiError = ApiError.builder()
                .message("Exception occurred")
                .subErrors(subErrors)
                .status(HttpStatus.BAD_REQUEST)
                .build();
        return buildErrorResponseEntity(apiError);
    }

    private ResponseEntity<ApiResponse<?>> buildErrorResponseEntity(ApiError apiError){
        return new ResponseEntity<>(new ApiResponse<>(apiError),apiError.getStatus());
    }

}
