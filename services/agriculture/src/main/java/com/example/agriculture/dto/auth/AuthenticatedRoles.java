package com.example.agriculture.dto.auth;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthenticatedRoles {
    private String name;
    private List<String> permissions;
}
