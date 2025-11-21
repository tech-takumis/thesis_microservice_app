<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useVoucherStore } from '@/stores/voucher'
import { useNotificationStore } from '@/stores/notification'
import AuthenticatedLayout from '../../../layouts/AuthenticatedLayout.vue'
import LoadingSpinner from '@/components/others/LoadingSpinner.vue'
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
const showFilterModal = ref(false)
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

const openFilterModal = () => {
    showFilterModal.value = true
}

const closeFilterModal = () => {
    showFilterModal.value = false
}

const applyFilters = () => {
    closeFilterModal()
}

const clearFilters = () => {
    filters.value = {
        title: '',
        voucherType: '',
        status: '',
        code: ''
    }
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
        ISSUED: 'bg-blue-100 text-blue-800',
        CLAIMED: 'bg-green-100 text-green-800',
        EXPIRED: 'bg-gray-100 text-gray-800',
        CANCELLED: 'bg-red-100 text-red-800'
    }
    return colors[status] || 'bg-gray-100 text-gray-800'
}

const getVoucherTypeColor = (type) => {
    const colors = {
        SEEDS: 'bg-green-100 text-green-800',
        FERTILIZER: 'bg-yellow-100 text-yellow-800',
        EQUIPMENT: 'bg-purple-100 text-purple-800',
        CASH: 'bg-blue-100 text-blue-800',
        OTHER: 'bg-gray-100 text-gray-800'
    }
    return colors[type] || 'bg-gray-100 text-gray-800'
}

// Lifecycle
onMounted(() => {
    fetchVouchers()
})
</script>

<template>
    <AuthenticatedLayout>
        <!-- Header -->
        <div class="mb-6 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
            <div class="flex items-center gap-3">
                <div class="p-2 bg-green-100 rounded-lg">
                    <TicketIcon class="w-6 h-6 text-green-600" />
                </div>
                <div>
                    <h1 class="text-2xl font-bold text-gray-900">All Vouchers</h1>
                    <p class="text-sm text-gray-500 mt-1">
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
                        class="block w-full pl-10 pr-3 py-2.5 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent text-sm"
                    />
                </div>

                <!-- Filter Button -->
                <button
                    @click="openFilterModal"
                    class="inline-flex items-center gap-2 px-4 py-2.5 border border-gray-300 rounded-lg text-sm font-medium text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-green-500"
                    :class="{ 'ring-2 ring-green-500': hasActiveFilters }"
                >
                    <FunnelIcon class="w-5 h-5" />
                    <span class="hidden sm:inline">Filter</span>
                    <span v-if="hasActiveFilters" class="inline-flex items-center justify-center w-5 h-5 text-xs font-bold text-white bg-green-600 rounded-full">
                        !
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
        <div v-if="loading" class="flex justify-center items-center py-12">
            <LoadingSpinner />
        </div>

        <!-- Vouchers Table -->
        <div v-else class="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
            <!-- Table Header with Bulk Actions -->
            <div class="px-4 py-3 border-b border-gray-200 bg-gray-50 flex items-center justify-between">
                <div class="flex items-center gap-3">
                    <span class="text-sm font-medium text-gray-700">
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
            <div class="hidden md:block overflow-x-auto">
                <table class="min-w-full divide-y divide-gray-200">
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
                    <tbody class="bg-white divide-y divide-gray-200">
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
                            class="hover:bg-gray-50 cursor-pointer transition-colors"
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

        <!-- Filter Modal -->
        <Teleport to="body">
            <div
                v-if="showFilterModal"
                class="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity z-50"
                @click="closeFilterModal"
            >
                <div class="fixed inset-0 z-50 overflow-y-auto">
                    <div class="flex min-h-full items-end justify-center p-4 text-center sm:items-center sm:p-0">
                        <div
                            class="relative transform overflow-hidden rounded-lg bg-white text-left shadow-xl transition-all sm:my-8 sm:w-full sm:max-w-lg"
                            @click.stop
                        >
                            <!-- Modal Header -->
                            <div class="bg-gray-50 px-4 py-3 border-b border-gray-200 flex items-center justify-between">
                                <h3 class="text-lg font-semibold text-gray-900">Filter Vouchers</h3>
                                <button
                                    @click="closeFilterModal"
                                    class="text-gray-400 hover:text-gray-500 focus:outline-none"
                                >
                                    <XMarkIcon class="h-6 w-6" />
                                </button>
                            </div>

                            <!-- Modal Body -->
                            <div class="bg-white px-4 py-5 sm:p-6 space-y-4">
                                <!-- Title Filter -->
                                <div>
                                    <label class="block text-sm font-medium text-gray-700 mb-1">Title</label>
                                    <input
                                        v-model="filters.title"
                                        type="text"
                                        placeholder="Search by title"
                                        class="block w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent text-sm"
                                    />
                                </div>

                                <!-- Voucher Code Filter -->
                                <div>
                                    <label class="block text-sm font-medium text-gray-700 mb-1">Voucher Code</label>
                                    <input
                                        v-model="filters.code"
                                        type="text"
                                        placeholder="Search by code"
                                        class="block w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent text-sm"
                                    />
                                </div>

                                <!-- Voucher Type Filter -->
                                <div>
                                    <label class="block text-sm font-medium text-gray-700 mb-1">Voucher Type</label>
                                    <select
                                        v-model="filters.voucherType"
                                        class="block w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent text-sm"
                                    >
                                        <option value="">All Types</option>
                                        <option value="SEEDS">Seeds</option>
                                        <option value="FERTILIZER">Fertilizer</option>
                                        <option value="EQUIPMENT">Equipment</option>
                                        <option value="CASH">Cash</option>
                                        <option value="OTHER">Other</option>
                                    </select>
                                </div>

                                <!-- Status Filter -->
                                <div>
                                    <label class="block text-sm font-medium text-gray-700 mb-1">Status</label>
                                    <select
                                        v-model="filters.status"
                                        class="block w-full px-3 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent text-sm"
                                    >
                                        <option value="">All Statuses</option>
                                        <option value="ISSUED">Issued</option>
                                        <option value="CLAIMED">Claimed</option>
                                        <option value="EXPIRED">Expired</option>
                                        <option value="CANCELLED">Cancelled</option>
                                    </select>
                                </div>
                            </div>

                            <!-- Modal Footer -->
                            <div class="bg-gray-50 px-4 py-3 sm:flex sm:flex-row-reverse gap-3">
                                <button
                                    @click="applyFilters"
                                    class="inline-flex w-full justify-center rounded-lg bg-green-600 px-4 py-2 text-sm font-semibold text-white shadow-sm hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-green-500 sm:w-auto"
                                >
                                    Apply Filters
                                </button>
                                <button
                                    @click="clearFilters"
                                    class="mt-3 inline-flex w-full justify-center rounded-lg bg-white px-4 py-2 text-sm font-semibold text-gray-900 shadow-sm ring-1 ring-inset ring-gray-300 hover:bg-gray-50 sm:mt-0 sm:w-auto"
                                >
                                    Clear All
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </Teleport>

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
</style>
