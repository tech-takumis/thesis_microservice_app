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
                  :to="{ name: 'agriculturist-submit-crop-data' }"
                  class="text-gray-400 hover:text-gray-500"
                >
                  <HomeIcon class="flex-shrink-0 h-5 w-5" />
                  <span class="sr-only">Application Types</span>
                </router-link>
              </div>
            </li>
            <li>
              <div class="flex items-center">
                <ChevronRightIcon class="flex-shrink-0 h-5 w-5 text-gray-400" />
                <button
                  class="ml-4 text-sm font-medium text-gray-500 hover:text-gray-700"
                  @click="navigateToApplicationList"
                >
                  Applications
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
            <h1 class="text-3xl font-bold text-gray-700">Application Submission Details</h1>
            <p class="mt-1 text-sm text-gray-600">
              {{ getApplicationTitle() }}
            </p>
          </div>
          <div v-if="shouldShowAIAnalysis">
            <router-link
              :to="{ name: 'agriculturist-damage-report', params: { applicationId: applicationData.id, applicationTypeId: route.params.applicationTypeId } }"
              class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-colors"
            >
              <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
              </svg>
              View AI Analysis
            </router-link>
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
<div v-else-if="applicationData" :key="route.params.id" class="space-y-4">

  <!-- Application Type Information -->
  <div
    v-if="applicationTypeData"
    class="bg-white border border-gray-200 rounded-xl overflow-hidden"
  >

    <!-- Header -->
    <div class="px-5 py-4 border-b border-gray-200 flex items-center gap-3 bg-gray-50">
      <DocumentIcon class="w-5 h-5 text-gray-700" />

      <h3 class="text-lg font-semibold text-gray-700">
        Application Type
      </h3>
    </div>

    <!-- Content -->
    <div class="px-5 py-4">
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">

        <!-- DetailField Box -->
        <div class="p-4 border rounded-lg bg-gray-50 shadow-sm">
          <DetailField label="Name" :value="applicationTypeData.name" />
        </div>

        <div class="p-4 border rounded-lg bg-gray-50 shadow-sm">
          <DetailField label="Description" :value="applicationTypeData.description" />
        </div>

      </div>
    </div>
  </div>

<!-- Batch Information -->
<div
  v-if="insuranceData?.batch"
  class="bg-white shadow-md border border-gray-200 rounded-xl overflow-hidden"
>
  <!-- Header -->
  <div class="px-5 py-4 border-b border-gray-200 flex items-center gap-3 bg-gray-50">
    <!-- Choose any icon -->
    <FolderIcon class="w-5 h-5 text-gray-700" />

    <h3 class="text-lg font-semibold text-gray-700">
      Batch Information
    </h3>
  </div>

  <!-- Content -->
  <div class="px-5 py-4">
    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">

      <!-- Boxed DetailField items -->
      <div class="p-4 border rounded-lg bg-gray-50 shadow-sm">
        <DetailField label="Batch Name" :value="insuranceData.batch.batchName" />
      </div>

      <div class="p-4 border rounded-lg bg-gray-50 shadow-sm">
        <DetailField label="Description" :value="insuranceData.batch.description" />
      </div>

      <div class="p-4 border rounded-lg bg-gray-50 shadow-sm">
        <DetailField label="Start Date" :value="formatDate(insuranceData.batch.startDate)" />
      </div>

      <div class="p-4 border rounded-lg bg-gray-50 shadow-sm">
        <DetailField label="End Date" :value="formatDate(insuranceData.batch.endDate)" />
      </div>

    </div>
  </div>
</div>

