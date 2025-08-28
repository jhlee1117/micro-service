package com.microservices.auth_service.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

import com.microservices.auth_service.dto.UserDto;
import com.microservices.auth_service.service.UserService;


@RestController
@RequestMapping("/user")
@Tag(name = "User API", description = "사용자 API 문서")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/list")
    public List<UserDto> getUserList() {
        return userService.getUserList();
    }

    @PostMapping("/create")
    public ResponseEntity<Object> createUser(@RequestBody UserDto userDto) {
        return userService.createUser(userDto);
    }

    @PutMapping("/{username}")
    public ResponseEntity<Object> updateUser(@PathVariable("username") String username, @RequestBody UserDto userDto) {        
        return userService.updateUser(username, userDto);
    }

    @DeleteMapping("/{username}")
    public ResponseEntity<String> deleteUser(@PathVariable("username") String username) {
        return userService.deleteUser(username);
    }

}
