<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useVoucherStore } from '@/stores/voucher'
import { useFarmerStore } from '@/stores/farmer'
import { useNotificationStore } from '@/stores/notification'
import AuthenticatedLayout from '@/layouts/AuthenticatedLayout.vue'
import { MUNICIPAL_AGRICULTURIST_NAVIGATION } from '@/lib/navigation'
import LoadingSpinner from '@/components/others/LoadingSpinner.vue'
import { Sprout } from 'lucide-vue-next'
import {
  ChevronRightIcon,
  QrCodeIcon,
  CalendarIcon,
  HashtagIcon,
  TagIcon,
  CheckCircleIcon,
  XCircleIcon,
  ClockIcon,
  UserIcon,
  DocumentTextIcon,
  ScaleIcon,
  PhoneIcon,
  EnvelopeIcon,
} from '@heroicons/vue/24/outline'
import { ArrowDownTrayIcon, PrinterIcon } from '@heroicons/vue/24/solid'

const route = useRoute()
const router = useRouter()
const voucherStore = useVoucherStore()
const farmerStore = useFarmerStore()
const notificationStore = useNotificationStore()
const navigation = MUNICIPAL_AGRICULTURIST_NAVIGATION

// State
const loading = ref(false)
const error = ref(null)
const qrCodeDataURL = ref(null)
const showQRModal = ref(false)
const qrCanvas = ref(null)
const ownerFarmer = ref(null)

// Computed
const voucher = computed(() => voucherStore.currentVoucher)

const voucherId = computed(() => route.params.id)

const statusColor = computed(() => {
  const colors = {
    ISSUED: { bg: 'bg-yellow-100', text: 'text-yellow-800', icon: ClockIcon },
    CLAIMED: { bg: 'bg-green-100', text: 'text-green-800', icon: CheckCircleIcon },
    EXPIRED: { bg: 'bg-gray-100', text: 'text-gray-800', icon: XCircleIcon },
    CANCELLED: { bg: 'bg-red-100', text: 'text-red-800', icon: XCircleIcon }
  }
  return colors[voucher.value?.status] || { bg: 'bg-gray-100', text: 'text-gray-800', icon: ClockIcon }
})

const voucherTypeColor = computed(() => {
  const colors = {
        SEEDS: 'bg-green-100 text-green-800',
        FERTILIZER: 'bg-yellow-100 text-yellow-800',
        EQUIPMENT: 'bg-gray-200 text-green-600',
        CASH: 'bg-green-100 text-green-800',
        OTHER: 'bg-gray-100 text-gray-800'
  }
  return colors[voucher.value?.voucherType] || 'bg-gray-100 text-gray-800'
})

// Methods
const fetchVoucher = async () => {
  loading.value = true
  error.value = null
  try {
    const result = await voucherStore.getVoucherById(voucherId.value)
    if (!result.success) {
      error.value = result.message
      notificationStore.showError(result.message)
      return
    }

    // Fetch farmer/owner information
    if (result.data?.ownerUserId) {
      const farmerResult = await farmerStore.fetchFarmerById(result.data.ownerUserId)
      if (farmerResult.success) {
        ownerFarmer.value = farmerResult.data
      }
    }
  } catch (err) {
    error.value = 'Failed to load voucher details'
    notificationStore.showError('Failed to load voucher details')
  } finally {
    loading.value = false
  }
}

const navigateToVoucherList = () => {
  router.push({ name: 'agriculturist-voucher-all' })
}

const formatDate = (dateString) => {
  if (!dateString) return 'N/A'
  try {
    return new Date(dateString).toLocaleDateString('en-PH', {
      year: 'numeric',
      month: 'long',
      day: 'numeric'
    })
  } catch (error) {
    return 'Invalid Date'
  }
}