<!-- Application Information -->
<div class="bg-white shadow-md border border-gray-200 rounded-xl overflow-hidden">

  <!-- Header -->
  <div class="px-5 py-4 border-b border-gray-200 flex items-center gap-3 bg-gray-50">
    <DocumentIcon class="w-5 h-5 text-gray-700" />

    <h3 class="text-lg font-semibold text-gray-700">
      Application Information
    </h3>
  </div>

  <!-- Content -->
  <div class="px-5 py-4">

    <!-- Dynamic Fields -->
    <div v-if="filteredDynamicFields.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">

      <div
        v-for="field in filteredDynamicFields"
        :key="field.key"
        class="p-4 border rounded-lg bg-gray-50 shadow-sm"
      >
        <DetailField :label="field.label" :value="field.value" />
      </div>

    </div>

    <!-- Empty State -->
    <div v-else class="flex items-center justify-center py-10">
      <div class="text-center space-y-2">

        <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-gray-100">
          <DocumentIcon class="h-6 w-6 text-gray-400" />
        </div>

        <h3 class="text-sm font-medium text-gray-900">
          No Information Available
        </h3>

        <p class="text-sm text-gray-500">
          No application information has been provided for this submission.
        </p>

      </div>
    </div>

  </div>
</div>


<!-- File Uploads -->
<div
  v-if="applicationData.fileUploads?.length > 0"
  class="bg-white shadow-md border border-gray-200 rounded-xl overflow-hidden"
>

  <!-- Header -->
  <div class="px-5 py-4 border-b border-gray-200 flex items-center gap-3 bg-gray-50">
    <DocumentIcon class="w-5 h-5 text-gray-700" />

    <h3 class="text-lg font-semibold text-gray-700">
      Uploaded Files
    </h3>
  </div>

  <!-- Content -->
  <div class="px-5 py-4">
    <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-5">

      <!-- File Item -->
      <div
        v-for="(file, index) in applicationData.fileUploads"
        :key="index"
        class="border rounded-lg bg-gray-50 shadow-sm p-2 hover:shadow-md transition-all"
      >
        <div class="aspect-square rounded-md overflow-hidden bg-gray-100">
          <img
            :src="file"
            :alt="`Uploaded file ${index + 1}`"
            class="w-full h-full object-cover transition-transform duration-300 hover:scale-105 cursor-pointer"
            @click="openImageModal(file)"
            @error="handleImageError"
          />
        </div>

        <div class="mt-2 text-sm text-gray-600 truncate text-center">
          File {{ index + 1 }}
        </div>
      </div>

    </div>
  </div>

