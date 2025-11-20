<template>
  <div class="min-h-screen bg-gradient-to-br from-slate-50 to-slate-100/50">
    <div class="max-w-7xl mx-auto px-8 py-8 sm:px-12 lg:px-16">
      <!-- Breadcrumb Navigation -->
      <nav class="flex mb-8" aria-label="Breadcrumb">
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
              @click="navigateBack"
            >
              Applications
            </button>
          </li>
          <li class="flex items-center">
            <ChevronRightIcon class="h-3 w-3 text-slate-300 mx-1" />
            <span class="text-xs font-medium text-slate-900">Inspection</span>
          </li>
        </ol>
      </nav>

      <!-- Page Header -->
      <div class="mb-8">
        <div class="flex items-start justify-between">
          <div>
            <h1 class="text-2xl font-light text-slate-900 tracking-tight">
              {{ isViewMode ? 'Inspection Details' : 'Field Inspection' }}
            </h1>
            <p class="mt-1 text-sm text-slate-500">
              {{ isViewMode ? 'View inspection findings and photos' : 'Record inspection findings and upload photos' }}
            </p>
          </div>
          <button
            v-if="insuranceData?.inspection?.schedule"
            @click="openScheduleModal"
            class="inline-flex items-center p-2 text-slate-400 hover:text-blue-600 hover:bg-blue-50/50 rounded-full transition-all duration-200"
            title="View Schedule Information"
          >
            <InformationCircleIcon class="h-5 w-5" />
          </button>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="flex justify-center items-center py-20">
        <LoadingSpinner message="Loading inspection data..." />
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="bg-red-50/50 border border-red-200/60 rounded-xl p-5 backdrop-blur-sm">
        <div class="flex">
          <ExclamationTriangleIcon class="h-5 w-5 text-red-500 mt-0.5 flex-shrink-0" />
          <div class="ml-3">
            <h3 class="text-sm font-medium text-red-900">Error Loading Inspection</h3>
            <p class="mt-1 text-sm text-red-700/80">{{ error }}</p>
          </div>
        </div>
      </div>

      <!-- Main Content -->
      <div v-else-if="insuranceData" class="space-y-6">
        <!-- Inspection Fields Form -->
        <div class="bg-white/70 backdrop-blur-sm rounded-2xl border border-slate-200/60 shadow-sm overflow-hidden">
          <div class="px-6 py-4 border-b border-slate-100/80">
            <h3 class="text-sm font-medium text-slate-700">Inspection Details</h3>
            <p class="mt-0.5 text-xs text-slate-500">Review and edit field information as needed</p>
          </div>
          <div class="p-6">
            <div v-if="filteredFields.length > 0 || boundaryFields.length > 0" class="space-y-6">
              <!-- Regular Fields -->
              <div class="grid grid-cols-1 md:grid-cols-2 gap-x-6 gap-y-5">
                <div v-for="field in filteredFields" :key="field.key" class="group">
                  <label :for="field.key" class="block text-xs font-semibold text-slate-700 mb-2 uppercase tracking-wider">
                    {{ field.label }}
                  </label>
                  <!-- Read-only CIC Number -->
                  <input
                    v-if="field.key === 'cic_no'"
                    :id="field.key"
                    type="text"
                    :value="inspectionFields[field.key]"
                    readonly
                    class="w-full px-4 py-3 mr-4 rounded-xl border-2 border-slate-200 bg-slate-50 text-slate-500 text-sm font-medium cursor-not-allowed focus:outline-none transition-all duration-200"
                  />
                  <!-- Text Input -->
                  <input
                    v-else-if="field.type === 'text' || field.type === 'number'"
                    :id="field.key"
                    :type="field.type"
                    v-model="inspectionFields[field.key]"
                    :readonly="isViewMode"
                    :class="[
                      'w-full px-4 py-3 mr-4 rounded-xl border-2 text-slate-900 text-sm font-medium transition-all duration-200',
                      isViewMode
                        ? 'border-slate-200 bg-slate-50 cursor-not-allowed'
                        : 'border-slate-200 bg-white placeholder:text-slate-400 focus:border-blue-400 focus:bg-blue-50/30 focus:outline-none focus:ring-4 focus:ring-blue-400/10 hover:border-slate-300'
                    ]"
                    @input="!isViewMode && markAsChanged()"
                  />
                  <!-- Date Input -->
                  <input
                    v-else-if="field.type === 'date'"
                    :id="field.key"
                    type="date"
                    v-model="inspectionFields[field.key]"
                    :readonly="isViewMode"
                    :class="[
                      'w-full px-4 py-3 rounded-xl mr-4 border-2 text-slate-900 text-sm font-medium transition-all duration-200',
                      isViewMode
                        ? 'border-slate-200 bg-slate-50 cursor-not-allowed'
                        : 'border-slate-200 bg-white focus:border-blue-400 focus:bg-blue-50/30 focus:outline-none focus:ring-4 focus:ring-blue-400/10 hover:border-slate-300'
                    ]"
                    @input="!isViewMode && markAsChanged()"
                  />
                  <!-- Textarea -->
                  <textarea
                    v-else-if="field.type === 'textarea'"
                    :id="field.key"
                    v-model="inspectionFields[field.key]"
                    rows="4"
                    :readonly="isViewMode"
                    :class="[
                      'w-full px-4 py-3 rounded-xl border-2 text-slate-900 text-sm font-medium resize-none transition-all duration-200',
                      isViewMode
                        ? 'border-slate-200 bg-slate-50 cursor-not-allowed'
                        : 'border-slate-200 bg-white placeholder:text-slate-400 focus:border-blue-400 focus:bg-blue-50/30 focus:outline-none focus:ring-4 focus:ring-blue-400/10 hover:border-slate-300'
                    ]"
                    @input="!isViewMode && markAsChanged()"
                  ></textarea>
                  <!-- Boolean/Checkbox -->
                  <div v-else-if="field.type === 'boolean'" class="flex items-center pt-2">
                    <div class="relative inline-flex items-center">
                      <input
                        :id="field.key"
                        type="checkbox"
                        v-model="inspectionFields[field.key]"
                        :disabled="isViewMode"
                        :class="[
                          'w-5 h-5 rounded-md border-2 text-blue-500 transition-all duration-200',
                          isViewMode
                            ? 'border-slate-300 cursor-not-allowed opacity-60'
                            : 'border-slate-300 focus:ring-4 focus:ring-blue-400/20 focus:border-blue-400 cursor-pointer'
                        ]"
                        @change="!isViewMode && markAsChanged()"
                      />
                      <label 
                        :for="field.key" 
                        :class="[
                          'ml-3 text-sm font-medium select-none',
                          isViewMode ? 'text-slate-500 cursor-not-allowed' : 'text-slate-700 cursor-pointer'
                        ]"
                      >
                        Yes
                      </label>
                    </div>
                  </div>
                  <!-- Default Text for Unknown Types -->
                  <input
                    v-else
                    :id="field.key"
                    type="text"
                    v-model="inspectionFields[field.key]"
                    :readonly="isViewMode"
                    :class="[
                      'w-full px-4 py-3 rounded-xl border-2 text-slate-900 text-sm font-medium transition-all duration-200',
                      isViewMode
                        ? 'border-slate-200 bg-slate-50 cursor-not-allowed'
                        : 'border-slate-200 bg-white placeholder:text-slate-400 focus:border-blue-400 focus:bg-blue-50/30 focus:outline-none focus:ring-4 focus:ring-blue-400/10 hover:border-slate-300'
                    ]"
                    @input="!isViewMode && markAsChanged()"
                  />
                </div>
              </div>

              <!-- Lot Boundaries Fields -->
              <div v-for="boundary in boundaryFields" :key="boundary.lotKey" class="border-t-2 border-slate-100 pt-8">
                <h4 class="text-sm font-semibold text-slate-800 mb-5 uppercase tracking-wider">{{ boundary.label }}</h4>
                <div class="grid grid-cols-1 md:grid-cols-2 gap-x-6 gap-y-5">
                  <div>
                    <label :for="`${boundary.lotKey}_north`" class="block text-xs font-semibold text-slate-700 mb-2 uppercase tracking-wider">
                      North Boundary
                    </label>
                    <input
                      :id="`${boundary.lotKey}_north`"
                      type="text"
                      v-model="inspectionFields[boundary.lotKey].north"
                      :readonly="isViewMode"
                      :class="[
                        'w-full px-4 py-3 rounded-xl border-2 text-slate-900 text-sm font-medium transition-all duration-200',
                        isViewMode
                          ? 'border-slate-200 bg-slate-50 cursor-not-allowed'
                          : 'border-slate-200 bg-white placeholder:text-slate-400 focus:border-blue-400 focus:bg-blue-50/30 focus:outline-none focus:ring-4 focus:ring-blue-400/10 hover:border-slate-300'
                      ]"
                      @input="!isViewMode && markAsChanged()"
                    />
                  </div>
                  <div>
                    <label :for="`${boundary.lotKey}_south`" class="block text-xs font-semibold text-slate-700 mb-2 uppercase tracking-wider">
                      South Boundary
                    </label>
                    <input
                      :id="`${boundary.lotKey}_south`"
                      type="text"
                      v-model="inspectionFields[boundary.lotKey].south"
                      :readonly="isViewMode"
                      :class="[
                        'w-full px-4 py-3 rounded-xl border-2 text-slate-900 text-sm font-medium transition-all duration-200',
                        isViewMode
                          ? 'border-slate-200 bg-slate-50 cursor-not-allowed'
                          : 'border-slate-200 bg-white placeholder:text-slate-400 focus:border-blue-400 focus:bg-blue-50/30 focus:outline-none focus:ring-4 focus:ring-blue-400/10 hover:border-slate-300'
                      ]"
                      @input="!isViewMode && markAsChanged()"
                    />
                  </div>
                  <div>
                    <label :for="`${boundary.lotKey}_east`" class="block text-xs font-semibold text-slate-700 mb-2 uppercase tracking-wider">
                      East Boundary
                    </label>
                    <input
                      :id="`${boundary.lotKey}_east`"
                      type="text"
                      v-model="inspectionFields[boundary.lotKey].east"
                      :readonly="isViewMode"
                      :class="[
                        'w-full px-4 py-3 rounded-xl border-2 text-slate-900 text-sm font-medium transition-all duration-200',
                        isViewMode
                          ? 'border-slate-200 bg-slate-50 cursor-not-allowed'
                          : 'border-slate-200 bg-white placeholder:text-slate-400 focus:border-blue-400 focus:bg-blue-50/30 focus:outline-none focus:ring-4 focus:ring-blue-400/10 hover:border-slate-300'
                      ]"
                      @input="!isViewMode && markAsChanged()"
                    />
                  </div>
                  <div>
                    <label :for="`${boundary.lotKey}_west`" class="block text-xs font-semibold text-slate-700 mb-2 uppercase tracking-wider">
                      West Boundary
                    </label>
                    <input
                      :id="`${boundary.lotKey}_west`"
                      type="text"
                      v-model="inspectionFields[boundary.lotKey].west"
                      :readonly="isViewMode"
                      :class="[
                        'w-full px-4 py-3 rounded-xl border-2 text-slate-900 text-sm font-medium transition-all duration-200',
                        isViewMode
                          ? 'border-slate-200 bg-slate-50 cursor-not-allowed'
                          : 'border-slate-200 bg-white placeholder:text-slate-400 focus:border-blue-400 focus:bg-blue-50/30 focus:outline-none focus:ring-4 focus:ring-blue-400/10 hover:border-slate-300'
                      ]"
                      @input="!isViewMode && markAsChanged()"
                    />
                  </div>
                </div>
              </div>
            </div>
            <div v-else class="text-center py-12">
              <DocumentIcon class="mx-auto h-12 w-12 text-slate-300" />
              <p class="mt-3 text-sm text-slate-500">No inspection fields available</p>
            </div>
          </div>
        </div>

        <!-- Photo Upload Section -->
        <div class="bg-white/70 backdrop-blur-sm rounded-2xl border border-slate-200/60 shadow-sm overflow-hidden">
          <div class="px-6 py-4 border-b border-slate-100/80">
            <h3 class="text-sm font-medium text-slate-700">Inspection Photos</h3>
            <p class="mt-0.5 text-xs text-slate-500">
              {{ isViewMode ? 'View photos of the crops and field conditions' : 'Upload photos of the crops and field conditions' }}
            </p>
          </div>
          <div class="p-6">
            <!-- File Upload Area - Hide in view mode -->
            <div v-if="isEditMode" class="mb-6">
              <label class="flex flex-col items-center justify-center w-full h-32 border-2 border-slate-200 border-dashed rounded-xl cursor-pointer bg-slate-50/50 hover:bg-slate-100/50 hover:border-slate-300 transition-all duration-200">
                <div class="flex flex-col items-center justify-center py-4">
                  <PhotoIcon class="w-10 h-10 mb-2 text-slate-400" />
                  <p class="mb-1 text-sm text-slate-600">
                    <span class="font-medium">Click to upload</span> or drag and drop
                  </p>
                  <p class="text-xs text-slate-500">PNG, JPG, JPEG up to 10MB</p>
                </div>
                <input
                  type="file"
                  class="hidden"
                  accept="image/*"
                  multiple
                  @change="handleFileUpload"
                />
              </label>
            </div>

            <!-- Uploaded Photos Preview -->
            <div v-if="uploadedPhotos.length > 0" class="grid grid-cols-2 md:grid-cols-4 gap-3 mb-6">
              <div
                v-for="(photo, index) in uploadedPhotos"
                :key="index"
                class="relative group aspect-square rounded-xl overflow-hidden bg-slate-100 border border-slate-200 hover:border-slate-300 transition-all duration-200"
              >
                <img
                  :src="photo.preview"
                  :alt="`Photo ${index + 1}`"
                  class="w-full h-full object-cover"
                />
                <button
                  v-if="isEditMode"
                  @click="removePhoto(index)"
                  class="absolute top-2 right-2 p-1.5 bg-red-500 hover:bg-red-600 text-white rounded-full opacity-0 group-hover:opacity-100 transition-all duration-200 shadow-lg"
                >
                  <XMarkIcon class="h-4 w-4" />
                </button>
                <div class="absolute bottom-0 inset-x-0 bg-gradient-to-t from-black/70 to-transparent text-white text-xs py-2 px-2.5 truncate">
                  {{ photo.file.name }}
                </div>
              </div>
            </div>

            <!-- Existing Photos (if any) -->
            <div v-if="insuranceData.inspection?.photos && insuranceData.inspection.photos.length > 0">
              <h4 class="text-xs font-medium text-slate-600 mb-3">Existing Photos</h4>
              <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
                <div
                  v-for="(photo, index) in insuranceData.inspection.photos"
                  :key="`existing-${index}`"
                  class="relative aspect-square rounded-xl overflow-hidden bg-slate-100 border border-slate-200 cursor-pointer hover:border-slate-300 hover:shadow-md transition-all duration-200"
                  @click="openImageModal(photo)"
                >
                  <img
                    :src="photo"
                    :alt="`Existing Photo ${index + 1}`"
                    class="w-full h-full object-cover"
                  />
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Action Buttons - Hide in view mode -->
        <div v-if="isEditMode" class="flex justify-end items-center pt-2">
          <button
            @click="submitInspection"
            :disabled="isSubmitting || !hasChanges"
            class="inline-flex items-center px-6 py-2.5 border border-transparent text-sm font-medium rounded-xl text-white bg-blue-500 hover:bg-blue-600 hover:shadow-lg hover:shadow-blue-500/30 transition-all duration-200 disabled:opacity-50 disabled:cursor-not-allowed disabled:hover:shadow-none"
          >
            <span v-if="!isSubmitting">
              <CheckCircleIcon class="w-4 h-4 mr-2 inline" />
              Complete Inspection
            </span>
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

      <!-- No Data State -->
      <div v-else class="text-center py-20">
        <DocumentIcon class="mx-auto h-16 w-16 text-slate-300" />
        <h3 class="mt-4 text-sm font-medium text-slate-900">No Data Available</h3>
        <p class="mt-1 text-sm text-slate-500">Unable to load inspection data.</p>
        <button
          @click="fetchInsuranceData"
          class="mt-6 inline-flex items-center px-5 py-2.5 border border-transparent text-sm font-medium rounded-xl text-white bg-blue-500 hover:bg-blue-600 hover:shadow-lg hover:shadow-blue-500/30 transition-all duration-200"
        >
          Try Again
        </button>
      </div>

      <!-- Schedule Information Modal -->
      <Transition name="fade">
        <div
          v-if="isScheduleModalOpen"
          @click="closeScheduleModal"
          class="fixed inset-0 bg-black/30 backdrop-blur-sm z-40"
        ></div>
      </Transition>

      <Transition name="slide-right">
        <div
          v-if="isScheduleModalOpen && insuranceData?.inspection?.schedule"
          class="fixed inset-y-0 right-0 z-50 w-96 bg-white shadow-2xl overflow-y-auto"
        >
          <!-- Modal Header -->
          <div class="sticky top-0 bg-white border-b border-slate-200 px-6 py-5">
            <div class="flex items-center justify-between">
              <h3 class="text-lg font-medium text-slate-900">Schedule Information</h3>
              <button
                @click="closeScheduleModal"
                class="text-slate-400 hover:text-slate-600 hover:bg-slate-100 p-1.5 rounded-lg transition-all duration-200"
              >
                <XMarkIcon class="h-5 w-5" />
              </button>
            </div>
          </div>

          <!-- Modal Body -->
          <div class="p-6 space-y-5">
            <div>
              <dt class="text-xs font-medium text-slate-500 uppercase tracking-wide">Schedule Type</dt>
              <dd class="mt-1.5 text-sm text-slate-900">{{ insuranceData.inspection.schedule.type }}</dd>
            </div>
            <div>
              <dt class="text-xs font-medium text-slate-500 uppercase tracking-wide">Scheduled Date</dt>
              <dd class="mt-1.5 text-sm text-slate-900">{{ formatDate(insuranceData.inspection.schedule.scheduleDate) }}</dd>
            </div>
            <div>
              <dt class="text-xs font-medium text-slate-500 uppercase tracking-wide">Priority</dt>
              <dd class="mt-1.5 text-sm">
                <span :class="[
                  'inline-flex items-center px-2.5 py-1 rounded-lg text-xs font-medium',
                  insuranceData.inspection.schedule.priority === 'HIGH' ? 'bg-red-100 text-red-700' :
                  insuranceData.inspection.schedule.priority === 'MEDIUM' ? 'bg-amber-100 text-amber-700' :
                  'bg-emerald-100 text-emerald-700'
                ]">
                  {{ insuranceData.inspection.schedule.priority }}
                </span>
              </dd>
            </div>
            <div>
              <dt class="text-xs font-medium text-slate-500 uppercase tracking-wide">Created At</dt>
              <dd class="mt-1.5 text-sm text-slate-900">{{ formatDate(insuranceData.inspection.schedule.createdAt) }}</dd>
            </div>
            <div>
              <dt class="text-xs font-medium text-slate-500 uppercase tracking-wide">Updated At</dt>
              <dd class="mt-1.5 text-sm text-slate-900">{{ formatDate(insuranceData.inspection.schedule.updatedAt) }}</dd>
            </div>
            <div v-if="insuranceData.inspection.schedule.notes">
              <dt class="text-xs font-medium text-slate-500 uppercase tracking-wide">Notes</dt>
              <dd class="mt-1.5 text-sm text-slate-700 whitespace-pre-wrap leading-relaxed">{{ insuranceData.inspection.schedule.notes }}</dd>
            </div>
          </div>
        </div>
      </Transition>

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
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useInsuranceStore } from '@/stores/insurance'
import { useInspectionStore } from '@/stores/inspection'
import { useToastStore } from '@/stores/toast'
import LoadingSpinner from '@/components/others/LoadingSpinner.vue'
import {
  HomeIcon,
  ChevronRightIcon,
  ExclamationTriangleIcon,
  DocumentIcon,
  XMarkIcon,
  CheckCircleIcon,
  PhotoIcon,
  InformationCircleIcon
} from '@heroicons/vue/24/outline'

