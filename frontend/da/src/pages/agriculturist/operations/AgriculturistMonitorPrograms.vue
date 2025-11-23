<script setup>
import AuthenticatedLayout from '@/layouts/AuthenticatedLayout.vue'
import LoadingSpinner from '@/components/others/LoadingSpinner.vue'
import BaseCard from '@/components/cards/BaseCard.vue'
import BaseButton from '@/components/buttons/BaseButton.vue'
import NotificationToast from '@/components/others/NotificationToast.vue'
import { MUNICIPAL_AGRICULTURIST_NAVIGATION } from '@/lib/navigation'
import { useProgramStore } from '@/stores/program.js'
import { useNotificationStore } from '@/stores/notification.js'
import { ref, onMounted, computed, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { X } from 'lucide-vue-next'

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

const closeModal = () => {
    showCreateModal.value = false
    closeProgramTypeDropdown()
    closeProgramStatusDropdown()
    resetForm()
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
            closeModal()
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

// Filter dropdown options
const filterTypeOptions = [
    { label: 'All Types', value: '' },
    { label: 'Training', value: 'TRAINING' },
    { label: 'Workshop', value: 'WORKSHOP' },
    { label: 'Research', value: 'RESEARCH' },
    { label: 'Distribution', value: 'DISTRIBUTION' },
    { label: 'Monitoring', value: 'MONITORING' },
    { label: 'Consultation', value: 'CONSULTATION' }
]

const filterStatusOptions = [
    { label: 'All Status', value: '' },
    { label: 'Pending', value: 'PENDING' },
    { label: 'Active', value: 'ACTIVE' },
    { label: 'Completed', value: 'COMPLETED' },
    { label: 'Inactive', value: 'INACTIVE' },
    { label: 'Cancelled', value: 'CANCELLED' }
]

const programTypeOptions = [
    { label: 'Training', value: 'TRAINING' },
    { label: 'Workshop', value: 'WORKSHOP' },
    { label: 'Research', value: 'RESEARCH' },
    { label: 'Distribution', value: 'DISTRIBUTION' },
    { label: 'Monitoring', value: 'MONITORING' },
    { label: 'Consultation', value: 'CONSULTATION' }
]

const programStatusOptions = [
    { label: 'Pending', value: 'PENDING' },
    { label: 'Active', value: 'ACTIVE' },
    { label: 'Completed', value: 'COMPLETED' },
    { label: 'Inactive', value: 'INACTIVE' },
    { label: 'Cancelled', value: 'CANCELLED' }
]

// Filter Type dropdown state
const isFilterTypeDropdownOpen = ref(false)
const highlightedFilterTypeIndex = ref(-1)

const toggleFilterTypeDropdown = () => {
    isFilterTypeDropdownOpen.value = !isFilterTypeDropdownOpen.value
    if (isFilterTypeDropdownOpen.value) {
        highlightedFilterTypeIndex.value = filterTypeOptions.findIndex(opt => opt.value === filters.value.type)
    }
}

const closeFilterTypeDropdown = () => {
    isFilterTypeDropdownOpen.value = false
    highlightedFilterTypeIndex.value = -1
}

const selectFilterType = (value) => {
    filters.value.type = value
    closeFilterTypeDropdown()
}

const onFilterTypeKeyDown = (e) => {
    if (!isFilterTypeDropdownOpen.value && (e.key === 'Enter' || e.key === ' ')) {
        e.preventDefault()
        toggleFilterTypeDropdown()
        return
    }
    if (isFilterTypeDropdownOpen.value) {
        if (e.key === 'ArrowDown') {
            e.preventDefault()
            highlightedFilterTypeIndex.value = Math.min(highlightedFilterTypeIndex.value + 1, filterTypeOptions.length - 1)
        } else if (e.key === 'ArrowUp') {
            e.preventDefault()
            highlightedFilterTypeIndex.value = Math.max(highlightedFilterTypeIndex.value - 1, 0)
        } else if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault()
            if (highlightedFilterTypeIndex.value >= 0) selectFilterType(filterTypeOptions[highlightedFilterTypeIndex.value].value)
        } else if (e.key === 'Escape') {
            e.preventDefault()
            closeFilterTypeDropdown()
        }
    }
}

const selectedFilterTypeDisplay = computed(() => {
    const option = filterTypeOptions.find(opt => opt.value === filters.value.type)
    return option ? option.label : 'All Types'
})

// Filter Status dropdown state
const isFilterStatusDropdownOpen = ref(false)
const highlightedFilterStatusIndex = ref(-1)

const toggleFilterStatusDropdown = () => {
    isFilterStatusDropdownOpen.value = !isFilterStatusDropdownOpen.value
    if (isFilterStatusDropdownOpen.value) {
        highlightedFilterStatusIndex.value = filterStatusOptions.findIndex(opt => opt.value === filters.value.status)
    }
}

const closeFilterStatusDropdown = () => {
    isFilterStatusDropdownOpen.value = false
    highlightedFilterStatusIndex.value = -1
}

const selectFilterStatus = (value) => {
    filters.value.status = value
    closeFilterStatusDropdown()
}

const onFilterStatusKeyDown = (e) => {
    if (!isFilterStatusDropdownOpen.value && (e.key === 'Enter' || e.key === ' ')) {
        e.preventDefault()
        toggleFilterStatusDropdown()
        return
    }
    if (isFilterStatusDropdownOpen.value) {
        if (e.key === 'ArrowDown') {
            e.preventDefault()
            highlightedFilterStatusIndex.value = Math.min(highlightedFilterStatusIndex.value + 1, filterStatusOptions.length - 1)
        } else if (e.key === 'ArrowUp') {
            e.preventDefault()
            highlightedFilterStatusIndex.value = Math.max(highlightedFilterStatusIndex.value - 1, 0)
        } else if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault()
            if (highlightedFilterStatusIndex.value >= 0) selectFilterStatus(filterStatusOptions[highlightedFilterStatusIndex.value].value)
        } else if (e.key === 'Escape') {
            e.preventDefault()
            closeFilterStatusDropdown()
        }
    }
}

