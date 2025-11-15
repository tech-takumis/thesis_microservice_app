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
                <component :is="getApplicationTypeIcon(applicationType.type)" class="h-6 w-6 text-indigo-600" />
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
            <div class="flex space-x-4">
              <div class="text-center">
                <p class="text-2xl font-bold text-gray-900">{{ applicationType.totalSubmissions || 0 }}</p>
                <p class="text-xs text-gray-500">Total Submissions</p>
              </div>
              <div class="text-center">
                <p class="text-2xl font-bold text-green-600">{{ applicationType.approvedSubmissions || 0 }}</p>
                <p class="text-xs text-gray-500">Approved</p>
              </div>
              <div class="text-center">
                <p class="text-2xl font-bold text-yellow-600">{{ applicationType.pendingSubmissions || 0 }}</p>
                <p class="text-xs text-gray-500">Pending</p>
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
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AuthenticatedLayout from '@/layouts/AuthenticatedLayout.vue'
import LoadingSpinner from '@/components/others/LoadingSpinner.vue'
import {
  DocumentIcon,
  ChevronRightIcon,
  ExclamationTriangleIcon,
  DocumentTextIcon,
  ClipboardDocumentListIcon,
  DocumentCheckIcon,
  PresentationChartBarIcon
} from '@heroicons/vue/24/outline'
import axios from '@/lib/axios'

const router = useRouter()
const loading = ref(false)
const error = ref(null)
const applicationTypes = ref([])

// Mock data for demonstration - replace with actual API call
const mockApplicationTypes = [
  {
    id: 1,
    name: 'Crop Insurance Application',
    description: 'Submit applications for crop insurance coverage',
    type: 'insurance',
    totalSubmissions: 45,
    approvedSubmissions: 32,
    pendingSubmissions: 13
  },
  {
    id: 2,
    name: 'Agricultural Loan Application',
    description: 'Apply for agricultural loans and financing',
    type: 'loan',
    totalSubmissions: 28,
    approvedSubmissions: 20,
    pendingSubmissions: 8
  },
  {
    id: 3,
    name: 'Crop Yield Report',
    description: 'Submit seasonal crop yield reports',
    type: 'report',
    totalSubmissions: 67,
    approvedSubmissions: 60,
    pendingSubmissions: 7
  },
  {
    id: 4,
    name: 'Equipment Subsidy Application',
    description: 'Apply for agricultural equipment subsidies',
    type: 'subsidy',
    totalSubmissions: 15,
    approvedSubmissions: 10,
    pendingSubmissions: 5
  }
]

const fetchApplicationTypes = async () => {
  loading.value = true
  error.value = null

  try {
    // Replace this with actual API call
    // const response = await axios.get('/api/v1/application-types')
    // applicationTypes.value = response.data

    // For now, use mock data
    await new Promise(resolve => setTimeout(resolve, 1000)) // Simulate API delay
    applicationTypes.value = mockApplicationTypes
  } catch (err) {
    error.value = err.response?.data?.message || 'Failed to load application types'
    console.error('Error fetching application types:', err)
  } finally {
    loading.value = false
  }
}

const getApplicationTypeIcon = (type) => {
  const icons = {
    insurance: DocumentCheckIcon,
    loan: DocumentTextIcon,
    report: PresentationChartBarIcon,
    subsidy: ClipboardDocumentListIcon,
    default: DocumentIcon
  }
  return icons[type] || icons.default
}

const navigateToApplicationType = (id) => {
  router.push(`/agriculturist/submit-crop-data/application-type/${id}`)
}

onMounted(() => {
  fetchApplicationTypes()
})
</script>
