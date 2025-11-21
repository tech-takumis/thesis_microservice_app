import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:get/get.dart';
import 'package:mobile/data/models/insurance_models.dart';
import 'package:mobile/injection_container.dart';
import 'package:mobile/presentation/controllers/application_controller.dart';
import 'package:mobile/presentation/controllers/auth_controller.dart';
import 'package:mobile/presentation/controllers/insurance_controller.dart';
import 'package:intl/intl.dart';

class ApplicationTrackerPage extends ConsumerStatefulWidget {
  const ApplicationTrackerPage({super.key});

  @override
  ConsumerState<ApplicationTrackerPage> createState() => _ApplicationTrackerPageState();
}

class _ApplicationTrackerPageState extends ConsumerState<ApplicationTrackerPage> {
  final applicationController = getIt<ApplicationController>();
  final insuranceController = getIt<InsuranceController>();

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      _loadApplications();
    });
  }

  Future<void> _loadApplications() async {
    final authState = ref.read(authProvider);
    await applicationController.fetchUserApplications(authState);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Application Tracker'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: _loadApplications,
          ),
        ],
      ),
      body: Obx(() {
        if (applicationController.isLoading.value) {
          return const Center(child: CircularProgressIndicator());
        }

        if (applicationController.errorMessage.value.isNotEmpty) {
          return Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                const Icon(Icons.error_outline, size: 64, color: Colors.red),
                const SizedBox(height: 16),
                Text(
                  'Error: ${applicationController.errorMessage.value}',
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 16),
                ElevatedButton(
                  onPressed: _loadApplications,
                  child: const Text('Retry'),
                ),
              ],
            ),
          );
        }

        final applications = applicationController.userApplications;

        if (applications.isEmpty) {
          return Center(
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                Icon(Icons.inbox, size: 64, color: Colors.grey[400]),
                const SizedBox(height: 16),
                Text(
                  'No applications submitted yet',
                  style: TextStyle(fontSize: 16, color: Colors.grey[600]),
                ),
              ],
            ),
          );
        }

        final authState = ref.read(authProvider);

        return RefreshIndicator(
          onRefresh: _loadApplications,
          child: ListView.builder(
            padding: const EdgeInsets.all(16),
            itemCount: applications.length,
            itemBuilder: (context, index) {
              final application = applications[index];
              return ApplicationTrackerCard(
                application: application,
                authState: authState,
              );
            },
          ),
        );
      }),
    );
  }
}

class ApplicationTrackerCard extends StatefulWidget {
  final Map<String, dynamic> application;
  final AuthState authState;

  const ApplicationTrackerCard({
    super.key,
    required this.application,
    required this.authState,
  });

  @override
  State<ApplicationTrackerCard> createState() => _ApplicationTrackerCardState();
}

class _ApplicationTrackerCardState extends State<ApplicationTrackerCard> {
  final insuranceController = getIt<InsuranceController>();
  InsuranceResponse? insurance;
  bool isLoadingInsurance = true;

  @override
  void initState() {
    super.initState();
    _loadInsurance();
  }

