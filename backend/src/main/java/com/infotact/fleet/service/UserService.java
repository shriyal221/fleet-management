package com.infotact.fleet.service;

import com.infotact.fleet.api.dto.AuthResponse;
import com.infotact.fleet.api.dto.LoginRequest;
import com.infotact.fleet.api.dto.RegisterRequest;
import com.infotact.fleet.domain.AppUser;
import com.infotact.fleet.domain.Role;
import com.infotact.fleet.exception.ResourceNotFoundException;
import com.infotact.fleet.repository.AppUserRepository;
import com.infotact.fleet.security.JwtTokenProvider;
import com.infotact.fleet.service.AuditService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class UserService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuditService auditService;

    public UserService(AppUserRepository userRepository, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider, AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.auditService = auditService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Username already exists: " + request.username());
        }
        if (userRepository.existsByContactNumber(request.contactNumber())) {
            throw new IllegalArgumentException("Contact number already registered: " + request.contactNumber());
        }

        AppUser user = new AppUser(
                request.username(),
                passwordEncoder.encode(request.password()),
                request.role(),
                request.name(),
                request.email(),
                request.contactNumber()
        );

        userRepository.save(user);
        auditService.log("USER_REGISTER", user.getUsername(), "Registered " + user.getName() + " (" + user.getRole() + ")");
        
        String token = jwtTokenProvider.generateToken(user);
        return new AuthResponse(token, user.getUsername(), List.of(user.getRole().name()), user.getName());
    }

    public AuthResponse login(LoginRequest request) {
        AppUser user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid username or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new IllegalArgumentException("Invalid username or password");
        }

        String token = jwtTokenProvider.generateToken(user);
        auditService.log("USER_LOGIN", user.getUsername(), "Logged in successfully");
        return new AuthResponse(token, user.getUsername(), List.of(user.getRole().name()), user.getName());
    }
}
