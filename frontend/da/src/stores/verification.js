import axios from '../lib/axios'
import { ref, computed } from 'vue'

export function useVerificationStore() {
  const verifications = ref([])
  const verification = ref(null)
  const loading = ref(false)
  const errors = ref([])
  const basePath = ref("/api/v1/verifications")

  const allVerifications = computed(() => verifications.value || [])
  const currentVerification = computed(() => verification.value)
  const isLoading = computed(() => loading.value)
  const errorsList = computed(() => errors.value || [])

  // Create verification for submission with files
  const createVerification = async (submissionId, verificationData) => {
    loading.value = true
    errors.value = []
    try {
      // Don't set Content-Type for FormData - let axios handle it automatically
      const config = {}

      const response = await axios.post(`${basePath.value}/submission/${submissionId}/with-files`, verificationData, config)
      verification.value = response.data
      return response.data
    } catch (error) {
      errors.value = error.response?.data?.errors || ['Failed to create verification']
      throw error
    } finally {
      loading.value = false
    }
  }

  // Get verification by ID
  const getVerificationById = async (verificationId) => {
    loading.value = true
    errors.value = []
    try {
      const response = await axios.get(`${basePath.value}/${verificationId}`)
      verification.value = response.data
      return response.data
    } catch (error) {
      errors.value = error.response?.data?.errors || ['Failed to fetch verification']
      throw error
    } finally {
      loading.value = false
    }
  }

  const getVerificationBySubmissionId = async (submissionId) => {
      try{
          loading.value = true
          errors.value = []
          const response = await axios.get(`${basePath.value}/submission/${submissionId}`)
          if(response.status === 200){
              errors.value = []
              verification.value = response.data
              return {success: true, data: response.data, message: "Verification fetched successfully"}
          }
          return {success: false, data: null, message: "Failed to fetch verification"}
      }catch (error){
          errors.value = error.response?.data?.errors || ['Failed to fetch verification']
          return {success: false, data: null, message: error.response?.data?.errors || ['Failed to fetch verification']}
      }
      finally {
          loading.value = false
      }
  }

  // Get all verifications with pagination
  const getAllVerifications = async (params = {}) => {
    loading.value = true
    errors.value = []
    try {
      const response = await axios.get(basePath.value, { params })
      verifications.value = response.data.content || response.data
      return response.data
    } catch (error) {
      errors.value = error.response?.data?.errors || ['Failed to fetch verifications']
      throw error
    } finally {
      loading.value = false
    }
  }

  // Update verification
  const updateVerification = async (verificationId, verificationData) => {
    loading.value = true
    errors.value = []
    try {
      const response = await axios.put(`${basePath.value}/${verificationId}`, verificationData)
      verification.value = response.data

      // Update in the list if it exists
      const index = verifications.value.findIndex(v => v.id === verificationId)
      if (index !== -1) {
        verifications.value[index] = response.data
      }

      return response.data
    } catch (error) {
      errors.value = error.response?.data?.errors || ['Failed to update verification']
      throw error
    } finally {
      loading.value = false
    }
  }

  // Delete verification
  const deleteVerification = async (verificationId) => {
    loading.value = true
    errors.value = []
    try {
      await axios.delete(`${basePath.value}/${verificationId}`)

      // Remove from the list if it exists
      verifications.value = verifications.value.filter(v => v.id !== verificationId)

      // Clear current verification if it's the one being deleted
      if (verification.value?.id === verificationId) {
        verification.value = null
      }

      return true
    } catch (error) {
      errors.value = error.response?.data?.errors || ['Failed to delete verification']
      throw error
    } finally {
      loading.value = false
    }
  }

  // Clear errors
  const clearErrors = () => {
    errors.value = []
  }

  // Reset store
  const resetStore = () => {
    verifications.value = []
    verification.value = null
    loading.value = false
    errors.value = []
  }

  return {
    // State
    allVerifications,
    currentVerification,
    isLoading,
    errorsList,

    // Actions
    createVerification,
    getVerificationById,
      getVerificationBySubmissionId,
    getAllVerifications,
    updateVerification,
    deleteVerification,
    clearErrors,
    resetStore
  }
}
