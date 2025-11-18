import axios from '@/lib/axios'
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export const useProgramStore = defineStore('program', () => {
    // State
    const programs = ref([])
    const loading = ref(false)
    const error = ref(null)

    // Getters as computed
    const allPrograms = computed(() => programs.value)
    const workshopPrograms = computed(() =>
        programs.value.filter(program => program.type === 'WORKSHOP')
    )
    const trainingPrograms = computed(() =>
        programs.value.filter(program => program.type === 'TRAINING')
    )
    const activePrograms = computed(() =>
        programs.value.filter(program => program.status === 'ACTIVE')
    )
    const completedPrograms = computed(() =>
        programs.value.filter(program => program.status === 'COMPLETED')
    )
    const inactivePrograms = computed(() =>
        programs.value.filter(program => program.status === 'INACTIVE')
    )
    const highCompletionPrograms = computed(() =>
        programs.value.filter(program => program.completion >= 80)
    )
    const mediumCompletionPrograms = computed(() =>
        programs.value.filter(program => program.completion >= 50 && program.completion < 80)
    )
    const lowCompletionPrograms = computed(() =>
        programs.value.filter(program => program.completion < 50)
    )
    const recentPrograms = computed(() =>
        programs.value
            .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
            .slice(0, 10)
    )
    const programsStats = computed(() => {
        const total = programs.value.length
        const active = programs.value.filter(program => program.status === 'ACTIVE').length
        const completed = programs.value.filter(program => program.status === 'COMPLETED').length
        const workshops = programs.value.filter(program => program.type === 'WORKSHOP').length
        const trainings = programs.value.filter(program => program.type === 'TRAINING').length
        const averageCompletion = total > 0
            ? programs.value.reduce((sum, program) => sum + program.completion, 0) / total
            : 0
        return { total, active, completed, workshops, trainings, averageCompletion }
    })

    // Methods that were getters with parameters
    const programsByType = (type) =>
        programs.value.filter(program => program.type === type)

    const programsByStatus = (status) =>
        programs.value.filter(program => program.status === status)

    // Actions as functions
    async function fetchPrograms() {
        loading.value = true
        error.value = null

        try {
            const response = await axios.get('/api/v1/agriculture/programs')
            programs.value = response.data
        } catch (err) {
            error.value = err.response?.data?.message || err.message || 'Failed to fetch programs'
            console.error('Error fetching programs:', err)
        } finally {
            loading.value = false
        }
    }

    async function getProgram(id) {
        loading.value = true
        error.value = null

        try {
            const response = await axios.get(`/api/v1/agriculture/programs/${id}`)
            return {success: true, message: "Program retrieved successfully", data: response.data}
        } catch (err) {
            error.value = err.response?.data?.message || err.message || 'Failed to get program'
            console.error('Error getting program:', err)
            return {success: false, message: err.response?.data?.message || err.message, data: null}
        } finally {
            loading.value = false
        }
    }

    async function getAllPrograms(page = 0, size = 10) {
        loading.value = true
        error.value = null

        try {
            const response = await axios.get('/api/v1/agriculture/programs', {
                params: { page, size }
            })
            programs.value = response.data.content || response.data
            return {success: true, message: "Programs retrieved successfully", data: response.data}
        } catch (err) {
            error.value = err.response?.data?.message || err.message || 'Failed to get all programs'
            console.error('Error getting all programs:', err)
            return {success: false, message: err.response?.data?.message || err.message, data: null}
        } finally {
            loading.value = false
        }
    }

    async function createProgram(programData) {
        loading.value = true
        error.value = null

        try {
            const response = await axios.post('/api/v1/agriculture/programs', programData)
            programs.value.push(response.data)
            return response.data
        } catch (err) {
            error.value = err.response?.data?.message || err.message || 'Failed to create program'
            console.error('Error creating program:', err)
            throw err
        } finally {
            loading.value = false
        }
    }

    async function updateProgram(id, programData) {
        loading.value = true
        error.value = null

        try {
            const response = await axios.put(`/api/v1/agriculture/programs/${id}`, programData)
            const index = programs.value.findIndex(program => program.id === id)
            if (index !== -1) {
                programs.value[index] = response.data
            }
            return response.data
        } catch (err) {
            error.value = err.response?.data?.message || err.message || 'Failed to update program'
            console.error('Error updating program:', err)
            throw err
        } finally {
            loading.value = false
        }
    }

    async function deleteProgram(id) {
        loading.value = true
        error.value = null

        try {
            await axios.delete(`/api/v1/agriculture/programs/${id}`)
            programs.value = programs.value.filter(program => program.id !== id)
            return true
        } catch (err) {
            error.value = err.response?.data?.message || err.message || 'Failed to delete program'
            console.error('Error deleting program:', err)
            throw err
        } finally {
            loading.value = false
        }
    }

    async function addBeneficiaries(programId, beneficiaryIds) {
        loading.value = true
        error.value = null

        try {
            const response = await axios.post(`/api/v1/agriculture/programs/${programId}/beneficiaries`, beneficiaryIds)

            // Update the program in the local array
            const index = programs.value.findIndex(program => program.id === programId)
            if (index !== -1) {
                programs.value[index] = response.data
            }

            return {success: true, message: "Beneficiaries added successfully", data: response.data}
        } catch (err) {
            error.value = err.response?.data?.message || err.message || 'Failed to add beneficiaries'
            console.error('Error adding beneficiaries:', err)
            return {success: false, message: err.response?.data?.message || err.message, data: null}
        } finally {
            loading.value = false
        }
    }

    async function removeBeneficiaries(programId, beneficiaryIds) {
        loading.value = true
        error.value = null

        try {
            const response = await axios.delete(`/api/v1/agriculture/programs/${programId}/beneficiaries`, {
                data: beneficiaryIds
            })

            // Update the program in the local array
            const index = programs.value.findIndex(program => program.id === programId)
            if (index !== -1) {
                programs.value[index] = response.data
            }

            return {success: true, message: "Beneficiaries removed successfully", data: response.data}
        } catch (err) {
            error.value = err.response?.data?.message || err.message || 'Failed to remove beneficiaries'
            console.error('Error removing beneficiaries:', err)
            return {success: false, message: err.response?.data?.message || err.message, data: null}
        } finally {
            loading.value = false
        }
    }

    async function getActiveCount() {
        loading.value = true
        error.value = null

        try {
            const response = await axios.get('/api/v1/agriculture/programs/count/active')
            return {success: true, message: "Active programs count retrieved successfully", data: response.data}
        } catch (err) {
            error.value = err.response?.data?.message || err.message || 'Failed to get active programs count'
            console.error('Error getting active programs count:', err)
            return {success: false, message: err.response?.data?.message || err.message, data: null}
        } finally {
            loading.value = false
        }
    }

    return {
        // State
        programs,
        loading,
        error,

        // Getters
        allPrograms,
        workshopPrograms,
        trainingPrograms,
        activePrograms,
        completedPrograms,
        inactivePrograms,
        highCompletionPrograms,
        mediumCompletionPrograms,
        lowCompletionPrograms,
        recentPrograms,
        programsStats,

        // Methods
        programsByType,
        programsByStatus,

        // Actions
        fetchPrograms,
        getProgram,
        getAllPrograms,
        createProgram,
        updateProgram,
        deleteProgram,
        addBeneficiaries,
        removeBeneficiaries,
        getActiveCount
    }
})
