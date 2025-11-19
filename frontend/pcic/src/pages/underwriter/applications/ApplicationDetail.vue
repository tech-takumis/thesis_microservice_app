<template>
  <div class="min-h-screen bg-gray-50">
    <div class="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
      <!-- Breadcrumb Navigation -->
      <nav class="flex mb-6" aria-label="Breadcrumb">
        <ol class="flex items-center space-x-3">
          <li>
            <router-link
              :to="{ name: 'underwriter-dashboard' }"
              class="text-gray-400 hover:text-gray-600 transition-colors"
            >
              <HomeIcon class="h-5 w-5" />
            </router-link>
          </li>
          <li class="flex items-center">
            <ChevronRightIcon class="h-4 w-4 text-gray-400 mx-2" />
            <button
              class="text-sm font-medium text-gray-500 hover:text-gray-700 transition-colors"
              @click="navigateToApplicationList"
            >
              Applications
            </button>
          </li>
          <li class="flex items-center">
            <ChevronRightIcon class="h-4 w-4 text-gray-400 mx-2" />
            <span class="text-sm font-medium text-gray-900">Details</span>
          </li>
        </ol>
      </nav>

      <!-- Page Header -->
      <div class="mb-6">
        <div class="flex items-start justify-between">
          <div>
            <h1 class="text-2xl font-bold text-gray-900">{{ getApplicationTitle() }}</h1>
            <div v-if="insuranceData" class="mt-2 flex items-center gap-3">
              <span
                :class="[
                  'inline-flex items-center px-2.5 py-0.5 rounded-md text-xs font-medium',
                  insuranceData.status === 'APPROVED'
                    ? 'bg-green-100 text-green-800'
                    : insuranceData.status === 'PENDING'
                    ? 'bg-yellow-100 text-yellow-800'
                    : 'bg-red-100 text-red-800',
                ]"
              >
                {{ insuranceData.status }}
              </span>
              <span class="text-xs text-gray-500">
                ID: {{ insuranceData.insuranceId?.substring(0, 13) }}...
              </span>
            </div>
          </div>
          <button
            v-if="shouldShowClaim && insuranceData?.inspection && insuranceData.inspection.inspectorName"
            @click="processClaim"
            class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-lg text-white bg-orange-600 hover:bg-orange-700 shadow-sm transition-all"
          >
            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path>
            </svg>
            File Claim
          </button>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="flex justify-center items-center py-12">
        <LoadingSpinner />
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-lg p-4">
        <div class="flex">
          <ExclamationTriangleIcon class="h-5 w-5 text-red-400 mt-0.5" />
          <div class="ml-3">
            <h3 class="text-sm font-medium text-red-800">Error Loading Details</h3>
            <p class="mt-1 text-sm text-red-700">{{ error }}</p>
          </div>
        </div>
      </div>

      <!-- Main Content Grid -->
      <div v-else-if="insuranceData" :key="route.params.insuranceId">
        <!-- Two Column Layout: 7-5 ratio -->
        <div class="grid grid-cols-12 gap-6">

          <!-- Left Column: Main Content (7 columns) -->
          <div class="col-span-12 lg:col-span-7 space-y-4">

            <!-- Batch Information -->
            <div v-if="insuranceData?.batch" class="bg-white rounded-lg shadow-sm border border-gray-200">
              <div class="px-4 py-3 border-b border-gray-100">
                <h3 class="text-sm font-semibold text-gray-900">Batch Information</h3>
              </div>
              <div class="p-4">
                <dl class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                  <div>
                    <DetailField label="Batch Name" :value="insuranceData.batch.batchName" />
                  </div>
                  <div>
                    <DetailField label="Description" :value="insuranceData.batch.description" />
                  </div>
                  <div>
                    <DetailField label="Start Date" :value="formatDate(insuranceData.batch.startDate)" />
                  </div>
                  <div>
                    <DetailField label="End Date" :value="formatDate(insuranceData.batch.endDate)" />
                  </div>
                </dl>
              </div>
            </div>

            <!-- Application Information -->
            <div class="bg-white rounded-lg shadow-sm border border-gray-200">
              <div class="px-4 py-3 border-b border-gray-100">
                <h3 class="text-sm font-semibold text-gray-900">Application Information</h3>
              </div>
              <div class="p-4">
                <dl v-if="filteredDynamicFields.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                  <div v-for="field in filteredDynamicFields" :key="field.key">
                    <DetailField :label="field.label" :value="field.value" />
                  </div>
                </dl>
                <div v-else class="text-center py-8">
                  <DocumentIcon class="mx-auto h-10 w-10 text-gray-300" />
                  <p class="mt-2 text-sm text-gray-500">No application information available</p>
                </div>
              </div>
            </div>


            <!-- Verification Documents -->
            <div v-if="insuranceData?.verification?.verificationDocuments?.length > 0" class="bg-white rounded-lg shadow-sm border border-gray-200">
              <div class="px-4 py-3 border-b border-gray-100">
                <h3 class="text-sm font-semibold text-gray-900">Verification Documents</h3>
              </div>
              <div class="p-4">
                <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
                  <div
                    v-for="(file, index) in insuranceData.verification.verificationDocuments"
                    :key="index"
                    class="group relative aspect-square rounded-lg overflow-hidden bg-gray-100 cursor-pointer hover:ring-2 hover:ring-blue-500 transition-all"
                    @click="openImageModal(file)"
                  >
                    <img
                      :src="file"
                      :alt="`Document ${index + 1}`"
                      class="w-full h-full object-cover group-hover:scale-105 transition-transform"
                      @error="handleImageError"
                    />
                    <div class="absolute bottom-0 inset-x-0 bg-black bg-opacity-50 text-white text-xs py-1 text-center">
                      Doc {{ index + 1 }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Right Column: Workflow Cards (5 columns) -->
          <div class="col-span-12 lg:col-span-5 space-y-4">


            <!-- Verification Card -->
            <div v-if="shouldShowVerification" class="bg-white rounded-lg shadow-sm border border-gray-200">
              <div class="p-4">
                <div v-if="insuranceData?.verification" class="space-y-3">
                  <div class="flex items-center gap-2">
                    <CheckCircleIcon class="h-5 w-5 text-green-600 flex-shrink-0" />
                    <h4 class="text-sm font-semibold text-gray-900">Verified</h4>
                  </div>
                  <div class="space-y-2 text-xs">
                    <p class="text-gray-600">
                      <span class="font-medium">By:</span> {{ insuranceData.verification.verifierName }}
                    </p>
                    <p class="text-gray-600">
                      <span class="font-medium">Date:</span> {{ formatDate(insuranceData.verification.verifiedAt) }}
                    </p>
                    <p v-if="insuranceData.verification.remarks" class="text-gray-600">
                      <span class="font-medium">Remarks:</span> {{ insuranceData.verification.remarks }}
                    </p>
                  </div>
                </div>
                <div v-else class="text-center py-4">
                  <ExclamationTriangleIcon class="mx-auto h-8 w-8 text-yellow-500" />
                  <p class="mt-2 text-xs font-medium text-gray-700">Pending Verification</p>
                  <p class="mt-1 text-xs text-gray-500">Awaiting review</p>
                </div>
              </div>
            </div>

            <!-- Inspection Card -->
            <div v-if="shouldShowInspection" class="bg-white rounded-lg shadow-sm border border-gray-200">
              <div class="p-4">
                <div v-if="insuranceData?.inspection && insuranceData.inspection.inspectorName" class="space-y-3">
                  <div class="flex items-center gap-2">
                    <CheckCircleIcon class="h-5 w-5 text-indigo-600 flex-shrink-0" />
                    <h4 class="text-sm font-semibold text-gray-900">Inspected</h4>
                  </div>
                  <div class="space-y-2 text-xs">
                    <p class="text-gray-600">
                      <span class="font-medium">By:</span> {{ insuranceData.inspection.inspectorName }}
                    </p>
                    <p class="text-gray-600">
                      <span class="font-medium">Date:</span> {{ formatDate(insuranceData.inspection.inspectedAt) }}
                    </p>
                  </div>
                </div>
                <div v-else class="text-center py-4">
                  <DocumentIcon class="mx-auto h-8 w-8 text-gray-400" />
                  <p class="mt-2 text-xs font-medium text-gray-700">Pending Inspection</p>
                  <button
                    @click="scheduleInspection"
                    class="mt-3 inline-flex items-center px-3 py-1.5 text-xs font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 transition-colors"
                  >
                    Schedule
                  </button>
                </div>
              </div>
            </div>

            <!-- Policy Card -->
            <div v-if="shouldShowPolicy" class="bg-white rounded-lg shadow-sm border border-gray-200">
              <div class="p-4">
                <div v-if="insuranceData?.policy" class="space-y-3">
                  <div class="flex items-center gap-2">
                    <CheckCircleIcon class="h-5 w-5 text-purple-600 flex-shrink-0" />
                    <h4 class="text-sm font-semibold text-gray-900">Policy Issued</h4>
                  </div>
                  <div class="space-y-2 text-xs">
                    <p class="text-gray-600">
                      <span class="font-medium">Number:</span> {{ insuranceData.policy.policyNumber }}
                    </p>
                    <p class="text-gray-600">
                      <span class="font-medium">Effective:</span> {{ formatDate(insuranceData.policy.effectiveDate) }}
                    </p>
                  </div>
                </div>
                <div v-else class="text-center py-4">
                  <DocumentIcon class="mx-auto h-8 w-8 text-gray-400" />
                  <p class="mt-2 text-xs font-medium text-gray-700">No Policy</p>
                  <button
                    @click="generatePolicy"
                    class="mt-3 inline-flex items-center px-3 py-1.5 text-xs font-medium rounded-md text-white bg-purple-600 hover:bg-purple-700 transition-colors"
                  >
                    Generate
                  </button>
                </div>
              </div>
            </div>

            <!-- Claim Card -->
            <div v-if="shouldShowClaim" class="bg-white rounded-lg shadow-sm border border-gray-200">
              <div class="p-4">
                <div v-if="insuranceData?.claim" class="space-y-3">
                  <div class="flex items-center gap-2">
                    <CheckCircleIcon class="h-5 w-5 text-orange-600 flex-shrink-0" />
                    <h4 class="text-sm font-semibold text-gray-900">Claim Filed</h4>
                  </div>
                  <div class="space-y-2 text-xs">
                    <p class="text-gray-600">
                      <span class="font-medium">Amount:</span> {{ formatCurrency(insuranceData.claim.claimAmount) }}
                    </p>
                    <p class="text-gray-600">
                      <span class="font-medium">Status:</span> {{ insuranceData.claim.status }}
                    </p>
                  </div>
                </div>
                <div v-else class="text-center py-4">
                  <DocumentIcon class="mx-auto h-8 w-8 text-gray-400" />
                  <p class="mt-2 text-xs font-medium text-gray-700">No Claim</p>
                  <button
                    v-if="insuranceData?.inspection && insuranceData.inspection.inspectorName"
                    @click="processClaim"
                    class="mt-3 inline-flex items-center px-3 py-1.5 text-xs font-medium rounded-md text-white bg-orange-600 hover:bg-orange-700 transition-colors"
                  >
                    Process
                  </button>
                  <p v-else class="mt-3 text-xs text-gray-400">Awaiting inspection</p>
                </div>
              </div>
            </div>
          </div>

        </div>

        <!-- Full Width: Insurance Metadata (12 columns) -->
        <div class="col-span-12 mt-4">
          <div class="bg-white rounded-lg shadow-sm border border-gray-200">
            <div class="px-4 py-3 border-b border-gray-100">
              <h3 class="text-sm font-semibold text-gray-900">Metadata</h3>
            </div>
            <div class="p-4">
              <dl class="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div>
                  <DetailField label="Insurance ID" :value="insuranceData.insuranceId" />
                </div>
                <div>
                  <DetailField label="Submission ID" :value="insuranceData.submissionId" />
                </div>
                <div>
                  <DetailField label="Status" :value="insuranceData.status" />
                </div>
              </dl>
            </div>
          </div>
        </div>
      </div>

      <!-- No Data State -->
      <div v-else class="text-center py-12">
        <DocumentIcon class="mx-auto h-12 w-12 text-gray-300" />
        <h3 class="mt-4 text-sm font-medium text-gray-900">No Data Available</h3>
        <p class="mt-2 text-sm text-gray-500">Unable to load insurance details.</p>
        <button
          @click="fetchApplicationDetails"
          class="mt-6 inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-lg text-white bg-blue-600 hover:bg-blue-700 shadow-sm transition-all"
        >
          Try Again
        </button>
      </div>

      <!-- Image Modal -->
      <div
        v-if="selectedImage"
        class="fixed inset-0 z-50 flex items-center justify-center bg-black/80 backdrop-blur-sm p-4"
        @click="closeImageModal"
      >
        <button
          class="absolute top-4 right-4 p-2 rounded-full bg-white/10 hover:bg-white/20 text-white transition-colors"
          @click="closeImageModal"
        >
          <XMarkIcon class="h-6 w-6" />
        </button>
        <img
          :src="selectedImage"
          alt="Full size"
          class="max-w-full max-h-full object-contain rounded-lg shadow-2xl"
          @click.stop
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useApplicationStore } from '@/stores/applications'
import { useInsuranceStore } from '@/stores/insurance'
import DetailField from '@/components/tables/DetailField.vue'
import LoadingSpinner from '@/components/others/LoadingSpinner.vue'
import {
  HomeIcon,
  ChevronRightIcon,
  ExclamationTriangleIcon,
  DocumentIcon,
  XMarkIcon,
  CheckCircleIcon
} from '@heroicons/vue/24/outline'

