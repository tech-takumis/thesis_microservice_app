import axios from '@/lib/axios'
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useProgramStore = defineStore('program', () => {
    // State
    const programs = ref([])
    const loading = ref(false)
    const error = ref(null)

    // Computed
    const allPrograms = computed(() => programs.value)
    const isLoading = computed(() => loading.value)
    const hasError = computed(() => error.value != null)
    const errorMessage = computed(() => error.value)

    // Helper function to map backend data to frontend format
    const mapProgramData = (backendProgram) => ({
        id: backendProgram.id,
        name: backendProgram.name,
        type: backendProgram.type,
        status: backendProgram.status,
        completion: backendProgram.completion,
        notes: backendProgram.notes,
        createdAt: backendProgram.createdAt,
        updatedAt: backendProgram.updatedAt
    })

    // Helper function to map frontend data to backend format
    const mapToBackendFormat = (frontendData) => ({
        name: frontendData.name || '',
        type: frontendData.type || 'TRAINING',
        status: frontendData.status || 'PENDING',
        completion: typeof frontendData.completion === 'number' ? frontendData.completion : 0,
        notes: frontendData.notes || ''
    })

    // Filter computed properties
    const activePrograms = computed(() =>
        programs.value.filter(program => program.status === 'ACTIVE')
    )
    const completedPrograms = computed(() =>
        programs.value.filter(program => program.status === 'COMPLETED')
    )
    const pendingPrograms = computed(() =>
        programs.value.filter(program => program.status === 'PENDING')
    )
    const latestPrograms = computed(() => {
        // Sort by createdAt (newest first) and return only the latest 5
        return [...programs.value]
            .sort((a, b) => {
                const dateA = new Date(a.createdAt || 0)
                const dateB = new Date(b.createdAt || 0)
                return dateB - dateA // Descending order (newest first)
            })
            .slice(0, 5)
    })
    const programsStats = computed(() => {
        const total = programs.value.length
        const active = programs.value.filter(program => program.status === 'ACTIVE').length
        const completed = programs.value.filter(program => program.status === 'COMPLETED').length
        const pending = programs.value.filter(program => program.status === 'PENDING').length
        const inactive = programs.value.filter(program => program.status === 'INACTIVE').length
        const cancelled = programs.value.filter(program => program.status === 'CANCELLED').length
        const averageCompletion = total > 0
            ? programs.value.reduce((sum, program) => sum + program.completion, 0) / total
            : 0
        return { total, active, completed, pending, inactive, cancelled, averageCompletion }
    })

    // Actions
    const getAllPrograms = async () => {
        try {
            loading.value = true
            error.value = null

            const response = await axios.get('/api/v1/programs')

            // Handle array response directly
            const rawPrograms = Array.isArray(response.data) ? response.data : []
            const mappedPrograms = rawPrograms.map(mapProgramData)

            programs.value = mappedPrograms

            return {
                success: true,
                message: "Programs retrieved successfully",
                data: mappedPrograms
            }
        } catch (error) {
            const errorMessage = error.response?.data?.message || error.message
            error.value = errorMessage
            programs.value = []
            return {
                success: false,
                message: errorMessage,
                data: []
            }
        } finally {
            loading.value = false
        }
    }

    const createProgram = async (data) => {
        loading.value = true
        error.value = null

        try {
            const payload = mapToBackendFormat(data)
            const response = await axios.post('/api/v1/programs', payload)

            if ((response.status > 200 && response.status < 300) || response.status === 200) {
                const program = mapProgramData(response.data)
                programs.value.unshift(program)

                return {
                    success: true,
                    message: "Program created successfully",
                    data: program
                }
            }

            return {
                success: false,
                message: "No program ID received from server",
                data: null
            }
        } catch (err) {
            const errorMessage = err.response?.data?.message || err.message || "Network error"
            error.value = errorMessage
            return {
                success: false,
                message: errorMessage,
                data: null
            }
        } finally {
            loading.value = false
        }
    }

    const updateProgram = async (id, data) => {
        try {
            loading.value = true
            error.value = null

            const backendData = mapToBackendFormat(data)
            const response = await axios.put(`/api/v1/programs/${id}`, backendData)
            const mappedProgram = mapProgramData(response.data)

            const index = programs.value.findIndex(p => p.id === id)
            if (index !== -1) {
                programs.value[index] = mappedProgram
            }

            return { success: true, message: "Program updated successfully", data: mappedProgram }
        } catch (error) {
            const errorMessage = error.response?.data?.message || error.message
            error.value = errorMessage
            return { success: false, message: errorMessage, data: null }
        } finally {
            loading.value = false
        }
    }

    const getProgram = async (id) => {
        try {
            loading.value = true
            error.value = null

            const response = await axios.get(`/api/v1/programs/${id}`)
            const mappedProgram = mapProgramData(response.data)

            return {
                success: true,
                message: "Program retrieved successfully",
                data: mappedProgram
            }
        } catch (error) {
            const errorMessage = error.response?.data?.message || error.message
            error.value = errorMessage
            return {
                success: false,
                message: errorMessage,
                data: null
            }
        } finally {
            loading.value = false
        }
    }

    const deleteProgram = async (id) => {
        try {
            loading.value = true
            error.value = null

            await axios.delete(`/api/v1/programs/${id}`)
            programs.value = programs.value.filter(p => p.id !== id)

            return { success: true, message: "Program deleted successfully" }
        } catch (error) {
            const errorMessage = error.response?.data?.message || error.message
            error.value = errorMessage
            return { success: false, message: errorMessage }
        } finally {
            loading.value = false
        }
    }

    return {
        // State
        programs,
        loading,
        error,

        // Computed
        allPrograms,
        isLoading,
        hasError,
        errorMessage,
        activePrograms,
        completedPrograms,
        pendingPrograms,
        latestPrograms,
        programsStats,

        // Actions
        getAllPrograms,
        getProgram,
        createProgram,
        updateProgram,
        deleteProgram
    }
})
