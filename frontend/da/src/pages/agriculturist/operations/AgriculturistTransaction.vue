<script setup>
import AuthenticatedLayout from '@/layouts/AuthenticatedLayout.vue'
import LoadingSpinner from '@/components/others/LoadingSpinner.vue'
import BaseCard from '@/components/cards/BaseCard.vue'
import BaseButton from '@/components/buttons/BaseButton.vue'
import TextInput from '@/components/TextInput.vue'
import NotificationToast from '@/components/others/NotificationToast.vue'
import { MUNICIPAL_AGRICULTURIST_NAVIGATION } from '@/lib/navigation'
import { useTransactionStore } from '@/stores/transaction.js'
import { useNotificationStore } from '@/stores/notification.js'
import { ref, onMounted, computed, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { FileText, X } from 'lucide-vue-next'

const navigation = MUNICIPAL_AGRICULTURIST_NAVIGATION
const transactionStore = useTransactionStore()
const notificationStore = useNotificationStore()
const router = useRouter()

// State
const showCreateModal = ref(false)
const showBulkDeleteModal = ref(false)
const showFilters = ref(false)
const selectedTransactions = ref(new Set())
const searchQuery = ref('')

// Filter states
const filters = ref({
    name: '',
    type: '',
    amount: { min: '', max: '' },
    status: '',
    date: { start: '', end: '' },
    positive: ''
})

const newTransaction = ref({
    type: 'EXPENSE',
    amount: '',
    description: '',
    date: new Date().toISOString().split('T')[0],
    status: 'PENDING'
})

// Computed
const filteredTransactions = computed(() => {
    let filtered = transactionStore.allTransactions

    // Apply search filter
    if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        filtered = filtered.filter(transaction =>
            transaction.description.toLowerCase().includes(query) ||
            transaction.type.toLowerCase().includes(query) ||
            transaction.status.toLowerCase().includes(query) ||
            transaction.amount.toString().includes(query)
        )
    }

    // Apply filters
    if (filters.value.name) {
        filtered = filtered.filter(t =>
            t.description.toLowerCase().includes(filters.value.name.toLowerCase())
        )
    }

    if (filters.value.type) {
        filtered = filtered.filter(t => t.type === filters.value.type)
    }

    if (filters.value.status) {
        filtered = filtered.filter(t => t.status === filters.value.status)
    }

    if (filters.value.positive !== '') {
        const isPositive = filters.value.positive === 'true'
        filtered = filtered.filter(t => t.positive === isPositive)
    }

    if (filters.value.amount.min) {
        filtered = filtered.filter(t => t.amount >= parseFloat(filters.value.amount.min))
    }

    if (filters.value.amount.max) {
        filtered = filtered.filter(t => t.amount <= parseFloat(filters.value.amount.max))
    }

    if (filters.value.date.start) {
        filtered = filtered.filter(t => new Date(t.date) >= new Date(filters.value.date.start))
    }

    if (filters.value.date.end) {
        filtered = filtered.filter(t => new Date(t.date) <= new Date(filters.value.date.end))
    }

    return filtered
})

const hasActiveFilters = computed(() => {
    return filters.value.name ||
           filters.value.type ||
           filters.value.status ||
           filters.value.positive !== '' ||
           filters.value.amount.min ||
           filters.value.amount.max ||
           filters.value.date.start ||
           filters.value.date.end
})

const selectedTransactionsCount = computed(() => selectedTransactions.value.size)
const hasSelectedTransactions = computed(() => selectedTransactionsCount.value > 0)

// Methods
const fetchTransactions = async () => {
    await transactionStore.getAllTransactions()
}

const handleCreateTransaction = async () => {
    try {
        if (!newTransaction.value.type || !newTransaction.value.amount || !newTransaction.value.description || !newTransaction.value.status) {
            notificationStore.showError('Please fill in all required fields before creating the transaction.')
            return
        }

        const transactionData = {
            ...newTransaction.value,
            amount: parseFloat(newTransaction.value.amount),
            date: newTransaction.value.date + 'T00:00:00'
        }

        const result = await transactionStore.createTransaction(transactionData)

        if (result.success) {
            showCreateModal.value = false
            resetForm()
            notificationStore.showSuccess('Transaction created successfully! 🎉')
        } else {
            notificationStore.showError(`Failed to create transaction: ${result.message}`)
        }
    } catch (error) {
        console.error('Error creating transaction:', error)
        notificationStore.showError('An unexpected error occurred while creating the transaction. Please try again.')
    }
}

const resetForm = () => {
    newTransaction.value = {
        type: 'EXPENSE',
        amount: '',
        description: '',
        date: new Date().toISOString().split('T')[0],
        status: 'PENDING'
    }
}

const clearFilters = () => {
    filters.value = {
        name: '',
        type: '',
        amount: { min: '', max: '' },
        status: '',
        date: { start: '', end: '' },
        positive: ''
    }
}

const toggleSelectAll = () => {
    if (selectedTransactions.value.size === filteredTransactions.value.length) {
        selectedTransactions.value = new Set()
    } else {
        selectedTransactions.value = new Set(filteredTransactions.value.map(t => t.id))
    }
}