const route = useRoute()
const router = useRouter()
const insuranceStore = useInsuranceStore()
const inspectionStore = useInspectionStore()
const toastStore = useToastStore()

const loading = ref(false)
const error = ref(null)
const insuranceData = ref(null)
const inspectionFields = ref({})
const uploadedPhotos = ref([])
const selectedImage = ref(null)
const isSubmitting = ref(false)
const hasChanges = ref(false)
const isScheduleModalOpen = ref(false)

// View mode handling
const currentAction = computed(() => route.query.action || 'edit')
const isViewMode = computed(() => currentAction.value === 'view')
const isEditMode = computed(() => currentAction.value === 'edit')

// Fetch insurance data
async function fetchInsuranceData() {
  const insuranceId = route.params.insuranceId

  if (!insuranceId) {
    error.value = 'No insurance ID provided'
    return
  }

  try {
    loading.value = true
    error.value = null

    const result = await insuranceStore.fetchInsuranceById(insuranceId)

    if (result.success) {
      insuranceData.value = result.data

      // Initialize inspection fields from verification data
      if (insuranceData.value?.verification?.fieldValues) {
        inspectionFields.value = { ...insuranceData.value.verification.fieldValues }
      }

      // If there's already inspection data, merge it
      if (insuranceData.value?.inspection?.fieldValues) {
        inspectionFields.value = {
          ...inspectionFields.value,
          ...insuranceData.value.inspection.fieldValues
        }
      }
    } else {
      error.value = result.message || 'Failed to load insurance data'
    }
  } catch (err) {
    console.error('Error fetching insurance data:', err)
    error.value = err.message || 'An unexpected error occurred'
  } finally {
    loading.value = false
  }
}

