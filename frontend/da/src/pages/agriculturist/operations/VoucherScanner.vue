<script setup>
import { ref, onMounted, onBeforeUnmount, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Html5Qrcode } from 'html5-qrcode'
import { useVoucherStore } from '@/stores/voucher'
import { useAuthStore } from '@/stores/auth'
import { useNotificationStore } from '@/stores/notification'
import AuthenticatedLayout from '@/layouts/AuthenticatedLayout.vue'
import { MUNICIPAL_AGRICULTURIST_NAVIGATION } from '@/lib/navigation'
import LoadingSpinner from '@/components/others/LoadingSpinner.vue'
import {
  QrCodeIcon,
  CheckCircleIcon,
  XCircleIcon,
  ArrowLeftIcon,
  CameraIcon,
  DocumentArrowUpIcon,
  ExclamationTriangleIcon
} from '@heroicons/vue/24/outline'

const router = useRouter()
const voucherStore = useVoucherStore()
const authStore = useAuthStore()
const notificationStore = useNotificationStore()
const navigation = MUNICIPAL_AGRICULTURIST_NAVIGATION

// State
const html5QrCode = ref(null)
const isScanning = ref(false)
const scanResult = ref(null)
const errorMessage = ref(null)
const permissionDenied = ref(false)
const claimedVoucher = ref(null)
const scanMode = ref('camera') // 'camera' or 'file'
const fileInputRef = ref(null)
const processing = ref(false)

// Computed
const userId = computed(() => authStore.userId)

// Methods
const startScanner = async () => {
  try {
    errorMessage.value = null
    permissionDenied.value = false
    isScanning.value = true

    html5QrCode.value = new Html5Qrcode('qr-reader')

    const config = {
      fps: 10,
      qrbox: { width: 250, height: 250 },
      aspectRatio: 1.0
    }

    await html5QrCode.value.start(
      { facingMode: 'environment' }, // Use back camera on mobile
      config,
      onScanSuccess,
      onScanFailure
    )
  } catch (err) {
    console.error('Failed to start scanner:', err)
    if (err.name === 'NotAllowedError' || err.name === 'PermissionDeniedError') {
      permissionDenied.value = true
      errorMessage.value = 'Camera permission denied. Please allow camera access to scan QR codes.'
    } else {
      errorMessage.value = err.message || 'Failed to start camera. Please check if camera is available.'
    }
    isScanning.value = false
  }
}

const stopScanner = async () => {
  try {
    if (html5QrCode.value && isScanning.value) {
      await html5QrCode.value.stop()
      html5QrCode.value.clear()
      html5QrCode.value = null
    }
  } catch (err) {
    console.error('Failed to stop scanner:', err)
  } finally {
    isScanning.value = false
  }
}

const onScanSuccess = async (decodedText, decodedResult) => {
  console.log('QR Code scanned:', decodedText)

  // Stop scanning immediately
  await stopScanner()

  // Process the scanned code
  await processScan(decodedText)
}

const onScanFailure = (error) => {
  // Ignore continuous scan failures (normal when no QR code is in view)
  // Only log if it's not the common "NotFoundError"
  if (!error.includes('NotFoundException')) {
    console.warn('QR scan error:', error)
  }
}

const processScan = async (code) => {
  try {
    processing.value = true
    errorMessage.value = null

    if (!userId.value) {
      errorMessage.value = 'User not authenticated. Please login again.'
      notificationStore.showError('User not authenticated')
      return
    }

    // Claim the voucher
    const result = await voucherStore.claimVoucher(code, userId.value)

    if (result.success) {
      scanResult.value = 'success'
      claimedVoucher.value = result.data
      notificationStore.showSuccess('Voucher claimed successfully!')
    } else {
      scanResult.value = 'error'
      errorMessage.value = result.message
      notificationStore.showError(result.message)
    }
  } catch (err) {
    scanResult.value = 'error'
    errorMessage.value = err.message || 'Failed to process voucher'
    notificationStore.showError('Failed to process voucher')
  } finally {
    processing.value = false
  }
}

const resetScanner = () => {
  scanResult.value = null
  claimedVoucher.value = null
  errorMessage.value = null
  permissionDenied.value = false
  startScanner()
}

const handleFileUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  try {
    processing.value = true
    errorMessage.value = null

    const html5QrCodeFile = new Html5Qrcode('qr-reader-file')
    const decodedText = await html5QrCodeFile.scanFile(file, true)

    console.log('QR Code from file:', decodedText)
    await processScan(decodedText)
  } catch (err) {
    console.error('Failed to scan file:', err)
    errorMessage.value = 'Failed to read QR code from image. Please try another image.'
    notificationStore.showError('Failed to read QR code from image')
  } finally {
    processing.value = false
    // Reset file input
    if (fileInputRef.value) {
      fileInputRef.value.value = ''
    }
  }
}

const triggerFileUpload = () => {
  fileInputRef.value?.click()
}

const navigateBack = () => {
  router.push({ name: 'agriculturist-voucher-all' })
}

const viewVoucherDetails = () => {
  if (claimedVoucher.value) {
    router.push({ name: 'agriculturist-voucher-detail', params: { id: claimedVoucher.value.id } })
  }
}

// Lifecycle
onMounted(() => {
  if (scanMode.value === 'camera') {
    startScanner()
  }
})

onBeforeUnmount(() => {
  stopScanner()
})
</script>

<template>
  <AuthenticatedLayout
    :navigation="navigation"
    role-title="Municipal Agriculturist"
    page-title="Voucher Scanner"
  >
    <div class="h-full flex flex-col space-y-4 sm:space-y-6 p-2 sm:p-0">
      <!-- Header -->
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 ml-4">
        <div class="flex items-center gap-3 sm:gap-4">
          <div>
            <h1 class="text-xl sm:text-2xl md:text-3xl font-bold text-green-600">
              Scan Voucher QR Code
            </h1>
            <p class="mt-1 text-sm text-gray-600">
              Scan a voucher QR code to claim it
            </p>
          </div>
        </div>
      </div>

      <!-- Camera Permission Denied -->
      <div v-if="permissionDenied" class="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
        <div class="flex items-start gap-3">
          <ExclamationTriangleIcon class="w-6 h-6 text-yellow-500 flex-shrink-0" />
          <div class="flex-1">
            <h3 class="text-sm font-medium text-yellow-800">Camera Permission Required</h3>
            <p class="text-sm text-yellow-700 mt-1">
              {{ errorMessage }}
            </p>
            <div class="mt-3 flex flex-col sm:flex-row gap-2">
              <button
                @click="startScanner"
                class="inline-flex items-center justify-center gap-2 px-4 py-2 bg-yellow-600 text-white rounded-lg text-sm font-medium hover:bg-yellow-700 transition-colors"
              >
                Try Again
              </button>
              <button
                @click="triggerFileUpload"
                class="inline-flex items-center justify-center gap-2 px-4 py-2 bg-gray-600 text-white rounded-lg text-sm font-medium hover:bg-gray-700 transition-colors"
              >
                <DocumentArrowUpIcon class="w-5 h-5" />
                Upload QR Image Instead
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- Scanner Container -->
      <div v-if="!scanResult" class="bg-gray-100 rounded-xl border border-gray-300 overflow-hidden">
        <!-- Mode Selector -->
        <div class="flex border-b border-gray-300">
          <button
            @click="scanMode = 'camera'; startScanner()"
            class="flex-1 px-4 py-3 text-sm font-medium transition-colors"
            :class="scanMode === 'camera' ? 'bg-green-50 text-green-700 border-b-2 border-green-600' : 'text-gray-600 hover:bg-gray-50'"
          >
            <CameraIcon class="w-5 h-5 inline-block mr-2" />
            Camera
          </button>
          <button
            @click="scanMode = 'file'; stopScanner()"
            class="flex-1 px-4 py-3 text-sm font-medium transition-colors"
            :class="scanMode === 'file' ? 'bg-green-50 text-green-700 border-b-2 border-green-600' : 'text-gray-600 hover:bg-gray-50'"
          >
            <DocumentArrowUpIcon class="w-5 h-5 inline-block mr-2" />
            Upload Image
          </button>
        </div>

        <div class="p-4 sm:p-6">
          <!-- Camera Scanner -->
          <div v-if="scanMode === 'camera'" class="space-y-4">
            <div class="flex items-center justify-center gap-2 text-sm text-gray-600 mb-4">
              <QrCodeIcon class="w-5 h-5 text-green-600" />
              <span>Position the QR code within the frame</span>
            </div>

            <!-- QR Reader Container -->
            <div class="relative mx-auto max-w-md">
              <div id="qr-reader" class="w-full rounded-lg overflow-hidden border-2 border-gray-300"></div>

              <!-- Scanning Indicator -->
              <div v-if="isScanning && !processing" class="absolute top-2 right-2 bg-green-600 text-white px-3 py-1 rounded-full text-xs font-medium flex items-center gap-2">
                <span class="animate-pulse">●</span>
                Scanning...
              </div>
            </div>

            <!-- Processing Indicator -->
            <div v-if="processing" class="flex flex-col items-center justify-center gap-3 py-6">
              <div class="relative">
                <div
                  class="h-14 w-14 rounded-full border-4 border-gray-200"></div>
                <div
                  class="absolute top-0 left-0 h-14 w-14 rounded-full border-4 border-green-600 border-t-transparent animate-spin"></div>
              </div>
              <p class="text-gray-600 font-medium tracking-wide">Processing voucher…</p>
            </div>
          </div>

          <!-- File Upload -->
          <div v-else-if="scanMode === 'file'" class="space-y-4">
            <div class="flex flex-col items-center justify-center py-12 px-4">
              <QrCodeIcon class="w-16 h-16 text-green-600 mb-4" />
              <h3 class="text-lg font-medium text-gray-900 mb-2">Upload QR Code Image</h3>
              <p class="text-sm text-gray-500 text-center mb-6">
                Select an image file containing the voucher QR code
              </p>

              <input
                ref="fileInputRef"
                type="file"
                accept="image/*"
                @change="handleFileUpload"
                class="hidden"
              />

              <button
                @click="triggerFileUpload"
                :disabled="processing"
                class="inline-flex items-center justify-center gap-2 px-6 py-3 bg-green-600 text-white rounded-lg text-sm font-medium hover:bg-green-700 disabled:bg-gray-400 disabled:cursor-not-allowed transition-colors"
              >
                <DocumentArrowUpIcon class="w-5 h-5" />
                Choose Image
              </button>

              <!-- Processing Indicator -->
              <div v-if="processing" class="flex flex-col items-center justify-center gap-3 py-6 mt-4">
                <div class="relative">
                  <div
                    class="h-14 w-14 rounded-full border-4 border-gray-200"></div>
                  <div
                    class="absolute top-0 left-0 h-14 w-14 rounded-full border-4 border-green-600 border-t-transparent animate-spin"></div>
                </div>
                <p class="text-gray-600 font-medium tracking-wide">Processing image…</p>
              </div>

              <!-- Hidden reader for file scanning -->
              <div id="qr-reader-file" class="hidden"></div>
            </div>
          </div>

          <!-- Error Message -->
          <div v-if="errorMessage && !permissionDenied" class="mt-4 bg-red-50 border border-red-200 rounded-lg p-4">
            <div class="flex items-start gap-3">
              <XCircleIcon class="w-5 h-5 text-red-500 flex-shrink-0 mt-0.5" />
              <div class="flex-1">
                <h3 class="text-sm font-medium text-red-800">Error</h3>
                <p class="text-sm text-red-700 mt-1">{{ errorMessage }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Success Result -->
      <div v-if="scanResult === 'success' && claimedVoucher" class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 sm:p-8">
        <div class="flex flex-col items-center text-center space-y-4">
          <div class="w-16 h-16 sm:w-20 sm:h-20 bg-green-100 rounded-full flex items-center justify-center">
            <CheckCircleIcon class="w-10 h-10 sm:w-12 sm:h-12 text-green-600" />
          </div>

          <div>
            <h2 class="text-xl sm:text-2xl font-bold text-gray-900 mb-2">Voucher Claimed Successfully!</h2>
            <p class="text-sm sm:text-base text-gray-600">The voucher has been claimed and is now ready to use.</p>
          </div>

          <!-- Voucher Details -->
          <div class="w-full max-w-md bg-gray-50 rounded-lg p-4 sm:p-6 space-y-3 text-left">
            <div>
              <p class="text-xs sm:text-sm font-medium text-gray-500">Title</p>
              <p class="text-sm sm:text-base text-gray-900 font-semibold mt-1">{{ claimedVoucher.title }}</p>
            </div>

            <div class="grid grid-cols-2 gap-3">
              <div>
                <p class="text-xs sm:text-sm font-medium text-gray-500">Code</p>
                <p class="text-xs sm:text-sm text-gray-900 mt-1 font-mono break-all">{{ claimedVoucher.code }}</p>
              </div>

              <div>
                <p class="text-xs sm:text-sm font-medium text-gray-500">Type</p>
                <p class="text-xs sm:text-sm text-gray-900 mt-1">{{ claimedVoucher.voucherType }}</p>
              </div>
            </div>

            <div>
              <p class="text-xs sm:text-sm font-medium text-gray-500">Quantity</p>
              <p class="text-xs sm:text-sm text-gray-900 mt-1">{{ claimedVoucher.quantity }} {{ claimedVoucher.unit }}</p>
            </div>
          </div>

          <!-- Action Buttons -->
          <div class="flex flex-col sm:flex-row gap-3 w-full max-w-md">
            <button
              @click="viewVoucherDetails"
              class="flex-1 inline-flex items-center justify-center gap-2 px-4 sm:px-6 py-2.5 sm:py-3 bg-green-600 text-white rounded-lg text-sm font-medium hover:bg-green-700 transition-colors"
            >
              View Details
            </button>
            <button
              @click="resetScanner"
              class="flex-1 inline-flex items-center justify-center gap-2 px-4 sm:px-6 py-2.5 sm:py-3 bg-gray-600 text-white rounded-lg text-sm font-medium hover:bg-gray-700 transition-colors"
            >
              <QrCodeIcon class="w-5 h-5" />
              Scan Another
            </button>
          </div>
        </div>
      </div>

      <!-- Error Result -->
      <div v-if="scanResult === 'error'" class="bg-white rounded-xl shadow-sm border border-gray-200 p-6 sm:p-8">
        <div class="flex flex-col items-center text-center space-y-4">
          <div class="w-16 h-16 sm:w-20 sm:h-20 bg-red-100 rounded-full flex items-center justify-center">
            <XCircleIcon class="w-10 h-10 sm:w-12 sm:h-12 text-red-600" />
          </div>

          <div>
            <h2 class="text-xl sm:text-2xl font-bold text-gray-900 mb-2">Claim Failed</h2>
            <p class="text-sm sm:text-base text-gray-600">{{ errorMessage || 'Failed to claim the voucher' }}</p>
          </div>

          <!-- Action Buttons -->
          <div class="flex flex-col sm:flex-row gap-3 w-full max-w-md">
            <button
              @click="resetScanner"
              class="flex-1 inline-flex items-center justify-center gap-2 px-4 sm:px-6 py-2.5 sm:py-3 bg-green-600 text-white rounded-lg text-sm font-medium hover:bg-green-700 transition-colors"
            >
              <QrCodeIcon class="w-5 h-5" />
              Try Again
            </button>
            <button
              @click="navigateBack"
              class="flex-1 inline-flex items-center justify-center gap-2 px-4 sm:px-6 py-2.5 sm:py-3 bg-gray-600 text-white rounded-lg text-sm font-medium hover:bg-gray-700 transition-colors"
            >
              Back to List
            </button>
          </div>
        </div>
      </div>
    </div>
  </AuthenticatedLayout>
</template>

<style scoped>
/* Override html5-qrcode default styles for better mobile experience */
:deep(#qr-reader) {
  border: none !important;
}

:deep(#qr-reader__dashboard_section_swaplink) {
  display: none !important;
}

:deep(#qr-reader video) {
  border-radius: 0.5rem;
}

:deep(#qr-reader__scan_region) {
  border-radius: 0.5rem !important;
}

/* Responsive adjustments for QR scanner */
@media (max-width: 640px) {
  :deep(#qr-reader) {
    width: 100% !important;
  }
}

/* Custom scrollbar */
::-webkit-scrollbar {
  width: 8px;
  height: 8px;
}

::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb {
  background: #cbd5e0;
  border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
  background: #a0aec0;
}
</style>
