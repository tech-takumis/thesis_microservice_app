<template>
  <AuthenticatedLayout>
    <div class="h-full flex flex-col min-h-0 overflow-hidden">
      <!-- Fixed Header Section -->
      <div class="flex-shrink-0 mb-4">
        <!-- Header with breadcrumb -->
        <nav class="flex mb-4" aria-label="Breadcrumb">
          <ol class="flex items-center space-x-4">
            <li>
              <div>
                <router-link
                  to="/agriculturist/submit-crop-data"
                  class="text-gray-400 hover:text-gray-500"
                >
                  <HomeIcon class="flex-shrink-0 h-5 w-5" />
                  <span class="sr-only">Submit Crop Data</span>
                </router-link>
              </div>
            </li>
            <li>
              <div class="flex items-center">
                <ChevronRightIcon class="flex-shrink-0 h-5 w-5 text-gray-400" />
                <router-link
                  to="/agriculturist/submit-crop-data"
                  class="ml-4 text-sm font-medium text-gray-500 hover:text-gray-700"
                >
                  Application
                </router-link>
              </div>
            </li>
            <li>
              <div class="flex items-center">
                <ChevronRightIcon class="flex-shrink-0 h-5 w-5 text-gray-400" />
                <button
                  class="ml-4 text-sm font-medium text-gray-500 hover:text-gray-700"
                  @click="goBack"
                >
                  Application submission
                </button>
              </div>
            </li>
            <li>
              <div class="flex items-center">
                <ChevronRightIcon class="flex-shrink-0 h-5 w-5 text-gray-400" />
                <span class="ml-4 text-sm font-medium text-gray-900">
                  Application Details
                </span>
              </div>
            </li>
          </ol>
        </nav>

        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-3xl font-bold text-gray-900">Application Submission Details</h1>
            <p class="mt-1 text-sm text-gray-600">
              {{ insuranceData?.application?.farmer?.firstName }} {{ insuranceData?.application?.farmer?.lastName }} - {{ insuranceData?.application?.dynamicFields?.cic_no || 'N/A' }}
            </p>
          </div>
          <div class="flex space-x-3">
            <button
              class="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md shadow-sm text-sm font-medium text-gray-700 bg-white hover:bg-gray-50"
              @click="goBack"
            >
              <ArrowLeftIcon class="h-4 w-4 mr-2" />
              Back
            </button>
          </div>
        </div>
      </div>

      <!-- Main Content Area - Scrollable -->
      <div class="flex-1 min-h-0 overflow-y-auto">
        <!-- Loading State -->
        <div v-if="loading" class="flex justify-center items-center flex-1">
          <LoadingSpinner />
        </div>

        <!-- Error State -->
        <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-md p-4 mb-4">
          <div class="flex">
            <div class="flex-shrink-0">
              <ExclamationTriangleIcon class="h-5 w-5 text-red-400" />
            </div>
            <div class="ml-3">
              <h3 class="text-sm font-medium text-red-800">Error Loading Application Details</h3>
              <div class="mt-2 text-sm text-red-700">{{ error }}</div>
            </div>
          </div>
        </div>

        <!-- Application Details -->
        <div v-else-if="insuranceData" class="space-y-4">
        <!-- Insurance Overview -->
        <div class="bg-white shadow-sm border border-gray-200 rounded-lg overflow-hidden">
          <div class="px-4 py-3 border-b border-gray-200">
            <h3 class="text-base font-medium text-gray-900">Insurance Overview</h3>
          </div>
          <div class="px-4 py-3">
             <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
              <DetailField label="Insurance ID" :value="insuranceData.insuranceId" />
              <DetailField label="CIC Number" :value="insuranceData.application?.dynamicFields?.cic_no" />
              <DetailField label="AI Processing Required" :value="insuranceData.requiredAIProcessing ? 'Yes' : 'No'" />
              <DetailField label="Created At" :value="formatDate(insuranceData.createdAt)" />
            </div>
          </div>
        </div>

        <!-- Farmer Information -->
        <div class="bg-white shadow-sm border border-gray-200 rounded-lg overflow-hidden">
          <div class="px-6 py-4 border-b border-gray-200">
            <h3 class="text-lg font-medium text-gray-900">Farmer Information</h3>
          </div>
          <div class="px-6 py-4">
            <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <DetailField label="Full Name" :value="`${insuranceData.farmer?.firstName} ${insuranceData.farmer?.lastName}`" />
              <DetailField label="Username/RSBSA" :value="insuranceData.farmer?.username" />
              <DetailField label="Email Address" :value="insuranceData.farmer?.email" />
              <DetailField label="Phone Number" :value="insuranceData.farmer?.phoneNumber" />
              <DetailField label="Address" :value="insuranceData.application?.dynamicFields?.address" />
              <DetailField label="Cell Phone" :value="insuranceData.application?.dynamicFields?.cell_phone_number" />
            </div>
          </div>
        </div>

        <!-- Batch Information -->
        <div v-if="insuranceData.batch" class="bg-white shadow-sm border border-gray-200 rounded-lg overflow-hidden">
          <div class="px-6 py-4 border-b border-gray-200">
            <h3 class="text-lg font-medium text-gray-900">Batch Information</h3>
          </div>
          <div class="px-6 py-4">
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              <DetailField label="Batch Name" :value="insuranceData.batch.batchName" />
              <DetailField label="Description" :value="insuranceData.batch.description" />
              <DetailField label="Total Applications" :value="insuranceData.batch.totalApplications" />
              <DetailField label="Max Applications" :value="insuranceData.batch.maxApplications" />
              <DetailField label="Available" :value="insuranceData.batch.available ? 'Yes' : 'No'" />
              <DetailField label="Start Date" :value="formatDate(insuranceData.batch.startDate)" />
              <DetailField label="End Date" :value="formatDate(insuranceData.batch.endDate)" />
            </div>
          </div>
        </div>

        <!-- Crop & Farm Information -->
        <div class="bg-white shadow-sm border border-gray-200 rounded-lg overflow-hidden">
          <div class="px-6 py-4 border-b border-gray-200">
            <h3 class="text-lg font-medium text-gray-900">Crop & Farm Information</h3>
          </div>
          <div class="px-6 py-4">
            <div v-if="filteredDynamicFields.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              <DetailField
                v-for="field in filteredDynamicFields"
                :key="field.key"
                :label="field.label"
                :value="field.value"
              />
            </div>
            <div v-else class="flex items-center justify-center py-8">
              <div class="text-center">
                <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-gray-100">
                  <DocumentIcon class="h-6 w-6 text-gray-400" />
                </div>
                <h3 class="mt-2 text-sm font-medium text-gray-900">No Information Available</h3>
                <p class="mt-1 text-sm text-gray-500">No crop and farm information has been provided for this application.</p>
              </div>
            </div>
          </div>
        </div>

        <!-- AI Analysis Results -->
        <div v-if="insuranceData.aiResult" class="bg-white shadow-sm border border-gray-200 rounded-lg overflow-hidden">
          <div class="px-6 py-4 border-b border-gray-200">
            <h3 class="text-lg font-medium text-gray-900">AI Analysis Results</h3>
          </div>
          <div class="px-6 py-4">
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              <DetailField label="Result" :value="insuranceData.aiResult.result" />
              <DetailField label="Prediction" :value="insuranceData.aiResult.prediction" />
              <DetailField label="Accuracy" :value="insuranceData.aiResult.accuracy" />
              <DetailField label="Confidence" :value="insuranceData.aiResult.confidence" />
              <DetailField label="Severity" :value="insuranceData.aiResult.severity" />
              <DetailField label="Lesion Area" :value="insuranceData.aiResult.lesion_area" />
              <DetailField label="Leaf Area" :value="insuranceData.aiResult.leaf_area" />
            </div>

            <!-- Top 3 Predictions -->
            <div v-if="insuranceData.aiResult.top3_predictions?.length > 0" class="mt-6">
              <h4 class="text-sm font-medium text-gray-900 mb-3">Top 3 Predictions</h4>
              <div class="space-y-2">
                <div
                  v-for="prediction in insuranceData.aiResult.top3_predictions"
                  :key="prediction.rank"
                  class="flex items-center justify-between p-3 bg-gray-50 rounded-lg"
                >
                  <div class="flex items-center space-x-3">
                    <span class="inline-flex items-center justify-center h-6 w-6 rounded-full bg-indigo-100 text-indigo-600 text-xs font-medium">
                      {{ prediction.rank }}
                    </span>
                    <span class="text-sm font-medium text-gray-900">{{ prediction.class_name }}</span>
                  </div>
                  <span class="text-sm text-gray-500">{{ prediction.confidence }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Policy Information -->
        <div class="bg-white shadow-sm border border-gray-200 rounded-lg overflow-hidden">
          <div class="px-6 py-4 border-b border-gray-200">
            <h3 class="text-lg font-medium text-gray-900">Policy Information</h3>
          </div>
          <div class="px-6 py-4">
            <div v-if="insuranceData.policy && insuranceData.policy.policyNumber" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              <DetailField label="Policy Number" :value="insuranceData.policy.policyNumber" />
              <DetailField label="Effective Date" :value="formatDate(insuranceData.policy.effectiveDate)" />
              <DetailField label="Expiry Date" :value="formatDate(insuranceData.policy.expiryDate)" />
            </div>
            <div v-else class="flex items-center justify-center py-8">
              <div class="text-center">
                <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-amber-100">
                  <DocumentIcon class="h-6 w-6 text-amber-600" />
                </div>
                <h3 class="mt-2 text-sm font-medium text-gray-900">Policy Not Issued Yet</h3>
                <p class="mt-1 text-sm text-gray-500">The insurance policy for this application has not been issued yet. Please wait for policy generation.</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Verification Information -->
        <div class="bg-white shadow-sm border border-gray-200 rounded-lg overflow-hidden">
          <div class="px-6 py-4 border-b border-gray-200">
            <h3 class="text-lg font-medium text-gray-900">Verification Information</h3>
          </div>
          <div class="px-6 py-4">
            <div v-if="insuranceData.verification && insuranceData.verification.verifiedBy" class="grid grid-cols-1 md:grid-cols-2 gap-6">
              <DetailField label="Verified By" :value="insuranceData.verification.verifiedBy" />
              <DetailField label="Verified At" :value="formatDate(insuranceData.verification.verifiedAt)" />
              <DetailField label="Remarks" :value="insuranceData.verification.remarks" />
            </div>
            <div v-else class="flex items-center justify-center py-8">
              <div class="text-center">
                <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-blue-100">
                  <ExclamationTriangleIcon class="h-6 w-6 text-blue-600" />
                </div>
                <h3 class="mt-2 text-sm font-medium text-gray-900">Verification Not Completed</h3>
                <p class="mt-1 text-sm text-gray-500">This application is awaiting verification from an authorized personnel. The verification process has not been completed yet.</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Inspection Information -->
        <div class="bg-white shadow-sm border border-gray-200 rounded-lg overflow-hidden">
          <div class="px-6 py-4 border-b border-gray-200">
            <h3 class="text-lg font-medium text-gray-900">Inspection Information</h3>
          </div>
          <div class="px-6 py-4">
            <div v-if="insuranceData.inspection && insuranceData.inspection.inspectorName">
              <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                <DetailField label="Inspector Name" :value="insuranceData.inspection.inspectorName" />
                <DetailField label="Inspected At" :value="formatDate(insuranceData.inspection.inspectedAt)" />
              </div>

              <!-- Inspection Photos -->
              <div v-if="insuranceData.inspection.photos?.length > 0" class="mt-6">
                <h4 class="text-sm font-medium text-gray-900 mb-3">Inspection Photos</h4>
                <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                  <div
                    v-for="(photo, index) in insuranceData.inspection.photos"
                    :key="index"
                    class="aspect-square bg-gray-100 rounded-lg overflow-hidden"
                  >
                    <img
                      :src="photo"
                      :alt="`Inspection photo ${index + 1}`"
                      class="w-full h-full object-cover hover:scale-105 transition-transform cursor-pointer"
                      @click="openImageModal(photo)"
                      @error="handleImageError"
                    />
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="flex items-center justify-center py-8">
              <div class="text-center">
                <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-indigo-100">
                  <DocumentIcon class="h-6 w-6 text-indigo-600" />
                </div>
                <h3 class="mt-2 text-sm font-medium text-gray-900">Inspection Not Scheduled</h3>
                <p class="mt-1 text-sm text-gray-500">Field inspection has not been scheduled for this application yet. An inspector will be assigned soon.</p>
              </div>
            </div>
          </div>
        </div>

        <!-- Claim Information -->
        <div class="bg-white shadow-sm border border-gray-200 rounded-lg overflow-hidden">
          <div class="px-6 py-4 border-b border-gray-200">
            <h3 class="text-lg font-medium text-gray-900">Claim Information</h3>
          </div>
          <div class="px-6 py-4">
            <div v-if="insuranceData.claim && insuranceData.claim.filedAt">
              <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                <DetailField label="Filed At" :value="formatDate(insuranceData.claim.filedAt)" />
                <DetailField label="Damage Assessment" :value="insuranceData.claim.damageAssessment" />
                <DetailField label="Claim Amount" :value="formatCurrency(insuranceData.claim.claimAmount)" />
              </div>

              <!-- Supporting Files -->
              <div v-if="insuranceData.claim.supportingFiles?.length > 0" class="mt-6">
                <h4 class="text-sm font-medium text-gray-900 mb-3">Supporting Files</h4>
                <div class="space-y-2">
                  <div
                    v-for="(file, index) in insuranceData.claim.supportingFiles"
                    :key="index"
                    class="flex items-center justify-between p-3 bg-gray-50 rounded-lg"
                  >
                    <div class="flex items-center space-x-3">
                      <DocumentIcon class="h-5 w-5 text-gray-400" />
                      <span class="text-sm font-medium text-gray-900">Supporting File {{ index + 1 }}</span>
                    </div>
                    <button
                      class="text-indigo-600 hover:text-indigo-800 text-sm font-medium"
                      @click="downloadFile(file)"
                    >
                      Download
                    </button>
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="flex items-center justify-center py-8">
              <div class="text-center">
                <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-gray-100">
                  <DocumentIcon class="h-6 w-6 text-gray-400" />
                </div>
                <h3 class="mt-2 text-sm font-medium text-gray-900">No Claim Filed Yet</h3>
                <p class="mt-1 text-sm text-gray-500">No insurance claim has been filed for this application yet. Claims can be filed when crop damage occurs.</p>
              </div>
            </div>
          </div>
        </div>

        <!-- File Uploads -->
        <div v-if="insuranceData.application?.fileUploads?.length > 0" class="bg-white shadow-sm border border-gray-200 rounded-lg overflow-hidden">
          <div class="px-6 py-4 border-b border-gray-200">
            <h3 class="text-lg font-medium text-gray-900">Uploaded Files</h3>
          </div>
          <div class="px-6 py-4">
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
              <div
                v-for="(file, index) in insuranceData.application.fileUploads"
                :key="index"
                class="aspect-square bg-gray-100 rounded-lg overflow-hidden"
              >
                <img
                  :src="file"
                  :alt="`Uploaded file ${index + 1}`"
                  class="w-full h-full object-cover hover:scale-105 transition-transform cursor-pointer"
                  @click="openImageModal(file)"
                  @error="handleImageError"
                />
              </div>
            </div>
          </div>
        </div>

        <!-- Application Metadata -->
        <div class="bg-white shadow-sm border border-gray-200 rounded-lg overflow-hidden">
          <div class="px-6 py-4 border-b border-gray-200">
            <h3 class="text-lg font-medium text-gray-900">Application Metadata</h3>
          </div>
          <div class="px-6 py-4">
            <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              <DetailField label="Application ID" :value="insuranceData.application?.id" />
              <DetailField label="Submitted At" :value="formatDate(insuranceData.application?.submittedAt)" />
              <DetailField label="Updated At" :value="formatDate(insuranceData.application?.updatedAt)" />
              <DetailField label="Version" :value="insuranceData.application?.version" />
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Image Modal -->
    <div
      v-if="selectedImage"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-75 backdrop-blur-sm"
      @click="closeImageModal"
    >
      <div class="relative max-w-4xl max-h-4xl p-4">
        <button
          class="absolute top-2 right-2 z-10 p-2 rounded-full bg-black bg-opacity-50 text-white hover:bg-opacity-75 transition-all"
          @click="closeImageModal"
        >
          <XMarkIcon class="h-6 w-6" />
        </button>
        <img
          :src="selectedImage"
          alt="Full size image"
          class="max-w-full max-h-full object-contain rounded-lg"
          @click.stop
        />
      </div>
      </div>
    </div>
  </AuthenticatedLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useInsuranceStore } from '@/stores/insurance'
