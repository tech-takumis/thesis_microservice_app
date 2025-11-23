package com.hashjosh.rsbsa.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RsbsaUpdateRequestDTO {
    private String rsbsaId;
    private String firstName;
    private String lastName;
    private String middleName;
    private String gender;
    private String civilStatus;
    private String address;
    private String barangay;
    private String dateOfBirth;
    private String municipality;
    private String province;
    private String contactNumber;
    private String email;
    private String farmingType;
    private String primaryCrop;
    private String secondaryCrop;
    private Float farmArea;
    private String farmLocation;
    private String tenureStatus;
    private String sourceOfIncome;
    private float estimatedIncome;
    private Integer householdSize;
    private String educationLevel;
    private boolean withDisability;
}