// Composables
const route = useRoute()
const router = useRouter()
const applicationStore = useApplicationStore()
const insuranceStore = useInsuranceStore()

// State
const loading = ref(false)
const error = ref(null)
const applicationData = ref(null)
const applicationTypeData = ref(null)
const insuranceData = ref(null)
const workflowData = ref(null)
const selectedImage = ref(null)

async function fetchApplicationDetails() {
  console.log('fetchApplicationDetails called with insuranceId:', route.params.insuranceId, 'submissionId:', route.params.submissionId)

  if (!route.params.insuranceId || !route.params.submissionId) {
    error.value = 'No insurance ID or submission ID provided'
    console.error('Missing required route params:', { insuranceId: route.params.insuranceId, submissionId: route.params.submissionId })
    return
  }

  try {
    loading.value = true
    error.value = null

    // Reset all data before fetching new data
    applicationData.value = null
    applicationTypeData.value = null
    insuranceData.value = null
    workflowData.value = null

    console.log('ApplicationDetail: Starting parallel fetch for insuranceId:', route.params.insuranceId, 'and submissionId:', route.params.submissionId)

    // Fetch insurance data and workflow in parallel
    const [insuranceResult, workflowResult] = await Promise.allSettled([
      insuranceStore.fetchInsuranceById(route.params.insuranceId),
      applicationStore.fetchApplicationWorkflow(route.params.submissionId)
    ])

    // Process insurance result
    if (insuranceResult.status === 'fulfilled' && insuranceResult.value.success) {
      insuranceData.value = insuranceResult.value.data
      console.log('Insurance data fetched:', insuranceData.value)

      // Extract verification field values as applicationData
      if (insuranceData.value?.verification?.fieldValues) {
        applicationData.value = {
          dynamicFields: insuranceData.value.verification.fieldValues,
          id: route.params.submissionId,
          submittedAt: insuranceData.value.verification.verifiedAt,
          updatedAt: insuranceData.value.verification.verifiedAt
        }
      }
    } else {
      const errorMsg = insuranceResult.status === 'rejected'
        ? insuranceResult.reason.message
        : insuranceResult.value.message
      console.error('Failed to fetch insurance:', errorMsg)
      error.value = errorMsg || 'Failed to load insurance details'
      return // Don't continue if insurance fetch failed
    }

    // Process workflow result
    if (workflowResult.status === 'fulfilled' && workflowResult.value.success) {
      workflowData.value = workflowResult.value.data
      console.log('Workflow data fetched:', workflowData.value)

      // Set workflow as application type data for compatibility
      applicationTypeData.value = {
        workflow: {
          verification_enabled: workflowData.value.verificationEnabled,
          inspection_enabled: workflowData.value.inspectionEnabled,
          policy_enabled: workflowData.value.policyEnabled,
          claim_enabled: workflowData.value.claimEnabled
        }
      }
    } else {
      const errorMsg = workflowResult.status === 'rejected'
        ? workflowResult.reason.message
        : workflowResult.value.message
      console.error('Failed to fetch workflow:', errorMsg)
      // Don't set as error since we can still display the insurance
    }
  } catch (err) {
    console.error('Error fetching application details:', err)
    error.value = err.message || 'An unexpected error occurred'
  } finally {
    loading.value = false
    console.log('ApplicationDetail: Fetch completed. Final state:', {
      hasApplicationData: !!applicationData.value,
      hasApplicationTypeData: !!applicationTypeData.value,
      hasInsuranceData: !!insuranceData.value,
      hasWorkflowData: !!workflowData.value,
      hasError: !!error.value,
      isLoading: loading.value
    })
  }
}



