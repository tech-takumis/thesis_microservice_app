<template>
  <AuthenticatedLayout>
    <div class="h-full flex flex-col min-h-0 overflow-hidden">
      <!-- Fixed Header Section -->
      <div class="flex-shrink-0 mb-4">
        <!-- Breadcrumb Navigation -->
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
                <router-link
                  :to="{ name: 'agriculturist-submit-crop-data-detail', params: { id: applicationId, applicationTypeId: applicationTypeId } }"
                  class="ml-4 text-sm font-medium text-gray-500 hover:text-gray-700"
                >
                  Application Details
                </router-link>
              </div>
            </li>
            <li>
              <div class="flex items-center">
                <ChevronRightIcon class="flex-shrink-0 h-5 w-5 text-gray-400" />
                <span class="ml-4 text-sm font-medium text-gray-900">
                  AI Damage Analysis
                </span>
              </div>
            </li>
          </ol>
        </nav>

        <!-- Page Header -->
        <div class="flex items-start justify-between w-full">
          <div>
            <h1 class="text-2xl font-bold text-gray-900">Damage Claim Review</h1>
            <p class="text-sm text-gray-500 mt-1">
              Submitted by <span class="font-semibold text-gray-700">{{ farmerName }}</span> — {{ formatDate(applicationData?.submittedAt) }}
            </p>
          </div>
        </div>
      </div>

      <!-- Main Content Area - Scrollable -->
      <div class="flex-1 min-h-0 overflow-y-auto">
        <!-- Loading State -->
        <div v-if="loading" class="flex justify-center items-center h-64">
          <div class="animate-spin rounded-full h-32 w-32 border-b-2 border-blue-600"></div>
        </div>

        <!-- Error State -->
        <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-lg p-4">
          <p class="text-red-800">{{ error }}</p>
        </div>

        <!-- AI Analysis Content -->
        <div v-else class="space-y-10 overflow-x-hidden">
          <!-- Section: AI Damage Analysis -->
          <section class="grid grid-cols-1 lg:grid-cols-3 gap-8 items-start">
        <!-- AI Image + Confidence -->
        <div class="lg:col-span-2 bg-white border border-gray-200 rounded-2xl shadow-sm p-6">
          <!-- Display analyzed images as carousel if available -->
          <div v-if="aiResult?.leaf_analysis_images?.length > 0" class="mb-6">
            <div class="relative">
              <!-- Carousel Container -->
              <div 
                class="overflow-hidden rounded-xl shadow-sm carousel-container"
                role="region"
                aria-label="AI Analysis Images"
                @touchstart="handleTouchStart"
                @touchend="handleTouchEnd"
              >
                <div
                  class="flex transition-transform duration-300 ease-in-out"
                  :style="{ transform: `translateX(-${currentImageIndex * 100}%)` }"
                >
                  <div
                    v-for="(image, index) in aiResult.leaf_analysis_images"
                    :key="image.image_path"
                    class="w-full flex-shrink-0"
                  >
                    <div class="relative">
                      <!-- Loading placeholder -->
                      <div
                        v-if="imageLoadingStates[index]"
                        class="absolute inset-0 bg-gray-200 animate-pulse flex items-center justify-center"
                      >
                        <svg class="w-8 h-8 text-gray-400 animate-spin" fill="none" viewBox="0 0 24 24">
                          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                        </svg>
                      </div>

                      <img
                        :src="image.presigned_url"
                        :alt="`AI analysis ${image.image_type}`"
                        class="w-full h-80 object-cover transition-opacity duration-300"
                        :class="{ 'opacity-0': imageLoadingStates[index] }"
                        @load="handleImageLoad(index)"
                        @error="handleImageError(index)"
                      />

                      <!-- Image overlay with type -->
                      <div class="absolute bottom-0 left-0 right-0 bg-gradient-to-t from-black/70 to-transparent p-4">
                        <h5 class="text-white text-sm font-medium capitalize">
                          {{ image.image_type.replace('_', ' ') }}
                        </h5>
                        <p class="text-white/80 text-xs">
                          {{ image.width }} × {{ image.height }} px
                          <span v-if="image.file_size" class="ml-2">
                            • {{ formatFileSize(image.file_size) }}
                          </span>
                        </p>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Navigation Buttons -->
              <button
                v-if="aiResult.leaf_analysis_images.length > 1"
                class="absolute left-2 top-1/2 transform -translate-y-1/2 bg-white/90 hover:bg-white text-gray-800 rounded-full p-3 shadow-lg carousel-btn"
                :class="{ 'opacity-50 cursor-not-allowed': currentImageIndex === 0 }"
                :disabled="currentImageIndex === 0"
                aria-label="Previous image"
                :aria-disabled="currentImageIndex === 0"
                @click="previousImage"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"></path>
                </svg>
              </button>

              <button
                v-if="aiResult.leaf_analysis_images.length > 1"
                class="absolute right-2 top-1/2 transform -translate-y-1/2 bg-white/90 hover:bg-white text-gray-800 rounded-full p-3 shadow-lg carousel-btn"
                :class="{ 'opacity-50 cursor-not-allowed': currentImageIndex === aiResult.leaf_analysis_images.length - 1 }"
                :disabled="currentImageIndex === aiResult.leaf_analysis_images.length - 1"
                aria-label="Next image"
                :aria-disabled="currentImageIndex === aiResult.leaf_analysis_images.length - 1"
                @click="nextImage"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"></path>
                </svg>
              </button>

              <!-- Dots indicator -->
              <div
                v-if="aiResult.leaf_analysis_images.length > 1"
                class="flex justify-center space-x-3 mt-4"
                role="tablist"
                aria-label="Image navigation"
              >
                <button
                  v-for="(_, index) in aiResult.leaf_analysis_images"
                  :key="`dot-${index}`"
                  class="w-3 h-3 rounded-full carousel-dot focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2"
                  :class="index === currentImageIndex ? 'bg-blue-600 scale-125' : 'bg-gray-300 hover:bg-gray-400'"
                  role="tab"
                  :aria-label="`Go to image ${index + 1}`"
                  :aria-selected="index === currentImageIndex"
                  @click="goToImage(index)"
                ></button>
              </div>

              <!-- Image counter -->
              <div
                v-if="aiResult.leaf_analysis_images.length > 1"
                class="absolute top-4 right-4 bg-black/60 text-white text-xs px-3 py-1 rounded-full backdrop-blur-sm carousel-counter"
                aria-live="polite"
              >
                {{ currentImageIndex + 1 }} / {{ aiResult.leaf_analysis_images.length }}
              </div>
            </div>
          </div>

          <!-- Fallback to uploaded images if no analysis images -->
          <div v-else-if="applicationData?.fileUploads?.length > 0" class="mb-6">
            <img
              :src="applicationData.fileUploads[0]"
              alt="Uploaded crop damage image"
              class="w-full h-80 object-cover rounded-xl shadow-sm"
            />
          </div>

          <div v-if="aiResult">
            <h4 class="text-gray-800 font-semibold mb-4">
              Primary Classification:
              <span class="text-green-600 font-bold">{{ aiResult.result }}</span>
            </h4>

            <div class="mb-4">
              <p class="text-sm text-gray-600">{{ aiResult.prediction }}</p>
              <p class="text-sm text-gray-600">Severity: {{ aiResult.severity.toFixed(2) }}%</p>
              <p class="text-sm text-gray-600">Lesion Area: {{ aiResult.lesion_area }} px</p>
              <p class="text-sm text-gray-600">Total Leaf Area: {{ aiResult.leaf_area }} px</p>
            </div>

            <!-- Confidence Bars -->
            <div class="space-y-4">
              <div v-for="(prediction, index) in aiResult.top3_predictions" :key="index">
                <p class="text-sm font-medium text-gray-700 mb-1">{{ prediction.class_name }}</p>
                <div class="w-full bg-gray-200 rounded-full h-2">
                  <div
                    :class="getConfidenceBarColor(index)"
                    class="h-2 rounded-full"
                    :style="`width: ${prediction.confidence}%`"
                  ></div>
                </div>
                <p class="text-xs text-gray-500 mt-1">{{ parseFloat(prediction.confidence).toFixed(2) }}%</p>
              </div>
            </div>
          </div>

          <div v-else class="text-center py-8">
            <p class="text-gray-500">No AI analysis available</p>
          </div>
        </div>

        <!-- Supporting Documents -->
        <aside class="space-y-5">
          <h3 class="text-lg font-semibold text-gray-900">Supporting Documents</h3>

          <!-- Uploaded Documents -->
          <div v-if="applicationData?.fileUploads?.length > 0">
            <div
              v-for="(fileUrl, index) in applicationData.fileUploads"
              :key="index"
              class="border border-gray-200 rounded-xl my-2 p-5 bg-white shadow-sm hover:shadow-md transition"
            >
              <div class="flex justify-between items-center mb-2">
                <p class="font-medium text-gray-800">Document {{ index + 1 }}</p>
              </div>
              <p class="text-sm text-gray-500 mb-2">{{ aiResult ? aiResult.result : 'Pending Analysis' }}</p>
              <a :href="fileUrl" target="_blank" class="text-blue-600 text-sm hover:underline font-medium">View</a>
            </div>
          </div>

          <!-- Action Buttons -->
          <div class="flex flex-col space-y-3 pt-4">
            <button
              class="w-full bg-green-600 text-white py-2 rounded-md text-sm font-semibold hover:bg-green-700 transition"
            >
              Approved
            </button>
          </div>
        </aside>
      </section>
        </div>
      </div>
    </div>
  </AuthenticatedLayout>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AuthenticatedLayout from '@/layouts/AuthenticatedLayout.vue'
