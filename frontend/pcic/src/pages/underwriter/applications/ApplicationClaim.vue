<template>
  <AuthenticatedLayout
    :navigation="underwriterNavigation"
    role-title="Underwriter Portal"
    page-title="Claim Processing"
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
                <span class="sr-only">Dashboard</span>
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
              <button
                @click="navigateToApplicationDetail"
                class="text-xs font-medium text-slate-500 hover:text-slate-700 transition-colors duration-200"
              >
                Application Details
              </button>
            </li>
            <li class="flex items-center">
              <ChevronRightIcon class="h-3 w-3 text-slate-300 mx-1" />
              <span class="text-xs font-medium text-slate-900">
                Claim Processing
              </span>
            </li>
          </ol>
        </nav>

        <!-- Page Header -->
        <div class="flex items-start justify-between">
          <div>
            <div class="flex items-center gap-3">
              <h1 class="text-2xl font-light text-slate-900 tracking-tight">
                {{ getPageTitle() }}
              </h1>
              <button
                @click="openCoverageModal"
                class="p-2 text-slate-400 hover:text-blue-600 hover:bg-blue-50/50 rounded-full transition-all duration-200"
                title="View Insurance Coverage Information"
              >
                <QuestionMarkCircleIcon class="h-5 w-5" />
              </button>
            </div>
            <p class="mt-1 text-sm text-slate-500">
              {{ getPageDescription() }}
            </p>
          </div>
        </div>
      </div>
    </template>

      <!-- Loading State -->
      <div v-if="loading" class="flex justify-center items-center py-20">
        <div class="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600"></div>
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="bg-red-50/50 border border-red-200/60 rounded-xl p-5 backdrop-blur-sm">
        <div class="flex">
          <ExclamationTriangleIcon class="h-5 w-5 text-red-500 mt-0.5 flex-shrink-0" />
          <div class="ml-3">
            <h3 class="text-sm font-medium text-red-900">Error Loading Inspection Data</h3>
            <p class="mt-1 text-sm text-red-700/80">{{ error }}</p>
          </div>
        </div>
      </div>

      <!-- Main Content -->
      <div v-else-if="inspectionData" class="space-y-8">
        <!-- Two Column Layout -->
        <div class="grid grid-cols-1 xl:grid-cols-3 gap-8">
          <!-- Left Column: Inspection Details -->
          <div class="xl:col-span-2 space-y-6">
            <!-- Inspector Information -->
            <div class="bg-white/70 backdrop-blur-sm rounded-2xl border border-slate-200/60 shadow-sm overflow-hidden">
              <div class="px-6 py-4 border-b border-slate-100/80">
                <h3 class="text-lg font-semibold text-slate-800">Inspection Information</h3>
              </div>
              <div class="p-6">
                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <label class="text-sm font-medium text-slate-600">Inspector</label>
                    <p class="text-slate-900 font-semibold">{{ inspectionData.inspectorName }}</p>
                  </div>
                  <div>
                    <label class="text-sm font-medium text-slate-600">Inspection Date</label>
                    <p class="text-slate-900">{{ formatDate(inspectionData.inspectedAt) }}</p>
                  </div>
                </div>
              </div>
            </div>

            <!-- Farmer & Farm Information -->
            <div class="bg-white/70 backdrop-blur-sm rounded-2xl border border-slate-200/60 shadow-sm overflow-hidden">
              <div class="px-6 py-4 border-b border-slate-100/80">
                <h3 class="text-lg font-semibold text-slate-800">Farmer & Farm Information</h3>
              </div>
              <div class="p-6">
                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <label class="text-sm font-medium text-slate-600">Farmer Name</label>
                    <p class="text-slate-900 font-semibold">{{ fieldValues.farmer_name }}</p>
                  </div>
                  <div>
                    <label class="text-sm font-medium text-slate-600">CIC Number</label>
                    <p class="text-slate-900">{{ fieldValues.cic_no }}</p>
                  </div>
                  <div class="md:col-span-2">
                    <label class="text-sm font-medium text-slate-600">Address</label>
                    <p class="text-slate-900">{{ fieldValues.address }}</p>
                  </div>
                  <div class="md:col-span-2">
                    <label class="text-sm font-medium text-slate-600">Farm Location</label>
                    <p class="text-slate-900">{{ fieldValues.farm_location }}</p>
                  </div>
                  <div>
                    <label class="text-sm font-medium text-slate-600">Contact Number</label>
                    <p class="text-slate-900">{{ fieldValues.cell_phone_number }}</p>
                  </div>
                  <div>
                    <label class="text-sm font-medium text-slate-600">Tenurial Status</label>
                    <p class="text-slate-900">{{ fieldValues.tenurial_status }}</p>
                  </div>
                </div>
              </div>
            </div>

            <!-- Crop & Damage Information -->
            <div class="bg-white/70 backdrop-blur-sm rounded-2xl border border-slate-200/60 shadow-sm overflow-hidden">
              <div class="px-6 py-4 border-b border-slate-100/80">
                <h3 class="text-lg font-semibold text-slate-800">Crop & Damage Details</h3>
              </div>
              <div class="p-6">
                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <label class="text-sm font-medium text-slate-600">Insured Crops</label>
                    <p class="text-slate-900 font-semibold">{{ fieldValues.insured_crops }}</p>
                  </div>
                  <div>
                    <label class="text-sm font-medium text-slate-600">Variety Planted</label>
                    <p class="text-slate-900">{{ fieldValues.variety_planted }}</p>
                  </div>
                  <div>
                    <label class="text-sm font-medium text-slate-600">Planting Method</label>
                    <p class="text-slate-900">{{ fieldValues.planting_method }}</p>
                  </div>
                  <div>
                    <label class="text-sm font-medium text-slate-600">Date of Planting</label>
                    <p class="text-slate-900">{{ formatDate(fieldValues.date_planting) }}</p>
                  </div>
                  <div>
                    <label class="text-sm font-medium text-slate-600">Area Insured</label>
                    <p class="font-semibold text-green-600">{{ formatArea(fieldValues.area_insured) }}</p>
                  </div>
                  <div>
                    <label class="text-sm font-medium text-slate-600">Area Damaged</label>
                    <p class="font-semibold text-red-600">{{ formatArea(fieldValues.area_damaged) }}</p>
                  </div>
                  <div>
                    <label class="text-sm font-medium text-slate-600">Date of Loss</label>
                    <p class="text-slate-900">{{ formatDate(fieldValues.date_of_loss) }}</p>
                  </div>
                  <div>
                    <label class="text-sm font-medium text-slate-600">Expected Harvest</label>
                    <p class="text-slate-900">{{ formatDate(fieldValues.expected_harvest_date) }}</p>
                  </div>
                  <div>
                    <label class="text-sm font-medium text-slate-600">Cause of Damage</label>
                    <p class="font-semibold text-red-600">{{ fieldValues.cause_of_damage }}</p>
                  </div>
                  <div>
                    <label class="text-sm font-medium text-slate-600">Cultivation Stage</label>
                    <p class="text-slate-900">{{ fieldValues.cultivation_stage }}</p>
                  </div>
                  <div class="md:col-span-2">
                    <label class="text-sm font-medium text-slate-600">Damage Description</label>
                    <p class="text-slate-900">{{ fieldValues.damage_description || 'No description provided' }}</p>
                  </div>
                </div>
              </div>
            </div>

            <!-- Lot Boundaries -->
            <div v-if="hasLotBoundaries" class="bg-white/70 backdrop-blur-sm rounded-2xl border border-slate-200/60 shadow-sm overflow-hidden">
              <div class="px-6 py-4 border-b border-slate-100/80">
                <h3 class="text-lg font-semibold text-slate-800">Lot Boundaries</h3>
              </div>
              <div class="p-6">
                <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div v-for="(lot, index) in lotBoundaries" :key="index" class="border border-slate-200 rounded-lg p-4">
                    <h4 class="font-medium text-slate-800 mb-3">Lot {{ index + 1 }} ({{ formatArea(fieldValues[`lot_${index + 1}_area`]) }})</h4>
                    <div class="grid grid-cols-2 gap-2 text-sm">
                      <div><span class="font-medium">North:</span> {{ lot.north || 'N/A' }}</div>
                      <div><span class="font-medium">South:</span> {{ lot.south || 'N/A' }}</div>
                      <div><span class="font-medium">East:</span> {{ lot.east || 'N/A' }}</div>
                      <div><span class="font-medium">West:</span> {{ lot.west || 'N/A' }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Right Column: Photos & Actions -->
          <div class="space-y-6">
            <!-- Inspection Photos -->
            <div class="bg-white/70 backdrop-blur-sm rounded-2xl border border-slate-200/60 shadow-sm overflow-hidden">
              <div class="px-6 py-4 border-b border-slate-100/80">
                <h3 class="text-lg font-semibold text-slate-800">Inspection Photos</h3>
              </div>
              <div class="p-6">
                <div v-if="inspectionData.photos && inspectionData.photos.length > 0" class="space-y-4">
                  <div
                    v-for="(photo, index) in inspectionData.photos"
                    :key="index"
                    class="group relative aspect-video rounded-xl overflow-hidden bg-slate-100 cursor-pointer border border-slate-200 hover:border-blue-400 hover:shadow-md transition-all duration-200"
                    @click="openImageModal(photo)"
                  >
                    <img
                      :src="photo"
                      :alt="`Inspection photo ${index + 1}`"
                      class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-200"
                      @error="handleImageError"
                    />
                    <div class="absolute inset-0 bg-black/0 group-hover:bg-black/20 transition-colors duration-200 flex items-center justify-center">
                      <div class="opacity-0 group-hover:opacity-100 transition-opacity duration-200">
                        <div class="bg-white/90 backdrop-blur-sm px-3 py-1 rounded-full text-sm font-medium text-slate-700">
                          View Full Size
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
                <div v-else class="text-center py-8">
                  <CameraIcon class="mx-auto h-12 w-12 text-slate-300" />
                  <p class="mt-3 text-sm text-slate-500">No photos available</p>
                </div>
              </div>
            </div>

            <!-- Action Buttons -->
            <div v-if="currentAction === 'file_claim'" class="bg-white/70 backdrop-blur-sm rounded-2xl border border-slate-200/60 shadow-sm overflow-hidden">
              <div class="px-6 py-4 border-b border-slate-100/80">
                <h3 class="text-lg font-semibold text-slate-800">Claim Actions</h3>
              </div>
              <div class="p-6 space-y-3">
                <button
                  @click="openManualClaimModal"
                  :disabled="isProcessing"
                  class="w-full bg-blue-600 text-white py-3 px-4 rounded-lg text-sm font-semibold hover:bg-blue-700 transition disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center"
                >
                  Process Manual Claim
                </button>
                
                <button
                  @click="openAIClaimModal"
                  class="w-full bg-purple-600 text-white py-3 px-4 rounded-lg text-sm font-semibold hover:bg-purple-700 transition flex items-center justify-center"
                >
                  <svg class="w-4 h-4 mr-2" fill="currentColor" viewBox="0 0 20 20">
                    <path d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"/>
                  </svg>
                  AI Analysis & Claim
                </button>
              </div>
            </div>

            <!-- Update Action Button -->
            <div v-if="currentAction === 'update'" class="bg-white/70 backdrop-blur-sm rounded-2xl border border-slate-200/60 shadow-sm overflow-hidden">
              <div class="px-6 py-4 border-b border-slate-100/80">
                <h3 class="text-lg font-semibold text-slate-800">Update Actions</h3>
              </div>
              <div class="p-6 space-y-3">
                <button
                  @click="handleUpdateAction"
                  :disabled="isProcessing"
                  class="w-full bg-green-600 text-white py-3 px-4 rounded-lg text-sm font-semibold hover:bg-green-700 transition disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center"
                >
                  Update Existing Claim
                </button>
              </div>
            </div>

            <!-- View Mode - Claim Details -->
            <div v-if="currentAction === 'view'" class="bg-white/70 backdrop-blur-sm rounded-2xl border border-slate-200/60 shadow-sm overflow-hidden">
              <div class="px-6 py-4 border-b border-slate-100/80">
                <h3 class="text-lg font-semibold text-slate-800">Claim Information</h3>
              </div>
              <div class="p-6">
                <div v-if="claimData" class="space-y-6">
                  <!-- Basic Claim Information -->
                  <div class="bg-blue-50 border border-blue-200 rounded-xl p-4">
                    <h4 class="font-semibold text-blue-900 mb-3">Claim Details</h4>
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-3 text-sm text-blue-800">
                      <div>
                        <p class="font-medium">Claim ID:</p>
                        <p class="break-all">{{ claimData.id }}</p>
                      </div>
                      <div>
                        <p class="font-medium">Insurance ID:</p>
                        <p class="break-all">{{ claimData.insuranceId }}</p>
                      </div>
                      <div>
                        <p class="font-medium">Claim Amount:</p>
                        <p class="font-semibold">{{ formatCurrency(claimData.claimAmount) }}</p>
                      </div>
                      <div>
                        <p class="font-medium">Filing Date:</p>
                        <p>{{ formatDate(claimData.filedAt || claimData.createdAt) }}</p>
                      </div>
                      <div class="md:col-span-2">
                        <p class="font-medium">Damage Assessment:</p>
                        <p class="mt-1">{{ claimData.damageAssessment || 'No assessment provided' }}</p>
                      </div>
                    </div>
                  </div>

                  <!-- AI Analysis Results -->
                  <div v-if="claimData.fieldValues" class="bg-purple-50 border border-purple-200 rounded-xl p-4">
                    <h4 class="font-semibold text-purple-900 mb-3">AI Analysis Results</h4>
                    <div class="grid grid-cols-1 md:grid-cols-2 gap-3 text-sm text-purple-800">
                      <div v-if="claimData.fieldValues.prediction">
                        <p class="font-medium">Prediction:</p>
                        <p>{{ claimData.fieldValues.prediction }}</p>
                      </div>
                      <div v-if="claimData.fieldValues.disease_type">
                        <p class="font-medium">Disease Type:</p>
                        <p class="capitalize">{{ claimData.fieldValues.disease_type }}</p>
                      </div>
                      <div v-if="claimData.fieldValues.severity_percentage">
                        <p class="font-medium">Severity:</p>
                        <p>{{ Number(claimData.fieldValues.severity_percentage).toFixed(2) }}%</p>
                      </div>
                      <div v-if="claimData.fieldValues.confidence">
                        <p class="font-medium">Confidence:</p>
                        <p>{{ Number(claimData.fieldValues.confidence).toFixed(2) }}%</p>
                      </div>
                      <div v-if="claimData.fieldValues.accuracy">
                        <p class="font-medium">Accuracy:</p>
                        <p>{{ claimData.fieldValues.accuracy }}</p>
                      </div>
                      <div v-if="claimData.fieldValues.leaf_area">
                        <p class="font-medium">Leaf Area:</p>
                        <p>{{ Number(claimData.fieldValues.leaf_area).toLocaleString() }} px²</p>
                      </div>
                      <div v-if="claimData.fieldValues.lesion_area">
                        <p class="font-medium">Lesion Area:</p>
                        <p>{{ Number(claimData.fieldValues.lesion_area).toLocaleString() }} px²</p>
                      </div>
                    </div>
                  </div>

                  <!-- Supporting Files -->
                  <div v-if="claimData.supportingFiles && claimData.supportingFiles.length > 0" class="bg-green-50 border border-green-200 rounded-xl p-4">
                    <h4 class="font-semibold text-green-900 mb-3">Supporting Files</h4>
                    <div class="grid grid-cols-2 md:grid-cols-3 gap-3">
                      <div
                        v-for="(file, index) in claimData.supportingFiles"
                        :key="index"
                        class="group relative aspect-square rounded-lg overflow-hidden bg-slate-100 cursor-pointer border border-green-200 hover:border-green-400 hover:shadow-md transition-all duration-200"
                        @click="openImageModal(file)"
                      >
                        <img
                          :src="file"
                          :alt="`Support ${index + 1}`"
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
                <div v-else class="text-center py-8">
                  <DocumentIcon class="mx-auto h-12 w-12 text-slate-300" />
                  <p class="mt-3 text-sm text-slate-500">No claim data available</p>
                </div>
                <button
                  @click="navigateToApplicationDetail"
                  class="w-full mt-6 bg-slate-600 text-white py-2 px-4 rounded-lg text-sm font-medium hover:bg-slate-700 transition"
                >
                  Back to Application Details
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- No Data State -->
      <div v-else class="text-center py-20">
        <DocumentIcon class="mx-auto h-16 w-16 text-slate-300" />
        <h2 class="mt-4 text-xl font-semibold text-slate-600">No Inspection Data Found</h2>
        <p class="mt-2 text-slate-500">
          No inspection record found for this insurance application.
        </p>
      </div>

      <!-- Image Modal -->
      <div
        v-if="selectedImage"
        class="fixed inset-0 z-50 flex items-center justify-center bg-black/75 backdrop-blur-sm"
        @click="closeImageModal"
      >
        <div class="relative max-w-4xl max-h-[90vh] p-4">
          <img
            :src="selectedImage"
            alt="Inspection photo"
            class="max-w-full max-h-full object-contain rounded-lg shadow-2xl"
          />
          <button
            @click="closeImageModal"
            class="absolute top-2 right-2 bg-black/50 hover:bg-black/70 text-white p-2 rounded-full transition-colors"
          >
            <XMarkIcon class="h-6 w-6" />
          </button>
        </div>
      </div>

      <!-- Coverage Information Modal -->
      <div
        v-if="showCoverageModal"
        class="fixed inset-0 z-50 flex items-center justify-center bg-black/50 backdrop-blur-sm p-4"
        @click="closeCoverageModal"
      >
        <div
          class="relative bg-white rounded-2xl shadow-2xl max-w-4xl max-h-[90vh] overflow-y-auto"
          @click.stop
        >
          <!-- Modal Header -->
          <div class="sticky top-0 bg-white border-b border-slate-200 px-6 py-4 rounded-t-2xl">
            <div class="flex items-center justify-between">
              <h3 class="text-xl font-semibold text-slate-800">Insurance Coverage & Claims Information</h3>
              <button
                @click="closeCoverageModal"
                class="text-slate-400 hover:text-slate-600 p-2 hover:bg-slate-100 rounded-full transition-colors"
              >
                <XMarkIcon class="h-6 w-6" />
              </button>
            </div>
          </div>

          <!-- Modal Content -->
          <div class="p-6">
            <!-- Amount of Cover Information -->
            <div class="bg-blue-50/50 rounded-xl p-6 mb-6">
              <h4 class="text-lg font-semibold text-blue-900 mb-4">AMOUNT OF COVER (Insurance Amount)</h4>
              <div class="space-y-3 text-sm text-blue-800">
                <p><strong>Based on the Farm Plan & Budget (FPB)</strong> = cost of production inputs.</p>
                <p>Farmer may add up to <strong>+20% of FPB</strong> to cover expected yield value.</p>
              </div>
            </div>

            <!-- Cover Ceiling Table -->
            <div class="mb-6">
              <h5 class="text-md font-semibold text-slate-800 mb-3">Cover Ceiling (Maximum per Hectare):</h5>
              <div class="overflow-x-auto">
                <table class="w-full border-collapse border border-slate-300 text-sm">
                  <thead>
                    <tr class="bg-slate-100">
                      <th class="border border-slate-300 px-4 py-2 text-left font-semibold">Rice Type</th>
                      <th class="border border-slate-300 px-4 py-2 text-left font-semibold">Self-Financed</th>
                      <th class="border border-slate-300 px-4 py-2 text-left font-semibold">Borrowing Farmer</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr>
                      <td class="border border-slate-300 px-4 py-2">Inbred (Commercial)</td>
                      <td class="border border-slate-300 px-4 py-2 font-semibold">₱41,000</td>
                      <td class="border border-slate-300 px-4 py-2 font-semibold">₱67,000</td>
                    </tr>
                    <tr class="bg-slate-50">
                      <td class="border border-slate-300 px-4 py-2">Inbred (Seed Production)</td>
                      <td class="border border-slate-300 px-4 py-2 font-semibold">₱50,000</td>
                      <td class="border border-slate-300 px-4 py-2 text-slate-400">—</td>
                    </tr>
                    <tr>
                      <td class="border border-slate-300 px-4 py-2">Hybrid (Commercial F1)</td>
                      <td class="border border-slate-300 px-4 py-2 font-semibold">₱50,000</td>
                      <td class="border border-slate-300 px-4 py-2 font-semibold">₱78,000</td>
                    </tr>
                    <tr class="bg-slate-50">
                      <td class="border border-slate-300 px-4 py-2">Hybrid (Seed Production A×R)</td>
                      <td class="border border-slate-300 px-4 py-2 font-semibold">₱120,000</td>
                      <td class="border border-slate-300 px-4 py-2 text-slate-400">—</td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <!-- Current Application Coverage -->
            <div class="bg-green-50/50 rounded-xl p-6 mb-6">
              <h5 class="text-md font-semibold text-green-900 mb-3">Current Application Coverage:</h5>
              <div class="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm">
                <div>
                  <label class="text-green-700 font-medium">Rice Variety</label>
                  <p class="text-green-900 font-semibold">{{ fieldValues.variety_planted }}</p>
                </div>
                <div>
                  <label class="text-green-700 font-medium">Area Insured</label>
                  <p class="text-green-900 font-semibold">{{ formatArea(fieldValues.area_insured) }}</p>
                </div>
                <div>
                  <label class="text-green-700 font-medium">Estimated Coverage</label>
                  <p class="text-green-900 font-semibold">{{ formatCurrency(estimatedCoverage) }}</p>
                </div>
              </div>
            </div>

            <!-- Claims Formula -->
            <div class="bg-orange-50/50 rounded-xl p-6">
              <h5 class="text-md font-semibold text-orange-900 mb-3">Formula (Simplified):</h5>
              <div class="bg-white rounded-lg p-4 mb-4 border border-orange-200">
                <p class="text-center text-lg font-mono font-semibold text-orange-900">
                  Indemnity = (Amount of Cover × % Yield Loss × Adjustment Factor)
                </p>
              </div>
              <div class="space-y-2 text-sm text-orange-800">
                <p><strong>Where:</strong></p>
                <ul class="list-disc list-inside space-y-1 ml-4">
                  <li><strong>% Yield Loss</strong> is determined by PCIC adjusters</li>
                  <li><strong>Adjustment Factor</strong> is based on crop stage (early stage = smaller payout)</li>
                </ul>
              </div>
              
              <!-- Estimated Claim Calculation -->
              <div class="mt-4 pt-4 border-t border-orange-200">
                <h6 class="font-semibold text-orange-900 mb-2">Estimated Claim Calculation:</h6>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
                  <div>
                    <label class="text-orange-700">Area Damaged</label>
                    <p class="text-orange-900 font-semibold">{{ formatArea(fieldValues.area_damaged) }}</p>
                  </div>
                  <div>
                    <label class="text-orange-700">Damage Percentage</label>
                    <p class="text-orange-900 font-semibold">{{ damagePercentage }}%</p>
                  </div>
                  <div>
                    <label class="text-orange-700">Estimated Yield Loss</label>
                    <p class="text-orange-900 font-semibold">{{ estimatedYieldLoss }}%</p>
                  </div>
                  <div>
                    <label class="text-orange-700">Estimated Indemnity</label>
                    <p class="text-orange-900 font-bold text-lg">{{ formatCurrency(estimatedIndemnity) }}</p>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Claim Processing Modal -->
      <Transition name="slide-right">
        <div
          v-if="showClaimModal"
          class="fixed inset-y-0 right-0 z-50 w-full max-w-md bg-white shadow-2xl flex flex-col"
        >
          <!-- Modal Content -->
          <div class="flex-1 overflow-y-auto">
            <div class="p-6 space-y-6">
              <!-- Close Button -->
              <div class="flex justify-end mb-4">
                <button
                  @click="closeClaimModal"
                  class="text-slate-400 hover:text-slate-600 p-2 hover:bg-slate-100 rounded-full transition-colors"
                >
                  <XMarkIcon class="h-6 w-6" />
                </button>
              </div>

              <!-- Modal Title -->
              <div class="mb-6">
                <h3 class="text-xl font-semibold text-slate-800">
                  {{ claimType === 'manual' ? 'Manual Claim Processing' : 'AI Claim Processing' }}
                </h3>
                <p class="text-slate-600 text-sm mt-1">
                  {{ claimType === 'manual' ? 'Process claim based on inspection findings' : 'Process claim with AI analysis integration' }}
                </p>
              </div>

              <!-- Information Section -->
              <div class="bg-blue-50 border border-blue-200 rounded-xl p-4">
                <div class="flex items-start">
                  <InformationCircleIcon class="h-5 w-5 text-blue-600 mt-0.5 flex-shrink-0" />
                  <div class="ml-3 text-sm">
                    <h4 class="font-medium text-blue-900 mb-2">Claim Information</h4>
                    <div v-if="claimType === 'manual'" class="text-blue-800 space-y-1">
                      <p>• Claim will be processed based on inspection findings</p>
                      <p>• Field values from inspection will be used automatically</p>
                      <p>• Upload supporting documents if available</p>
                      <p>• Estimated indemnity: <span class="font-semibold">{{ formatCurrency(estimatedIndemnity) }}</span></p>
                    </div>
                    <div v-else class="text-blue-800 space-y-1">
                      <p>• Claim will integrate with AI analysis results</p>
                      <p>• AI analysis data will be included in the claim</p>
                      <p>• Upload additional supporting documents if needed</p>
                      <p>• AI confidence score will be recorded</p>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Claim Summary -->
              <div class="space-y-4">
                <h4 class="font-semibold text-slate-800">Claim Summary</h4>
                <div class="bg-slate-50 rounded-lg p-4 space-y-3">
                  <div class="grid grid-cols-2 gap-4 text-sm">
                    <div>
                      <label class="text-slate-600 font-medium">Farmer</label>
                      <p class="text-slate-900">{{ farmerName }}</p>
                    </div>
                    <div>
                      <label class="text-slate-600 font-medium">CIC Number</label>
                      <p class="text-slate-900">{{ fieldValues.cic_no }}</p>
                    </div>
                    <div>
                      <label class="text-slate-600 font-medium">Area Damaged</label>
                      <p class="font-semibold text-red-600">{{ formatArea(fieldValues.area_damaged) }}</p>
                    </div>
                    <div>
                      <label class="text-slate-600 font-medium">Cause of Damage</label>
                      <p class="font-semibold text-red-600">{{ fieldValues.cause_of_damage }}</p>
                    </div>
                  </div>
                </div>
              </div>

              <!-- File Upload Section -->
              <div class="space-y-4">
                <h4 class="font-semibold text-slate-800">Supporting Documents</h4>
                <div class="border-2 border-dashed border-slate-300 rounded-lg p-6 text-center hover:border-blue-400 transition-colors">
                  <input
                    ref="fileInput"
                    type="file"
                    multiple
                    accept="image/*,.pdf,.doc,.docx"
                    @change="handleFileSelect"
                    class="hidden"
                  />
                  <button
                    @click="$refs.fileInput.click()"
                    class="flex flex-col items-center space-y-2"
                  >
                    <DocumentIcon class="h-12 w-12 text-slate-400" />
                    <div class="text-sm text-slate-600">
                      <span class="font-medium text-blue-600">Click to upload</span>
                      <p class="mt-1">or drag and drop files here</p>
                      <p class="mt-1 text-xs text-slate-500">PNG, JPG, PDF, DOC up to 10MB each</p>
                    </div>
                  </button>
                </div>

                <!-- Selected Files -->
                <div v-if="selectedFiles.length > 0" class="space-y-2">
                  <h5 class="text-sm font-medium text-slate-700">Selected Files ({{ selectedFiles.length }})</h5>
                  <div class="space-y-2 max-h-40 overflow-y-auto">
                    <div
                      v-for="(file, index) in selectedFiles"
                      :key="index"
                      class="flex items-center justify-between bg-slate-50 rounded-lg p-3"
                    >
                      <div class="flex items-center space-x-3">
                        <DocumentIcon class="h-5 w-5 text-slate-400" />
                        <div class="min-w-0 flex-1">
                          <p class="text-sm font-medium text-slate-900 truncate">{{ file.name }}</p>
                          <p class="text-xs text-slate-500">{{ formatFileSize(file.size) }}</p>
                        </div>
                      </div>
                      <button
                        @click="removeFile(index)"
                        class="text-slate-400 hover:text-red-600 p-1"
                      >
                        <XMarkIcon class="h-4 w-4" />
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Modal Footer -->
          <div class="flex-shrink-0 bg-white border-t border-slate-200 px-6 py-4">
            <div class="flex space-x-3">
              <button
                @click="closeClaimModal"
                class="flex-1 bg-slate-200 text-slate-800 py-2 px-4 rounded-lg text-sm font-medium hover:bg-slate-300 transition"
              >
                Cancel
              </button>
              <button
                @click="submitClaim"
                :disabled="isSubmitting"
                class="flex-1 bg-blue-600 text-white py-2 px-4 rounded-lg text-sm font-medium hover:bg-blue-700 transition disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center"
              >
                <span v-if="!isSubmitting">Submit Claim</span>
                <span v-else class="flex items-center">
                  <svg class="animate-spin h-4 w-4 mr-2" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  Submitting...
                </span>
              </button>
            </div>
          </div>
        </div>
      </Transition>

      <!-- Update Claim Modal -->
      <Transition name="slide-right">
        <div
          v-if="showUpdateModal"
          class="fixed inset-y-0 right-0 z-50 w-full max-w-md bg-white shadow-2xl flex flex-col"
        >
          <!-- Modal Content -->
          <div class="flex-1 overflow-y-auto">
            <div class="p-6 space-y-6">
              <!-- Close Button -->
              <div class="flex justify-end mb-4">
                <button
                  @click="closeUpdateModal"
                  class="text-slate-400 hover:text-slate-600 p-2 hover:bg-slate-100 rounded-full transition-colors"
                >
                  <XMarkIcon class="h-6 w-6" />
                </button>
              </div>

              <!-- Modal Title -->
              <div class="mb-6">
                <h3 class="text-xl font-semibold text-slate-800">Update Claim</h3>
                <p class="text-slate-600 text-sm mt-1">
                  Update the claim amount and damage assessment details
                </p>
              </div>

              <!-- Current Claim Info -->
              <div v-if="claimData" class="bg-blue-50 border border-blue-200 rounded-xl p-4">
                <h4 class="font-semibold text-blue-900 mb-2">Current Claim Information</h4>
                <div class="space-y-2 text-sm text-blue-800">
                  <p><strong>Claim ID:</strong> {{ claimData.id?.substring(0, 8) }}...</p>
                  <p><strong>Current Amount:</strong> {{ formatCurrency(claimData.claimAmount) }}</p>
                  <p><strong>Filed:</strong> {{ formatDate(claimData.filedAt) }}</p>
                </div>
              </div>

              <!-- Update Form -->
              <div class="space-y-4">
                <div>
                  <label for="updateClaimAmount" class="block text-sm font-medium text-slate-700 mb-2">
                    Claim Amount (PHP)
                  </label>
                  <input
                    id="updateClaimAmount"
                    v-model="updateForm.claimAmount"
                    type="number"
                    min="0.01"
                    step="0.01"
                    class="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors"
                    placeholder="Enter claim amount"
                  />
                </div>

                <div>
                  <label for="updateDamageAssessment" class="block text-sm font-medium text-slate-700 mb-2">
                    Damage Assessment
                  </label>
                  <textarea
                    id="updateDamageAssessment"
                    v-model="updateForm.damageAssessment"
                    rows="4"
                    maxlength="2000"
                    class="w-full px-3 py-2 border border-slate-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors resize-none"
                    placeholder="Describe the damage assessment details..."
                  ></textarea>
                  <div class="text-xs text-slate-500 mt-1">
                    {{ updateForm.damageAssessment?.length || 0 }}/2000 characters
                  </div>
                </div>
              </div>

              <!-- File Upload Section -->
              <div class="space-y-4">
                <h4 class="font-semibold text-slate-800">Supporting Documents</h4>
                <div class="border-2 border-dashed border-slate-300 rounded-lg p-6 text-center hover:border-blue-400 transition-colors">
                  <input
                    ref="updateFileInput"
                    type="file"
                    multiple
                    accept=".pdf,.jpg,.jpeg,.png,.doc,.docx"
                    @change="handleUpdateFileSelect"
                    class="hidden"
                  />
                  <button
                    @click="$refs.updateFileInput.click()"
                    class="flex flex-col items-center space-y-2"
                  >
                    <DocumentIcon class="h-8 w-8 text-slate-400" />
                    <span class="text-sm text-slate-600">Click to upload files</span>
                    <span class="text-xs text-slate-500">PDF, Images, Word documents</span>
                  </button>
                </div>

                <!-- Selected Files -->
                <div v-if="updateFiles.length > 0" class="space-y-2">
                  <h5 class="text-sm font-medium text-slate-700">Selected Files ({{ updateFiles.length }})</h5>
                  <div class="space-y-2 max-h-40 overflow-y-auto">
                    <div
                      v-for="(file, index) in updateFiles"
                      :key="index"
                      class="flex items-center justify-between p-2 bg-slate-50 rounded-lg"
                    >
                      <div class="flex items-center space-x-2 flex-1 min-w-0">
                        <DocumentIcon class="h-4 w-4 text-slate-400 flex-shrink-0" />
                        <span class="text-sm text-slate-600 truncate">{{ file.name }}</span>
                        <span class="text-xs text-slate-500 flex-shrink-0">({{ formatFileSize(file.size) }})</span>
                      </div>
                      <button
                        @click="removeUpdateFile(index)"
                        class="text-red-500 hover:text-red-700 p-1 rounded transition-colors"
                      >
                        <XMarkIcon class="h-4 w-4" />
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>

          <!-- Modal Footer -->
          <div class="flex-shrink-0 bg-white border-t border-slate-200 px-6 py-4">
            <div class="flex space-x-3">
              <button
                @click="closeUpdateModal"
                class="flex-1 bg-slate-200 text-slate-800 py-2 px-4 rounded-lg text-sm font-medium hover:bg-slate-300 transition"
              >
                Cancel
              </button>
              <button
                @click="submitUpdate"
                :disabled="isUpdating || !updateForm.claimAmount || updateForm.claimAmount <= 0"
                class="flex-1 bg-blue-600 text-white py-2 px-4 rounded-lg text-sm font-medium hover:bg-blue-700 transition disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center"
              >
                <span v-if="!isUpdating">Update Claim</span>
                <span v-else class="flex items-center">
                  <svg class="animate-spin h-4 w-4 mr-2" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  Updating...
                </span>
              </button>
            </div>
          </div>
        </div>
      </Transition>
  </AuthenticatedLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useInspectionStore } from '@/stores/inspection'
