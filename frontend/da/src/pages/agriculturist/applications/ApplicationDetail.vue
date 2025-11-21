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
                <span class="ml-4 text-sm font-medium text-black">
                  Application Details
                </span>
              </div>
            </li>
          </ol>
        </nav>

        <div class="flex items-center justify-between ml-5">
          <div>
            <h1 class="text-3xl font-bold text-green-600">Application Submission Details</h1>
            <p class="mt-1 text-sm text-gray-600">
              {{ getApplicationTitle() }}
            </p>
          </div>
          <div class="flex items-center gap-3">
            <!-- View AI Analysis Button -->
            <router-link
              v-if="shouldShowAIAnalysis && applicationData?.id && (insuranceData?.id || route.params.id)"
              :to="{ name: 'agriculturist-damage-report', params: { applicationId: route.params.id, applicationTypeId: route.params.applicationTypeId } }"
              class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 transition-colors"
            >
              <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path>
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path>
              </svg>
              View AI Analysis
            </router-link>

            <!-- View Location Map Button -->
            <router-link
              v-if="applicationData?.id && applicationData?.coordinates"
              :to="{ name: 'agriculturist-submit-crop-data-map', params: { applicationId: route.params.id } }"
              class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 transition-colors"
            >
              <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7"></path>
              </svg>
              View Location Map
            </router-link>
          </div>
        </div>
      </div>

      <!-- Main Content Area - Left column scrolls, main doesn't -->
      <div class="flex-1 min-h-0 overflow-hidden">
        <div class="h-full">
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
<div v-else-if="applicationData" :key="route.params.id" class="bg-gray-50 rounded-xl shadow-sm p-3 grid grid-cols-1 lg:grid-cols-3 gap-1 h-full">

  <!-- LEFT SIDE — Application Info (scrollable) -->
  <div
    ref="leftColumnRef"
    class="lg:col-span-2 space-y-4 min-h-0 overflow-y-auto pr-2 pl-2"
    :style="{ scrollPaddingTop: '1rem', scrollPaddingBottom: '1rem' }"
  >

<!-- USER PROFILE STYLE APPLICATION INFO -->
<div class="bg-gray-100 rounded-xl border border-gray-300 shadow-sm">
  <div class="px-4 py-4">

    <!-- Profile Fields -->
    <div
      v-if="filteredDynamicFields.length > 0"
      class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-2 gap-x-6 gap-y-3"
    >
      <div
        v-for="field in filteredDynamicFields"
        :key="field.key"
        class="flex flex-col"
      >
        <!-- Label -->
        <span class="text-[10px] font-semibold text-green-600 uppercase tracking-wider mb-1">
          {{ field.label }}
        </span>

        <!-- VALUE BOX -->
        <div
          class="w-full bg-white rounded-md px-3 py-2.5 border border-gray-200 shadow-sm hover:shadow transition-all duration-150"
        >
          <span class="text-sm text-gray-900 font-medium break-words">
            {{ field.value }}
          </span>
        </div>
      </div>
    </div>

    <!-- Empty State -->
    <div v-else class="flex flex-col items-center justify-center py-12">
      <div class="h-14 w-14 rounded-full bg-gray-200 flex items-center justify-center shadow-inner">
        <DocumentIcon class="h-7 w-7 text-gray-400" />
      </div>

      <h3 class="mt-3 text-sm font-semibold text-gray-700">
        No Information Available
      </h3>

      <p class="text-xs text-gray-500 text-center max-w-xs mt-1">
        No fields were found for this application. Once details are added,
        they will appear here in a clean, profile-style layout.
      </p>
    </div>

  </div>
</div>



<!-- Application Type Information -->
<div
  v-if="applicationTypeData"
  class="bg-gray-100 rounded-xl border border-gray-300 shadow-sm"
>
  <!-- Header -->
  <div class="px-5 py-3 border-b border-gray-300 flex items-center gap-2">
    <DocumentIcon class="w-4 h-4 text-green-600" />
    <h3 class="text-[11px] font-bold text-gray-700 uppercase tracking-wider">
      Application Type
    </h3>
  </div>

  <!-- Content -->
  <div class="px-5 py-4">
    <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-2 gap-x-6 gap-y-4">

      <!-- Name -->
      <div class="flex flex-col">
        <span class="text-[10px] font-semibold text-green-600 uppercase tracking-wide mb-1">
          Name
        </span>
        <div class="w-full bg-white rounded-md px-3 py-2.5 border border-gray-200 shadow-sm hover:shadow transition">
          <span class="text-sm text-gray-900 font-medium break-words">
            {{ applicationTypeData.name }}
          </span>
        </div>
      </div>

      <!-- Description -->
      <div class="flex flex-col">
        <span class="text-[10px] font-semibold text-green-600 uppercase tracking-wide mb-1">
          Description
        </span>
        <div class="w-full bg-white rounded-md px-3 py-2.5 border border-gray-200 shadow-sm hover:shadow transition">
          <span class="text-sm text-gray-900 font-medium break-words">
            {{ applicationTypeData.description }}
          </span>
        </div>
      </div>

    </div>
  </div>
