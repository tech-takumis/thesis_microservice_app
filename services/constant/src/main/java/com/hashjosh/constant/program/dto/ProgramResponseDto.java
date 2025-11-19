package com.hashjosh.constant.program.dto;

import com.hashjosh.constant.program.enums.ProgramStatus;
import com.hashjosh.constant.program.enums.ProgramType;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Data
@Builder
public class ProgramResponseDto {
    private UUID id;
    private String name;
    private ProgramType type;
    private ProgramStatus status;
    private int completion;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
