import 'dart:convert';
import 'package:dio/dio.dart';
import 'package:file_picker/file_picker.dart';
import 'package:mobile/data/models/application_data.dart';
import 'package:mobile/data/models/application_submission.dart';
import 'package:mobile/injection_container.dart'; // For getIt
import '../../presentation/controllers/auth_controller.dart';
import 'storage_service.dart';
import 'package:http_parser/http_parser.dart';
import 'package:mime/mime.dart';

class ApplicationApiService {
  final Dio _dio;
  final String baseUrl;

  ApplicationApiService(this._dio, {required this.baseUrl}) {
    _dio.options = BaseOptions(
      baseUrl: baseUrl,
      connectTimeout: const Duration(seconds: 10),
      receiveTimeout: const Duration(seconds: 10),
      headers: {
        'Accept': 'application/json',
        // Don't set default Content-Type - let individual requests handle it
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
        logPrint: (obj) => print('🌐 API: $obj'),
      ),
    );

    _dio.interceptors.add(
      InterceptorsWrapper(
        onRequest: (options, handler) async {
          final token = getIt<StorageService>().getAccessToken();
          if (token != null && token.isNotEmpty) {
            options.headers['Authorization'] = 'Bearer $token';
            print(
              '🌐 API: Adding Authorization header: Bearer ...${token.substring(token.length - 20)}',
            );
          }
          return handler.next(options);
        },
        onError: (error, handler) {
          print('🚨 API Error: ${error.message}');
          print('🚨 API Error Type: ${error.type}');
          print('🚨 API Error Response: ${error.response?.data}');
          print('🚨 API Error Stack: ${error.stackTrace}');
          handler.next(error);
        },
      ),
    );
  }

  // Method to fetch application data - updated to handle direct array response
  Future<ApplicationContent?> fetchApplicationById(String id,AuthState authState) async {
    if (!(authState.isLoggedIn && authState.token != null && authState.token!.isNotEmpty)) {
      print('User not logged in, skipping fetchApplications');
      return null;
    }
    try {
      print('🚀 Fetching application type: $id');
      final response = await _dio.get(
        '/application/types/$id?sections=true&fields=true',
        options: Options(
          headers: {
            'Content-Type': 'application/json',
          },
        ),
      );
      print('✅ Application type fetched successfully: ${response.statusCode}');
      return ApplicationContent.fromJson(response.data);
    } on DioException catch (e) {
      print('❌ Fetch application type failed: ${e.message}');
      return null;
    } catch (e) {
      print('❌ Unexpected error: $e');
      return null;
    }
  }

  Future<ApplicationResponse?> fetchApplications(AuthState authState) async {
    try {
      if (!(authState.isLoggedIn && authState.token != null && authState.token!.isNotEmpty)) {
        print('User not logged in, skipping fetchApplications');
        return null;
      }
      print('🚀 [DEBUG] ApplicationApiService baseUrl: $baseUrl');
      print('🚀 Fetching applications from: $baseUrl/application/types');
      final response = await _dio.get(
        '/application/types?sections=true&fields=true',
        options: Options(
          headers: {
            'Content-Type': 'application/json',
          },
        ),
      );
      print('✅ Applications fetched successfully: ${response.statusCode}');
      
      // API returns a direct array
      final List<dynamic> data = response.data as List<dynamic>;
      return ApplicationResponse(
        statusCode: response.statusCode ?? 200,
        message: 'Success',
        content: data.map((e) => ApplicationContent.fromJson(e)).toList(),
      );
    } catch (e) {
      print('❌ Fetch applications failed: $e');
      throw Exception('Failed to fetch applications');
    }
  }

