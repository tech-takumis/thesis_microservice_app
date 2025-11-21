<template>
  <AuthenticatedLayout
    :navigation="navigation"
    :role-title="roleTitle"
    page-title="Application Location Map"
  >
    <div class="h-full flex flex-col min-h-0 overflow-hidden">
      <!-- Header Section -->
      <div class="flex-shrink-0 mb-4">
        <!-- Breadcrumb Navigation -->
        <nav class="flex mb-4" aria-label="Breadcrumb">
          <ol class="flex items-center space-x-4">
            <li>
              <div>
                <router-link
                  :to="dashboardRoute"
                  class="text-green-600 hover:text-green-700"
                >
                  <HomeIcon class="flex-shrink-0 h-5 w-5" />
                  <span class="sr-only">Dashboard</span>
                </router-link>
              </div>
            </li>
            <li>
              <div class="flex items-center">
                <ChevronRightIcon class="flex-shrink-0 h-5 w-5 text-gray-400" />
                <button
                  class="ml-4 text-sm font-medium text-gray-500 hover:text-gray-700"
                  @click="navigateBack"
                >
                  Application Details
                </button>
              </div>
            </li>
            <li>
              <div class="flex items-center">
                <ChevronRightIcon class="flex-shrink-0 h-5 w-5 text-gray-400" />
                <span class="ml-4 text-sm font-medium text-black">
                  Location Map
                </span>
              </div>
            </li>
          </ol>
        </nav>

        <!-- Header -->
        <div class="flex items-center justify-between">
          <div>
            <h1 class="text-3xl font-bold text-green-600">Application Location Map</h1>
            <p v-if="applicationData" class="mt-1 text-sm text-gray-600">
              {{ applicationData.dynamicFields?.farmer_name || applicationData.dynamicFields?.first_name + applicationData.dynamicFields?.last_name || 'Unknown Farmer' }} -
              {{ applicationData.applicationTypeName || 'Application' }}
            </p>
          </div>
        </div>
      </div>

      <!-- Loading State -->
      <div
        v-if="loading"
        class="flex-1 flex items-center justify-center bg-gray-50 rounded-lg"
      >
        <div class="text-center">
          <div class="relative mb-4">
            <div class="h-14 w-14 rounded-full border-4 border-gray-200 mx-auto"></div>
            <div class="absolute top-0 left-1/2 transform -translate-x-1/2 h-14 w-14 rounded-full border-4 border-green-600 border-t-transparent animate-spin"></div>
          </div>
          <p class="text-gray-600 font-medium">Loading map data...</p>
        </div>
      </div>

      <!-- Error State -->
      <div
        v-else-if="error"
        class="flex-1 flex items-center justify-center bg-red-50 rounded-lg p-6"
      >
        <div class="text-center">
          <ExclamationTriangleIcon class="h-12 w-12 text-red-500 mx-auto mb-4" />
          <h3 class="text-lg font-medium text-red-800 mb-2">Error Loading Map</h3>
          <p class="text-red-600">{{ error }}</p>
          <button
            @click="navigateBack"
            class="mt-4 inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-green-600 hover:bg-green-700"
          >
            Go Back
          </button>
        </div>
      </div>

      <!-- Map Container -->
      <div v-else-if="applicationData && coordinates" class="flex-1 min-h-0 flex gap-4">
        <!-- Map -->
        <div class="flex-1 bg-white rounded-lg shadow-sm overflow-hidden">
          <div id="map" class="w-full h-full"></div>
        </div>

        <!-- Application Info Sidebar -->
        <div class="w-80 bg-white rounded-lg shadow-sm p-4 overflow-y-auto">
          <h2 class="text-lg font-semibold text-gray-900 mb-4">Application Information</h2>

          <!-- Farmer Info -->
          <div class="mb-4 pb-4 border-b border-gray-200">
            <h3 class="text-sm font-medium text-gray-500 mb-2">Farmer Details</h3>
            <p class="text-sm text-gray-900 font-medium">
              {{ applicationData.dynamicFields?.farmer_name || applicationData.dynamicFields?.first_name + applicationData.dynamicFields?.last_name || 'N/A' }}
            </p>
            <p class="text-sm text-gray-600 mt-1">
              {{ applicationData.dynamicFields?.cell_phone_number || 'No phone' }}
            </p>
            <p class="text-sm text-gray-600 mt-1">
              {{ applicationData.dynamicFields?.address || 'No address' }}
            </p>
          </div>

          <!-- Application Type -->
          <div class="mb-4 pb-4 border-b border-gray-200">
            <h3 class="text-sm font-medium text-gray-500 mb-2">Application Type</h3>
            <p class="text-sm text-gray-900">{{ applicationData.applicationTypeName }}</p>
          </div>

          <!-- Farm Location -->
          <div class="mb-4 pb-4 border-b border-gray-200">
            <h3 class="text-sm font-medium text-gray-500 mb-2">Farm Location</h3>
            <p class="text-sm text-gray-900">
              {{ applicationData.dynamicFields?.farm_location || 'Not specified' }}
            </p>
            <p class="text-xs text-gray-500 mt-1">
              Coordinates: {{ coordinates.lat }}, {{ coordinates.lng }}
            </p>
          </div>

          <!-- Crop Details (if available) -->
          <div v-if="applicationData.dynamicFields?.insured_crops" class="mb-4 pb-4 border-b border-gray-200">
            <h3 class="text-sm font-medium text-gray-500 mb-2">Crop Information</h3>
            <div class="space-y-1">
              <p class="text-sm text-gray-900">
                <span class="font-medium">Crop:</span> {{ applicationData.dynamicFields.insured_crops }}
              </p>
              <p v-if="applicationData.dynamicFields.variety_planted" class="text-sm text-gray-900">
                <span class="font-medium">Variety:</span> {{ applicationData.dynamicFields.variety_planted }}
              </p>
              <p v-if="applicationData.dynamicFields.planting_method" class="text-sm text-gray-900">
                <span class="font-medium">Method:</span> {{ applicationData.dynamicFields.planting_method }}
              </p>
            </div>
          </div>

          <!-- Area Details -->
          <div v-if="applicationData.dynamicFields?.lot_1_area" class="mb-4 pb-4 border-b border-gray-200">
            <h3 class="text-sm font-medium text-gray-500 mb-2">Area Information</h3>
            <div class="space-y-1">
              <p v-if="applicationData.dynamicFields.lot_1_area" class="text-sm text-gray-900">
                <span class="font-medium">Lot 1:</span> {{ applicationData.dynamicFields.lot_1_area }} ha
              </p>
              <p v-if="applicationData.dynamicFields.lot_2_area > 0" class="text-sm text-gray-900">
                <span class="font-medium">Lot 2:</span> {{ applicationData.dynamicFields.lot_2_area }} ha
              </p>
              <p v-if="applicationData.dynamicFields.lot_3_area > 0" class="text-sm text-gray-900">
                <span class="font-medium">Lot 3:</span> {{ applicationData.dynamicFields.lot_3_area }} ha
              </p>
              <p v-if="applicationData.dynamicFields.lot_4_area > 0" class="text-sm text-gray-900">
                <span class="font-medium">Lot 4:</span> {{ applicationData.dynamicFields.lot_4_area }} ha
              </p>
              <p v-if="applicationData.dynamicFields.area_insured" class="text-sm text-gray-900 font-medium mt-2">
                <span class="font-semibold">Total Insured:</span> {{ applicationData.dynamicFields.area_insured }} ha
              </p>
            </div>
          </div>

          <!-- Submitted Date -->
          <div class="mb-4">
            <h3 class="text-sm font-medium text-gray-500 mb-2">Submission Date</h3>
            <p class="text-sm text-gray-900">
              {{ formatDate(applicationData.submittedAt) }}
            </p>
          </div>

          <!-- Images (if available) -->
          <div v-if="applicationData.fileUploads && applicationData.fileUploads.length > 0" class="mb-4">
            <h3 class="text-sm font-medium text-gray-500 mb-2">Attached Images</h3>
            <div class="grid grid-cols-2 gap-2">
              <img
                v-for="(image, index) in applicationData.fileUploads.slice(0, 4)"
                :key="index"
                :src="image"
                :alt="`Application image ${index + 1}`"
                class="w-full h-20 object-cover rounded border border-gray-200 cursor-pointer hover:opacity-75"
                @click="openImageModal(image)"
              />
            </div>
            <p v-if="applicationData.fileUploads.length > 4" class="text-xs text-gray-500 mt-2">
              +{{ applicationData.fileUploads.length - 4 }} more images
            </p>
          </div>
        </div>
      </div>

      <!-- No Coordinates State -->
      <div
        v-else-if="applicationData && !coordinates"
        class="flex-1 flex items-center justify-center bg-yellow-50 rounded-lg p-6"
      >
        <div class="text-center">
          <svg class="h-12 w-12 text-yellow-500 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 20l-5.447-2.724A1 1 0 013 16.382V5.618a1 1 0 011.447-.894L9 7m0 13l6-3m-6 3V7m6 10l4.553 2.276A1 1 0 0021 18.382V7.618a1 1 0 00-.553-.894L15 4m0 13V4m0 0L9 7"></path>
          </svg>
          <h3 class="text-lg font-medium text-yellow-800 mb-2">No Location Data</h3>
          <p class="text-yellow-600">This application does not have coordinates information.</p>
          <button
            @click="navigateBack"
            class="mt-4 inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-green-600 hover:bg-green-700"
          >
            Go Back
          </button>
        </div>
      </div>
    </div>
  </AuthenticatedLayout>
