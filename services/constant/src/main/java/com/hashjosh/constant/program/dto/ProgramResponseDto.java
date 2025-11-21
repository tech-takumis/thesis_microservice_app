package com.hashjosh.constant.program.dto;

import com.hashjosh.constant.program.enums.ProgramStatus;
import com.hashjosh.constant.program.enums.ProgramType;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
