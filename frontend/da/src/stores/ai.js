import {defineStore} from 'pinia'
import {ref,computed} from 'vue'
import axios from '@/lib/axios'

export const useAIStore = defineStore('ai', () => {
    const aiResult = ref(null)
    const loading = ref(false)
    const error = ref(null)

    const hasResult = computed(() => aiResult.value !== null)
    const isLoading = computed(() => loading.value)
    const hasError = computed(() => error.value !== null)
    const getResult = computed(() => aiResult.value)

    const getAiResult = async (applicationId) => {
        try{
            loading.value = true
            error.value = null

            const response = await axios.get(`/api/v1/ai/${applicationId}`)

            if(response.status === 200){
                aiResult.value = response.data
                return {success: true, message: "AI result fetched successfully", data: response.data}
            }

            return {success: false, message: "Failed to fetch AI result", data: null}
        }catch (error){
            console.log("Error fetching AI result:", error)
            error.value = error.response?.data?.message || error.message
            return {success: false, message: error.response?.data?.message || error.message, data: null}
        }finally {
            loading.value = false
        }
    }

    return {
        getResult,
        hasResult,
        isLoading,
        hasError,
        getAiResult
    }

})