import { useClaimStore } from '@/stores/claim'
import { useToastStore } from '@/stores/toast'
import { UNDERWRITER_NAVIGATION } from '@/lib/navigation'
import AuthenticatedLayout from '@/layouts/AuthenticatedLayout.vue'
import {
  HomeIcon,
  ChevronRightIcon,
  ExclamationTriangleIcon,
  DocumentIcon,
  CameraIcon,
  XMarkIcon,
  QuestionMarkCircleIcon,
  InformationCircleIcon
} from '@heroicons/vue/24/outline'

const route = useRoute()
const router = useRouter()
const inspectionStore = useInspectionStore()
const claimStore = useClaimStore()
const toastStore = useToastStore()
const underwriterNavigation = UNDERWRITER_NAVIGATION

// State
const inspectionData = ref(null)
const loading = ref(true)
const error = ref(null)
const selectedImage = ref(null)
const isProcessing = ref(false)
const showCoverageModal = ref(false)
const showClaimModal = ref(false)
const claimType = ref('manual') // 'manual' or 'ai'
const selectedFiles = ref([])
const additionalNotes = ref('')
const isSubmitting = ref(false)

// Action handling based on query parameters
const currentAction = ref('view') // Default to view mode
const claimData = ref(null)
const showUpdateModal = ref(false)
const updateForm = ref({
  claimAmount: 0,
  damageAssessment: ''
})
const updateFiles = ref([])
const isUpdating = ref(false)

