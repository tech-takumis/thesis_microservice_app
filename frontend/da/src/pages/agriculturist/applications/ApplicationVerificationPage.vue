<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AuthenticatedLayout from '@/layouts/AuthenticatedLayout.vue'
import BaseCard from '@/components/cards/BaseCard.vue'
import LoadingSpinner from '@/components/others/LoadingSpinner.vue'
import { MUNICIPAL_AGRICULTURIST_NAVIGATION } from '@/lib/navigation'
import { useApplicationStore, useApplicationTypeStore } from '@/stores/applications'
import { useVerificationStore } from '@/stores/verification'
import { PencilIcon, HomeIcon, ChevronRightIcon, TrashIcon, PlusIcon, DocumentIcon } from '@heroicons/vue/24/outline'

const navigation = MUNICIPAL_AGRICULTURIST_NAVIGATION
const route = useRoute()
const router = useRouter()

// Initialize stores with error handling
let fetchApplicationById, fetchApplicationTypeById, createVerification
try {
  const applicationStore = useApplicationStore()
  const applicationTypeStore = useApplicationTypeStore()
  const verificationStore = useVerificationStore()
  fetchApplicationById = applicationStore.fetchApplicationById
  fetchApplicationTypeById = applicationTypeStore.fetchById
  createVerification = verificationStore.createVerification
} catch (err) {
  console.error('Error initializing stores:', err)
}

const applicationData = ref(null)
const applicationTypeData = ref(null)
const loading = ref(true)
const error = ref(null)
const editingField = ref(null)
const editValue = ref('')
const editingFileUploads = ref(false)
const fileUploadInput = ref(null)

// Verification submission state
const submittingVerification = ref(false)
const verificationRemarks = ref('')
const showVerificationForm = ref(false)

// Fetch application and application type data
const fetchData = async () => {
  try {
    loading.value = true
    error.value = null

    const applicationId = route.params.applicationId
    const applicationTypeId = route.params.applicationTypeId

    // Validate required route parameters
    if (!applicationId || !applicationTypeId) {
      throw new Error('Missing required parameters: applicationId or applicationTypeId')
    }

    // Validate store methods are available
    if (!fetchApplicationById || !fetchApplicationTypeById) {
      throw new Error('Store methods not available')
    }

    // Fetch both application and application type data
    const [applicationResult, applicationTypeResult] = await Promise.all([
      fetchApplicationById(applicationId),
      fetchApplicationTypeById(applicationTypeId, false)
    ])

    if (applicationResult.success && applicationTypeResult.success) {
      applicationData.value = applicationResult.data
      applicationTypeData.value = applicationTypeResult.data
    } else {
      throw new Error(applicationResult.error || applicationTypeResult.error)
    }

  } catch (err) {
    error.value = 'Failed to load application data'
    console.error('Error fetching data:', err)
  } finally {
    loading.value = false
  }
}

// Handle field editing
const startEdit = (fieldKey, currentValue) => {
  editingField.value = fieldKey
  editValue.value = currentValue
}

const cancelEdit = () => {
  editingField.value = null
  editValue.value = ''
}

const saveEdit = () => {
  if (applicationData.value && editingField.value) {
    // Ensure we're updating the original dynamicFields object
    if (!applicationData.value.dynamicFields) {
      applicationData.value.dynamicFields = {}
    }
    applicationData.value.dynamicFields[editingField.value] = editValue.value
    editingField.value = null
    editValue.value = ''
    // TODO: Here you would typically call an API to save the changes
  }
}

// Format field key for display
const formatFieldKey = (key) => {
  return key.replace(/_/g, ' ').replace(/\b\w/g, l => l.toUpperCase())
}

// Check if field value is empty
const isEmptyValue = (value) => {
  if (value === null || value === undefined) return true
  if (typeof value === 'string' && value.trim() === '') return true
  if (typeof value === 'number' && value === 0) return true
  if (typeof value === 'object' && value !== null) {
    // Check if object has all empty values
    return Object.values(value).every(v =>
      v === '' || v === null || v === undefined ||
      (typeof v === 'string' && v.trim() === '') ||
      (typeof v === 'number' && v === 0)
    )
  }
  return false
}

// Get filtered dynamic fields (excluding empty values)
const getFilteredDynamicFields = () => {
  if (!applicationData.value?.dynamicFields) return {}

  const filtered = {}
  for (const [key, value] of Object.entries(applicationData.value.dynamicFields)) {
    if (!isEmptyValue(value)) {
      filtered[key] = value
    }
  }
  return filtered
}

