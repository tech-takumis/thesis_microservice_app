package com.hashjosh.insurance.service;

import com.hashjosh.insurance.mapper.ClaimMapper;
import com.hashjosh.insurance.repository.ClaimRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClaimService {
    private final ClaimRepository claimRepository;
    private final ClaimMapper claimMapper;
}
