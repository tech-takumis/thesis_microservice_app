<template>
  <AuthenticatedLayout
    :navigation="underwriterNavigation"
    role-title="Underwriter Portal"
    page-title="Application Details"
  >
    <template #header>
      <div class="space-y-4">
        <!-- Breadcrumb Navigation -->
        <nav class="flex" aria-label="Breadcrumb">
          <ol class="flex items-center space-x-1.5">
            <li>
              <router-link
                :to="{ name: 'underwriter-dashboard' }"
                class="text-slate-400 hover:text-slate-700 transition-colors duration-200"
              >
                <HomeIcon class="h-4 w-4" />
              </router-link>
            </li>
            <li class="flex items-center">
              <ChevronRightIcon class="h-3 w-3 text-slate-300 mx-1" />
              <button
                class="text-xs font-medium text-slate-500 hover:text-slate-700 transition-colors duration-200"
                @click="navigateToApplicationList"
              >
                Applications
              </button>
            </li>
            <li class="flex items-center">
              <ChevronRightIcon class="h-3 w-3 text-slate-300 mx-1" />
              <span class="text-xs font-medium text-slate-900">Details</span>
            </li>
          </ol>
        </nav>

        <!-- Page Header -->
        <div class="flex items-start justify-between">
          <div>
            <h1 class="text-2xl font-light text-slate-900 tracking-tight">{{ getApplicationTitle() }}</h1>
            <div v-if="insuranceData" class="mt-3 flex items-center gap-3">
              <span
                :class="[
                  'inline-flex items-center px-3 py-1 rounded-lg text-xs font-medium',
                  insuranceData.status === 'APPROVED'
                    ? 'bg-emerald-100 text-emerald-700'
                    : insuranceData.status === 'PENDING'
                    ? 'bg-amber-100 text-amber-700'
                    : 'bg-red-100 text-red-700',
                ]"
              >
                {{ insuranceData.status }}
              </span>
              <span class="text-xs text-slate-500 font-medium">
                ID: {{ insuranceData.insuranceId?.substring(0, 13) }}...
              </span>
            </div>
          </div>
          <div class="flex items-center gap-2">
             <router-link
              v-if="applicationData?.coordinates"
              :to="{ name: 'agriculturist-submit-crop-data-map', params: { applicationId: route.params.submissionId } }"
              class="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 transition-colors"
            >
              <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7"></path>
              </svg>
              View Location Map
            </router-link>
            <button
              v-if="insuranceData?.batch"
              @click="openBatchModal"
              class="inline-flex items-center p-2 text-slate-400 hover:text-blue-600 hover:bg-blue-50/50 rounded-full transition-all duration-200"
              title="View Batch Information"
            >
              <InformationCircleIcon class="size-6" />
            </button>
          </div>
        </div>
      </div>
    </template>

      <!-- Loading State -->
      <div v-if="loading" class="flex justify-center items-center py-20">
        <LoadingSpinner />
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="bg-red-50/50 border border-red-200/60 rounded-xl p-5 backdrop-blur-sm">
        <div class="flex">
          <ExclamationTriangleIcon class="h-5 w-5 text-red-500 mt-0.5 flex-shrink-0" />
          <div class="ml-3">
            <h3 class="text-sm font-medium text-red-900">Error Loading Details</h3>
            <p class="mt-1 text-sm text-red-700/80">{{ error }}</p>
          </div>
        </div>
      </div>

      <!-- Main Content Grid -->
      <div v-else-if="insuranceData" :key="route.params.insuranceId">
        <!-- Two Column Layout: 7-5 ratio -->
        <div class="grid grid-cols-12 gap-6">
          <!-- Left Column: Main Content (7 columns) -->
          <div class="col-span-12 lg:col-span-7 space-y-6">
            <!-- Application Information -->
            <div class="bg-white/70 backdrop-blur-sm rounded-2xl border border-slate-200/60 shadow-sm overflow-hidden">
              <div class="px-6 py-4 border-b border-slate-100/80 flex items-center justify-between">
                <h3 class="text-sm font-medium text-slate-700">Application Information</h3>
                <button
                  v-if="shouldShowAIAnalysis"
                  @click="viewAIAnalysis"
                  class="inline-flex items-center px-3 py-1.5 text-xs font-medium rounded-lg text-purple-600 bg-purple-50 border border-purple-200 hover:bg-purple-100 hover:border-purple-300 transition-all duration-200"
                >
                  <svg class="w-3 h-3 mr-1" fill="currentColor" viewBox="0 0 20 20">
                    <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
                  </svg>
                  AI Analysis
                </button>
              </div>
              <div class="p-6">
                <dl v-if="filteredDynamicFields.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
                  <div v-for="field in filteredDynamicFields" :key="field.key">
                    <DetailField :label="field.label" :value="field.value" />
                  </div>
                </dl>
                <div v-else class="text-center py-12">
                  <DocumentIcon class="mx-auto h-12 w-12 text-slate-300" />
                  <p class="mt-3 text-sm text-slate-500">No application information available</p>
                </div>
              </div>
            </div>


            <!-- Verification Documents -->
            <div v-if="insuranceData?.verification?.verificationDocuments?.length > 0" class="bg-white/70 backdrop-blur-sm rounded-2xl border border-slate-200/60 shadow-sm overflow-hidden">
              <div class="px-6 py-4 border-b border-slate-100/80">
                <h3 class="text-sm font-medium text-slate-700">Documents</h3>
              </div>
              <div class="p-6">
                <div class="grid grid-cols-3 md:grid-cols-5 gap-3">
                  <div
                    v-for="(file, index) in insuranceData.verification.verificationDocuments"
                    :key="index"
                    class="group relative aspect-square rounded-xl overflow-hidden bg-slate-100 cursor-pointer border border-slate-200 hover:border-blue-400 hover:shadow-md transition-all duration-200"
                    @click="openImageModal(file)"
                  >
                    <img
                      :src="file"
                      :alt="`Doc ${index + 1}`"
                      class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-200"
                      @error="handleImageError"
                    />
                    <div class="absolute bottom-0 inset-x-0 bg-gradient-to-t from-black/70 to-transparent text-white text-xs py-1.5 text-center font-medium">
                      {{ index + 1 }}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <div class="col-span-12 lg:col-span-5 space-y-4">
            <!-- Verification Card -->
            <div v-if="shouldShowVerification" class="bg-white/70 backdrop-blur-sm rounded-2xl border border-slate-200/60 shadow-sm overflow-hidden">
              <div class="p-5">
                <div v-if="insuranceData?.verification" class="space-y-3">
                  <div class="flex items-center justify-between">
                    <div class="flex items-center gap-2">
                      <CheckCircleIcon class="h-5 w-5 text-emerald-500 flex-shrink-0" />
                      <h4 class="text-sm font-semibold text-slate-900">Verified</h4>
                    </div>
                  </div>
                  <div class="space-y-2 text-xs ml-7">
                    <p class="text-slate-600">
                      <span class="font-semibold">By:</span> {{ insuranceData.verification.verifierName }}
                    </p>
                    <p class="text-slate-600">
                      <span class="font-semibold">Date:</span> {{ formatDate(insuranceData.verification.verifiedAt) }}
                    </p>
                    <p v-if="insuranceData.verification.remarks" class="text-slate-600">
                      <span class="font-semibold">Remarks:</span> {{ insuranceData.verification.remarks }}
                    </p>
                  </div>
                </div>
                <div v-else class="text-center py-6">
                  <ExclamationTriangleIcon class="mx-auto h-8 w-8 text-amber-400" />
                  <p class="mt-2 text-xs font-medium text-slate-700">Pending Verification</p>
                </div>
              </div>
            </div>

            <!-- Inspection Card -->
            <div v-if="shouldShowInspection" class="bg-white/70 backdrop-blur-sm rounded-2xl border border-slate-200/60 shadow-sm overflow-hidden">
              <div class="p-5">
                <!-- Completed Inspection (inspected = true) -->
                <div v-if="insuranceData?.inspection && insuranceData.inspection.inspected === true" class="space-y-3">
                  <div class="flex items-center justify-between">
                    <div class="flex items-center gap-2">
                      <CheckCircleIcon class="h-5 w-5 text-emerald-500 flex-shrink-0" />
                      <h4 class="text-sm font-semibold text-slate-900">Inspection Completed</h4>
                    </div>
                    <router-link
                      :to="{
                        name: 'application-inspection',
                        params: { insuranceId: route.params.insuranceId },
                        query: { action: 'view' }
                      }"
                      class="text-xs text-blue-600 hover:text-blue-800 font-medium transition-colors duration-200"
                    >
                      View
                    </router-link>
                  </div>
                  <div class="space-y-2 text-xs ml-7">
                    <p class="text-slate-600">
                      <span class="font-semibold">Inspector:</span> {{ insuranceData.inspection.inspectorName }}
                    </p>
                    <p class="text-slate-600">
                      <span class="font-semibold">Date:</span> {{ formatDate(insuranceData.inspection.inspectedAt) }}
                    </p>
                  </div>
                </div>
                
                <div v-else-if="insuranceData?.inspection && insuranceData.inspection.schedule && insuranceData.inspection.inspected === false" class="space-y-3">
                  <div class="flex items-center gap-2">
                    <svg class="h-5 w-5 text-blue-500 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z"></path>
                    </svg>
                    <h4 class="text-sm font-semibold text-slate-900">Scheduled</h4>
                  </div>
                  <div class="space-y-2 text-xs ml-7">
                    <p class="text-slate-600">
                      <span class="font-semibold">Date:</span> {{ formatDate(insuranceData.inspection.schedule.scheduleDate) }}
                    </p>
                  </div>
                  <button
                    @click="proceedInspection"
                    class="w-full mt-2 inline-flex justify-center items-center px-4 py-2.5 text-sm font-medium rounded-xl text-white bg-blue-500 hover:bg-blue-600 hover:shadow-lg hover:shadow-blue-500/30 transition-all duration-200"
                  >
                    Proceed
                  </button>
                </div>
                <!-- Not Scheduled -->
                <div v-else class="text-center py-6">
                  <DocumentIcon class="mx-auto h-8 w-8 text-slate-300" />
                  <p class="mt-2 text-xs font-medium text-slate-700">No Schedule</p>
                  <button
                    @click="scheduleInspection"
                    class="mt-3 inline-flex items-center px-4 py-2 text-xs font-medium rounded-xl text-white bg-indigo-500 hover:bg-indigo-600 hover:shadow-lg hover:shadow-indigo-500/30 transition-all duration-200"
                  >
                    Schedule
                  </button>
                </div>
              </div>
            </div>

            <!-- Policy Card -->
            <div v-if="shouldShowPolicy" class="bg-white/70 backdrop-blur-sm rounded-2xl border border-slate-200/60 shadow-sm overflow-hidden">
              <div class="p-5">
                <div v-if="insuranceData?.policy" class="space-y-3">
                  <div class="flex items-center justify-between">
                    <div class="flex items-center gap-2">
                      <CheckCircleIcon class="h-5 w-5 text-purple-500 flex-shrink-0" />
                      <h4 class="text-sm font-semibold text-slate-900">Policy Issued</h4>
                    </div>
                  </div>
                  <div class="space-y-2 text-xs ml-7">
                    <p class="text-slate-600">
                      <span class="font-semibold">Number:</span> {{ insuranceData.policy.policyNumber }}
                    </p>
                    <p class="text-slate-600">
                      <span class="font-semibold">Effective:</span> {{ formatDate(insuranceData.policy.effectiveDate) }}
                    </p>
                    <p class="text-slate-600">
                      <span class="font-semibold">Expiry:</span> {{ formatDate(insuranceData.policy.expiryDate) }}
                    </p>
                  </div>
                </div>
                <div v-else class="text-center py-6">
                  <DocumentIcon class="mx-auto h-8 w-8 text-slate-300" />
                  <p class="mt-2 text-xs font-medium text-slate-700">No Policy</p>
                  <button
                    @click="openPolicyModal"
                    :disabled="isGeneratingPolicy"
                    class="mt-3 inline-flex items-center px-4 py-2 text-xs font-medium rounded-xl text-white bg-purple-500 hover:bg-purple-600 hover:shadow-lg hover:shadow-purple-500/30 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    <span v-if="!isGeneratingPolicy">Generate</span>
                    <span v-else class="flex items-center">
                      <svg class="animate-spin h-3 w-3 mr-1" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                        <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                        <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                      </svg>
                      Generating...
                    </span>
                  </button>
                </div>
              </div>
            </div>

            <!-- Claim Card -->
            <div v-if="shouldShowClaim" class="bg-white/70 backdrop-blur-sm rounded-2xl border border-slate-200/60 shadow-sm overflow-hidden">
              <div class="p-5">
                <div v-if="insuranceData?.claim" class="space-y-3">
                  <div class="flex items-center justify-between">
                    <div class="flex items-center gap-2">
                      <CheckCircleIcon class="h-5 w-5 text-orange-500 flex-shrink-0" />
                      <h4 class="text-sm font-semibold text-slate-900">Claim Filed</h4>
                    </div>
                    <div class="flex items-center gap-1">
                      <router-link
                        :to="{
                          name: 'underwriter-applications-claim',
                          params: { insuranceId: route.params.insuranceId, submissionId: route.params.submissionId },
                          query: { action: 'view' }
                        }"
                        class="text-xs text-blue-600 hover:text-blue-800 font-medium transition-colors duration-200 px-2 py-1 rounded hover:bg-blue-50"
                      >
                        View
                      </router-link>
                      <router-link
                        :to="{
                          name: 'underwriter-applications-claim',
                          params: { insuranceId: route.params.insuranceId, submissionId: route.params.submissionId },
                          query: { action: 'update' }
                        }"
                        class="text-xs text-green-600 hover:text-green-800 font-medium transition-colors duration-200 px-2 py-1 rounded hover:bg-green-50"
                      >
                        Update
                      </router-link>
                    </div>
                  </div>
                  <div class="space-y-2 text-xs ml-7">
                    <p class="text-slate-600">
                      <span class="font-semibold">Amount:</span> {{ formatCurrency(insuranceData.claim.claimAmount) }}
                    </p>
                    <p class="text-slate-600">
                      <span class="font-semibold">Status:</span> {{ insuranceData.status}}
                    </p>
                    <p class="text-slate-600">
                      <span class="font-semibold">Filed:</span> {{ formatDate(insuranceData.claim.filedAt) }}
                    </p>
                  </div>
                </div>
                <div v-else class="text-center py-6">
                  <DocumentIcon class="mx-auto h-8 w-8 text-slate-300" />
                  <p class="mt-2 text-xs font-medium text-slate-700">No Claim</p>
                  <div class="mt-3 space-y-2">
                    <router-link
                      v-if="insuranceData?.inspection && insuranceData.inspection.inspected === true"
                      :to="{
                        name: 'underwriter-applications-claim',
                        params: { insuranceId: route.params.insuranceId, submissionId: route.params.submissionId },
                        query: { action: 'file_claim' }
                      }"
                      class="inline-flex items-center px-4 py-2 text-xs font-medium rounded-xl text-white bg-orange-500 hover:bg-orange-600 hover:shadow-lg hover:shadow-orange-500/30 transition-all duration-200"
                    >
                      File Claim
                    </router-link>
                  </div>
                  <p v-if="!insuranceData?.inspection || !insuranceData.inspection.inspected" class="mt-3 text-xs text-slate-400">
                    {{ insuranceData?.inspection && insuranceData.inspection.schedule ? 'Awaiting completion' : 'Awaiting inspection' }}
                  </p>
                </div>
              </div>
            </div>
          </div>

        </div>
      </div>

      <!-- No Data State -->
      <div v-else class="text-center py-20">
        <DocumentIcon class="mx-auto h-16 w-16 text-slate-300" />
        <h3 class="mt-4 text-sm font-medium text-slate-900">No Data Available</h3>
        <p class="mt-1 text-sm text-slate-500">Unable to load insurance details.</p>
        <button
          @click="fetchApplicationDetails"
          class="mt-6 inline-flex items-center px-5 py-2.5 border border-transparent text-sm font-medium rounded-xl text-white bg-blue-500 hover:bg-blue-600 hover:shadow-lg hover:shadow-blue-500/30 transition-all duration-200"
        >
          Try Again
        </button>
      </div>

      <!-- Image Modal -->
      <div
        v-if="selectedImage"
        class="fixed inset-0 z-50 flex items-center justify-center bg-black/90 backdrop-blur-md p-4"
        @click="closeImageModal"
      >
        <button
          class="absolute top-6 right-6 p-2.5 rounded-full bg-white/10 hover:bg-white/20 text-white transition-all duration-200"
          @click="closeImageModal"
        >
          <XMarkIcon class="h-6 w-6" />
        </button>
        <img
          :src="selectedImage"
          alt="Full size"
          class="max-w-full max-h-full object-contain rounded-2xl shadow-2xl"
          @click.stop
        />
      </div>

      <!-- Batch Information Modal -->
      <Transition name="fade">
        <div
          v-if="isBatchModalOpen"
          @click="closeBatchModal"
          class="fixed inset-0 bg-black/30 backdrop-blur-sm z-40"
        ></div>
      </Transition>

      <Transition name="slide-right">
        <div
          v-if="isBatchModalOpen && insuranceData?.batch"
          class="fixed inset-y-0 right-0 z-50 w-96 bg-white shadow-2xl overflow-y-auto"
        >
          <!-- Modal Header -->
          <div class="sticky top-0 bg-white border-b border-slate-200 px-6 py-5">
            <div class="flex items-center justify-between">
              <h3 class="text-lg font-medium text-slate-900">Batch Information</h3>
              <button
                @click="closeBatchModal"
                class="text-slate-400 hover:text-slate-600 hover:bg-slate-100 p-1.5 rounded-lg transition-all duration-200"
              >
                <XMarkIcon class="h-5 w-5" />
              </button>
            </div>
          </div>

          <!-- Modal Body -->
          <div class="p-6 space-y-5">
            <div>
              <dt class="text-xs font-medium text-slate-500 uppercase tracking-wide">Batch ID</dt>
              <dd class="mt-1.5 text-sm text-slate-900 break-all">{{ insuranceData.batch.id }}</dd>
            </div>
            <div>
              <dt class="text-xs font-medium text-slate-500 uppercase tracking-wide">Batch Name</dt>
              <dd class="mt-1.5 text-sm text-slate-900">{{ insuranceData.batch.batchName }}</dd>
            </div>
            <div>
              <dt class="text-xs font-medium text-slate-500 uppercase tracking-wide">Description</dt>
              <dd class="mt-1.5 text-sm text-slate-900">{{ insuranceData.batch.description }}</dd>
            </div>
            <div>
              <dt class="text-xs font-medium text-slate-500 uppercase tracking-wide">Total Applications</dt>
              <dd class="mt-1.5 text-sm text-slate-900">{{ insuranceData.batch.totalApplications }} / {{ insuranceData.batch.maxApplications }}</dd>
            </div>
            <div>
              <dt class="text-xs font-medium text-slate-500 uppercase tracking-wide">Start Date</dt>
              <dd class="mt-1.5 text-sm text-slate-900">{{ formatDate(insuranceData.batch.startDate) }}</dd>
            </div>
            <div>
              <dt class="text-xs font-medium text-slate-500 uppercase tracking-wide">End Date</dt>
              <dd class="mt-1.5 text-sm text-slate-900">{{ formatDate(insuranceData.batch.endDate) }}</dd>
            </div>
            <div>
              <dt class="text-xs font-medium text-slate-500 uppercase tracking-wide">Status</dt>
              <dd class="mt-1.5 text-sm">
                <span :class="[
                  'inline-flex items-center px-2.5 py-1 rounded-lg text-xs font-medium',
                  insuranceData.batch.available ? 'bg-emerald-100 text-emerald-700' : 'bg-red-100 text-red-700'
                ]">
                  {{ insuranceData.batch.available ? 'Available' : 'Closed' }}
                </span>
              </dd>
            </div>
          </div>
        </div>
      </Transition>

      <!-- Policy Generation Modal -->
      <Transition name="fade">
        <div
          v-if="isPolicyModalOpen"
          @click="closePolicyModal"
          class="fixed inset-0 bg-black/50 backdrop-blur-sm z-40"
        ></div>
      </Transition>

      <Transition name="modal">
        <div
          v-if="isPolicyModalOpen"
          class="fixed inset-0 z-50 flex items-center justify-center p-4"
          @click="closePolicyModal"
        >
          <div
            class="bg-white rounded-2xl shadow-2xl w-full max-w-md"
            @click.stop
          >
            <!-- Modal Header -->
            <div class="px-6 py-5 border-b border-slate-200">
              <div class="flex items-center justify-between">
                <h3 class="text-lg font-semibold text-slate-900">Generate Policy</h3>
                <button
                  @click="closePolicyModal"
                  class="text-slate-400 hover:text-slate-600 hover:bg-slate-100 p-1.5 rounded-lg transition-all duration-200"
                >
                  <XMarkIcon class="h-5 w-5" />
                </button>
              </div>
              <p class="mt-1 text-sm text-slate-500">
                Set the effective and expiry dates for the insurance policy
              </p>
            </div>

            <!-- Modal Body -->
            <div class="p-6 space-y-5">
              <div>
                <label for="effectiveDate" class="block text-sm font-medium text-slate-700 mb-2">
                  Effective Date <span class="text-red-500">*</span>
                </label>
                <input
                  id="effectiveDate"
                  type="date"
                  v-model="policyForm.effectiveDate"
                  class="w-full px-4 py-3 rounded-xl border-2 border-slate-200 bg-white text-slate-900 text-sm font-medium focus:border-purple-400 focus:bg-purple-50/30 focus:outline-none focus:ring-4 focus:ring-purple-400/10 hover:border-slate-300 transition-all duration-200"
                  required
                />
              </div>

              <div>
                <label for="expiryDate" class="block text-sm font-medium text-slate-700 mb-2">
                  Expiry Date <span class="text-red-500">*</span>
                </label>
                <input
                  id="expiryDate"
                  type="date"
                  v-model="policyForm.expiryDate"
                  class="w-full px-4 py-3 rounded-xl border-2 border-slate-200 bg-white text-slate-900 text-sm font-medium focus:border-purple-400 focus:bg-purple-50/30 focus:outline-none focus:ring-4 focus:ring-purple-400/10 hover:border-slate-300 transition-all duration-200"
                  required
                />
              </div>
            </div>

            <!-- Modal Footer -->
            <div class="px-6 py-4 border-t border-slate-200 bg-slate-50/50 rounded-b-2xl">
              <div class="flex items-center justify-end gap-3">
                <button
                  @click="closePolicyModal"
                  class="px-4 py-2.5 text-sm font-medium text-slate-700 bg-white border border-slate-300 rounded-xl hover:bg-slate-50 hover:border-slate-400 transition-all duration-200"
                >
                  Cancel
                </button>
                <button
                  @click="generatePolicy"
                  :disabled="isGeneratingPolicy || !policyForm.effectiveDate || !policyForm.expiryDate"
                  class="px-6 py-2.5 text-sm font-medium text-white bg-purple-500 border border-purple-500 rounded-xl hover:bg-purple-600 hover:shadow-lg hover:shadow-purple-500/30 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:shadow-none"
                >
                  <span v-if="!isGeneratingPolicy">Generate Policy</span>
                  <span v-else class="flex items-center">
                    <svg class="animate-spin h-4 w-4 mr-2" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                      <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    Generating...
                  </span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </Transition>

      <!-- Schedule Inspection Modal -->
      <ScheduleInspectionModal
        :is-open="isScheduleModalOpen"
        :insurance-id="route.params.insuranceId"
        @close="closeScheduleModal"
        @success="handleScheduleSuccess"
      />
  </AuthenticatedLayout>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useApplicationStore } from '@/stores/application'