import { useApplicationStore } from '@/stores/applications'
import { useAIStore } from '@/stores/ai'
import {
  HomeIcon,
  ChevronRightIcon
} from '@heroicons/vue/24/outline'

const route = useRoute()
const router = useRouter()
const applicationStore = useApplicationStore()
const aiStore = useAIStore()

const applicationData = ref(null)
const aiResult = ref(null)
const loading = ref(true)
const error = ref(null)
const currentImageIndex = ref(0)
const imageLoadingStates = ref({})
const isAutoPlaying = ref(false)
const autoPlayInterval = ref(null)

// Get application ID and application type ID from route params
const applicationId = route.params.applicationId
const applicationTypeId = route.params.applicationTypeId

// Computed properties
const farmerName = computed(() => {
  const dynamicFields = applicationData.value?.dynamicFields
  if (!dynamicFields) return 'Unknown Farmer'

  // Try different field combinations for farmer name
  if (dynamicFields.farmer_name) return dynamicFields.farmer_name
  if (dynamicFields.first_name && dynamicFields.last_name) {
    return `${dynamicFields.first_name} ${dynamicFields.last_name}`
  }
  if (dynamicFields.first_name) return dynamicFields.first_name
  return 'Unknown Farmer'
})

const filteredDynamicFields = computed(() => {
  if (!applicationData.value?.dynamicFields) return {}

  const fields = applicationData.value.dynamicFields
  const filtered = {}

  // Filter out empty values and complex objects for display
  Object.keys(fields).forEach(key => {
    const value = fields[key]
    if (value !== null && value !== undefined && value !== '' &&
        typeof value !== 'object' && !key.includes('boundaries') && !key.includes('signature')) {
      filtered[key] = value
    }
  })

  return filtered
})

