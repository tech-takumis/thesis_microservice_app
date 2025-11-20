<template>
  <AuthenticatedLayout
    :navigation="navigation"
    role-title="Municipal Agriculturist"
    page-title="Claim Detail">
    <div class="h-full flex flex-col min-h-0 overflow-hidden">
      <!-- Fixed Header Section -->
      <div class="flex-shrink-0 mb-4">
        <!-- Header with breadcrumb -->
        <nav class="flex mb-4" aria-label="Breadcrumb">
          <ol class="flex items-center space-x-4">
            <li>
              <div>
                <router-link
                  :to="{ name: 'agriculturist-dashboard' }"
                  class="text-gray-400 hover:text-gray-500"
                >
                  <HomeIcon class="flex-shrink-0 h-5 w-5" />
                  <span class="sr-only">Dashboard</span>
                </router-link>
              </div>
            </li>
            <li>
              <div class="flex items-center">
                <ChevronRightIcon class="flex-shrink-0 h-5 w-5 text-gray-400" />
                <router-link
                  :to="{ name: 'agriculturist-process-claims' }"
                  class="ml-4 text-sm font-medium text-gray-500 hover:text-gray-700"
                >
                  Process Claims
                </router-link>
              </div>
            </li>
            <li>
              <div class="flex items-center">
                <ChevronRightIcon class="flex-shrink-0 h-5 w-5 text-gray-400" />
                <span class="ml-4 text-sm font-medium text-black">
                  Claim Details
                </span>
              </div>
            </li>
          </ol>
        </nav>

        <div class="flex items-center justify-between ml-5">
          <div>
            <h1 class="text-3xl font-bold text-green-600">Claim Details</h1>
            <p class="mt-1 text-sm text-gray-600">
              {{ claim ? claim.farmerName : 'Loading...' }}
            </p>
          </div>
        </div>
      </div>

      <!-- Main Content Area -->
      <div class="flex-1 min-h-0 overflow-hidden">
        <div class="h-full">
          <!-- Loading State -->
          <div v-if="loading" class="flex justify-center items-center flex-1 h-full">
            <LoadingSpinner />
          </div>

          <!-- Error State -->
          <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-md p-4 mb-4">
            <div class="flex">
              <div class="flex-shrink-0">
                <ExclamationTriangleIcon class="h-5 w-5 text-red-400" />
              </div>
              <div class="ml-3">
                <h3 class="text-sm font-medium text-red-800">Error Loading Claim Details</h3>
                <div class="mt-2 text-sm text-red-700">{{ error }}</div>
              </div>
            </div>
          </div>

          <!-- Claim Details -->
          <div v-else-if="claim" class="bg-gray-50 rounded-xl shadow-sm p-3 h-full overflow-auto">
            <div class="space-y-4">
              <!-- Claim Overview Card -->
              <div class="bg-gray-100 rounded-xl border border-gray-300 shadow-sm">
                <div class="px-4 py-4">
                  <h2 class="text-lg font-bold text-gray-900 mb-4">Claim Overview</h2>

                  <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-x-6 gap-y-3">
                    <!-- Claim ID -->
                    <div class="flex flex-col">
                      <span class="text-[10px] font-semibold text-green-600 uppercase tracking-wider mb-1">
                        Claim ID
                      </span>
                      <div class="w-full bg-white rounded-md px-3 py-2.5 border border-gray-200 shadow-sm hover:shadow transition-all duration-150">
                        <span class="text-sm text-gray-900 font-medium break-words font-mono">
                          {{ claim.id }}
                        </span>
                      </div>
                    </div>

                    <!-- Farmer Name -->
                    <div class="flex flex-col">
                      <span class="text-[10px] font-semibold text-green-600 uppercase tracking-wider mb-1">
                        Farmer Name
                      </span>
                      <div class="w-full bg-white rounded-md px-3 py-2.5 border border-gray-200 shadow-sm hover:shadow transition-all duration-150">
                        <span class="text-sm text-gray-900 font-medium break-words">
                          {{ claim.farmerName }}
                        </span>
                      </div>
                    </div>

                    <!-- Claim Amount -->
                    <div class="flex flex-col">
                      <span class="text-[10px] font-semibold text-green-600 uppercase tracking-wider mb-1">
                        Claim Amount
                      </span>
                      <div class="w-full bg-white rounded-md px-3 py-2.5 border border-gray-200 shadow-sm hover:shadow transition-all duration-150">
                        <span class="text-sm font-bold text-green-600">
                          {{ formatCurrency(claim.claimAmount) }}
                        </span>
                      </div>
                    </div>

                    <!-- Filed At -->
                    <div class="flex flex-col">
                      <span class="text-[10px] font-semibold text-green-600 uppercase tracking-wider mb-1">
                        Filed At
                      </span>
                      <div class="w-full bg-white rounded-md px-3 py-2.5 border border-gray-200 shadow-sm hover:shadow transition-all duration-150">
                        <span class="text-sm text-gray-900 font-medium break-words">
                          {{ formatDate(claim.filedAt) }}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Damage Assessment Card -->
              <div class="bg-gray-100 rounded-xl border border-gray-300 shadow-sm">
                <div class="px-4 py-4">
                  <h2 class="text-lg font-bold text-gray-900 mb-4">Damage Assessment</h2>

                  <div class="w-full bg-white rounded-md px-3 py-2.5 border border-gray-200 shadow-sm">
                    <p class="text-sm text-gray-900">{{ claim.damageAssessment }}</p>
                  </div>
                </div>
              </div>

              <!-- Farm Information Card -->
              <div v-if="claim.fieldValues" class="bg-gray-100 rounded-xl border border-gray-300 shadow-sm">
                <div class="px-4 py-4">
                  <h2 class="text-lg font-bold text-gray-900 mb-4">Farm Information</h2>

                  <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-x-6 gap-y-3">
                    <!-- CIC Number -->
                    <div v-if="claim.fieldValues.cic_no" class="flex flex-col">
                      <span class="text-[10px] font-semibold text-green-600 uppercase tracking-wider mb-1">
                        CIC Number
                      </span>
                      <div class="w-full bg-white rounded-md px-3 py-2.5 border border-gray-200 shadow-sm hover:shadow transition-all duration-150">
                        <span class="text-sm text-gray-900 font-medium break-words">
                          {{ claim.fieldValues.cic_no }}
                        </span>
                      </div>
                    </div>

                    <!-- Farm Location -->
                    <div v-if="claim.fieldValues.farm_location" class="flex flex-col md:col-span-2">
                      <span class="text-[10px] font-semibold text-green-600 uppercase tracking-wider mb-1">
                        Farm Location
                      </span>
                      <div class="w-full bg-white rounded-md px-3 py-2.5 border border-gray-200 shadow-sm hover:shadow transition-all duration-150">
                        <span class="text-sm text-gray-900 font-medium break-words">
                          {{ claim.fieldValues.farm_location }}
                        </span>
                      </div>
                    </div>

                    <!-- Address -->
                    <div v-if="claim.fieldValues.address" class="flex flex-col md:col-span-2">
                      <span class="text-[10px] font-semibold text-green-600 uppercase tracking-wider mb-1">
                        Address
                      </span>
                      <div class="w-full bg-white rounded-md px-3 py-2.5 border border-gray-200 shadow-sm hover:shadow transition-all duration-150">
                        <span class="text-sm text-gray-900 font-medium break-words">
                          {{ claim.fieldValues.address }}
                        </span>
                      </div>
                    </div>

                    <!-- Phone Number -->
                    <div v-if="claim.fieldValues.cell_phone_number" class="flex flex-col">
                      <span class="text-[10px] font-semibold text-green-600 uppercase tracking-wider mb-1">
                        Phone Number
                      </span>
                      <div class="w-full bg-white rounded-md px-3 py-2.5 border border-gray-200 shadow-sm hover:shadow transition-all duration-150">
                        <span class="text-sm text-gray-900 font-medium break-words">
                          {{ claim.fieldValues.cell_phone_number }}
                        </span>
                      </div>
                    </div>

                    <!-- Insured Crops -->
                    <div v-if="claim.fieldValues.insured_crops" class="flex flex-col">
                      <span class="text-[10px] font-semibold text-green-600 uppercase tracking-wider mb-1">
                        Insured Crops
                      </span>
                      <div class="w-full bg-white rounded-md px-3 py-2.5 border border-gray-200 shadow-sm hover:shadow transition-all duration-150">
                        <span class="text-sm text-gray-900 font-medium break-words">
                          {{ claim.fieldValues.insured_crops }}
                        </span>
                      </div>
                    </div>

                    <!-- Variety Planted -->
                    <div v-if="claim.fieldValues.variety_planted" class="flex flex-col">
                      <span class="text-[10px] font-semibold text-green-600 uppercase tracking-wider mb-1">
                        Variety Planted
                      </span>
                      <div class="w-full bg-white rounded-md px-3 py-2.5 border border-gray-200 shadow-sm hover:shadow transition-all duration-150">
                        <span class="text-sm text-gray-900 font-medium break-words">
                          {{ claim.fieldValues.variety_planted }}
                        </span>
                      </div>
                    </div>

                    <!-- Planting Method -->
                    <div v-if="claim.fieldValues.planting_method" class="flex flex-col">
                      <span class="text-[10px] font-semibold text-green-600 uppercase tracking-wider mb-1">
                        Planting Method
                      </span>
                      <div class="w-full bg-white rounded-md px-3 py-2.5 border border-gray-200 shadow-sm hover:shadow transition-all duration-150">
                        <span class="text-sm text-gray-900 font-medium break-words">
                          {{ claim.fieldValues.planting_method }}
                        </span>
                      </div>
                    </div>

                    <!-- Tenurial Status -->
                    <div v-if="claim.fieldValues.tenurial_status" class="flex flex-col">
                      <span class="text-[10px] font-semibold text-green-600 uppercase tracking-wider mb-1">
                        Tenurial Status
                      </span>
                      <div class="w-full bg-white rounded-md px-3 py-2.5 border border-gray-200 shadow-sm hover:shadow transition-all duration-150">
                        <span class="text-sm text-gray-900 font-medium break-words">
                          {{ claim.fieldValues.tenurial_status }}
                        </span>
                      </div>
                    </div>

                    <!-- Area Insured -->
                    <div v-if="claim.fieldValues.area_insured" class="flex flex-col">
                      <span class="text-[10px] font-semibold text-green-600 uppercase tracking-wider mb-1">
                        Area Insured
                      </span>
                      <div class="w-full bg-white rounded-md px-3 py-2.5 border border-gray-200 shadow-sm hover:shadow transition-all duration-150">
                        <span class="text-sm text-gray-900 font-medium break-words">
                          {{ claim.fieldValues.area_insured }} hectares
                        </span>
                      </div>
                    </div>

                    <!-- Area Damaged -->
                    <div v-if="claim.fieldValues.area_damaged" class="flex flex-col">
                      <span class="text-[10px] font-semibold text-green-600 uppercase tracking-wider mb-1">
                        Area Damaged
                      </span>
                      <div class="w-full bg-white rounded-md px-3 py-2.5 border border-gray-200 shadow-sm hover:shadow transition-all duration-150">
                        <span class="text-sm text-red-600 font-medium break-words">
                          {{ claim.fieldValues.area_damaged }} hectares
                        </span>
                      </div>
                    </div>

                    <!-- Date Planting -->
                    <div v-if="claim.fieldValues.date_planting" class="flex flex-col">
                      <span class="text-[10px] font-semibold text-green-600 uppercase tracking-wider mb-1">
                        Date Planting
                      </span>
                      <div class="w-full bg-white rounded-md px-3 py-2.5 border border-gray-200 shadow-sm hover:shadow transition-all duration-150">
                        <span class="text-sm text-gray-900 font-medium break-words">
                          {{ formatSimpleDate(claim.fieldValues.date_planting) }}
                        </span>
                      </div>
                    </div>

                    <!-- Expected Harvest Date -->
                    <div v-if="claim.fieldValues.expected_harvest_date" class="flex flex-col">
                      <span class="text-[10px] font-semibold text-green-600 uppercase tracking-wider mb-1">
                        Expected Harvest Date
                      </span>
                      <div class="w-full bg-white rounded-md px-3 py-2.5 border border-gray-200 shadow-sm hover:shadow transition-all duration-150">
                        <span class="text-sm text-gray-900 font-medium break-words">
                          {{ formatSimpleDate(claim.fieldValues.expected_harvest_date) }}
                        </span>
                      </div>
                    </div>

                    <!-- Cultivation Stage -->
                    <div v-if="claim.fieldValues.cultivation_stage" class="flex flex-col">
                      <span class="text-[10px] font-semibold text-green-600 uppercase tracking-wider mb-1">
                        Cultivation Stage
                      </span>
                      <div class="w-full bg-white rounded-md px-3 py-2.5 border border-gray-200 shadow-sm hover:shadow transition-all duration-150">
                        <span class="text-sm text-gray-900 font-medium break-words">
                          {{ claim.fieldValues.cultivation_stage }}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Damage Details Card -->
              <div v-if="claim.fieldValues" class="bg-gray-100 rounded-xl border border-gray-300 shadow-sm">
                <div class="px-4 py-4">
                  <h2 class="text-lg font-bold text-gray-900 mb-4">Damage Details</h2>

                  <div class="grid grid-cols-1 md:grid-cols-2 gap-x-6 gap-y-3">
                    <!-- Cause of Damage -->
                    <div v-if="claim.fieldValues.cause_of_damage" class="flex flex-col">
                      <span class="text-[10px] font-semibold text-green-600 uppercase tracking-wider mb-1">
                        Cause of Damage
                      </span>
                      <div class="w-full bg-white rounded-md px-3 py-2.5 border border-gray-200 shadow-sm hover:shadow transition-all duration-150">
                        <span class="text-sm text-gray-900 font-medium break-words">
                          {{ claim.fieldValues.cause_of_damage }}
                        </span>
                      </div>
                    </div>

                    <!-- Date of Loss -->
                    <div v-if="claim.fieldValues.date_of_loss" class="flex flex-col">
                      <span class="text-[10px] font-semibold text-green-600 uppercase tracking-wider mb-1">
                        Date of Loss
                      </span>
                      <div class="w-full bg-white rounded-md px-3 py-2.5 border border-gray-200 shadow-sm hover:shadow transition-all duration-150">
                        <span class="text-sm text-gray-900 font-medium break-words">
                          {{ formatSimpleDate(claim.fieldValues.date_of_loss) }}
                        </span>
                      </div>
                    </div>

                    <!-- Damage Description -->
                    <div v-if="claim.fieldValues.damage_description" class="flex flex-col md:col-span-2">
                      <span class="text-[10px] font-semibold text-green-600 uppercase tracking-wider mb-1">
                        Damage Description
                      </span>
                      <div class="w-full bg-white rounded-md px-3 py-2.5 border border-gray-200 shadow-sm hover:shadow transition-all duration-150">
                        <span class="text-sm text-gray-900 font-medium break-words">
                          {{ claim.fieldValues.damage_description }}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Supporting Documents Card -->
              <div v-if="claim.supportingFiles && claim.supportingFiles.length > 0" class="bg-gray-100 rounded-xl border border-gray-300 shadow-sm">
                <div class="px-4 py-4">
                  <h2 class="text-lg font-bold text-gray-900 mb-4">
                    Supporting Documents ({{ claim.supportingFiles.length }})
                  </h2>

                  <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                    <div v-for="(file, index) in claim.supportingFiles" :key="index" class="relative group">
                      <img
                        :src="file"
                        :alt="`Supporting document ${index + 1}`"
                        class="w-full h-48 object-cover rounded-lg border border-gray-300 shadow-sm hover:shadow-md transition-all duration-150 cursor-pointer"
                        @click="openImageModal(file)"
                      />
                      <div class="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-10 transition-all duration-150 rounded-lg flex items-center justify-center">
                        <svg class="w-8 h-8 text-white opacity-0 group-hover:opacity-100 transition-all duration-150" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0zM10 7v3m0 0v3m0-3h3m-3 0H7"></path>
                        </svg>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Image Modal -->
    <div v-if="showImageModal" class="fixed inset-0 bg-black bg-opacity-75 z-50 flex items-center justify-center p-4" @click="closeImageModal">
      <div class="relative max-w-5xl max-h-full">
        <button
          class="absolute -top-10 right-0 text-white hover:text-gray-300 transition-colors"
          @click="closeImageModal">
          <svg class="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
          </svg>
        </button>
        <img
          :src="selectedImage"
          alt="Full size document"
          class="max-w-full max-h-screen object-contain rounded-lg"
          @click.stop
        />
      </div>
    </div>
  </AuthenticatedLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { HomeIcon, ChevronRightIcon, ExclamationTriangleIcon } from '@heroicons/vue/24/outline'