// Get IDs from route params
const insuranceId = route.params.insuranceId
const submissionId = route.params.submissionId

// Computed properties
const farmerName = computed(() => {
  return inspectionData.value?.fieldValues?.farmer_name || 'Unknown Farmer'
})

const fieldValues = computed(() => {
  return inspectionData.value?.fieldValues || {}
})

const lotBoundaries = computed(() => {
  if (!fieldValues.value) return []
  
  const boundaries = []
  for (let i = 1; i <= 4; i++) {
    const boundary = fieldValues.value[`lot_${i}_boundaries`]
    const area = fieldValues.value[`lot_${i}_area`]
    
    if (boundary && area > 0) {
      boundaries.push(boundary)
    }
  }
  return boundaries
})

const hasLotBoundaries = computed(() => {
  return lotBoundaries.value.length > 0
})

// Page title and description based on current action
const getPageTitle = () => {
  switch (currentAction.value) {
    case 'view':
      return 'Claim Details'
    case 'update':
      return 'Update Claim'
    case 'file_claim':
      return 'File New Claim'
    case 'ai-analysis':
      return 'AI Claim Analysis'
    default:
      return 'Claim Processing'
  }
}

const getPageDescription = () => {
  const farmer = farmerName.value
  switch (currentAction.value) {
    case 'view':
      return `Viewing claim information for ${farmer} in read-only mode`
    case 'update':
      return `Update existing claim details for ${farmer}`
    case 'file_claim':
      return `Review inspection findings and process new claim for ${farmer}`
    case 'ai-analysis':
      return `Process claim with AI analysis integration for ${farmer}`
    default:
      return `Review inspection findings and process claim for ${farmer}`
  }
}