  Future<void> _loadInsurance() async {
    setState(() => isLoadingInsurance = true);
    await insuranceController.fetchInsuranceByApplicationId(
      widget.application['id'],
      widget.authState,
    );
    setState(() {
      insurance = insuranceController.selectedInsurance.value;
      isLoadingInsurance = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    final applicationName = widget.application['applicationTypeName'] ?? 'Unknown';
    final submittedAt = widget.application['submittedAt'];
    final dynamicFields = widget.application['dynamicFields'] as Map<String, dynamic>?;

    final importantInfo = _extractImportantFields(applicationName, dynamicFields);
    final status = insurance?.status ?? 'PENDING';
    final statusColor = _getStatusColor(status);

    return Card(
      margin: const EdgeInsets.only(bottom: 16),
      elevation: 2,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: InkWell(
        onTap: () {
          _showApplicationDetails(context);
        },
        borderRadius: BorderRadius.circular(12),
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              // Header with title and status
              Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          applicationName,
                          style: const TextStyle(
                            fontSize: 16,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        const SizedBox(height: 4),
                        Text(
                          'Submitted: ${_formatDate(submittedAt)}',
                          style: TextStyle(
                            fontSize: 12,
                            color: Colors.grey[600],
                          ),
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(width: 12),
                  // Status chip in top right corner
                  isLoadingInsurance
                      ? const SizedBox(
                          width: 20,
                          height: 20,
                          child: CircularProgressIndicator(strokeWidth: 2),
                        )
                      : _buildStatusChip(status, statusColor),
                ],
              ),

              const Divider(height: 24),

              // Important information
              ...importantInfo.entries.map((entry) {
                return Padding(
                  padding: const EdgeInsets.only(bottom: 8),
                  child: Row(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      SizedBox(
                        width: 120,
                        child: Text(
                          entry.key,
                          style: TextStyle(
                            fontSize: 13,
                            color: Colors.grey[700],
                            fontWeight: FontWeight.w500,
                          ),
                        ),
                      ),
                      const SizedBox(width: 8),
                      Expanded(
                        child: Text(
                          entry.value,
                          style: const TextStyle(
                            fontSize: 13,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                      ),
                    ],
                  ),
                );
              }).toList(),

              // Show policy info if available
              if (insurance?.policy != null) ...[
                const Divider(height: 24),
                Container(
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: Colors.green.withOpacity(0.1),
                    borderRadius: BorderRadius.circular(8),
                    border: Border.all(color: Colors.green.withOpacity(0.3)),
                  ),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Row(
                        children: [
                          Icon(Icons.verified_user, size: 16, color: Colors.green[700]),
                          const SizedBox(width: 8),
                          Text(
                            'Policy Issued',
                            style: TextStyle(
                              fontSize: 13,
                              fontWeight: FontWeight.bold,
                              color: Colors.green[700],
                            ),
                          ),
                        ],
                      ),
                      const SizedBox(height: 8),
                      Text(
                        'Policy No: ${insurance!.policy!.policyNumber}',
                        style: const TextStyle(fontSize: 12),
                      ),
                      Text(
                        'Valid until: ${_formatDate(insurance!.policy!.endDate?.toString())}',
                        style: TextStyle(fontSize: 12, color: Colors.grey[600]),
                      ),
                    ],
                  ),
                ),
              ],

              const SizedBox(height: 12),

              // View details button
              Align(
                alignment: Alignment.centerRight,
                child: TextButton.icon(
                  onPressed: () => _showApplicationDetails(context),
                  icon: const Icon(Icons.arrow_forward, size: 16),
                  label: const Text('View Details'),
                  style: TextButton.styleFrom(
                    padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildStatusChip(String status, Color color) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
      decoration: BoxDecoration(
        color: color,
        borderRadius: BorderRadius.circular(12),
      ),
      child: Text(
        _formatStatus(status),
        style: const TextStyle(
          color: Colors.white,
          fontSize: 11,
          fontWeight: FontWeight.bold,
        ),
      ),
    );
  }

  /// Extracts important fields from dynamicFields based on application type
  Map<String, String> _extractImportantFields(
    String applicationName,
    Map<String, dynamic>? fields,
  ) {
    if (fields == null) return {};

    final Map<String, String> important = {};

    if (applicationName.contains('Crop Insurance')) {
      // Extract for Crop Insurance Application
      if (fields['first_name'] != null || fields['last_name'] != null) {
        important['Farmer Name'] = '${fields['first_name'] ?? ''} ${fields['last_name'] ?? ''}'.trim();
      }
      if (fields['lot_1_area'] != null) {
        important['Farm Area'] = '${fields['lot_1_area']} hectares';
      }
      if (fields['lot_1_variety'] != null) {
        important['Variety'] = fields['lot_1_variety'];
      }
      if (fields['lot_1_location'] != null) {
        final location = fields['lot_1_location'];
        if (location is Map) {
          important['Location'] = '${location['barangay']}, ${location['city']}';
        }
      }
      if (fields['cell_phone_number'] != null) {
        important['Contact'] = fields['cell_phone_number'];
      }
    } else if (applicationName.contains('Claim for Indemnity')) {
      // Extract for Claim for Indemnity
      if (fields['farmer_name'] != null) {
        important['Farmer Name'] = fields['farmer_name'];
      }
      if (fields['cic_no'] != null) {
        important['CIC No.'] = fields['cic_no'];
      }
      if (fields['area_damaged'] != null) {
        important['Area Damaged'] = '${fields['area_damaged']} hectares';
      }
      if (fields['cause_of_damage'] != null) {
        important['Cause'] = fields['cause_of_damage'];
      }
      if (fields['date_of_loss'] != null) {
        important['Date of Loss'] = _formatDate(fields['date_of_loss']);
      }
      if (fields['cell_phone_number'] != null) {
        important['Contact'] = fields['cell_phone_number'];
      }
    } else {
      // Generic extraction for unknown types
      final priorityKeys = [
        'farmer_name', 'first_name', 'last_name',
        'address', 'contact', 'cell_phone_number',
        'area', 'lot_1_area',
      ];

      for (final key in priorityKeys) {
        if (fields[key] != null && fields[key].toString().isNotEmpty) {
          important[_formatKey(key)] = fields[key].toString();
          if (important.length >= 5) break;
        }
      }
    }

    return important;
  }

  String _formatKey(String key) {
    return key
        .replaceAll('_', ' ')
        .split(' ')
        .map((word) => word.isEmpty ? '' : word[0].toUpperCase() + word.substring(1))
        .join(' ');
  }

  String _formatStatus(String status) {
    return status.replaceAll('_', ' ');
  }

  Color _getStatusColor(String status) {
    switch (status.toUpperCase()) {
      case 'PENDING':
        return Colors.orange;
      case 'SCHEDULE_ASSIGNED_FOR_INSPECTION':
        return Colors.blue;
      case 'INSPECTION_COMPLETED':
        return Colors.indigo;
      case 'VERIFIED':
        return Colors.teal;
      case 'CLAIMED_ISSUED':
        return Colors.purple;
      case 'POLICY_ISSUED':
        return Colors.green;
      case 'REJECTED':
        return Colors.red;
      default:
        return Colors.grey;
    }
  }

  String _formatDate(String? dateStr) {
    if (dateStr == null || dateStr.isEmpty) return 'N/A';
    try {
      final date = DateTime.parse(dateStr);
      return DateFormat('MMM dd, yyyy').format(date);
    } catch (e) {
      return dateStr;
    }
  }

  void _showApplicationDetails(BuildContext context) {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (context) => DraggableScrollableSheet(
        initialChildSize: 0.9,
        minChildSize: 0.5,
        maxChildSize: 0.95,
        expand: false,
        builder: (context, scrollController) {
          return ApplicationDetailSheet(
            application: widget.application,
            insurance: insurance,
            scrollController: scrollController,
          );
        },
      ),
    );
  }
}

class ApplicationDetailSheet extends StatelessWidget {
  final Map<String, dynamic> application;
  final InsuranceResponse? insurance;
  final ScrollController scrollController;

  const ApplicationDetailSheet({
    super.key,
    required this.application,
    required this.insurance,
    required this.scrollController,
  });

  @override
  Widget build(BuildContext context) {
    final dynamicFields = application['dynamicFields'] as Map<String, dynamic>?;

    return Container(
      padding: const EdgeInsets.all(20),
      child: ListView(
        controller: scrollController,
        children: [
          Center(
            child: Container(
              width: 40,
              height: 4,
              decoration: BoxDecoration(
                color: Colors.grey[300],
                borderRadius: BorderRadius.circular(2),
              ),
            ),
          ),
          const SizedBox(height: 20),

          Text(
            application['applicationTypeName'] ?? 'Application Details',
            style: const TextStyle(fontSize: 20, fontWeight: FontWeight.bold),
          ),
          const SizedBox(height: 8),
          Text(
            'Application ID: ${application['id']}',
            style: TextStyle(fontSize: 12, color: Colors.grey[600]),
          ),

          const Divider(height: 32),

          // Status Section
          if (insurance != null) ...[
            _buildSection(
              'Current Status',
              Icons.info_outline,
              [
                _buildDetailRow('Status', insurance!.status),
                if (insurance!.batch != null)
                  _buildDetailRow('Batch', insurance!.batch!.batchNumber ?? 'N/A'),
                if (insurance!.verification != null)
                  _buildDetailRow(
                    'Verified At',
                    _formatDate(insurance!.verification!.verifiedAt?.toString()),
                  ),
                if (insurance!.policy != null)
                  _buildDetailRow('Policy No.', insurance!.policy!.policyNumber ?? 'N/A'),
              ],
            ),
            const Divider(height: 32),
          ],

          // Application Details
          if (dynamicFields != null) ...[
            _buildSection(
              'Application Details',
              Icons.description,
              dynamicFields.entries.where((e) => !_isComplexField(e.value)).map((e) {
                return _buildDetailRow(
                  _formatKey(e.key),
                  e.value?.toString() ?? 'N/A',
                );
              }).toList(),
            ),
            const Divider(height: 32),
          ],

          // Files
          if (application['fileUploads'] != null) ...[
            _buildSection(
              'Uploaded Documents',
              Icons.attachment,
              [
                ...(application['fileUploads'] as List).map((url) {
                  return ListTile(
                    contentPadding: EdgeInsets.zero,
                    leading: const Icon(Icons.insert_drive_file),
                    title: Text(
                      _getFileName(url),
                      style: const TextStyle(fontSize: 13),
                    ),
                    trailing: IconButton(
                      icon: const Icon(Icons.download),
                      onPressed: () {
                        // Download file
                      },
                    ),
                  );
                }).toList(),
              ],
            ),
          ],
        ],
      ),
    );
  }

  Widget _buildSection(String title, IconData icon, List<Widget> children) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            Icon(icon, size: 20),
            const SizedBox(width: 8),
            Text(
              title,
              style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
            ),
          ],
        ),
        const SizedBox(height: 16),
        ...children,
      ],
    );
  }

  Widget _buildDetailRow(String label, String value) {
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Row(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          SizedBox(
            width: 140,
            child: Text(
              label,
              style: TextStyle(
                fontSize: 13,
                color: Colors.grey[700],
                fontWeight: FontWeight.w500,
              ),
            ),
          ),
          const SizedBox(width: 8),
          Expanded(
            child: Text(
              value,
              style: const TextStyle(fontSize: 13, fontWeight: FontWeight.w600),
            ),
          ),
        ],
      ),
    );
  }

  bool _isComplexField(dynamic value) {
    return value is Map || value is List;
  }

  String _formatKey(String key) {
    return key
        .replaceAll('_', ' ')
        .split(' ')
        .map((word) => word.isEmpty ? '' : word[0].toUpperCase() + word.substring(1))
        .join(' ');
  }

  String _formatDate(String? dateStr) {
    if (dateStr == null || dateStr.isEmpty) return 'N/A';
    try {
      final date = DateTime.parse(dateStr);
      return DateFormat('MMM dd, yyyy hh:mm a').format(date);
    } catch (e) {
      return dateStr;
    }
  }

  String _getFileName(String url) {
    final uri = Uri.parse(url);
    final path = uri.pathSegments.last;
    return path.split('?').first;
  }
}