import AuthenticatedLayout from '@/layouts/AuthenticatedLayout.vue'
import LoadingSpinner from '@/components/others/LoadingSpinner.vue'
import { MUNICIPAL_AGRICULTURIST_NAVIGATION } from '@/lib/navigation'
import { useClaimStore } from '@/stores/claim.js'

const route = useRoute()
const router = useRouter()
const navigation = MUNICIPAL_AGRICULTURIST_NAVIGATION
const claimStore = useClaimStore()

// State
const claim = ref(null)
const loading = ref(false)
const error = ref(null)
const showImageModal = ref(false)
const selectedImage = ref(null)

// Methods
const fetchClaimDetail = async () => {
  try {
    loading.value = true
    error.value = null

    const claimId = route.params.id
    const result = await claimStore.getClaimById(claimId)

    if (result.success) {
      claim.value = result.data
    } else {
      error.value = result.message || 'Failed to load claim details'
    }
  } catch (err) {
    console.error('Error fetching claim:', err)
    error.value = err.message || 'An unexpected error occurred'
  } finally {
    loading.value = false
  }
}

const formatCurrency = (amount) => {
  if (!amount && amount !== 0) return '�0.00'
  return new Intl.NumberFormat('en-PH', {
    style: 'currency',
    currency: 'PHP'
  }).format(amount)
}

const formatDate = (dateString) => {
  if (!dateString) return 'N/A'
  try {
    return new Date(dateString).toLocaleDateString('en-PH', {
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

const formatSimpleDate = (dateString) => {
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

const openImageModal = (imageUrl) => {
  selectedImage.value = imageUrl
  showImageModal.value = true
}

const closeImageModal = () => {
  showImageModal.value = false
  selectedImage.value = null
}

const goBack = () => {
  router.push({ name: 'agriculturist-process-claims' })
}

// Lifecycle
onMounted(async () => {
  await fetchClaimDetail()
})
</script>

<style scoped>
/* Custom scrollbar styling */
.overflow-auto::-webkit-scrollbar {
  width: 8px;
}

.overflow-auto::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 4px;
}

.overflow-auto::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 4px;
}

.overflow-auto::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

.break-words {
  word-wrap: break-word;
  word-break: break-word;
}
</style>
