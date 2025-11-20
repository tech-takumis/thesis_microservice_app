import 'package:dio/dio.dart';
import 'package:mobile/data/models/api_error_response.dart';
import 'dart:convert';

/// Utility class for handling and categorizing API errors
class ApiErrorHandler {

  /// Parses DioException and returns user-friendly error message
  static String handleDioException(DioException e) {
    print('🔍 Handling DioException: ${e.type}');
    print('🔍 Status Code: ${e.response?.statusCode}');
    print('🔍 Response Data: ${e.response?.data}');

    if (e.response != null && e.response!.data != null) {
      try {
        final apiError = parseApiErrorResponse(e.response!.data);
        if (apiError != null) {
          print('✅ Successfully parsed API error: ${apiError.message} (${apiError.status})');
          return getUserFriendlyMessage(apiError);
        }
      } catch (parseError) {
        print('❌ Failed to parse API error response: $parseError');
        // If parsing fails, continue to default handling
      }
    }

    // Handle network and timeout errors
    switch (e.type) {
      case DioExceptionType.connectionTimeout:
      case DioExceptionType.receiveTimeout:
      case DioExceptionType.sendTimeout:
        return 'Connection timeout. Please check your internet connection and try again.';

      case DioExceptionType.connectionError:
        return 'Cannot connect to server. Please check your internet connection.';

      case DioExceptionType.badResponse:
        return 'Server returned an error. Please try again later.';

      case DioExceptionType.cancel:
        return 'Request was cancelled.';

      default:
        return 'An error occurred. Please try again.';
    }
  }

  /// Attempts to parse API error response from various formats
  static ApiErrorResponse? parseApiErrorResponse(dynamic responseData) {
    print('🔍 Attempting to parse API error response...');
    print('🔍 Response data type: ${responseData.runtimeType}');

    try {
      Map<String, dynamic> errorData;

      if (responseData is Map<String, dynamic>) {
        errorData = responseData;
        print('✅ Response is already a Map<String, dynamic>');
      } else if (responseData is String) {
        print('🔍 Response is String, attempting JSON decode...');
        errorData = jsonDecode(responseData);
        print('✅ Successfully decoded JSON string');
      } else {
        print('❌ Unsupported response data type: ${responseData.runtimeType}');
        return null;
      }

      print('🔍 Error data keys: ${errorData.keys.toList()}');

      // Check if it contains the expected ApiErrorResponse fields
      if (errorData.containsKey('success') &&
          errorData.containsKey('message') &&
          errorData.containsKey('status')) {
        print('✅ Found ApiErrorResponse structure, parsing...');
        final apiError = ApiErrorResponse.fromJson(errorData);
        print('✅ Successfully created ApiErrorResponse: ${apiError.message}');
        return apiError;
      } else {
        print('❌ Missing expected ApiErrorResponse fields');
      }
    } catch (e) {
      print('❌ Error parsing API error response: $e');
      // Parsing failed, return null to indicate no structured error response
    }

    return null;
  }

  /// Converts API error to user-friendly message based on status code and content
  static String getUserFriendlyMessage(ApiErrorResponse apiError) {
    switch (apiError.status) {
      case 400: // Bad Request
        return _handleBadRequest(apiError.message);

      case 401: // Unauthorized
        return 'Your session has expired. Please log in again to continue.';

      case 403: // Forbidden
        return 'You do not have permission to perform this action.';

      case 404: // Not Found
        return _handleNotFound(apiError.message);

      case 409: // Conflict
        return _handleConflict(apiError.message);

      case 413: // Payload Too Large
        return 'One or more files are too large. Please reduce file sizes and try again.';

      case 415: // Unsupported Media Type
        return 'One or more files have unsupported formats. Please use supported file types.';

      case 422: // Unprocessable Entity
        return 'The submitted data is invalid. Please review your information and try again.';

      case 429: // Too Many Requests
        return 'Too many requests. Please wait a moment before trying again.';

      case 500: // Internal Server Error
        return 'A server error occurred. Please try again later.';

      case 502: // Bad Gateway
      case 503: // Service Unavailable
      case 504: // Gateway Timeout
        return 'The service is temporarily unavailable. Please try again in a few minutes.';

      default:
        return apiError.message.isNotEmpty
            ? 'Error: ${apiError.message}'
            : 'An unexpected error occurred. Please try again.';
    }
  }

  static String _handleBadRequest(String message) {
    final lowerMessage = message.toLowerCase();

    if (lowerMessage.contains('validation')) {
      return 'Please check your form data. Some fields contain invalid information.';
    } else if (lowerMessage.contains('required')) {
      return 'Please fill in all required fields before submitting.';
    } else if (lowerMessage.contains('file')) {
      return 'There is an issue with one or more uploaded files. Please check file formats and sizes.';
    } else if (lowerMessage.contains('format')) {
      return 'Invalid data format. Please check your input and try again.';
    } else if (lowerMessage.contains('size')) {
      return 'File size exceeds the allowed limit. Please use smaller files.';
    }

    return 'Invalid request: $message';
  }

  static String _handleNotFound(String message) {
    final lowerMessage = message.toLowerCase();

    if (lowerMessage.contains('application')) {
      return 'The application form you are trying to access no longer exists.';
    } else if (lowerMessage.contains('user')) {
      return 'User account not found. Please check your credentials.';
    }

    return 'The requested resource was not found: $message';
  }

  static String _handleConflict(String message) {
    final lowerMessage = message.toLowerCase();

    if (lowerMessage.contains('duplicate') || lowerMessage.contains('already exists')) {
      return 'This item already exists or you have already submitted a similar request.';
    } else if (lowerMessage.contains('version')) {
      return 'The data has been modified by another user. Please refresh and try again.';
    }

    return 'Conflict occurred: $message';
  }

  /// Determines if the error is recoverable by user action
  static bool isRecoverableError(ApiErrorResponse apiError) {
    switch (apiError.status) {
      case 400: // Bad Request - user can fix input
      case 401: // Unauthorized - user can re-login
      case 403: // Forbidden - may be temporary
      case 413: // Payload Too Large - user can reduce file size
      case 415: // Unsupported Media Type - user can change file type
      case 422: // Unprocessable Entity - user can fix data
      case 429: // Too Many Requests - user can wait
        return true;

      case 404: // Not Found
      case 409: // Conflict
        return true; // Might be recoverable depending on context

      case 500: // Internal Server Error
      case 502: // Bad Gateway
      case 503: // Service Unavailable
      case 504: // Gateway Timeout
        return false; // Server-side issues

      default:
        return false; // Unknown errors are considered non-recoverable
    }
  }

  /// Suggests action to user based on error type
  static String? getSuggestedAction(ApiErrorResponse apiError) {
    switch (apiError.status) {
      case 401:
        return 'Please log in again';
      case 413:
        return 'Reduce file sizes and try again';
      case 415:
        return 'Use supported file formats (PDF, JPG, PNG)';
      case 429:
        return 'Wait a few minutes before trying again';
      case 500:
      case 502:
      case 503:
      case 504:
        return 'Please try again later';
      default:
        return null;
    }
  }
}
