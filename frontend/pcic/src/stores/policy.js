import {defineStore} from 'pinia';
import {ref, computed} from 'vue';
import axios from '@/lib/axios';

export const usePolicyStore = defineStore('policy', () => {
    const policies = ref([]);
    const policy = ref(null)
    const loading = ref(false);
    const error = ref(null);
    const baseUrl = ref('/api/v1/policies');

    const isLoading = computed(() => loading.value);
    const errorMessage = computed(() => error.value);
    const allPolicies = computed(() => policies.value);
    const currentPolicy = computed(() => policy.value);

    // Create a new policy
    const createPolicy = async (data) => {
        try{
            loading.value = true
            error.value = null

            const response = await axios.post(baseUrl.value, data)

            if(response.status === 201){
                policy.value = response.data
                return {success: true, message: "Policy created successfully", data: response.data}
            }

            return {success: false, message: "Failed to create policy", error: response.data?.message}
        }catch(error){
            console.error("Error creating policy:", error.response?.data || error.message);
            error.value = error.response?.data?.message || error.message;
            return {success: false, message: "Failed to create policy, error occurred", error: error.value}
        }finally{
            loading.value = false   
        }
    }

    // Get policy by ID
    const getPolicyById = async (id) => {
        try {
            loading.value = true;
            error.value = null;

            const response = await axios.get(`${baseUrl.value}/${id}`);

            if(response.status === 200){
                policy.value = response.data
                return {success: true, message: "Policy fetched successfully", data: response.data}
            }

            return {success: false, message: "Failed to fetch policy", error: response.data?.message}
        }catch(error){
            console.error("Error fetching policy by ID:", error.response?.data || error.message);
            error.value = error.response?.data?.message || error.message;
            return {success: false, message: "Failed to fetch policy, error occurred", error: error.value}
        }finally{
            loading.value = false
        }
    }

    // Get policy by insurance ID
    const getPolicyByInsuranceId = async (insuranceId) => {
        try {
            loading.value = true;
            error.value = null;

            const response = await axios.get(`${baseUrl.value}/insurance/${insuranceId}`);

            if(response.status === 200){
                policy.value = response.data
                return {success: true, message: "Policy fetched successfully", data: response.data}
            }

            return {success: false, message: "Failed to fetch policy", error: response.data?.message}
        }catch(error){
            console.error("Error fetching policy by insurance ID:", error.response?.data || error.message);
            error.value = error.response?.data?.message || error.message;
            return {success: false, message: "Failed to fetch policy, error occurred", error: error.value}
        }finally{
            loading.value = false
        }
    }

    // Get policy by policy number
    const getPolicyByPolicyNumber = async (policyNumber) => {
        try {
            loading.value = true;
            error.value = null;

            const response = await axios.get(`${baseUrl.value}/policy-number/${policyNumber}`);

            if(response.status === 200){
                policy.value = response.data
                return {success: true, message: "Policy fetched successfully", data: response.data}
            }

            return {success: false, message: "Failed to fetch policy", error: response.data?.message}
        }catch(error){
            console.error("Error fetching policy by policy number:", error.response?.data || error.message);
            error.value = error.response?.data?.message || error.message;
            return {success: false, message: "Failed to fetch policy, error occurred", error: error.value}
        }finally{
            loading.value = false
        }
    }

    // Get all policies
    const getAllPolicies = async () => {
        try {
            loading.value = true;
            error.value = null;

            const response = await axios.get(baseUrl.value);

            if(response.status === 200){
                policies.value = response.data
                return {success: true, message: "Policies fetched successfully", data: response.data}
            }

            return {success: false, message: "Failed to fetch policies", error: response.data?.message}
        }catch(error){
            console.error("Error fetching policies:", error.response?.data || error.message);
            error.value = error.response?.data?.message || error.message;
            return {success: false, message: "Failed to fetch policies, error occurred", error: error.value}
        }finally{
            loading.value = false
        }
    }

    // Get policies by farmer ID
    const getPoliciesByFarmerId = async (farmerId) => {
        try {
            loading.value = true;
            error.value = null;

            const response = await axios.get(`${baseUrl.value}/farmer/${farmerId}`);

            if(response.status === 200){
                policies.value = response.data
                return {success: true, message: "Farmer policies fetched successfully", data: response.data}
            }

            return {success: false, message: "Failed to fetch farmer policies", error: response.data?.message}
        }catch(error){
            console.error("Error fetching farmer policies:", error.response?.data || error.message);
            error.value = error.response?.data?.message || error.message;
            return {success: false, message: "Failed to fetch farmer policies, error occurred", error: error.value}
        }finally{
            loading.value = false
        }
    }

    // Get active policies
    const getActivePolicies = async () => {
        try {
            loading.value = true;
            error.value = null;

            const response = await axios.get(`${baseUrl.value}/active`);

            if(response.status === 200){
                policies.value = response.data
                return {success: true, message: "Active policies fetched successfully", data: response.data}
            }

            return {success: false, message: "Failed to fetch active policies", error: response.data?.message}
        }catch(error){
            console.error("Error fetching active policies:", error.response?.data || error.message);
            error.value = error.response?.data?.message || error.message;
            return {success: false, message: "Failed to fetch active policies, error occurred", error: error.value}
        }finally{
            loading.value = false
        }
    }

    // Get expired policies
    const getExpiredPolicies = async () => {
        try {
            loading.value = true;
            error.value = null;

            const response = await axios.get(`${baseUrl.value}/expired`);

            if(response.status === 200){
                policies.value = response.data
                return {success: true, message: "Expired policies fetched successfully", data: response.data}
            }

            return {success: false, message: "Failed to fetch expired policies", error: response.data?.message}
        }catch(error){
            console.error("Error fetching expired policies:", error.response?.data || error.message);
            error.value = error.response?.data?.message || error.message;
            return {success: false, message: "Failed to fetch expired policies, error occurred", error: error.value}
        }finally{
            loading.value = false
        }
    }

    // Update policy
    const updatePolicy = async (id, data) => {
        try {
            loading.value = true;
            error.value = null;

            const response = await axios.put(`${baseUrl.value}/${id}`, data);

            if(response.status === 200){
                policy.value = response.data
                return {success: true, message: "Policy updated successfully", data: response.data}
            }

            return {success: false, message: "Failed to update policy", error: response.data?.message}
        }catch(error){
            console.error("Error updating policy:", error.response?.data || error.message);
            error.value = error.response?.data?.message || error.message;
            return {success: false, message: "Failed to update policy, error occurred", error: error.value}
        }finally{
            loading.value = false
        }
    }

    // Delete policy
    const deletePolicy = async (id) => {
        try {
            loading.value = true;
            error.value = null;

            const response = await axios.delete(`${baseUrl.value}/${id}`);

            if(response.status === 204){
                // Remove from policies array if it exists
                const index = policies.value.findIndex(p => p.id === id);
                if (index > -1) {
                    policies.value.splice(index, 1);
                }
                
                // Clear current policy if it matches deleted ID
                if (policy.value?.id === id) {
                    policy.value = null;
                }

                return {success: true, message: "Policy deleted successfully"}
            }

            return {success: false, message: "Failed to delete policy", error: response.data?.message}
        }catch(error){
            console.error("Error deleting policy:", error.response?.data || error.message);
            error.value = error.response?.data?.message || error.message;
            return {success: false, message: "Failed to delete policy, error occurred", error: error.value}
        }finally{
            loading.value = false
        }
    }

    return {
        // State
        isLoading,
        errorMessage,
        allPolicies,
        currentPolicy,
        
        // Actions
        createPolicy,
        getPolicyById,
        getPolicyByInsuranceId,
        getPolicyByPolicyNumber,
        getAllPolicies,
        getPoliciesByFarmerId,
        getActivePolicies,
        getExpiredPolicies,
        updatePolicy,
        deletePolicy
    }
})