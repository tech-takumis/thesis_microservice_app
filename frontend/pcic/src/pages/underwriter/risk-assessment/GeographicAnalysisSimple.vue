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
          <h2 class="text-2xl font-bold text-gray-900">Geographic Analysis</h2>
          <p class="mt-1 text-sm text-gray-600">
            Risk assessment and geographic analysis of agricultural insurance applications across the Philippines
          </p>
        </div>
      </div>
    </template>

    <!-- Main Content -->
    <div class="h-full flex flex-col space-y-6">
      <!-- Simple Map Container -->
      <div class="flex-1 bg-white rounded-lg shadow overflow-hidden">
        <div class="h-full">
          <!-- Map -->
          <div id="map" class="h-full w-full"></div>
          
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
      </div>
    </div>
  </AuthenticatedLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import AuthenticatedLayout from '@/layouts/AuthenticatedLayout.vue'
import LoadingSpinner from '@/components/others/LoadingSpinner.vue'
import { UNDERWRITER_NAVIGATION } from '@/lib/navigation'
import L from 'leaflet'
import 'leaflet/dist/leaflet.css'

// Navigation
const underwriterNavigation = UNDERWRITER_NAVIGATION

// Map state
const mapLoading = ref(true)

// Initialize map when component mounts
onMounted(() => {
  // Create map centered on Philippines
  const map = L.map('map').setView([12.8797, 121.7740], 6)

  // Add tile layer
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a> contributors'
  }).addTo(map)

  // Add sample markers for risk zones
  const riskZones = [
    {
      name: 'Central Luzon Rice Belt',
      coordinates: [15.4817, 120.5979],
      riskLevel: 'High',
      color: 'red'
    },
    {
      name: 'Iloilo Agricultural Zone', 
      coordinates: [10.7202, 122.5621],
      riskLevel: 'Medium',
      color: 'orange'
    },
    {
      name: 'Davao Fruit Farms',
      coordinates: [7.1907, 125.4553],
      riskLevel: 'Low', 
      color: 'green'
    }
  ]

  // Add markers
  riskZones.forEach(zone => {
    const marker = L.circleMarker(zone.coordinates, {
      radius: 20,
      color: zone.color,
      fillColor: zone.color,
      fillOpacity: 0.6
    }).addTo(map)

    marker.bindPopup(`
      <div class="p-2">
        <h3 class="font-semibold">${zone.name}</h3>
        <p class="text-sm">Risk Level: ${zone.riskLevel}</p>
      </div>
    `)
  })

  // Hide loading after map is ready
  setTimeout(() => {
    mapLoading.value = false
  }, 1000)
})
</script>

<style scoped>
/* Ensure map container has proper size */
#map {
  min-height: 500px;
}
</style>