// Coverage calculations
const estimatedCoverage = computed(() => {
  if (!fieldValues.value.area_insured || !fieldValues.value.variety_planted) return 0
  
  const area = parseFloat(fieldValues.value.area_insured) || 0
  const variety = fieldValues.value.variety_planted
  
  // Determine coverage rate based on variety (assuming self-financed for simplicity)
  let ratePerHectare = 41000 // Default to Inbred Commercial
  
  if (variety.toLowerCase().includes('seed production') && variety.toLowerCase().includes('inbred')) {
    ratePerHectare = 50000
  } else if (variety.toLowerCase().includes('hybrid') && variety.toLowerCase().includes('commercial')) {
    ratePerHectare = 50000
  } else if (variety.toLowerCase().includes('hybrid') && variety.toLowerCase().includes('seed production')) {
    ratePerHectare = 120000
  }
  
  return area * ratePerHectare
})

const damagePercentage = computed(() => {
  if (!fieldValues.value.area_insured || !fieldValues.value.area_damaged) return 0
  
  const areaInsured = parseFloat(fieldValues.value.area_insured) || 0
  const areaDamaged = parseFloat(fieldValues.value.area_damaged) || 0
  
  if (areaInsured === 0) return 0
  
  return Math.round((areaDamaged / areaInsured) * 100)
})

