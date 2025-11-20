import 'dart:async';
import 'package:another_flushbar/flushbar.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:mobile/data/models/application_data.dart';
import 'package:mobile/presentation/controllers/multi_step_application_controller.dart';
import 'package:mobile/presentation/widgets/application_widgets/application_step_form.dart';
import 'package:mobile/presentation/widgets/common/step_indicator.dart';
import 'package:mobile/presentation/controllers/auth_controller.dart';


class MultiStepApplicationPage extends ConsumerStatefulWidget {
  final ApplicationContent application;

  const MultiStepApplicationPage({
    super.key,
    required this.application,
  });

  @override
  ConsumerState<MultiStepApplicationPage> createState() => _MultiStepApplicationPageState();
}

class _MultiStepApplicationPageState extends ConsumerState<MultiStepApplicationPage> {
  late final MultiStepApplicationController controller;

  Future<void> _submitApplication(BuildContext context, AuthState authState) async {
    // Clear any previous errors before starting
    controller.clearError();

    print('🚀 Starting application submission...');
    final response = await controller.submitApplication(authState);

    if (!context.mounted) return;

    if (response.success) {
      print('✅ Application submitted successfully, showing success message');

      // Show success message with application ID if available
      String successMessage = 'Application submitted successfully!';
      if (response.applicationId.isNotEmpty) {
        successMessage += '\nApplication ID: ${response.applicationId}';
      }
      if (response.message.isNotEmpty) {
        successMessage = response.message;
      }

      Flushbar(
        message: successMessage,
        backgroundColor: Colors.green.withOpacity(0.9),
        duration: const Duration(seconds: 3),
        margin: const EdgeInsets.all(12),
        borderRadius: BorderRadius.circular(8),
        flushbarPosition: FlushbarPosition.TOP,
        icon: const Icon(
          Icons.check_circle,
          color: Colors.white,
        ),
      ).show(context);

      // Navigate after the flushbar duration (2.5 seconds to be safe)
      Timer(const Duration(milliseconds: 2500), () {
        if (context.mounted) {
          print('🚀 Timer completed, navigating to home');
          try {
            // Pop all routes back to the base route (which should be home)
            Navigator.of(context).popUntil((route) => route.isFirst);
            print('✅ Navigation to home completed');
          } catch (e) {
            print('❌ Navigation error: $e');
            // Fallback: just pop this page
            try {
              if (Navigator.of(context).canPop()) {
                Navigator.of(context).pop();
              }
            } catch (e2) {
              print('❌ Fallback navigation also failed: $e2');
            }
          }
        }
      });
    } else {
      // Show detailed error message from backend
      print('❌ Application submission failed: ${response.message}');

      final errorMessage = response.message.isNotEmpty
          ? response.message
          : 'Failed to submit application. Please try again.';

      // Determine error type for appropriate action
      final isAuthError = errorMessage.toLowerCase().contains('session') ||
                         errorMessage.toLowerCase().contains('unauthorized') ||
                         errorMessage.toLowerCase().contains('login');

      final isNetworkError = errorMessage.toLowerCase().contains('connection') ||
                           errorMessage.toLowerCase().contains('timeout') ||
                           errorMessage.toLowerCase().contains('network');

      final isValidationError = errorMessage.toLowerCase().contains('validation') ||
                              errorMessage.toLowerCase().contains('required') ||
                              errorMessage.toLowerCase().contains('invalid');

      // Choose appropriate icon
      IconData errorIcon = Icons.error_outline;
      if (isAuthError) {
        errorIcon = Icons.lock_outline;
      } else if (isNetworkError) {
        errorIcon = Icons.wifi_off;
      } else if (isValidationError) {
        errorIcon = Icons.warning_amber_outlined;
      }

      Flushbar(
        message: errorMessage,
        backgroundColor: Colors.red.withOpacity(0.9),
        duration: Duration(seconds: isValidationError ? 8 : 6), // Longer for validation errors
        margin: const EdgeInsets.all(12),
        borderRadius: BorderRadius.circular(8),
        flushbarPosition: FlushbarPosition.TOP,
        icon: Icon(
          errorIcon,
          color: Colors.white,
        ),
        mainButton: isAuthError
            ? TextButton(
                onPressed: () {
                  Navigator.of(context).pushNamedAndRemoveUntil(
                    '/login',
                    (route) => false
                  );
                },
                child: const Text(
                  'Login',
                  style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
                ),
              )
            : isNetworkError
                ? TextButton(
                    onPressed: () {
                      // Retry submission
                      _submitApplication(context, authState);
                    },
                    child: const Text(
                      'Retry',
                      style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold),
                    ),
                  )
                : null,
      ).show(context);
    }
  }



  @override
  void initState() {
    super.initState();
    controller = MultiStepApplicationController(widget.application);
    controller.initialize();
  }

  @override
  void dispose() {
    controller.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final authState = ref.watch(authProvider);
    const primaryGreen = Color(0xFF4CAF50);

    return Scaffold(
      backgroundColor: Colors.grey[50],
      appBar: AppBar(
        title: Text(widget.application.name),
        backgroundColor: primaryGreen,
        foregroundColor: Colors.white,
        elevation: 0,
      ),
      body: ValueListenableBuilder<bool>(
        valueListenable: controller.isLoading,
        builder: (context, isLoading, child) {
          return Stack(
            children: [
              Column(
                children: [
                  // Step indicator
                  Container(
                    color: Colors.white,
                    padding: const EdgeInsets.all(16),
                    child: StepIndicator(
                      currentStep: controller.currentStep + 1,
                      totalSteps: controller.totalSteps,
                      stepTitles: widget.application.sections
                          .map((section) => section.title)
                          .toList(),
                    ),
                  ),

                  // Form content
                  Expanded(
                    child: IndexedStack(
                      index: controller.currentStep,
                      children: widget.application.sections.asMap().entries.map((entry) {
                        final index = entry.key;
                        final section = entry.value;

                        return ApplicationStepForm(
                          formKey: controller.formKeys[index],
                          section: section,
                          controller: controller,
                        );
                      }).toList(),
                    ),
                  ),

                  // Error message display
                  ValueListenableBuilder<String>(
                    valueListenable: controller.errorMessage,
                    builder: (context, errorMsg, child) {
                      if (errorMsg.isEmpty) return const SizedBox.shrink();

                      return Container(
                        margin: const EdgeInsets.all(16),
                        padding: const EdgeInsets.all(12),
                        decoration: BoxDecoration(
                          color: Colors.red.withOpacity(0.1),
                          border: Border.all(color: Colors.red.withOpacity(0.3)),
                          borderRadius: BorderRadius.circular(8),
                        ),
                        child: Column(
                          crossAxisAlignment: CrossAxisAlignment.start,
                          children: [
                            Row(
                              children: [
                                Icon(
                                  Icons.error_outline,
                                  color: Colors.red[600],
                                  size: 20,
                                ),
                                const SizedBox(width: 8),
                                Expanded(
                                  child: Text(
                                    errorMsg,
                                    style: TextStyle(
                                      color: Colors.red[700],
                                      fontSize: 14,
                                    ),
                                  ),
                                ),
                                IconButton(
                                  onPressed: () => controller.clearError(),
                                  icon: Icon(
                                    Icons.close,
                                    color: Colors.red[600],
                                    size: 18,
                                  ),
                                  constraints: const BoxConstraints(
                                    minWidth: 24,
                                    minHeight: 24,
                                  ),
                                  padding: EdgeInsets.zero,
                                ),
                              ],
                            ),
                            // Show retry button for certain errors
                            if (errorMsg.toLowerCase().contains('connection') ||
                                errorMsg.toLowerCase().contains('timeout') ||
                                errorMsg.toLowerCase().contains('server') ||
                                errorMsg.toLowerCase().contains('try again'))
                              Padding(
                                padding: const EdgeInsets.only(top: 8),
                                child: SizedBox(
                                  width: double.infinity,
                                  child: OutlinedButton.icon(
                                    onPressed: () {
                                      controller.clearError();
                                      // Trigger revalidation of current step
                                      setState(() {});
                                    },
                                    icon: const Icon(Icons.refresh, size: 16),
                                    label: const Text('Retry'),
                                    style: OutlinedButton.styleFrom(
                                      foregroundColor: Colors.red[600],
                                      side: BorderSide(color: Colors.red.withOpacity(0.3)),
                                      padding: const EdgeInsets.symmetric(vertical: 8),
                                    ),
                                  ),
                                ),
                              ),
                          ],
                        ),
                      );
                    },
                  ),

                  // Navigation buttons
                  Container(
                    padding: const EdgeInsets.all(16),
                    decoration: BoxDecoration(
                      color: Colors.white,
                      boxShadow: [
                        BoxShadow(
                          color: const Color.fromARGB(0, 0, 0, 0).withAlpha(204),
                          blurRadius: 10,
                          offset: const Offset(0, -2),
                        ),
                      ],
                    ),
                    child: SafeArea(
                      child: Row(
                        children: [
                  // Previous button
                  if (controller.currentStep != 0)
                    Expanded(
                      flex: 1,
                      child: ValueListenableBuilder<bool>(
                        valueListenable: controller.isLoading,
                        builder: (context, isLoading, child) {
                          return OutlinedButton(
                            onPressed: isLoading
                                ? null
                                : () {
                                    setState(() {
                                      controller.previousStep();
                                    });
                                  },
                            style: OutlinedButton.styleFrom(
                              foregroundColor: primaryGreen,
                              side: BorderSide(color: primaryGreen),
                              padding: const EdgeInsets.symmetric(vertical: 16),
                              shape: RoundedRectangleBorder(
                                borderRadius: BorderRadius.circular(12),
                              ),
                            ),
                            child: const Text('Previous'),
                          );
                        },
                      ),
                    ),

                  if (controller.currentStep != 0) const SizedBox(width: 16),

                  // Next/Submit button
                  Expanded(
                    flex: 2,
                    child: ValueListenableBuilder<bool>(
                      valueListenable: controller.isLoading,
                      builder: (context, isLoading, child) {
                        return ElevatedButton(
                          onPressed: isLoading
                              ? null
                              : (controller.currentStep == controller.totalSteps - 1
                                  ? () => _submitApplication(context, authState)
                                  : () {
                                      setState(() {
                                        controller.nextStep();
                                      });
                                    }),
                          style: ElevatedButton.styleFrom(
                            backgroundColor: primaryGreen,
                            foregroundColor: Colors.white,
                            padding: const EdgeInsets.symmetric(vertical: 16, horizontal: 12),
                            shape: RoundedRectangleBorder(
                              borderRadius: BorderRadius.circular(12),
                            ),
                            elevation: 2,
                          ),
                          child: isLoading
                              ? const SizedBox(
                                  height: 20,
                                  width: 20,
                                  child: CircularProgressIndicator(
                                    strokeWidth: 2,
                                    valueColor: AlwaysStoppedAnimation<Color>(Colors.white),
                                  ),
                                )
                              : Row(
                                  mainAxisAlignment: MainAxisAlignment.center,
                                  mainAxisSize: MainAxisSize.min,
                                  children: [
                                    Flexible(
                                      child: Text(
                                        controller.currentStep == controller.totalSteps - 1
                                            ? 'Submit'
                                            : 'Next',
                                        style: const TextStyle(
                                          fontSize: 16,
                                          fontWeight: FontWeight.w600,
                                        ),
                                        textAlign: TextAlign.center,
                                        overflow: TextOverflow.ellipsis,
                                      ),
                                    ),
                                    const SizedBox(width: 8),
                                    Icon(
                                      controller.currentStep == controller.totalSteps - 1
                                          ? Icons.send
                                          : Icons.arrow_forward,
                                      size: 20,
                                    ),
                                  ],
                                ),
                        );
                      },
                    ),
                  ),
                        ],
                      ),
                    ),
                  ),
                ],
              ),

              // Loading overlay
              if (isLoading && controller.currentStep == controller.totalSteps - 1)
                Container(
                  color: Colors.black.withOpacity(0.5),
                  child: const Center(
                    child: Card(
                      margin: EdgeInsets.all(32),
                      child: Padding(
                        padding: EdgeInsets.all(24),
                        child: Column(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            CircularProgressIndicator(
                              color: Color(0xFF4CAF50),
                            ),
                            SizedBox(height: 16),
                            Text(
                              'Submitting Application...',
                              style: TextStyle(
                                fontSize: 16,
                                fontWeight: FontWeight.w500,
                              ),
                            ),
                            SizedBox(height: 8),
                            Text(
                              'Please wait while we process your submission',
                              style: TextStyle(
                                fontSize: 12,
                                color: Colors.grey,
                              ),
                              textAlign: TextAlign.center,
                            ),
                          ],
                        ),
                      ),
                    ),
                  ),
                ),
            ],
          );
        },
      ),
    );
  }
}