const navigateToApplicationList = () => {
  // Navigate back to underwriter applications list
  router.push({
    name: 'underwriter-applications-all'
  })
}

const getApplicationTitle = () => {
  if (applicationData.value?.dynamicFields) {
    const fields = applicationData.value.dynamicFields

    // Try to get farmer name from different possible field combinations
    let farmerName = ''
    if (fields.first_name || fields.last_name) {
      farmerName = `${fields.first_name || ''} ${fields.middle_name || ''} ${fields.last_name || ''}`.replace(/\s+/g, ' ').trim()
    } else if (fields.farmer_name) {
      farmerName = fields.farmer_name
    }

    // Try to get identifier (CIC number or other unique identifier)
    const identifier = fields.cic_no || fields.id || fields.application_number

    // Try to get batch name from insurance data
    const batchName = insuranceData.value?.batch?.batchName

    if (farmerName && batchName) {
      return `${farmerName} - ${batchName}`
    } else if (farmerName && identifier) {
      return `${farmerName} - ${identifier}`
    } else if (farmerName) {
      return `${farmerName}`
    } else if (batchName) {
      return batchName
    } else {
      return 'Insurance Application'
    }
  }
  return 'Insurance Application Details'
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
  if (value === null || value === undefined || value === 0) return 'N/A'
  return `${parseFloat(value).toFixed(1)} ha`
}

