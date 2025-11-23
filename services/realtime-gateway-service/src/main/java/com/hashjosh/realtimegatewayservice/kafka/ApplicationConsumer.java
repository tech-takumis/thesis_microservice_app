package com.hashjosh.realtimegatewayservice.kafka;

import com.hashjosh.kafkacommon.application.*;
import com.hashjosh.kafkacommon.voucher.NewVoucherCreated;
import com.hashjosh.kafkacommon.voucher.VoucherClaimedEvent;
import com.hashjosh.realtimegatewayservice.clients.FarmerResponse;
import com.hashjosh.realtimegatewayservice.clients.FarmerServiceClient;
import com.hashjosh.realtimegatewayservice.entity.Notification;
import com.hashjosh.realtimegatewayservice.repository.NotificationRepository;
import com.hashjosh.realtimegatewayservice.service.EmailService;
import com.hashjosh.realtimegatewayservice.dto.NotificationResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
@Slf4j
public class ApplicationConsumer {

    private final SimpMessagingTemplate messagingTemplate;
    private final FarmerServiceClient farmerServiceClient;
    private final EmailService emailService;
    private final NotificationRepository notificationRepository;

    @KafkaListener(topics = "application-submitted", groupId = "realtime-group-app-submitted")
    public void listenApplicationSubmitted(@Payload ApplicationSubmittedEvent event) {
        handleApplicationSubmitted(event);
    }

    @KafkaListener(topics = "application-verified", groupId = "realtime-group-verification-completed")
    public void listenVerificationEvenet(@Payload VerificationCompletedEvent event) {
        handleVerificationEvent(event);
    }

    @KafkaListener(topics = "application-inspection-schedule", groupId = "realtime-group-inspection-scheduled")
    public void listenInspectionScheduled(@Payload InspectionScheduledEvent event) {
        handleInspectionScheduled(event);
    }

    @KafkaListener(topics = "application-inspection-completed", groupId = "realtime-group-inspection-completed")
    public void listenInspectionCompleted(@Payload InspectionCompletedEvent event) {
        handleInspectionCompleted(event);
    }

    @KafkaListener(topics = "application-policy-issued", groupId = "realtime-group-policy-issued")
    public void listenPolicyIssued(@Payload PolicyIssuedEvent event) {
        handlePolicyIssued(event);
    }

    @KafkaListener(topics = "application-claim", groupId = "realtime-group-claim-processed")
    public void listenClaimProcessed(@Payload ClaimProcessedEvent event) {
        handleClaimProcessed(event);
    }


    @KafkaListener(topics = "new-voucher-created", groupId = "realtime-group-new-voucher-claim")
    public void listenNewVoucherCreated(@Payload NewVoucherCreated event) {
        handleNewVoucherCreated(event);
    }

    @KafkaListener(topics = "voucher-claimed", groupId = "realtime-group-voucher-claimed")
    public void listenVoucherClaimed(@Payload VoucherClaimedEvent event) {
        handleVoucherClaimed(event);
    }

    private void handleVoucherClaimed(VoucherClaimedEvent event) {
        handleNotification(
                event.getOwnerUserId(),
                NotificationResponseDTO.builder()
                        .id(event.getVoucherId())
                        .title("Voucher Claimed")
                        .message("Your voucher has been claimed: " + event.getTitle())
                        .time(LocalDateTime.now())
                        .read(false)
                        .build(),
                "Voucher Claimed",
                "Your voucher has been claimed: " + event.getTitle(),
                "AGRICULTURE"
        );
    }

    private void handleNewVoucherCreated(NewVoucherCreated event) {
        handleNotification(
                event.getOwnerUserId(),
                NotificationResponseDTO.builder()
                        .id(event.getVoucherId())
                        .title("New Voucher Created")
                        .message("A new voucher has been created for you: " + event.getTitle())
                        .time(LocalDateTime.now())
                        .read(false)
                        .build(),
                "New Voucher Created",
                "A new voucher has been created for you: " + event.getTitle(),
                "AGRICULTURE"
        );
    }

    private void handleApplicationSubmitted(ApplicationSubmittedEvent e) {
        handleNotification(
            e.getUserId(),
            NotificationResponseDTO.builder()
                .id(e.getSubmissionId())
                .title("Application Submitted")
                .message("Your application has been submitted successfully.")
                .time(e.getSubmittedAt() != null ? e.getSubmittedAt() : LocalDateTime.now())
                .read(false)
                .build(),
            "Application Submitted",
            "Your application has been submitted successfully.",
                "STAFF"
        );
    }


