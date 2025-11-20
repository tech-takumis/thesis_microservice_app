package com.hashjosh.program.mapper;

import com.hashjosh.program.dto.CreateVoucherRequestDto;
import com.hashjosh.program.dto.UpdateVoucherRequestDto;
import com.hashjosh.program.dto.VoucherResponseDto;
import com.hashjosh.program.entity.Voucher;
import org.springframework.stereotype.Component;

@Component
public class VoucherMapper {


    // New methods for specific DTOs
    public Voucher toEntity(CreateVoucherRequestDto dto) {
        return Voucher.builder()
                .ownerUserId(dto.getOwnerUserId())
                .title(dto.getTitle())
                .voucherType(dto.getVoucherType())
                .unit(dto.getUnit())
                .quantity(dto.getQuantity())
                .issueDate(dto.getIssueDate())
                .expiryDate(dto.getExpiryDate())
                .referenceNumber(dto.getReferenceNumber())
                .build();
    }

    public VoucherResponseDto toResponseDto(Voucher entity) {
        return VoucherResponseDto.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .ownerUserId(entity.getOwnerUserId())
                .title(entity.getTitle())
                .voucherType(entity.getVoucherType())
                .unit(entity.getUnit())
                .quantity(entity.getQuantity())
                .issueDate(entity.getIssueDate())
                .expiryDate(entity.getExpiryDate())
                .referenceNumber(entity.getReferenceNumber())
                .status(entity.getStatus())
                .claimedAt(entity.getClaimedAt())
                .claimedByUserId(entity.getClaimedByUserId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public void updateEntityFromDto(Voucher entity, UpdateVoucherRequestDto dto) {
        if (dto.getTitle() != null) entity.setTitle(dto.getTitle());
        if (dto.getVoucherType() != null) entity.setVoucherType(dto.getVoucherType());
        if (dto.getUnit() != null) entity.setUnit(dto.getUnit());
        if (dto.getQuantity() != null) entity.setQuantity(dto.getQuantity());
        if (dto.getIssueDate() != null) entity.setIssueDate(dto.getIssueDate());
        if (dto.getExpiryDate() != null) entity.setExpiryDate(dto.getExpiryDate());
        if (dto.getReferenceNumber() != null) entity.setReferenceNumber(dto.getReferenceNumber());
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus());
        if (dto.getOwnerUserId() != null) entity.setOwnerUserId(dto.getOwnerUserId());
    }
}
