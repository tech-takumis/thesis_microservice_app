import {defineStore } from "pinia";
import {ref,computed} from "vue";
import axios from "@/lib/axios";

export const useApplicationStore = defineStore('application', () => {
    const application = ref(null)
    const applications = ref([])
    const loading =  ref(false)
    const error = ref(null)
    const workflow = ref(null)

    const getAllApplications = computed(() => applications.value)
    const getApplication = computed(() => application.value)
    const isLoading = computed(() => loading.value)
    const hasError = computed(() => error.value)
    const getWorkflow = computed(() => workflow.value)
    const basePath = ref('/api/v1/applications')

    const fetchAllApplications = async () => {
        try{
            loading.value = true
            error.value = null

            const response = await axios.get(basePath.value)
            if(response.status > 200 && response.status < 300 || response.status === 200){
                applications.value = response.data
                return {success: true, message: "Successfully fetch all applications", data: response.data}
            }

            return {success: false, message: response.data.message, data: null}
        }catch (err){
            console.log("Failed to fetch all applications");
            error.value = err.response?.data?.message || err.message;
            return {success: false, message: error.value, data: null}
        }finally {
            loading.value = false
        }
    }

    const fetchApplicationById = async (id) => {
        try{
            loading.value = true
            error.value = null

            const response = await axios.get(`${basePath.value}/${id}`)

            if(response.status > 200 && response.status < 300 || response.status === 200){
                application.value = response.data
                return {success: true, message: "Successfully fetch application by id", data: response.data}
            }

            return {success: false, message: response.data.message, data: null}
        }catch (err){
            console.log("Failed to fetch application by id");
            error.value = err.response?.data?.message || err.message;
            return {success: false, error: error.value, data: null}
        }finally {
            loading.value = false
        }
    }



    const fetchApplicationWorkflow = async (id) => {
        try{
            loading.value = true
            error.value = null

            const response = await axios.get(`${basePath.value}/${id}/workflow`)

            if(response.status > 200 && response.status < 300 || response.status === 200){
                workflow.value = response.data
                return {success: true, message: "Successfully fetch application workflow", data: response.data}
            }

            return {success: false, message: response.data.message, data: null}
        }catch (err){
            console.log("Failed to fetch application workflow");
            error.value = err.response?.data?.message || err.message;
            return {success: false, message: error.value, data: null}
        }finally {
            loading.value = false
        }
    }

    const updateApplication = async (id, data) => {
        try{
            loading.value = true
            error.value = null

            const response = await axios.put(`${basePath.value}/${id}`, data)

            if(response.status > 200 && response.status < 300 || response.status === 200){
                applications.value = applications.value.map(app => app.id === id ? response.data : app)
                return {success: true, message: "Successfully update application", data: response.data}
            }

            return {success: false, message: response.data.message, data: null}
        }catch (err){
            console.log("Failed to update application");
            error.value = err.response?.data?.message || err.message;
            return {success: false, message: error.value, data: null}
        }finally {
            loading.value = false
        }
    }


    const deleteApplication = async (id) => {
        try{
            loading.value = true
            error.value = null

            const response = await axios.delete(`${basePath.value}/${id}`)

            if(response.status > 200 && response.status < 300 || response.status === 200){
                applications.value = applications.value.filter(app => app.id !== id)
                return {success: true, message: "Successfully delete application", data: null}
            }

            return {success: false, message: response.data.message, data: null}
        }catch (err){
            console.log("Failed to delete application");
            error.value = err.response?.data?.message || err.message;
            return {success: false, message: error.value, data: null}
        }finally {
            loading.value = false
        }
    }

    const isAiAnalysisRequired = async (applicationId) => {
        try{
            loading.value = true
            error.value = null

            const response = await axios.get(`${basePath.value}/${applicationId}/required-ai-analysis`)

            if(response.status > 200 && response.status < 300 || response.status === 200){
                return {success: true, message: "Successfully checked AI analysis requirement", data: response.data}
            }

            return {success: false, message: response.data.message, data: null}
        }catch (err){
            console.log("Failed to check AI analysis requirement");
            error.value = err.response?.data?.message || err.message;
            return {success: false, message: error.value, data: null}
        }finally {
            loading.value = false
        }
    }

    return {
        getAllApplications,
        getApplication,
        isLoading,
        hasError,
        getWorkflow,
        fetchAllApplications,
        fetchApplicationById,
        fetchApplicationWorkflow,
        updateApplication,
        deleteApplication,
        isAiAnalysisRequired
    }
})