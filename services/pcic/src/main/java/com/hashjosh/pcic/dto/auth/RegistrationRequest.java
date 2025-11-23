package com.hashjosh.pcic.dto.auth;

import jakarta.validation.constraints.*;
import lombok.*;

import java.util.Set;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationRequest {

    @NotBlank(message = "Token is required")
    private String token;

    // No need we will generate username automatically
    private String username;

    @NotBlank(message = "Firstname is required")
    @Size(max = 100,message = "Firstname must not exceed to 100 character")
    private String firstName;

    @NotBlank(message = "Lastname is required")
    @Size(max = 100,message = "Lastname must not exceed to 100 character")
    private String lastName;

    @NotBlank(message = "Password is required")
    private String password;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Phone number is required")
    @Size(max = 100, message = "Phone number must not exceed up to 100 character")
    @Pattern(
            regexp = "^\\+63\\d{10}$",
            message = "Phone number must start with +63 followed by 10 digits (e.g. +639123456789)"
    )
    private String phoneNumber;

    @NotBlank(message = "Address is required")
    @Size(max = 100,message = "Address must not exceed to 100 character")
    private String address;

    @NotEmpty(message = "PCIC must have at least one associated  role")
    private Set<String> roleNames;

}
