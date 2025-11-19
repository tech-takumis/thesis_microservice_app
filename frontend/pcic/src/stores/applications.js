import {defineStore} from 'pinia'
import {ref,computed} from 'vue'
import axios from "@/lib/axios"


export const useApplicationStore = defineStore("applicationStore",() => {
    const applications = ref([])
    const workflow = ref(null)
    const loading = ref(false)
    const error = ref(null)
    const baseUrl = ref("/api/v1/applications")

    const getAllApplication = computed(() => applications.value)
    const isLoading = computed(() => loading.value)
    const hasError = computed(() => error.value)
    const getApplicationWorkflow = computed(() => workflow.value)

    const fetchApplications = async () => {
        try{
            loading.value = true
            error.value = null

            const response = await axios.get(baseUrl.value)

            if(response.status >= 200 || response.status < 300){
                console.log("Successfully fetch all application types "+" "+response.data);
                applications.value = response.data
                return {success: "true", message: "Successfully fetch all application types", data: response.data}
            }
            return {success: "false", message: response.data.message, data: null}
        }catch (error){
            console.log("Failed to fetch all application types");
            error.value = error.data.message;
            loading.value = false
            return {success: "false", message: error.value, data: null}
        }finally {
            loading.value = false
        }
    }

    const fetchApplicationsById = async (id) => {
        try{
            loading.value = true
            error.value = null

            const response = await axios.get(`${baseUrl.value}/${id}`)

            if(response.status >= 200 || response.status < 300){
                console.log("Successfully fetch application by id "+" "+response.data);
                return {success: "true", message: "Successfully fetch application by id", data: response.data}
            }

            return {success: "false", message: response.data.message, data: null}
        }catch (error){
            console.log("Failed to fetch application by id");
            error.value = error.data.message;
            loading.value = false
            return {success: "false", message: error.value, data: null}
        }finally {
            loading.value = false
        }
    }
    const fetchApplicationWorkflow = async (id) => {
        try{
            loading.value = true
            error.value = null

            const response = await axios.get(`${baseUrl.value}/${id}/workflow`)

            if(response.status >= 200 && response.status < 300){
                console.log("Application workflow fetched successfully", response.data);
                workflow.value = response.data
                return {success: true, message: "Application workflow fetched successfully", data: response.data}
            }

            return {success: false, message: response.data.message, data: null}
        }catch (error) {
            console.log("Failed to fetch application workflow:", error.response?.data?.message || error.message);
            error.value = error.response?.data?.message || error.message;
            loading.value = false
            return {success: false, message: error.value, data: null}
        }finally {
            loading.value = false
        }
    }

    const deleteApplication = async (id) => {
        try{
            loading.value = true
            error.value = null

            const response = await axios.delete(`${baseUrl.value}/${id}`)

            if(response.status >= 200 || response.status < 300){
                console.log("Successfully delete application by id "+" "+response.data);
                return {success: "true", message: "Successfully delete application by id", data: response.data}
            }

            return {success: "false", message: response.data.message, data: null}
        }catch (error){
            console.log("Failed to delete application by id");
            error.value = error.data.message;
            loading.value = false
            return {success: "false", message: error.value, data: null}
        }finally {
            loading.value = false
        }
    }

    return {
        getAllApplication,
        isLoading,
        hasError,
        getApplicationWorkflow,
        fetchApplications,
        fetchApplicationsById,
        fetchApplicationWorkflow,
        deleteApplication,
    }
})