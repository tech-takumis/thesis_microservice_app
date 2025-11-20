import 'package:flutter/material.dart';
import 'package:mobile/data/models/api_error_response.dart';
import 'package:mobile/data/utils/api_error_handler.dart';
import 'package:dio/dio.dart';
import 'dart:convert';

/// Test widget to validate API error handling functionality
class ApiErrorTestWidget extends StatefulWidget {
  const ApiErrorTestWidget({super.key});

  @override
  State<ApiErrorTestWidget> createState() => _ApiErrorTestWidgetState();
}

class _ApiErrorTestWidgetState extends State<ApiErrorTestWidget> {
  String _result = '';

  void _testApiErrorParsing() {
    setState(() {
      _result = 'Testing API Error Parsing...\n\n';
    });

    // Test various API error responses that match your backend structure
    final testCases = [
      {
        'name': 'Bad Request (400)',
        'data': {
          'success': false,
          'message': 'Validation failed for field: email',
          'status': 400,
          'timestamp': DateTime.now().toIso8601String(),
          'details': {'field': 'email', 'error': 'Invalid format'}
        }
      },
      {
        'name': 'Unauthorized (401)',
        'data': {
          'success': false,
          'message': 'Authentication token has expired',
          'status': 401,
          'timestamp': DateTime.now().toIso8601String(),
        }
      },
      {
        'name': 'Not Found (404)',
        'data': {
          'success': false,
          'message': 'Application type not found',
          'status': 404,
          'timestamp': DateTime.now().toIso8601String(),
        }
      },
      {
        'name': 'Conflict (409)',
        'data': {
          'success': false,
          'message': 'Application already exists for this user',
          'status': 409,
          'timestamp': DateTime.now().toIso8601String(),
        }
      },
      {
        'name': 'Internal Server Error (500)',
        'data': {
          'success': false,
          'message': 'An unexpected error occurred',
          'status': 500,
          'timestamp': DateTime.now().toIso8601String(),
          'details': {'error': 'Database connection failed'}
        }
      },
    ];

    for (final testCase in testCases) {
      try {
        // Parse the error response
        final apiError = ApiErrorHandler.parseApiErrorResponse(testCase['data']);

        if (apiError != null) {
          final userMessage = ApiErrorHandler.getUserFriendlyMessage(apiError);
          final isRecoverable = ApiErrorHandler.isRecoverableError(apiError);
          final suggestedAction = ApiErrorHandler.getSuggestedAction(apiError);

          setState(() {
            _result += '✅ ${testCase['name']}:\n';
            _result += '   Original: ${apiError.message}\n';
            _result += '   User-friendly: $userMessage\n';
            _result += '   Recoverable: $isRecoverable\n';
            if (suggestedAction != null) {
              _result += '   Suggested action: $suggestedAction\n';
            }
            _result += '\n';
          });
        } else {
          setState(() {
            _result += '❌ ${testCase['name']}: Failed to parse\n\n';
          });
        }
      } catch (e) {
        setState(() {
          _result += '❌ ${testCase['name']}: Exception - $e\n\n';
        });
      }
    }

    // Test DioException handling
    _testDioExceptionHandling();
  }

  void _testDioExceptionHandling() {
    setState(() {
      _result += 'Testing DioException Handling...\n\n';
    });

    // Simulate different DioException types
    final dioExceptions = [
      {
        'name': 'Connection Timeout',
        'exception': DioException(
          requestOptions: RequestOptions(path: '/test'),
          type: DioExceptionType.connectionTimeout,
        )
      },
      {
        'name': 'Connection Error',
        'exception': DioException(
          requestOptions: RequestOptions(path: '/test'),
          type: DioExceptionType.connectionError,
        )
      },
      {
        'name': 'Bad Response with API Error',
        'exception': DioException(
          requestOptions: RequestOptions(path: '/test'),
          type: DioExceptionType.badResponse,
          response: Response(
            requestOptions: RequestOptions(path: '/test'),
            statusCode: 400,
            data: {
              'success': false,
              'message': 'Invalid file format',
              'status': 400,
              'timestamp': DateTime.now().toIso8601String(),
            }
          )
        )
      },
    ];

    for (final testCase in dioExceptions) {
      try {
        final userMessage = ApiErrorHandler.handleDioException(testCase['exception'] as DioException);

        setState(() {
          _result += '✅ ${testCase['name']}:\n';
          _result += '   User message: $userMessage\n\n';
        });
      } catch (e) {
        setState(() {
          _result += '❌ ${testCase['name']}: Exception - $e\n\n';
        });
      }
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('API Error Handler Test'),
        backgroundColor: const Color(0xFF4CAF50),
        foregroundColor: Colors.white,
      ),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            ElevatedButton(
              onPressed: _testApiErrorParsing,
              style: ElevatedButton.styleFrom(
                backgroundColor: const Color(0xFF4CAF50),
                foregroundColor: Colors.white,
              ),
              child: const Text('Run API Error Tests'),
            ),
            const SizedBox(height: 16),
            Expanded(
              child: SingleChildScrollView(
                child: Container(
                  width: double.infinity,
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: Colors.grey[100],
                    borderRadius: BorderRadius.circular(8),
                    border: Border.all(color: Colors.grey[300]!),
                  ),
                  child: Text(
                    _result.isEmpty ? 'Click the button to run tests...' : _result,
                    style: const TextStyle(
                      fontFamily: 'monospace',
                      fontSize: 12,
                    ),
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