const toggleTransactionSelection = (transactionId) => {
    const newSelectedSet = new Set(selectedTransactions.value)
    if (newSelectedSet.has(transactionId)) {
        newSelectedSet.delete(transactionId)
    } else {
        newSelectedSet.add(transactionId)
    }
    selectedTransactions.value = newSelectedSet
}

const handleBulkDelete = async () => {
    try {
        const count = selectedTransactions.value.size
        const promises = Array.from(selectedTransactions.value).map(id =>
            transactionStore.deleteTransaction(id)
        )

        await Promise.all(promises)
        selectedTransactions.value = new Set()
        showBulkDeleteModal.value = false
        notificationStore.showSuccess(`${count} transaction${count > 1 ? 's' : ''} deleted successfully! 🗑️`)
    } catch (error) {
        console.error('Error deleting transactions:', error)
        notificationStore.showError('Failed to delete some transactions. Please try again.')
    }
}

const formatCurrency = (amount) => {
    if (!amount && amount !== 0) return '₱0.00'
    return new Intl.NumberFormat('en-PH', {
        style: 'currency',
        currency: 'PHP'
    }).format(amount)
}

const formatDate = (dateString) => {
    if (!dateString) return 'N/A'
    try {
        return new Date(dateString).toLocaleDateString('en-PH', {
            year: 'numeric',
            month: 'short',
            day: 'numeric'
        })
    } catch (error) {
        return 'Invalid Date'
    }
}

const navigateToTransactionDetail = (transactionId) => {
    router.push({
        name: 'agriculturist-transaction-detail',
        params: { id: transactionId }
    })
}

// Filter dropdown options
const typeOptions = [
    { label: 'All Types', value: '' },
    { label: 'Income', value: 'INCOME' },
    { label: 'Expense', value: 'EXPENSE' }
]

const statusOptions = [
    { label: 'All Status', value: '' },
    { label: 'Pending', value: 'PENDING' },
    { label: 'Scheduled', value: 'SCHEDULED' },
    { label: 'Completed', value: 'COMPLETED' },
    { label: 'Cancelled', value: 'CANCELLED' }
]

const positiveOptions = [
    { label: 'All', value: '' },
    { label: 'Positive', value: 'true' },
    { label: 'Negative', value: 'false' }
]

const transactionTypeOptions = [
    { label: 'Income', value: 'INCOME' },
    { label: 'Expense', value: 'EXPENSE' }
]

const transactionStatusOptions = [
    { label: 'Pending', value: 'PENDING' },
    { label: 'Scheduled', value: 'SCHEDULED' },
    { label: 'Completed', value: 'COMPLETED' },
    { label: 'Cancelled', value: 'CANCELLED' }
]

// Filter Type dropdown state
const isFilterTypeDropdownOpen = ref(false)
const highlightedFilterTypeIndex = ref(-1)

const toggleFilterTypeDropdown = () => {
    isFilterTypeDropdownOpen.value = !isFilterTypeDropdownOpen.value
    if (isFilterTypeDropdownOpen.value) {
        highlightedFilterTypeIndex.value = typeOptions.findIndex(opt => opt.value === filters.value.type)
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
            highlightedFilterTypeIndex.value = Math.min(highlightedFilterTypeIndex.value + 1, typeOptions.length - 1)
        } else if (e.key === 'ArrowUp') {
            e.preventDefault()
            highlightedFilterTypeIndex.value = Math.max(highlightedFilterTypeIndex.value - 1, 0)
        } else if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault()
            if (highlightedFilterTypeIndex.value >= 0) selectFilterType(typeOptions[highlightedFilterTypeIndex.value].value)
        } else if (e.key === 'Escape') {
            e.preventDefault()
            closeFilterTypeDropdown()
        }
    }
}

const selectedFilterTypeDisplay = computed(() => {
    const option = typeOptions.find(opt => opt.value === filters.value.type)
    return option ? option.label : 'All Types'
})

// Filter Status dropdown state
const isFilterStatusDropdownOpen = ref(false)
const highlightedFilterStatusIndex = ref(-1)

const toggleFilterStatusDropdown = () => {
    isFilterStatusDropdownOpen.value = !isFilterStatusDropdownOpen.value
    if (isFilterStatusDropdownOpen.value) {
        highlightedFilterStatusIndex.value = statusOptions.findIndex(opt => opt.value === filters.value.status)
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
            highlightedFilterStatusIndex.value = Math.min(highlightedFilterStatusIndex.value + 1, statusOptions.length - 1)
        } else if (e.key === 'ArrowUp') {
            e.preventDefault()
            highlightedFilterStatusIndex.value = Math.max(highlightedFilterStatusIndex.value - 1, 0)
        } else if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault()
            if (highlightedFilterStatusIndex.value >= 0) selectFilterStatus(statusOptions[highlightedFilterStatusIndex.value].value)
        } else if (e.key === 'Escape') {
            e.preventDefault()
            closeFilterStatusDropdown()
        }
    }
}

