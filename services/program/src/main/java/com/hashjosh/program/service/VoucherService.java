package com.hashjosh.program.service;

import com.hashjosh.program.dto.CreateVoucherRequestDto;
import com.hashjosh.program.dto.UpdateVoucherRequestDto;
import com.hashjosh.program.dto.VoucherResponseDto;
import com.hashjosh.program.entity.Voucher;
import com.hashjosh.program.enums.VoucherStatus;
import com.hashjosh.program.mapper.VoucherMapper;
import com.hashjosh.program.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VoucherService {

    private final VoucherRepository voucherRepository;
    private final VoucherMapper voucherMapper;

    @Transactional
    public VoucherResponseDto createVoucher(CreateVoucherRequestDto dto) {
        Voucher voucher = voucherMapper.toEntity(dto);

        // Generate unique code
        voucher.setCode(UUID.randomUUID().toString());
        voucher.setStatus(VoucherStatus.ISSUED);
        voucher.setCreatedAt(LocalDateTime.now());

        Voucher saved = voucherRepository.save(voucher);
        return voucherMapper.toResponseDto(saved);
    }

    @Transactional
    public VoucherResponseDto claimVoucher(String code, UUID userId) {
        Optional<Voucher> optional = voucherRepository.findByCode(code);
        if (optional.isEmpty()) throw new RuntimeException("Voucher not found");
        Voucher voucher = optional.get();
        if (voucher.getStatus() != VoucherStatus.ISSUED) throw new RuntimeException("Voucher not claimable");
        voucher.setStatus(VoucherStatus.CLAIMED);
        voucher.setClaimedByUserId(userId);
        voucher.setClaimedAt(LocalDateTime.now());
        voucher.setUpdatedAt(LocalDateTime.now());
        Voucher saved = voucherRepository.save(voucher);
        return voucherMapper.toResponseDto(saved);
    }

    public List<VoucherResponseDto> getVouchersByOwner(UUID ownerUserId) {
        return voucherRepository.findByOwnerUserId(ownerUserId)
                .stream().map(voucherMapper::toResponseDto).toList();
    }

    public List<VoucherResponseDto> getVouchersByStatus(VoucherStatus status) {
        return voucherRepository.findByStatus(status)
                .stream().map(voucherMapper::toResponseDto).toList();
    }

    public Optional<VoucherResponseDto> getVoucherByCode(String code) {
        return voucherRepository.findByCode(code).map(voucherMapper::toResponseDto);
    }

    @Transactional
    public VoucherResponseDto updateVoucher(UUID id, UpdateVoucherRequestDto dto) {
        Optional<Voucher> optional = voucherRepository.findById(id);
        if (optional.isEmpty()) throw new RuntimeException("Voucher not found");

        Voucher existingVoucher = optional.get();
        voucherMapper.updateEntityFromDto(existingVoucher, dto);
        existingVoucher.setUpdatedAt(LocalDateTime.now());

        Voucher saved = voucherRepository.save(existingVoucher);
        return voucherMapper.toResponseDto(saved);
    }


    @Transactional
    public void deleteVoucher(UUID id) {
        if (!voucherRepository.existsById(id)) {
            throw new RuntimeException("Voucher not found");
        }
        voucherRepository.deleteById(id);
    }

    public Optional<VoucherResponseDto> getVoucherById(UUID id) {
        return voucherRepository.findById(id).map(voucherMapper::toResponseDto);
    }
}
