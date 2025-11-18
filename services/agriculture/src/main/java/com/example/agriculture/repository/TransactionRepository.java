package com.example.agriculture.repository;

import com.example.agriculture.entity.Transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    List<Transaction> findByType(String type);
    List<Transaction> findByDateBetween(LocalDateTime startDate, LocalDateTime endDate);
}
