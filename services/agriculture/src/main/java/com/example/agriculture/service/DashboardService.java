package com.example.agriculture.service;

import com.example.agriculture.dto.dashboard.MunicipalDashboardResponse;
import com.example.agriculture.dto.transaction.TransactionResponse;
import com.example.agriculture.entity.Transaction;
import com.example.agriculture.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class DashboardService {
    private final TransactionRepository transactionRepository;

    public MunicipalDashboardResponse getMunicipalDashboardData() {
        // First we need to fetch the data from various sources
        // First from Programs
        // Then from Transactions
        List<TransactionResponse> transactions = transactionRepository.findAll().stream()
                .map(this::toTransactionResponse)
                .toList();

        return MunicipalDashboardResponse.builder()
                .dashboardId(UUID.randomUUID())
                .transactions(transactions)
                .build();
    }


    private TransactionResponse toTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .transactionId(transaction.getId())
                .amount(transaction.getAmount())
                .date(transaction.getDate())
                .type(transaction.getType())
                .build();
    }
}