// Generate field label from key
const generateFieldLabel = (fieldKey) => {
  return fieldKey
    .replace(/_/g, ' ')
    .replace(/\b\w/g, char => char.toUpperCase())
    .replace(/\bCic\b/g, 'CIC')
    .replace(/\bId\b/g, 'ID')
    .replace(/\bNo\b/g, 'Number')
}

// Determine field type
const determineFieldType = (key, value) => {
  if (typeof value === 'boolean') return 'boolean'
  if (typeof value === 'number') return 'number'
  if (key.includes('date')) return 'date'
  if (key.includes('notes') || key.includes('remarks') || key.includes('description')) return 'textarea'
  return 'text'
}

// Filter and format fields for display (exclude boundary objects)
const filteredFields = computed(() => {
  if (!inspectionFields.value) return []

  const fields = []

  Object.entries(inspectionFields.value).forEach(([key, value]) => {
    // Skip certain fields
    if (key === 'farmer_signature') return

    // Skip boundary fields (they will be handled separately)
    if (key.includes('boundaries')) return

    // Skip null, undefined
    if (value === null || value === undefined) return

    // Skip empty strings
    if (typeof value === 'string' && value.trim() === '') return

    // Skip empty objects (but not boundary objects)
    if (typeof value === 'object' && !Array.isArray(value)) {
      const hasData = Object.values(value).some(v =>
        v && typeof v === 'string' && v.trim() !== ''
      )
      if (!hasData) return
    }

    fields.push({
      key,
      label: generateFieldLabel(key),
      type: determineFieldType(key, value),
      value
    })
  })

  return fields
})