const selectedFilterStatusDisplay = computed(() => {
    const option = filterStatusOptions.find(opt => opt.value === filters.value.status)
    return option ? option.label : 'All Status'
})

// Program Type dropdown state (in create modal)
const isProgramTypeDropdownOpen = ref(false)
const highlightedProgramTypeIndex = ref(-1)

const toggleProgramTypeDropdown = () => {
    isProgramTypeDropdownOpen.value = !isProgramTypeDropdownOpen.value
    if (isProgramTypeDropdownOpen.value) {
        highlightedProgramTypeIndex.value = programTypeOptions.findIndex(opt => opt.value === newProgram.value.type)
    }
}

const closeProgramTypeDropdown = () => {
    isProgramTypeDropdownOpen.value = false
    highlightedProgramTypeIndex.value = -1
}

const selectProgramType = (value) => {
    newProgram.value.type = value
    closeProgramTypeDropdown()
}

const onProgramTypeKeyDown = (e) => {
    if (!isProgramTypeDropdownOpen.value && (e.key === 'Enter' || e.key === ' ')) {
        e.preventDefault()
        toggleProgramTypeDropdown()
        return
    }
    if (isProgramTypeDropdownOpen.value) {
        if (e.key === 'ArrowDown') {
            e.preventDefault()
            highlightedProgramTypeIndex.value = Math.min(highlightedProgramTypeIndex.value + 1, programTypeOptions.length - 1)
        } else if (e.key === 'ArrowUp') {
            e.preventDefault()
            highlightedProgramTypeIndex.value = Math.max(highlightedProgramTypeIndex.value - 1, 0)
        } else if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault()
            if (highlightedProgramTypeIndex.value >= 0) selectProgramType(programTypeOptions[highlightedProgramTypeIndex.value].value)
        } else if (e.key === 'Escape') {
            e.preventDefault()
            closeProgramTypeDropdown()
        }
    }
}

