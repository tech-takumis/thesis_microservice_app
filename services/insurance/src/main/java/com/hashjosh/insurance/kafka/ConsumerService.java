package com.hashjosh.insurance.kafka;

import com.hashjosh.constant.pcic.enums.InspectionStatus;
import com.hashjosh.constant.pcic.enums.InsuranceStatus;
import com.hashjosh.insurance.entity.*;
import com.hashjosh.insurance.repository.*;
import com.hashjosh.kafkacommon.application.ApplicationReceived;
import com.hashjosh.kafkacommon.application.ApplicationSubmittedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConsumerService {
    private final InsuranceRepository insuranceRepository;
    private final BatchRepository batchRepository;
    private final VerificationRepository verificationRepository;
    private final InspectionRepository inspectionRepository;
    private final PolicyRepository policyRepository;
    private final ClaimRepository claimRepository;
    private final KafkaProducer producer;

    @KafkaListener(topics = "application-submitted", groupId = "pcic-application-submitted-group")
    public void listenApplicationSubmitted(@Payload ApplicationSubmittedEvent event) {
        handleApplicationSubmittedEvent(event);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void handleApplicationSubmittedEvent(ApplicationSubmittedEvent event) {

        List<Batch> batches = batchRepository.findAllAvailableBatchesByApplicationTypeId(event.getApplicationTypeId());

        Batch selectedBatch = null;

        if (batches.isEmpty()) {
            log.info("No available batches found for application: {}. Creating new batch.", event.getSubmissionId());
            selectedBatch = createNewBatch(event);
        } else {
            // Find an available batch with space
            for (Batch batch : batches) {
                boolean notFull = batch.getTotalApplications() < batch.getMaxApplications();
                boolean available = batch.isAvailable();

                if (notFull && available) {
                    selectedBatch = batch;
                    selectedBatch.setTotalApplications(selectedBatch.getTotalApplications() + 1);
                    batchRepository.save(selectedBatch);
                    break;
                }
            }

            // If no available batch found with space, create a new one
            if (selectedBatch == null) {
                log.info("All existing batches are full for application: {}. Creating new batch.", event.getSubmissionId());
                selectedBatch = createNewBatch(event);
            }
        }

        // Create insurance record and its other related processes
        createInsuranceRecord(event, selectedBatch);

        ApplicationReceived receivedEvent = ApplicationReceived.builder()
                .provider("PCIC")
                .userId(event.getUserId())
                .submissionId(event.getSubmissionId())
                .status(InspectionStatus.PENDING.name())
                .build();

        producer.publishEvent("application-lifecycle", receivedEvent);

        log.info("Application {} sent to PCIC for inspection", event.getSubmissionId());
    }


    @Transactional(propagation = Propagation.REQUIRED)
    public void createInsuranceRecord(ApplicationSubmittedEvent event, Batch batch) {
        Insurance insurance = Insurance.builder()
                .batch(batch)
                .submissionId(event.getSubmissionId())
                .farmerId(event.getUserId())
                .farmerName(event.getFullName())
                .currentStatus(InsuranceStatus.PENDING)
                .isAIProcessed(event.getObjectKeysForAIAnalysis() != null && !event.getObjectKeysForAIAnalysis().isEmpty())
                .createdAt(LocalDateTime.now())
                .build();

        Insurance savedInsurance = insuranceRepository.save(insurance);

        Verification verification = Verification.builder()
                .insurance(savedInsurance)
                .build();
        verificationRepository.save(verification);

        Inspection inspection = Inspection.builder()
                .insurance(savedInsurance)
                .build();

        inspectionRepository.save(inspection);

        Policy policy = Policy.builder()
                .insurance(savedInsurance)
                .build();
        policyRepository.save(policy);

        Claim claim = Claim.builder()
                .insurance(savedInsurance)
                .build();
        claimRepository.save(claim);
        log.info("Created insurance record for application: {}", event.getSubmissionId());
    }

    private Batch createNewBatch(ApplicationSubmittedEvent event) {
        String batchName = generateBatchName(event.getProvider(), event.getApplicationTypeId());

        Batch newBatch = Batch.builder()
                .applicationTypeId(event.getApplicationTypeId())
                .provider(event.getProvider())
                .name(batchName)
                .description("Auto-generated batch for " + event.getProvider() + " applications")
                .totalApplications(1) // Starting with 1 application
                .maxApplications(10) // Default max applications per batch
                .isAvailable(true)
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(30)) // Default 30 days duration
                .createdAt(LocalDateTime.now())
                .build();

        Batch savedBatch = batchRepository.save(newBatch);
        log.info("Created new batch: {} for application type: {}", savedBatch.getName(), event.getApplicationTypeId());

        return savedBatch;
    }

    private String generateBatchName(String provider, UUID applicationTypeId) {
        String timestamp = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        return String.format("%s-BATCH-%s-%s", provider, applicationTypeId.toString().substring(0, 8), timestamp);
    }


}