    private void handleVerificationEvent(VerificationCompletedEvent e) {
        handleNotification(
                e.getUserId(),
                NotificationResponseDTO.builder()
                        .id(e.getSubmissionId())
                        .title("Verification completed")
                        .message("Verification completed")
                        .time(e.getVerifiedAt() != null ? e.getVerifiedAt() : LocalDateTime.now())
                        .read(false)
                        .build(),
                "Verification completed" ,
                "Verification completed",
                "STAFF"
        );
    }

    private void handleInspectionScheduled(InspectionScheduledEvent e) {
        handleNotification(
            e.getFarmerId(),
            NotificationResponseDTO.builder()
                .id(e.getSubmissionId())
                .title("Inspection Scheduled")
                .message("An inspection has been scheduled for your application.")
                .time(e.getScheduledAt() != null ? e.getScheduledAt() : LocalDateTime.now())
                .read(false)
                .build(),
            "Inspection Scheduled",
            "An inspection has been scheduled for your application.",
                "PCIC"
        );
    }

    private void handleInspectionCompleted(InspectionCompletedEvent e) {
        handleNotification(
            e.getUserId(),
            NotificationResponseDTO.builder()
                .id(e.getSubmissionId())
                .title("Inspection Completed")
                .message("Inspection for your application has been completed.")
                .time(e.getInspectedAt() != null ? e.getInspectedAt() : LocalDateTime.now())
                .read(false)
                .build(),
            "Inspection Completed",
            "Inspection for your application has been completed.",
                "STAFF"
        );
    }

    private void handlePolicyIssued(PolicyIssuedEvent e) {
        handleNotification(
            e.getUserId(),
            NotificationResponseDTO.builder()
                .id(e.getSubmissionId())
                .title("Policy Issued")
                .message("A policy has been issued for your application. Policy #: " + e.getPolicyNumber())
                .time(e.getIssuedAt() != null ? e.getIssuedAt() : LocalDateTime.now())
                .read(false)
                .build(),
            "Policy Issued: #" + e.getPolicyNumber(),
            "A policy has been issued for your application. Policy #: " + e.getPolicyNumber(),
                "STAFF"
        );
    }

    private void handleClaimProcessed(ClaimProcessedEvent e) {
        handleNotification(
            e.getUserId(),
            NotificationResponseDTO.builder()
                .id(e.getSubmissionId())
                .title("Claim Processed")
                .message("Your claim has been processed. Amount: "+ e.getClaimAmount() +" wait for further details.")
                .time(e.getProcessedAt() != null ? e.getProcessedAt() : LocalDateTime.now())
                .read(false)
                .build(),
            "Claim Processed",
            "Your claim has been processed. Amount: " + e.getClaimAmount() +" wait for further details.",
                "STAFF"
        );
    }

    private void handleNotification(UUID receiverId, NotificationResponseDTO notification, String emailSubject, String emailMessage, String tenant) {
        if (receiverId == null || notification == null) {
            log.warn("Could not extract receiverId or notification from event");
            return;
        }
        Notification notificationEntity = Notification.builder()
                .recipient(receiverId.toString())
                .type("APPLICATION")
                .tenant(tenant)
                .status("SENT")
                .title(notification.getTitle())
                .message(notification.getMessage())
                .createdAt(notification.getTime() != null ? notification.getTime() : LocalDateTime.now())
                .build();

        log.info("✅ Saved notification to database for user {}", receiverId);
        notificationRepository.save(notificationEntity);

        // Tenant
        // STAFF -> means broadcast to all staff members
        // PCIC -> means broadcast to all pcic members
        // AGRICULTURE -> means broadcast to all agriculture users

        messagingTemplate.convertAndSendToUser(
                receiverId.toString(),
                "/queue/application.notifications",
                notification
        );

        switch (tenant) {
            case "STAFF" -> messagingTemplate.convertAndSend("/topic/staff.application.notifications", notification);
            case "PCIC" -> messagingTemplate.convertAndSend("/topic/pcic.application.notifications", notification);
            case "AGRICULTURE" ->
                    messagingTemplate.convertAndSend("/topic/agriculture.application.notifications", notification);
        }

        log.info("✅ Sent WebSocket notification to user {}", receiverId);
        try {
            FarmerResponse farmer = farmerServiceClient.getFarmerById(receiverId);
            log.info("Farmer response: {}", farmer);
            String recipientEmail = farmer.getEmail();
            if (recipientEmail != null && !recipientEmail.isBlank()) {
                emailService.sendEmail(recipientEmail, emailSubject, emailMessage, false);
                log.info("✅ Sent email notification to {}", recipientEmail);
            } else {
                log.warn("No email found for userId={}", receiverId);
            }
        } catch (Exception ex) {
            log.error("🚫 Failed to send email notification: {}", ex.getMessage(), ex);
        }
    }
}