const selectedFilterStatusDisplay = computed(() => {
    const option = statusOptions.find(opt => opt.value === filters.value.status)
    return option ? option.label : 'All Status'
})

// Filter Positive dropdown state
const isFilterPositiveDropdownOpen = ref(false)
const highlightedFilterPositiveIndex = ref(-1)

const toggleFilterPositiveDropdown = () => {
    isFilterPositiveDropdownOpen.value = !isFilterPositiveDropdownOpen.value
    if (isFilterPositiveDropdownOpen.value) {
        highlightedFilterPositiveIndex.value = positiveOptions.findIndex(opt => opt.value === filters.value.positive)
    }
}

const closeFilterPositiveDropdown = () => {
    isFilterPositiveDropdownOpen.value = false
    highlightedFilterPositiveIndex.value = -1
}

const selectFilterPositive = (value) => {
    filters.value.positive = value
    closeFilterPositiveDropdown()
}

const onFilterPositiveKeyDown = (e) => {
    if (!isFilterPositiveDropdownOpen.value && (e.key === 'Enter' || e.key === ' ')) {
        e.preventDefault()
        toggleFilterPositiveDropdown()
        return
    }
    if (isFilterPositiveDropdownOpen.value) {
        if (e.key === 'ArrowDown') {
            e.preventDefault()
            highlightedFilterPositiveIndex.value = Math.min(highlightedFilterPositiveIndex.value + 1, positiveOptions.length - 1)
        } else if (e.key === 'ArrowUp') {
            e.preventDefault()
            highlightedFilterPositiveIndex.value = Math.max(highlightedFilterPositiveIndex.value - 1, 0)
        } else if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault()
            if (highlightedFilterPositiveIndex.value >= 0) selectFilterPositive(positiveOptions[highlightedFilterPositiveIndex.value].value)
        } else if (e.key === 'Escape') {
            e.preventDefault()
            closeFilterPositiveDropdown()
        }
    }
}

const selectedFilterPositiveDisplay = computed(() => {
    const option = positiveOptions.find(opt => opt.value === filters.value.positive)
    return option ? option.label : 'All'
})

// Transaction Type dropdown state (in create modal)
const isTransactionTypeDropdownOpen = ref(false)
const highlightedTransactionTypeIndex = ref(-1)

const toggleTransactionTypeDropdown = () => {
    isTransactionTypeDropdownOpen.value = !isTransactionTypeDropdownOpen.value
    if (isTransactionTypeDropdownOpen.value) {
        highlightedTransactionTypeIndex.value = transactionTypeOptions.findIndex(opt => opt.value === newTransaction.value.type)
    }
}

const closeTransactionTypeDropdown = () => {
    isTransactionTypeDropdownOpen.value = false
    highlightedTransactionTypeIndex.value = -1
}

const selectTransactionType = (value) => {
    newTransaction.value.type = value
    closeTransactionTypeDropdown()
}

const onTransactionTypeKeyDown = (e) => {
    if (!isTransactionTypeDropdownOpen.value && (e.key === 'Enter' || e.key === ' ')) {
        e.preventDefault()
        toggleTransactionTypeDropdown()
        return
    }
    if (isTransactionTypeDropdownOpen.value) {
        if (e.key === 'ArrowDown') {
            e.preventDefault()
            highlightedTransactionTypeIndex.value = Math.min(highlightedTransactionTypeIndex.value + 1, transactionTypeOptions.length - 1)
        } else if (e.key === 'ArrowUp') {
            e.preventDefault()
            highlightedTransactionTypeIndex.value = Math.max(highlightedTransactionTypeIndex.value - 1, 0)
        } else if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault()
            if (highlightedTransactionTypeIndex.value >= 0) selectTransactionType(transactionTypeOptions[highlightedTransactionTypeIndex.value].value)
        } else if (e.key === 'Escape') {
            e.preventDefault()
            closeTransactionTypeDropdown()
        }
    }
}

const selectedTransactionTypeDisplay = computed(() => {
    const option = transactionTypeOptions.find(opt => opt.value === newTransaction.value.type)
    return option ? option.label : '💸 Expense'
})

// Transaction Status dropdown state (in create modal)
const isTransactionStatusDropdownOpen = ref(false)
const highlightedTransactionStatusIndex = ref(-1)

const toggleTransactionStatusDropdown = () => {
    isTransactionStatusDropdownOpen.value = !isTransactionStatusDropdownOpen.value
    if (isTransactionStatusDropdownOpen.value) {
        highlightedTransactionStatusIndex.value = transactionStatusOptions.findIndex(opt => opt.value === newTransaction.value.status)
    }
}

const closeTransactionStatusDropdown = () => {
    isTransactionStatusDropdownOpen.value = false
    highlightedTransactionStatusIndex.value = -1
}

const selectTransactionStatus = (value) => {
    newTransaction.value.status = value
    closeTransactionStatusDropdown()
}