</div>


          <!-- Verification Information -->
          <div v-if="shouldShowVerification" class="bg-white shadow-sm border border-gray-200 rounded-lg overflow-hidden">
            <div class="px-6 py-4 border-b border-gray-200">
              <h3 class="text-lg font-medium text-gray-700">Verification Information</h3>
            </div>
            <div class="px-6 py-4">
              <!-- Verification Complete -->
              <div v-if="insuranceData?.verification" class="space-y-6">
                <!-- Success Header with Icon -->
                <div class="flex items-center space-x-3 p-4 bg-green-50 rounded-lg border border-green-200">
                  <div class="flex-shrink-0">
                    <CheckCircleIcon class="h-8 w-8 text-green-600" />
                  </div>
                  <div class="flex-1">
                    <h4 class="text-lg font-medium text-green-800">Application Verified Successfully</h4>
                    <p class="text-sm text-green-600 mt-1">This application has been verified and approved by authorized personnel.</p>
                  </div>
                </div>

                <!-- Verification Details -->
                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <DetailField label="Verifier Name" :value="insuranceData.verification.verifierName || 'Not Available'" />
                  <DetailField label="Verified At" :value="formatDate(insuranceData.verification.verifiedAt)" />
                  <div class="md:col-span-2">
                    <DetailField label="Verification Remarks" :value="insuranceData.verification.remarks || 'No remarks provided'" />
                  </div>
                </div>

                <!-- Additional Verification Information -->
                <div v-if="insuranceData.verification.fieldValues" class="bg-gray-50 p-4 rounded-lg">
                  <h5 class="text-sm font-medium text-gray-900 mb-2">Verified Field Information</h5>
                  <p class="text-sm text-gray-600">Field values have been verified and validated as part of this verification process.</p>
                </div>
              </div>

              <!-- Verification Not Complete -->
              <div v-else class="flex items-center justify-center py-8">
                <div class="text-center">
                  <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-blue-100">
                    <ExclamationTriangleIcon class="h-6 w-6 text-blue-600" />
                  </div>
                  <h3 class="mt-2 text-sm font-medium text-gray-900">Verification Not Completed</h3>
                  <p class="mt-1 text-sm text-gray-500">This application is awaiting verification from an authorized personnel. The verification process has not been completed yet.</p>
                  <router-link
                    :to="{ name: 'agriculturist-application-verification', params: { applicationId: applicationData.id, applicationTypeId: route.params.applicationTypeId } }"
                    class="mt-4 inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                  >
                    Start Verification
                  </router-link>
                </div>
              </div>
            </div>
          </div>

          <!-- Inspection Information -->
          <div v-if="shouldShowInspection" class="bg-white shadow-sm border border-gray-200 rounded-lg overflow-hidden">
            <div class="px-6 py-4 border-b border-gray-200">
              <h3 class="text-lg font-medium text-gray-900">Inspection Information</h3>
            </div>
            <div class="px-6 py-4">
              <div v-if="insuranceData?.inspection && insuranceData.inspection.inspectorName">
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

          <!-- Policy Information -->
          <div v-if="shouldShowPolicy" class="bg-white shadow-sm border border-gray-200 rounded-lg overflow-hidden">
            <div class="px-6 py-4 border-b border-gray-200">
              <h3 class="text-lg font-medium text-gray-700">Policy Information</h3>
            </div>
            <div class="px-6 py-4">
              <!-- Policy Complete -->
              <div v-if="insuranceData?.policy" class="space-y-6">
                <!-- Success Header with Icon -->
                <div class="flex items-center space-x-3 p-4 bg-purple-50 rounded-lg border border-purple-200">
                  <div class="flex-shrink-0">
                    <CheckCircleIcon class="h-8 w-8 text-purple-600" />
                  </div>
                  <div class="flex-1">
                    <h4 class="text-lg font-medium text-purple-800">Policy Issued Successfully</h4>
                    <p class="text-sm text-purple-600 mt-1">Insurance policy has been generated and is now active.</p>
                  </div>
                </div>

                <!-- Policy Details -->
                <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                  <DetailField label="Policy Number" :value="insuranceData.policy.policyNumber || 'Not Available'" />
                  <DetailField label="Effective Date" :value="formatDate(insuranceData.policy.effectiveDate)" />
                  <DetailField label="Expiry Date" :value="formatDate(insuranceData.policy.expiryDate)" />
                  <div v-if="insuranceData.policy.coverageAmount" class="md:col-span-2 lg:col-span-1">
                    <DetailField label="Coverage Amount" :value="insuranceData.policy.coverageAmount" />
                  </div>
                </div>

                <!-- Additional Policy Information -->
                <div v-if="insuranceData.policy.terms || insuranceData.policy.conditions" class="bg-gray-50 p-4 rounded-lg">
                  <h5 class="text-sm font-medium text-gray-900 mb-2">Policy Information</h5>
                  <p class="text-sm text-gray-600">Policy terms and conditions are active. Please refer to your policy document for complete details.</p>
                </div>
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

          <!-- Claim Information -->
          <div v-if="shouldShowClaim" class="bg-white shadow-sm border border-gray-200 rounded-lg overflow-hidden">
            <div class="px-6 py-4 border-b border-gray-200">
              <h3 class="text-lg font-medium text-gray-900">Claim Information</h3>
            </div>
            <div class="px-6 py-4">
              <!-- Claim Complete -->
              <div v-if="insuranceData?.claim" class="space-y-6">
                <!-- Success Header with Icon -->
                <div class="flex items-center space-x-3 p-4 bg-orange-50 rounded-lg border border-orange-200">
                  <div class="flex-shrink-0">
                    <CheckCircleIcon class="h-8 w-8 text-orange-600" />
                  </div>
                  <div class="flex-1">
                    <h4 class="text-lg font-medium text-orange-800">Claim Filed Successfully</h4>
                    <p class="text-sm text-orange-600 mt-1">Insurance claim has been filed and is being processed.</p>
                  </div>
                </div>

                <!-- Claim Details -->
                <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                  <DetailField label="Filed At" :value="formatDate(insuranceData.claim.filedAt)" />
                  <DetailField label="Damage Assessment" :value="insuranceData.claim.damageAssessment || 'Pending Assessment'" />
                  <DetailField label="Claim Amount" :value="formatCurrency(insuranceData.claim.claimAmount) || 'To be determined'" />
                  <div v-if="insuranceData.claim.status" class="md:col-span-2 lg:col-span-1">
                    <DetailField label="Status" :value="insuranceData.claim.status" />
                  </div>
                </div>

                <!-- Supporting Files -->
                <div v-if="insuranceData.claim.supportingFiles?.length > 0" class="bg-gray-50 p-4 rounded-lg">
                  <h4 class="text-sm font-medium text-gray-900 mb-3">Supporting Files ({{ insuranceData.claim.supportingFiles.length }})</h4>
                  <div class="space-y-2">
                    <div
                      v-for="(file, index) in insuranceData.claim.supportingFiles"
                      :key="index"
                      class="flex items-center justify-between p-3 bg-white rounded border"
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

                <!-- Additional Claim Information -->
                <div v-if="insuranceData.claim.remarks" class="bg-gray-50 p-4 rounded-lg">
                  <h5 class="text-sm font-medium text-gray-900 mb-2">Claim Remarks</h5>
                  <p class="text-sm text-gray-600">{{ insuranceData.claim.remarks }}</p>
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



          <!-- Application Metadata -->
          <div class="bg-white shadow-sm border border-gray-200 rounded-lg overflow-hidden">
            <div class="px-6 py-4 border-b border-gray-200">
              <h3 class="text-lg font-medium text-gray-700">Application Metadata</h3>
            </div>
            <div class="px-6 py-4">
              <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
                <DetailField label="Application ID" :value="applicationData.id" />
                <DetailField label="Submitted At" :value="formatDate(applicationData.submittedAt)" />
                <DetailField label="Updated At" :value="formatDate(applicationData.updatedAt)" />
              </div>
            </div>
          </div>
        </div>

        <!-- No Data State - Fallback when data is not loading, no error, but no applicationData -->
        <div v-else class="flex justify-center items-center flex-1 min-h-96">
          <div class="text-center">
            <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-gray-100">
              <DocumentIcon class="h-6 w-6 text-gray-400" />
            </div>
            <h3 class="mt-2 text-sm font-medium text-gray-900">No Application Data</h3>
            <p class="mt-1 text-sm text-gray-500">The application data could not be loaded. Please try refreshing the page.</p>
            <div class="mt-6">
              <button
                class="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-indigo-600 hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500"
                @click="fetchApplicationDetails"
              >
                Try Again
              </button>
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
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useApplicationStore, useApplicationTypeStore } from '@/stores/applications'
import { useInsuranceStore } from '@/stores/insurance'
import AuthenticatedLayout from '@/layouts/AuthenticatedLayout.vue'
import DetailField from '@/components/tables/DetailField.vue'
import LoadingSpinner from '@/components/others/LoadingSpinner.vue'
import {
  HomeIcon,
  ChevronRightIcon,
  ExclamationTriangleIcon,
  DocumentIcon,
  XMarkIcon,
  FolderIcon,
  CheckCircleIcon
} from '@heroicons/vue/24/outline'

