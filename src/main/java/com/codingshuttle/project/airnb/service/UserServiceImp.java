package com.codingshuttle.project.airnb.service;

import com.codingshuttle.project.airnb.entites.User;
import com.codingshuttle.project.airnb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.lang.module.ResolutionException;

@Service
@RequiredArgsConstructor
public class UserServiceImp implements UserService, UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public User getUserById(long userId) {
        return userRepository.findById(userId).orElseThrow(()->new ResolutionException("User not found with user id: "+userId));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByEmail(username).orElse(null);
    }

}