import AuthenticatedLayout from '@/layouts/AuthenticatedLayout.vue'
import LoadingSpinner from '@/components/others/LoadingSpinner.vue'
import DetailField from '@/components/tables/DetailField.vue'
import {
  HomeIcon,
  ChevronRightIcon,
  ArrowLeftIcon,
  ExclamationTriangleIcon,
  DocumentIcon,
  XMarkIcon
} from '@heroicons/vue/24/outline'

const router = useRouter()
const route = useRoute()
const insuranceStore = useInsuranceStore()

const loading = ref(false)
const error = ref(null)
const insuranceData = ref(null)
const selectedImage = ref(null)

const fetchInsuranceDetails = async () => {
  loading.value = true
  error.value = null

  try {
    const insuranceId = route.params.id
    console.log('Fetching insurance details for ID:', insuranceId)

    const result = await insuranceStore.fetchInsuranceById(
      insuranceId,
      true, // batch
      true, // application
      true, // farmer
      true, // verification
      true, // inspection
      true, // policy
      true  // claim
    )

    if (result.success === "true") {
      console.log('Insurance details fetched successfully:', result.data)
      insuranceData.value = result.data
    } else {
      console.error('Failed to fetch insurance details:', result.message)
      error.value = result.message || 'Failed to load insurance details'
    }
  } catch (err) {
    console.error('Error fetching insurance details:', err)
    error.value = err.message || 'An unexpected error occurred'
  } finally {
    loading.value = false
  }
}

