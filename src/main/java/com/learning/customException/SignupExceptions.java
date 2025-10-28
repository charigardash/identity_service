package com.learning.customException;

import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

public class SignupExceptions extends IdentityAppExcpetion{

    public SignupExceptions(String message, HttpStatus httpStatus){
        super("Error while sign up :" + message, httpStatus);
    }
}
