// Insurance Status Enum
enum InsuranceStatus {
  PENDING,
  SCHEDULE_ASSIGNED_FOR_INSPECTION,
  INSPECTION_COMPLETED,
  VERIFIED,
  CLAIMED_ISSUED,
  POLICY_ISSUED,
  REJECTED;

  static InsuranceStatus fromString(String status) {
    return InsuranceStatus.values.firstWhere(
      (e) => e.name == status,
      orElse: () => InsuranceStatus.PENDING,
    );
  }
}

// Batch Response DTO
class BatchResponseDTO {
  final String? batchId;
  final String? batchNumber;
  final String? status;
  final DateTime? createdAt;
  final DateTime? updatedAt;

  BatchResponseDTO({
    this.batchId,
    this.batchNumber,
    this.status,
    this.createdAt,
    this.updatedAt,
  });

  factory BatchResponseDTO.fromJson(Map<String, dynamic> json) {
    return BatchResponseDTO(
      batchId: json['batchId'],
      batchNumber: json['batchNumber'],
      status: json['status'],
      createdAt: json['createdAt'] != null
          ? DateTime.parse(json['createdAt'])
          : null,
      updatedAt: json['updatedAt'] != null
          ? DateTime.parse(json['updatedAt'])
          : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'batchId': batchId,
      'batchNumber': batchNumber,
      'status': status,
      'createdAt': createdAt?.toIso8601String(),
      'updatedAt': updatedAt?.toIso8601String(),
    };
  }
}

// Verification Response
class VerificationResponse {
  final String? verificationId;
  final String? status;
  final String? verifiedBy;
  final DateTime? verifiedAt;
  final String? remarks;

  VerificationResponse({
    this.verificationId,
    this.status,
    this.verifiedBy,
    this.verifiedAt,
    this.remarks,
  });

  factory VerificationResponse.fromJson(Map<String, dynamic> json) {
    return VerificationResponse(
      verificationId: json['verificationId'],
      status: json['status'],
      verifiedBy: json['verifiedBy'],
      verifiedAt: json['verifiedAt'] != null
          ? DateTime.parse(json['verifiedAt'])
          : null,
      remarks: json['remarks'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'verificationId': verificationId,
      'status': status,
      'verifiedBy': verifiedBy,
      'verifiedAt': verifiedAt?.toIso8601String(),
      'remarks': remarks,
    };
  }
}

// Inspection Response
class InspectionResponse {
  final String? inspectionId;
  final String? status;
  final String? inspectedBy;
  final DateTime? inspectedAt;
  final DateTime? scheduledDate;
  final String? findings;
  final String? remarks;

  InspectionResponse({
    this.inspectionId,
    this.status,
    this.inspectedBy,
    this.inspectedAt,
    this.scheduledDate,
    this.findings,
    this.remarks,
  });

  factory InspectionResponse.fromJson(Map<String, dynamic> json) {
    return InspectionResponse(
      inspectionId: json['inspectionId'],
      status: json['status'],
      inspectedBy: json['inspectedBy'],
      inspectedAt: json['inspectedAt'] != null
          ? DateTime.parse(json['inspectedAt'])
          : null,
      scheduledDate: json['scheduledDate'] != null
          ? DateTime.parse(json['scheduledDate'])
          : null,
      findings: json['findings'],
      remarks: json['remarks'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'inspectionId': inspectionId,
      'status': status,
      'inspectedBy': inspectedBy,
      'inspectedAt': inspectedAt?.toIso8601String(),
      'scheduledDate': scheduledDate?.toIso8601String(),
      'findings': findings,
      'remarks': remarks,
    };
  }
}

// Claim Response
class ClaimResponse {
  final String? claimId;
  final String? claimNumber;
  final String? status;
  final double? claimAmount;
  final DateTime? claimDate;
  final DateTime? approvedDate;
  final String? remarks;

