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
                <span class="ml-4 text-sm font-medium text-gray-900">
                  Verification
                </span>
              </div>
            </li>
          </ol>
        </nav>

        <!-- Header -->
        <div class="flex items-center justify-between">
          <div>
            <h1 v-if="applicationTypeData" class="text-3xl font-bold text-gray-700">
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

      <!-- Content -->
      <div v-else-if="applicationData" class="flex-1 overflow-hidden">
        <div class="h-full overflow-y-auto">
          <BaseCard title="Dynamic Fields" class="mb-6">
            <div class="space-y-4">
              <div
                v-for="(value, key) in getFilteredDynamicFields()"
                :key="key"
                class="border-b border-gray-200 pb-4 last:border-b-0 last:pb-0"
              >
                <div class="flex items-start justify-between">
                  <div class="flex-1 min-w-0">
                    <label class="block text-sm font-medium text-gray-700 mb-1">
                      {{ formatFieldKey(key) }}
                    </label>

                    <!-- Edit Mode -->
                    <div v-if="editingField === key" class="space-y-2">
                      <textarea
                        v-if="typeof value === 'object'"
                        v-model="editValue"
                        class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                        rows="4"
                      ></textarea>
                      <input
                        v-else
                        v-model="editValue"
                        type="text"
                        class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                      />
                      <div class="flex space-x-2">
                        <button
                          class="px-3 py-1 bg-green-600 text-white text-sm rounded-md hover:bg-green-700"
                          @click="saveEdit"
                        >
                          Save
                        </button>
                        <button
                          class="px-3 py-1 bg-gray-600 text-white text-sm rounded-md hover:bg-gray-700"
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
                        class="text-sm text-gray-900 whitespace-pre-wrap bg-gray-50 p-2 rounded border font-mono"
                      >{{ formatFieldValue(value) }}</pre>
                      <p v-else class="text-sm text-gray-900">
                        {{ formatFieldValue(value) }}
                      </p>
                    </div>
                  </div>

                  <!-- Edit Button -->
                  <button
                    v-if="editingField !== key"
                    class="ml-3 p-1 text-gray-400 hover:text-gray-600"
                    title="Edit field"
                    @click="startEdit(key, value)"
                  >
                    <PencilIcon class="h-4 w-4" />
                  </button>
                </div>
              </div>
            </div>
          </BaseCard>

          <!-- File Uploads Section -->
          <BaseCard
            v-if="canEditFileUploads()"
            title="File Uploads"
            class="mb-6"
          >
            <div class="space-y-4">
              <!-- File Uploads Header -->
              <div class="flex items-center justify-between">
                <span class="text-sm font-medium text-gray-700">
                  Uploaded Documents ({{ applicationData.fileUploads?.length || 0 }})
                </span>
                <button
                  v-if="canEditFileUploads() && !editingFileUploads"
                  class="text-sm text-blue-600 hover:text-blue-800 flex items-center"
                  title="Edit file uploads"
                  @click="startEditFileUploads"
                >
                  <PencilIcon class="h-4 w-4 mr-1" />
                  Edit Files
                </button>
                <div v-if="editingFileUploads" class="flex space-x-2">
                  <button
                    class="px-3 py-1 bg-green-600 text-white text-sm rounded-md hover:bg-green-700"
                    @click="saveFileUploads"
                  >
                    Save
                  </button>
                  <button
                    class="px-3 py-1 bg-gray-600 text-white text-sm rounded-md hover:bg-gray-700"
                    @click="cancelEditFileUploads"
                  >
                    Cancel
                  </button>
                </div>
              </div>

              <!-- File List -->
              <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                <!-- No files message -->
                <div
                  v-if="!applicationData.fileUploads || applicationData.fileUploads.length === 0"
                  class="col-span-full text-center py-8 text-gray-500"
                >
                  <DocumentIcon class="h-12 w-12 mx-auto text-gray-300 mb-2" />
                  <p class="text-sm">No documents uploaded yet</p>
                  <p v-if="canEditFileUploads()" class="text-xs mt-1">
                    Click "Edit Files" to add documents
                  </p>
                </div>

                <!-- Existing Files -->
                <div
                  v-for="(fileUrl, index) in applicationData.fileUploads"
                  :key="index"
                  class="relative border border-gray-200 rounded-lg p-4 hover:border-gray-300 transition-colors"
                >
                  <!-- File Icon and Info -->
                  <div class="flex items-start space-x-3">
                    <DocumentIcon class="h-8 w-8 text-blue-500 flex-shrink-0 mt-1" />
                    <div class="flex-1 min-w-0">
                      <p class="text-sm font-medium text-gray-900 truncate">
                        {{ getFileName(fileUrl) }}
                      </p>
                      <p class="text-xs text-gray-500 mt-1">
                        Document {{ index + 1 }}
                      </p>
                    </div>

                    <!-- Remove button (only in edit mode) -->
                    <button
                      v-if="editingFileUploads"
                      class="text-red-500 hover:text-red-700 p-1"
                      title="Remove file"
                      @click="removeFileUpload(index)"
                    >
                      <TrashIcon class="h-4 w-4" />
                    </button>
                  </div>

                  <!-- File Preview/Link -->
                  <div class="mt-3">
                    <a
                      :href="fileUrl"
                      target="_blank"
                      class="text-blue-600 hover:text-blue-800 text-sm"
                    >
                      View Document
                    </a>
                  </div>
                </div>

                <!-- Add New File Button (only in edit mode) -->
                <div
                  v-if="editingFileUploads"
                  class="border-2 border-dashed border-gray-300 rounded-lg p-4 flex flex-col items-center justify-center hover:border-gray-400 transition-colors cursor-pointer"
                  @click="addFileUpload"
                >
                  <PlusIcon class="h-8 w-8 text-gray-400 mb-2" />
                  <span class="text-sm text-gray-500">Add New File</span>
                </div>
              </div>
            </div>
          </BaseCard>

          <!-- Verification Submission Section -->
          <BaseCard title="Submit Verification" class="mb-6">
            <div v-if="!showVerificationForm" class="text-center py-6">
              <div class="max-w-md mx-auto">
                <p class="text-sm text-gray-600 mb-4">
                  Review all the information above and submit your verification for this application.
                </p>
                <button
                  class="w-full px-6 py-3 bg-green-600 text-white font-medium rounded-md hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 disabled:opacity-50 disabled:cursor-not-allowed"
                  :disabled="submittingVerification"
                  @click="showVerificationSubmissionForm"
                >
                  Submit Verification
                </button>
              </div>
            </div>

            <!-- Verification Form -->
            <div v-else class="space-y-4">
              <div>
                <label for="verificationRemarks" class="block text-sm font-medium text-gray-700 mb-2">
                  Verification Remarks *
                </label>
                <textarea
                  id="verificationRemarks"
                  v-model="verificationRemarks"
                  rows="4"
                  class="w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-green-500 focus:border-green-500"
                  placeholder="Enter your verification remarks, observations, and any recommendations..."
                  :disabled="submittingVerification"
                ></textarea>
              </div>

              <!-- File Information -->
              <div v-if="canEditFileUploads() && applicationData.fileUploads?.length > 0" class="bg-blue-50 p-4 rounded-md">
                <h4 class="text-sm font-medium text-blue-900 mb-2">Files to be submitted:</h4>
                <ul class="text-sm text-blue-700 space-y-1">
                  <li v-for="(fileUrl, index) in applicationData.fileUploads" :key="index" class="flex items-center">
                    <DocumentIcon class="h-4 w-4 mr-2 flex-shrink-0" />
                    {{ getFileName(fileUrl) }}
                  </li>
                </ul>
              </div>

              <!-- Field Values Summary -->
              <div class="bg-gray-50 p-4 rounded-md">
                <h4 class="text-sm font-medium text-gray-900 mb-2">Verified Field Values:</h4>
                <div class="text-sm text-gray-600">
                  {{ Object.keys(getFilteredDynamicFields()).length }} field(s) will be submitted
                </div>
              </div>

              <!-- Action Buttons -->
              <div class="flex justify-end space-x-3 pt-4">
                <button
                  class="px-4 py-2 text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-gray-500"
                  :disabled="submittingVerification"
                  @click="cancelVerificationSubmission"
                >
                  Cancel
                </button>
                <button
                  class="px-6 py-2 bg-green-600 text-white font-medium rounded-md hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 disabled:opacity-50 disabled:cursor-not-allowed flex items-center"
                  :disabled="submittingVerification || !verificationRemarks.trim()"
                  @click="submitVerification"
                >
                  <LoadingSpinner v-if="submittingVerification" class="h-4 w-4 mr-2" />
                  {{ submittingVerification ? 'Submitting...' : 'Submit Verification' }}
                </button>
              </div>
            </div>
          </BaseCard>

          <!-- Hidden File Input -->
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