const estimatedYieldLoss = computed(() => {
  // Base yield loss on damage percentage and cultivation stage
  const baseYieldLoss = damagePercentage.value
  const cultivationStage = fieldValues.value.cultivation_stage || ''
  
  // Adjust based on crop stage (early stage = smaller impact)
  let adjustmentFactor = 1.0
  
  if (cultivationStage.toLowerCase().includes('month')) {
    const months = parseInt(cultivationStage.match(/\d+/)?.[0]) || 0
    if (months < 1) adjustmentFactor = 0.3
    else if (months < 2) adjustmentFactor = 0.5
    else if (months < 3) adjustmentFactor = 0.7
    else adjustmentFactor = 0.9
  }
  
  return Math.round(baseYieldLoss * adjustmentFactor)
})

const estimatedIndemnity = computed(() => {
  if (!estimatedCoverage.value || !estimatedYieldLoss.value) return 0
  
  const coverage = estimatedCoverage.value
  const yieldLossPercent = estimatedYieldLoss.value / 100
  const adjustmentFactor = 0.8 // Standard adjustment factor
  
  return Math.round(coverage * yieldLossPercent * adjustmentFactor)
})

// Helper functions
const formatDate = (dateString) => {
  if (!dateString) return 'N/A'
  
  try {
    const date = new Date(dateString)
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (error) {
    console.error('Error formatting date:', error)
    return 'Invalid Date'
  }
}

const formatArea = (value) => {
  if (value === null || value === undefined || value === 0) return 'N/A'
  return `${parseFloat(value).toFixed(1)} ha`
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
    return `₱${amount.toLocaleString()}`
  }
}

