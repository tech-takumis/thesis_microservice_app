<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useVoucherStore } from '@/stores/voucher'
import { useNotificationStore } from '@/stores/notification'
import AuthenticatedLayout from '../../../layouts/AuthenticatedLayout.vue'
import LoadingSpinner from '@/components/others/LoadingSpinner.vue'
import BaseButton from '@/components/buttons/BaseButton.vue'
import {
    MagnifyingGlassIcon,
    FunnelIcon,
    TrashIcon,
    XMarkIcon,
    PlusIcon,
    TicketIcon
} from '@heroicons/vue/24/outline'

const router = useRouter()
const voucherStore = useVoucherStore()
const notificationStore = useNotificationStore()

// State
const loading = ref(false)
const searchQuery = ref('')
const showFilters = ref(false)
const showBulkDeleteModal = ref(false)
const selectedVouchers = ref(new Set())

// Filters
const filters = ref({
    title: '',
    voucherType: '',
    status: '',
    code: ''
})

// Computed
const filteredVouchers = computed(() => {
    let filtered = voucherStore.allVouchers

    // Apply search filter
    if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        filtered = filtered.filter(voucher =>
            voucher.title?.toLowerCase().includes(query) ||
            voucher.code?.toLowerCase().includes(query) ||
            voucher.referenceNumber?.toLowerCase().includes(query) ||
            voucher.voucherType?.toLowerCase().includes(query)
        )
    }

    // Apply filters
    if (filters.value.title) {
        filtered = filtered.filter(v =>
            v.title?.toLowerCase().includes(filters.value.title.toLowerCase())
        )
    }

    if (filters.value.voucherType) {
        filtered = filtered.filter(v => v.voucherType === filters.value.voucherType)
    }

    if (filters.value.status) {
        filtered = filtered.filter(v => v.status === filters.value.status)
    }

    if (filters.value.code) {
        filtered = filtered.filter(v =>
            v.code?.toLowerCase().includes(filters.value.code.toLowerCase())
        )
    }

    return filtered
})

const hasActiveFilters = computed(() => {
    return filters.value.title || filters.value.voucherType ||
           filters.value.status || filters.value.code
})

const allSelected = computed(() => {
    return filteredVouchers.value.length > 0 &&
           filteredVouchers.value.every(v => selectedVouchers.value.has(v.id))
})

// Methods
const fetchVouchers = async () => {
    loading.value = true
    try {
        const result = await voucherStore.getAllVouchers()
        if (!result.success) {
            notificationStore.showError(result.message)
        }
    } catch (error) {
        console.error('Error fetching vouchers:', error)
        notificationStore.showError('Failed to load vouchers')
    } finally {
        loading.value = false
    }
}

const toggleVoucherSelection = (voucherId) => {
    if (selectedVouchers.value.has(voucherId)) {
        selectedVouchers.value.delete(voucherId)
    } else {
        selectedVouchers.value.add(voucherId)
    }
}

const toggleSelectAll = () => {
    if (allSelected.value) {
        selectedVouchers.value.clear()
    } else {
        filteredVouchers.value.forEach(v => selectedVouchers.value.add(v.id))
    }
}

const openBulkDeleteModal = () => {
    showBulkDeleteModal.value = true
}

const closeBulkDeleteModal = () => {
    showBulkDeleteModal.value = false
}

const handleBulkDelete = async () => {
    try {
        const count = selectedVouchers.value.size
        const promises = Array.from(selectedVouchers.value).map(id =>
            voucherStore.deleteVoucher(id)
        )

        await Promise.all(promises)
        selectedVouchers.value = new Set()
        showBulkDeleteModal.value = false
        notificationStore.showSuccess(`${count} voucher${count > 1 ? 's' : ''} deleted successfully!`)
    } catch (error) {
        console.error('Error deleting vouchers:', error)
        notificationStore.showError('Failed to delete some vouchers. Please try again.')
    }
}

