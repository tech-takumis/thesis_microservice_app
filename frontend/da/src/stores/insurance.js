import {defineStore} from 'pinia'
import {ref,computed} from 'vue'
import axios from '@/lib/axios'


const useInsuranceStore = defineStore('insurance', () => {
    const insurance = ref([])
    const loading = ref(false)
    const error = ref(null)


    const allInsurance = computed(() => insurance.value || [])
    const isLoading = computed(() => loading.value)
    const hasError = computed(() => error.value !== null)

    const fetchAllInsurance = async (batch,application,farmer,verification,inspection,policy,claim) => {
        try{
            loading.value = true
            error.value = null
            const response = await axios.get(`/api/v1/insurance?batch=${batch}&application=${application}&farmer=${farmer}&verification=${verification}&inspection=${inspection}&policy=${policy}&claim=${claim}`)
           if(response.status === 200){
               insurance.value = response.data
               return {success: "true", message: "Insurance fetched successfully", data: insurance.value}
           }

           return {success: "false", message: "Failed to fetch insurance", data: null}
        }catch (error){
            console.log("Error fetching insurance:", error)
            error.value = error.data.message
            return {success: error.success, message: error.message, data: null}
        } finally {
            loading.value = false
        }
    }

    const fetchInsuranceById = async (id,batch,application,farmer,verification,inspection,policy,claim) => {
        try{
            loading.value = true
            error.value = null
            const response = await axios.get(`/api/v1/insurance/${id}?batch=${batch}&application=${application}&farmer=${farmer}&verification=${verification}&inspection=${inspection}&policy=${policy}&claim=${claim}`)
            if(response.status === 200){
                insurance.value = response.data
                console.log("Fetched insurance:", insurance.value)
                return {success: "true", message: "Insurance fetched successfully", data: insurance.value}
            }
            return {success: "false", message: "Failed to fetch insurance", data: null}
        }catch (error){
            console.log("Error fetching insurance:", error)
            error.value = error.data.message
            return {success: error.success, message: error.message, data: null}
        } finally {
            loading.value = false
        }
    }

    const fetchInsuranceByApplicationTypeIds = async (applicationTypeId) => {
        try{
            loading.value = true
            error.value = null
            const response = await axios.get(`/api/v1/insurance/application-type/${applicationTypeId}`)
            if(response.status === 200){
                insurance.value = response.data
                console.log("Fetched insurance:", insurance.value)
                return {success: "true", message: "Insurance fetched successfully", data: insurance.value}
            }
            return {success: "false", message: "Failed to fetch insurance", data: null}
        }catch (error){
            console.log("Error fetching insurance:", error)
            error.value = error.data.message
            return {success: error.success, message: error.message, data: null}
        }finally {
            loading.value = false
        }
    }

    const updateInsurance = async (id,data) => {
        try{
            loading.value = true
            error.value = null
            const response = await axios.put(`/api/v1/insurance/${id}`,data)
            if(response.status === 200){
                return {success: "true", message: "Insurance updated successfully", data: response.data}
            }
            return {success: "false", message: "Failed to update insurance", data: null}
        }catch (error){
            console.log("Error updating insurance:", error)
        }
    }

    const deleteInsurance = async (id) => {
        try{
            loading.value = true
            error.value = null
            const response = await axios.delete(`/api/v1/insurance/${id}`)
            if(response.status === 200){
                return {success: "true", message: "Insurance deleted successfully", data: response.data}
            }
            return {success: "false", message: "Failed to delete insurance", data: null}
        }catch (error){
            console.log("Error deleting insurance:", error)
            error.value = error.data.message
            return {success: error.success, message: error.message, data: null}
        }finally {
            loading.value = false
        }
    }

    return {
        allInsurance,
        isLoading,
        hasError,
        fetchAllInsurance,
        fetchInsuranceById,
        fetchInsuranceByApplicationTypeIds,
        updateInsurance,
        deleteInsurance
    }
})

