package com.hashjosh.insurance.service;

import com.hashjosh.insurance.mapper.PolicyMapper;
import com.hashjosh.insurance.repository.PolicyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PolicyService {
    private final PolicyRepository policyRepository;
    private final PolicyMapper policyMapper;
}