// Extract boundary fields for special handling
const boundaryFields = computed(() => {
  if (!inspectionFields.value) return []

  const boundaries = []

  Object.entries(inspectionFields.value).forEach(([key, value]) => {
    // Only process boundary fields
    if (!key.includes('boundaries')) return

    // Extract lot number from key (e.g., "lot_1_boundaries" -> "1")
    const lotMatch = key.match(/lot_(\d+)_boundaries/)
    const lotNumber = lotMatch ? lotMatch[1] : null

    if (lotNumber) {
      // Check if corresponding lot area exists and is greater than 0
      const areaKey = `lot_${lotNumber}_area`
      const lotArea = inspectionFields.value[areaKey]

      // Skip if lot area is 0 or doesn't exist
      if (!lotArea || lotArea === 0) return
    }

    // Check if boundary has any data
    if (typeof value === 'object' && !Array.isArray(value)) {
      const hasData = Object.values(value).some(v =>
        v && typeof v === 'string' && v.trim() !== ''
      )

      // Only include boundaries with data
      if (hasData) {
        boundaries.push({
          lotKey: key,
          label: `Lot ${lotNumber} Boundaries`
        })
      }
    }
  })

  return boundaries
})

// Format date
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
    return 'Invalid Date'
  }
}

// Mark form as changed
const markAsChanged = () => {
  hasChanges.value = true
}