const selectedProgramTypeDisplay = computed(() => {
    const option = programTypeOptions.find(opt => opt.value === newProgram.value.type)
    return option ? option.label : 'Training'
})

// Program Status dropdown state (in create modal)
const isProgramStatusDropdownOpen = ref(false)
const highlightedProgramStatusIndex = ref(-1)

const toggleProgramStatusDropdown = () => {
    isProgramStatusDropdownOpen.value = !isProgramStatusDropdownOpen.value
    if (isProgramStatusDropdownOpen.value) {
        highlightedProgramStatusIndex.value = programStatusOptions.findIndex(opt => opt.value === newProgram.value.status)
    }
}

const closeProgramStatusDropdown = () => {
    isProgramStatusDropdownOpen.value = false
    highlightedProgramStatusIndex.value = -1
}

const selectProgramStatus = (value) => {
    newProgram.value.status = value
    closeProgramStatusDropdown()
}

const onProgramStatusKeyDown = (e) => {
    if (!isProgramStatusDropdownOpen.value && (e.key === 'Enter' || e.key === ' ')) {
        e.preventDefault()
        toggleProgramStatusDropdown()
        return
    }
    if (isProgramStatusDropdownOpen.value) {
        if (e.key === 'ArrowDown') {
            e.preventDefault()
            highlightedProgramStatusIndex.value = Math.min(highlightedProgramStatusIndex.value + 1, programStatusOptions.length - 1)
        } else if (e.key === 'ArrowUp') {
            e.preventDefault()
            highlightedProgramStatusIndex.value = Math.max(highlightedProgramStatusIndex.value - 1, 0)
        } else if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault()
            if (highlightedProgramStatusIndex.value >= 0) selectProgramStatus(programStatusOptions[highlightedProgramStatusIndex.value].value)
        } else if (e.key === 'Escape') {
            e.preventDefault()
            closeProgramStatusDropdown()
        }
    }
}

const selectedProgramStatusDisplay = computed(() => {
    const option = programStatusOptions.find(opt => opt.value === newProgram.value.status)
    return option ? option.label : 'Pending'
})

// Close on outside click
const onClickOutside = (e) => {
    const filterTypeEl = document.querySelector('#filter-type-dropdown')
    const filterStatusEl = document.querySelector('#filter-status-dropdown')
    const programTypeEl = document.querySelector('#program-type-dropdown')
    const programStatusEl = document.querySelector('#program-status-dropdown')
    
    if (filterTypeEl && !filterTypeEl.contains(e.target)) closeFilterTypeDropdown()
    if (filterStatusEl && !filterStatusEl.contains(e.target)) closeFilterStatusDropdown()
    if (programTypeEl && !programTypeEl.contains(e.target)) closeProgramTypeDropdown()
    if (programStatusEl && !programStatusEl.contains(e.target)) closeProgramStatusDropdown()
}

// Lifecycle
onMounted(async () => {
    await fetchPrograms()
    document.addEventListener('click', onClickOutside)
})

onBeforeUnmount(() => {
    document.removeEventListener('click', onClickOutside)
})
</script>

