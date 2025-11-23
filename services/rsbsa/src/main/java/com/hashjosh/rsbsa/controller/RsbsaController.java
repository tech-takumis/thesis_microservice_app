package com.hashjosh.rsbsa.controller;

import com.hashjosh.rsbsa.service.RsbsaService;
import com.hashjosh.rsbsa.dto.RsbsaRequestDto;
import com.hashjosh.rsbsa.dto.RsbsaResponseDto;
import com.hashjosh.rsbsa.dto.RsbsaUpdateRequestDTO;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/rsbsa")
public class RsbsaController {
    private final RsbsaService rsbsaService;

    public RsbsaController(RsbsaService rsbsaService) {
        this.rsbsaService = rsbsaService;
    }

    @PostMapping()
    public ResponseEntity<RsbsaResponseDto> save(@RequestBody RsbsaRequestDto dto){
        return new ResponseEntity<>(rsbsaService.save(dto), HttpStatus.CREATED);
    }


    @GetMapping()
    public ResponseEntity<List<RsbsaResponseDto>> findAll(){
        return new ResponseEntity<>(rsbsaService.findAll(),HttpStatus.OK);
    }

    @GetMapping("{rsbsa-id}/exists")
    public ResponseEntity<Boolean> exists(@PathVariable("rsbsa-id") String rsbaId){
        return new ResponseEntity<>(rsbsaService.existsByRsbsaId(rsbaId), HttpStatus.OK);
    }

    @GetMapping("/{rsbsa-id}")
    public ResponseEntity<RsbsaResponseDto> findByRsbsaId(
            @PathVariable("rsbsa-id") String rsbaId,
            HttpServletRequest request
    ){
        return new ResponseEntity<>(rsbsaService.findByRsbsaId(rsbaId,request), HttpStatus.OK);
    }

    @PutMapping("/{rsbsa-id}")
    public ResponseEntity<RsbsaResponseDto> updateRsbsa(
            @PathVariable("rsbsa-id") String rsbsaId,
            @RequestBody RsbsaUpdateRequestDTO dto
    ) {
        return ResponseEntity.ok(rsbsaService.updateRsbsa(rsbsaId, dto));
    }

    @DeleteMapping("/{rsbsa-id}")
    public ResponseEntity<Void> deleteRsbsa(@PathVariable("rsbsa-id") String rsbsaId) {
        rsbsaService.deleteRsbsa(rsbsaId);
        return ResponseEntity.noContent().build();
    }
}
