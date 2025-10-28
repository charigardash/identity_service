package com.learning.customException;

import org.springframework.http.HttpStatus;

public class IdentityAppExcpetion extends RuntimeException{
    private final HttpStatus status;

    public IdentityAppExcpetion(String message, HttpStatus status){
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus(){
        return this.status;
    }
}
