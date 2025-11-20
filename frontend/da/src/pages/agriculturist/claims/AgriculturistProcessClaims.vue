<script setup>
import AuthenticatedLayout from '@/layouts/AuthenticatedLayout.vue'
import LoadingSpinner from '@/components/others/LoadingSpinner.vue'
import BaseCard from '@/components/cards/BaseCard.vue'
import BaseButton from '@/components/buttons/BaseButton.vue'
import NotificationToast from '@/components/others/NotificationToast.vue'
import { MUNICIPAL_AGRICULTURIST_NAVIGATION } from '@/lib/navigation'
import { useClaimStore } from '@/stores/claim.js'
import { useNotificationStore } from '@/stores/notification.js'
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'

const navigation = MUNICIPAL_AGRICULTURIST_NAVIGATION
const claimStore = useClaimStore()
const notificationStore = useNotificationStore()
const router = useRouter()

// State
const showBulkDeleteModal = ref(false)
const showFilters = ref(false)
const selectedClaims = ref(new Set())
const searchQuery = ref('')

// Filter states
const filters = ref({
    applicationName: '',
    amount: { min: '', max: '' },
    location: ''
})

// Helper to get farm location from claim
const getFarmLocation = (claim) => {
    return claim.fieldValues?.farm_location || 'N/A'
}

// Computed
const filteredClaims = computed(() => {
    let filtered = claimStore.allClaims

    // Apply search filter
    if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        filtered = filtered.filter(claim =>
            claim.farmerName?.toLowerCase().includes(query) ||
            getFarmLocation(claim).toLowerCase().includes(query) ||
            claim.claimAmount?.toString().includes(query) ||
            claim.damageAssessment?.toLowerCase().includes(query)
        )
    }

    // Apply filters
    if (filters.value.applicationName) {
        filtered = filtered.filter(c =>
            c.farmerName?.toLowerCase().includes(filters.value.applicationName.toLowerCase())
        )
    }

    if (filters.value.location) {
        filtered = filtered.filter(c =>
            getFarmLocation(c).toLowerCase().includes(filters.value.location.toLowerCase())
        )
    }

    if (filters.value.amount.min) {
        filtered = filtered.filter(c => c.claimAmount >= parseFloat(filters.value.amount.min))
    }

    if (filters.value.amount.max) {
        filtered = filtered.filter(c => c.claimAmount <= parseFloat(filters.value.amount.max))
    }

    return filtered
})

const hasActiveFilters = computed(() => {
    return filters.value.applicationName ||
           filters.value.location ||
           filters.value.amount.min ||
           filters.value.amount.max
})

const selectedClaimsCount = computed(() => selectedClaims.value.size)
const hasSelectedClaims = computed(() => selectedClaimsCount.value > 0)

// Methods
const fetchClaims = async () => {
    await claimStore.getAllClaims()
}

const clearFilters = () => {
    filters.value = {
        applicationName: '',
        amount: { min: '', max: '' },
        location: ''
    }
}

const toggleSelectAll = () => {
    if (selectedClaims.value.size === filteredClaims.value.length) {
        selectedClaims.value = new Set()
    } else {
        selectedClaims.value = new Set(filteredClaims.value.map(c => c.id))
    }
}

const toggleClaimSelection = (claimId) => {
    const newSelectedSet = new Set(selectedClaims.value)
    if (newSelectedSet.has(claimId)) {
        newSelectedSet.delete(claimId)
    } else {
        newSelectedSet.add(claimId)
    }
    selectedClaims.value = newSelectedSet
}

