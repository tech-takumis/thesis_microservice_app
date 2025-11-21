import 'package:dio/dio.dart';
import 'package:mobile/data/models/voucher_models.dart';
import 'package:mobile/data/utils/api_error_handler.dart';
import 'package:mobile/injection_container.dart';
import '../../presentation/controllers/auth_controller.dart';
import 'storage_service.dart';

class VoucherApiService {
  final Dio _dio;
  final String baseUrl;

  VoucherApiService(this._dio, {required this.baseUrl}) {
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
        logPrint: (obj) => print('🌐 Voucher API: $obj'),
      ),
    );

    _dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (options, handler) async {
          final token = getIt<StorageService>().getAccessToken();
          if (token != null && token.isNotEmpty) {
            options.headers['Authorization'] = 'Bearer $token';
            print(
              '🌐 Voucher API: Adding Authorization header: Bearer ...${token.substring(token.length - 20)}',
            );
          }
          return handler.next(options);
        },
        onError: (error, handler) {
          print('🚨 Voucher API Error: ${error.message}');
          print('🚨 Voucher API Error Type: ${error.type}');
          print('🚨 Voucher API Error Response: ${error.response?.data}');
          print('🚨 Voucher API Error Stack: ${error.stackTrace}');
          handler.next(error);
        },
      ),
    );
  }

  /// GET /user/all
  /// Fetches all vouchers
  Future<List<VoucherResponseDto>> getAllVouchers(AuthState authState) async {
    try {
      if (!(authState.isLoggedIn && authState.token != null && authState.token!.isNotEmpty)) {
        print('User not logged in, skipping getAllVouchers');
        return [];
      }

      print('🔍 Fetching all vouchers');
      final response = await _dio.get('/vouchers/user/all');

      print('✅ Vouchers fetched successfully: ${response.statusCode}');

      if (response.data is List) {
        return (response.data as List)
            .map((e) => VoucherResponseDto.fromJson(e as Map<String, dynamic>))
            .toList();
      } else {
        print('⚠️ Unexpected response format: ${response.data.runtimeType}');
        return [];
      }
    } on DioException catch (e) {
      print('❌ Fetch all vouchers failed: ${e.message}');
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

  /// GET /owner/{ownerUserId}
  /// Fetches vouchers by owner user ID
  Future<List<VoucherResponseDto>> getVouchersByOwner(
    String ownerUserId,
    AuthState authState,
  ) async {
    try {
      if (!(authState.isLoggedIn && authState.token != null && authState.token!.isNotEmpty)) {
        print('User not logged in, skipping getVouchersByOwner');
        return [];
      }

      print('🔍 Fetching vouchers by owner: $ownerUserId');
      final response = await _dio.get('/vouchers/owner/$ownerUserId');

      print('✅ Vouchers fetched successfully: ${response.statusCode}');

      if (response.data is List) {
        return (response.data as List)
            .map((e) => VoucherResponseDto.fromJson(e as Map<String, dynamic>))
            .toList();
      } else {
        print('⚠️ Unexpected response format: ${response.data.runtimeType}');
        return [];
      }
    } on DioException catch (e) {
      print('❌ Fetch vouchers by owner failed: ${e.message}');
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

  /// GET /status/{status}
  /// Fetches vouchers by status
  Future<List<VoucherResponseDto>> getVouchersByStatus(
    VoucherStatus status,
    AuthState authState,
  ) async {
    try {
      if (!(authState.isLoggedIn && authState.token != null && authState.token!.isNotEmpty)) {
        print('User not logged in, skipping getVouchersByStatus');
        return [];
      }

      print('🔍 Fetching vouchers by status: ${status.name}');
      final response = await _dio.get('/vouchers/status/${status.name}');

      print('✅ Vouchers fetched successfully: ${response.statusCode}');

      if (response.data is List) {
        return (response.data as List)
            .map((e) => VoucherResponseDto.fromJson(e as Map<String, dynamic>))
            .toList();
      } else {
        print('⚠️ Unexpected response format: ${response.data.runtimeType}');
        return [];
      }
    } on DioException catch (e) {
      print('❌ Fetch vouchers by status failed: ${e.message}');
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

  /// GET /code/{code}
  /// Fetches a voucher by code
  Future<VoucherResponseDto?> getVoucherByCode(
    String code,
    AuthState authState,
  ) async {
    try {
      if (!(authState.isLoggedIn && authState.token != null && authState.token!.isNotEmpty)) {
        print('User not logged in, skipping getVoucherByCode');
        return null;
      }

      print('🔍 Fetching voucher by code: $code');
      final response = await _dio.get('/vouchers/code/$code');

      print('✅ Voucher fetched successfully: ${response.statusCode}');

      if (response.data is Map<String, dynamic>) {
        return VoucherResponseDto.fromJson(response.data);
      } else {
        print('⚠️ Unexpected response format: ${response.data.runtimeType}');
        return null;
      }
    } on DioException catch (e) {
      print('❌ Fetch voucher by code failed: ${e.message}');
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

  /// GET /{id}
  /// Fetches a voucher by ID
  Future<VoucherResponseDto?> getVoucherById(
    String id,
    AuthState authState,
  ) async {
    try {
      if (!(authState.isLoggedIn && authState.token != null && authState.token!.isNotEmpty)) {
        print('User not logged in, skipping getVoucherById');
        return null;
      }

      print('🔍 Fetching voucher by ID: $id');
      final response = await _dio.get('/vouchers/$id');

      print('✅ Voucher fetched successfully: ${response.statusCode}');

      if (response.data is Map<String, dynamic>) {
        return VoucherResponseDto.fromJson(response.data);
      } else {
        print('⚠️ Unexpected response format: ${response.data.runtimeType}');
        return null;
      }
    } on DioException catch (e) {
      print('❌ Fetch voucher by ID failed: ${e.message}');
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

  /// DELETE /{id}
  /// Deletes a voucher by ID
  Future<bool> deleteVoucher(String id, AuthState authState) async {
    try {
      if (!(authState.isLoggedIn && authState.token != null && authState.token!.isNotEmpty)) {
        print('User not logged in, skipping deleteVoucher');
        return false;
      }

      print('🗑️ Deleting voucher: $id');
      final response = await _dio.delete('/vouchers/$id');

      print('✅ Voucher deleted successfully: ${response.statusCode}');
      return response.statusCode == 200 || response.statusCode == 204;
    } on DioException catch (e) {
      print('❌ Delete voucher failed: ${e.message}');
      print('❌ Response status: ${e.response?.statusCode}');
      print('❌ Response data: ${e.response?.data}');

      final apiError = ApiErrorHandler.parseApiErrorResponse(e.response?.data);
      if (apiError != null) {
        print('🔍 Parsed API error: ${apiError.message} (Status: ${apiError.status})');
      }

      return false;
    } catch (e) {
      print('❌ Unexpected error during delete: $e');
      return false;
    }
  }
}
