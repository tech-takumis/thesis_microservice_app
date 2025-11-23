package com.hashjosh.pcic.mapper;


import com.hashjosh.pcic.dto.auth.AuthenticatedUser;
import com.hashjosh.pcic.dto.auth.RegistrationRequest;
import com.hashjosh.pcic.dto.role.AuthenticatedRoleResponse;
import com.hashjosh.pcic.entity.Pcic;
import com.hashjosh.pcic.entity.PcicProfile;
import com.hashjosh.pcic.entity.Permission;
import com.hashjosh.pcic.entity.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final PasswordEncoder passwordEncoder;

    public PcicProfile toPcicProfileEntity(RegistrationRequest request) {
        return PcicProfile.builder()
                .build();
    }
    public Pcic toUserEntity(
            RegistrationRequest request, Set<Role> roles) {

        PcicProfile profile = toPcicProfileEntity(request);
        Pcic pcic =  Pcic.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .address(request.getAddress())
                .roles(roles)
                .pcicProfile(profile)
                .build();

        profile.setPcic(pcic);
        return pcic;
    }

    public AuthenticatedUser toAuthenticatedResponse(Pcic pcic) {

        Set<AuthenticatedRoleResponse> roles = new HashSet<>();

        pcic.getRoles().forEach(role -> {
            roles.add(toAuthenticatedRoleResponse(role));
        });

        return AuthenticatedUser.builder()
                .userId(pcic.getId())
                .username(pcic.getUsername())
                .firstName(pcic.getFirstName())
                .lastName(pcic.getLastName())
                .email(pcic.getEmail())
                .phoneNumber(pcic.getPhoneNumber())
                .address(pcic.getAddress())
                .roles(roles)
                .build();
    }

    public AuthenticatedRoleResponse toAuthenticatedRoleResponse(Role role) {

        List<String> permissions = role.getPermissions().stream()
                .map(Permission::getName)
                .collect(Collectors.toList());

        return AuthenticatedRoleResponse.builder()
                .name(role.getName())
                .permissions(permissions)
                .build();
    }

}
// No changes needed for circular reference.