// Composables
const route = useRoute()
const router = useRouter()
const applicationStore = useApplicationStore()
const applicationTypeStore = useApplicationTypeStore()
const insuranceStore = useInsuranceStore()

// State
const loading = ref(false)
const error = ref(null)
const applicationData = ref(null)
const applicationTypeData = ref(null)
const insuranceData = ref(null)
const selectedImage = ref(null)

async function fetchApplicationDetails() {
  console.log('fetchApplicationDetails called with route.params.id:', route.params.id, 'applicationTypeId:', route.params.applicationTypeId)

  if (!route.params.id || !route.params.applicationTypeId) {
    error.value = 'No application ID or application type ID provided'
    console.error('Missing required route params:', { id: route.params.id, applicationTypeId: route.params.applicationTypeId })
    return
  }

  try {
    loading.value = true
    error.value = null
    
    // Reset all data before fetching new data
    applicationData.value = null
    applicationTypeData.value = null
    insuranceData.value = null
    
    console.log('ApplicationDetail: Starting parallel fetch for application ID:', route.params.id, 'and application type ID:', route.params.applicationTypeId)

    // Fetch all data in parallel for faster loading
    const [applicationResult, applicationTypeResult, insuranceResult] = await Promise.allSettled([
      applicationStore.fetchApplicationById(route.params.id),
      applicationTypeStore.fetchApplicationTypesById(route.params.applicationTypeId, false),
      insuranceStore.fetchInsuranceByApplicationId(route.params.id)
    ])

    // Process application result
    if (applicationResult.status === 'fulfilled' && applicationResult.value.success) {
      applicationData.value = applicationResult.value.data
      console.log('Application data fetched:', applicationData.value)
    } else {
      const errorMsg = applicationResult.status === 'rejected'
        ? applicationResult.reason.message
        : applicationResult.value.message || applicationResult.value.error
      console.error('Failed to fetch application:', errorMsg)
      error.value = errorMsg || 'Failed to load application details'
      return // Don't continue if application fetch failed
    }

    // Process application type result
    if (applicationTypeResult.status === 'fulfilled' && applicationTypeResult.value.success) {
      applicationTypeData.value = applicationTypeResult.value.data
      console.log('Application type data fetched:', applicationTypeData.value)
    } else {
      const errorMsg = applicationTypeResult.status === 'rejected'
        ? applicationTypeResult.reason.message
        : applicationTypeResult.value.message
      console.error('Failed to fetch application type:', errorMsg)
      // Don't set as error since we can still display the application
    }

    // Process insurance result
    if (insuranceResult.status === 'fulfilled' && insuranceResult.value.success) {
      insuranceData.value = insuranceResult.value.data
      console.log('Insurance data fetched:', insuranceData.value)
    } else {
      const errorMsg = insuranceResult.status === 'rejected'
        ? insuranceResult.reason.message
        : insuranceResult.value.message
      console.error('Failed to fetch insurance details:', errorMsg)
      // Don't set error here as insurance might not exist yet
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
      hasError: !!error.value,
      isLoading: loading.value
    })
  }
}



