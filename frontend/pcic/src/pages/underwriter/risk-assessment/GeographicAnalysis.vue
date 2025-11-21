<template>
  <AuthenticatedLayout
    :navigation="underwriterNavigation"
    role-title="Underwriter Portal"
    page-title="Geographic Analysis"
  >
    <!-- Header -->
    <template #header>
      <div class="flex items-center justify-between">
        <div>
          <h2 class="text-2xl font-semibold text-green-600">Geographic Analysis</h2>
          <p class="mt-1 text-sm text-gray-600">
            Risk assessment and geographic analysis of agricultural insurance applications across the Philippines
          </p>
        </div>
        <div class="flex items-center space-x-3">
          <!-- Refresh Button -->
          <button
            @click="fetchApplications"
            :disabled="loading"
            class="inline-flex items-center px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-white bg-green-600 hover:bg-green-700 disabled:opacity-50"
          >
            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
            </svg>
            Refresh
          </button>
        </div>
      </div>
    </template>

    <!-- Main Content -->
    <div class="h-full flex flex-col space-y-6">

      <!-- Map and Legend Container -->
      <div class="flex-1 bg-gray-100 overflow-hidden">
        <div class="h-full flex">
          <!-- Map -->
          <div class="flex-1 relative">
            <l-map
              ref="mapRef"
              v-model:zoom="zoom"
              :center="center"
              :options="mapOptions"
              class="h-full w-full"
              @ready="onMapReady"
            >
              <!-- Tile Layer -->
              <l-tile-layer
                :url="tileUrl"
                :attribution="attribution"
              />
              
              <!-- Application Markers -->
              <l-circle-marker
                v-for="marker in filteredApplications"
                :key="marker.id"
                :lat-lng="marker.coordinates"
                :radius="marker.radius"
                :color="marker.color"
                :fillColor="marker.fillColor"
                :fillOpacity="0.7"
                :weight="marker.weight"
              >
                <l-popup>
                  <div class="p-3 min-w-64">
                    <h3 class="font-semibold text-gray-900 mb-2">{{ marker.name }}</h3>
                    <div class="space-y-1 text-sm">
                      <p class="text-gray-600">
                        <span class="font-medium">Type:</span> {{ marker.applicationTypeName }}
                      </p>
                      <p class="text-gray-600">
                        <span class="font-medium">Crop:</span> {{ marker.crop }}
                      </p>
                      <p class="text-gray-600">
                        <span class="font-medium">Area:</span> {{ marker.area }} hectares
                      </p>
                      <p class="text-gray-600">
                        <span class="font-medium">Location:</span> {{ marker.location }}
                      </p>
                      <p class="text-gray-600">
                        <span class="font-medium">Submitted:</span> {{ formatDate(marker.submittedAt) }}
                      </p>
                    </div>
                  </div>
                </l-popup>
              </l-circle-marker>


            </l-map>
            
            <!-- Loading overlay -->
            <div
              v-if="mapLoading"
              class="absolute inset-0 bg-white bg-opacity-75 flex items-center justify-center z-10"
            >
              <div class="text-center">
                <LoadingSpinner />
                <p class="mt-2 text-sm text-gray-600">Loading geographic data...</p>
              </div>
            </div>
          </div>

          <!-- Applications List Panel -->
          <div class="w-80 bg-gray-50 border-gray-200 overflow-y-auto">
            <div class="p-6">
              <h3 class="font-semibold text-gray-900 mb-4">
                Applications ({{ filteredApplications.length }})
              </h3>
              
              <!-- Application Type Legend -->
              <div class="space-y-2 mb-6">
                <div class="flex items-center">
                  <div class="w-4 h-4 rounded-full bg-green-600 mr-3"></div>
                  <span class="text-sm text-gray-700">Insurance Applications</span>
                </div>
                <div class="flex items-center">
                  <div class="w-4 h-4 rounded-full bg-red-600 mr-3"></div>
                  <span class="text-sm text-gray-700">Claims for Indemnity</span>
                </div>
                <div class="flex items-center">
                  <div class="w-4 h-4 rounded-full bg-yellow-400 mr-3"></div>
                  <span class="text-sm text-gray-700">Inspection Requests</span>
                </div>
              </div>

              <!-- Loading State -->
              <div v-if="loading" class="text-center py-8">
                <LoadingSpinner />
                <p class="text-sm text-gray-600 mt-2">Loading applications...</p>
              </div>

              <!-- Applications List -->
              <div v-else class="space-y-3">
                <div 
                  v-for="app in filteredApplications.slice(0, 20)" 
                  :key="app.id"
                  class="bg-gray-100 rounded-xl p-3 border border-gray-300 hover:bg-green-50 cursor-pointer"
                  @click="focusOnApplication(app)"
                >
                  <div class="flex items-start">
                    <div 
                      class="w-3 h-3 rounded-full mt-1 mr-3 flex-shrink-0"
                      :style="{ backgroundColor: app.color }"
                    ></div>
                    <div class="min-w-0 flex-1">
                      <p class="text-sm font-medium text-gray-900 truncate">{{ app.name }}</p>
                      <p class="text-xs text-gray-600 truncate">{{ app.crop }} • {{ app.area }}ha</p>
                      <p class="text-xs text-gray-500 truncate">{{ app.location }}</p>
                      <p class="text-xs text-gray-400">{{ formatDate(app.submittedAt) }}</p>
                    </div>
                  </div>
                </div>
                
                <div v-if="filteredApplications.length === 0" class="text-center py-8">
                  <p class="text-sm text-gray-500">No applications found</p>
                </div>
                
                <div v-if="filteredApplications.length > 20" class="text-center py-2">
                  <p class="text-xs text-gray-500">
                    Showing 20 of {{ filteredApplications.length }} applications
                  </p>
                </div>
              </div>

              <!-- Statistics -->
              <div class="mt-6 pt-6 border-t border-gray-200">
                <h4 class="font-medium text-gray-900 mb-3">Summary</h4>
                <div class="space-y-2 text-sm">
                  <div class="flex justify-between">
                    <span class="text-gray-600">Total Applications:</span>
                    <span class="font-medium">{{ applications.length }}</span>
                  </div>
                  <div class="flex justify-between">
                    <span class="text-gray-600">With Coordinates:</span>
                    <span class="font-medium">{{ applicationMarkers.length }}</span>
                  </div>
                  <div class="flex justify-between">
                    <span class="text-gray-600">Total Area:</span>
                    <span class="font-medium">{{ totalArea.toFixed(1) }}ha</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </AuthenticatedLayout>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import AuthenticatedLayout from '@/layouts/AuthenticatedLayout.vue'