  Future<ApplicationSubmissionResponse> submitApplication(
    AuthState authState,
    ApplicationSubmissionRequest request,
    Map<String, PlatformFile> files,
  ) async {
    try {
      if (!(authState.isLoggedIn && authState.token != null && authState.token!.isNotEmpty)) {
        print('User not logged in, skipping submitApplication');
        return ApplicationSubmissionResponse(success: false, message: 'User not logged in', applicationId: '');
      }

      // Prepare submission JSON and process field values
      final Map<String, dynamic> updatedFieldValues = Map<String, dynamic>.from(request.fieldValues);

      // Handle different field types based on backend expectations
      print('🔍 Processing special field types:');

      // Convert farm_location to TEXT (string) as backend expects TEXT type
      if (updatedFieldValues['farm_location'] is Map<String, dynamic>) {
        final locationMap = updatedFieldValues['farm_location'] as Map<String, dynamic>;
        final locationString = [
          locationMap['barangay'],
          locationMap['city'],
          locationMap['province'],
          locationMap['region']
        ].where((part) => part != null && part.toString().trim().isNotEmpty)
         .join(', ');
        updatedFieldValues['farm_location'] = locationString;
        print('✅ farm_location: converted to TEXT: "$locationString"');
      }

      // Keep other location fields as JSON objects (lot_1_location, etc.)
      final locationFields = ['lot_1_location', 'lot_2_location', 'lot_3_location', 'lot_4_location'];
      for (final locationField in locationFields) {
        if (updatedFieldValues[locationField] is Map<String, dynamic>) {
          print('✅ $locationField: keeping as JSON object');
        }
      }

      // Keep boundary fields as JSON objects
      final boundaryFields = ['lot_1_boundaries', 'lot_2_boundaries', 'lot_3_boundaries', 'lot_4_boundaries'];
      for (final boundaryField in boundaryFields) {
        if (updatedFieldValues[boundaryField] is Map<String, dynamic>) {
          print('✅ $boundaryField: keeping as JSON object');
        }
      }

      // Ensure area fields are proper numbers
      final areaFields = ['lot_1_area', 'lot_2_area', 'lot_3_area', 'lot_4_area', 'area_insured', 'area_damaged'];
      for (final areaField in areaFields) {
        final value = updatedFieldValues[areaField];
        if (value != null) {
          if (value is num) {
            print('✅ $areaField: already a number ($value)');
          } else if (value is String && value.isNotEmpty) {
            final parsedValue = double.tryParse(value);
            if (parsedValue != null) {
              updatedFieldValues[areaField] = parsedValue;
              print('✅ $areaField: converted string to number ($parsedValue)');
            } else {
              updatedFieldValues[areaField] = 0.0;
              print('⚠️ $areaField: invalid number format, defaulting to 0.0');
            }
          } else {
            updatedFieldValues[areaField] = 0.0;
            print('⚠️ $areaField: null/empty, defaulting to 0.0');
          }
        }
      }

      // Mark file fields with empty string in submission
      for (final fileKey in files.keys) {
        updatedFieldValues[fileKey] = "";
      }

      // Process field values to ensure backend compatibility with proper data types
      final processedFieldValues = <String, dynamic>{};
      updatedFieldValues.forEach((key, value) {
        if (value == null) {
          // Handle null values based on field type expectations
          if (key.contains('area') || key.contains('amount') || key.contains('cost')) {
            processedFieldValues[key] = 0.0; // Numbers default to 0
          } else {
            processedFieldValues[key] = ""; // Strings default to empty
          }
        } else if (value is String) {
          // Trim whitespace but preserve the value
          final trimmedValue = value.trim();
          processedFieldValues[key] = trimmedValue.isEmpty ? "" : trimmedValue;
        } else if (value is num) {
          // Preserve numbers as-is (critical for area fields)
          processedFieldValues[key] = value;
        } else if (value is bool) {
          processedFieldValues[key] = value;
        } else if (value is Map<String, dynamic>) {
          // Keep JSON objects as-is (location, boundaries, etc.)
          processedFieldValues[key] = value;
          print('🔍 Preserving JSON object for $key');
        } else if (value is List) {
          // Keep arrays as-is
          processedFieldValues[key] = value;
        } else {
          // For other types, convert to string
          processedFieldValues[key] = value.toString();
        }
      });

      // Use processed values
      final finalFieldValues = processedFieldValues;

      // Get coordinates before submission and ensure proper format
      final coordinates = request.coordinates.toString();
      print('🌍 Coordinates format: "$coordinates" (type: ${coordinates.runtimeType})');

      // Create submission structure with fields at root level for @JsonAnySetter mapping
      final submissionData = <String, dynamic>{
        'applicationTypeId': request.applicationTypeId,
        'coordinates': coordinates,
      };

      // Add all field values directly at root level (not nested)
      submissionData.addAll(finalFieldValues);

      final submissionJson = jsonEncode(submissionData);

      print('🚀 Submitting application for type: ${request.applicationTypeId}');
      print('📋 Field values (will be sent at root level): $finalFieldValues');
      print('📁 Files: ${files.length}');
      print('🌍 Coordinates: $coordinates');

      if (files.isNotEmpty) {
        print('📁 File details:');
        files.forEach((fieldName, file) {
          print('  Field "$fieldName": ${file.name} (${file.extension}) - Size: ${file.size} bytes');
        });
      }

      // Allowed mime types mapping
      const allowedMimeTypes = {
        'pdf': 'application/pdf',
        'jpg': 'image/jpeg',
        'jpeg': 'image/jpeg',
        'png': 'image/png',
        'gif': 'image/gif',
        'bmp': 'image/bmp',
        'tiff': 'image/tiff',
        'tif': 'image/tiff',
        'webp': 'image/webp',
        'doc': 'application/msword',
        'docx': 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
        'xls': 'application/vnd.ms-excel',
        'xlsx': 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
        'ppt': 'application/vnd.ms-powerpoint',
        'pptx': 'application/vnd.openxmlformats-officedocument.presentationml.presentation',
        'txt': 'text/plain',
        'rtf': 'application/rtf',
      };

      final multipartFiles = <MapEntry<String, MultipartFile>>[];
      for (final entry in files.entries) {
        final fieldName = entry.key;
        final file = entry.value;

        // Skip files without content
        if (file.bytes == null && file.path == null) {
          print('⚠️ Skipping file ${file.name} for field "$fieldName" - no content available');
          continue;
        }

        if (file.bytes != null && file.bytes!.isEmpty) {
          print('⚠️ Skipping file ${file.name} for field "$fieldName" - empty file');
          continue;
        }
        // Extract extension from filename if not available
        String? extension = file.extension;
        if (extension == null || extension.isEmpty) {
          final parts = file.name.split('.');
          if (parts.length > 1) {
            extension = parts.last.toLowerCase();
          }
        } else {
          extension = extension.toLowerCase();
        }

        // Handle files without extension by examining content
        if (extension == null || extension.isEmpty) {
          if (file.bytes != null && file.bytes!.isNotEmpty) {
            final header = file.bytes!.take(8).toList();
            // Check for common file signatures
            if (header.length >= 4) {
              // PDF signature
              if (header[0] == 0x25 && header[1] == 0x50 && header[2] == 0x44 && header[3] == 0x46) {
                extension = 'pdf';
              }
              // JPEG signature
              else if (header[0] == 0xFF && header[1] == 0xD8 && header[2] == 0xFF) {
                extension = 'jpg';
              }
              // PNG signature
              else if (header[0] == 0x89 && header[1] == 0x50 && header[2] == 0x4E && header[3] == 0x47) {
                extension = 'png';
              }
            }
          }
        }

        print('🔍 Processing file: ${file.name}, extension: $extension');

        // First try to get MIME type from our allowed extensions mapping
        String? mimeType = allowedMimeTypes[extension ?? ''];

        // If not found, try lookupMimeType as fallback, but never use application/octet-stream
        if (mimeType == null) {
          final detectedMimeType = lookupMimeType(file.name, headerBytes: file.bytes);
          print('🔍 Detected MIME type: $detectedMimeType');

          // Only use the detected MIME type if it's in our allowed list and not application/octet-stream
          if (detectedMimeType != null &&
              detectedMimeType != 'application/octet-stream' &&
              allowedMimeTypes.containsValue(detectedMimeType)) {
            mimeType = detectedMimeType;
          }
        }

        // If still not found, try common fallbacks based on extension
        if (mimeType == null && extension != null) {
          switch (extension) {
            case 'jpg':
            case 'jpeg':
              mimeType = 'image/jpeg';
              break;
            case 'png':
              mimeType = 'image/png';
              break;
            case 'pdf':
              mimeType = 'application/pdf';
              break;
            case 'doc':
              mimeType = 'application/msword';
              break;
            case 'docx':
              mimeType = 'application/vnd.openxmlformats-officedocument.wordprocessingml.document';
              break;
            default:
              break;
          }
        }

        // If still not found, throw error with helpful message
        if (mimeType == null) {
          throw Exception(
            "File '${file.name}' with extension '$extension' is not supported. Allowed types: ${allowedMimeTypes.keys.join(', ')}."
          );
        }

        // Final safety check - never allow application/octet-stream
        if (mimeType == 'application/octet-stream') {
          throw Exception(
            "File '${file.name}' could not be properly identified. Please ensure the file has a proper extension and is not corrupted."
          );
        }

        print('✅ Using MIME type: $mimeType for file: ${file.name}');

        final mediaTypeParts = mimeType.split('/');
        if (mediaTypeParts.length != 2) {
          throw Exception("Invalid MIME type format: $mimeType");
        }
        final mediaType = MediaType(mediaTypeParts[0], mediaTypeParts[1]);

        if (file.bytes != null) {
          multipartFiles.add(
            MapEntry(
              fieldName,
              MultipartFile.fromBytes(
                file.bytes!,
                filename: file.name,
                contentType: mediaType,
              ),
            ),
          );
        } else if (file.path != null) {
          multipartFiles.add(
            MapEntry(
              fieldName,
              await MultipartFile.fromFile(
                file.path!,
                filename: file.name,
                contentType: mediaType,
              ),
            ),
          );
        }
      }

      // Build FormData using fromMap method for better compatibility
      final Map<String, dynamic> formDataMap = {
        'submission': MultipartFile.fromString(
          submissionJson,
          contentType: MediaType('application', 'json'),
        ),
      };

      // Add each file to the map
      for (final fileEntry in multipartFiles) {
        formDataMap[fileEntry.key] = fileEntry.value;
        print('📎 Added file "${fileEntry.key}": ${fileEntry.value.filename} (${fileEntry.value.contentType})');
      }

      final formData = FormData.fromMap(formDataMap);

      print('📋 FormData fields: ${formData.fields.map((e) => '${e.key}=${e.value}').toList()}');
      print('📁 FormData files: ${formData.files.length} files with keys: ${formData.files.map((e) => e.key).toList()}');
      // Validate JSON and show size info
      final jsonSize = submissionJson.length;
      print('📊 Submission JSON size: $jsonSize characters');
      if (jsonSize > 1000) {
        print('📊 Submission JSON (first 500 chars): ${submissionJson.substring(0, 500)}...');
        print('📊 Submission JSON (last 500 chars): ...${submissionJson.substring(jsonSize - 500)}');
      } else {
        print('📊 Full submission JSON: $submissionJson');
      }

      // Validate JSON structure and integrity
      try {
        // Check if JSON ends properly
        if (!submissionJson.endsWith('}')) {
          throw Exception('JSON appears to be truncated - does not end with }');
        }

        final decoded = jsonDecode(submissionJson);
        print('✅ JSON is valid, contains ${decoded.length} fields');

        // Verify required structure
        if (!decoded.containsKey('applicationTypeId')) {
          throw Exception('Missing applicationTypeId in submission');
        }
        if (!decoded.containsKey('coordinates')) {
          throw Exception('Missing coordinates in submission');
        }

        // Check for required fields by category at root level

        print('🔍 Checking structure (fields at root level):');
        print('  Root keys: ${decoded.keys.toList()}');
        print('  Total fields: ${decoded.length}');

        print('🔍 Checking required fields at root level:');

        // Check string/text fields at root level
        final stringFields = ['last_name', 'first_name', 'address', 'sex', 'civil_status',
                             'farm_location', 'lot_1_variety', 'lot_1_planting_method', 'lot_1_tenurial_status'];
        for (final field in stringFields) {
          final value = decoded[field];
          if (value == null) {
            print('❌ Text field "$field" is null');
          } else if (value is! String) {
            print('❌ Text field "$field" is not a string: ${value.runtimeType}');
          } else if (value.toString().trim().isEmpty) {
            print('⚠️ Text field "$field" is empty');
          } else {
            print('✅ Text field "$field": "${value.toString().trim()}"');
          }
        }

        // Check JSON object fields (location and boundary objects)
        final objectFields = ['lot_1_location', 'lot_1_boundaries', 'lot_2_location', 'lot_2_boundaries',
                             'lot_3_location', 'lot_3_boundaries', 'lot_4_location', 'lot_4_boundaries'];
        for (final field in objectFields) {
          final value = decoded[field];
          if (value == null) {
            print('⚠️ Object field "$field" is null (may be optional)');
          } else if (value is Map) {
            print('✅ Object field "$field": JSON object with ${value.length} properties');
          } else {
            print('❌ Object field "$field" is not a JSON object: ${value.runtimeType}');
          }
        }

        // Check numeric fields at root level (CRITICAL for validation)
        final numericFields = ['lot_1_area', 'lot_2_area', 'lot_3_area', 'lot_4_area',
                              'area_insured', 'area_damaged', 'amount_of_cover', 'production_cost'];
        for (final field in numericFields) {
          final value = decoded[field];
          if (value == null) {
            print('⚠️ Numeric field "$field" is null (may be optional)');
          } else if (value is num) {
            print('✅ Numeric field "$field": $value (${value.runtimeType})');
          } else if (value is String && double.tryParse(value) != null) {
            print('⚠️ Numeric field "$field": string that can be parsed to number: "$value"');
          } else {
            print('❌ Numeric field "$field" is not a valid number: "$value" (${value.runtimeType})');
          }
        }

        // Check date fields at root level
        final dateFields = ['lot_1_date_planting', 'lot_1_date_harvest'];
        for (final field in dateFields) {
          final value = decoded[field];
          if (value == null) {
            print('❌ Date field "$field" is null');
          } else if (value.toString().trim().isEmpty) {
            print('❌ Date field "$field" is empty');
          } else {
            try {
              DateTime.parse(value.toString());
              print('✅ Date field "$field": "$value"');
            } catch (e) {
              print('❌ Date field "$field" has invalid format: "$value"');
            }
          }
        }

        // Check special fields
        print('🔍 Special fields:');
        print('  applicationTypeId: ${decoded['applicationTypeId']}');
        print('  coordinates: ${decoded['coordinates']}');

        // Count total non-null, non-empty fields
        final nonEmptyFields = decoded.entries.where((e) =>
          e.value != null && e.value.toString().trim().isNotEmpty
        ).length;
        print('📊 Total non-empty fields: $nonEmptyFields/${decoded.length}');

        // Verify we have the expected structure for @JsonAnySetter
        final fieldCount = decoded.length - 2; // Subtract applicationTypeId and coordinates
        print('📊 Application field count: $fieldCount (should match backend DTO expectations)');

        // Debug specific problematic fields from validation errors
        print('🔍 Checking fields that caused validation errors:');

        // Check area fields (must be NUMBER type)
        final areaFields = ['lot_1_area', 'lot_2_area', 'lot_3_area', 'lot_4_area'];
        for (final field in areaFields) {
          if (decoded.containsKey(field)) {
            final value = decoded[field];
            print('  $field: ${value.runtimeType} = $value ${value is num ? "✅" : "❌"}');
          } else {
            print('  $field: MISSING');
          }
        }

        // Check farm_location (must be TEXT type)
        if (decoded.containsKey('farm_location')) {
          final value = decoded['farm_location'];
          print('  farm_location: ${value.runtimeType} = "$value" ${value is String ? "✅" : "❌"}');
        } else {
          print('  farm_location: MISSING');
        }

        // Check other location fields (can be JSON objects)
        if (decoded.containsKey('lot_1_location')) {
          final loc = decoded['lot_1_location'];
          print('  lot_1_location: ${loc.runtimeType} = $loc ${loc is Map ? "✅" : "❌"}');
        }
        if (decoded.containsKey('lot_1_boundaries')) {
          final bounds = decoded['lot_1_boundaries'];
          print('  lot_1_boundaries: ${bounds.runtimeType} = $bounds ${bounds is Map ? "✅" : "❌"}');
        }
      } catch (e) {
        print('❌ JSON validation failed: $e');
        throw Exception('Invalid JSON structure: $e');
      }

      if (multipartFiles.isEmpty && files.isNotEmpty) {
        throw Exception('No valid files could be processed. Please check file types and content.');
      }

      // Debug FormData structure before sending
      print('🔍 FormData debug info:');
      print('  Fields count: ${formData.fields.length}');
      for (var field in formData.fields) {
        print('  Field: ${field.key} = ${field.value.length > 100 ? field.value.substring(0, 100) + '...' : field.value}');
      }
      print('  Files count: ${formData.files.length}');
      for (var file in formData.files) {
        print('  File: ${file.key} -> ${file.value.filename} (${file.value.contentType})');
      }

      // Verify FormData is properly constructed
      print('🔍 FormData type: ${formData.runtimeType}');
      print('🔍 Total parts in FormData: ${formData.files.length + formData.fields.length}');

      // Create a temporary Dio instance with verbose logging for this request
      final tempDio = Dio();
      tempDio.options = BaseOptions(
        baseUrl: _dio.options.baseUrl,
        connectTimeout: _dio.options.connectTimeout,
        receiveTimeout: _dio.options.receiveTimeout,
      );

      tempDio.interceptors.add(
        LogInterceptor(
          requestBody: false, // Don't log binary data
          responseBody: true,
          requestHeader: true,
          responseHeader: true,
          error: true,
          logPrint: (obj) => print('🔍 DETAILED: $obj'),
        ),
      );

      // Prepare headers without Content-Type to let Dio auto-detect
      final headers = <String, dynamic>{
        'Authorization': 'Bearer ${authState.token}',
        'Accept': 'application/json',
      };

      // Explicitly ensure no Content-Type is set
      print('🔍 Request headers before sending: $headers');

      final response = await tempDio.post(
        '/applications/submit',
        data: formData,
        options: Options(
          headers: headers,
          sendTimeout: const Duration(minutes: 5),
          receiveTimeout: const Duration(minutes: 5),
          // Explicitly set contentType to null to force multipart detection
          contentType: null,
        ),
      );

      print('✅ Application submitted successfully: \\${response.statusCode}');
      if (response.data is Map<String, dynamic> || response.data is Map) {
        return ApplicationSubmissionResponse.fromJson(response.data);
      } else if (response.data is String) {
        try {
          final Map<String, dynamic> json = jsonDecode(response.data);
          return ApplicationSubmissionResponse.fromJson(json);
        } catch (_) {
          return ApplicationSubmissionResponse(success: false, message: response.data.toString(), applicationId: '');
        }
      } else {
        return ApplicationSubmissionResponse(success: false, message: 'Unknown response format', applicationId: '');
      }
    } on DioException catch (e) {
      print('❌ Application submission failed: ${e.message}');
      print('❌ Response data: ${e.response?.data}');

      // Check if it's still the octet-stream error and try manual approach
      if (e.response?.data != null &&
          e.response!.data.toString().contains('application/octet-stream') &&
          e.response!.data.toString().contains('not supported')) {
        print('🔄 Detected Content-Type issue, trying manual multipart construction...');
        return await _submitApplicationManual(authState, request, files);
      }

      if (e.response?.data is Map<String, dynamic> || e.response?.data is Map) {
        return ApplicationSubmissionResponse.fromJson(e.response?.data);
      } else if (e.response?.data is String) {
        try {
          final Map<String, dynamic> json = jsonDecode(e.response?.data);
          return ApplicationSubmissionResponse.fromJson(json);
        } catch (_) {
          return ApplicationSubmissionResponse(success: false, message: (e.response?.data?.toString() ?? ''), applicationId: '');
        }
      }
      if (e.type == DioExceptionType.connectionTimeout ||
          e.type == DioExceptionType.receiveTimeout ||
          e.type == DioExceptionType.sendTimeout) {
        return ApplicationSubmissionResponse(success: false, message: 'Connection timeout. Please try again.', applicationId: '');
      } else if (e.type == DioExceptionType.connectionError) {
        return ApplicationSubmissionResponse(success: false, message: 'Cannot connect to server. Please check your connection.', applicationId: '');
      }
      return ApplicationSubmissionResponse(success: false, message: 'Failed to submit application', applicationId: '');
    } catch (e) {
      print('❌ Unexpected error: ${e.toString()}');
      // Show error for unsupported file type
      return ApplicationSubmissionResponse(success: false, message: e.toString(), applicationId: '');
    }
  }

