<script setup>
import AuthenticatedLayout from '@/layouts/AuthenticatedLayout.vue'
import LoadingSpinner from '@/components/others/LoadingSpinner.vue'
import BaseCard from '@/components/cards/BaseCard.vue'
import BaseButton from '@/components/buttons/BaseButton.vue'
import NotificationToast from '@/components/others/NotificationToast.vue'
import { MUNICIPAL_AGRICULTURIST_NAVIGATION } from '@/lib/navigation'
import { useProgramStore } from '@/stores/program.js'
import { useNotificationStore } from '@/stores/notification.js'
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'

const navigation = MUNICIPAL_AGRICULTURIST_NAVIGATION
const programStore = useProgramStore()
const notificationStore = useNotificationStore()
const router = useRouter()

// State
const showFilters = ref(false)
const showCreateModal = ref(false)
const selectedPrograms = ref(new Set())
const searchQuery = ref('')

// Filter states
const filters = ref({
    name: '',
    type: '',
    status: '',
    completion: { min: '', max: '' }
})

// New program form
const newProgram = ref({
    name: '',
    type: 'TRAINING',
    status: 'PENDING',
    completion: 0,
    notes: ''
})

// Computed
const filteredPrograms = computed(() => {
    let filtered = programStore.allPrograms

    // Apply search filter
    if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        filtered = filtered.filter(program =>
            program.name.toLowerCase().includes(query) ||
            (program.notes && program.notes.toLowerCase().includes(query)) ||
            program.status.toLowerCase().includes(query) ||
            (program.type && program.type.toLowerCase().includes(query))
        )
    }

    // Apply filters
    if (filters.value.name) {
        filtered = filtered.filter(p =>
            p.name.toLowerCase().includes(filters.value.name.toLowerCase())
        )
    }

    if (filters.value.type) {
        filtered = filtered.filter(p => p.type === filters.value.type)
    }

    if (filters.value.status) {
        filtered = filtered.filter(p => p.status === filters.value.status)
    }

    if (filters.value.completion.min) {
        filtered = filtered.filter(p => p.completion >= parseFloat(filters.value.completion.min))
    }

    if (filters.value.completion.max) {
        filtered = filtered.filter(p => p.completion <= parseFloat(filters.value.completion.max))
    }

    return filtered
})

const hasActiveFilters = computed(() => {
    return filters.value.name ||
           filters.value.type ||
           filters.value.status ||
           filters.value.completion.min ||
           filters.value.completion.max
})

const selectedProgramsCount = computed(() => selectedPrograms.value.size)
const hasSelectedPrograms = computed(() => selectedProgramsCount.value > 0)

// Methods
const fetchPrograms = async () => {
    await programStore.getAllPrograms()
}

const handleCreateProgram = async () => {
    try {
        if (!newProgram.value.name || !newProgram.value.type || !newProgram.value.status) {
            notificationStore.showError('Please fill in all required fields before creating the program.')
            return
        }

        const programData = {
            ...newProgram.value,
            completion: parseInt(newProgram.value.completion)
        }

        const result = await programStore.createProgram(programData)

        if (result.success) {
            showCreateModal.value = false
            resetForm()
            notificationStore.showSuccess('Program created successfully!')
        } else {
            notificationStore.showError(`Failed to create program: ${result.message}`)
        }
    } catch (error) {
        console.error('Error creating program:', error)
        notificationStore.showError('An unexpected error occurred while creating the program. Please try again.')
    }
}

const clearFilters = () => {
    filters.value = {
        name: '',
        type: '',
        status: '',
        completion: { min: '', max: '' }
    }
}

const toggleSelectAll = () => {
    if (selectedPrograms.value.size === filteredPrograms.value.length) {
        selectedPrograms.value = new Set()
    } else {
        selectedPrograms.value = new Set(filteredPrograms.value.map(p => p.id))
    }
}

const toggleProgramSelection = (programId) => {
    const newSelectedSet = new Set(selectedPrograms.value)
    if (newSelectedSet.has(programId)) {
        newSelectedSet.delete(programId)
    } else {
        newSelectedSet.add(programId)
    }
    selectedPrograms.value = newSelectedSet
}

const formatDate = (dateString) => {
    if (!dateString) return 'N/A'
    try {
        return new Date(dateString).toLocaleDateString('en-PH', {
            year: 'numeric',
            month: 'short',
            day: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        })
    } catch (error) {
        return 'Invalid Date'
    }
}

