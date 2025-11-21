import 'package:dio/dio.dart';
import 'package:mobile/data/models/insurance_models.dart';
import 'package:mobile/data/utils/api_error_handler.dart';
import 'package:mobile/injection_container.dart';
import '../../presentation/controllers/auth_controller.dart';
import 'storage_service.dart';

class InsuranceApiService {
  final Dio _dio;
  final String baseUrl;

  InsuranceApiService(this._dio, {required this.baseUrl}) {
    _dio.options = BaseOptions(
      baseUrl: baseUrl,
      connectTimeout: const Duration(seconds: 10),
      receiveTimeout: const Duration(seconds: 10),
      headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json',
      },
    );

    // Add interceptors for logging and authentication
    _dio.interceptors.add(
      LogInterceptor(
        requestBody: true,
        responseBody: true,
        requestHeader: true,
        responseHeader: false,
        error: true,
        logPrint: (obj) => print('🌐 Insurance API: $obj'),
      ),
    );

    _dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (options, handler) async {
          final token = getIt<StorageService>().getAccessToken();
          if (token != null && token.isNotEmpty) {
            options.headers['Authorization'] = 'Bearer $token';
            print(
              '🌐 Insurance API: Adding Authorization header: Bearer ...${token.substring(token.length - 20)}',
            );
          }
          return handler.next(options);
        },
        onError: (error, handler) {
          print('🚨 Insurance API Error: ${error.message}');
          print('🚨 Insurance API Error Type: ${error.type}');
          print('🚨 Insurance API Error Response: ${error.response?.data}');
          print('🚨 Insurance API Error Stack: ${error.stackTrace}');
          handler.next(error);
        },
      ),
    );
  }

  /// GET /api/v1/insurance
  /// Fetches all insurance records
  Future<List<InsuranceResponse>> getAllInsurance(AuthState authState) async {
    try {
      if (!(authState.isLoggedIn && authState.token != null && authState.token!.isNotEmpty)) {
        print('User not logged in, skipping getAllInsurance');
        return [];
      }

      print('🔍 Fetching all insurance records');
      final response = await _dio.get('/insurance');

      print('✅ Insurance records fetched successfully: ${response.statusCode}');

      if (response.data is List) {
        return (response.data as List)
            .map((e) => InsuranceResponse.fromJson(e as Map<String, dynamic>))
            .toList();
      } else {
        print('⚠️ Unexpected response format: ${response.data.runtimeType}');
        return [];
      }
    } on DioException catch (e) {
      print('❌ Fetch all insurance failed: ${e.message}');
      print('❌ Response status: ${e.response?.statusCode}');
      print('❌ Response data: ${e.response?.data}');

      final apiError = ApiErrorHandler.parseApiErrorResponse(e.response?.data);
      if (apiError != null) {
        print('🔍 Parsed API error: ${apiError.message} (Status: ${apiError.status})');
      }

      return [];
    } catch (e) {
      print('❌ Unexpected error: $e');
      return [];
    }
  }

  /// GET /api/v1/insurance/user/all
  /// Fetches all insurance records for the current user
  Future<List<InsuranceResponse>> getInsuranceByUser(AuthState authState) async {
    try {
      if (!(authState.isLoggedIn && authState.token != null && authState.token!.isNotEmpty)) {
        print('User not logged in, skipping getInsuranceByUser');
        return [];
      }

      print('🔍 Fetching insurance records for current user');
      final response = await _dio.get('/insurance/user/all');

      print('✅ User insurance records fetched successfully: ${response.statusCode}');

      if (response.data is List) {
        return (response.data as List)
            .map((e) => InsuranceResponse.fromJson(e as Map<String, dynamic>))
            .toList();
      } else {
        print('⚠️ Unexpected response format: ${response.data.runtimeType}');
        return [];
      }
    } on DioException catch (e) {
      print('❌ Fetch user insurance failed: ${e.message}');
      print('❌ Response status: ${e.response?.statusCode}');
      print('❌ Response data: ${e.response?.data}');

      final apiError = ApiErrorHandler.parseApiErrorResponse(e.response?.data);
      if (apiError != null) {
        print('🔍 Parsed API error: ${apiError.message} (Status: ${apiError.status})');
      }

      return [];
    } catch (e) {
      print('❌ Unexpected error: $e');
      return [];
    }
  }

  /// GET /api/v1/insurance/{insurance-Id}
  /// Fetches a single insurance record by ID
  Future<InsuranceResponse?> getInsuranceById(
    String insuranceId,
    AuthState authState,
  ) async {
    try {
      if (!(authState.isLoggedIn && authState.token != null && authState.token!.isNotEmpty)) {
        print('User not logged in, skipping getInsuranceById');
        return null;
      }

      print('🔍 Fetching insurance by ID: $insuranceId');
      final response = await _dio.get('/insurance/$insuranceId');

      print('✅ Insurance fetched successfully: ${response.statusCode}');

      if (response.data is Map<String, dynamic>) {
        return InsuranceResponse.fromJson(response.data);
      } else {
        print('⚠️ Unexpected response format: ${response.data.runtimeType}');
        return null;
      }
    } on DioException catch (e) {
      print('❌ Fetch insurance by ID failed: ${e.message}');
      print('❌ Response status: ${e.response?.statusCode}');
      print('❌ Response data: ${e.response?.data}');

      final apiError = ApiErrorHandler.parseApiErrorResponse(e.response?.data);
      if (apiError != null) {
        print('🔍 Parsed API error: ${apiError.message} (Status: ${apiError.status})');
      }

      return null;
    } catch (e) {
      print('❌ Unexpected error: $e');
      return null;
    }
  }

  /// GET /api/v1/insurance/application/{application-id}
  /// Fetches insurance record by application ID
  Future<InsuranceResponse?> getInsuranceByApplicationId(
    String applicationId,
    AuthState authState,
  ) async {
    try {
      if (!(authState.isLoggedIn && authState.token != null && authState.token!.isNotEmpty)) {
        print('User not logged in, skipping getInsuranceByApplicationId');
        return null;
      }

      print('🔍 Fetching insurance by application ID: $applicationId');
      final response = await _dio.get('/insurance/application/$applicationId');

      print('✅ Insurance fetched successfully: ${response.statusCode}');

      if (response.data is Map<String, dynamic>) {
        return InsuranceResponse.fromJson(response.data);
      } else {
        print('⚠️ Unexpected response format: ${response.data.runtimeType}');
        return null;
      }
    } on DioException catch (e) {
      print('❌ Fetch insurance by application ID failed: ${e.message}');
      print('❌ Response status: ${e.response?.statusCode}');
      print('❌ Response data: ${e.response?.data}');

      final apiError = ApiErrorHandler.parseApiErrorResponse(e.response?.data);
      if (apiError != null) {
        print('🔍 Parsed API error: ${apiError.message} (Status: ${apiError.status})');
      }

      return null;
    } catch (e) {
      print('❌ Unexpected error: $e');
      return null;
    }
  }
}