</div>


<!-- Batch Information -->
<div
  v-if="insuranceData?.batch"
  class="bg-gray-100 rounded-xl border border-gray-300 shadow-sm"
>
  <!-- Header -->
  <div class="px-5 py-3 border-b border-gray-300 flex items-center gap-2">
    <FolderIcon class="w-4 h-4 text-green-600" />
    <h3 class="text-[11px] font-bold text-gray-700 uppercase tracking-wider">
      Batch Information
    </h3>
  </div>

  <!-- Content -->
  <div class="px-5 py-4">
    <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-2 gap-x-6 gap-y-4">

      <!-- Batch Name -->
      <div class="flex flex-col">
        <span class="text-[10px] font-semibold text-green-600 uppercase tracking-wide mb-1">
          Batch Name
        </span>
        <div class="w-full bg-white rounded-md px-3 py-2.5 border border-gray-200 shadow-sm hover:shadow transition">
          <span class="text-sm text-gray-900 font-medium break-words">
            {{ insuranceData.batch.batchName }}
          </span>
        </div>
      </div>

      <!-- Description -->
      <div class="flex flex-col">
        <span class="text-[10px] font-semibold text-green-600 uppercase tracking-wide mb-1">
          Description
        </span>
        <div class="w-full bg-white rounded-md px-3 py-2.5 border border-gray-200 shadow-sm hover:shadow transition">
          <span class="text-sm text-gray-900 font-medium break-words">
            {{ insuranceData.batch.description }}
          </span>
        </div>
      </div>

      <!-- Start Date -->
      <div class="flex flex-col">
        <span class="text-[10px] font-semibold text-green-600 uppercase tracking-wide mb-1">
          Start Date
        </span>
        <div class="w-full bg-white rounded-md px-3 py-2.5 border border-gray-200 shadow-sm hover:shadow transition">
          <span class="text-sm text-gray-900 font-medium break-words">
            {{ formatDate(insuranceData.batch.startDate) }}
          </span>
        </div>
      </div>

      <!-- End Date -->
      <div class="flex flex-col">
        <span class="text-[10px] font-semibold text-green-600 uppercase tracking-wide mb-1">
          End Date
        </span>
        <div class="w-full bg-white rounded-md px-3 py-2.5 border border-gray-200 shadow-sm hover:shadow transition">
          <span class="text-sm text-gray-900 font-medium break-words">
            {{ formatDate(insuranceData.batch.endDate) }}
          </span>
        </div>
      </div>

    </div>
  </div>
</div>


<!-- Uploaded Files (carded) --> 
<div v-if="applicationData.fileUploads?.length > 0" class="bg-gray-50 overflow-hidden">
  <div class="px-6 py-3 flex items-center gap-2">
    <ImageIcon class="w-4 h-4 text-green-700" />
    <h3 class="text-xs font-bold text-gray-700 uppercase tracking-wide">Uploaded Files</h3>
  </div>

  <div class="px-6 py-4">
    <div class="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-3">
      <button
        v-for="(file, index) in applicationData.fileUploads"
        :key="index"
        @click="openImageModal(file)"
        class="group relative overflow-hidden rounded-md bg-gray-50 hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-offset-1 focus:ring-green-300 transition"
      >
        <div class="aspect-square bg-gray-100 flex items-center justify-center">
          <img
            :src="file"
            :alt="`Uploaded file ${index + 1}`"
            class="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105"
            @error="handleImageError"
          />
        </div>

        <div class="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/50 to-transparent text-white text-xs text-center py-1">
          <span class="inline-block truncate px-2">File {{ index + 1 }}</span>
        </div>
      </button>
    </div>
  </div>
</div>

