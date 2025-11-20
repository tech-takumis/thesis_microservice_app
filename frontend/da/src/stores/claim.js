import { defineStore } from "pinia";
import { ref, computed } from "vue";
import axios from "@/lib/axios";

export const useClaimStore = defineStore("claim", () => {
    // State
    const claims = ref([])
    const currentClaim = ref(null)
    const loading = ref(false)
    const error = ref(null)

    // Computed
    const allClaims = computed(() => claims.value)
    const isLoading = computed(() => loading.value)
    const hasError = computed(() => error.value != null)
    const errorMessage = computed(() => error.value)

    // Helper function to create FormData for multipart requests
    const createFormData = (claimData, supportingFiles = null) => {
        const formData = new FormData()
        formData.append('claim', JSON.stringify(claimData))
        
        if (supportingFiles && supportingFiles.length > 0) {
            supportingFiles.forEach((file) => {
                formData.append('supportingFiles', file)
            })
        }
        
        return formData
    }


    const getClaimById = async (claimId) => {
        try {
            loading.value = true
            error.value = null

            const response = await axios.get(`/api/v1/claims/${claimId}`)
            currentClaim.value = response.data

            return {
                success: true,
                message: "Claim retrieved successfully",
                data: response.data
            }
        } catch (err) {
            const errorMessage = err.response?.data?.message || err.message || "Failed to retrieve claim"
            error.value = errorMessage
            return {
                success: false,
                message: errorMessage,
                data: null
            }
        } finally {
            loading.value = false
        }
    }

    const getAllClaims = async () => {
        try {
            loading.value = true
            error.value = null

            const response = await axios.get('/api/v1/claims')
            const claimsList = Array.isArray(response.data) ? response.data : []
            claims.value = claimsList

            return {
                success: true,
                message: "Claims retrieved successfully",
                data: claimsList
            }
        } catch (err) {
            const errorMessage = err.response?.data?.message || err.message || "Failed to retrieve claims"
            error.value = errorMessage
            claims.value = []
            return {
                success: false,
                message: errorMessage,
                data: []
            }
        } finally {
            loading.value = false
        }
    }

    const getClaimsByInsuranceId = async (insuranceId) => {
        try {
            loading.value = true
            error.value = null

            const response = await axios.get(`/api/v1/claims/insurance/${insuranceId}`)

            return {
                success: true,
                message: "Claims retrieved successfully",
                data: response.data
            }
        } catch (err) {
            const errorMessage = err.response?.data?.message || err.message || "Failed to retrieve claims for insurance"
            error.value = errorMessage
            return {
                success: false,
                message: errorMessage,
                data: null
            }
        } finally {
            loading.value = false
        }
    }

    const deleteClaim = async (claimId) => {
        try {
            loading.value = true
            error.value = null

            await axios.delete(`/api/v1/claims/${claimId}`)
            claims.value = claims.value.filter(c => c.id !== claimId)

            return {
                success: true,
                message: "Claim deleted successfully"
            }
        } catch (err) {
            const errorMessage = err.response?.data?.message || err.message || "Failed to delete claim"
            error.value = errorMessage
            return {
                success: false,
                message: errorMessage
            }
        } finally {
            loading.value = false
        }
    }

    return {
        // State
        claims,
        currentClaim,
        loading,
        error,

        // Computed
        allClaims,
        isLoading,
        hasError,
        errorMessage,

        // Actions
        getClaimById,
        getAllClaims,
        getClaimsByInsuranceId,
        deleteClaim,
    }
});