package com.hashjosh.program.runner;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import com.hashjosh.program.entity.Schedule;
import com.hashjosh.constant.program.enums.SchedulePriority;
import com.hashjosh.constant.program.enums.ScheduleType;
import com.hashjosh.program.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Configuration
@RequiredArgsConstructor
public class ScheduleInitializer {

    private final ScheduleRepository scheduleRepository;
    private final ObjectMapper objectMapper;

    @Bean
    CommandLineRunner seedSchedules() {
        return args -> {
            initializeSchedule();
        };
    }

    private boolean isScheduleNotNull() {
        return scheduleRepository.count() > 0;
    }

    private void initializeSchedule() {
        if (!isScheduleNotNull()) {
            List<Schedule> schedules = new ArrayList<>();

            // RCEF Seed Program Schedules
            schedules.add(createSchedule(
                    ScheduleType.VISIT,
                    LocalDateTime.of(2025, 9, 20, 9, 0),
                    SchedulePriority.HIGH,
                    "Addtional notes can be found in the attached document."
            ));

            schedules.add(createSchedule(
                    ScheduleType.MEETING,
                    LocalDateTime.of(2025, 9, 22, 14, 0),
                    SchedulePriority.MEDIUM,
                    "Addtional notes can be found in the attached document."
            ));

            // National Rice Program Schedules
            schedules.add(createSchedule(
                    ScheduleType.TRAINING,
                    LocalDateTime.of(2025, 9, 25, 8, 30),
                    SchedulePriority.HIGH,
                    "Addtional notes can be found in the attached document."
            ));

            schedules.add(createSchedule(
                    ScheduleType.WORKSHOP,
                    LocalDateTime.of(2025, 9, 28, 10, 0),
                    SchedulePriority.MEDIUM,
                    "Addtional notes can be found in the attached document."
            ));

            // Corn Program Schedules
            schedules.add(createSchedule(
                    ScheduleType.VISIT,
                    LocalDateTime.of(2025, 10, 2, 13, 0),
                    SchedulePriority.HIGH,
                    "Addtional notes can be found in the attached document."
            ));

            schedules.add(createSchedule(
                    ScheduleType.MEETING,
                    LocalDateTime.of(2025, 10, 5, 15, 0),
                    SchedulePriority.LOW,
                    "Addtional notes can be found in the attached document."
            ));

            // Organic Agriculture Program Schedules
            schedules.add(createSchedule(
                    ScheduleType.TRAINING,
                    LocalDateTime.of(2025, 10, 8, 9, 0),
                    SchedulePriority.HIGH,
                    "Addtional notes can be found in the attached document."
            ));

            schedules.add(createSchedule(
                    ScheduleType.WORKSHOP,
                    LocalDateTime.of(2025, 10, 12, 14, 0),
                    SchedulePriority.MEDIUM,
                    "Addtional notes can be found in the attached document."
            ));

            // High-Value Crops Program Schedules
            schedules.add(createSchedule(
                    ScheduleType.VISIT,
                    LocalDateTime.of(2025, 10, 15, 11, 0),
                    SchedulePriority.HIGH,
                    "Addtional notes can be found in the attached document."
            ));

            schedules.add(createSchedule(
                    ScheduleType.MEETING,
                    LocalDateTime.of(2025, 10, 18, 16, 0),
                    SchedulePriority.MEDIUM,
                   "Addtional notes can be found in the attached document."
            ));

            scheduleRepository.saveAll(schedules);
            System.out.println("✅ Inserted " + schedules.size() + " real DA program schedules.");
        } else {
            System.out.println("✅ Schedules already exist. Skipping initialization.");
        }
    }

    private Schedule createSchedule(ScheduleType type, LocalDateTime scheduleDate, SchedulePriority priority, String notes) {
        Schedule schedule = new Schedule();
        schedule.setType(type);
        schedule.setScheduleDate(scheduleDate);
        schedule.setPriority(priority);
        schedule.setCreatedAt(LocalDateTime.now());
        schedule.setUpdatedAt(LocalDateTime.now());
        schedule.setNotes(notes);
        return schedule;
    }
}