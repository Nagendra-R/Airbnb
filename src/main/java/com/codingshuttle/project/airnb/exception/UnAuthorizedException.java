package com.codingshuttle.project.airnb.exception;

public class UnAuthorizedException extends RuntimeException {
    public UnAuthorizedException(String message){
        super(message);
    }
}