const goBack = () => {
  router.go(-1)
}

const formatDate = (dateString) => {
  if (!dateString) return 'N/A'

  try {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (error) {
    console.error('Error formatting date:', error)
    return 'Invalid Date'
  }
}

const formatCurrency = (amount) => {
  if (!amount) return 'N/A'

  try {
    return new Intl.NumberFormat('en-PH', {
      style: 'currency',
      currency: 'PHP'
    }).format(amount)
  } catch (error) {
    console.error('Error formatting currency:', error)
    return `₱${amount}`
  }
}

const formatArea = (value) => {
  if (value === null || value === undefined) return 'N/A'
  return `${parseFloat(value).toFixed(1)} ha`
}

// Field label mappings for better display
const fieldLabelMap = {
  'cic_no': 'CIC Number',
  'farmer_name': 'Farmer Name',
  'address': 'Address',
  'cell_phone_number': 'Cell Phone Number',
  'insured_crops': 'Insured Crops',
  'variety_planted': 'Variety Planted',
  'farm_location': 'Farm Location',
  'date_planting': 'Date of Planting',
  'expected_harvest_date': 'Expected Harvest Date',
  'area_insured': 'Area Insured',
  'area_damaged': 'Area Damaged',
  'date_of_loss': 'Date of Loss',
  'cultivation_stage': 'Cultivation Stage',
  'lot_1_area': 'Lot 1 Area',
  'lot_2_area': 'Lot 2 Area',
  'lot_3_area': 'Lot 3 Area',
  'lot_4_area': 'Lot 4 Area',
  'cause_of_loss_leaf': 'Cause of Loss (Leaf)',
  'cause_of_loss_overall': 'Cause of Loss (Overall)',
  'cause_of_loss_with_self': 'Cause of Loss (Self Assessment)'
}

// Computed property to get filtered dynamic fields
const filteredDynamicFields = computed(() => {
  if (!insuranceData.value?.application?.dynamicFields) return []

  const fields = []
  const dynamicFields = insuranceData.value.application.dynamicFields

  Object.entries(dynamicFields).forEach(([key, value]) => {
    // Skip empty values, null, undefined, empty strings, and complex objects
    if (value === null || value === undefined || value === '' ||
        (typeof value === 'object' && value !== null)) {
      return
    }

    // Skip empty string values specifically (but keep numeric zeros)
    if (typeof value === 'string' && value.trim() === '') {
      return
    }

    const label = fieldLabelMap[key] || key.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase())
    let formattedValue = value

    // Format specific field types
    if (key.includes('date') && value) {
      formattedValue = formatDate(value)
    } else if (key.includes('area') && (value || value === 0)) {
      formattedValue = formatArea(value)
    }

    fields.push({
      key,
      label,
      value: formattedValue
    })
  })

  return fields
})


