package com.codingshuttle.project.airnb.security;


import com.codingshuttle.project.airnb.dto.LoginRequestDto;
import com.codingshuttle.project.airnb.dto.SignUpRequestDto;
import com.codingshuttle.project.airnb.dto.UserDto;
import com.codingshuttle.project.airnb.entites.User;
import com.codingshuttle.project.airnb.entites.enums.Role;
import com.codingshuttle.project.airnb.exception.ResourceNotFoundException;
import com.codingshuttle.project.airnb.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public UserDto signUp(SignUpRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail()).orElse(null);
        if (user != null) {
            throw new ResourceNotFoundException("User already exists with same Email: " + request.getEmail());
        }
        User newUser = modelMapper.map(request,User.class);
        newUser.setRoles(Set.of(Role.GUEST));
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser = userRepository.save(newUser);

        return modelMapper.map(newUser, UserDto.class);
    }

    public String[] login(LoginRequestDto loginRequestDto){
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginRequestDto.getEmail(),loginRequestDto.getPassword()));
        User user = (User) authentication.getPrincipal();
        String[] arr = new String[2];
        arr[0] = jwtService.generateAccessToken(user);
        arr[1] = jwtService.generateRefreshToken(user);
        return arr;
    }

    public String refreshToken(String refreshToken){
        Long id = jwtService.getUserIdFromToken(refreshToken);
        User user = userRepository.findById(id)
                .orElseThrow(()-> new ResourceNotFoundException("User not found with ID: "+id));

          return jwtService.generateRefreshToken(user);
    }

}