const getPriorityClass = (priority) => {
  switch (priority) {
    case 'HIGH':
      return 'bg-red-100 text-red-800'
    case 'MEDIUM':
      return 'bg-yellow-100 text-yellow-800'
    case 'LOW':
      return 'bg-green-100 text-green-800'
    default:
      return 'bg-gray-100 text-gray-800'
  }
}

// Image modal functions
const openImageModal = (imageUrl) => {
  selectedImage.value = imageUrl
}

const closeImageModal = () => {
  selectedImage.value = null
}

// Coverage modal functions
const openCoverageModal = () => {
  showCoverageModal.value = true
}

const closeCoverageModal = () => {
  showCoverageModal.value = false
}

const handleImageError = (event) => {
  event.target.src = 'data:image/svg+xml,%3Csvg xmlns="http://www.w3.org/2000/svg" width="200" height="200"%3E%3Crect fill="%23ddd" width="200" height="200"/%3E%3Ctext fill="%23999" x="50%25" y="50%25" text-anchor="middle" dy=".3em"%3EImage not found%3C/text%3E%3C/svg%3E'
}

// Navigation functions
const navigateToApplicationList = () => {
  router.push({
    name: 'underwriter-applications-all'
  })
}

const navigateToApplicationDetail = () => {
  if (insuranceId && submissionId) {
    router.push({
      name: 'underwriter-applications-detail',
      params: {
        insuranceId: insuranceId,
        submissionId: submissionId
      }
    })
  } else {
    navigateToApplicationList()
  }
}

