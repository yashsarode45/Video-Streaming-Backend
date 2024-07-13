package com.backend.videostreaming.services;

import com.backend.videostreaming.entities.User;
import com.backend.videostreaming.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

// Now this will be used to inject the UserDetailsService and the one we wrote in app config
@Service
public class CustomUserDetailService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // load user from database
        User user = userRepository.findByEmail(username).orElseThrow(() -> new RuntimeException("User not found"));

        return user;
    }
}