const navigateToVoucherDetail = (voucherId) => {
    router.push({
        name: 'agriculturist-voucher-detail',
        params: { id: voucherId }
    })
}

const navigateToGenerateVoucher = () => {
    router.push({ name: 'agriculturist-voucher-generate' })
}

const clearFilters = () => {
    filters.value = {
        title: '',
        voucherType: '',
        status: '',
        code: ''
    }
    closeVoucherTypeDropdown()
    closeStatusDropdown()
}

// Voucher Type dropdown options
const voucherTypeOptions = [
    { label: 'All Types', value: '' },
    { label: 'Seeds', value: 'SEEDS' },
    { label: 'Fertilizer', value: 'FERTILIZER' },
    { label: 'Equipment', value: 'EQUIPMENT' },
    { label: 'Cash', value: 'CASH' },
    { label: 'Other', value: 'OTHER' }
]

// Status dropdown options
const statusOptions = [
    { label: 'All Statuses', value: '' },
    { label: 'Issued', value: 'ISSUED' },
    { label: 'Claimed', value: 'CLAIMED' },
    { label: 'Expired', value: 'EXPIRED' },
    { label: 'Cancelled', value: 'CANCELLED' }
]

// Custom dropdown state for voucher type
const isVoucherTypeDropdownOpen = ref(false)
const highlightedVoucherTypeIndex = ref(-1)

const toggleVoucherTypeDropdown = () => {
    isVoucherTypeDropdownOpen.value = !isVoucherTypeDropdownOpen.value
    if (isVoucherTypeDropdownOpen.value) {
        highlightedVoucherTypeIndex.value = voucherTypeOptions.findIndex(opt => opt.value === filters.value.voucherType)
    }
}

const closeVoucherTypeDropdown = () => {
    isVoucherTypeDropdownOpen.value = false
    highlightedVoucherTypeIndex.value = -1
}

const selectVoucherType = (value) => {
    filters.value.voucherType = value
    closeVoucherTypeDropdown()
}

const onVoucherTypeKeyDown = (e) => {
    if (!isVoucherTypeDropdownOpen.value && (e.key === 'Enter' || e.key === ' ')) {
        e.preventDefault()
        toggleVoucherTypeDropdown()
        return
    }
    if (isVoucherTypeDropdownOpen.value) {
        if (e.key === 'ArrowDown') {
            e.preventDefault()
            highlightedVoucherTypeIndex.value = Math.min(highlightedVoucherTypeIndex.value + 1, voucherTypeOptions.length - 1)
        } else if (e.key === 'ArrowUp') {
            e.preventDefault()
            highlightedVoucherTypeIndex.value = Math.max(highlightedVoucherTypeIndex.value - 1, 0)
        } else if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault()
            if (highlightedVoucherTypeIndex.value >= 0) selectVoucherType(voucherTypeOptions[highlightedVoucherTypeIndex.value].value)
        } else if (e.key === 'Escape') {
            e.preventDefault()
            closeVoucherTypeDropdown()
        }
    }
}

const selectedVoucherTypeDisplay = computed(() => {
    const option = voucherTypeOptions.find(opt => opt.value === filters.value.voucherType)
    return option ? option.label : 'All Types'
})

// Custom dropdown state for status
const isStatusDropdownOpen = ref(false)
const highlightedStatusIndex = ref(-1)

const toggleStatusDropdown = () => {
    isStatusDropdownOpen.value = !isStatusDropdownOpen.value
    if (isStatusDropdownOpen.value) {
        highlightedStatusIndex.value = statusOptions.findIndex(opt => opt.value === filters.value.status)
    }
}

const closeStatusDropdown = () => {
    isStatusDropdownOpen.value = false
    highlightedStatusIndex.value = -1
}

const selectStatus = (value) => {
    filters.value.status = value
    closeStatusDropdown()
}