<!-- Application Metadata -->
<div ref="metadataRef" class="bg-gray-100 rounded-xl border border-gray-300 shadow-sm">
  <!-- Header -->
  <div class="px-5 py-3 border-b border-gray-300 flex items-center gap-2">
    <InformationCircleIcon class="w-4 h-4 text-green-700" />
    <h3 class="text-[11px] font-bold text-gray-700 uppercase tracking-wider">
      Application Metadata
    </h3>
  </div>

  <!-- Content -->
  <div class="px-5 py-4">
    <div class="grid grid-cols-1 md:grid-cols-3 gap-x-6 gap-y-4">

      <!-- Application ID -->
      <div class="flex flex-col">
        <span class="text-[10px] font-semibold text-green-700 uppercase tracking-wide mb-1">
          Application ID
        </span>
        <div class="w-full bg-white rounded-md px-3 py-2.5 border border-gray-200 shadow-sm hover:shadow transition">
          <span class="text-sm text-gray-900 font-medium break-words">
            {{ applicationData.id }}
          </span>
        </div>
      </div>

      <!-- Submitted At -->
      <div class="flex flex-col">
        <span class="text-[10px] font-semibold text-green-700 uppercase tracking-wide mb-1">
          Submitted At
        </span>
        <div class="w-full bg-white rounded-md px-3 py-2.5 border border-gray-200 shadow-sm hover:shadow transition">
          <span class="text-sm text-gray-900 font-medium break-words">
            {{ formatDate(applicationData.submittedAt) }}
          </span>
        </div>
      </div>

      <!-- Updated At -->
      <div class="flex flex-col">
        <span class="text-[10px] font-semibold text-green-700 uppercase tracking-wide mb-1">
          Updated At
        </span>
        <div class="w-full bg-white rounded-md px-3 py-2.5 border border-gray-200 shadow-sm hover:shadow transition">
          <span class="text-sm text-gray-900 font-medium break-words">
            {{ formatDate(applicationData.updatedAt) }}
          </span>
        </div>
      </div>

    </div>
  </div>
</div>
    </div>

  <!-- RIGHT SIDE — STACKED CARDS (scrollable) -->
  <div
    ref="rightColumnRef"
    class="space-y-4 min-h-0 overflow-y-auto pl-2"
    :style="{ scrollPaddingTop: '1rem', scrollPaddingBottom: '1rem' }"
  >

<!-- Verification Information -->
<div v-if="shouldShowVerification" :class="verificationCardClasses">

<!-- Verification header removed (display only content) -->


  <div class="px-3 py-3">

    <!-- Verification Complete -->
    <div v-if="insuranceData?.verification" class="space-y-6">

      <!-- Simple Verification Completed (small green vibe) -->
      <div class="flex items-center gap-3 p-2 rounded-xl">
        <div class="flex-shrink-0 bg-green-600 rounded-full p-1.5">
          <CheckCircleIcon class="h-6 w-6 text-white" />
        </div>

        <div>
          <p class="text-lg font-semibold text-green-600">Verified</p>
          <p class="text-xs text-gray-600">Reviewed and approved</p>
        </div>
      </div>

      <!-- Verification Details (name & date inline, no boxes) -->
      <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
        <div class="flex flex-col">
          <span :class="isVerified ? 'text-xs font-medium text-green-600' : 'text-xs font-medium text-gray-500'">Verifier</span>
          <span :class="isVerified ? 'mt-1 text-sm font-medium text-gray-700' : 'mt-1 text-sm font-medium text-gray-800'">{{ insuranceData.verification.verifierName || 'Not Available' }}</span>
        </div>

        <div class="flex flex-col">
          <span :class="isVerified ? 'text-xs font-medium text-green-600' : 'text-xs font-medium text-gray-500'">Verified At</span>
          <span :class="isVerified ? 'mt-1 text-sm font-medium text-gray-700' : 'mt-1 text-sm font-medium text-gray-800'">{{ formatDate(insuranceData.verification.verifiedAt) }}</span>
        </div>

        <div class="md:col-span-2">
          <DetailField
            label="Verification Remarks"
            :value="insuranceData.verification.remarks || 'No remarks provided'"
          />
        </div>
      </div>

      <!-- Additional Info -->
      <div
        v-if="insuranceData.verification.fieldValues"
        class="bg-yellow-100 p-4 rounded-lg border border-yellow-300"
      >
        <h5 class="text-sm font-semibold text-yellow-800 mb-1">
          Verified Field Information
        </h5>
        <p class="text-sm text-yellow-700">
          Field values were validated as part of the verification process.
        </p>
      </div>

    </div>

    <!-- Verification Not Complete -->
    <div v-else class="flex items-center justify-center py-10">
      <div class="text-center">

        <!-- Icon -->
        <div class="mx-auto h-14 w-14 flex items-center justify-center rounded-xl bg-red-100 border border-red-100">
          <ExclamationTriangleIcon class="h-7 w-7 text-red-600" />
        </div>

        <h3 class="mt-4 text-lg font-semibold text-red-600">
          Verification Pending
        </h3>
        <p class="mt-1 text-sm text-gray-800 max-w-sm mx-auto">
          This application is still waiting for an authorized verifier to review and approve it.
        </p>

        <!-- Button -->
        <router-link
          :to="{ name: 'agriculturist-application-verification', params: { 
            applicationId: applicationData.id, 
            applicationTypeId: route.params.applicationTypeId 
          } }"
          class="mt-5 inline-flex items-center px-5 py-2.5 rounded-lg shadow bg-green-600 text-white font-medium 
                 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-600
                 transition-all"
        >
          Start Verification
        </router-link>

      </div>
    </div>

  </div>