const openImageModal = (imageUrl) => {
  selectedImage.value = imageUrl
}

const closeImageModal = () => {
  selectedImage.value = null
}

const handleImageError = (event) => {
  event.target.src = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="200" height="200"%3E%3Crect fill="%23ddd" width="200" height="200"/%3E%3Ctext fill="%23999" x="50%25" y="50%25" text-anchor="middle" dy=".3em"%3EImage not found%3C/text%3E%3C/svg%3E'
}

const downloadFile = (fileUrl) => {
  window.open(fileUrl, '_blank')
}

onMounted(() => {
  fetchInsuranceDetails()
})
</script>

<style scoped>
.aspect-square {
  aspect-ratio: 1 / 1;
}

/* Compact card styling */
:deep(.bg-white.shadow-sm.border.border-gray-200.rounded-lg) {
  margin-bottom: 1rem;
}

:deep(.bg-white.shadow-sm.border.border-gray-200.rounded-lg .px-6) {
  padding-left: 1rem;
  padding-right: 1rem;
}

:deep(.bg-white.shadow-sm.border.border-gray-200.rounded-lg .py-4) {
  padding-top: 0.75rem;
  padding-bottom: 0.75rem;
}

:deep(.grid.gap-6) {
  gap: 1rem;
}
</style>

