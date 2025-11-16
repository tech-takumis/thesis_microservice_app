<template>
  <AuthenticatedLayout>
    <div class="px-4 sm:px-6 lg:px-8 py-8">
      <!-- Header -->
      <div class="mb-8">
        <h1 class="text-3xl font-bold text-gray-900">Submit Crop Data</h1>
        <p class="mt-1 text-sm text-gray-600">
          Manage different types of agricultural applications and submissions
        </p>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="flex justify-center items-center py-12">
        <LoadingSpinner />
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-md p-4 mb-6">
        <div class="flex">
          <div class="flex-shrink-0">
            <ExclamationTriangleIcon class="h-5 w-5 text-red-400" />
          </div>
          <div class="ml-3">
            <h3 class="text-sm font-medium text-red-800">Error Loading Application Types</h3>
            <div class="mt-2 text-sm text-red-700">{{ error }}</div>
          </div>
        </div>
      </div>

      <!-- Application Types Grid -->
      <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        <div
          v-for="applicationType in applicationTypes"
          :key="applicationType.id"
          @click="navigateToApplicationType(applicationType.id)"
          class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 hover:shadow-md hover:border-indigo-300 transition-all duration-200 cursor-pointer group"
        >
          <div class="flex items-center justify-between">
            <div class="flex items-center min-w-0">
              <div class="p-3 rounded-lg bg-indigo-100 flex-shrink-0 group-hover:bg-indigo-200 transition-colors">
                <component :is="getApplicationTypeIcon(applicationType)" class="h-6 w-6 text-indigo-600" />
              </div>
              <div class="ml-4 min-w-0 flex-1">
                <h3 class="text-lg font-semibold text-gray-900 truncate group-hover:text-indigo-600 transition-colors">
                  {{ applicationType.name }}
                </h3>
                <p class="text-sm text-gray-500 truncate">{{ applicationType.description }}</p>
              </div>
            </div>
          </div>

          <div class="mt-4 flex items-center justify-between">
            <div class="flex items-center space-x-4">
              <div class="flex items-center space-x-2">
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-blue-100 text-blue-800">
                  {{ applicationType.provider }}
                </span>
                <span class="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
                  {{ applicationType.layout }}
                </span>
              </div>
              <div v-if="applicationType.printable" class="flex items-center text-green-600">
                <PrinterIcon class="h-4 w-4 mr-1" />
                <span class="text-xs font-medium">Printable</span>
              </div>
            </div>
            <ChevronRightIcon class="h-5 w-5 text-gray-400 group-hover:text-indigo-600 transition-colors" />
          </div>
        </div>
      </div>

      <!-- Empty State -->
      <div v-if="!loading && !error && applicationTypes.length === 0" class="text-center py-12">
        <DocumentIcon class="mx-auto h-12 w-12 text-gray-400" />
        <h3 class="mt-2 text-sm font-medium text-gray-900">No Application Types</h3>
        <p class="mt-1 text-sm text-gray-500">
          No application types are currently available.
        </p>
      </div>
    </div>
  </AuthenticatedLayout>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useApplicationTypeStore } from '@/stores/applications'
import AuthenticatedLayout from '@/layouts/AuthenticatedLayout.vue'
import LoadingSpinner from '@/components/others/LoadingSpinner.vue'
import {
  DocumentIcon,
  ChevronRightIcon,
  ExclamationTriangleIcon,
  DocumentTextIcon,
  ClipboardDocumentListIcon,
  DocumentCheckIcon,
  PresentationChartBarIcon,
  PrinterIcon
} from '@heroicons/vue/24/outline'

const router = useRouter()
const applicationTypeStore = useApplicationTypeStore()

// Computed properties from store
const loading = computed(() => applicationTypeStore.isLoading)
const error = computed(() => applicationTypeStore.error)
const applicationTypes = computed(() => applicationTypeStore.allApplicationTypes)

const fetchApplicationTypes = async () => {
  const result = await applicationTypeStore.fetchAllApplicationTypes(null, null, null)
  if (!result.success) {
    console.error('Failed to fetch application types:', result.error)
  }
}

const getApplicationTypeIcon = (applicationType) => {
  // Determine icon based on name or provider
  const name = applicationType.name?.toLowerCase() || ''
  const provider = applicationType.provider?.toLowerCase() || ''

  if (name.includes('insurance') || name.includes('crop insurance')) {
    return DocumentCheckIcon
  } else if (name.includes('claim') || name.includes('indemnity')) {
    return ClipboardDocumentListIcon
  } else if (name.includes('loan')) {
    return DocumentTextIcon
  } else if (name.includes('report') || name.includes('yield')) {
    return PresentationChartBarIcon
  } else if (provider === 'agriculture') {
    return DocumentCheckIcon
  } else if (provider === 'pcic') {
    return ClipboardDocumentListIcon
  }

  return DocumentIcon
}

const navigateToApplicationType = (id) => {
  router.push(`/agriculturist/submit-crop-data/application-type/${id}`)
}

onMounted(() => {
  fetchApplicationTypes()
})
</script>