<template>
    <AuthenticatedLayout
        :navigation="navigation"
        role-title="Municipal Agriculturist"
        page-title="Monitor Programs">

        <div class="flex flex-col h-full space-y-4">
            <!-- Header -->
            <div class="mb-3 mt-4 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 ml-4">
                <div>
                    <h1 class="text-3xl font-bold text-green-600">Program Monitoring</h1>
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
            <div
                v-if="programStore.isLoading"
                class="flex flex-col items-center justify-center flex-1 space-y-4 min-h-[60vh]"
            >
                <!-- Spinner -->
                <div class="relative">
                    <div
                        class="h-14 w-14 rounded-full border-4 border-gray-200"></div>
                    <div
                        class="absolute top-0 left-0 h-14 w-14 rounded-full border-4 border-green-600 border-t-transparent animate-spin"></div>
                </div>

                <!-- Loading Label -->
                <p class="text-gray-600 font-medium tracking-wide">
                    Loading data…
                </p>
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
            <div v-else class="bg-gray-100 rounded-xl border border-gray-300 shadow-sm flex flex-col flex-1 min-h-0 overflow-hidden">
                <!-- Table Header with Search and Filters -->
                <div class="p-4 border-b border-gray-300 space-y-4 flex-shrink-0">
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
                                    class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-xl focus:ring-1 focus:ring-green-500 focus:border-transparent text-sm" />
                            </div>

<!-- Filter Toggle Button -->
<BaseButton
  class="bg-green-600 border-green-600 text-white hover:bg-green-700 hover:border-green-700"
  @click="showFilters = !showFilters"