const navigateToAIAnalysis = () => {
  if (insuranceId && submissionId) {
    router.push({
      name: 'damage-claim-review',
      params: {
        insuranceId: insuranceId,
        submissionId: submissionId
      }
    })
  } else {
    toastStore.error('Required information not found')
  }
}

// Claim modal functions
const openManualClaimModal = () => {
  claimType.value = 'manual'
  showClaimModal.value = true
}

const openAIClaimModal = () => {
  claimType.value = 'ai'
  showClaimModal.value = true
}

const closeClaimModal = () => {
  showClaimModal.value = false
  selectedFiles.value = []
  additionalNotes.value = ''
  claimType.value = 'manual'
}

// File handling functions
const handleFileSelect = (event) => {
  const files = Array.from(event.target.files)
  selectedFiles.value = [...selectedFiles.value, ...files]
}

const removeFile = (index) => {
  selectedFiles.value.splice(index, 1)
}

const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes'
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return parseFloat((bytes / Math.pow(1024, i)).toFixed(2)) + ' ' + sizes[i]
}

// Claim submission function
const submitClaim = async () => {
  if (!inspectionData.value) {
    toastStore.error('No inspection data available')
    return
  }

  try {
    isSubmitting.value = true

    // Prepare claim data from inspection field values
    const claimData = {
      insuranceId: insuranceId,
      applicationId: submissionId,
      damageDescription: fieldValues.value.damage_description || additionalNotes.value || 'Claim based on inspection findings',
      causeOfDamage: fieldValues.value.cause_of_damage,
      areaInsured: fieldValues.value.area_insured,
      areaDamaged: fieldValues.value.area_damaged,
      dateOfLoss: fieldValues.value.date_of_loss,
      inspectionId: inspectionData.value.id,
      claimAmount: estimatedIndemnity.value,
      fieldValues: fieldValues.value, // Pass all field values
      additionalNotes: additionalNotes.value
    }

    let result
    if (claimType.value === 'manual') {
      result = await claimStore.createClaimManually(claimData, selectedFiles.value)
    } else {
      // For AI claims, we might want to include AI analysis data
      claimData.aiAnalysisResults = {} // This would come from AI store if available
      result = await claimStore.createClaimFromAI(claimData, selectedFiles.value)
    }

    if (result.success) {
      toastStore.success(`${claimType.value === 'manual' ? 'Manual' : 'AI'} claim created successfully!`)
      closeClaimModal()
      
      // Navigate back to application detail after a short delay
      setTimeout(() => {
        navigateToApplicationDetail()
      }, 1500)
    } else {
      toastStore.error(result.message || `Failed to create ${claimType.value} claim`)
    }
  } catch (error) {
    console.error('Error creating claim:', error)
    toastStore.error('An error occurred while creating the claim')
  } finally {
    isSubmitting.value = false
  }
}

