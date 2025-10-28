package com.learning.service.serviceImpl;

import com.learning.dbentity.UserEntity;
import com.learning.repository.UserTestRepository;
import com.learning.service.UserService;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserServiceImpl implements UserService {

    public UserTestRepository userRepository;

    UserServiceImpl(UserTestRepository userRepository)
    {
        this.userRepository = userRepository;
    }
    @Override
    public UserEntity saveUser(UserEntity user) {
        return userRepository.save(user);
    }

    @Override
    public List<UserEntity> getAllUser() {
        return userRepository.findAll();
    }
}