// Helper functions
const formatFieldLabel = (key) => {
  return key.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase())
}

const formatFieldValue = (value) => {
  if (typeof value === 'boolean') {
    return value ? 'Yes' : 'No'
  }
  if (typeof value === 'number') {
    return value.toString()
  }
  return value || 'N/A'
}

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
  } catch (e) {
    return dateString
  }
}

const getConfidenceBarColor = (index) => {
  const colors = ['bg-green-600', 'bg-yellow-400', 'bg-blue-400']
  return colors[index] || 'bg-gray-400'
}

// Carousel methods
const nextImage = () => {
  if (aiResult.value?.leaf_analysis_images && currentImageIndex.value < aiResult.value.leaf_analysis_images.length - 1) {
    currentImageIndex.value++
  }
}

const previousImage = () => {
  if (currentImageIndex.value > 0) {
    currentImageIndex.value--
  }
}

const goToImage = (index) => {
  if (aiResult.value?.leaf_analysis_images && index >= 0 && index < aiResult.value.leaf_analysis_images.length) {
    currentImageIndex.value = index
  }
}

// Touch/swipe support
let touchStartX = 0
let touchEndX = 0

const handleTouchStart = (event) => {
  touchStartX = event.touches[0].clientX
}

const handleTouchEnd = (event) => {
  touchEndX = event.changedTouches[0].clientX
  handleSwipe()
}

const handleSwipe = () => {
  const swipeThreshold = 50
  const diff = touchStartX - touchEndX

  if (Math.abs(diff) > swipeThreshold) {
    if (diff > 0) {
      // Swiped left - go to next image
      nextImage()
    } else {
      // Swiped right - go to previous image
      previousImage()
    }
  }
}