// Check if route parameters are valid
const hasValidRouteParams = () => {
  return route.params.applicationId && route.params.applicationTypeId
}

// Check if file uploads should be editable
const canEditFileUploads = () => {
  return applicationTypeData.value && applicationTypeData.value.requiresAIAnalysis === false
}

// File upload handling functions
const startEditFileUploads = () => {
  editingFileUploads.value = true
}

const cancelEditFileUploads = () => {
  editingFileUploads.value = false
}

const saveFileUploads = () => {
  editingFileUploads.value = false
  // TODO: Here you would typically call an API to save the file uploads
}

const removeFileUpload = (index) => {
  if (applicationData.value && applicationData.value.fileUploads) {
    applicationData.value.fileUploads.splice(index, 1)
  }
}

const addFileUpload = () => {
  if (fileUploadInput.value) {
    fileUploadInput.value.click()
  }
}

const handleFileUpload = (event) => {
  const files = event.target.files
  if (files && files.length > 0) {
    // For demo purposes, we'll create mock URLs
    // In a real application, you would upload the files to your server
    for (let i = 0; i < files.length; i++) {
      const file = files[i]
      const mockUrl = `http://localhost:9000/documents/${Date.now()}-${file.name}`
      if (!applicationData.value.fileUploads) {
        applicationData.value.fileUploads = []
      }
      applicationData.value.fileUploads.push(mockUrl)
    }
    // Clear the input
    event.target.value = ''
  }
}

// Get filename from URL
const getFileName = (url) => {
  const parts = url.split('/')
  const filename = parts[parts.length - 1]
  return filename.split('?')[0] // Remove query parameters
}

// Format field value for display
const formatFieldValue = (value) => {
  if (typeof value === 'object' && value !== null) {
    return JSON.stringify(value, null, 2)
  }
  return String(value)
}

// Verification submission functions
const showVerificationSubmissionForm = () => {
  showVerificationForm.value = true
}

const cancelVerificationSubmission = () => {
  showVerificationForm.value = false
  verificationRemarks.value = ''
}

const submitVerification = async () => {
  try {
    submittingVerification.value = true
    error.value = null

    // Validate required data
    if (!verificationRemarks.value.trim()) {
      error.value = 'Please enter verification remarks'
      return
    }

    if (!applicationData.value?.id) {
      error.value = 'Application ID not found'
      return
    }

    // Prepare verification data
    const verificationData = {
      remarks: verificationRemarks.value.trim(),
      fieldValues: applicationData.value.dynamicFields || {}
    }

    // Create FormData for multipart/form-data submission
    const formData = new FormData()

    // Add verification data as JSON string to a Blob to ensure proper content type
    const verificationBlob = new Blob([JSON.stringify(verificationData)], { type: 'application/json' })
    formData.append('verification', verificationBlob)

    // Handle file uploads by fetching them from URLs
    if (canEditFileUploads() && applicationData.value.fileUploads && applicationData.value.fileUploads.length > 0) {
      try {
        // Fetch files from URLs and add them to FormData
        const filePromises = applicationData.value.fileUploads.map(async (fileUrl) => {
          try {
            const response = await fetch(fileUrl)
            if (!response.ok) {
              console.warn(`Failed to fetch file from URL: ${fileUrl}`)
              return null
            }

            const blob = await response.blob()
            const fileName = getFileName(fileUrl)

            // Create File object from blob
            const file = new File([blob], fileName, { type: blob.type || 'application/octet-stream' })

            return { file, fileName, fileUrl }
          } catch (error) {
            console.error(`Error fetching file from ${fileUrl}:`, error)
            return null
          }
        })

        const files = await Promise.all(filePromises)

        // Add successfully fetched files to FormData
        files.forEach(fileData => {
          if (fileData) {
            const { file, fileName } = fileData

            // Categorize files based on filename patterns
            if (fileName.toLowerCase().includes('inspection') || fileName.toLowerCase().includes('report')) {
              formData.append('inspectionReport', file)
            } else if (fileName.toLowerCase().includes('photo') || fileName.toLowerCase().includes('field')) {
              formData.append('fieldPhotos', file)
            } else if (fileName.toLowerCase().includes('signed') || fileName.toLowerCase().includes('document')) {
              formData.append('signedDocument', file)
            } else {
              // Default to fieldPhotos
              formData.append('fieldPhotos', file)
            }
          }
        })

      } catch (error) {
        console.error('Error processing files:', error)
        // Continue with verification even if file processing fails
        error.value = null // Clear any error state since we can continue without files
      }
    }

    // Debug FormData contents
    console.log('FormData entries:')
    for (const [key, value] of formData.entries()) {
      if (value instanceof File) {
        console.log(`${key}:`, {
          name: value.name,
          size: value.size,
          type: value.type
        })
      } else if (value instanceof Blob) {
        console.log(`${key}:`, {
          size: value.size,
          type: value.type,
          content: value
        })
      } else {
        console.log(`${key}:`, value)
      }
    }

    console.log('Submitting verification with FormData:', formData)

    // Submit verification using the store (submissionId is the application ID)
    const result = await createVerification(applicationData.value.id, formData)

    if (result) {
      // Success - redirect back to application detail or show success message
      router.push({
        name: 'agriculturist-submit-crop-data-detail',
        params: {
          id: route.params.applicationId,
          applicationTypeId: route.params.applicationTypeId
        }
      })
    }

  } catch (err) {
    console.error('Error submitting verification:', err)
    error.value = 'Failed to submit verification. Please try again.'
  } finally {
    submittingVerification.value = false
  }
}