  // Backup method for manual multipart construction if FormData fails
  Future<ApplicationSubmissionResponse> _submitApplicationManual(
    AuthState authState,
    ApplicationSubmissionRequest request,
    Map<String, PlatformFile> files,
  ) async {
    print('🔄 Attempting manual multipart construction...');

    // Create boundary manually
    final boundary = 'dart-dio-boundary-${DateTime.now().millisecondsSinceEpoch}';

    // Build multipart body manually
    final List<int> body = [];

    // Add submission part with same processing as main method
    final updatedFieldValues = Map<String, dynamic>.from(request.fieldValues);
    for (final fileKey in files.keys) {
      updatedFieldValues[fileKey] = "";
    }

    // Handle field type processing same as main method
    // Convert farm_location to TEXT
    if (updatedFieldValues['farm_location'] is Map<String, dynamic>) {
      final locationMap = updatedFieldValues['farm_location'] as Map<String, dynamic>;
      final locationString = [
        locationMap['barangay'],
        locationMap['city'],
        locationMap['province'],
        locationMap['region']
      ].where((part) => part != null && part.toString().trim().isNotEmpty)
       .join(', ');
      updatedFieldValues['farm_location'] = locationString;
    }

    // Ensure area fields are numbers
    final areaFields = ['lot_1_area', 'lot_2_area', 'lot_3_area', 'lot_4_area', 'area_insured', 'area_damaged'];
    for (final areaField in areaFields) {
      final value = updatedFieldValues[areaField];
      if (value != null) {
        if (value is num) {
          // Already a number
        } else if (value is String && value.isNotEmpty) {
          final parsedValue = double.tryParse(value);
          updatedFieldValues[areaField] = parsedValue ?? 0.0;
        } else {
          updatedFieldValues[areaField] = 0.0;
        }
      }
    }

    // Process field values same as main method
    final processedFieldValues = <String, dynamic>{};
    updatedFieldValues.forEach((key, value) {
      if (value == null) {
        if (key.contains('area') || key.contains('amount') || key.contains('cost')) {
          processedFieldValues[key] = 0.0;
        } else {
          processedFieldValues[key] = "";
        }
      } else if (value is String) {
        processedFieldValues[key] = value.trim();
      } else if (value is num || value is bool) {
        processedFieldValues[key] = value;
      } else if (value is Map<String, dynamic> || value is List) {
        processedFieldValues[key] = value;
      } else {
        processedFieldValues[key] = value.toString();
      }
    });

    // Create submission with fields at root level
    final submissionData = <String, dynamic>{
      'applicationTypeId': request.applicationTypeId,
      'coordinates': request.coordinates,
    };
    submissionData.addAll(processedFieldValues);

    final submissionJson = jsonEncode(submissionData);

    body.addAll('--$boundary\r\n'.codeUnits);
    body.addAll('Content-Disposition: form-data; name="submission"\r\n'.codeUnits);
    body.addAll('Content-Type: application/json\r\n\r\n'.codeUnits);
    body.addAll(submissionJson.codeUnits);
    body.addAll('\r\n'.codeUnits);

    // Add file parts
    for (final entry in files.entries) {
      final fieldName = entry.key;
      final file = entry.value;

      if (file.bytes == null) continue;

      // Use proper MIME type detection (same as main method)
      String? extension = file.extension?.toLowerCase();
      if (extension == null || extension.isEmpty) {
        final parts = file.name.split('.');
        if (parts.length > 1) {
          extension = parts.last.toLowerCase();
        }
      }

      const allowedMimeTypes = {
        'pdf': 'application/pdf',
        'jpg': 'image/jpeg',
        'jpeg': 'image/jpeg',
        'png': 'image/png',
        'gif': 'image/gif',
        'doc': 'application/msword',
        'docx': 'application/vnd.openxmlformats-officedocument.wordprocessingml.document',
      };

      String mimeType = allowedMimeTypes[extension] ?? lookupMimeType(file.name) ?? 'image/jpeg';

      // Never use application/octet-stream
      if (mimeType == 'application/octet-stream') {
        mimeType = 'image/jpeg'; // Default fallback
      }

      body.addAll('--$boundary\r\n'.codeUnits);
      body.addAll('Content-Disposition: form-data; name="$fieldName"; filename="${file.name}"\r\n'.codeUnits);
      body.addAll('Content-Type: $mimeType\r\n\r\n'.codeUnits);
      body.addAll(file.bytes!);
      body.addAll('\r\n'.codeUnits);
    }

    // End boundary
    body.addAll('--$boundary--\r\n'.codeUnits);

    try {
      final response = await _dio.post(
        '/applications/submit',
        data: Stream.fromIterable([body]),
        options: Options(
          headers: {
            'Authorization': 'Bearer ${authState.token}',
            'Content-Type': 'multipart/form-data; boundary=$boundary',
            'Accept': 'application/json',
          },
          sendTimeout: const Duration(minutes: 5),
          receiveTimeout: const Duration(minutes: 5),
        ),
      );

      return ApplicationSubmissionResponse.fromJson(response.data);
    } catch (e) {
      print('❌ Manual multipart submission failed: $e');
      return ApplicationSubmissionResponse(success: false, message: 'Manual submission failed: $e', applicationId: '');
    }
  }
}
