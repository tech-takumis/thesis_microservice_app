package com.hashjosh.pcic.service;


import com.hashjosh.jwtshareable.service.JwtService;
import com.hashjosh.kafkacommon.agriculture.NewInvitationEvent;
import com.hashjosh.kafkacommon.pcic.PcicRegistrationContract;
import com.hashjosh.pcic.config.CustomUserDetails;
import com.hashjosh.pcic.dto.auth.AuthenticatedUser;
import com.hashjosh.pcic.dto.auth.LoginRequest;
import com.hashjosh.pcic.dto.auth.LoginResponse;
import com.hashjosh.pcic.dto.auth.RegistrationRequest;
import com.hashjosh.pcic.entity.Pcic;
import com.hashjosh.pcic.entity.Permission;
import com.hashjosh.pcic.entity.Role;
import com.hashjosh.pcic.entity.InvitationToken;
import com.hashjosh.pcic.exception.ApiException;
import com.hashjosh.pcic.kafka.KafkaProducer;
import com.hashjosh.pcic.mapper.UserMapper;
import com.hashjosh.pcic.repository.PcicRepository;
import com.hashjosh.pcic.repository.RoleRepository;
import com.hashjosh.pcic.repository.InvitationTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final PcicRepository pcicRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;
    private final KafkaProducer pcicProducer;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final InvitationTokenRepository invitationTokenRepository;

    public Pcic register(RegistrationRequest request) {

        InvitationToken invitationToken = validateRegistrationToken(request.getToken());
        if (!invitationToken.getEmail().equals(request.getEmail())) {
            throw ApiException.badRequest("Email does not match invitation token");
        }

        if (pcicRepository.existsByEmail(request.getEmail())) {
            throw ApiException.conflict("Email already exists");
        }

        String username = generateUsername();
        request.setUsername(username);

        Set<Role> roles = new HashSet<>();
        request.getRoleNames().forEach(roleName -> {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> ApiException.notFound("Role not found"));
            roles.add(role);
        });

        Pcic pcic = userMapper.toUserEntity(request, roles);
        Pcic registeredPcic = pcicRepository.save(pcic);

        invitationToken.setUsed(true);
        invitationTokenRepository.save(invitationToken);

        publishUserRegistrationEvent(request, pcic);

        return registeredPcic;
    }

    private void publishUserRegistrationEvent(RegistrationRequest request, Pcic savedPcic) {
        PcicRegistrationContract contract =
                PcicRegistrationContract.builder()
                        .userId(savedPcic.getId())
                        .username(savedPcic.getUsername())
                        .password(request.getPassword())
                        .email(savedPcic.getEmail())
                        .firstName(savedPcic.getFirstName())
                        .lastName(savedPcic.getLastName())
                        .phoneNumber(savedPcic.getPhoneNumber())
                        .build();

        pcicProducer.publishEvent("pcic-events",contract);
    }

    public LoginResponse login(LoginRequest request, String clientIp, String userAgent) {
        // ✅ authenticate user
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(auth);
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();

        Pcic pcic = getPcicWithRoles(userDetails.getUserId());

        Map<String,Object> claims = buildClaims(pcic);

        // ✅ generate tokens (do NOT call login or authenticate again)
        String accessToken = jwtService.generateAccessToken(
                pcic.getUsername(),
                claims,
                jwtService.getAccessTokenExpiry(request.isRememberMe())
        );

        String refreshToken = jwtService.generateRefreshToken(
                pcic.getUsername(), clientIp, userAgent,
                jwtService.getRefreshTokenExpiry(request.isRememberMe())
        );

        String webSocketToken = jwtService.generateWebSocketToken(
                pcic.getUsername(),
                claims
        );

        AuthenticatedUser response = userMapper.toAuthenticatedResponse(pcic);

        return new LoginResponse(accessToken, refreshToken, webSocketToken, response);
    }

    @Transactional(readOnly = true)
    public AuthenticatedUser getAuthenticatedUser() {

        var Authentication = SecurityContextHolder.getContext().getAuthentication();
        if (Authentication == null || !Authentication.isAuthenticated()) {
            throw ApiException.unauthorized("User is not authenticated");
        }

        Object principal = Authentication.getPrincipal();
        if (!(principal instanceof CustomUserDetails userDetails)) {
            throw ApiException.unauthorized("User is not authenticated");
        }
        Pcic pcic = pcicRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> ApiException.notFound("User not found"));

        return userMapper.toAuthenticatedResponse(pcic);
    }

    public Pcic getPcicWithRoles(UUID userId) {
        return pcicRepository.findByIdWithRolesAndPermissions(userId)
                .orElseThrow(() -> ApiException.notFound("User not found with ID: " + userId));
    }
    private Set<String> extractPermissions(Set<Role> roles) {
        return roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .collect(Collectors.toSet());
    }

    private Set<String> extractRoles(Set<Role> roles) {
        return roles.stream()
                .map(Role::getName)
                .collect(Collectors.toSet());
    }

    private Map<String,Object> buildClaims(Pcic pcic) {

        // Extract permissions first
        Set<String> permissions = extractPermissions(pcic.getRoles());

        // Convert roles to string names
        Set<String> roles = extractRoles(pcic.getRoles());

        return Map.of(
                "userId", pcic.getId(),
                "firstname", pcic.getFirstName(),
                "lastname", pcic.getLastName(),
                "email", pcic.getEmail(),
                "phoneNumber", pcic.getPhoneNumber(),
                "roles", roles,
                "permissions", permissions
        );
    }

    public String generateRegistrationToken(String email) {
        String token = generateInvitationToken();
        LocalDateTime expiry = LocalDateTime.now().plusDays(1);
        InvitationToken invitationToken = InvitationToken.builder()
                .token(token)
                .email(email)
                .expiryDate(expiry)
                .used(false)
                .build();
        invitationTokenRepository.save(invitationToken);
        // Publish event to Kafka
        NewInvitationEvent event = NewInvitationEvent.builder()
            .email(email)
            .token(token)
            .registrationLink("http://localhost:3000/register?token=" + token)
            .expiryDate(expiry)
            .build();
        pcicProducer.publishEvent("agriculture-invitations", event);
        return token;
    }
    public InvitationToken validateRegistrationToken(String token) {
        InvitationToken invitationToken = invitationTokenRepository.findByToken(token)
                .orElseThrow(() -> ApiException.notFound("Invitation token not found"));
        if (invitationToken.isExpired()) {
            throw ApiException.badRequest("Invitation token expired");
        }
        if (invitationToken.isUsed()) {
            throw ApiException.conflict("Invitation token already used");
        }
        return invitationToken;
    }

    public String generateInvitationToken(){
        byte[] randomBytes = new byte[64];
        new SecureRandom().nextBytes(randomBytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }

    public String generateUsername() {
        Optional<String> lastUsernameOpt = pcicRepository.findLastUsername();
        String prefix = "200-";
        int nextNumber = 1;
        if (lastUsernameOpt.isPresent()) {
            String lastUsername = lastUsernameOpt.get();
            // Extract numeric part after dash
            String[] parts = lastUsername.split("-");
            if (parts.length == 2 && parts[1].matches("\\d+")) {
                nextNumber = Integer.parseInt(parts[1]) + 1;
            }
        }
        return String.format("%s%04d", prefix, nextNumber);
    }
}
