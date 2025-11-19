import {defineStore} from 'pinia'
import {ref,computed} from 'vue'
import axios from '@/lib/axios'


export const useInspectionStore = defineStore('inspection', () => {
    const inspections = ref([])
    const loading = ref(false)
    const schedule = ref(null)
    const error = ref(null)
    const baseUrl = ref("/api/v1/inspection")

    const isLoading = computed(() => loading.value)
    const errorMessage = computed(() => error.value)
    const getInspectionSchedule = computed(() => schedule.value)
    const allInspections = computed(() => inspections.value)

    const createInspection = async (insuranceId, fieldValues, photos = []) => {
        try{
            loading.value = true
            error.value = false

            // Create FormData for multipart request
            const formData = new FormData()
            
            // Add inspection request as JSON part
            const inspectionRequest = {
                fieldValues: fieldValues
            }
            formData.append('request', new Blob([JSON.stringify(inspectionRequest)], {
                type: 'application/json'
            }))
            
            // Add photos if provided
            if (photos && photos.length > 0) {
                photos.forEach((photo) => {
                    formData.append('photos', photo)
                })
            }

            const response = await axios.post(`${baseUrl.value}/insurance/${insuranceId}`, formData, {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            })

            if(response.status === 201){
                return { success: true, data: response.data, message: "Inspection created successfully" }
            }
            return { success: false, error: response.data.message, message: "Failed to create the inspection" }
        }catch (error) {
            console.error("Error creating inspection:", error.response?.data || error.message)
            error.value = error.response?.data?.message || error.message
            return { success: false, error: error.value, message: "Failed to create the inspection, error occurred" }
        }finally {
            loading.value = false
            error.value = null
        }
    }

    const scheduleInspection = async (id,data) => {
        try{
            loading.value = true
            error.value = false

            const response = await axios.post(`${baseUrl.value}/${id}/schedule`, data)

            if(response.status > 200 && response.status < 300){
                schedule.value = response.data
                return { success: true, data: response.data, message: "Inspection scheduled successfully" }
            }
            return { success: false, error: response.data.message, message: "Failed to schedule the inspection" }
        }catch (error) {
            error.value = error.response?.message
            return { success: false, error: error.value, message: "Failed to schedule the inspection, error occurred" }
        }finally {
            loading.value = false
        }
    }

    const fetchInspections = async () => {
        try{
            loading.value = true
            error.value = false

            const response = await axios.get(baseUrl.value)

            inspections.value = response.data
            return { success: true, message:"Application fetch successfully", data: response.data }
        }catch (error) {
            console.error("Error fetching inspections:", error.response?.data || error.message)
            error.value = error.response?.message
            return { success: false, error: error.value, message: "Failed to fetch the applications" }
        }finally {
            loading.value = false
            error.value = null
        }
    }

    const completeInspection = async (id, fieldValues, photos = []) => {
        try{
            loading.value = true
            error.value = false

            // Create FormData for multipart request
            const formData = new FormData()
            
            // Add inspection request as JSON part
            const inspectionRequest = {
                fieldValues: fieldValues
            }
            formData.append('request', new Blob([JSON.stringify(inspectionRequest)], {
                type: 'application/json'
            }))
            
            // Add photos if provided
            if (photos && photos.length > 0) {
                photos.forEach((photo) => {
                    formData.append('photos', photo)
                })
            }

            const response = await axios.put(`${baseUrl.value}/${id}`, formData, {
                headers: {
                    'Content-Type': 'multipart/form-data'
                }
            })
            
            if(response.status === 200){
                return { success: true, message: "Inspection completed successfully" , data: response.data }
            }

            return { success: false, error: response.data.message, message: "Failed to complete the inspection" }
        }catch (error) {
            console.error("Error completing inspection:", error.response?.data || error.message)
            error.value = error.response?.data?.message || error.message
            return { success: false, error: error.value , message: "Failed to complete the inspection, error occurred" }
        }finally {
            loading.value = false
            error.value = null
        }
    }

    const deleteInspection = async (id) => {
        try{
            loading.value = true
            error.value = false

            const response = await axios.delete(`${baseUrl.value}/${id}`)
            if(response.status === 200){
                return { success: true, message: "Inspection deleted successfully" }
            }
            return { success: false, error: response.data.message, message: "Failed to delete the inspection" }

        }catch (error) {
            console.error("Error fetching inspections:", error.response?.data || error.message)
            error.value = error.response?.message
            return { success: false, error: error.value, message: "Failed to delete the inspection" }
        }finally {
            loading.value = false
            error.value = null
        }
    }

    return {
        isLoading,
        errorMessage,
        allInspections,
        getInspectionSchedule,
        fetchInspections,
        scheduleInspection,
        createInspection,
        completeInspection,
        deleteInspection,
    }
})