// Dynamic label generation function
const generateFieldLabel = (fieldKey) => {
  return fieldKey
    .replace(/_/g, ' ') // Replace underscores with spaces
    .replace(/\b\w/g, char => char.toUpperCase()) // Capitalize first letter of each word
    .replace(/\bCic\b/g, 'CIC') // Special case for CIC
    .replace(/\bId\b/g, 'ID') // Special case for ID
    .replace(/\bNo\b/g, 'Number') // Replace 'No' with 'Number'
    .replace(/\bLot (\d+)\b/g, 'Lot $1') // Ensure proper formatting for lot numbers
    .replace(/\bDate (\w+)/g, 'Date of $1') // Convert 'Date Planting' to 'Date of Planting'
    .replace(/\bArea (\w+)/g, 'Area $1') // Proper formatting for area fields
    .replace(/\bCell Phone/g, 'Cell Phone') // Ensure proper casing
    .replace(/\bPhone Number/g, 'Phone Number') // Ensure proper casing
}


const filteredDynamicFields = computed(() => {
  if (!applicationData.value?.dynamicFields) return []

  const fields = []
  const dynamicFields = applicationData.value.dynamicFields

  // Process each field dynamically based on its key and value type
  Object.entries(dynamicFields).forEach(([key, value]) => {
    // Skip farmer_signature as it's usually empty or not meant for display
    if (key === 'farmer_signature') {
      return
    }

    // Skip null, undefined values
    if (value === null || value === undefined) {
      return
    }

    // Skip empty string values (but keep numeric zeros and booleans)
    if (typeof value === 'string' && value.trim() === '') {
      return
    }

    // Skip empty objects (check if object has no meaningful content)
    if (typeof value === 'object') {
      if (key.includes('location')) {
        // For location objects, check if any location field has content
        const hasLocationData = Object.values(value).some(locationValue =>
          locationValue && typeof locationValue === 'string' && locationValue.trim()
        )
        if (!hasLocationData) return
      } else if (key.includes('boundaries')) {
        // For boundary objects, check if any boundary has content
        const hasBoundaryData = Object.values(value).some(boundaryValue =>
          boundaryValue && typeof boundaryValue === 'string' && boundaryValue.trim()
        )
        if (!hasBoundaryData) return
      } else {
        // Skip other complex objects that we can't display meaningfully
        return
      }
    }

    // Generate dynamic label from field key
    const label = generateFieldLabel(key)
    let formattedValue = value

    // Format specific field types dynamically
    if (key.includes('date') && value && typeof value === 'string') {
      formattedValue = formatDate(value)
    } else if (key.includes('area') && (value || value === 0) && typeof value === 'number') {
      formattedValue = formatArea(value)
    } else if (key.includes('location') && typeof value === 'object' && value !== null) {
      // Format any location object dynamically
      const location = value
      const locationParts = [
        location.barangay,
        location.city,
        location.province,
        location.region
      ].filter(part => part && part.trim())
      formattedValue = locationParts.length > 0 ? locationParts.join(', ') : 'Not specified'
    } else if (key.includes('boundaries') && typeof value === 'object' && value !== null) {
      // Format any boundaries object dynamically
      const boundaries = value
      const boundaryParts = []

      if (boundaries.north && boundaries.north.trim()) boundaryParts.push(`North: ${boundaries.north}`)
      if (boundaries.south && boundaries.south.trim()) boundaryParts.push(`South: ${boundaries.south}`)
      if (boundaries.east && boundaries.east.trim()) boundaryParts.push(`East: ${boundaries.east}`)
      if (boundaries.west && boundaries.west.trim()) boundaryParts.push(`West: ${boundaries.west}`)

      formattedValue = boundaryParts.length > 0 ? boundaryParts.join('; ') : 'Not specified'
    } else if (typeof value === 'boolean') {
      formattedValue = value ? 'Yes' : 'No'
    } else if (typeof value === 'number') {
      formattedValue = value.toString()
    } else if (typeof value === 'string') {
      formattedValue = value
    }

    fields.push({
      key,
      label,
      value: formattedValue
    })
  })

  return fields
})