import { useInsuranceStore } from '@/stores/insurance'
import { usePolicyStore } from '@/stores/policy'
import { useClaimStore } from '@/stores/claim'
import { useToastStore } from '@/stores/toast'
import { UNDERWRITER_NAVIGATION } from '@/lib/navigation'
import AuthenticatedLayout from '@/layouts/AuthenticatedLayout.vue'
import DetailField from '@/components/tables/DetailField.vue'
import LoadingSpinner from '@/components/others/LoadingSpinner.vue'
import ScheduleInspectionModal from '@/components/modals/ScheduleInspectionModal.vue'
import {
  HomeIcon,
  ChevronRightIcon,
  ExclamationTriangleIcon,
  DocumentIcon,
  XMarkIcon,
  CheckCircleIcon,
  InformationCircleIcon
} from '@heroicons/vue/24/outline'

// Composables
const route = useRoute()
const router = useRouter()
const applicationStore = useApplicationStore()
const insuranceStore = useInsuranceStore()
const policyStore = usePolicyStore()
const claimStore = useClaimStore()
const toastStore = useToastStore()
const underwriterNavigation = UNDERWRITER_NAVIGATION

// State
const loading = ref(false)
const error = ref(null)
const applicationData = ref(null)
const applicationTypeData = ref(null)
const insuranceData = ref(null)
const workflowData = ref(null)
const selectedImage = ref(null)
const isScheduleModalOpen = ref(false)
const isBatchModalOpen = ref(false)
const isPolicyModalOpen = ref(false)
const policyForm = ref({
  effectiveDate: '',
  expiryDate: ''
})
const isGeneratingPolicy = ref(false)
const aiAnalysisRequired = ref(null)
const isUpdateClaimModalOpen = ref(false)
const updateClaimForm = ref({
  claimAmount: 0,
  damageAssessment: ''
})
const updateClaimFiles = ref([])
const isUpdatingClaim = ref(false)
const currentClaim = ref(null)