const useBatchStore = defineStore('batch', () => {
    const batches = ref([])
    const loading = ref(false)
    const error = ref(null)

    const allBatch = computed(() => batches.value)
    const isLoading = computed(() => loading.value)
    const hasError = computed(() => error.value !== null)

    const createBatch = async (data) => {
        try{
            loading.value = true
            error.value = null
            const response = await axios.post('/api/v1/batch',data)
            if(response.status === 201){
                return {success: "true", message: "Batch created successfully", data: response.data}
            }
            return {success: "false", message: "Failed to create batch", data: null}
        }catch (error){
            console.log("Error creating batch:", error)
            error.value = error.data.message
            return {success: error.success, message: error.message, data: null}
        }finally {
            loading.value = false
        }
    }

    const fetchBatchByApplicationId = async (applicationId,insurance) => {
        try{
            loading.value = true
            error.value = null
            const response = await axios.get(`/api/v1/batch/application-type/${applicationId}?insurance=${insurance}`)
            if(response.status === 200){
                batches.value = response.data
                console.log("Fetched batch:", batches.value)
                return {success: "true", message: "Batch fetched successfully", data: batches.value}
            }
            return {success: "false", message: "Failed to fetch batch", data: null}
        }catch (error){
            console.log("Error fetching batch:", error)
            error.value = error.data.message
            return {success: error.success, message: error.message, data: null}
        }
    }

    const fetchBatchByApplicationTypeId = async (applicationTypeId) => {
        try{
            loading.value = true
            error.value = null
            const response = await axios.get(`/api/v1/batch/application-type/${applicationTypeId}`)
            if(response.status === 200){
                batches.value = response.data
                console.log("Fetched batch:", batches.value)
                return {success: "true", message: "Batch fetched successfully", data: batches.value}
            }
            return {success: "false", message: "Failed to fetch batch", data: null}
        }catch (error){
            console.log("Error fetching batch:", error)
            error.value = error.data.message
            return {success: error.success, message: error.message, data: null}
        }finally {
            loading.value = false
        }
    }



    const fetchAllBatch = async (insurance) => {
        try {
            loading.value = true
            error.value = null
            const response = await axios.get(
                `/api/v1/batch?insurance=${insurance}`,
            )
            if (response.status === 200) {
                batches.value = response.data
                return {
                    success: 'true',
                    message: 'Batches fetched successfully',
                    data: batches.value,
                }
            }

            return {
                success: 'false',
                message: 'Failed to fetch batches',
                data: null,
            }
        } catch (error) {
            console.log('Error fetching batches:', error)
            error.value = error.data.message
            return {
                success: error.success,
                message: error.message,
                data: null,
            }
        } finally {
            loading.value = false
        }
    }

    const fetchBatchById = async (id,insurance) => {
        try{
            loading.value = true
            error.value = null
            const response = await axios.get(`/api/v1/batch/${id}?insurance=${insurance}`)
            if(response.status === 200){
                batches.value = response.data
                return {success: "true", message: "Batch fetched successfully", data: batches.value}
            }
            return {success: "false", message: "Failed to fetch batch", data: null}
        }catch (error){
            console.log("Error fetching batch:", error)
            error.value = error.data.message
            return {success: error.success, message: error.message, data: null}
        }finally {
            loading.value = false
        }
    }

    const updateBatch = async (id,data) => {
        try{
            loading.value = true
            error.value = null
            const response = await axios.put(`/api/v1/batch/${id}`,data)
            if(response.status === 200){
                return {success: "true", message: "Batch updated successfully", data: response.data}
            }
            return {success: "false", message: "Failed to update batch", data: null}
        }catch (error){
            console.log("Error updating batch:", error)
            error.value = error.data.message
            return {success: error.success, message: error.message, data: null}
        }finally {
            loading.value = false
        }
    }

    const deleteBatch = async (id) => {
        try{
            loading.value = true
            error.value = null
            const response = await axios.delete(`/api/v1/batch/${id}`)
            if(response.status === 200){
                return {success: "true", message: "Batch deleted successfully", data: response.data}
            }
            return {success: "false", message: "Failed to delete batch", data: null}
        }catch (error){
            console.log("Error deleting batch:", error)
            error.value = error.data.message
            return {success: error.success, message: error.message, data: null}
        }finally {
            loading.value = false
        }
    }

    return {
        allBatch,
        isLoading,
        hasError,
        createBatch,
        fetchAllBatch,
        fetchBatchByApplicationTypeId,
        fetchBatchById,
        fetchBatchByApplicationId,
        updateBatch,
        deleteBatch
    }
})


export {useInsuranceStore,useBatchStore}