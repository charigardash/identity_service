package com.learning.service;

import com.learning.dbentity.UserEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {
    UserEntity saveUser(UserEntity user);
    List<UserEntity> getAllUser();
}