const calculateClaimAmount = () => {
  // Simple calculation based on damaged area
  // In a real system, this would be more complex with rates, coverage limits, etc.
  const areaDamaged = parseFloat(fieldValues.value.area_damaged) || 0
  const baseRate = 50000 // PHP per hectare (example rate)
  return areaDamaged * baseRate
}

// Data fetching
const fetchInspectionData = async () => {
  try {
    loading.value = true
    error.value = null

    console.log('Fetching inspection data for insurance ID:', insuranceId)

    const result = await inspectionStore.fetchInspectionByInsuranceId(insuranceId)
    
    if (result.success) {
      inspectionData.value = result.data
      console.log('Inspection data fetched:', result.data)
    } else {
      error.value = result.message || 'Failed to fetch inspection data'
      console.error('Failed to fetch inspection data:', result.error)
    }
  } catch (err) {
    console.error('Error fetching inspection data:', err)
    error.value = err.message || 'Failed to load inspection data'
  } finally {
    loading.value = false
  }
}

// Action handling functions
const initializeAction = () => {
  const action = route.query.action || 'view'
  currentAction.value = action
  
  switch (action) {
    case 'file_claim':
      // Show claim filing modal automatically
      setTimeout(() => openManualClaimModal(), 500)
      break
    case 'update':
      // Fetch claim data and show update modal
      setTimeout(() => handleUpdateAction(), 500)
      break
    case 'ai-analysis':
      // Show AI claim modal automatically
      setTimeout(() => openAIClaimModal(), 500)
      break
    case 'view':
      // Fetch claim data for viewing
      setTimeout(() => fetchClaimDataForView(), 500)
      break
    default:
      // Just view the inspection data (default behavior)
      break
  }
}

const fetchClaimDataForView = async () => {
  if (!insuranceId) return

  try {
    const result = await claimStore.getClaimsByInsuranceId(insuranceId)
    
    if (result.success && result.data) {
      claimData.value = result.data
    } else {
      console.log('No claim data found for viewing')
      claimData.value = null
    }
  } catch (error) {
    console.error('Error fetching claim for view:', error)
    claimData.value = null
  }
}

const handleUpdateAction = async () => {
  if (!insuranceId) {
    toastStore.error('Insurance ID not found')
    return
  }

  try {
    // Fetch existing claim data
    const result = await claimStore.getClaimsByInsuranceId(insuranceId)
    
    if (result.success && result.data) {
      claimData.value = result.data
      
      // Populate update form
      updateForm.value = {
        claimAmount: result.data.claimAmount || 0,
        damageAssessment: result.data.damageAssessment || ''
      }
      
      showUpdateModal.value = true
    } else {
      toastStore.error(result.message || 'No existing claim found to update')
    }
  } catch (error) {
    console.error('Error fetching claim for update:', error)
    toastStore.error('An error occurred while fetching claim data')
  }
}

const closeUpdateModal = () => {
  showUpdateModal.value = false
  updateForm.value = {
    claimAmount: 0,
    damageAssessment: ''
  }
  updateFiles.value = []
  claimData.value = null
}

const handleUpdateFileSelect = (event) => {
  const files = Array.from(event.target.files)
  updateFiles.value = [...updateFiles.value, ...files]
}

const removeUpdateFile = (index) => {
  updateFiles.value.splice(index, 1)
}

const submitUpdate = async () => {
  if (!claimData.value?.id) {
    toastStore.error('No claim ID found')
    return
  }

  if (!updateForm.value.claimAmount || updateForm.value.claimAmount <= 0) {
    toastStore.error('Please enter a valid claim amount')
    return
  }

  try {
    isUpdating.value = true

    const updateData = {
      claimAmount: updateForm.value.claimAmount,
      damageAssessment: updateForm.value.damageAssessment
    }

    const result = await claimStore.updateClaim(
      claimData.value.id,
      updateData,
      updateFiles.value
    )

    if (result.success) {
      toastStore.success('Claim updated successfully!')
      closeUpdateModal()
      
      // Navigate back to application detail
      setTimeout(() => {
        navigateToApplicationDetail()
      }, 1500)
    } else {
      toastStore.error(result.message || 'Failed to update claim')
    }
  } catch (error) {
    console.error('Error updating claim:', error)
    toastStore.error('An error occurred while updating the claim')
  } finally {
    isUpdating.value = false
  }
}

onMounted(() => {
  if (insuranceId) {
    fetchInspectionData()
    // Initialize action after data is loaded
    setTimeout(() => initializeAction(), 100)
  } else {
    error.value = 'No insurance ID provided'
    loading.value = false
  }
})
</script>

<style scoped>
/* Hide scrollbar for Chrome, Safari and Opera */
.overflow-y-auto::-webkit-scrollbar {
  display: none;
}

/* Hide scrollbar for IE, Edge and Firefox */
.overflow-y-auto {
  -ms-overflow-style: none;  /* IE and Edge */
  scrollbar-width: none;  /* Firefox */
}

.aspect-video {
  aspect-ratio: 16 / 9;
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
</style>