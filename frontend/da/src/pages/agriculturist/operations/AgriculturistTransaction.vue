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
import { ref, onMounted, computed } from 'vue'

const navigation = MUNICIPAL_AGRICULTURIST_NAVIGATION
const transactionStore = useTransactionStore()
const notificationStore = useNotificationStore()

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

// Lifecycle
onMounted(async () => {
    await fetchTransactions()
})
</script>

<template>
    <AuthenticatedLayout
        :navigation="navigation"
        role-title="Municipal Agriculturist"
        page-title="Transaction Management">

        <div class="flex flex-col h-full space-y-4">
            <!-- Header -->
            <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                <div>
                    <h1 class="text-2xl font-bold text-gray-900">Transaction Management</h1>
                    <p class="text-sm text-gray-600">
                        {{ filteredTransactions.length }} transaction{{ filteredTransactions.length !== 1 ? 's' : '' }}
                        {{ searchQuery || hasActiveFilters ? '(filtered)' : '' }}
                    </p>
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
            <div v-if="transactionStore.isLoading" class="flex justify-center items-center flex-1">
                <LoadingSpinner />
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
                                    placeholder="Search transactions..."
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
                                <select v-model="filters.type" class="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:ring-1 focus:ring-green-500 focus:border-transparent">
                                    <option value="">All Types</option>
                                    <option value="INCOME">Income</option>
                                    <option value="EXPENSE">Expense</option>
                                </select>
                            </div>

                            <div>
                                <label class="block text-xs font-medium text-gray-700 mb-1">Status</label>
                                <select v-model="filters.status" class="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:ring-1 focus:ring-green-500 focus:border-transparent">
                                    <option value="">All Status</option>
                                    <option value="PENDING">Pending</option>
                                    <option value="SCHEDULED">Scheduled</option>
                                    <option value="COMPLETED">Completed</option>
                                    <option value="CANCELLED">Cancelled</option>
                                </select>
                            </div>

                            <div>
                                <label class="block text-xs font-medium text-gray-700 mb-1">Positive</label>
                                <select v-model="filters.positive" class="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:ring-1 focus:ring-green-500 focus:border-transparent">
                                    <option value="">All</option>
                                    <option value="true">Positive</option>
                                    <option value="false">Negative</option>
                                </select>
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
                        <table class="min-w-full divide-y divide-gray-200">
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
                            <tbody class="bg-white divide-y divide-gray-200">
                                <tr v-for="transaction in filteredTransactions" :key="transaction.id" class="hover:bg-gray-50">
                                    <td class="px-4 py-4 whitespace-nowrap">
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
                            <div v-for="transaction in filteredTransactions" :key="transaction.id" class="p-4">
                                <div class="flex items-start justify-between mb-2">
                                    <div class="flex items-start gap-3 flex-1 min-w-0">
                                        <input
                                            type="checkbox"
                                            :checked="selectedTransactions.has(transaction.id)"
                                            class="mt-1 rounded border-gray-300 text-green-600 focus:ring-green-500 flex-shrink-0"
                                            @change="toggleTransactionSelection(transaction.id)" />
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
        <div v-if="showCreateModal" class="fixed inset-0 bg-gray-500 bg-opacity-75 modal-backdrop flex items-center justify-center z-50 p-4">
            <div class="bg-white rounded-xl shadow-2xl max-w-4xl w-full max-h-[90vh] overflow-auto modal-content">
                <!-- Modal Header -->
                <div class="px-8 py-6 border-b border-gray-200 bg-gradient-to-r from-green-50 to-emerald-50">
                    <div class="flex justify-between items-center">
                        <div>
                            <h3 class="text-2xl font-bold text-gray-900">Add New Transaction</h3>
                            <p class="text-gray-600 mt-1">Create a new agricultural transaction record</p>
                        </div>
                        <button
                            class="text-gray-400 hover:text-gray-600 transition-colors"
                            @click="showCreateModal = false">
                            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"></path>
                            </svg>
                        </button>
                    </div>
                </div>

                <!-- Modal Body -->
                <div class="px-8 py-6">
                    <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
                        <!-- Left Column -->
                        <div class="space-y-6">
                            <!-- Transaction Type & Status -->
                            <div class="grid grid-cols-2 gap-4">
                                <div>
                                    <label class="block text-sm font-semibold text-gray-700 mb-2">Transaction Type</label>
                                    <select
                                        v-model="newTransaction.type"
                                        class="w-full rounded-lg border-gray-300 shadow-sm focus:border-green-500 focus:ring-green-500 bg-white">
                                        <option value="INCOME">💰 Income</option>
                                        <option value="EXPENSE">💸 Expense</option>
                                    </select>
                                </div>
                                <div>
                                    <label class="block text-sm font-semibold text-gray-700 mb-2">Status</label>
                                    <select
                                        v-model="newTransaction.status"
                                        class="w-full rounded-lg border-gray-300 shadow-sm focus:border-green-500 focus:ring-green-500 bg-white">
                                        <option value="PENDING">⏳ Pending</option>
                                        <option value="SCHEDULED">📅 Scheduled</option>
                                        <option value="COMPLETED">✅ Completed</option>
                                        <option value="CANCELLED">❌ Cancelled</option>
                                    </select>
                                </div>
                            </div>

                            <!-- Amount -->
                            <div>
                                <label class="block text-sm font-semibold text-gray-700 mb-2">Amount (PHP)</label>
                                <div class="relative">
                                    <span class="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-500 font-medium">₱</span>
                                    <TextInput
                                        v-model="newTransaction.amount"
                                        type="number"
                                        step="0.01"
                                        placeholder="0.00"
                                        class="w-full pl-8 rounded-lg border-gray-300 shadow-sm focus:border-green-500 focus:ring-green-500" />
                                </div>
                            </div>

                            <!-- Date -->
                            <div>
                                <label class="block text-sm font-semibold text-gray-700 mb-2">Transaction Date</label>
                                <input
                                    v-model="newTransaction.date"
                                    type="date"
                                    class="w-full rounded-lg border-gray-300 shadow-sm focus:border-green-500 focus:ring-green-500" />
                            </div>
                        </div>

                        <!-- Right Column -->
                        <div class="space-y-6">
                            <!-- Description -->
                            <div>
                                <label class="block text-sm font-semibold text-gray-700 mb-2">Description</label>
                                <textarea
                                    v-model="newTransaction.description"
                                    rows="6"
                                    placeholder="Enter detailed description of the transaction..."
                                    class="w-full rounded-lg border-gray-300 shadow-sm focus:border-green-500 focus:ring-green-500 resize-none">
                </textarea>
                            </div>

                            <!-- Transaction Preview Card -->
                            <div class="bg-gray-50 rounded-lg p-4 border border-gray-200">
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
                </div>

                <!-- Modal Footer -->
                <div class="px-8 py-6 border-t border-gray-200 bg-gray-50 flex justify-end space-x-4">
                    <BaseButton
                        variant="secondary"
                        class="px-6 py-2.5"
                        @click="showCreateModal = false">
                        Cancel
                    </BaseButton>
                    <BaseButton
                        class="bg-green-600 hover:bg-green-700 px-6 py-2.5"
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
</style>