// Note: Navigation is now handled by router-link in breadcrumb

onMounted(async () => {
  try {
    await fetchData()
  } catch (err) {
    console.error('Error during component mounting:', err)
    error.value = 'Failed to load application data'
  }
})
</script>

<template>
  <AuthenticatedLayout
    :navigation="navigation"
    role-title="Municipal Agriculturist"
    page-title="Application Verification"
  >
    <div class="h-full flex flex-col min-h-0 overflow-hidden">
      <!-- Fixed Header Section -->
      <div class="flex-shrink-0 mb-4">
        <!-- Breadcrumb -->
        <nav class="flex mb-4" aria-label="Breadcrumb">
          <ol class="flex items-center space-x-4">
            <li>
              <div>
                <router-link
                  :to="{ name: 'agriculturist-submit-crop-data' }"
                  class="text-green-600 hover:text-gray-500"
                >
                  <HomeIcon class="flex-shrink-0 h-5 w-5" />
                  <span class="sr-only">Application Types</span>
                </router-link>
              </div>
            </li>
            <li>
              <div class="flex items-center">
                <ChevronRightIcon class="flex-shrink-0 h-5 w-5 text-gray-400" />
                <router-link
                  v-if="hasValidRouteParams()"
                  :to="{
                    name: 'agriculturist-submit-crop-data-detail',
                    params: {
                      id: route.params.applicationId,
                      applicationTypeId: route.params.applicationTypeId
                    }
                  }"
                  class="ml-4 text-sm font-medium text-gray-500 hover:text-gray-700"
                >
                  Application Detail
                </router-link>
                <span v-else class="ml-4 text-sm font-medium text-gray-400">
                  Application Detail
                </span>
              </div>
            </li>
            <li>
              <div class="flex items-center">
                <ChevronRightIcon class="flex-shrink-0 h-5 w-5 text-gray-400" />
                <span class="ml-4 text-sm font-medium text-black">
                  Verification
                </span>
              </div>
            </li>
          </ol>
        </nav>

        <!-- Header -->
        <div class="flex items-center justify-between ml-5">
          <div>
            <h1 v-if="applicationTypeData" class="text-3xl font-bold text-green-600">
              {{ applicationTypeData.name }}
            </h1>
            <p class="mt-1 text-sm text-gray-600">
              Application Verification
            </p>
          </div>
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="loading" class="flex justify-center items-center h-64">
        <LoadingSpinner />
      </div>

      <!-- Error State -->
      <div v-else-if="error" class="text-center py-12">
        <div class="text-red-600 mb-4">{{ error }}</div>
        <button
          class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700"
          @click="fetchData"
        >
          Try Again
        </button>
      </div>

     <!-- CONTENT -->
