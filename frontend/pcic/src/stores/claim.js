import {defineStore} from 'pinia'
import {ref,computed} from 'vue'
import axios from '@/lib/axios'


export const useClaimStore = defineStore("claimStore",() => {
    const claims = ref([])
    const claim = ref(null)
    const loading = ref(false);
    const error = ref(null)
    const basePath = ref("/api/v1/claims")

    const allClaims = computed(() => claims.value);
    const currentClaim = computed(() => claim.value)
    const isLoading = computed(() => loading.value)
    const hasError = computed(() => error.value !== null)

    // Create claim manually with multipart form data
    const createClaimManually = async (claimData, supportingFiles = []) => {
        try {
            loading.value = true
            error.value = null

            // Create FormData for multipart request
            const formData = new FormData()

            // Add claim data as JSON blob (isFinalized should be part of claimData)
            formData.append('claim', new Blob([JSON.stringify(claimData)], {
                type: 'application/json'
            }))

            // Add supporting files if provided
            if (supportingFiles && supportingFiles.length > 0) {
                supportingFiles.forEach((file) => {
                    formData.append('supportingFiles', file)
                })
            }

            const response = await axios.post(basePath.value, formData, {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            })

            if (response.status === 201) {
                claims.value.push(response.data)
                claim.value = response.data
                return {
                    success: true,
                    message: 'Claim created successfully',
                    data: response.data,
                }
            }

            return {
                success: false,
                message: response.data?.message || 'Failed to create claim',
                data: null,
            }
        } catch (error) {
            console.error('Failed to create manual claim:', error.response?.data || error.message)
            error.value = error.response?.data?.message || error.message
            return { 
                success: false, 
                message: error.value || 'Failed to create claim, error occurred', 
                data: null 
            }
        } finally {
            loading.value = false
        }
    }

    // Create claim from AI with multipart form data
    const createClaimFromAI = async (claimAIData, supportingFiles = []) => {
        try {
            loading.value = true
            error.value = null

            // Create FormData for multipart request
            const formData = new FormData()

            // Add claim AI data as JSON blob (isFinalized should be part of claimAIData)
            formData.append('claim', new Blob([JSON.stringify(claimAIData)], {
                type: 'application/json'
            }))

            // Add supporting files if provided
            if (supportingFiles && supportingFiles.length > 0) {
                supportingFiles.forEach((file) => {
                    formData.append('supportingFiles', file)
                })
            }

            const response = await axios.post(`${basePath.value}/ai`, formData, {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            })

            if (response.status === 201) {
                claims.value.push(response.data)
                claim.value = response.data
                return {
                    success: true,
                    message: 'AI claim created successfully',
                    data: response.data,
                }
            }

            return {
                success: false,
                message: response.data?.message || 'Failed to create AI claim',
                data: null,
            }
        } catch (error) {
            console.error('Failed to create AI claim:', error.response?.data || error.message)
            error.value = error.response?.data?.message || error.message
            return { 
                success: false, 
                message: error.value || 'Failed to create AI claim, error occurred', 
                data: null 
            }
        } finally {
            loading.value = false
        }
    }

    // Get claim by ID
    const getClaimById = async (claimId) => {
        try {
            loading.value = true
            error.value = null

            const response = await axios.get(`${basePath.value}/${claimId}`)
            
            if (response.status === 200) {
                claim.value = response.data
                return {
                    success: true,
                    message: 'Claim fetched successfully',
                    data: response.data,
                }
            }

            return {
                success: false,
                message: response.data?.message || 'Failed to fetch claim',
                data: null,
            }
        } catch (error) {
            console.error('Failed to fetch claim by ID:', error.response?.data || error.message)
            error.value = error.response?.data?.message || error.message
            return { 
                success: false, 
                message: error.value || 'Failed to fetch claim, error occurred', 
                data: null 
            }
        } finally {
            loading.value = false
        }
    }

    // Get all claims
    const getAllClaims = async () => {
        try{
            loading.value = true
            error.value = null

            const response = await axios.get(basePath.value);
            if(response.status === 200){
                claims.value = response.data
                return {success: true, message: "Claims fetched successfully", data: response.data}
            }
            return {success: false, message: response.data?.message || "Failed to fetch claims", data: null};

        }catch (error){
            console.error("Failed to fetch all claims:", error.response?.data || error.message);
            error.value = error.response?.data?.message || error.message;
            return {success: false, message: error.value || "Failed to fetch claims, error occurred", data: null}
        }finally {
            loading.value = false
        }
    }

    // Get claims by insurance ID
    const getClaimsByInsuranceId = async (insuranceId) => {
        try {
            loading.value = true
            error.value = null

            const response = await axios.get(`${basePath.value}/insurance/${insuranceId}`)
            
            if (response.status === 200) {
                // Since backend now returns a single claim object, wrap it in an array for consistency
                claims.value = response.data ? [response.data] : []
                return {
                    success: true,
                    message: 'Claims fetched successfully',
                    data: response.data,
                }
            }

            return {
                success: false,
                message: response.data?.message || 'Failed to fetch claims',
                data: null,
            }
        } catch (error) {
            console.error('Failed to fetch claims by insurance ID:', error.response?.data || error.message)
            error.value = error.response?.data?.message || error.message
            return { 
                success: false, 
                message: error.value || 'Failed to fetch claims, error occurred', 
                data: null 
            }
        } finally {
            loading.value = false
        }
    }



    // Update claim with multipart form data
    const updateClaim = async (claimId, claimData, supportingFiles = []) => {
        try{
            loading.value = true
            error.value = null

            // Create FormData for multipart request
            const formData = new FormData()

            // Add claim data as JSON blob (isFinalized should be part of claimData)
            formData.append('claim', new Blob([JSON.stringify(claimData)], {
                type: 'application/json'
            }))

            // Add supporting files if provided
            if (supportingFiles && supportingFiles.length > 0) {
                supportingFiles.forEach((file) => {
                    formData.append('supportingFiles', file)
                })
            }

            const response = await axios.put(`${basePath.value}/${claimId}`, formData, {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            })
            
            if(response.status === 200){
                const idx = claims.value.findIndex(claim => claim.id === claimId)
                if(idx !== -1){
                    claims.value[idx] = response.data
                }
                claim.value = response.data
                return {success: true, message: "Claim updated successfully", data: response.data}
            }
            return {success: false, message: response.data?.message || "Failed to update claim", data: null};
        }catch (error){
            console.error("Failed to update claim:", error.response?.data || error.message);
            error.value = error.response?.data?.message || error.message;
            return {success: false, message: error.value || "Failed to update claim, error occurred", data: null}
        }finally {
            loading.value = false
        }
    }

    // Delete claim
    const deleteClaim = async (claimId) => {
        try{
            loading.value = true
            error.value = null

            const response = await axios.delete(`${basePath.value}/${claimId}`);
            if(response.status === 204){
                const idx = claims.value.findIndex(claim => claim.id === claimId)
                if(idx !== -1){
                    claims.value.splice(idx, 1)
                }
                
                // Clear current claim if it matches deleted ID
                if (claim.value?.id === claimId) {
                    claim.value = null;
                }
                
                return {success: true, message: "Claim deleted successfully", data: null}
            }

            return {success: false, message: response.data?.message || "Failed to delete claim", data: null};
        }catch (error){
            console.error("Failed to delete claim:", error.response?.data || error.message);
            error.value = error.response?.data?.message || error.message;
            return {success: false, message: error.value || "Failed to delete claim, error occurred", data: null}
        }finally {
            loading.value = false
        }
    }

    return {
        // State
        allClaims,
        currentClaim,
        isLoading,
        hasError,
        
        // Actions
        createClaimManually,
        createClaimFromAI,
        getClaimById,
        getAllClaims,
        getClaimsByInsuranceId,
        updateClaim,
        deleteClaim,
        
    }
})