const getStatusColor = (status) => {
    switch (status?.toUpperCase()) {
        case 'ACTIVE':
            return 'bg-blue-100 text-blue-800'
        case 'COMPLETED':
            return 'bg-green-100 text-green-800'
        case 'PENDING':
            return 'bg-yellow-100 text-yellow-800'
        case 'INACTIVE':
            return 'bg-gray-100 text-gray-800'
        case 'CANCELLED':
            return 'bg-red-100 text-red-800'
        default:
            return 'bg-gray-100 text-gray-800'
    }
}

const getTypeColor = (type) => {
    switch (type?.toUpperCase()) {
        case 'TRAINING':
            return 'bg-purple-100 text-purple-800'
        case 'DISTRIBUTION':
            return 'bg-green-100 text-green-800'
        case 'MONITORING':
            return 'bg-blue-100 text-blue-800'
        case 'CONSULTATION':
            return 'bg-orange-100 text-orange-800'
        default:
            return 'bg-gray-100 text-gray-800'
    }
}

const getCompletionColor = (percentage) => {
    if (percentage >= 80) return 'text-green-600'
    if (percentage >= 50) return 'text-yellow-600'
    return 'text-red-600'
}

const resetForm = () => {
    newProgram.value = {
        name: '',
        type: 'TRAINING',
        status: 'PENDING',
        completion: 0,
        notes: ''
    }
}

const navigateToProgramDetail = (programId) => {
    router.push({
        name: 'agriculturist-monitor-programs-detail',
        params: { id: programId }
    })
}

// Lifecycle
onMounted(async () => {
    await fetchPrograms()
})
</script>