const handleBulkDelete = async () => {
    try {
        const count = selectedClaims.value.size
        const promises = Array.from(selectedClaims.value).map(id =>
            claimStore.deleteClaim(id)
        )

        await Promise.all(promises)
        selectedClaims.value = new Set()
        showBulkDeleteModal.value = false
        notificationStore.showSuccess(`${count} claim${count > 1 ? 's' : ''} deleted successfully!`)
    } catch (error) {
        console.error('Error deleting claims:', error)
        notificationStore.showError('Failed to delete some claims. Please try again.')
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


const navigateToClaimDetail = (claimId) => {
    router.push({
        name: 'agriculturist-claim-detail',
        params: { id: claimId }
    })
}

// Lifecycle
onMounted(async () => {
    await fetchClaims()
})
</script>

<template>
    <AuthenticatedLayout
        :navigation="navigation"
        role-title="Municipal Agriculturist"
        page-title="Process Claims">

        <div class="flex flex-col h-full space-y-4">
            <!-- Header -->
            <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                <div>
                    <h1 class="text-2xl font-bold text-gray-900">Process Claims</h1>
                </div>
                <div class="flex items-center gap-3">
                    <!-- Search Input -->
                    <div class="relative">
                        <svg class="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
                        </svg>
                        <input
                            v-model="searchQuery"
                            type="text"
                            placeholder="Search claims..."
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

            <!-- Loading State -->
            <div v-if="claimStore.isLoading" class="flex justify-center items-center flex-1">
                <LoadingSpinner />
            </div>

            <!-- Error State -->
            <BaseCard v-else-if="claimStore.hasError" class="p-8">
                <div class="flex items-center justify-center">
                    <div class="text-center">
                        <svg class="w-12 h-12 text-red-500 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.732-.833-2.5 0L4.268 6.5c-.77.833-.192 2.5 1.732 2.5z"></path>
                        </svg>
                        <h3 class="text-lg font-medium text-gray-900 mb-2">Error Loading Claims</h3>
                        <p class="text-gray-600 mb-4">{{ claimStore.errorMessage }}</p>
                        <BaseButton class="bg-green-600 hover:bg-green-700" @click="fetchClaims">
                            Try Again
                        </BaseButton>
                    </div>
                </div>
            </BaseCard>

            <!-- Claims Table -->
            <div v-else class="bg-white rounded-lg border border-gray-200 shadow-sm flex flex-col flex-1 min-h-0 overflow-hidden">
                <!-- Filter Panel -->
                <div v-if="showFilters" class="p-4 bg-gray-50 border-b border-gray-200">
                    <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                        <div>
                            <label class="block text-xs font-medium text-gray-700 mb-1">Farmer Name</label>
                            <input
                                v-model="filters.applicationName"
                                type="text"
                                placeholder="Filter by farmer name..."
                                class="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:ring-1 focus:ring-green-500 focus:border-transparent" />
                        </div>

                        <div>
                            <label class="block text-xs font-medium text-gray-700 mb-1">Farm Location</label>
                            <input
                                v-model="filters.location"
                                type="text"
                                placeholder="Filter by location..."
                                class="w-full px-3 py-2 text-sm border border-gray-300 rounded-md focus:ring-1 focus:ring-green-500 focus:border-transparent" />
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
                    </div>

                    <div class="mt-4 flex justify-end gap-2">
                        <BaseButton variant="secondary" @click="clearFilters">Clear Filters</BaseButton>
                    </div>
                </div>

                <!-- Table Header with Delete Button -->
                <div v-if="hasSelectedClaims" class="p-4 border-b border-gray-200 bg-green-50 flex justify-between items-center">
                    <p class="text-sm text-gray-700">
                    </p>
                    <BaseButton
                        class="bg-red-600 hover:bg-red-700"
                        @click="showBulkDeleteModal = true">
                        <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path>
                        </svg>
                        Delete {{ selectedClaimsCount }}
                    </BaseButton>
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
                                            :checked="selectedClaims.size === filteredClaims.length && filteredClaims.length > 0"
                                            :indeterminate="selectedClaims.size > 0 && selectedClaims.size < filteredClaims.length"
                                            class="rounded border-gray-300 text-green-600 focus:ring-green-500"
                                            @change="toggleSelectAll" />
                                    </th>
                                    <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Farmer Name</th>
                                    <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Claim Amount</th>
                                    <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Farm Location</th>
                                    <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Filed At</th>
                                    <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Damage Assessment</th>
                                </tr>
                            </thead>
                            <tbody class="bg-white divide-y divide-gray-200">
                                <tr v-for="claim in filteredClaims" :key="claim.id" class="hover:bg-gray-50 cursor-pointer transition-colors" @click="navigateToClaimDetail(claim.id)">
                                    <td class="px-4 py-4 whitespace-nowrap" @click.stop>
                                        <input
                                            type="checkbox"
                                            :checked="selectedClaims.has(claim.id)"
                                            class="rounded border-gray-300 text-green-600 focus:ring-green-500"
                                            @change="toggleClaimSelection(claim.id)" />
                                    </td>
                                    <td class="px-4 py-4 text-sm text-gray-900 max-w-xs">
                                        <div class="truncate font-medium" :title="claim.farmerName">
                                            {{ claim.farmerName || 'N/A' }}
                                        </div>
                                    </td>
                                    <td class="px-4 py-4 whitespace-nowrap text-sm font-medium text-green-600">
                                        {{ formatCurrency(claim.claimAmount) }}
                                    </td>
                                    <td class="px-4 py-4 text-sm text-gray-900 max-w-xs">
                                        <div class="truncate" :title="getFarmLocation(claim)">
                                            {{ getFarmLocation(claim) }}
                                        </div>
                                    </td>
                                    <td class="px-4 py-4 whitespace-nowrap text-sm text-gray-900">
                                        {{ formatDate(claim.filedAt) }}
                                    </td>
                                    <td class="px-4 py-4 text-sm text-gray-600 max-w-xs">
                                        <div class="truncate" :title="claim.damageAssessment">
                                            {{ claim.damageAssessment || 'N/A' }}
                                        </div>
                                    </td>
                                </tr>
                                <tr v-if="filteredClaims.length === 0">
                                    <td colspan="6" class="px-4 py-8 text-center text-gray-500">
                                        No claims found
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
                            <div v-for="claim in filteredClaims" :key="claim.id" class="p-4 cursor-pointer hover:bg-gray-50 transition-colors" @click="navigateToClaimDetail(claim.id)">
                                <div class="flex items-start justify-between mb-2">
                                    <div class="flex items-start gap-3 flex-1 min-w-0">
                                        <div @click.stop>
                                            <input
                                                type="checkbox"
                                                :checked="selectedClaims.has(claim.id)"
                                                class="mt-1 rounded border-gray-300 text-green-600 focus:ring-green-500 flex-shrink-0"
                                                @change="toggleClaimSelection(claim.id)" />
                                        </div>
                                        <div class="flex-1 min-w-0">
                                            <p class="text-sm font-medium text-gray-900">{{ claim.farmerName || 'N/A' }}</p>
                                            <p class="text-xs text-gray-600 mt-1 break-words">{{ getFarmLocation(claim) }}</p>
                                            <p class="text-xs text-gray-500 mt-1">{{ formatDate(claim.filedAt) }}</p>
                                        </div>
                                    </div>
                                    <p class="text-lg font-semibold text-green-600 whitespace-nowrap ml-2 flex-shrink-0">
                                        {{ formatCurrency(claim.claimAmount) }}
                                    </p>
                                </div>
                                <div class="ml-8">
                                    <p class="text-xs text-gray-600 truncate">
                                        {{ claim.damageAssessment || 'N/A' }}
                                    </p>
                                </div>
                            </div>
                            <div v-if="filteredClaims.length === 0" class="p-8 text-center text-gray-500">
                                No claims found
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Bulk Delete Confirmation Modal -->
        <div v-if="showBulkDeleteModal" class="fixed inset-0 bg-gray-500 bg-opacity-75 flex items-center justify-center z-50">
            <div class="bg-white rounded-lg shadow-xl max-w-md w-full mx-4">
                <div class="px-6 py-4">
                    <h3 class="text-lg font-medium text-gray-900 mb-2">Delete Multiple Claims</h3>
                    <p class="text-gray-600">Are you sure you want to delete {{ selectedClaimsCount }} claim{{ selectedClaimsCount > 1 ? 's' : '' }}? This action cannot be undone.</p>
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
                        Delete {{ selectedClaimsCount }} Claim{{ selectedClaimsCount > 1 ? 's' : '' }}
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
