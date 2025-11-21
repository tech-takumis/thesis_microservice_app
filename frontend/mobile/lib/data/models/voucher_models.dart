// Voucher Status Enum
enum VoucherStatus {
  ACTIVE,
  USED,
  EXPIRED,
  CANCELLED,
  PENDING;

  static VoucherStatus fromString(String status) {
    return VoucherStatus.values.firstWhere(
      (e) => e.name == status,
      orElse: () => VoucherStatus.PENDING,
    );
  }
}

// Voucher Response DTO
class VoucherResponseDto {
  final String id;
  final String code;
  final String? ownerUserId;
  final String? ownerName;
  final String status;
  final double? amount;
  final String? description;
  final DateTime? issuedDate;
  final DateTime? expiryDate;
  final DateTime? usedDate;
  final String? usedBy;
  final String? applicationId;
  final String? insuranceId;
  final String? remarks;

  VoucherResponseDto({
    required this.id,
    required this.code,
    this.ownerUserId,
    this.ownerName,
    required this.status,
    this.amount,
    this.description,
    this.issuedDate,
    this.expiryDate,
    this.usedDate,
    this.usedBy,
    this.applicationId,
    this.insuranceId,
    this.remarks,
  });

  factory VoucherResponseDto.fromJson(Map<String, dynamic> json) {
    return VoucherResponseDto(
      id: json['id'] ?? '',
      code: json['code'] ?? '',
      ownerUserId: json['ownerUserId'],
      ownerName: json['ownerName'],
      status: json['status'] ?? 'PENDING',
      amount: json['amount']?.toDouble(),
      description: json['description'],
      issuedDate: json['issuedDate'] != null
          ? DateTime.parse(json['issuedDate'])
          : null,
      expiryDate: json['expiryDate'] != null
          ? DateTime.parse(json['expiryDate'])
          : null,
      usedDate: json['usedDate'] != null
          ? DateTime.parse(json['usedDate'])
          : null,
      usedBy: json['usedBy'],
      applicationId: json['applicationId'],
      insuranceId: json['insuranceId'],
      remarks: json['remarks'],
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'code': code,
      'ownerUserId': ownerUserId,
      'ownerName': ownerName,
      'status': status,
      'amount': amount,
      'description': description,
      'issuedDate': issuedDate?.toIso8601String(),
      'expiryDate': expiryDate?.toIso8601String(),
      'usedDate': usedDate?.toIso8601String(),
      'usedBy': usedBy,
      'applicationId': applicationId,
      'insuranceId': insuranceId,
      'remarks': remarks,
    };
  }

  VoucherStatus get voucherStatus => VoucherStatus.fromString(status);

  bool get isExpired => expiryDate != null && DateTime.now().isAfter(expiryDate!);
  bool get isActive => status == 'ACTIVE' && !isExpired;
  bool get isUsed => status == 'USED';
}
