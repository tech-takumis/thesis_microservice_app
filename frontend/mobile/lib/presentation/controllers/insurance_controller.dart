import 'package:get/get.dart';
import 'package:mobile/data/models/insurance_models.dart';
import 'package:mobile/data/services/insurance_api_service.dart';
import 'package:mobile/injection_container.dart';
import 'package:mobile/presentation/controllers/auth_controller.dart';

class InsuranceController extends GetxController {
  var insurances = <InsuranceResponse>[].obs;
  var selectedInsurance = Rxn<InsuranceResponse>();
  var isLoading = true.obs;
  var errorMessage = ''.obs;
  var isRetrying = false.obs;

  /// Fetch all insurance records
  Future<void> fetchAllInsurance(AuthState authState) async {
    if (!(authState.isLoggedIn && authState.token != null && authState.token!.isNotEmpty)) {
      print('User not logged in, skipping fetchAllInsurance');
      return;
    }

    try {
      isLoading.value = true;
      errorMessage.value = '';

      print('🔄 Starting to fetch all insurance records...');
      final response = await getIt<InsuranceApiService>().getAllInsurance(authState);

      insurances.value = response;
      print('✅ Successfully loaded ${response.length} insurance records');
    } catch (e, stack) {
      print('❌ Error fetching insurance records: $e');
      print('❌ Stack: $stack');
      errorMessage.value = e.toString().replaceFirst('Exception: ', '');
    } finally {
      isLoading.value = false;
    }
  }

  /// Fetch insurance records for current user
  Future<void> fetchUserInsurance(AuthState authState) async {
    if (!(authState.isLoggedIn && authState.token != null && authState.token!.isNotEmpty)) {
      print('User not logged in, skipping fetchUserInsurance');
      return;
    }

    try {
      isLoading.value = true;
      errorMessage.value = '';

      print('🔄 Starting to fetch user insurance records...');
      final response = await getIt<InsuranceApiService>().getInsuranceByUser(authState);

      insurances.value = response;
      print('✅ Successfully loaded ${response.length} insurance records for user');
    } catch (e, stack) {
      print('❌ Error fetching user insurance records: $e');
      print('❌ Stack: $stack');
      errorMessage.value = e.toString().replaceFirst('Exception: ', '');
    } finally {
      isLoading.value = false;
    }
  }

  /// Fetch insurance by ID
  Future<void> fetchInsuranceById(String insuranceId, AuthState authState) async {
    if (!(authState.isLoggedIn && authState.token != null && authState.token!.isNotEmpty)) {
      print('User not logged in, skipping fetchInsuranceById');
      return;
    }

    try {
      isLoading.value = true;
      errorMessage.value = '';

      print('🔄 Fetching insurance by ID: $insuranceId');
      final response = await getIt<InsuranceApiService>().getInsuranceById(insuranceId, authState);

      if (response != null) {
        selectedInsurance.value = response;
        print('✅ Successfully loaded insurance: ${response.insuranceId}');
      } else {
        print('⚠️ Received null response from API');
        errorMessage.value = 'Insurance not found';
      }
    } catch (e, stack) {
      print('❌ Error fetching insurance by ID: $e');
      print('❌ Stack: $stack');
      errorMessage.value = e.toString().replaceFirst('Exception: ', '');
    } finally {
      isLoading.value = false;
    }
  }

  /// Fetch insurance by application ID
  Future<void> fetchInsuranceByApplicationId(String applicationId, AuthState authState) async {
    if (!(authState.isLoggedIn && authState.token != null && authState.token!.isNotEmpty)) {
      print('User not logged in, skipping fetchInsuranceByApplicationId');
      return;
    }

    try {
      isLoading.value = true;
      errorMessage.value = '';

      print('🔄 Fetching insurance by application ID: $applicationId');
      final response = await getIt<InsuranceApiService>().getInsuranceByApplicationId(applicationId, authState);

      if (response != null) {
        selectedInsurance.value = response;
        print('✅ Successfully loaded insurance for application: $applicationId');
      } else {
        print('⚠️ Received null response from API');
        errorMessage.value = 'Insurance not found for this application';
      }
    } catch (e, stack) {
      print('❌ Error fetching insurance by application ID: $e');
      print('❌ Stack: $stack');
      errorMessage.value = e.toString().replaceFirst('Exception: ', '');
    } finally {
      isLoading.value = false;
    }
  }

  /// Retry fetching all insurance records
  Future<void> retryFetchInsurance(AuthState authState) async {
    if (!(authState.isLoggedIn && authState.token != null && authState.token!.isNotEmpty)) {
      print('User not logged in, skipping retryFetchInsurance');
      return;
    }

    try {
      isRetrying.value = true;
      errorMessage.value = '';

      final response = await getIt<InsuranceApiService>().getAllInsurance(authState);

      insurances.value = response;
      print('✅ Successfully loaded ${response.length} insurance records on retry');
    } catch (e) {
      print('❌ Error retrying insurance fetch: $e');
      errorMessage.value = e.toString().replaceFirst('Exception: ', '');
    } finally {
      isRetrying.value = false;
    }
  }

  /// Clear error message
  void clearError() {
    errorMessage.value = '';
  }

  /// Clear selected insurance
  void clearSelectedInsurance() {
    selectedInsurance.value = null;
  }
}
