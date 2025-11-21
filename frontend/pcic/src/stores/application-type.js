import {defineStore} from 'pinia'
import {ref,computed} from 'vue'
import axios from '@/lib/axios'

export const useApplicationTypeStore = defineStore('applicationType', () => {
    const applicationTypes = ref([])
    const loading = ref(false)
    const error = ref(null)
    const basePath = ref('/api/v1/application/types')


    const allApplicationTypes = computed(() => applicationTypes.value)
    const isLoading = computed(() => loading.value)
    const errorMessage = computed(() => error.value)
    const fetchAllApplicationTypes = async () => {
        try{
            loading.value = true
            error.value = null
            const response = await axios.get(basePath.value)

            if(response.status > 200 && response.status < 300 || response.status === 200){
                applicationTypes.value = response.data
                return {success: true, message: "Successfully fetch all application types", data: response.data}
            }

            return {success: false, message: response.data.message, data: null}
        }catch (err){
            console.log("Failed to fetch all application types");
            error.value = err.response?.data?.message || err.message;
            return {success: false, message: error.value, data: null}
        }finally {
            loading.value = false
        }
    }

    // Utility: Map application types for dropdown/filter
    const mappedApplicationTypes = computed(() => {
        return applicationTypes.value.map(type => ({
            label: type.name || type.typeName || 'Unknown',
            value: type.id || type.typeId || type.code || type.name,
            description: type.description || '',
            raw: type
        }))
    })

    // Utility: Build query params from mapping object
    const buildQueryParams = (paramsObj = {}) => {
        const searchParams = new URLSearchParams()
        Object.entries(paramsObj).forEach(([key, value]) => {
            if (value !== undefined && value !== null && value !== '') {
                searchParams.append(key, value)
            }
        })
        return searchParams.toString()
    }

    // Example: Get query string for selected filters
    // const queryString = buildQueryParams({ type: 'rice', region: 'Luzon' })

    // Fetch application types with optional query params
    const fetchApplicationTypes = async ({ provider, sections, application } = {}) => {
        try {
            loading.value = true
            error.value = null
            const params = buildQueryParams({ provider, sections, application })
            const url = params ? `${basePath.value}?${params}` : basePath.value
            const response = await axios.get(url)
            if (response.status >= 200 && response.status < 300) {
                applicationTypes.value = response.data
                return { success: true, message: 'Fetched application types', data: response.data }
            }
            return { success: false, message: response.data.message, data: null }
        } catch (err) {
            error.value = err.response?.data?.message || err.message
            return { success: false, message: error.value, data: null }
        } finally {
            loading.value = false
        }
    }

    // Fetch single application type by ID (with optional params)
    const fetchApplicationTypeById = async (id, { sections, application } = {}) => {
        try {
            loading.value = true
            error.value = null
            const params = buildQueryParams({ sections, application })
            const url = params ? `${basePath.value}/${id}?${params}` : `${basePath.value}/${id}`
            const response = await axios.get(url)
            if (response.status >= 200 && response.status < 300) {
                return { success: true, message: 'Fetched application type', data: response.data }
            }
            return { success: false, message: response.data.message, data: null }
        } catch (err) {
            error.value = err.response?.data?.message || err.message
            return { success: false, message: error.value, data: null }
        } finally {
            loading.value = false
        }
    }

    // Check if application type requires AI analysis
    const checkRequiresAIAnalysis = async (id) => {
        try {
            loading.value = true
            error.value = null
            const url = `${basePath.value}/${id}/requires-ai-analysis`
            const response = await axios.get(url)
            if (response.status >= 200 && response.status < 300) {
                return { success: true, requiresAI: response.data }
            }
            return { success: false, requiresAI: false }
        } catch (err) {
            error.value = err.response?.data?.message || err.message
            return { success: false, requiresAI: false }
        } finally {
            loading.value = false
        }
    }

    // Create new application type
    const createApplicationType = async (dto) => {
        try {
            loading.value = true
            error.value = null
            const response = await axios.post(basePath.value, dto)
            if (response.status === 201) {
                // Optionally refresh list
                await fetchAllApplicationTypes()
                return { success: true, message: 'Created application type', data: response.data }
            }
            return { success: false, message: response.data.message, data: null }
        } catch (err) {
            error.value = err.response?.data?.message || err.message
            return { success: false, message: error.value, data: null }
        } finally {
            loading.value = false
        }
    }

    // Delete application type by ID
    const deleteApplicationType = async (id) => {
        try {
            loading.value = true
            error.value = null
            const url = `${basePath.value}/${id}`
            const response = await axios.delete(url)
            if (response.status === 204) {
                // Optionally refresh list
                await fetchAllApplicationTypes()
                return { success: true, message: 'Deleted application type' }
            }
            return { success: false, message: response.data.message }
        } catch (err) {
            error.value = err.response?.data?.message || err.message
            return { success: false, message: error.value }
        } finally {
            loading.value = false
        }
    }

    return {
        allApplicationTypes,
        isLoading,
        errorMessage,
        fetchAllApplicationTypes,
        mappedApplicationTypes,
        buildQueryParams,
        fetchApplicationTypes,
        fetchApplicationTypeById,
        checkRequiresAIAnalysis,
        createApplicationType,
        deleteApplicationType
    }

})