>
  <svg
    class="w-4 h-4 mr-2"
    fill="none"
    stroke="currentColor"
    viewBox="0 0 24 24"
  >
    <path
      stroke-linecap="round"
      stroke-linejoin="round"
      stroke-width="2"
      d="M3 4a1 1 0 011-1h16a1 1 0 011 1v2.586a1 1 0 01-.293.707l-6.414 6.414a1 1 0 00-.293.707V17l-4 4v-6.586a1 1 0 00-.293-.707L3.293 7.207A1 1 0 013 6.5V4z"
    ></path>
  </svg>

  Filters

  <span
    v-if="hasActiveFilters"
    class="ml-1 px-2 py-0.5 bg-white/20 text-white text-xs rounded-full"
  >
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
                                <div id="filter-type-dropdown" class="relative">
                                    <button
                                        type="button"
                                        @click="toggleFilterTypeDropdown"
                                        @keydown.stop.prevent="onFilterTypeKeyDown"
                                        :aria-expanded="isFilterTypeDropdownOpen"
                                        aria-haspopup="listbox"
                                        class="w-full flex items-center justify-between px-3 py-2 text-sm border border-gray-300 rounded-xl bg-white shadow-sm focus:ring-1 focus:ring-green-500 focus:border-transparent"
                                    >
                                        <span class="text-gray-900">{{ selectedFilterTypeDisplay }}</span>
                                        <svg
                                            class="w-4 h-4 text-green-600 transform transition-transform duration-200"
                                            :class="isFilterTypeDropdownOpen ? 'rotate-180' : ''"
                                            viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg"
                                            aria-hidden="true"
                                        >
                                            <path d="M6 8l4 4 4-4" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/>
                                        </svg>
                                    </button>
                                    <ul
                                        v-show="isFilterTypeDropdownOpen"
                                        role="listbox"
                                        tabindex="-1"
                                        class="origin-top-right absolute right-0 left-0 mt-2 bg-white rounded-xl shadow-lg ring-1 ring-black ring-opacity-5 overflow-auto max-h-56 py-1 focus:outline-none z-50 transition-transform duration-150"
                                    >
                                        <li
                                            v-for="(option, idx) in filterTypeOptions"
                                            :key="option.value"
                                            role="option"
                                            :aria-selected="option.value === filters.type"
                                            @mouseenter="highlightedFilterTypeIndex = idx"
                                            @mouseleave="highlightedFilterTypeIndex = -1"
                                            @click="selectFilterType(option.value)"
                                            :class=" [
                                                'px-3 py-2 cursor-pointer flex items-center justify-between text-sm',
                                                highlightedFilterTypeIndex === idx ? 'bg-green-50' : 'hover:bg-green-50',
                                                option.value === filters.type ? 'font-semibold text-green-700' : 'text-gray-700'
                                            ]"
                                        >
                                            <span>{{ option.label }}</span>
                                            <svg v-if="option.value === filters.type" class="w-4 h-4 text-green-600" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                <path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                            </svg>
                                        </li>
                                    </ul>
                                </div>
                            </div>

                            <div>
                                <label class="block text-xs font-medium text-gray-700 mb-1">Status</label>
                                <div id="filter-status-dropdown" class="relative">
                                    <button
                                        type="button"
                                        @click="toggleFilterStatusDropdown"
                                        @keydown.stop.prevent="onFilterStatusKeyDown"
                                        :aria-expanded="isFilterStatusDropdownOpen"
                                        aria-haspopup="listbox"
                                        class="w-full flex items-center justify-between px-3 py-2 text-sm border border-gray-300 rounded-xl bg-white shadow-sm focus:ring-1 focus:ring-green-500 focus:border-transparent"
                                    >
                                        <span class="text-gray-900">{{ selectedFilterStatusDisplay }}</span>
                                        <svg
                                            class="w-4 h-4 text-green-600 transform transition-transform duration-200"
                                            :class="isFilterStatusDropdownOpen ? 'rotate-180' : ''"
                                            viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg"
                                            aria-hidden="true"
                                        >
                                            <path d="M6 8l4 4 4-4" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/>
                                        </svg>
                                    </button>
                                    <ul
                                        v-show="isFilterStatusDropdownOpen"
                                        role="listbox"
                                        tabindex="-1"
                                        class="origin-top-right absolute right-0 left-0 mt-2 bg-white rounded-xl shadow-lg ring-1 ring-black ring-opacity-5 overflow-auto max-h-56 py-1 focus:outline-none z-50 transition-transform duration-150"
                                    >
                                        <li
                                            v-for="(option, idx) in filterStatusOptions"
                                            :key="option.value"
                                            role="option"
                                            :aria-selected="option.value === filters.status"
                                            @mouseenter="highlightedFilterStatusIndex = idx"
                                            @mouseleave="highlightedFilterStatusIndex = -1"
                                            @click="selectFilterStatus(option.value)"
                                            :class=" [
                                                'px-3 py-2 cursor-pointer flex items-center justify-between text-sm',
                                                highlightedFilterStatusIndex === idx ? 'bg-green-50' : 'hover:bg-green-50',
                                                option.value === filters.status ? 'font-semibold text-green-700' : 'text-gray-700'
                                            ]"
                                        >
                                            <span>{{ option.label }}</span>
                                            <svg v-if="option.value === filters.status" class="w-4 h-4 text-green-600" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                <path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                            </svg>
                                        </li>
                                    </ul>
                                </div>
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
                                </tr>
                            </thead>
                            <tbody class="bg-white divide-y divide-gray-300">
                                <tr v-for="program in filteredPrograms" :key="program.id" class="hover:bg-green-50 cursor-pointer transition-colors" @click="navigateToProgramDetail(program.id)">
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
        <Transition name="modal">
            <div v-if="showCreateModal" class="fixed inset-0 z-50">
                <!-- Backdrop -->
                <Transition name="backdrop">
                    <div
                        v-if="showCreateModal"
                        class="absolute inset-0 bg-gray-500 bg-opacity-75 transition-opacity"
                        @click="closeModal">
                    </div>
                </Transition>

                <!-- Slide Panel -->
                <Transition name="slide">
                    <div
                        v-if="showCreateModal"
                        class="absolute right-0 top-0 h-full w-5/12 bg-white shadow-2xl flex flex-col">
                        <!-- Modal Header -->
                        <div class="px-6 py-4 border-b border-gray-200 flex justify-between items-center flex-shrink-0">
                            <h3 class="text-lg font-medium text-gray-900">Create New Program</h3>
                            <button
                                @click="closeModal"
                                class="text-gray-400 hover:text-gray-600 transition-colors">
                                <X class="h-5 w-5" />
                            </button>
                        </div>

                        <!-- Modal Body -->
                        <div class="flex-1 overflow-y-auto px-6 py-6">
                            <form @submit.prevent="handleCreateProgram" class="space-y-6">
                                <!-- Program Name -->
                                <div>
                                    <label for="programName" class="block text-sm font-medium text-gray-700 mb-2">
                                        Program Name <span class="text-red-500">*</span>
                                    </label>
                                    <input
                                        id="programName"
                                        v-model="newProgram.name"
                                        type="text"
                                        required
                                        class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-1 focus:ring-green-500 focus:border-transparent"
                                        placeholder="Enter program name" />
                                </div>

                                <!-- Program Type -->
                                <div>
                                    <label for="type" class="block text-sm font-medium text-gray-700 mb-2">
                                        Program Type <span class="text-red-500">*</span>
                                    </label>
                                    <div id="program-type-dropdown" class="relative">
                                        <button
                                            type="button"
                                            @click="toggleProgramTypeDropdown"
                                            @keydown.stop.prevent="onProgramTypeKeyDown"
                                            :aria-expanded="isProgramTypeDropdownOpen"
                                            aria-haspopup="listbox"
                                            class="w-full flex items-center justify-between px-3 py-2.5 text-sm border border-gray-300 rounded-xl bg-white shadow-sm focus:ring-1 focus:ring-green-500 focus:border-transparent"
                                        >
                                            <span class="text-gray-900">{{ selectedProgramTypeDisplay }}</span>
                                            <svg
                                                class="w-4 h-4 text-green-600 transform transition-transform duration-200"
                                                :class="isProgramTypeDropdownOpen ? 'rotate-180' : ''"
                                                viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg"
                                                aria-hidden="true"
                                            >
                                                <path d="M6 8l4 4 4-4" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/>
                                            </svg>
                                        </button>
                                        <ul
                                            v-show="isProgramTypeDropdownOpen"
                                            role="listbox"
                                            tabindex="-1"
                                            class="origin-top-right absolute right-0 left-0 mt-2 bg-white rounded-xl shadow-lg ring-1 ring-black ring-opacity-5 overflow-auto max-h-56 py-1 focus:outline-none z-50 transition-transform duration-150"
                                        >
                                            <li
                                                v-for="(option, idx) in programTypeOptions"
                                                :key="option.value"
                                                role="option"
                                                :aria-selected="option.value === newProgram.type"
                                                @mouseenter="highlightedProgramTypeIndex = idx"
                                                @mouseleave="highlightedProgramTypeIndex = -1"
                                                @click="selectProgramType(option.value)"
                                                :class=" [
                                                    'px-3 py-2 cursor-pointer flex items-center justify-between text-sm',
                                                    highlightedProgramTypeIndex === idx ? 'bg-green-50' : 'hover:bg-green-50',
                                                    option.value === newProgram.type ? 'font-semibold text-green-700' : 'text-gray-700'
                                                ]"
                                            >
                                                <span>{{ option.label }}</span>
                                                <svg v-if="option.value === newProgram.type" class="w-4 h-4 text-green-600" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                    <path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                                </svg>
                                            </li>
                                        </ul>
                                    </div>
                                </div>

                                <!-- Status -->
                                <div>
                                    <label for="status" class="block text-sm font-medium text-gray-700 mb-2">
                                        Status <span class="text-red-500">*</span>
                                    </label>
                                    <div id="program-status-dropdown" class="relative">
                                        <button
                                            type="button"
                                            @click="toggleProgramStatusDropdown"
                                            @keydown.stop.prevent="onProgramStatusKeyDown"
                                            :aria-expanded="isProgramStatusDropdownOpen"
                                            aria-haspopup="listbox"
                                            class="w-full flex items-center justify-between px-3 py-2.5 text-sm border border-gray-300 rounded-xl bg-white shadow-sm focus:ring-1 focus:ring-green-500 focus:border-transparent"
                                        >
                                            <span class="text-gray-900">{{ selectedProgramStatusDisplay }}</span>
                                            <svg
                                                class="w-4 h-4 text-green-600 transform transition-transform duration-200"
                                                :class="isProgramStatusDropdownOpen ? 'rotate-180' : ''"
                                                viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg"
                                                aria-hidden="true"
                                            >
                                                <path d="M6 8l4 4 4-4" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/>
                                            </svg>
                                        </button>
                                        <ul
                                            v-show="isProgramStatusDropdownOpen"
                                            role="listbox"
                                            tabindex="-1"
                                            class="origin-top-right absolute right-0 left-0 mt-2 bg-white rounded-xl shadow-lg ring-1 ring-black ring-opacity-5 overflow-auto max-h-56 py-1 focus:outline-none z-50 transition-transform duration-150"
                                        >
                                            <li
                                                v-for="(option, idx) in programStatusOptions"
                                                :key="option.value"
                                                role="option"
                                                :aria-selected="option.value === newProgram.status"
                                                @mouseenter="highlightedProgramStatusIndex = idx"
                                                @mouseleave="highlightedProgramStatusIndex = -1"
                                                @click="selectProgramStatus(option.value)"
                                                :class=" [
                                                    'px-3 py-2 cursor-pointer flex items-center justify-between text-sm',
                                                    highlightedProgramStatusIndex === idx ? 'bg-green-50' : 'hover:bg-green-50',
                                                    option.value === newProgram.status ? 'font-semibold text-green-700' : 'text-gray-700'
                                                ]"
                                            >
                                                <span>{{ option.label }}</span>
                                                <svg v-if="option.value === newProgram.status" class="w-4 h-4 text-green-600" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                    <path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                                </svg>
                                            </li>
                                        </ul>
                                    </div>
                                </div>

                                <!-- Completion Percentage -->
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
                                        class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-1 focus:ring-green-500 focus:border-transparent"
                                        placeholder="0" />
                                </div>

                                <!-- Notes -->
                                <div>
                                    <label for="notes" class="block text-sm font-medium text-gray-700 mb-2">
                                        Notes
                                    </label>
                                    <textarea
                                        id="notes"
                                        v-model="newProgram.notes"
                                        rows="4"
                                        class="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-1 focus:ring-green-500 focus:border-transparent"
                                        placeholder="Enter program notes (optional)"></textarea>
                                </div>
                            </form>
                        </div>

                        <!-- Modal Footer -->
                        <div class="px-6 py-4 border-t border-gray-200 flex justify-end space-x-3 flex-shrink-0">
                            <BaseButton
                                variant="secondary"
                                @click="closeModal">
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
                </Transition>
            </div>
        </Transition>

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