// Function to check if AI analysis is required for this application
const checkAiAnalysisRequirement = async (submissionId) => {
  try {
    const result = await applicationStore.isAiAnalysisRequired(submissionId)
    if (result.success) {
      aiAnalysisRequired.value = result.data
      console.log('AI Analysis requirement check:', result.data)
    } else {
      console.warn('Failed to check AI analysis requirement:', result.message)
      aiAnalysisRequired.value = false
    }
  } catch (error) {
    console.error('Error checking AI analysis requirement:', error)
    aiAnalysisRequired.value = false
  }
}

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

    // Fetch insurance data, application data, and workflow in parallel
    const [insuranceResult, applicationResult, workflowResult] = await Promise.allSettled([
      insuranceStore.fetchInsuranceById(route.params.insuranceId),
      applicationStore.fetchApplicationById(route.params.submissionId),
      applicationStore.fetchApplicationWorkflow(route.params.submissionId)
    ])

    // Check AI analysis requirement after initial fetch
    await checkAiAnalysisRequirement(route.params.submissionId)

    // Process insurance result
    if (insuranceResult.status === 'fulfilled' && insuranceResult.value.success) {
      insuranceData.value = insuranceResult.value.data
      console.log('Insurance data fetched:', insuranceData.value)
    } else {
      const errorMsg = insuranceResult.status === 'rejected'
        ? insuranceResult.reason.message
        : insuranceResult.value.message
      console.error('Failed to fetch insurance:', errorMsg)
      error.value = errorMsg || 'Failed to load insurance details'
      return // Don't continue if insurance fetch failed
    }

    // Process application result
    if (applicationResult.status === 'fulfilled' && applicationResult.value.success) {
      applicationData.value = applicationResult.value.data
      console.log('Application data fetched:', applicationData.value)
      console.log('Coordinates found:', applicationData.value?.coordinates)
    } else {
      const errorMsg = applicationResult.status === 'rejected'
        ? applicationResult.reason.message
        : applicationResult.value.message
      console.error('Failed to fetch application:', errorMsg)
      // Don't set as error since we can still display the insurance
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
      hasCoordinates: !!applicationData.value?.coordinates,
      coordinates: applicationData.value?.coordinates,
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

// Check if AI analysis should be shown based on backend requirement check
const shouldShowAIAnalysis = computed(() => {
  return applicationData.value !== null && aiAnalysisRequired.value === true
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
  isScheduleModalOpen.value = true
}

const closeScheduleModal = () => {
  isScheduleModalOpen.value = false
}

const handleScheduleSuccess = async (scheduleData) => {
  console.log('Inspection scheduled successfully:', scheduleData)

  // Show success toast
  toastStore.success('Inspection scheduled successfully!')

  // Refetch the insurance data to get updated inspection info
  await fetchApplicationDetails()
}

const openBatchModal = () => {
  isBatchModalOpen.value = true
}

const closeBatchModal = () => {
  isBatchModalOpen.value = false
}

const proceedInspection = () => {
  if (insuranceData.value?.insuranceId) {
    router.push({
      path: `/underwriter/application/${insuranceData.value.insuranceId}/inspection`
    })
  } else {
    alert('Cannot proceed to inspection: Insurance ID not found.')
  }
}

// Policy modal functions
const openPolicyModal = () => {
  if (!insuranceData.value?.verification) {
    toastStore.error('Cannot generate policy: Application must be verified first.')
    return
  }
  
  // Set default dates
  const today = new Date()
  const nextYear = new Date(today)
  nextYear.setFullYear(today.getFullYear() + 1)
  
  policyForm.value.effectiveDate = today.toISOString().split('T')[0]
  policyForm.value.expiryDate = nextYear.toISOString().split('T')[0]
  
  isPolicyModalOpen.value = true
}

const closePolicyModal = () => {
  isPolicyModalOpen.value = false
  policyForm.value.effectiveDate = ''
  policyForm.value.expiryDate = ''
}

const generatePolicy = async () => {
  if (!policyForm.value.effectiveDate || !policyForm.value.expiryDate) {
    toastStore.error('Please select both effective date and expiry date')
    return
  }
  
  if (new Date(policyForm.value.effectiveDate) >= new Date(policyForm.value.expiryDate)) {
    toastStore.error('Expiry date must be after effective date')
    return
  }
  
  try {
    isGeneratingPolicy.value = true
    
    const policyRequest = {
      insuranceId: insuranceData.value.insuranceId,
      effectiveDate: new Date(policyForm.value.effectiveDate).toISOString(),
      expiryDate: new Date(policyForm.value.expiryDate).toISOString()
    }
    
    const result = await policyStore.createPolicy(policyRequest)
    
    if (result.success) {
      // Update the insurance data with the new policy
      insuranceData.value.policy = result.data
      
      toastStore.success(`Policy generated successfully! Policy Number: ${result.data.policyNumber}`)
      closePolicyModal()
    } else {
      toastStore.error(result.message || 'Failed to generate policy')
    }
  } catch (error) {
    console.error('Error generating policy:', error)
    toastStore.error('An error occurred while generating the policy')
  } finally {
    isGeneratingPolicy.value = false
  }
}

const processClaim = () => {
  // Navigate to application claim page
  if (insuranceData.value?.insuranceId) {
    router.push({
      name: 'underwriter-applications-claim',
      params: { 
        insuranceId: insuranceData.value.insuranceId,
        submissionId: route.params.submissionId
      },
      query: { action: 'file_claim' }
    })
  } else {
    toastStore.error('Cannot process claim: Insurance ID not found.')
  }
}

const viewAIAnalysis = () => {
  if (insuranceData.value?.insuranceId && route.params.submissionId) {
    router.push({
      name: 'damage-claim-review',
      params: {
        insuranceId: insuranceData.value.insuranceId,
        submissionId: route.params.submissionId
      }
    })
  } else {
    toastStore.error('Required information not found')
  }
}

// Update claim modal functions
const openUpdateClaimModal = async () => {
  if (!insuranceData.value?.insuranceId) {
    toastStore.error('Insurance ID not found')
    return
  }

  try {
    // Fetch the current claim data
    const result = await claimStore.getClaimsByInsuranceId(insuranceData.value.insuranceId)
    
    if (result.success && result.data) {
      currentClaim.value = result.data
      
      // Populate form with current values
      updateClaimForm.value = {
        claimAmount: result.data.claimAmount || 0,
        damageAssessment: result.data.damageAssessment || ''
      }
      
      // Clear any previous files
      updateClaimFiles.value = []
      
      isUpdateClaimModalOpen.value = true
    } else {
      toastStore.error(result.message || 'Failed to fetch claim data')
    }
  } catch (error) {
    console.error('Error fetching claim for update:', error)
    toastStore.error('An error occurred while fetching claim data')
  }
}

const closeUpdateClaimModal = () => {
  isUpdateClaimModalOpen.value = false
  updateClaimForm.value = {
    claimAmount: 0,
    damageAssessment: ''
  }
  updateClaimFiles.value = []
  currentClaim.value = null
}

const handleUpdateClaimFileSelect = (event) => {
  const files = Array.from(event.target.files)
  updateClaimFiles.value = [...updateClaimFiles.value, ...files]
}

const removeUpdateClaimFile = (index) => {
  updateClaimFiles.value.splice(index, 1)
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes'
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return parseFloat((bytes / Math.pow(1024, i)).toFixed(2)) + ' ' + sizes[i]
}

const submitUpdateClaim = async () => {
  if (!currentClaim.value?.id) {
    toastStore.error('No claim ID found')
    return
  }

  if (!updateClaimForm.value.claimAmount || updateClaimForm.value.claimAmount <= 0) {
    toastStore.error('Please enter a valid claim amount')
    return
  }

  try {
    isUpdatingClaim.value = true

    const updateData = {
      claimAmount: updateClaimForm.value.claimAmount,
      damageAssessment: updateClaimForm.value.damageAssessment
    }

    const result = await claimStore.updateClaim(
      currentClaim.value.id,
      updateData,
      updateClaimFiles.value
    )

    if (result.success) {
      // Update the insurance data with the updated claim
      insuranceData.value.claim = result.data
      
      toastStore.success('Claim updated successfully!')
      closeUpdateClaimModal()
      
      // Optionally refetch the application details to ensure consistency
      await fetchApplicationDetails()
    } else {
      toastStore.error(result.message || 'Failed to update claim')
    }
  } catch (error) {
    console.error('Error updating claim:', error)
    toastStore.error('An error occurred while updating the claim')
  } finally {
    isUpdatingClaim.value = false
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
    aiAnalysisRequired.value = null
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

/* Slide from right animation */
.slide-right-enter-active,
.slide-right-leave-active {
  transition: transform 0.3s ease-out;
}

.slide-right-enter-from {
  transform: translateX(100%);
}

.slide-right-leave-to {
  transform: translateX(100%);
}

/* Fade animation for backdrop */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* Modal animation */
.modal-enter-active,
.modal-leave-active {
  transition: all 0.3s ease;
}

.modal-enter-from {
  opacity: 0;
  transform: scale(0.9) translateY(-20px);
}

.modal-leave-to {
  opacity: 0;
  transform: scale(0.9) translateY(-20px);
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