</div>


<!-- Inspection Information -->
<div
  v-if="shouldShowInspection"
  class="bg-gray-100 shadow-sm border border-gray-300 rounded-xl overflow-hidden"
>

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
                  <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-yellow-100">
                    <DocumentIcon class="h-6 w-6 text-yellow-600" />
                  </div>
                  <h3 class="mt-2 text-sm font-medium text-gray-900">Inspection Not Scheduled</h3>
                  <p class="mt-1 text-sm text-gray-800">Field inspection has not been scheduled for this application yet. An inspector will be assigned soon.</p>
                </div>
              </div>
            </div>
          </div>

          <!-- Policy Information -->
          <div v-if="shouldShowPolicy" class="bg-gray-100 shadow-sm border border-gray-300 rounded-lg overflow-hidden">
            <div class="px-6 py-4 border-b border-gray-300">
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
                  <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-red-100">
                    <DocumentIcon class="h-6 w-6 text-red-600" />
                  </div>
                  <h3 class="mt-2 text-sm font-medium text-gray-900">Policy Not Issued Yet</h3>
                  <p class="mt-1 text-sm text-gray-500">The insurance policy for this application has not been issued yet. Please wait for policy generation.</p>
                </div>
              </div>
            </div>
          </div>

            <!-- Claim Information -->
            <div v-if="shouldShowClaim" class="bg-gray-100 shadow-sm border border-gray-300 rounded-xl overflow-hidden">
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
                    <div class="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-gray-300">
                      <DocumentIcon class="h-6 w-6 text-gray-500" />
                    </div>
                    <h3 class="mt-2 text-sm font-medium text-gray-900">No Claim Filed Yet</h3>
                    <p class="mt-1 text-sm text-gray-800">No insurance claim has been filed for this application yet. Claims can be filed when crop damage occurs.</p>
                  </div>
                </div>
              </div>
            </div>
        </div>

        <!-- No Data State - Fallback when data is not loading, no error, but no applicationData -->
        <div v-if="!applicationData && !loading && !error" class="flex justify-center items-center flex-1 min-h-96">
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
    </div>
  </AuthenticatedLayout>
</template>

<script setup>
import { ref, computed, onMounted, watch, nextTick } from 'vue'
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
  CheckCircleIcon,
  ShieldCheckIcon,
  ClipboardDocumentCheckIcon,
  ClipboardIcon,
  InformationCircleIcon
} from '@heroicons/vue/24/outline'
import { ImageIcon } from "lucide-vue-next";


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
const leftColumnRef = ref(null)
const metadataRef = ref(null)
const rightColumnRef = ref(null)

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
    // Ensure metadata is visible after loading, but skip automatic scroll on full page reloads
    if (applicationData.value) {
      let isReload = false
      try {
        // Prefer Navigation Timing Level 2
        const navEntries = performance.getEntriesByType && performance.getEntriesByType('navigation')
        if (navEntries && navEntries.length > 0) {
          isReload = navEntries[0].type === 'reload'
        } else if (performance.navigation && performance.navigation.type !== undefined) {
          // Fallback for older browsers: type === 1 indicates reload
          isReload = performance.navigation.type === 1
        }
      } catch (e) {
        // If any error, assume not a reload so behavior is unchanged
        isReload = false
      }

      if (!isReload) {
        scrollMetadataIntoView()
      }
    }
  }
}

const scrollMetadataIntoView = (smooth = true) => {
  // If a dedicated metadata element exists, scroll it into view; otherwise scroll to the bottom of the left column
  nextTick(() => {
    try {
      if (metadataRef.value && metadataRef.value.scrollIntoView) {
        metadataRef.value.scrollIntoView({ behavior: smooth ? 'smooth' : 'auto', block: 'end', inline: 'nearest' })
        return
      }

      if (leftColumnRef.value) {
        const el = leftColumnRef.value
        el.scrollTop = el.scrollHeight
      }
    } catch (e) {
      if (leftColumnRef.value) leftColumnRef.value.scrollTop = leftColumnRef.value.scrollHeight
    }
  })
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

// Classes for verification card depending on verification state
const verificationCardClasses = computed(() => {
  const verified = !!insuranceData.value?.verification
  return [
    'rounded-xl overflow-hidden',
    verified ? 'border border-green-500 bg-green-100 text-white' : 'border border-gray-300 bg-red-200 text-green-600'
  ].join(' ')
})

const verificationHeaderClasses = computed(() => {
  const verified = !!insuranceData.value?.verification
  return [
    'px-6 py-4 flex items-center gap-3',
    verified ? 'border-b border-green-200 bg-green-200 text-gray-800' : 'border-b border-gray-300 bg-white text-gray-800'
  ].join(' ')
})

const isVerified = computed(() => {
  return !!insuranceData.value?.verification
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