/* Modal Backdrop Animation */
.backdrop-enter-active,
.backdrop-leave-active {
    transition: opacity 0.3s ease;
}

.backdrop-enter-from,
.backdrop-leave-to {
    opacity: 0;
}

/* Slide Panel Animation */
.slide-enter-active {
    transition: transform 0.3s ease-out;
}

.slide-leave-active {
    transition: transform 0.3s ease-in;
}

.slide-enter-from {
    transform: translateX(100%);
}

.slide-leave-to {
    transform: translateX(100%);
}

/* Custom dropdown & UI polish */
::selection {
  background-color: rgba(16, 185, 129, 0.12); /* soft green selection */
}

#filter-type-dropdown button,
#filter-status-dropdown button,
#program-type-dropdown button,
#program-status-dropdown button {
  -webkit-tap-highlight-color: transparent;
}

#filter-type-dropdown ul,
#filter-status-dropdown ul,
#program-type-dropdown ul,
#program-status-dropdown ul {
  -webkit-overflow-scrolling: touch;
}

#filter-type-dropdown li,
#filter-status-dropdown li,
#program-type-dropdown li,
#program-status-dropdown li {
  transition: background-color 160ms ease, color 160ms ease;
}

#filter-type-dropdown svg,
#filter-status-dropdown svg,
#program-type-dropdown svg,
#program-status-dropdown svg {
  transition: transform 200ms ease;
}
</style>