import LoadingSpinner from '@/components/others/LoadingSpinner.vue'
import { useApplicationStore } from '@/stores/application'

// Import Leaflet and Vue Leaflet components
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'
import { LMap, LTileLayer, LCircleMarker, LPopup, LControl } from '@vue-leaflet/vue-leaflet'
import { UNDERWRITER_NAVIGATION } from '@/lib/navigation'

// Fix Leaflet default marker icons issue
import iconRetinaUrl from 'leaflet/dist/images/marker-icon-2x.png'
import iconUrl from 'leaflet/dist/images/marker-icon.png'
import shadowUrl from 'leaflet/dist/images/marker-shadow.png'

// Configure default marker icons
delete L.Icon.Default.prototype._getIconUrl
L.Icon.Default.mergeOptions({
  iconRetinaUrl,
  iconUrl,
  shadowUrl,
})

// Navigation and role data
const underwriterNavigation = UNDERWRITER_NAVIGATION
const applicationStore = useApplicationStore()

// Applications data
const applications = ref([])
const loading = ref(false)

// Map configuration
const mapRef = ref(null)
const zoom = ref(6)
const center = ref([12.8797, 121.7740]) // Philippines center coordinates
const mapLoading = ref(true)

const mapOptions = {
  zoomControl: true,
  attributionControl: true
}

const tileUrl = 'https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png'
const attribution = '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'

// Map view state
const mapView = ref('risk')
const selectedRegion = ref('')

// Risk filters
const riskFilters = ref({
  high: true,
  medium: true,
  low: true
})

// Statistics removed - no longer using static cards

// Philippine regions
const regions = ref([
  'National Capital Region',
  'Cordillera Administrative Region',
  'Region I - Ilocos Region',
  'Region II - Cagayan Valley',
  'Region III - Central Luzon',
  'Region IV-A - CALABARZON',
  'Region IV-B - MIMAROPA',
  'Region V - Bicol Region',
  'Region VI - Western Visayas',
  'Region VII - Central Visayas',
  'Region VIII - Eastern Visayas',
  'Region IX - Zamboanga Peninsula',
  'Region X - Northern Mindanao',
  'Region XI - Davao Region',
  'Region XII - SOCCSKSARGEN',
  'Region XIII - Caraga',
  'Autonomous Region in Muslim Mindanao'
])

// Helper functions to extract information from different application types
const extractFarmerName = (app) => {
  const fields = app.dynamicFields || {}
  
  // Try different field combinations for farmer name
  if (fields.farmer_name) return fields.farmer_name
  if (fields.first_name && fields.last_name) {
    return `${fields.first_name} ${fields.middle_name || ''} ${fields.last_name}`.trim()
  }
  if (fields.applicant_name) return fields.applicant_name
  if (fields.name) return fields.name
  
  return 'Unknown Farmer'
}