// Check if verification should be shown based on workflow
const shouldShowVerification = computed(() => {
  return applicationTypeData.value?.workflow?.verification_enabled === true
})

// Check if inspection should be shown based on workflow
const shouldShowInspection = computed(() => {
  return applicationTypeData.value?.workflow?.inspection_enabled === true
})

// Check if policy should be shown based on workflow
const shouldShowPolicy = computed(() => {
  return applicationTypeData.value?.workflow?.policy_enabled === true
})

// Check if claim should be shown based on workflow
const shouldShowClaim = computed(() => {
  return applicationTypeData.value?.workflow?.claim_enabled === true
})

// Check if AI analysis should be shown based on application type
const shouldShowAIAnalysis = computed(() => {
  return applicationTypeData.value?.requiresAIAnalysis === true
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

// Workflow action handlers
const scheduleInspection = () => {
  // TODO: Implement inspection scheduling logic
  // For now, show an alert
  alert('Inspection scheduling feature coming soon. This will allow you to assign an inspector and schedule a field visit.')
}

const generatePolicy = async () => {
  // TODO: Implement policy generation logic
  if (!insuranceData.value?.verification) {
    alert('Cannot generate policy: Application must be verified first.')
    return
  }

  if (!confirm('Are you sure you want to generate an insurance policy for this application?')) {
    return
  }

  try {
    // API call to generate policy would go here
    alert('Policy generation feature coming soon. This will create an insurance policy with coverage details, premium calculation, and policy number.')
  } catch (error) {
    console.error('Error generating policy:', error)
    alert('Failed to generate policy. Please try again.')
  }
}

const processClaim = () => {
  // Navigate to damage claim review page
  if (insuranceData.value?.insuranceId) {
    router.push({
      name: 'damage-claim-review',
      params: { insuranceId: insuranceData.value.insuranceId }
    })
  } else {
    alert('Cannot process claim: Insurance ID not found.')
  }
}

// Watch for route changes to reload data when navigating between applications
watch(() => [route.params.insuranceId, route.params.submissionId], ([newInsuranceId, newSubmissionId], [oldInsuranceId, oldSubmissionId]) => {
  console.log('ApplicationDetail: Route params changed from', {oldInsuranceId, oldSubmissionId}, 'to', {newInsuranceId, newSubmissionId})
  if ((newInsuranceId && newInsuranceId !== oldInsuranceId) || (newSubmissionId && newSubmissionId !== oldSubmissionId)) {
    // Reset state immediately
    applicationData.value = null
    applicationTypeData.value = null
    insuranceData.value = null
    workflowData.value = null
    error.value = null
    fetchApplicationDetails()
  }
}, { immediate: false })

onMounted(() => {
  fetchApplicationDetails()
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