// Image loading handlers
const handleImageLoad = (index) => {
  imageLoadingStates.value[index] = false
}

const handleImageError = (index) => {
  imageLoadingStates.value[index] = false
  console.error(`Failed to load image at index ${index}`)
}

// Initialize loading states when AI result is available
const initializeImageLoadingStates = () => {
  if (aiResult.value?.leaf_analysis_images) {
    const states = {}
    aiResult.value.leaf_analysis_images.forEach((_, index) => {
      states[index] = true
    })
    imageLoadingStates.value = states
  }
}

// File size formatter
const formatFileSize = (bytes) => {
  if (bytes === 0) return '0 Bytes'
  const sizes = ['Bytes', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(1024))
  return parseFloat((bytes / Math.pow(1024, i)).toFixed(2)) + ' ' + sizes[i]
}

// Auto-play functionality
const startAutoPlay = () => {
  if (!aiResult.value?.leaf_analysis_images?.length || aiResult.value.leaf_analysis_images.length <= 1) return

  isAutoPlaying.value = true
  autoPlayInterval.value = setInterval(() => {
    if (currentImageIndex.value < aiResult.value.leaf_analysis_images.length - 1) {
      nextImage()
    } else {
      currentImageIndex.value = 0 // Loop back to first image
    }
  }, 3000) // Change image every 3 seconds
}

const stopAutoPlay = () => {
  isAutoPlaying.value = false
  if (autoPlayInterval.value) {
    clearInterval(autoPlayInterval.value)
    autoPlayInterval.value = null
  }
}



// Navigation function for breadcrumb
const navigateToApplicationList = () => {
  // Use applicationTypeId from route params for consistent navigation
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

// Fetch data
const fetchData = async () => {
  try {
    loading.value = true
    error.value = null

    // Fetch application data
    const appResponse = await applicationStore.fetchApplicationById(applicationId)
    if (!appResponse.success) {
      error.value = appResponse.error || 'Failed to fetch application data'
      return
    }
    applicationData.value = appResponse.data

    // Fetch AI result
    const aiResponse = await aiStore.getAiResult(applicationId)
    if (aiResponse.success) {
      aiResult.value = aiResponse.data
      // Initialize image loading states
      initializeImageLoadingStates()
    } else {
      console.warn('AI result not available:', aiResponse.message)
      // Don't throw error for AI result as it might not exist
    }

  } catch (err) {
    console.error('Error fetching data:', err)
    error.value = err.message || 'Failed to load data'
  } finally {
    loading.value = false
  }
}

// Keyboard navigation
const handleKeyPress = (event) => {
  if (!aiResult.value?.leaf_analysis_images?.length) return

  if (event.key === 'ArrowLeft') {
    previousImage()
  } else if (event.key === 'ArrowRight') {
    nextImage()
  }
}

// Watch for AI result changes to reset carousel
watch(aiResult, (newValue) => {
  currentImageIndex.value = 0
  if (newValue?.leaf_analysis_images) {
    initializeImageLoadingStates()
  }
})

onMounted(() => {
  if (applicationId && applicationTypeId) {
    fetchData()
  } else {
    error.value = 'Application ID or Application Type ID not found in route'
    loading.value = false
  }

  // Add keyboard event listener
  window.addEventListener('keydown', handleKeyPress)
})

onUnmounted(() => {
  // Remove keyboard event listener
  window.removeEventListener('keydown', handleKeyPress)
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

/* Carousel specific styles */
.carousel-container {
  touch-action: pan-y pinch-zoom;
}



/* Button hover effects */
.carousel-btn {
  backdrop-filter: blur(8px);
  transition: all 0.2s ease-in-out;
}

.carousel-btn:hover {
  backdrop-filter: blur(12px);
  transform: scale(1.05);
}

.carousel-btn:active {
  transform: scale(0.95);
}

/* Dots hover effects */
.carousel-dot {
  transition: all 0.2s ease-in-out;
}

.carousel-dot:hover {
  transform: scale(1.2);
}



/* Loading spinner animation */
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}

/* Mobile responsiveness */
@media (max-width: 640px) {
  .carousel-btn {
    width: 2rem;
    height: 2rem;
    padding: 0.25rem;
  }
  
  .carousel-counter {
    font-size: 0.75rem;
    padding: 0.25rem 0.5rem;
  }
}
</style>