const navigateToApplicationList = () => {
  // Use applicationTypeId from route params for consistent navigation
  const applicationTypeId = route.params.applicationTypeId || applicationData.value?.applicationTypeId

  if (applicationTypeId) {
    router.push({
      name: 'agriculturist-application-type',
      params: { id: applicationTypeId }
    })
  } else {
    // Fallback to application types if we don't have the type ID
    router.push({ name: 'agriculturist-submit-crop-data' })
  }
}

const getApplicationTitle = () => {
  if (applicationTypeData.value && applicationData.value?.dynamicFields) {
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

    // Try to get application type name
    const typeName = applicationTypeData.value.name || 'Application'

    if (farmerName && identifier) {
      return `${farmerName} - ${identifier}`
    } else if (farmerName) {
      return `${farmerName} - ${typeName}`
    } else if (identifier) {
      return `${identifier} - ${typeName}`
    } else {
      return `${typeName} Details`
    }
  }
  return 'Application Details'
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


// Watch for route changes to reload data when navigating between applications
watch(() => route.params.id, (newId, oldId) => {
  console.log('ApplicationDetail: Route param changed from', oldId, 'to', newId)
  if (newId && newId !== oldId) {
    // Reset state immediately
    applicationData.value = null
    applicationTypeData.value = null
    insuranceData.value = null
    error.value = null
    fetchApplicationDetails()
  }
}, { immediate: false })

// Also watch for full route changes to catch breadcrumb navigation
watch(() => route.fullPath, (newPath, oldPath) => {
  console.log('ApplicationDetail: Route path changed from', oldPath, 'to', newPath)
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
