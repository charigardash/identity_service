package com.learning.customException;

import org.springframework.http.HttpStatus;

public class UserNotFoundException extends IdentityAppExcpetion{
    public UserNotFoundException(String username) {
        super("User with name " + username + " not found", HttpStatus.NOT_FOUND);
    }
}