</template>

<script setup>
import { ref, onMounted, computed, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useApplicationStore } from '@/stores/application'
import AuthenticatedLayout from '@/layouts/AuthenticatedLayout.vue'
import {
  ADMIN_NAVIGATION,
  UNDERWRITER_NAVIGATION
} from '@/lib/navigation'
import {
  HomeIcon,
  ChevronRightIcon,
  ArrowLeftIcon,
  ExclamationTriangleIcon
} from '@heroicons/vue/24/outline'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()
const applicationStore = useApplicationStore()

// State
const loading = ref(false)
const error = ref(null)
const applicationData = ref(null)
let map = null
let marker = null

// Computed
const navigation = computed(() => {
  const role = authStore.userData?.roles?.[0]?.name
  if (role === 'ADMIN') return ADMIN_NAVIGATION
  if (role === 'UNDERWRITER') return UNDERWRITER_NAVIGATION
  // Return underwriter navigation as default for other roles accessing this page
  return UNDERWRITER_NAVIGATION
})

const roleTitle = computed(() => {
  const role = authStore.userData?.roles?.[0]?.name
  return role || 'Staff Portal'
})

const dashboardRoute = computed(() => {
  const role = authStore.userData?.roles?.[0]?.name
  if (role === 'ADMIN') return { name: 'admin-dashboard' }
  if (role === 'UNDERWRITER') return { name: 'underwriter-dashboard' }
  if (role === 'CLAIM_PROCESSOR') return { name: 'claims-processor-dashboard' }
  if (role === 'TELLER') return { name: 'teller-dashboard' }
  // Default to underwriter dashboard for other roles
  return { name: 'underwriter-dashboard' }
})

