package com.hashjosh.program.runner;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.hashjosh.program.entity.Program;
import com.hashjosh.constant.program.enums.ProgramStatus;
import com.hashjosh.constant.program.enums.ProgramType;
import com.hashjosh.program.repository.ProgramRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class ProgramInitializer {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ProgramRepository programRepository;

    @Bean
    CommandLineRunner seedPrograms() {
        return args -> {
            initializePrograms();
        };
    }

    private boolean isProgramNotNull() {
        return programRepository.count() > 0;
    }

    private void initializePrograms() {
        if (!isProgramNotNull()) {
            List<Program> programs = new ArrayList<>();

            // Rice Competitiveness Enhancement Fund (RCEF) Programs
            programs.add(createProgram(
                    "Rice Competitiveness Enhancement Fund (RCEF) - Seed Program",
                    ProgramType.TRAINING,
                    ProgramStatus.ACTIVE,
                    75,
                    "Additional notes here."
            ));

            programs.add(createProgram(
                    "RCEF - Mechanization Program",
                    ProgramType.WORKSHOP,
                    ProgramStatus.ACTIVE,
                    60,
                    "Additional notes here."
            ));

            // National Rice Program
            programs.add(createProgram(
                    "National Rice Program - Hybrid Rice Production",
                    ProgramType.TRAINING,
                    ProgramStatus.COMPLETED,
                    100,
                    "Additional notes here."
            ));

            programs.add(createProgram(
                    "National Rice Program - Palay Check System",
                    ProgramType.WORKSHOP,
                    ProgramStatus.ACTIVE,
                    85,
                    "Additional notes here."
            ));

            // Corn Program
            programs.add(createProgram(
                    "National Corn Program - Hybrid Corn Seed Distribution",
                    ProgramType.TRAINING,
                    ProgramStatus.ACTIVE,
                    70,
                    "Additional notes here."
            ));

            programs.add(createProgram(
                    "Corn-Based Farmers Association Training",
                    ProgramType.WORKSHOP,
                    ProgramStatus.COMPLETED,
                    95,
                    "Additional notes here."
            ));

            // Organic Agriculture Program
            programs.add(createProgram(
                    "National Organic Agriculture Program (NOAP)",
                    ProgramType.TRAINING,
                    ProgramStatus.ACTIVE,
                    80,
                    "Additional notes here."
            ));

            programs.add(createProgram(
                    "Integrated Organic Farming System Training",
                    ProgramType.WORKSHOP,
                    ProgramStatus.ACTIVE,
                    65,
                    "Additional notes here."
            ));

            // High-Value Crops Program
            programs.add(createProgram(
                    "High-Value Crops Development Program - Vegetable Production",
                    ProgramType.TRAINING,
                    ProgramStatus.COMPLETED,
                    100,
                    "Additional notes here."
            ));

            programs.add(createProgram(
                    "Fruit Crops Production and Marketing Training",
                    ProgramType.WORKSHOP,
                    ProgramStatus.ACTIVE,
                    75,
                   "Additional notes here."
            ));

            programRepository.saveAll(programs);
            System.out.println("✅ Inserted " + programs.size() + " real DA programs.");
        } else {
            System.out.println("✅ Programs already exist. Skipping initialization.");
        }
    }

    private Program createProgram(String name, ProgramType type, ProgramStatus status, int completion, String notes) {
        Program program = new Program();
        program.setName(name);
        program.setType(type);
        program.setStatus(status);
        program.setCompletion(completion);
        program.setCreatedAt(LocalDateTime.now());
        program.setUpdatedAt(LocalDateTime.now());
        program.setNotes(notes);
        return program;
    }
}
