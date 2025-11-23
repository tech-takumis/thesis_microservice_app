import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from '@/lib/axios'

export const useRsbsaStore = defineStore('rsbsa', () => {
    const rsbsa = ref([])
    const loading = ref(false)
    const error = ref(null)
    const baseURL = ref('http://localhost:9001/api/v1/rsbsa')

    const allRsbsa = computed(() => rsbsa.value)
    const isLoading = computed(() => loading.value)
    const hasError = computed(() => error.value != null)
    const errorMessage = computed(() => error.value)

    // Save (POST)
    const createRsbsa = async data => {
        loading.value = true
        error.value = null
        try {
            const response = await axios.post(baseURL.value, data)
            if (response.status === 201) {
                return { success: true, data: response.data }
            }
            return {
                success: false,
                message: 'Failed to create RSBSA',
                data: null,
            }
        } catch (err) {
            error.value = err
            return { success: false, message: err.message, data: null }
        } finally {
            loading.value = false
        }
    }

    // Find All (GET)
    const fetchAllRsbsa = async () => {
        loading.value = true
        error.value = null
        try {
            const response = await axios.get(baseURL.value)
            if (response.status === 302 || response.status === 200) {
                rsbsa.value = response.data
                return { success: true, data: response.data }
            }
            return {
                success: false,
                message: 'Failed to fetch RSBSA',
                data: null,
            }
        } catch (err) {
            error.value = err
            return { success: false, message: err.message, data: null }
        } finally {
            loading.value = false
        }
    }

    // Exists (GET)
    const checkRsbsaExists = async rsbsaId => {
        loading.value = true
        error.value = null
        try {
            const response = await axios.get(
                `${baseURL.value}/${rsbsaId}/exists`,
            )
            if (response.status === 200) {
                return { success: true, exists: response.data }
            }
            return {
                success: false,
                message: 'Failed to check existence',
                exists: false,
            }
        } catch (err) {
            error.value = err
            return { success: false, message: err.message, exists: false }
        } finally {
            loading.value = false
        }
    }

    // Find by RSBSA ID (GET)
    const fetchRsbsaById = async rsbsaId => {
        loading.value = true
        error.value = null
        try {
            const response = await axios.get(`${baseURL.value}/${rsbsaId}`)
            if (response.status === 200) {
                return { success: true, data: response.data }
            }
            return {
                success: false,
                message: 'Failed to fetch RSBSA by ID',
                data: null,
            }
        } catch (err) {
            error.value = err
            return { success: false, message: err.message, data: null }
        } finally {
            loading.value = false
        }
    }

    // Update (PUT)
    const updateRsbsa = async (rsbsaId, updateData) => {
        loading.value = true
        error.value = null
        try {
            const response = await axios.put(
                `${baseURL.value}/${rsbsaId}`,
                updateData,
            )
            if (response.status === 200) {
                return { success: true, data: response.data }
            }
            return {
                success: false,
                message: 'Failed to update RSBSA',
                data: null,
            }
        } catch (err) {
            error.value = err
            return { success: false, message: err.message, data: null }
        } finally {
            loading.value = false
        }
    }

    // Delete (DELETE)
    const deleteRsbsa = async rsbsaId => {
        loading.value = true
        error.value = null
        try {
            const response = await axios.delete(`${baseURL.value}/${rsbsaId}`)
            if (response.status === 204 || response.status === 200) {
                // Remove from local state
                rsbsa.value = rsbsa.value.filter(r => r.rsbsaId !== rsbsaId)
                return { success: true, message: 'RSBSA deleted successfully' }
            }
            return { success: false, message: 'Failed to delete RSBSA' }
        } catch (err) {
            error.value = err
            return { success: false, message: err.message }
        } finally {
            loading.value = false
        }
    }

    // Bulk Delete (DELETE)
    const bulkDeleteRsbsa = async rsbsaIds => {
        loading.value = true
        error.value = null
        try {
            const deletePromises = rsbsaIds.map(id =>
                axios.delete(`${baseURL.value}/${id}`)
            )

            const results = await Promise.allSettled(deletePromises)

            const successCount = results.filter(r =>
                r.status === 'fulfilled' &&
                (r.value.status === 204 || r.value.status === 200)
            ).length

            const failedCount = results.length - successCount

            // Remove successfully deleted items from local state
            if (successCount > 0) {
                rsbsa.value = rsbsa.value.filter(r => !rsbsaIds.includes(r.rsbsaId))
            }

            if (failedCount === 0) {
                return {
                    success: true,
                    message: `Successfully deleted ${successCount} RSBSA record(s)`,
                    deleted: successCount
                }
            } else if (successCount > 0) {
                return {
                    success: true,
                    message: `Deleted ${successCount} RSBSA record(s), ${failedCount} failed`,
                    deleted: successCount,
                    failed: failedCount
                }
            } else {
                return {
                    success: false,
                    message: 'Failed to delete all RSBSA records',
                    deleted: 0,
                    failed: failedCount
                }
            }
        } catch (err) {
            error.value = err
            return { success: false, message: err.message }
        } finally {
            loading.value = false
        }
    }

    return {
        rsbsa,
        loading,
        error,
        baseURL,
        allRsbsa,
        isLoading,
        hasError,
        errorMessage,
        createRsbsa,
        fetchAllRsbsa,
        checkRsbsaExists,
        fetchRsbsaById,
        updateRsbsa,
        deleteRsbsa,
        bulkDeleteRsbsa,
    }
})