<div v-else-if="applicationData" class="flex-1 overflow-hidden">
  <div class="h-full overflow-y-auto space-y-5 px-1 py-1">

    <!-- Dynamic Fields -->
    <BaseCard class="shadow-sm rounded-xl border border-gray-200">
      <template #header>
        <div class="flex items-center gap-2 bg-gray-100">
          <DocumentIcon class="h-5 w-5 text-green-600" />
          <h3 class="text-lg font-semibold text-gray-800">
            Dynamic Fields
          </h3>
        </div>
      </template>

      <div class="space-y-5">
        <div
          v-for="(value, key) in getFilteredDynamicFields()"
          :key="key"
          class="pb-4 border-b border-gray-300 last:border-none last:pb-0"
        >
          <div class="flex items-start justify-between">
            <!-- Field label & value -->
            <div class="flex-1 min-w-0">
              <label class="block text-sm font-medium text-gray-700 mb-1">
                {{ formatFieldKey(key) }}
              </label>

              <!-- Edit Mode -->
              <div v-if="editingField === key" class="space-y-3">
                <!-- JSON/Object -->
                <textarea
                  v-if="typeof value === 'object'"
                  v-model="editValue"
                  class="w-full px-3 py-2 rounded-lg border border-gray-300 focus:ring-green-500 focus:border-green-500"
                  rows="4"
                ></textarea>

                <!-- Normal text -->
                <input
                  v-else
                  v-model="editValue"
                  class="w-full px-3 py-2 rounded-lg border border-gray-300 focus:ring-green-500 focus:border-green-500"
                />

                <!-- Action Buttons -->
                <div class="flex gap-2">
                  <button
                    class="px-3 py-1.5 bg-green-600 text-white rounded-md hover:bg-green-700"
                    @click="saveEdit"
                  >
                    Save
                  </button>
                  <button
                    class="px-3 py-1.5 bg-red-600 text-white rounded-md hover:bg-red-700"
                    @click="cancelEdit"
                  >
                    Cancel
                  </button>
                </div>
              </div>

              <!-- Display Mode -->
              <div v-else>
                <pre
                  v-if="typeof value === 'object'"
                  class="text-sm text-gray-800 bg-gray-50 p-3 border rounded-lg whitespace-pre-wrap font-mono"
                >{{ formatFieldValue(value) }}</pre>

                <p v-else class="text-sm text-gray-900">
                  {{ formatFieldValue(value) }}
                </p>
              </div>
            </div>

            <!-- Edit Button -->
            <button
              v-if="editingField !== key"
              class="ml-3 p-2 rounded hover:bg-gray-100 text-gray-500 hover:text-gray-700"
              @click="startEdit(key, value)"
            >
              <PencilIcon class="h-4 w-4" />
            </button>
          </div>
        </div>
      </div>
    </BaseCard>


    <!-- File Uploads -->
    <BaseCard v-if="canEditFileUploads()" class="shadow-sm rounded-xl border border-gray-200">
      <template #header>
        <div class="flex items-center gap-2">
          <FolderIcon class="h-5 w-5 text-green-600" />
          <h3 class="text-lg font-semibold text-gray-800">File Uploads</h3>
        </div>
      </template>

      <div class="space-y-4">
        <!-- Header Actions -->
        <div class="flex items-center justify-between">
          <span class="text-sm font-medium text-gray-700">
            Uploaded Documents ({{ applicationData.fileUploads?.length || 0 }})
          </span>

          <template v-if="!editingFileUploads">
            <button
              class="flex items-center text-sm text-green-600 hover:text-green-700"
              @click="startEditFileUploads"
            >
              <PencilIcon class="h-4 w-4 mr-1" /> Edit Files
            </button>
          </template>

          <template v-else>
            <div class="flex gap-2">
              <button
                class="px-3 py-1 bg-green-600 text-white rounded-md hover:bg-green-700"
                @click="saveFileUploads"
              >
                Save
              </button>
              <button
                class="px-3 py-1 bg-gray-600 text-white rounded-md hover:bg-gray-700"
                @click="cancelEditFileUploads"
              >
                Cancel
              </button>
            </div>
          </template>
        </div>

        <!-- File Grid -->
        <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">

          <!-- Empty -->
          <div
            v-if="!applicationData.fileUploads?.length"
            class="col-span-full text-center py-10 text-gray-500"
          >
            <DocumentIcon class="h-12 w-12 mx-auto text-gray-300 mb-2" />
            <p>No documents uploaded yet</p>
          </div>

          <!-- Files -->
          <div
            v-for="(fileUrl, index) in applicationData.fileUploads"
            :key="index"
            class="border border-gray-200 rounded-lg p-4 hover:border-gray-300 transition"
          >
            <div class="flex items-start gap-3">
              <DocumentIcon class="h-8 w-8 text-green-500" />

              <div class="flex-1">
                <p class="text-sm font-medium text-gray-900 truncate">
                  {{ getFileName(fileUrl) }}
                </p>
                <p class="text-xs text-gray-500">Document {{ index + 1 }}</p>
              </div>

              <!-- Remove (Edit mode) -->
              <button
                v-if="editingFileUploads"
                class="p-1 text-red-500 hover:text-red-700"
                @click="removeFileUpload(index)"
              >
                <TrashIcon class="h-4 w-4" />
              </button>
            </div>

            <!-- Link -->
            <a
              :href="fileUrl"
              target="_blank"
              class="text-sm mt-3 inline-block text-green-600 hover:text-green-700"
            >
              View Document
            </a>
          </div>

          <!-- Add new -->
          <div
            v-if="editingFileUploads"
            class="border-2 border-dashed border-gray-300 rounded-lg p-6 flex flex-col items-center justify-center cursor-pointer hover:border-gray-400"
            @click="addFileUpload"
          >
            <PlusIcon class="h-8 w-8 text-gray-400 mb-2" />
            <span class="text-sm text-gray-500">Add New File</span>
          </div>
        </div>
      </div>
    </BaseCard>


    <!-- Submit Verification -->
    <BaseCard class="shadow-sm rounded-xl border border-gray-200 mb-10">
      <template #header>
        <div class="flex items-center gap-2">
          <ArrowUpTrayIcon class="h-5 w-5 text-green-600" />
          <h3 class="text-lg font-semibold text-gray-800">
            Submit Verification
          </h3>
        </div>
      </template>

      <!-- Start -->
      <div v-if="!showVerificationForm" class="py-8 text-center">
        <p class="text-sm text-gray-600 mb-5">
          Review all information before submitting verification.
        </p>

        <button
          class="w-full md:w-auto px-6 py-3 bg-green-600 text-white rounded-lg hover:bg-green-700"
          @click="showVerificationSubmissionForm"
        >
          Submit Verification
        </button>
      </div>

      <!-- Form -->
      <div v-else class="space-y-5">

        <!-- Remarks -->
        <div>
          <label class="text-sm font-medium text-gray-700 mb-1 block">
            Verification Remarks *
          </label>
          <textarea
            v-model="verificationRemarks"
            rows="4"
            class="w-full px-3 py-2 rounded-lg border border-gray-300 focus:ring-green-500 focus:border-green-500"
            placeholder="Enter your remarks here..."
          ></textarea>
        </div>

        <!-- Files Summary -->
        <div
          v-if="applicationData.fileUploads?.length > 0"
          class="bg-blue-50 p-4 rounded-lg"
        >
          <h4 class="text-sm font-medium text-blue-900 mb-2">
            Files to be submitted:
          </h4>
          <ul class="text-sm text-blue-700 space-y-1">
            <li
              v-for="(fileUrl, index) in applicationData.fileUploads"
              :key="index"
              class="flex items-center"
            >
              <DocumentIcon class="h-4 w-4 mr-2" />
              {{ getFileName(fileUrl) }}
            </li>
          </ul>
        </div>

        <!-- Fields Summary -->
        <div class="bg-gray-50 p-4 rounded-lg">
          <h4 class="text-sm font-medium text-gray-900 mb-1">
            Verified Field Values:
          </h4>
          <p class="text-sm text-gray-600">
            {{ Object.keys(getFilteredDynamicFields()).length }} field(s) will be submitted
          </p>
        </div>

        <!-- Actions -->
        <div class="flex justify-end gap-3 pt-4">
          <button
            class="px-5 py-2 bg-white border border-gray-300 rounded-lg hover:bg-gray-50"
            @click="cancelVerificationSubmission"
          >
            Cancel
          </button>

          <button
            class="px-6 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 flex items-center"
            :disabled="!verificationRemarks.trim()"
            @click="submitVerification"
          >
            <LoadingSpinner v-if="submittingVerification" class="h-4 w-4 mr-2" />
            {{ submittingVerification ? 'Submitting...' : 'Submit Verification' }}
          </button>
        </div>
      </div>
    </BaseCard>

    <!-- Hidden Input -->
    <input
      ref="fileUploadInput"
      type="file"
      multiple
      accept="image/*,.pdf,.doc,.docx"
      class="hidden"
      @change="handleFileUpload"
    />
  </div>
</div>

    </div>
  </AuthenticatedLayout>
</template>
