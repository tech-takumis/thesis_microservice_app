import 'package:get/get.dart';
import 'package:mobile/data/models/voucher_models.dart';
import 'package:mobile/data/services/voucher_api_service.dart';
import 'package:mobile/injection_container.dart';
import 'package:mobile/presentation/controllers/auth_controller.dart';

class VoucherController extends GetxController {
  var vouchers = <VoucherResponseDto>[].obs;
  var selectedVoucher = Rxn<VoucherResponseDto>();
  var isLoading = true.obs;
  var errorMessage = ''.obs;
  var isRetrying = false.obs;

  /// Fetch all vouchers
  Future<void> fetchAllVouchers(AuthState authState) async {
    if (!(authState.isLoggedIn && authState.token != null && authState.token!.isNotEmpty)) {
      print('User not logged in, skipping fetchAllVouchers');
      return;
    }

    try {
      isLoading.value = true;
      errorMessage.value = '';

      print('🔄 Starting to fetch all vouchers...');
      final response = await getIt<VoucherApiService>().getAllVouchers(authState);

      vouchers.value = response;
      print('✅ Successfully loaded ${response.length} vouchers');
    } catch (e, stack) {
      print('❌ Error fetching vouchers: $e');
      print('❌ Stack: $stack');
      errorMessage.value = e.toString().replaceFirst('Exception: ', '');
    } finally {
      isLoading.value = false;
    }
  }

  /// Fetch vouchers by owner user ID
  Future<void> fetchVouchersByOwner(String ownerUserId, AuthState authState) async {
    if (!(authState.isLoggedIn && authState.token != null && authState.token!.isNotEmpty)) {
      print('User not logged in, skipping fetchVouchersByOwner');
      return;
    }

    try {
      isLoading.value = true;
      errorMessage.value = '';

      print('🔄 Fetching vouchers by owner: $ownerUserId');
      final response = await getIt<VoucherApiService>().getVouchersByOwner(ownerUserId, authState);

      vouchers.value = response;
      print('✅ Successfully loaded ${response.length} vouchers for owner: $ownerUserId');
    } catch (e, stack) {
      print('❌ Error fetching vouchers by owner: $e');
      print('❌ Stack: $stack');
      errorMessage.value = e.toString().replaceFirst('Exception: ', '');
    } finally {
      isLoading.value = false;
    }
  }

  /// Fetch vouchers by status
  Future<void> fetchVouchersByStatus(VoucherStatus status, AuthState authState) async {
    if (!(authState.isLoggedIn && authState.token != null && authState.token!.isNotEmpty)) {
      print('User not logged in, skipping fetchVouchersByStatus');
      return;
    }

    try {
      isLoading.value = true;
      errorMessage.value = '';

      print('🔄 Fetching vouchers by status: ${status.name}');
      final response = await getIt<VoucherApiService>().getVouchersByStatus(status, authState);

      vouchers.value = response;
      print('✅ Successfully loaded ${response.length} vouchers with status: ${status.name}');
    } catch (e, stack) {
      print('❌ Error fetching vouchers by status: $e');
      print('❌ Stack: $stack');
      errorMessage.value = e.toString().replaceFirst('Exception: ', '');
    } finally {
      isLoading.value = false;
    }
  }

  /// Fetch voucher by code
  Future<void> fetchVoucherByCode(String code, AuthState authState) async {
    if (!(authState.isLoggedIn && authState.token != null && authState.token!.isNotEmpty)) {
      print('User not logged in, skipping fetchVoucherByCode');
      return;
    }

    try {
      isLoading.value = true;
      errorMessage.value = '';

      print('🔄 Fetching voucher by code: $code');
      final response = await getIt<VoucherApiService>().getVoucherByCode(code, authState);

      if (response != null) {
        selectedVoucher.value = response;
        print('✅ Successfully loaded voucher: ${response.code}');
      } else {
        print('⚠️ Received null response from API');
        errorMessage.value = 'Voucher not found with code: $code';
      }
    } catch (e, stack) {
      print('❌ Error fetching voucher by code: $e');
      print('❌ Stack: $stack');
      errorMessage.value = e.toString().replaceFirst('Exception: ', '');
    } finally {
      isLoading.value = false;
    }
  }

