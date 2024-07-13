package com.backend.videostreaming.controllers;

import com.backend.videostreaming.DTOs.UserDTO;
import com.backend.videostreaming.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/home")
public class HomeController {

    @Autowired
    private UserService userService;

    @GetMapping("/users")
    public List<UserDTO> getUser() {

        System.out.println("Getting Users");
        return userService.getUserDTOs();
    }

}
