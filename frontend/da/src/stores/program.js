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
        id: backendProgram.programId,
        name: backendProgram.programName,
        description: backendProgram.description,
        budget: backendProgram.budget,
        completedPercentage: backendProgram.completedPercentage,
        status: backendProgram.status,
        startDate: backendProgram.startDate,
        endDate: backendProgram.endDate,
        beneficiaries: backendProgram.beneficiaries || []
    })

    // Helper function to map frontend data to backend format
    const mapToBackendFormat = (frontendData) => ({
        programName: frontendData.name,
        description: frontendData.description,
        budget: frontendData.budget,
        startDate: frontendData.startDate,
        endDate: frontendData.endDate,
        status: frontendData.status || 'planned'
    })

    // Filter computed properties
    const activePrograms = computed(() =>
        programs.value.filter(program => program.status === 'ongoing')
    )
    const completedPrograms = computed(() =>
        programs.value.filter(program => program.status === 'completed')
    )
    const plannedPrograms = computed(() =>
        programs.value.filter(program => program.status === 'planned')
    )
    const highBudgetPrograms = computed(() =>
        programs.value.filter(program => program.budget >= 2000000)
    )
    const programsStats = computed(() => {
        const total = programs.value.length
        const active = programs.value.filter(program => program.status === 'ongoing').length
        const completed = programs.value.filter(program => program.status === 'completed').length
        const planned = programs.value.filter(program => program.status === 'planned').length
        const totalBudget = programs.value.reduce((sum, program) => sum + program.budget, 0)
        const averageCompletion = total > 0
            ? programs.value.reduce((sum, program) => sum + program.completedPercentage, 0) / total
            : 0
        return { total, active, completed, planned, totalBudget, averageCompletion }
    })

    // Actions
    const getAllPrograms = async () => {
        try {
            loading.value = true
            error.value = null

            const response = await axios.get('/api/v1/agriculture/programs')

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
            const response = await axios.post('/api/v1/agriculture/programs', payload)

            if (response.data && response.data.programId) {
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
            const response = await axios.put(`/api/v1/agriculture/programs/${id}`, backendData)
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

    const deleteProgram = async (id) => {
        try {
            loading.value = true
            error.value = null

            await axios.delete(`/api/v1/agriculture/programs/${id}`)
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
        plannedPrograms,
        highBudgetPrograms,
        programsStats,

        // Actions
        getAllPrograms,
        createProgram,
        updateProgram,
        deleteProgram
    }
})
