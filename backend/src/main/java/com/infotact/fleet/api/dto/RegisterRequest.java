package com.infotact.fleet.api.dto;

import com.infotact.fleet.domain.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    String username,
    
    @NotBlank(message = "Password is required")
    @Size(min = 6, max = 100, message = "Password must be at least 6 characters")
    String password,
    
    @NotNull(message = "Role is required")
    Role role,
    
    String name,
    String email,
    
    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid contact number format")
    String contactNumber
) {}
