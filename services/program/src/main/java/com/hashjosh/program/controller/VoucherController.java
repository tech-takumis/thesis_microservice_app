package com.hashjosh.program.controller;

import com.hashjosh.program.dto.CreateVoucherRequestDto;
import com.hashjosh.program.dto.UpdateVoucherRequestDto;
import com.hashjosh.program.dto.VoucherResponseDto;
import com.hashjosh.program.enums.VoucherStatus;
import com.hashjosh.program.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/vouchers")
public class VoucherController {

    private final VoucherService voucherService;

    @PostMapping
    public VoucherResponseDto createVoucher(@RequestBody CreateVoucherRequestDto dto) {
        return voucherService.createVoucher(dto);
    }

    @PostMapping("/claim")
    public VoucherResponseDto claimVoucher(@RequestParam String code, @RequestParam UUID userId) {
        return voucherService.claimVoucher(code, userId);
    }

    @GetMapping("/user/all")
    public List<VoucherResponseDto> getAllVouchers() {
        return voucherService.getAllVouchers();
    }

    @GetMapping("/owner/{ownerUserId}")
    public List<VoucherResponseDto> getVouchersByOwner(@PathVariable UUID ownerUserId) {
        return voucherService.getVouchersByOwner(ownerUserId);
    }

    @GetMapping("/status/{status}")
    public List<VoucherResponseDto> getVouchersByStatus(@PathVariable VoucherStatus status) {
        return voucherService.getVouchersByStatus(status);
    }

    @GetMapping("/code/{code}")
    public Optional<VoucherResponseDto> getVoucherByCode(@PathVariable String code) {
        return voucherService.getVoucherByCode(code);
    }

    @GetMapping("/{id}")
    public Optional<VoucherResponseDto> getVoucherById(@PathVariable UUID id) {
        return voucherService.getVoucherById(id);
    }

    @PutMapping("/{id}")
    public VoucherResponseDto updateVoucher(@PathVariable UUID id, @RequestBody UpdateVoucherRequestDto dto) {
        return voucherService.updateVoucher(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteVoucher(@PathVariable UUID id) {
        voucherService.deleteVoucher(id);
    }
}