const coordinates = computed(() => {
  if (!applicationData.value?.coordinates) return null

  const [lat, lng] = applicationData.value.coordinates.split(',').map(coord => parseFloat(coord.trim()))

  if (isNaN(lat) || isNaN(lng)) return null

  return { lat, lng }
})

// Methods
const fetchApplicationData = async () => {
  loading.value = true
  error.value = null

  const applicationId = route.params.applicationId

  if (!applicationId) {
    error.value = 'No application ID provided'
    loading.value = false
    return
  }

  try {
    console.log('Fetching application data for ID:', applicationId)
    const result = await applicationStore.fetchApplicationById(applicationId)

    if (result.success) {
      applicationData.value = result.data
      console.log('Application data fetched:', applicationData.value)
    } else {
      error.value = result.error || 'Failed to load application data'
      console.error('Failed to fetch application:', result.error)
    }
  } catch (err) {
    console.error('Error fetching application:', err)
    error.value = err.message || 'An unexpected error occurred'
  } finally {
    loading.value = false
  }
}

const initializeMap = () => {
  if (!coordinates.value || map) return

  // Initialize Leaflet map
  map = window.L.map('map').setView([coordinates.value.lat, coordinates.value.lng], 15)

  // Add OpenStreetMap tile layer
  window.L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors',
    maxZoom: 19
  }).addTo(map)

  // Create custom popup content
  const popupContent = `
    <div class="p-2">
      <h3 class="font-semibold text-sm mb-2">${applicationData.dynamicFields?.farmer_name || applicationData.dynamicFields?.first_name + applicationData.dynamicFields?.last_name || 'Unknown Farmer'}</h3>
      <div class="text-xs space-y-1">
        <p><strong>Type:</strong> ${applicationData.value.applicationTypeName}</p>
        ${applicationData.value.dynamicFields?.farm_location ? `<p><strong>Location:</strong> ${applicationData.value.dynamicFields.farm_location}</p>` : ''}
        ${applicationData.value.dynamicFields?.insured_crops ? `<p><strong>Crop:</strong> ${applicationData.value.dynamicFields.insured_crops}</p>` : ''}
        ${applicationData.value.dynamicFields?.area_insured ? `<p><strong>Area:</strong> ${applicationData.value.dynamicFields.area_insured} ha</p>` : ''}
      </div>
    </div>
  `

  // Add marker with popup
  marker = window.L.marker([coordinates.value.lat, coordinates.value.lng])
    .addTo(map)
    .bindPopup(popupContent)
    .openPopup()

  // Add circle to show approximate area
  window.L.circle([coordinates.value.lat, coordinates.value.lng], {
    color: '#16a34a',
    fillColor: '#22c55e',
    fillOpacity: 0.2,
    radius: 100
  }).addTo(map)
}

const formatDate = (dateString) => {
  if (!dateString) return 'N/A'
  return new Date(dateString).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const navigateBack = () => {
  router.back()
}

const openImageModal = (imageUrl) => {
  window.open(imageUrl, '_blank')
}

// Lifecycle
onMounted(async () => {
  // Load Leaflet CSS and JS
  const link = document.createElement('link')
  link.rel = 'stylesheet'
  link.href = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.css'
  document.head.appendChild(link)

  const script = document.createElement('script')
  script.src = 'https://unpkg.com/leaflet@1.9.4/dist/leaflet.js'
  script.onload = async () => {
    await fetchApplicationData()
    if (coordinates.value) {
      // Wait a bit for the map container to be ready
      setTimeout(initializeMap, 100)
    }
  }
  document.head.appendChild(script)
})

onUnmounted(() => {
  // Clean up map instance
  if (map) {
    map.remove()
    map = null
  }
})
</script>

<style scoped>
#map {
  min-height: 400px;
}

/* Ensure the map container takes full height */
.flex-1 {
  min-height: 0;
}
</style>