  /// Fetch voucher by ID
  Future<void> fetchVoucherById(String id, AuthState authState) async {
    if (!(authState.isLoggedIn && authState.token != null && authState.token!.isNotEmpty)) {
      print('User not logged in, skipping fetchVoucherById');
      return;
    }

    try {
      isLoading.value = true;
      errorMessage.value = '';

      print('🔄 Fetching voucher by ID: $id');
      final response = await getIt<VoucherApiService>().getVoucherById(id, authState);

      if (response != null) {
        selectedVoucher.value = response;
        print('✅ Successfully loaded voucher: ${response.id}');
      } else {
        print('⚠️ Received null response from API');
        errorMessage.value = 'Voucher not found';
      }
    } catch (e, stack) {
      print('❌ Error fetching voucher by ID: $e');
      print('❌ Stack: $stack');
      errorMessage.value = e.toString().replaceFirst('Exception: ', '');
    } finally {
      isLoading.value = false;
    }
  }

  /// Delete voucher by ID
  Future<bool> deleteVoucher(String id, AuthState authState) async {
    if (!(authState.isLoggedIn && authState.token != null && authState.token!.isNotEmpty)) {
      print('User not logged in, skipping deleteVoucher');
      return false;
    }

    try {
      isLoading.value = true;
      errorMessage.value = '';

      print('🔄 Deleting voucher: $id');
      final success = await getIt<VoucherApiService>().deleteVoucher(id, authState);

      if (success) {
        // Remove from local list if present
        vouchers.removeWhere((v) => v.id == id);

        // Clear selected if it was deleted
        if (selectedVoucher.value?.id == id) {
          selectedVoucher.value = null;
        }

        print('✅ Voucher deleted successfully');
        return true;
      } else {
        errorMessage.value = 'Failed to delete voucher';
        return false;
      }
    } catch (e, stack) {
      print('❌ Error deleting voucher: $e');
      print('❌ Stack: $stack');
      errorMessage.value = e.toString().replaceFirst('Exception: ', '');
      return false;
    } finally {
      isLoading.value = false;
    }
  }

  /// Retry fetching all vouchers
  Future<void> retryFetchVouchers(AuthState authState) async {
    if (!(authState.isLoggedIn && authState.token != null && authState.token!.isNotEmpty)) {
      print('User not logged in, skipping retryFetchVouchers');
      return;
    }

    try {
      isRetrying.value = true;
      errorMessage.value = '';

      final response = await getIt<VoucherApiService>().getAllVouchers(authState);

      vouchers.value = response;
      print('✅ Successfully loaded ${response.length} vouchers on retry');
    } catch (e) {
      print('❌ Error retrying voucher fetch: $e');
      errorMessage.value = e.toString().replaceFirst('Exception: ', '');
    } finally {
      isRetrying.value = false;
    }
  }

  /// Clear error message
  void clearError() {
    errorMessage.value = '';
  }

  /// Clear selected voucher
  void clearSelectedVoucher() {
    selectedVoucher.value = null;
  }

  /// Filter vouchers by status (client-side)
  List<VoucherResponseDto> getVouchersByStatusLocal(VoucherStatus status) {
    return vouchers.where((v) => v.status == status.name).toList();
  }

  /// Get active vouchers (client-side filter)
  List<VoucherResponseDto> getActiveVouchers() {
    return vouchers.where((v) => v.isActive).toList();
  }

  /// Get expired vouchers (client-side filter)
  List<VoucherResponseDto> getExpiredVouchers() {
    return vouchers.where((v) => v.isExpired).toList();
  }

  /// Get used vouchers (client-side filter)
  List<VoucherResponseDto> getUsedVouchers() {
    return vouchers.where((v) => v.isUsed).toList();
  }
}