// Handle file upload
const handleFileUpload = (event) => {
  const files = Array.from(event.target.files)

  files.forEach(file => {
    if (file.size > 10 * 1024 * 1024) {
      toastStore.error('File size must be less than 10MB')
      return
    }

    const reader = new FileReader()
    reader.onload = (e) => {
      uploadedPhotos.value.push({
        file,
        preview: e.target.result
      })
      hasChanges.value = true
    }
    reader.readAsDataURL(file)
  })

  event.target.value = '' // Reset input
}

// Remove uploaded photo
const removePhoto = (index) => {
  uploadedPhotos.value.splice(index, 1)
  hasChanges.value = true
}

// Open image modal
const openImageModal = (imageUrl) => {
  selectedImage.value = imageUrl
}

// Close image modal
const closeImageModal = () => {
  selectedImage.value = null
}

// Open schedule modal
const openScheduleModal = () => {
  isScheduleModalOpen.value = true
}

// Close schedule modal
const closeScheduleModal = () => {
  isScheduleModalOpen.value = false
}

// Submit inspection
const submitInspection = async () => {
  // Check if we have an existing inspection ID or need to create a new one
  const insuranceId = route.params.insuranceId
  
  if (!insuranceId) {
    toastStore.error('Insurance ID not found')
    return
  }

  try {
    isSubmitting.value = true

    // Prepare photos array from uploaded photos
    const photos = uploadedPhotos.value.map(photo => photo.file)

    let result


      result = await inspectionStore.completeInspection(
        insuranceData.value.inspection.id,
        inspectionFields.value,
        photos
      )

    if (result.success) {
      toastStore.success(result.message || 'Inspection completed successfully!')
      hasChanges.value = false

      // Navigate back after a short delay
      setTimeout(() => {
        navigateBack()
      }, 1500)
    } else {
      toastStore.error(result.message || 'Failed to complete inspection')
    }
  } catch (err) {
    console.error('Error submitting inspection:', err)
    toastStore.error(err.message || 'An unexpected error occurred')
  } finally {
    isSubmitting.value = false
  }
}

// Navigate back
const navigateBack = () => {
  router.back()
}

onMounted(() => {
  fetchInsuranceData()
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
</style>
