package com.learning.customException;

import org.springframework.http.HttpStatus;

public class AuthenticationFailedException extends IdentityAppExcpetion{
    public AuthenticationFailedException() {
        super("Can't authorize user", HttpStatus.FORBIDDEN);
    }
}
