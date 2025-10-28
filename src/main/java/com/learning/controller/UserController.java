package com.learning.controller;

import com.learning.dbentity.UserEntity;
import com.learning.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/learning")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/save")
    public ResponseEntity<UserEntity> saveUser(@RequestBody UserEntity user){
        UserEntity savedUser = userService.saveUser(user);
        return new ResponseEntity<>(savedUser, HttpStatus.OK);
    }

    @GetMapping("/fetchAllUser")
    public ResponseEntity<List<UserEntity>> fetchAllSaveUser(){
        List<UserEntity> entities = userService.getAllUser();
        return new ResponseEntity<>(entities, HttpStatus.OK);
    }
}
