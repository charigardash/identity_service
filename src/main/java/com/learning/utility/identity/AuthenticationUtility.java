package com.learning.utility.identity;

import com.learning.dbentity.identity.Role;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AuthenticationUtility {

    public static String generateRefreshToken(){
        return UUID.randomUUID().toString();
    }

}