const onStatusKeyDown = (e) => {
    if (!isStatusDropdownOpen.value && (e.key === 'Enter' || e.key === ' ')) {
        e.preventDefault()
        toggleStatusDropdown()
        return
    }
    if (isStatusDropdownOpen.value) {
        if (e.key === 'ArrowDown') {
            e.preventDefault()
            highlightedStatusIndex.value = Math.min(highlightedStatusIndex.value + 1, statusOptions.length - 1)
        } else if (e.key === 'ArrowUp') {
            e.preventDefault()
            highlightedStatusIndex.value = Math.max(highlightedStatusIndex.value - 1, 0)
        } else if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault()
            if (highlightedStatusIndex.value >= 0) selectStatus(statusOptions[highlightedStatusIndex.value].value)
        } else if (e.key === 'Escape') {
            e.preventDefault()
            closeStatusDropdown()
        }
    }
}

const selectedStatusDisplay = computed(() => {
    const option = statusOptions.find(opt => opt.value === filters.value.status)
    return option ? option.label : 'All Statuses'
})

// Close on outside click
const onClickOutside = (e) => {
    const voucherTypeEl = document.querySelector('#voucher-type-dropdown')
    const statusEl = document.querySelector('#status-dropdown')
    
    if (voucherTypeEl && !voucherTypeEl.contains(e.target)) closeVoucherTypeDropdown()
    if (statusEl && !statusEl.contains(e.target)) closeStatusDropdown()
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

const getStatusColor = (status) => {
    const colors = {
        ISSUED: 'bg-yellow-100 text-yellow-800',
        CLAIMED: 'bg-green-100 text-green-800',
        EXPIRED: 'bg-red-100 text-red-800',
        CANCELLED: 'bg-red-100 text-red-800'
    }
    return colors[status] || 'bg-gray-100 text-gray-800'
}

const getVoucherTypeColor = (type) => {
    const colors = {
        SEEDS: 'bg-green-100 text-green-800',
        FERTILIZER: 'bg-yellow-100 text-yellow-800',
        EQUIPMENT: 'bg-gray-200 text-green-600',
        CASH: 'bg-green-100 text-green-800',
        OTHER: 'bg-gray-100 text-gray-800'
    }
    return colors[type] || 'bg-gray-100 text-gray-800'
}

// Lifecycle
onMounted(() => {
    fetchVouchers()
    document.addEventListener('click', onClickOutside)
})

onBeforeUnmount(() => {
    document.removeEventListener('click', onClickOutside)
})
</script>

<template>
    <AuthenticatedLayout>
        <!-- Header -->
        <div class="mb-3 mt-2 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-3 ml-4">
            <div class="flex items-center gap-3">
                <div>
                    <h1 class="text-3xl font-bold text-green-600">All Vouchers</h1>
                    <p class="mt-1 text-sm text-gray-600">
                        Manage and track all vouchers
                    </p>
                </div>
            </div>

            <div class="flex flex-wrap items-center gap-3">
                <!-- Search -->
                <div class="relative flex-1 sm:flex-initial sm:min-w-[280px]">
                    <div class="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
                        <MagnifyingGlassIcon class="h-5 w-5 text-gray-400" />
                    </div>
                    <input
                        v-model="searchQuery"
                        type="text"
                        placeholder="Search vouchers..."
                        class="block w-full pl-10 pr-3 py-2.5 border border-gray-300 rounded-lg focus:ring-1 focus:ring-green-500 focus:border-transparent text-sm"
                    />
                </div>

                <!-- Filter Button -->
                <button
                    class="inline-flex items-center px-3 py-2 rounded-lg text-sm font-medium text-gray-700 bg-white border border-gray-300 shadow-sm hover:bg-green-600 hover:text-white focus:ring-2 focus:ring-offset-2 focus:ring-green-500 transition-all duration-300 ease-in-out"
                    @click="showFilters = !showFilters"
                >
                    <FunnelIcon class="w-4 h-4 mr-1" />
                    Filters
                    <span
                        v-if="hasActiveFilters"
                        class="ml-1 px-2 py-0.5 bg-green-600 text-white text-xs rounded-full"
                    >
                        Active
                    </span>
                </button>

                <!-- Generate Voucher Button -->
                <button
                    @click="navigateToGenerateVoucher"
                    class="inline-flex items-center gap-2 px-4 py-2.5 bg-green-600 text-white rounded-lg text-sm font-medium hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-green-500"
                >
                    <PlusIcon class="w-5 h-5" />
                    <span class="hidden sm:inline">Generate Voucher</span>
                </button>
            </div>
        </div>

        <!-- Loading State -->
        <div
            v-if="loading"
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

        <!-- Vouchers Table -->
        <div v-else class="bg-gray-100 rounded-lg border border-gray-200 shadow-sm flex flex-col flex-1 min-h-0 overflow-hidden">
            <!-- Filter Panel -->
            <div v-if="showFilters" class="p-4 bg-gray-50 border-b border-gray-300">
                <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                    <!-- Title Filter -->
                    <div>
                        <label class="block text-xs font-medium text-gray-700 mb-1">Title</label>
                        <input
                            v-model="filters.title"
                            type="text"
                            placeholder="Search by title"
                            class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:ring-1 focus:ring-green-500 focus:border-transparent"
                        />
                    </div>

                    <!-- Voucher Code Filter -->
                    <div>
                        <label class="block text-xs font-medium text-gray-700 mb-1">Voucher Code</label>
                        <input
                            v-model="filters.code"
                            type="text"
                            placeholder="Search by code"
                            class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg focus:ring-1 focus:ring-green-500 focus:border-transparent"
                        />
                    </div>

                    <!-- Voucher Type Filter -->
                    <div>
                        <label class="block text-xs font-medium text-gray-700 mb-1">Voucher Type</label>
                        <div id="voucher-type-dropdown" class="relative">
                            <button
                                type="button"
                                @click="toggleVoucherTypeDropdown"
                                @keydown.stop.prevent="onVoucherTypeKeyDown"
                                :aria-expanded="isVoucherTypeDropdownOpen"
                                aria-haspopup="listbox"
                                class="w-full flex items-center justify-between px-3 py-2 text-sm border border-gray-300 rounded-xl bg-white shadow-sm focus:ring-1 focus:ring-green-500 focus:border-transparent"
                            >
                                <span class="text-gray-900">{{ selectedVoucherTypeDisplay }}</span>
                                <svg
                                    class="w-4 h-4 text-green-600 transform transition-transform duration-200"
                                    :class="isVoucherTypeDropdownOpen ? 'rotate-180' : ''"
                                    viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg"
                                    aria-hidden="true"
                                >
                                    <path d="M6 8l4 4 4-4" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/>
                                </svg>
                            </button>
                            <ul
                                v-show="isVoucherTypeDropdownOpen"
                                role="listbox"
                                tabindex="-1"
                                class="origin-top-right absolute right-0 left-0 mt-2 bg-white rounded-xl shadow-lg ring-1 ring-black ring-opacity-5 overflow-auto max-h-56 py-1 focus:outline-none z-50 transition-transform duration-150"
                            >
                                <li
                                    v-for="(option, idx) in voucherTypeOptions"
                                    :key="option.value"
                                    role="option"
                                    :aria-selected="option.value === filters.voucherType"
                                    @mouseenter="highlightedVoucherTypeIndex = idx"
                                    @mouseleave="highlightedVoucherTypeIndex = -1"
                                    @click="selectVoucherType(option.value)"
                                    :class=" [
                                        'px-3 py-2 cursor-pointer flex items-center justify-between text-sm',
                                        highlightedVoucherTypeIndex === idx ? 'bg-green-50' : 'hover:bg-green-50',
                                        option.value === filters.voucherType ? 'font-semibold text-green-700' : 'text-gray-700'
                                    ]"
                                >
                                    <span>{{ option.label }}</span>
                                    <svg v-if="option.value === filters.voucherType" class="w-4 h-4 text-green-600" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                                        <path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                                    </svg>
                                </li>
                            </ul>
                        </div>
                    </div>

                    <!-- Status Filter -->
                    <div>
                        <label class="block text-xs font-medium text-gray-700 mb-1">Status</label>
                        <div id="status-dropdown" class="relative">
                            <button
                                type="button"
                                @click="toggleStatusDropdown"
                                @keydown.stop.prevent="onStatusKeyDown"
                                :aria-expanded="isStatusDropdownOpen"
                                aria-haspopup="listbox"
                                class="w-full flex items-center justify-between px-3 py-2 text-sm border border-gray-300 rounded-xl bg-white shadow-sm focus:ring-1 focus:ring-green-500 focus:border-transparent"
                            >
                                <span class="text-gray-900">{{ selectedStatusDisplay }}</span>
                                <svg
                                    class="w-4 h-4 text-green-600 transform transition-transform duration-200"
                                    :class="isStatusDropdownOpen ? 'rotate-180' : ''"
                                    viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg"
                                    aria-hidden="true"
                                >
                                    <path d="M6 8l4 4 4-4" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/>
                                </svg>
                            </button>
                            <ul
                                v-show="isStatusDropdownOpen"
                                role="listbox"
                                tabindex="-1"
                                class="origin-top-right absolute right-0 left-0 mt-2 bg-white rounded-xl shadow-lg ring-1 ring-black ring-opacity-5 overflow-auto max-h-56 py-1 focus:outline-none z-50 transition-transform duration-150"
                            >
                                <li
                                    v-for="(option, idx) in statusOptions"
                                    :key="option.value"
                                    role="option"
                                    :aria-selected="option.value === filters.status"
                                    @mouseenter="highlightedStatusIndex = idx"
                                    @mouseleave="highlightedStatusIndex = -1"
                                    @click="selectStatus(option.value)"
                                    :class=" [
                                        'px-3 py-2 cursor-pointer flex items-center justify-between text-sm',
                                        highlightedStatusIndex === idx ? 'bg-green-50' : 'hover:bg-green-50',
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
                </div>

                <div class="mt-4 flex justify-end gap-2">
                    <button
                        @click="clearFilters"
                        class="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md shadow-sm hover:bg-red-600 hover:text-white transition"
                    >
                        Reset
                    </button>
                </div>
            </div>

            <!-- Table Header with Bulk Actions -->
            <div class="px-4 py-3 border-b border-gray-300 bg-gray-100 flex items-center justify-between">
                <div class="flex items-center gap-3">
                    <span class="text-sm font-medium text-green-600">
                        {{ filteredVouchers.length }} voucher{{ filteredVouchers.length !== 1 ? 's' : '' }}
                    </span>
                    <span v-if="selectedVouchers.size > 0" class="text-sm text-gray-500">
                        ({{ selectedVouchers.size }} selected)
                    </span>
                </div>

                <!-- Delete Button (shown when selections exist) -->
                <button
                    v-if="selectedVouchers.size > 0"
                    @click="openBulkDeleteModal"
                    class="inline-flex items-center gap-2 px-3 py-1.5 bg-red-600 text-white rounded-md text-sm font-medium hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-500"
                >
                    <TrashIcon class="w-4 h-4" />
                    Delete Selected
                </button>
            </div>

            <!-- Desktop Table View -->
            <div class="hidden md:block overflow-x-auto flex-1 min-h-0">
                <table class="min-w-full divide-y divide-gray-300">
                    <thead class="bg-gray-50">
                        <tr>
                            <th scope="col" class="px-4 py-3 text-left">
                                <input
                                    type="checkbox"
                                    :checked="allSelected"
                                    @change="toggleSelectAll"
                                    class="rounded border-gray-300 text-green-600 focus:ring-green-500"
                                />
                            </th>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Title
                            </th>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Type
                            </th>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Quantity
                            </th>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Issue Date
                            </th>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Expiry Date
                            </th>
                            <th scope="col" class="px-4 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                                Status
                            </th>
                        </tr>
                    </thead>
                    <tbody class="bg-white divide-y divide-gray-300">
                        <tr
                            v-if="filteredVouchers.length === 0"
                            class="hover:bg-gray-50"
                        >
                            <td colspan="8" class="px-4 py-8 text-center text-sm text-gray-500">
                                No vouchers found
                            </td>
                        </tr>
                        <tr
                            v-for="voucher in filteredVouchers"
                            :key="voucher.id"
                            class="hover:bg-green-50 cursor-pointer transition-colors"
                            @click="navigateToVoucherDetail(voucher.id)"
                        >
                            <td class="px-4 py-4 whitespace-nowrap" @click.stop>
                                <input
                                    type="checkbox"
                                    :checked="selectedVouchers.has(voucher.id)"
                                    @change="toggleVoucherSelection(voucher.id)"
                                    class="rounded border-gray-300 text-green-600 focus:ring-green-500"
                                />
                            </td>
                            <td class="px-4 py-4 text-sm text-gray-900 max-w-xs">
                                <div class="truncate font-medium" :title="voucher.title">
                                    {{ voucher.title || 'N/A' }}
                                </div>
                            </td>
                            <td class="px-4 py-4 whitespace-nowrap">
                                <span
                                    class="inline-flex px-2 py-1 text-xs font-semibold rounded-full"
                                    :class="getVoucherTypeColor(voucher.voucherType)"
                                >
                                    {{ voucher.voucherType }}
                                </span>
                            </td>
                            <td class="px-4 py-4 whitespace-nowrap text-sm text-gray-900">
                                {{ voucher.quantity }} {{ voucher.unit }}
                            </td>
                            <td class="px-4 py-4 whitespace-nowrap text-sm text-gray-900">
                                {{ formatDate(voucher.issueDate) }}
                            </td>
                            <td class="px-4 py-4 whitespace-nowrap text-sm text-gray-900">
                                {{ formatDate(voucher.expiryDate) }}
                            </td>
                            <td class="px-4 py-4 whitespace-nowrap">
                                <span
                                    class="inline-flex px-2 py-1 text-xs font-semibold rounded-full"
                                    :class="getStatusColor(voucher.status)"
                                >
                                    {{ voucher.status }}
                                </span>
                            </td>
                        </tr>
                    </tbody>
                </table>
            </div>

            <!-- Mobile Card View -->
            <div class="md:hidden divide-y divide-gray-200">
                <div v-if="filteredVouchers.length === 0" class="px-4 py-8 text-center text-sm text-gray-500">
                    No vouchers found
                </div>
                <div
                    v-for="voucher in filteredVouchers"
                    :key="voucher.id"
                    class="p-4 hover:bg-gray-50 cursor-pointer transition-colors"
                    @click="navigateToVoucherDetail(voucher.id)"
                >
                    <div class="flex items-start gap-3" @click.stop>
                        <input
                            type="checkbox"
                            :checked="selectedVouchers.has(voucher.id)"
                            @change="toggleVoucherSelection(voucher.id)"
                            class="mt-1 rounded border-gray-300 text-green-600 focus:ring-green-500"
                        />
                        <div class="flex-1 min-w-0">
                            <div class="flex items-start justify-between gap-2 mb-2">
                                <div>
                                    <p class="text-sm font-medium text-gray-900">{{ voucher.code }}</p>
                                    <p class="text-xs text-gray-500 mt-0.5">{{ voucher.title }}</p>
                                </div>
                                <span
                                    class="inline-flex px-2 py-1 text-xs font-semibold rounded-full whitespace-nowrap"
                                    :class="getStatusColor(voucher.status)"
                                >
                                    {{ voucher.status }}
                                </span>
                            </div>
                            <div class="space-y-1">
                                <div class="flex items-center justify-between text-xs">
                                    <span class="text-gray-500">Type:</span>
                                    <span
                                        class="inline-flex px-2 py-0.5 font-semibold rounded-full"
                                        :class="getVoucherTypeColor(voucher.voucherType)"
                                    >
                                        {{ voucher.voucherType }}
                                    </span>
                                </div>
                                <div class="flex items-center justify-between text-xs">
                                    <span class="text-gray-500">Quantity:</span>
                                    <span class="text-gray-900 font-medium">{{ voucher.quantity }} {{ voucher.unit }}</span>
                                </div>
                                <div class="flex items-center justify-between text-xs">
                                    <span class="text-gray-500">Issue Date:</span>
                                    <span class="text-gray-900">{{ formatDate(voucher.issueDate) }}</span>
                                </div>
                                <div class="flex items-center justify-between text-xs">
                                    <span class="text-gray-500">Expiry Date:</span>
                                    <span class="text-gray-900">{{ formatDate(voucher.expiryDate) }}</span>
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Bulk Delete Confirmation Modal -->
        <Teleport to="body">
            <div
                v-if="showBulkDeleteModal"
                class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity z-50"
                @click="closeBulkDeleteModal"
            >
                <div class="fixed inset-0 z-50 overflow-y-auto">
                    <div class="flex min-h-full items-end justify-center p-4 text-center sm:items-center sm:p-0">
                        <div
                            class="relative transform overflow-hidden rounded-lg bg-white text-left shadow-xl transition-all sm:my-8 sm:w-full sm:max-w-lg"
                            @click.stop
                        >
                            <div class="bg-white px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
                                <div class="sm:flex sm:items-start">
                                    <div class="mx-auto flex h-12 w-12 flex-shrink-0 items-center justify-center rounded-full bg-red-100 sm:mx-0 sm:h-10 sm:w-10">
                                        <TrashIcon class="h-6 w-6 text-red-600" />
                                    </div>
                                    <div class="mt-3 text-center sm:ml-4 sm:mt-0 sm:text-left">
                                        <h3 class="text-lg font-semibold leading-6 text-gray-900">
                                            Delete Vouchers
                                        </h3>
                                        <div class="mt-2">
                                            <p class="text-sm text-gray-500">
                                                Are you sure you want to delete {{ selectedVouchers.size }} voucher{{ selectedVouchers.size > 1 ? 's' : '' }}?
                                                This action cannot be undone.
                                            </p>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div class="bg-gray-50 px-4 py-3 sm:flex sm:flex-row-reverse sm:px-6 gap-3">
                                <button
                                    @click="handleBulkDelete"
                                    class="inline-flex w-full justify-center rounded-lg bg-red-600 px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-500 sm:w-auto"
                                >
                                    Delete
                                </button>
                                <button
                                    @click="closeBulkDeleteModal"
                                    class="mt-3 inline-flex w-full justify-center rounded-lg bg-white px-4 py-2 text-sm font-semibold text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-gray-50 sm:mt-0 sm:w-auto"
                                >
                                    Cancel
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </Teleport>
    </AuthenticatedLayout>
</template>

<style scoped>
/* Custom scrollbar styling */
::-webkit-scrollbar {
    width: 8px;
    height: 8px;
}

::-webkit-scrollbar-track {
    background: #f1f1f1;
    border-radius: 4px;
}

::-webkit-scrollbar-thumb {
    background: #cbd5e0;
    border-radius: 4px;
}

::-webkit-scrollbar-thumb:hover {
    background: #a0aec0;
}

/* Custom dropdown & UI polish */
::selection {
  background-color: rgba(16, 185, 129, 0.12); /* soft green selection */
}

#voucher-type-dropdown button,
#status-dropdown button {
  -webkit-tap-highlight-color: transparent;
}

#voucher-type-dropdown ul,
#status-dropdown ul {
  -webkit-overflow-scrolling: touch;
}

#voucher-type-dropdown li,
#status-dropdown li {
  transition: background-color 160ms ease, color 160ms ease;
}

#voucher-type-dropdown svg,
#status-dropdown svg {
  transition: transform 200ms ease;
}
</style>