const onTransactionStatusKeyDown = (e) => {
    if (!isTransactionStatusDropdownOpen.value && (e.key === 'Enter' || e.key === ' ')) {
        e.preventDefault()
        toggleTransactionStatusDropdown()
        return
    }
    if (isTransactionStatusDropdownOpen.value) {
        if (e.key === 'ArrowDown') {
            e.preventDefault()
            highlightedTransactionStatusIndex.value = Math.min(highlightedTransactionStatusIndex.value + 1, transactionStatusOptions.length - 1)
        } else if (e.key === 'ArrowUp') {
            e.preventDefault()
            highlightedTransactionStatusIndex.value = Math.max(highlightedTransactionStatusIndex.value - 1, 0)
        } else if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault()
            if (highlightedTransactionStatusIndex.value >= 0) selectTransactionStatus(transactionStatusOptions[highlightedTransactionStatusIndex.value].value)
        } else if (e.key === 'Escape') {
            e.preventDefault()
            closeTransactionStatusDropdown()
        }
    }
}

const selectedTransactionStatusDisplay = computed(() => {
    const option = transactionStatusOptions.find(opt => opt.value === newTransaction.value.status)
    return option ? option.label : '⏳ Pending'
})

// Close on outside click
const onClickOutside = (e) => {
    const filterTypeEl = document.querySelector('#filter-type-dropdown')
    const filterStatusEl = document.querySelector('#filter-status-dropdown')
    const filterPositiveEl = document.querySelector('#filter-positive-dropdown')
    const transactionTypeEl = document.querySelector('#transaction-type-dropdown')
    const transactionStatusEl = document.querySelector('#transaction-status-dropdown')
    
    if (filterTypeEl && !filterTypeEl.contains(e.target)) closeFilterTypeDropdown()
    if (filterStatusEl && !filterStatusEl.contains(e.target)) closeFilterStatusDropdown()
    if (filterPositiveEl && !filterPositiveEl.contains(e.target)) closeFilterPositiveDropdown()
    if (transactionTypeEl && !transactionTypeEl.contains(e.target)) closeTransactionTypeDropdown()
    if (transactionStatusEl && !transactionStatusEl.contains(e.target)) closeTransactionStatusDropdown()
}