const formatDateTime = (dateString) => {
  if (!dateString) return 'N/A'
  try {
    return new Date(dateString).toLocaleString('en-PH', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (error) {
    return 'Invalid Date'
  }
}

const generateQRCode = async () => {
  if (!voucher.value?.code) {
    notificationStore.showError('Voucher code not available')
    return
  }

  try {
    const result = await voucherStore.generateQRCode(voucher.value.code, {
      width: 400,
      margin: 2
    })

    if (result.success) {
      qrCodeDataURL.value = result.data
      showQRModal.value = true
      notificationStore.showSuccess('QR code generated successfully!')
    } else {
      notificationStore.showError(result.message)
    }
  } catch (err) {
    notificationStore.showError('Failed to generate QR code')
  }
}

const generateQRCodeCanvas = async () => {
  if (!voucher.value?.code) {
    notificationStore.showError('Voucher code not available')
    return
  }

  if (!qrCanvas.value) {
    notificationStore.showError('Canvas element not available')
    return
  }

  try {
    const result = await voucherStore.generateQRCodeCanvas(qrCanvas.value, voucher.value.code, {
      width: 400,
      margin: 2
    })

    if (result.success) {
      showQRModal.value = true
      notificationStore.showSuccess('QR code generated on canvas!')
    } else {
      notificationStore.showError(result.message)
    }
  } catch (err) {
    notificationStore.showError('Failed to generate QR code on canvas')
  }
}

const downloadQRCode = () => {
  if (!qrCodeDataURL.value) {
    notificationStore.showError('QR code not generated yet')
    return
  }

  const link = document.createElement('a')
  link.href = qrCodeDataURL.value
  link.download = `voucher-${voucher.value.code}-qr.png`
  link.click()
  notificationStore.showSuccess('QR code downloaded!')
}

const printQRCode = () => {
  if (!qrCodeDataURL.value && !qrCanvas.value) {
    notificationStore.showError('QR code not generated yet')
    return
  }

  const printWindow = window.open('', '_blank')
  const imageUrl = qrCodeDataURL.value || qrCanvas.value.toDataURL()

  printWindow.document.write(`
    <html>
      <head>
        <title>Print Voucher QR Code</title>
        <style>
          body {
            display: flex;
            flex-direction: column;
            align-items: center;
            justify-content: center;
            min-height: 100vh;
            margin: 0;
            font-family: Arial, sans-serif;
          }
          .voucher-info {
            text-align: center;
            margin-bottom: 20px;
          }
          h1 {
            font-size: 24px;
            margin-bottom: 10px;
          }
          p {
            font-size: 14px;
            color: #666;
            margin: 5px 0;
          }
          img {
            max-width: 400px;
            height: auto;
          }
          @media print {
            body {
              padding: 20px;
            }
          }
        </style>
      </head>
      <body>
        <div class="voucher-info">
          <h1>${voucher.value.title}</h1>
          <p><strong>Code:</strong> ${voucher.value.code}</p>
          <p><strong>Type:</strong> ${voucher.value.voucherType}</p>
          <p><strong>Quantity:</strong> ${voucher.value.quantity} ${voucher.value.unit}</p>
          <p><strong>Reference:</strong> ${voucher.value.referenceNumber}</p>
        </div>
        <img src="${imageUrl}" alt="Voucher QR Code" />
        <script type="text/javascript">
          window.onload = function() {
            window.print();
            window.onafterprint = function() {
              window.close();
            }
          }
        <\/script>
      </body>
    </html>
  `)
  printWindow.document.close()
}

const closeQRModal = () => {
  showQRModal.value = false
}

// Lifecycle
onMounted(() => {
  fetchVoucher()
})
</script>

<template>
  <AuthenticatedLayout
    :navigation="navigation"
    role-title="Municipal Agriculturist"
    page-title="Voucher Details"
  >
    <div class="h-full flex flex-col space-y-6 ml-4">
      <!-- Breadcrumb Navigation -->
      <nav class="flex" aria-label="Breadcrumb">
        <ol class="flex items-center space-x-4">
          <li>
            <div>
              <button
                @click="navigateToVoucherList"
                class="text-gray-400 hover:text-gray-500 flex items-center gap-2"
              >
                <span class="text-sm font-medium">Voucher List</span>
              </button>
            </div>
          </li>
          <li>
            <div class="flex items-center">
              <ChevronRightIcon class="flex-shrink-0 h-5 w-5 text-gray-400" />
              <span class="ml-4 text-sm font-medium text-green-600">
                Voucher Details
              </span>
            </div>
          </li>
        </ol>
      </nav>

      <!-- Header -->
      <div class="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 ml-4">
        <div class="flex items-center gap-4">
          <div>
            <h1 class="text-2xl sm:text-3xl font-bold text-green-600">Voucher Details</h1>
            <p class="text-xs sm:text-sm text-gray-500 mt-1">View voucher information and generate QR code</p>
          </div>
        </div>

        <!-- QR Code Action Buttons - Only show when status is ISSUED -->
        <div v-if="voucher && !loading && voucher.status === 'ISSUED'" class="flex flex-col sm:flex-row gap-2 sm:gap-3 w-full sm:w-auto">
          <button
            @click="generateQRCode"
            class="inline-flex items-center justify-center gap-2 px-4 py-2.5 bg-green-600 text-white rounded-lg text-sm font-medium hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-green-500 transition-colors"
          >
            <QrCodeIcon class="w-5 h-5" />
            <span>Generate QR Code</span>
          </button>
          <button
            @click="generateQRCodeCanvas"
            class="inline-flex items-center justify-center gap-2 px-4 py-2.5 bg-blue-600 text-white rounded-lg text-sm font-medium hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors"
          >
            <QrCodeIcon class="w-5 h-5" />
            <span>Generate on Canvas</span>
          </button>
        </div>
      </div>

      <!-- Loading State -->
      <div
        v-if="loading"
        class="flex flex-col items-center justify-center flex-1 space-y-4 min-h-[60vh]"
      >
        <!-- Spinner -->
        <div class="relative">
          <div
            class="h-14 w-14 rounded-full border-4 border-gray-200"></div>
          <div
            class="absolute top-0 left-0 h-14 w-14 rounded-full border-4 border-green-600 border-t-transparent animate-spin"></div>
        </div>

        <!-- Loading Label -->
        <p class="text-gray-600 font-medium tracking-wide">
          Loading data…
        </p>
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-lg p-4">
        <div class="flex items-start gap-3">
          <XCircleIcon class="w-5 h-5 text-red-500 flex-shrink-0 mt-0.5" />
          <div>
            <h3 class="text-sm font-medium text-red-800">Error Loading Voucher</h3>
            <p class="text-sm text-red-700 mt-1">{{ error }}</p>
          </div>
        </div>
      </div>

      <!-- Voucher Details -->
      <div v-else-if="voucher" class="grid grid-cols-1 lg:grid-cols-3 gap-6">
<!-- Redesigned Voucher Information Card -->
<div class="lg:col-span-2 bg-gray-100 rounded-xl  border border-gray-300 p-8 space-y-4">
  <!-- Header Section -->
  <div class="flex items-center justify-between pb-4 border-b border-gray-100">
    <h2 class="text-xl font-semibold text-green-600 mb-6 flex items-center gap-2">
      <span class="w-2 h-2 bg-green-500 rounded-full"></span>
      Voucher Information
    </h2>

    <span
      class="inline-flex items-center gap-2 px-3 py-1.5 text-sm font-semibold rounded-full shadow-sm"
      :class="[statusColor.bg, statusColor.text]"
    >
      <component :is="statusColor.icon" class="w-4 h-4" />
      {{ voucher.status }}
    </span>
  </div>

  <!-- Content Section -->
  <div class="space-y-6">
    <!-- Title -->
    <div class="flex items-start gap-4">
      <DocumentTextIcon class="w-6 h-6 text-green-600 flex-shrink-0 mt-1" />
      <div class="flex-1">
        <p class="text-sm font-medium text-gray-500">Title</p>
        <p class="text-lg text-gray-900 font-semibold mt-1 leading-snug break-words">
          {{ voucher.title }}
        </p>
      </div>
    </div>

    <!-- Type & Quantity -->
    <div class="grid grid-cols-1 sm:grid-cols-2 gap-6">
      <div class="bg-gray-50 rounded-xl p-4 flex items-start gap-3 shadow-sm border border-gray-200">
        <TagIcon class="w-5 h-5 text-green-500" />
        <div>
          <p class="text-sm font-medium text-gray-500">Voucher Type</p>
          <span class="inline-flex px-2.5 py-1 text-xs font-semibold rounded-full mt-1" :class="voucherTypeColor">
            {{ voucher.voucherType }}
          </span>
        </div>
      </div>

      <div class="bg-gray-50 rounded-xl p-4 flex items-start gap-3 shadow-sm border border-gray-200">
        <ScaleIcon class="w-5 h-5 text-green-500" />
        <div>
          <p class="text-sm font-medium text-gray-500">Quantity</p>
          <p class="text-lg font-semibold text-gray-800 mt-1">
            {{ voucher.quantity }} {{ voucher.unit }}
          </p>
        </div>
      </div>
    </div>

    <!-- Code & Reference -->
    <div class="grid grid-cols-1 sm:grid-cols-2 gap-6">
      <div class="bg-gray-50 rounded-xl p-4 flex items-start gap-3 shadow-sm border border-gray-200">
        <QrCodeIcon class="w-5 h-5 text-green-500" />
        <div class="flex-1 min-w-0">
          <p class="text-sm font-medium text-gray-500">Voucher Code</p>
          <p class="text-sm text-gray-900 mt-1 font-mono break-all">{{ voucher.code }}</p>
        </div>
      </div>

      <div class="bg-gray-50 rounded-xl p-4 flex items-start gap-3 shadow-sm border border-gray-200">
        <HashtagIcon class="w-5 h-5 text-green-500" />
        <div>
          <p class="text-sm font-medium text-gray-500">Reference Number</p>
          <p class="text-sm text-gray-900 mt-1 font-mono">{{ voucher.referenceNumber }}</p>
        </div>
      </div>
    </div>

    <!-- Dates -->
    <div class="grid grid-cols-1 sm:grid-cols-2 gap-6">
      <div class="bg-gray-50 rounded-xl p-4 flex items-start gap-3 shadow-sm border border-gray-200">
        <CalendarIcon class="w-5 h-5 text-green-500" />
        <div>
          <p class="text-sm font-medium text-gray-500">Issue Date</p>
          <p class="text-sm text-gray-900 mt-1">{{ formatDate(voucher.issueDate) }}</p>
        </div>
      </div>

      <div class="bg-gray-50 rounded-xl p-4 flex items-start gap-3 shadow-sm border border-gray-200">
        <CalendarIcon class="w-5 h-5 text-green-500" />
        <div>
          <p class="text-sm font-medium text-gray-500">Expiry Date</p>
          <p class="text-sm text-gray-900 mt-1">{{ formatDate(voucher.expiryDate) }}</p>
        </div>
      </div>
    </div>

    <!-- Claimed Info -->
    <div v-if="voucher.claimedAt" class="grid grid-cols-1 sm:grid-cols-2 gap-6">
      <div class="bg-gray-50 rounded-xl p-4 flex items-start gap-3 shadow-sm border border-gray-200">
        <CalendarIcon class="w-5 h-5 text-green-500" />
        <div>
          <p class="text-sm font-medium text-gray-500">Claimed At</p>
          <p class="text-sm text-gray-900 mt-1">{{ formatDateTime(voucher.claimedAt) }}</p>
        </div>
      </div>

      <div v-if="voucher.claimedByUserId" class="bg-gray-50 rounded-xl p-4 flex items-start gap-3 shadow-sm border border-gray-200">
        <UserIcon class="w-5 h-5 text-green-500" />
        <div>
          <p class="text-sm font-medium text-gray-500">Claimed By User ID</p>
          <p class="text-xs text-gray-900 mt-1 font-mono break-all">{{ voucher.claimedByUserId }}</p>
        </div>
      </div>
    </div>
  </div>
</div>

<!-- Farmer Information Card -->
<div class="bg-gray-100 rounded-xl border border-gray-300 p-6">
  <h2 class="text-xl font-semibold text-green-600 mb-6 flex items-center gap-2">
    <span class="w-2 h-2 bg-green-500 rounded-full"></span>
    Farmer Information
  </h2>

  <div v-if="ownerFarmer" class="space-y-6">
    
    <!-- Full Name -->
    <div class="flex items-start gap-4 pb-5 border-b border-gray-100">
      <UserIcon class="w-6 h-6 text-gray-400 flex-shrink-0" />
      <div class="flex-1 min-w-0">
        <p class="text-sm font-medium text-gray-500">Full Name</p>
        <p class="text-lg font-semibold text-gray-900 mt-1">
          {{ ownerFarmer.firstName }} {{ ownerFarmer.lastName }}
        </p>
      </div>
    </div>

    <!-- RSBSA Number -->
    <div class="flex items-start gap-4 pb-5 border-b border-gray-100">
      <HashtagIcon class="w-6 h-6 text-gray-400 flex-shrink-0" />
      <div class="flex-1 min-w-0">
        <p class="text-sm font-medium text-gray-500">RSBSA Number</p>
        <p class="text-base text-gray-900 mt-1 font-mono bg-gray-100 px-2 py-1 rounded-md inline-block">
          {{ ownerFarmer.username }}
        </p>
      </div>
    </div>

    <!-- Email -->
    <div class="flex items-start gap-4 pb-5 border-b border-gray-100">
      <EnvelopeIcon class="w-6 h-6 text-gray-400 flex-shrink-0" />
      <div class="flex-1 min-w-0">
        <p class="text-sm font-medium text-gray-500">Email</p>
        <p class="text-base text-gray-900 mt-1 break-all">
          {{ ownerFarmer.email }}
        </p>
      </div>
    </div>

    <!-- Phone Number -->
    <div class="flex items-start gap-4">
      <PhoneIcon class="w-6 h-6 text-gray-400 flex-shrink-0" />
      <div class="flex-1 min-w-0">
        <p class="text-sm font-medium text-gray-500">Phone Number</p>
        <p class="text-base text-gray-900 mt-1">
          {{ ownerFarmer.phoneNumber }}
        </p>
      </div>
    </div>
  </div>

  <!-- Loading -->
  <div v-else-if="loading" class="flex justify-center items-center py-10">
    <LoadingSpinner />
  </div>

  <!-- No Data -->
  <div v-else class="text-center py-10">
    <UserIcon class="w-12 h-12 text-gray-300 mx-auto mb-3" />
    <p class="text-sm text-gray-500">No farmer information available</p>
  </div>

  <!-- Hidden QR Canvas -->
  <div class="mt-6 pt-6 border-t border-gray-100">
    <canvas ref="qrCanvas" class="hidden"></canvas>
  </div>
</div>

      </div>
    </div>

<!-- QR Code Modal -->
<Teleport to="body">
  <div
    v-if="showQRModal"
    class="fixed inset-0 bg-gray-300 bg-opacity-20 backdrop-blur-sm transition-opacity z-50"
    @click="closeQRModal"
  >
    <!-- Drawer Panel -->
    <div
      class="fixed inset-y-0 right-0 w-full max-w-xl bg-white border border-gray-300 transform transition-all duration-300"
      :class="showQRModal ? 'translate-x-0' : 'translate-x-full'"
      @click.stop
    >
      <!-- Modal Header -->
      <div class="flex items-center justify-between px-6 py-4 border-b border-gray-200">
        <h3 class="text-lg font-semibold text-green-600">Voucher QR Code</h3>
        <button
          @click="closeQRModal"
          class="text-gray-400 hover:text-gray-500 focus:outline-none"
        >
          <XCircleIcon class="w-6 h-6" />
        </button>
      </div>

      <!-- Modal Body -->
      <div class="px-6 py-8 overflow-y-auto h-[calc(100vh-140px)]">
        <div class="flex flex-row items-center justify-center gap-8 w-full">

          <!-- QR Code Image -->
          <div
            v-if="qrCodeDataURL"
            class="bg-white p-4 rounded-lg border-2 border-gray-300 flex-shrink-0 flex items-center justify-center"
            style="min-width: 220px; max-width: 280px;"
          >
            <img :src="qrCodeDataURL" alt="Voucher QR Code" class="w-full max-w-xs mx-auto" />
          </div>

          <!-- Canvas QR Fallback -->
          <div
            v-else-if="qrCanvas"
            class="bg-white p-4 rounded-lg border-2 border-gray-200 flex-shrink-0 flex items-center justify-center"
            style="min-width: 220px; max-width: 280px;"
          >
            <canvas ref="qrCanvas"></canvas>
          </div>

          <!-- Voucher Info -->
          <div class="text-left space-y-2 flex-1 min-w-0">
            <p class="text-lg font-semibold text-gray-900 flex items-center gap-2">
              <Sprout class="w-5 h-5 text-green-600" />
              {{ voucher.title }}
            </p>

            <p class="text-sm text-gray-500">
              <strong>Code:</strong>
              <span class="font-mono text-gray-700">{{ voucher.code }}</span>
            </p>

            <p class="text-sm text-gray-500">
              <strong>Reference:</strong>
              <span class="font-mono text-gray-700">{{ voucher.referenceNumber }}</span>
            </p>

            <p class="text-sm text-gray-500">
              <strong>Type:</strong> {{ voucher.voucherType }}
            </p>

            <p class="text-sm text-gray-500">
              <strong>Quantity:</strong> {{ voucher.quantity }} {{ voucher.unit }}
            </p>
          </div>

        </div>
      </div>

      <!-- Modal Footer -->
      <div class="flex justify-end gap-3 px-6 py-4 bg-white border-t border-gray-300">
        <button
          @click="printQRCode"
          class="inline-flex items-center gap-2 px-4 py-2 bg-white text-gray-800 rounded-md text-sm font-medium hover:bg-gray-200"
        >
          <PrinterIcon class="w-4 h-4" />
          Print
        </button>
        <button
          @click="downloadQRCode"
          class="inline-flex items-center gap-2 px-4 py-2 bg-green-600 text-white rounded-md text-sm font-medium hover:bg-green-700"
        >
          <ArrowDownTrayIcon class="w-4 h-4" />
          Download
        </button>
      </div>
    </div>
  </div>
</Teleport>

  </AuthenticatedLayout>
</template>

<style scoped>
/* Custom scrollbar for better UX */
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
