import {defineStore} from 'pinia'
import {ref,computed} from 'vue'
import axios from '@/lib/axios'


export const useInsuranceStore = defineStore("insuranceStore",() => {
    const insurance = ref([])
    const loading = ref(false);
    const error = ref(null)
    const basePath = ref("/api/v1/insurance")


    const allInsurance = computed(() => insurance.value);
    const isLoading = computed(() => loading.value)
    const hasError = computed(() => error.value);


    const fetchAllInsurance = async () => {
        try{
            loading.value = true
            error.value = null

            const response = await axios.get(basePath.value);

            if(response.status === 200){
                insurance.value = response.data
                return {success: "true", message: "Successfully fetch all application"}
            }

            return {success: "false",message: response.data.message};
        }catch (error){
            console.log("Failed to fetch all insurance");
            error.value = error.data.message;
            loading.value = false
            return {success: "false", message: error.value}
        }finally {
            loading.value = false
            error.value = null
        }

    }

    const fetchVerifiedInsurance = async () => {
        try{
            loading.value = true
            error.value = null

            const response = await axios.get(`${basePath.value}/verified`);

            if(response.data && response.data.length > 0){
                insurance.value = response.data;
                return {success: "true", message: "Successfully fetch all verified insurance", data: insurance.value}
            }

            return {success: "false", message: "No verified insurance found",data:null}
        }catch (error){
            console.log("Failed to fetch verified insurance");
            error.value = error.data.message;
            loading.value = false
            return {success: "false", message: error.value, data: null}
        }finally {
            loading.value = false
        }
    }

    const fetchInsuranceById = async (id) => {
        try{
            loading.value = true
            error.value = null

            const response = await axios.get(`${basePath.value}/${id}`);

            if(response.data && response.data.insuranceId){
                return {success: true, message: "Successfully fetch insurance by id", data: response.data}
            }
            return {success: false, message: "Insurance not found", data: null}
        }catch (error){
            loading.value = false
            error.value = error.response?.data?.message || error.message;
            return {success: false, message: error.value, data: null}
        }finally {
            loading.value = false
        }
    }

    const fetchInsuranceBySubmissionId = async (submissionId) => {
        try{
            loading.value = true
            error.value = null

            const response = await axios.get(`${basePath.value}/submission/${submissionId}`);

            if(response.data && response.data.insuranceId){
                return {success: true, message: "Successfully fetch insurance by submission id", data: response.data}
            }
            return {success: false, message: "Insurance not found", data: null}
        }catch (error){
            loading.value = false
            error.value = error.response?.data?.message || error.message;
            return {success: false, message: error.value, data: null}
        }finally {
            loading.value = false
        }
    }

    const updateInsurance = async (id, data) => {
        try{
            loading.value = true
            error.value = null

            const response = await axios.put(`${basePath.value}/${id}`, data);

            if(response.status === 200){
                return {success: "true", message: "Successfully update insurance"}
            }
            return {success: "false",message: response.data.message};
        }catch (error){
            console.log("Failed to update insurance");
            error.value = error.data.message;
            loading.value = false
            return {success: "false", message: error.value}
        }finally {
            loading.value = false
            error.value = null
        }
    }

    const deleteInsurance = async (id) => {
        try{
            loading.value = true
            error.value = null

            const response = await axios.delete(`${basePath.value}/${id}`);
            if(response.status === 200){
                return {success: "true", message: "Successfully delete insurance"}
            }
            return {success: "false",message: response.data.message};
        }catch (error){
            console.log("Failed to delete insurance");
            error.value = error.data.message;
            loading.value = false
            return {success: "false", message: error.value}
        }finally {
            loading.value = false
            error.value = null
        }
    }

    return {
        allInsurance,
        isLoading,
        hasError,
        fetchAllInsurance,
        fetchVerifiedInsurance,
        fetchInsuranceById,
        fetchInsuranceBySubmissionId,
        updateInsurance,
        deleteInsurance,
    }

});