// Lifecycle
onMounted(async () => {
    await fetchTransactions()
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
        page-title="Transaction Management">

        <div class="flex flex-col h-full space-y-4">
<!-- Header -->
<div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 border-b border-gray-200 pb-3 ml-4">
    <div class="flex items-center gap-2">
        <div>
            <h1 class="text-3xl font-bold text-green-600">
                Application Workspace
            </h1>

            <p class="mt-1 text-sm text-gray-600">
                {{ filteredTransactions.length }} transaction{{ filteredTransactions.length !== 1 ? 's' : '' }}
                {{ searchQuery || hasActiveFilters ? '(filtered)' : '' }}
            </p>
        </div>
    </div>
                <BaseButton
                    class="bg-green-600 hover:bg-green-700"
                    @click="showCreateModal = true">
                    <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
                    </svg>
                    Add Transaction
                </BaseButton>
            </div>

            <!-- Loading State -->
            <div
                v-if="transactionStore.isLoading"
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
            <BaseCard v-else-if="transactionStore.hasError" class="p-8">
                <div class="flex items-center justify-center">
                    <div class="text-center">
                        <svg class="w-12 h-12 text-red-500 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.732-.833-2.5 0L4.268 6.5c-.77.833-.192 2.5 1.732 2.5z"></path>
                        </svg>
                        <h3 class="text-lg font-medium text-gray-900 mb-2">Error Loading Transactions</h3>
                        <p class="text-gray-600 mb-4">{{ transactionStore.errorMessage }}</p>
                        <BaseButton class="bg-green-600 hover:bg-green-700" @click="fetchTransactions">
                            Try Again
                        </BaseButton>
                    </div>
                </div>
            </BaseCard>

            <!-- Transactions Table -->
            <div v-else class="bg-gray-100 rounded-xl border border-gray-300 flex flex-col flex-1 min-h-0 overflow-hidden">
                <!-- Table Header with Search and Filters -->
                <div class="p-4 border-b border-gray-300 space-y-4 flex-shrink-0 bg-gray-100">
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
                                    placeholder="Search transactions..."
                                    class="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg  focus:ring-1 focus:ring-green-500 focus:border-transparent text-sm" />
                            </div>

                            <!-- Filter Toggle Button -->
                        <BaseButton
                        class="bg-green-600 border-green-600 text-white hover:bg-green-700 hover:border-green-700"
                        :class="{
                            'bg-gray-600 border-gray-600 hover:bg-gray-700 hover:border-gray-700':
                            showFilters || hasActiveFilters
                        }"
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

                        <!-- Bulk Delete Button -->
                        <BaseButton
                            v-if="hasSelectedTransactions"
                            class="bg-red-600 hover:bg-red-700"
                            @click="showBulkDeleteModal = true">
                            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                            </svg>
                            Delete {{ selectedTransactionsCount }}
                        </BaseButton>
                    </div>

                    <!-- Filter Panel -->
                    <div v-if="showFilters" class="p-4 bg-gray-50 rounded-lg border border-gray-200">
                        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                            <div>
                                <label class="block text-xs font-medium text-gray-700 mb-1">Name</label>
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
                                            v-for="(option, idx) in typeOptions"
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
                                            v-for="(option, idx) in statusOptions"
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
                                <label class="block text-xs font-medium text-gray-700 mb-1">Positive</label>
                                <div id="filter-positive-dropdown" class="relative">
                                    <button
                                        type="button"
                                        @click="toggleFilterPositiveDropdown"
                                        @keydown.stop.prevent="onFilterPositiveKeyDown"
                                        :aria-expanded="isFilterPositiveDropdownOpen"
                                        aria-haspopup="listbox"
                                        class="w-full flex items-center justify-between px-3 py-2 text-sm border border-gray-300 rounded-xl bg-white shadow-sm focus:ring-1 focus:ring-green-500 focus:border-transparent"
                                    >
                                        <span class="text-gray-900">{{ selectedFilterPositiveDisplay }}</span>
                                        <svg
                                            class="w-4 h-4 text-green-600 transform transition-transform duration-200"
                                            :class="isFilterPositiveDropdownOpen ? 'rotate-180' : ''"
                                            viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg"
                                            aria-hidden="true"
                                        >
                                            <path d="M6 8l4 4 4-4" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/>
                                        </svg>
                                    </button>
                                    <ul
                                        v-show="isFilterPositiveDropdownOpen"
                                        role="listbox"
                                        tabindex="-1"
                                        class="origin-top-right absolute right-0 left-0 mt-2 bg-white rounded-xl shadow-lg ring-1 ring-black ring-opacity-5 overflow-auto max-h-56 py-1 focus:outline-none z-50 transition-transform duration-150"
                                    >
                                        <li
                                            v-for="(option, idx) in positiveOptions"
                                            :key="option.value"
                                            role="option"
                                            :aria-selected="option.value === filters.positive"
                                            @mouseenter="highlightedFilterPositiveIndex = idx"
                                            @mouseleave="highlightedFilterPositiveIndex = -1"
                                            @click="selectFilterPositive(option.value)"
                                            :class=" [
                                                'px-3 py-2 cursor-pointer flex items-center justify-between text-sm',
                                                highlightedFilterPositiveIndex === idx ? 'bg-green-50' : 'hover:bg-green-50',
                                                option.value === filters.positive ? 'font-semibold text-green-700' : 'text-gray-700'
                                            ]"
                                        >
                                            <span>{{ option.label }}</span>
                                            <svg v-if="option.value === filters.positive" class="w-4 h-4 text-green-600" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                <path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                            </svg>
                                        </li>
                                    </ul>
                                </div>
                            </div>

                            <div>
                                <label class="block text-xs font-medium text-gray-700 mb-1">Amount Range</label>
                                <div class="flex gap-2">
                                    <input
                                        v-model="filters.amount.min"
                                        type="number"
                                        placeholder="Min"
                                        class="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:ring-1 focus:ring-green-500 focus:border-transparent" />
                                    <input
                                        v-model="filters.amount.max"
                                        type="number"
                                        placeholder="Max"
                                        class="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:ring-1 focus:ring-green-500 focus:border-transparent" />
                                </div>
                            </div>

                            <div>
                                <label class="block text-xs font-medium text-gray-700 mb-1">Date Range</label>
                                <div class="flex gap-2">
                                    <input
                                        v-model="filters.date.start"
                                        type="date"
                                        class="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:ring-1 focus:ring-green-500 focus:border-transparent" />
                                    <input
                                        v-model="filters.date.end"
                                        type="date"
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
                        <table class="min-w-full divide-y divide-gray-300">
                            <thead class="bg-gray-50 sticky top-0 z-10">
                                <tr>
                                    <th scope="col" class="w-12 px-4 py-3">
                                        <input
                                            type="checkbox"
                                            :checked="selectedTransactions.size === filteredTransactions.length && filteredTransactions.length > 0"
                                            :indeterminate="selectedTransactions.size > 0 && selectedTransactions.size < filteredTransactions.length"
                                            class="rounded border-gray-300 text-green-600 focus:ring-green-500"
                                            @change="toggleSelectAll" />
                                    </th>
                                    <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Date</th>
                                    <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Type</th>
                                    <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Description</th>
                                    <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Amount</th>
                                    <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                                </tr>
                            </thead>
                            <tbody class="bg-white divide-y divide-gray-300">
                                <tr v-for="transaction in filteredTransactions" :key="transaction.id" class="hover:bg-green-50 cursor-pointer transition-colors" @click="navigateToTransactionDetail(transaction.id)">
                                    <td class="px-4 py-4 whitespace-nowrap" @click.stop>
                                        <input
                                            type="checkbox"
                                            :checked="selectedTransactions.has(transaction.id)"
                                            class="rounded border-gray-300 text-green-600 focus:ring-green-500"
                                            @change="toggleTransactionSelection(transaction.id)" />
                                    </td>
                                    <td class="px-4 py-4 whitespace-nowrap text-sm text-gray-900">{{ formatDate(transaction.date) }}</td>
                                    <td class="px-4 py-4 whitespace-nowrap">
                                        <span :class="{
                                            'bg-green-100 text-green-800': transaction.type === 'INCOME',
                                            'bg-red-100 text-red-800': transaction.type === 'EXPENSE'
                                        }" class="px-2 py-1 text-xs font-medium rounded-full">
                                            {{ transaction.type }}
                                        </span>
                                    </td>
                                    <td class="px-4 py-4 text-sm text-gray-900 max-w-xs">
                                        <div class="truncate" :title="transaction.description">
                                            {{ transaction.description }}
                                        </div>
                                    </td>
                                    <td class="px-4 py-4 whitespace-nowrap text-sm font-medium" :class="{
                                        'text-green-600': transaction.positive,
                                        'text-red-600': !transaction.positive
                                    }">
                                        {{ formatCurrency(transaction.amount) }}
                                    </td>
                                    <td class="px-4 py-4 whitespace-nowrap">
                                        <span :class="{
                                            'bg-yellow-100 text-yellow-800': transaction.status === 'SCHEDULED',
                                            'bg-green-100 text-green-800': transaction.status === 'COMPLETED',
                                            'bg-gray-100 text-gray-800': transaction.status === 'PENDING',
                                            'bg-red-100 text-red-800': transaction.status === 'CANCELLED'
                                        }" class="px-2 py-1 text-xs font-medium rounded-full">
                                            {{ transaction.status }}
                                        </span>
                                    </td>
                                </tr>
                                <tr v-if="filteredTransactions.length === 0">
                                    <td colspan="6" class="px-4 py-8 text-center text-gray-500">
                                        No transactions found
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
                            <div v-for="transaction in filteredTransactions" :key="transaction.id" class="p-4 cursor-pointer hover:bg-gray-50 transition-colors" @click="navigateToTransactionDetail(transaction.id)">
                                <div class="flex items-start justify-between mb-2">
                                    <div class="flex items-start gap-3 flex-1 min-w-0">
                                        <div @click.stop>
                                            <input
                                                type="checkbox"
                                                :checked="selectedTransactions.has(transaction.id)"
                                                class="mt-1 rounded border-gray-300 text-green-600 focus:ring-green-500 flex-shrink-0"
                                                @change="toggleTransactionSelection(transaction.id)" />
                                        </div>
                                        <div class="flex-1 min-w-0">
                                            <p class="text-sm font-medium text-gray-900">{{ formatDate(transaction.date) }}</p>
                                            <p class="text-sm text-gray-600 mt-1 break-words">{{ transaction.description }}</p>
                                        </div>
                                    </div>
                                    <p :class="{
                                        'text-green-600': transaction.positive,
                                        'text-red-600': !transaction.positive
                                    }" class="text-lg font-semibold whitespace-nowrap ml-2 flex-shrink-0">
                                        {{ formatCurrency(transaction.amount) }}
                                    </p>
                                </div>
                                <div class="flex gap-2 ml-8">
                                    <span :class="{
                                        'bg-green-100 text-green-800': transaction.type === 'INCOME',
                                        'bg-red-100 text-red-800': transaction.type === 'EXPENSE'
                                    }" class="px-2 py-1 text-xs font-medium rounded-full">
                                        {{ transaction.type }}
                                    </span>
                                    <span :class="{
                                        'bg-yellow-100 text-yellow-800': transaction.status === 'SCHEDULED',
                                        'bg-green-100 text-green-800': transaction.status === 'COMPLETED',
                                        'bg-gray-100 text-gray-800': transaction.status === 'PENDING',
                                        'bg-red-100 text-red-800': transaction.status === 'CANCELLED'
                                    }" class="px-2 py-1 text-xs font-medium rounded-full">
                                        {{ transaction.status }}
                                    </span>
                                </div>
                            </div>
                            <div v-if="filteredTransactions.length === 0" class="p-8 text-center text-gray-500">
                                No transactions found
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Create Transaction Modal -->
        <div v-if="showCreateModal" class="fixed inset-0 z-50 flex justify-end bg-white/10 backdrop-blur-sm transition-all" aria-labelledby="modal-title" role="dialog" aria-modal="true">
            <div class="bg-white h-full w-full max-w-2xl border border-gray-300 transform transition-all translate-x-0 overflow-y-auto">
                <!-- Modal Header -->
                <div class="flex items-center justify-between px-6 py-4 border-b border-gray-200">
                    <div>
                        <h3 class="text-lg font-semibold text-gray-800">Add New Transaction</h3>
                        <p class="text-sm text-gray-600 mt-1">Create a new agricultural transaction record</p>
                    </div>
                    <button
                        class="text-gray-400 hover:text-gray-600 transition"
                        @click="showCreateModal = false">
                        <X class="h-5 w-5" />
                    </button>
                </div>

                <!-- Modal Body -->
                <div class="px-6 py-5">
                    <div class="space-y-4">
                        <!-- Transaction Type & Status -->
                        <div class="grid grid-cols-2 gap-4">
                            <div>
                                <label class="block text-sm font-medium text-gray-700 mb-1">Transaction Type</label>
                                <div id="transaction-type-dropdown" class="relative">
                                    <button
                                        type="button"
                                        @click="toggleTransactionTypeDropdown"
                                        @keydown.stop.prevent="onTransactionTypeKeyDown"
                                        :aria-expanded="isTransactionTypeDropdownOpen"
                                        aria-haspopup="listbox"
                                        class="w-full flex items-center justify-between px-3 py-2.5 text-sm border border-gray-300 rounded-xl bg-white shadow-sm focus:ring-1 focus:ring-green-500 focus:border-transparent"
                                    >
                                        <span class="text-gray-900">{{ selectedTransactionTypeDisplay }}</span>
                                        <svg
                                            class="w-4 h-4 text-green-600 transform transition-transform duration-200"
                                            :class="isTransactionTypeDropdownOpen ? 'rotate-180' : ''"
                                            viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg"
                                            aria-hidden="true"
                                        >
                                            <path d="M6 8l4 4 4-4" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/>
                                        </svg>
                                    </button>
                                    <ul
                                        v-show="isTransactionTypeDropdownOpen"
                                        role="listbox"
                                        tabindex="-1"
                                        class="origin-top-right absolute right-0 left-0 mt-2 bg-white rounded-xl shadow-lg ring-1 ring-black ring-opacity-5 overflow-auto max-h-56 py-1 focus:outline-none z-50 transition-transform duration-150"
                                    >
                                        <li
                                            v-for="(option, idx) in transactionTypeOptions"
                                            :key="option.value"
                                            role="option"
                                            :aria-selected="option.value === newTransaction.type"
                                            @mouseenter="highlightedTransactionTypeIndex = idx"
                                            @mouseleave="highlightedTransactionTypeIndex = -1"
                                            @click="selectTransactionType(option.value)"
                                            :class=" [
                                                'px-3 py-2 cursor-pointer flex items-center justify-between text-sm',
                                                highlightedTransactionTypeIndex === idx ? 'bg-green-50' : 'hover:bg-green-50',
                                                option.value === newTransaction.type ? 'font-semibold text-green-700' : 'text-gray-700'
                                            ]"
                                        >
                                            <span>{{ option.label }}</span>
                                            <svg v-if="option.value === newTransaction.type" class="w-4 h-4 text-green-600" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                <path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                            </svg>
                                        </li>
                                    </ul>
                                </div>
                            </div>
                            <div>
                                <label class="block text-sm font-medium text-gray-700 mb-1">Status</label>
                                <div id="transaction-status-dropdown" class="relative">
                                    <button
                                        type="button"
                                        @click="toggleTransactionStatusDropdown"
                                        @keydown.stop.prevent="onTransactionStatusKeyDown"
                                        :aria-expanded="isTransactionStatusDropdownOpen"
                                        aria-haspopup="listbox"
                                        class="w-full flex items-center justify-between px-3 py-2.5 text-sm border border-gray-300 rounded-xl bg-white shadow-sm focus:ring-1 focus:ring-green-500 focus:border-transparent"
                                    >
                                        <span class="text-gray-900">{{ selectedTransactionStatusDisplay }}</span>
                                        <svg
                                            class="w-4 h-4 text-green-600 transform transition-transform duration-200"
                                            :class="isTransactionStatusDropdownOpen ? 'rotate-180' : ''"
                                            viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg"
                                            aria-hidden="true"
                                        >
                                            <path d="M6 8l4 4 4-4" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/>
                                        </svg>
                                    </button>
                                    <ul
                                        v-show="isTransactionStatusDropdownOpen"
                                        role="listbox"
                                        tabindex="-1"
                                        class="origin-top-right absolute right-0 left-0 mt-2 bg-white rounded-xl shadow-lg ring-1 ring-black ring-opacity-5 overflow-auto max-h-56 py-1 focus:outline-none z-50 transition-transform duration-150"
                                    >
                                        <li
                                            v-for="(option, idx) in transactionStatusOptions"
                                            :key="option.value"
                                            role="option"
                                            :aria-selected="option.value === newTransaction.status"
                                            @mouseenter="highlightedTransactionStatusIndex = idx"
                                            @mouseleave="highlightedTransactionStatusIndex = -1"
                                            @click="selectTransactionStatus(option.value)"
                                            :class=" [
                                                'px-3 py-2 cursor-pointer flex items-center justify-between text-sm',
                                                highlightedTransactionStatusIndex === idx ? 'bg-green-50' : 'hover:bg-green-50',
                                                option.value === newTransaction.status ? 'font-semibold text-green-700' : 'text-gray-700'
                                            ]"
                                        >
                                            <span>{{ option.label }}</span>
                                            <svg v-if="option.value === newTransaction.status" class="w-4 h-4 text-green-600" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                                <path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                            </svg>
                                        </li>
                                    </ul>
                                </div>
                            </div>
                        </div>

                        <!-- Amount -->
                        <div>
                            <label class="block text-sm font-medium text-gray-700 mb-1">Amount (PHP)</label>
                            <div class="relative">
                                <span class="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-500 font-medium">₱</span>
                                <TextInput
                                    v-model="newTransaction.amount"
                                    type="number"
                                    step="0.01"
                                    placeholder="0.00"
                                    class="w-full pl-8 rounded-xl border-gray-300 shadow-sm focus:ring-1 focus:ring-green-500 focus:border-transparent" />
                            </div>
                        </div>

                        <!-- Date -->
                        <div>
                            <label class="block text-sm font-medium text-gray-700 mb-1">Transaction Date</label>
                            <input
                                v-model="newTransaction.date"
                                type="date"
                                class="w-full rounded-xl border-gray-300 shadow-sm focus:ring-1 focus:ring-green-500 focus:border-transparent px-3 py-2.5" />
                        </div>

                        <!-- Description -->
                        <div>
                            <label class="block text-sm font-medium text-gray-700 mb-1">Description</label>
                            <textarea
                                v-model="newTransaction.description"
                                rows="4"
                                placeholder="Enter detailed description of the transaction..."
                                class="w-full rounded-xl border-gray-300 shadow-sm focus:ring-1 focus:ring-green-500 focus:border-transparent resize-none px-3 py-2.5">
                            </textarea>
                        </div>

                        <!-- Transaction Preview Card -->
                        <div class="bg-green-100 rounded-lg p-4 border border-green-300">
                            <h4 class="text-sm font-semibold text-gray-700 mb-3">Transaction Preview</h4>
                            <div class="space-y-2 text-sm">
                                <div class="flex justify-between">
                                    <span class="text-gray-600">Type:</span>
                                    <span :class="newTransaction.type === 'INCOME' ? 'text-green-600' : 'text-red-600'" class="font-medium">
                                        {{ newTransaction.type || 'Not selected' }}
                                    </span>
                                </div>
                                <div class="flex justify-between">
                                    <span class="text-gray-600">Amount:</span>
                                    <span class="font-medium text-gray-900">
                                        {{ newTransaction.amount ? formatCurrency(parseFloat(newTransaction.amount)) : '₱0.00' }}
                                    </span>
                                </div>
                                <div class="flex justify-between">
                                    <span class="text-gray-600">Status:</span>
                                    <span class="font-medium text-gray-900">{{ newTransaction.status || 'Not selected' }}</span>
                                </div>
                                <div class="flex justify-between">
                                    <span class="text-gray-600">Date:</span>
                                    <span class="font-medium text-gray-900">{{ newTransaction.date || 'Not selected' }}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- Modal Footer -->
                <div class="bg-white px-6 py-4 flex justify-end gap-3 border-t border-gray-200">
                    <BaseButton
                        variant="secondary"
                        @click="showCreateModal = false">
                        Cancel
                    </BaseButton>
                    <BaseButton
                        class="bg-green-600 hover:bg-green-700"
                        @click="handleCreateTransaction">
                        <svg class="w-5 h-5 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4v16m8-8H4"></path>
                        </svg>
                        Create Transaction
                    </BaseButton>
                </div>
            </div>
        </div>

        <!-- Bulk Delete Confirmation Modal -->
        <div v-if="showBulkDeleteModal" class="fixed inset-0 bg-gray-500 bg-opacity-75 flex items-center justify-center z-50">
            <div class="bg-white rounded-lg shadow-xl max-w-md w-full mx-4">
                <div class="px-6 py-4">
                    <h3 class="text-lg font-medium text-gray-900 mb-2">Delete Multiple Transactions</h3>
                    <p class="text-gray-600">Are you sure you want to delete {{ selectedTransactionsCount }} transaction{{ selectedTransactionsCount > 1 ? 's' : '' }}? This action cannot be undone.</p>
                </div>
                <div class="px-6 py-4 border-t border-gray-200 flex justify-end space-x-3">
                    <BaseButton
                        variant="secondary"
                        @click="showBulkDeleteModal = false">
                        Cancel
                    </BaseButton>
                    <BaseButton
                        class="bg-red-600 hover:bg-red-700"
                        @click="handleBulkDelete">
                        Delete {{ selectedTransactionsCount }} Transaction{{ selectedTransactionsCount > 1 ? 's' : '' }}
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

/* Custom dropdown & UI polish */
::selection {
  background-color: rgba(16, 185, 129, 0.12); /* soft green selection */
}

#filter-type-dropdown button,
#filter-status-dropdown button,
#filter-positive-dropdown button,
#transaction-type-dropdown button,
#transaction-status-dropdown button {
  -webkit-tap-highlight-color: transparent;
}

#filter-type-dropdown ul,
#filter-status-dropdown ul,
#filter-positive-dropdown ul,
#transaction-type-dropdown ul,
#transaction-status-dropdown ul {
  -webkit-overflow-scrolling: touch;
}

#filter-type-dropdown li,
#filter-status-dropdown li,
#filter-positive-dropdown li,
#transaction-type-dropdown li,
#transaction-status-dropdown li {
  transition: background-color 160ms ease, color 160ms ease;
}

#filter-type-dropdown svg,
#filter-status-dropdown svg,
#filter-positive-dropdown svg,
#transaction-type-dropdown svg,
#transaction-status-dropdown svg {
  transition: transform 200ms ease;
}
</style>