<template>
    <AuthenticatedLayout
        :navigation="navigation"
        role-title="Municipal Agriculturist"
        page-title="Monitor Programs">

        <div class="flex flex-col h-full space-y-4">
            <!-- Header -->
            <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                <div>
                    <h1 class="text-2xl font-bold text-gray-900">Program Monitoring</h1>
                    <p class="text-sm text-gray-600">
                        {{ filteredPrograms.length }} program{{ filteredPrograms.length !== 1 ? 's' : '' }}
                        {{ searchQuery || hasActiveFilters ? '(filtered)' : '' }}
                    </p>
                </div>
                <BaseButton
                    class="bg-green-600 hover:bg-green-700"
                    @click="showCreateModal = true">
                    <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
                    </svg>
                    Create Program
                </BaseButton>
            </div>

            <!-- Loading State -->
            <div v-if="programStore.isLoading" class="flex justify-center items-center flex-1">
                <LoadingSpinner />
            </div>

            <!-- Error State -->
            <BaseCard v-else-if="programStore.hasError" class="p-8">
                <div class="flex items-center justify-center">
                    <div class="text-center">
                        <svg class="w-12 h-12 text-red-500 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.732-.833-2.5 0L4.268 6.5c-.77.833-.192 2.5 1.732 2.5z"></path>
                        </svg>
                        <h3 class="text-lg font-medium text-gray-900 mb-2">Error Loading Programs</h3>
                        <p class="text-gray-600 mb-4">{{ programStore.errorMessage }}</p>
                        <BaseButton class="bg-green-600 hover:bg-green-700" @click="fetchPrograms">
                            Try Again
                        </BaseButton>
                    </div>
                </div>
            </BaseCard>

            <!-- Programs Table -->
            <div v-else class="bg-white rounded-lg border border-gray-200 shadow-sm flex flex-col flex-1 min-h-0 overflow-hidden">
                <!-- Table Header with Search and Filters -->
                <div class="p-4 border-b border-gray-200 space-y-4 flex-shrink-0">
                    <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                        <div class="flex items-center gap-3 flex-1 max-w-lg">
                            <!-- Search Input -->
                            <div class="relative flex-1">
                                <svg class="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
                                </svg>
                                <input
                                    v-model="searchQuery"
                                    type="text"
                                    placeholder="Search programs..."
                                    class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent text-sm" />
                            </div>

                            <!-- Filter Toggle Button -->
                            <BaseButton
                                variant="secondary"
                                :class="{ 'bg-green-50 border-green-200 text-green-700': showFilters || hasActiveFilters }"
                                @click="showFilters = !showFilters">
                                <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.207A1 1 0 013 6.5V4z"></path>
                                </svg>
                                Filters
                                <span v-if="hasActiveFilters" class="ml-1 px-2 py-0.5 bg-green-100 text-green-800 text-xs rounded-full">
                                    Active
                                </span>
                            </BaseButton>
                        </div>
                    </div>

                    <!-- Filter Panel -->
                    <div v-if="showFilters" class="p-4 bg-gray-50 rounded-lg border border-gray-200">
                        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                            <div>
                                <label class="block text-xs font-medium text-gray-700 mb-1">Program Name</label>
                                <input
                                    v-model="filters.name"
                                    type="text"
                                    placeholder="Filter by name..."
                                    class="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:ring-1 focus:ring-green-500 focus:border-transparent" />
                            </div>

                            <div>
                                <label class="block text-xs font-medium text-gray-700 mb-1">Type</label>
                                <select v-model="filters.type" class="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:ring-1 focus:ring-green-500 focus:border-transparent">
                                    <option value="">All Types</option>
                                    <option value="TRAINING">Training</option>
                                    <option value="DISTRIBUTION">Distribution</option>
                                    <option value="MONITORING">Monitoring</option>
                                    <option value="CONSULTATION">Consultation</option>
                                </select>
                            </div>

                            <div>
                                <label class="block text-xs font-medium text-gray-700 mb-1">Status</label>
                                <select v-model="filters.status" class="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:ring-1 focus:ring-green-500 focus:border-transparent">
                                    <option value="">All Status</option>
                                    <option value="PENDING">Pending</option>
                                    <option value="ACTIVE">Active</option>
                                    <option value="COMPLETED">Completed</option>
                                    <option value="INACTIVE">Inactive</option>
                                    <option value="CANCELLED">Cancelled</option>
                                </select>
                            </div>

                            <div>
                                <label class="block text-xs font-medium text-gray-700 mb-1">Completion %</label>
                                <div class="flex gap-2">
                                    <input
                                        v-model="filters.completion.min"
                                        type="number"
                                        placeholder="Min %"
                                        min="0"
                                        max="100"
                                        class="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:ring-1 focus:ring-green-500 focus:border-transparent" />
                                    <input
                                        v-model="filters.completion.max"
                                        type="number"
                                        placeholder="Max %"
                                        min="0"
                                        max="100"
                                        class="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:ring-1 focus:ring-green-500 focus:border-transparent" />
                                </div>
                            </div>
                        </div>

                        <div class="mt-4 flex justify-end gap-2">
                            <BaseButton variant="secondary" @click="clearFilters">Clear Filters</BaseButton>
                        </div>
                    </div>
                </div>

                <!-- Desktop Table -->
                <div class="hidden md:flex flex-col flex-1 min-h-0">
                    <div class="flex-1 overflow-y-auto">
                        <table class="min-w-full divide-y divide-gray-200">
                            <thead class="bg-gray-50 sticky top-0 z-10">
                                <tr>
                                    <th scope="col" class="w-12 px-4 py-3">
                                        <input
                                            type="checkbox"
                                            :checked="selectedPrograms.size === filteredPrograms.length && filteredPrograms.length > 0"
                                            :indeterminate="selectedPrograms.size > 0 && selectedPrograms.size < filteredPrograms.length"
                                            class="rounded border-gray-300 text-green-600 focus:ring-green-500"
                                            @change="toggleSelectAll" />
                                    </th>
                                    <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Program Name</th>
                                    <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Type</th>
                                    <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                                    <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Completion</th>
                                    <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Created At</th>
                                    <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Updated At</th>
                                </tr>
                            </thead>
                            <tbody class="bg-white divide-y divide-gray-200">
                                <tr v-for="program in filteredPrograms" :key="program.id" class="hover:bg-gray-50 cursor-pointer transition-colors" @click="navigateToProgramDetail(program.id)">
                                    <td class="px-4 py-4 whitespace-nowrap" @click.stop>
                                        <input
                                            type="checkbox"
                                            :checked="selectedPrograms.has(program.id)"
                                            class="rounded border-gray-300 text-green-600 focus:ring-green-500"
                                            @change="toggleProgramSelection(program.id)" />
                                    </td>
                                    <td class="px-4 py-4 text-sm text-gray-900 max-w-xs">
                                        <div class="font-medium truncate" :title="program.name">{{ program.name }}</div>
                                        <div class="text-xs text-gray-500 truncate" :title="program.notes">{{ program.notes || 'No notes' }}</div>
                                    </td>
                                    <td class="px-4 py-4 whitespace-nowrap">
                                        <span :class="getTypeColor(program.type)" class="px-2 py-1 text-xs font-medium rounded-full capitalize">
                                            {{ program.type }}
                                        </span>
                                    </td>
                                    <td class="px-4 py-4 whitespace-nowrap">
                                        <span :class="getStatusColor(program.status)" class="px-2 py-1 text-xs font-medium rounded-full capitalize">
                                            {{ program.status }}
                                        </span>
                                    </td>
                                    <td class="px-4 py-4 whitespace-nowrap">
                                        <div class="flex items-center">
                                            <div class="w-16 bg-gray-200 rounded-full h-2 mr-2">
                                                <div class="bg-green-600 h-2 rounded-full" :style="{ width: `${program.completion}%` }"></div>
                                            </div>
                                            <span :class="getCompletionColor(program.completion)" class="text-sm font-medium">
                                                {{ program.completion }}%
                                            </span>
                                        </div>
                                    </td>
                                    <td class="px-4 py-4 whitespace-nowrap text-sm text-gray-900">{{ formatDate(program.createdAt) }}</td>
                                    <td class="px-4 py-4 whitespace-nowrap text-sm text-gray-900">{{ formatDate(program.updatedAt) }}</td>
                                </tr>
                                <tr v-if="filteredPrograms.length === 0">
                                    <td colspan="7" class="px-4 py-8 text-center text-gray-500">
                                        No programs found
                                    </td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                </div>

                <!-- Mobile Cards -->
                <div class="md:hidden flex flex-col flex-1 min-h-0">
                    <div class="flex-1 overflow-y-auto">
                        <div class="divide-y divide-gray-200">
                            <div v-for="program in filteredPrograms" :key="program.id" class="p-4 cursor-pointer hover:bg-gray-50 transition-colors" @click="navigateToProgramDetail(program.id)">
                                <div class="flex items-start justify-between mb-3">
                                    <div class="flex items-start gap-3 flex-1 min-w-0">
                                        <div @click.stop>
                                            <input
                                                type="checkbox"
                                                :checked="selectedPrograms.has(program.id)"
                                                class="mt-1 rounded border-gray-300 text-green-600 focus:ring-green-500 flex-shrink-0"
                                                @change="toggleProgramSelection(program.id)" />
                                        </div>
                                        <div class="flex-1 min-w-0">
                                            <p class="text-sm font-medium text-gray-900 break-words">{{ program.name }}</p>
                                            <p class="text-xs text-gray-600 mt-1 break-words">{{ program.notes || 'No notes' }}</p>
                                            <div class="flex items-center mt-2 gap-2 flex-wrap">
                                                <span :class="getTypeColor(program.type)" class="px-2 py-1 text-xs font-medium rounded-full capitalize">
                                                    {{ program.type }}
                                                </span>
                                                <span :class="getStatusColor(program.status)" class="px-2 py-1 text-xs font-medium rounded-full capitalize">
                                                    {{ program.status }}
                                                </span>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <div class="ml-8 space-y-2">
                                    <div class="flex justify-between text-sm">
                                        <span class="text-gray-600">Completion:</span>
                                        <div class="flex items-center">
                                            <div class="w-12 bg-gray-200 rounded-full h-2 mr-2">
                                                <div class="bg-green-600 h-2 rounded-full" :style="{ width: `${program.completion}%` }"></div>
                                            </div>
                                            <span :class="getCompletionColor(program.completion)" class="font-medium">
                                                {{ program.completion }}%
                                            </span>
                                        </div>
                                    </div>
                                    <div class="flex justify-between text-sm">
                                        <span class="text-gray-600">Created:</span>
                                        <span class="font-medium">{{ formatDate(program.createdAt) }}</span>
                                    </div>
                                    <div class="flex justify-between text-sm">
                                        <span class="text-gray-600">Updated:</span>
                                        <span class="font-medium">{{ formatDate(program.updatedAt) }}</span>
                                    </div>
                                </div>
                            </div>
                            <div v-if="filteredPrograms.length === 0" class="p-8 text-center text-gray-500">
                                No programs found
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Create Program Modal -->
        <div v-if="showCreateModal" class="fixed inset-0 bg-gray-500 bg-opacity-75 flex items-center justify-center z-50 p-4">
            <div class="bg-white rounded-lg shadow-xl max-w-4xl w-full max-h-[90vh] overflow-y-auto">
                <!-- Modal Header -->
                <div class="px-6 py-4 border-b border-gray-200 flex justify-between items-center">
                    <h3 class="text-lg font-medium text-gray-900">Create New Program</h3>
                    <button
                        @click="showCreateModal = false"
                        class="text-gray-400 hover:text-gray-600 transition-colors">
                        <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                        </svg>
                    </button>
                </div>

                <!-- Modal Body -->
                <div class="px-6 py-6">
                    <form @submit.prevent="handleCreateProgram" class="space-y-6">
                        <!-- Row 1: Program Name and Type -->
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <div>
                                <label for="programName" class="block text-sm font-medium text-gray-700 mb-2">
                                    Program Name <span class="text-red-500">*</span>
                                </label>
                                <input
                                    id="programName"
                                    v-model="newProgram.name"
                                    type="text"
                                    required
                                    class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-green-500 focus:border-transparent"
                                    placeholder="Enter program name" />
                            </div>

                            <div>
                                <label for="type" class="block text-sm font-medium text-gray-700 mb-2">
                                    Program Type <span class="text-red-500">*</span>
                                </label>
                                <select
                                    id="type"
                                    v-model="newProgram.type"
                                    required
                                    class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-green-500 focus:border-transparent">
                                    <option value="TRAINING">Training</option>
                                    <option value="DISTRIBUTION">Distribution</option>
                                    <option value="MONITORING">Monitoring</option>
                                    <option value="CONSULTATION">Consultation</option>
                                </select>
                            </div>
                        </div>

                        <!-- Row 2: Status and Completion Percentage -->
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <div>
                                <label for="status" class="block text-sm font-medium text-gray-700 mb-2">
                                    Status <span class="text-red-500">*</span>
                                </label>
                                <select
                                    id="status"
                                    v-model="newProgram.status"
                                    required
                                    class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-green-500 focus:border-transparent">
                                    <option value="PENDING">Pending</option>
                                    <option value="ACTIVE">Active</option>
                                    <option value="COMPLETED">Completed</option>
                                    <option value="INACTIVE">Inactive</option>
                                    <option value="CANCELLED">Cancelled</option>
                                </select>
                            </div>

                            <div>
                                <label for="completion" class="block text-sm font-medium text-gray-700 mb-2">
                                    Completion Percentage
                                </label>
                                <input
                                    id="completion"
                                    v-model="newProgram.completion"
                                    type="number"
                                    min="0"
                                    max="100"
                                    class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-green-500 focus:border-transparent"
                                    placeholder="0" />
                            </div>
                        </div>

                        <!-- Row 3: Notes -->
                        <div>
                            <label for="notes" class="block text-sm font-medium text-gray-700 mb-2">
                                Notes
                            </label>
                            <textarea
                                id="notes"
                                v-model="newProgram.notes"
                                rows="4"
                                class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-2 focus:ring-green-500 focus:border-transparent"
                                placeholder="Enter program notes (optional)"></textarea>
                        </div>
                    </form>
                </div>

                <!-- Modal Footer -->
                <div class="px-6 py-4 border-t border-gray-200 flex justify-end space-x-3">
                    <BaseButton
                        variant="secondary"
                        @click="showCreateModal = false">
                        Cancel
                    </BaseButton>
                    <BaseButton
                        class="bg-green-600 hover:bg-green-700"
                        @click="handleCreateProgram"
                        :disabled="programStore.isLoading">
                        <svg v-if="programStore.isLoading" class="animate-spin -ml-1 mr-3 h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                        </svg>
                        {{ programStore.isLoading ? 'Creating...' : 'Create Program' }}
                    </BaseButton>
                </div>
            </div>
        </div>

        <!-- Notification Toast -->
        <NotificationToast />

    </AuthenticatedLayout>
</template>

<style scoped>
/* Custom scrollbar styling */
.overflow-y-auto::-webkit-scrollbar {
    width: 8px;
}

.overflow-y-auto::-webkit-scrollbar-track {
    background: #f1f5f9;
    border-radius: 4px;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
    background: #cbd5e1;
    border-radius: 4px;
}

.overflow-y-auto::-webkit-scrollbar-thumb:hover {
    background: #94a3b8;
}

/* Better text handling */
.truncate {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
}

.break-words {
    word-wrap: break-word;
    word-break: break-word;
}

/* Ensure minimum width for mobile layout */
.min-w-0 {
    min-width: 0;
}
</style>
