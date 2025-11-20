package com.hashjosh.constant.program.dto;

import com.hashjosh.constant.program.enums.ProgramStatus;
import com.hashjosh.constant.program.enums.ProgramType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramUpdateRequestDto {
    private String name;
    private ProgramType type;
    private ProgramStatus status;
    private int completion;
    private String notes;
}