  ClaimResponse({
    this.claimId,
    this.claimNumber,
    this.status,
    this.claimAmount,
    this.claimDate,
    this.approvedDate,
    this.remarks,
  });

  factory ClaimResponse.fromJson(Map<String, dynamic> json) {
    return ClaimResponse(
      claimId: json['claimId'],
      claimNumber: json['claimNumber'],
      status: json['status'],
      claimAmount: json['claimAmount']?.toDouble(),
      claimDate: json['claimDate'] != null
          ? DateTime.parse(json['claimDate'])
          : null,
      approvedDate: json['approvedDate'] != null
          ? DateTime.parse(json['approvedDate'])
          : null,
      remarks: json['remarks'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'claimId': claimId,
      'claimNumber': claimNumber,
      'status': status,
      'claimAmount': claimAmount,
      'claimDate': claimDate?.toIso8601String(),
      'approvedDate': approvedDate?.toIso8601String(),
      'remarks': remarks,
    };
  }
}

// Policy Response
class PolicyResponse {
  final String? policyId;
  final String? policyNumber;
  final String? status;
  final double? coverageAmount;
  final DateTime? startDate;
  final DateTime? endDate;
  final String? terms;

  PolicyResponse({
    this.policyId,
    this.policyNumber,
    this.status,
    this.coverageAmount,
    this.startDate,
    this.endDate,
    this.terms,
  });

  factory PolicyResponse.fromJson(Map<String, dynamic> json) {
    return PolicyResponse(
      policyId: json['policyId'],
      policyNumber: json['policyNumber'],
      status: json['status'],
      coverageAmount: json['coverageAmount']?.toDouble(),
      startDate: json['startDate'] != null
          ? DateTime.parse(json['startDate'])
          : null,
      endDate: json['endDate'] != null
          ? DateTime.parse(json['endDate'])
          : null,
      terms: json['terms'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'policyId': policyId,
      'policyNumber': policyNumber,
      'status': status,
      'coverageAmount': coverageAmount,
      'startDate': startDate?.toIso8601String(),
      'endDate': endDate?.toIso8601String(),
      'terms': terms,
    };
  }
}

// Main Insurance Response
class InsuranceResponse {
  final String insuranceId;
  final String submissionId;
  final String applicationName;
  final String status;
  final BatchResponseDTO? batch;
  final VerificationResponse? verification;
  final InspectionResponse? inspection;
  final ClaimResponse? claim;
  final PolicyResponse? policy;

  InsuranceResponse({
    required this.insuranceId,
    required this.submissionId,
    required this.applicationName,
    required this.status,
    this.batch,
    this.verification,
    this.inspection,
    this.claim,
    this.policy,
  });

  factory InsuranceResponse.fromJson(Map<String, dynamic> json) {
    return InsuranceResponse(
      insuranceId: json['insuranceId'] ?? '',
      submissionId: json['submissionId'] ?? '',
      applicationName: json['applicationName'] ?? '',
      status: json['status'] ?? 'PENDING',
      batch: json['batch'] != null
          ? BatchResponseDTO.fromJson(json['batch'])
          : null,
      verification: json['verification'] != null
          ? VerificationResponse.fromJson(json['verification'])
          : null,
      inspection: json['inspection'] != null
          ? InspectionResponse.fromJson(json['inspection'])
          : null,
      claim: json['claim'] != null
          ? ClaimResponse.fromJson(json['claim'])
          : null,
      policy: json['policy'] != null
          ? PolicyResponse.fromJson(json['policy'])
          : null,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'insuranceId': insuranceId,
      'submissionId': submissionId,
      'applicationName': applicationName,
      'status': status,
      'batch': batch?.toJson(),
      'verification': verification?.toJson(),
      'inspection': inspection?.toJson(),
      'claim': claim?.toJson(),
      'policy': policy?.toJson(),
    };
  }

  InsuranceStatus get insuranceStatus => InsuranceStatus.fromString(status);
}
