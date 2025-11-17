package com.hashjosh.application.controller;

import com.hashjosh.application.dto.type.ApplicationTypeRequestDto;
import com.hashjosh.constant.application.ApplicationTypeResponseDto;
import com.hashjosh.application.service.ApplicationTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/application/types")
@RequiredArgsConstructor
public class ApplicationTypeController {

    private final ApplicationTypeService applicationTypeService;

    @PostMapping()
    public ResponseEntity<ApplicationTypeResponseDto> create(
            @RequestBody ApplicationTypeRequestDto dto
    ){
        ApplicationTypeResponseDto responseDto = applicationTypeService.create(dto);

        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @GetMapping()
    public ResponseEntity<List<ApplicationTypeResponseDto>> getAll(
            @RequestParam(name = "provider", required = false) String provider,
            @RequestParam(name = "sections", required = false) Boolean sections,
            @RequestParam(name = "application", required = false) Boolean application
    ){

        if(provider != null && !provider.isBlank()){
            List<ApplicationTypeResponseDto> allApplicationType = applicationTypeService.findByProvider(provider,application,sections);
            return new ResponseEntity<>(allApplicationType,HttpStatus.OK);
        }else{
            List<ApplicationTypeResponseDto> allApplicationType = applicationTypeService.findAll(application,sections);
            return new ResponseEntity<>(allApplicationType,HttpStatus.OK);
        }

    }

    @GetMapping("/{id}")
    public ResponseEntity<ApplicationTypeResponseDto> getById(
            @PathVariable UUID id,
            @RequestParam(name = "sections", required = false) Boolean sections,
            @RequestParam(name = "application", required = false) Boolean application
    ){
       return new ResponseEntity<>(applicationTypeService.findById(id,application,sections),HttpStatus.OK);
    }

    @GetMapping("/{id}/requires-ai-analysis")
    public ResponseEntity<Boolean> isRequiresAIAnalysis(@PathVariable("id") UUID applicationTypeId){
        Boolean requiresPredictions = applicationTypeService.requiresPredictions(applicationTypeId);
        return new ResponseEntity<>(requiresPredictions,HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable UUID id){
        applicationTypeService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}