const extractLocation = (app) => {
  const fields = app.dynamicFields || {}
  
  if (fields.farm_location) return fields.farm_location
  if (fields.address) return fields.address
  
  // Try to construct from lot location if available
  if (fields.lot_1_location && typeof fields.lot_1_location === 'object') {
    const loc = fields.lot_1_location
    return `${loc.barangay || ''}, ${loc.city || ''}, ${loc.province || ''}`
      .replace(/^,\s*|,\s*$/g, '')
      .replace(/,\s*,/g, ',')
  }
  
  return 'Unknown Location'
}

const extractCropInfo = (app) => {
  const fields = app.dynamicFields || {}
  
  if (fields.insured_crops) return fields.insured_crops
  if (fields.crop_type) return fields.crop_type
  if (fields.lot_1_variety) return fields.lot_1_variety
  if (fields.variety_planted) return fields.variety_planted
  
  return 'Unknown Crop'
}

const extractArea = (app) => {
  const fields = app.dynamicFields || {}
  
  if (fields.area_insured) return parseFloat(fields.area_insured) || 0
  if (fields.lot_1_area) return parseFloat(fields.lot_1_area) || 0
  if (fields.total_area) return parseFloat(fields.total_area) || 0
  
  return 0
}

const getApplicationTypeColor = (applicationTypeName) => {
  switch (applicationTypeName?.toLowerCase()) {
    case 'crop insurance application':
      return { color: '#22c55e', fillColor: '#22c55e' } // Green for insurance applications
    case 'claim for indemnity':
      return { color: '#ef4444', fillColor: '#ef4444' } // Red for claims
    case 'inspection request':
      return { color: '#eab308', fillColor: '#eab308' } // Yellow for inspections
    default:
      return { color: '#6b7280', fillColor: '#6b7280' } // Gray for others
  }
}

// Computed properties for map markers
const applicationMarkers = computed(() => {
  return applications.value
    .filter(app => app.coordinates)
    .map(app => {
      const [lat, lng] = app.coordinates.split(',').map(coord => parseFloat(coord.trim()))
      const colors = getApplicationTypeColor(app.applicationTypeName)
      
      return {
        id: app.id,
        name: extractFarmerName(app),
        coordinates: [lat, lng],
        radius: 15,
        ...colors,
        weight: 2,
        applicationTypeName: app.applicationTypeName,
        location: extractLocation(app),
        crop: extractCropInfo(app),
        area: extractArea(app),
        submittedAt: app.submittedAt,
        dynamicFields: app.dynamicFields
      }
    })
})

// Computed properties
const filteredApplications = computed(() => {
  let filtered = [...applicationMarkers.value]
  
  if (selectedRegion.value) {
    filtered = filtered.filter(app => 
      app.location.toLowerCase().includes(selectedRegion.value.toLowerCase())
    )
  }
  
  return filtered
})

const totalArea = computed(() => {
  return applicationMarkers.value.reduce((sum, app) => sum + app.area, 0)
})

// Methods
const fetchApplications = async () => {
  try {
    loading.value = true
    const result = await applicationStore.fetchAllApplications()
    
    if (result.success && result.data) {
      applications.value = result.data
      console.log('Loaded applications:', result.data.length)
    } else {
      console.warn('No applications found or failed to fetch')
      applications.value = []
    }
  } catch (error) {
    console.error('Failed to fetch applications:', error)
    applications.value = []
  } finally {
    loading.value = false
  }
}

const onMapReady = () => {
  console.log('Map is ready!')
  setTimeout(() => {
    mapLoading.value = false
  }, 500)
}

const filterByRegion = () => {
  console.log('Filtering by region:', selectedRegion.value)
}

const focusOnApplication = (app) => {
  if (mapRef.value?.leafletObject) {
    mapRef.value.leafletObject.setView(app.coordinates, 15)
  }
}

const formatDate = (dateString) => {
  if (!dateString) return 'N/A'
  return new Date(dateString).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

// Lifecycle hooks
onMounted(() => {
  fetchApplications()
  setTimeout(() => {
    mapLoading.value = false
  }, 1500)
})

onUnmounted(() => {
  // Cleanup if needed
})
</script>

<style scoped>
/* Custom styles for the map */
.leaflet-container {
  height: 100%;
  width: 100%;
  background: #f8fafc;
}

/* Custom popup styles */
:deep(.leaflet-popup-content-wrapper) {
  border-radius: 8px;
  box-shadow: 0 10px 15px -3px rgb(0 0 0 / 0.1), 0 4px 6px -4px rgb(0 0 0 / 0.1);
}

:deep(.leaflet-popup-tip) {
  background: white;
}

/* Control panel styles */
:deep(.leaflet-control) {
  border: none !important;
  background: none !important;
  margin: 10px !important;
}

/* Loading spinner overlay */
.loading-overlay {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(2px);
}
</style>