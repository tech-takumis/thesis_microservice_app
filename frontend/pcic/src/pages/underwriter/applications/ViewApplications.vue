<template>
    <AuthenticatedLayout
        :navigation="underwriterNavigation"
        role-title="Underwriter Portal"
        page-title="All Applications">
        <template #header>
            <div class="flex items-center justify-between w-full">
                <!-- Left: Logo + Title -->
                <div class="flex items-center space-x-3">
                    <!-- Title -->
                    <div>
                        <h1 class="text-2xl font-light text-slate-900 tracking-tight">
                            All Applications
                        </h1>
                        <p class="text-sm text-slate-600">
                            Review and manage crop insurance applications
                        </p>
                    </div>
                </div>
            </div>
        </template>

        <div class="flex flex-col h-full">
            <!-- Controls Section -->
            <div class="flex-shrink-0 space-y-4 mb-4">
                <div class="flex items-center justify-between space-x-4">
                    <!-- Left: Search -->
                    <div class="relative w-64">
                        <Search
                            class="absolute left-3 top-1/2 transform -translate-y-1/2 h-4 w-4 text-slate-400" />
                        <input
                            v-model="searchQuery"
                            type="text"
                            placeholder="Search by name, crop, location, ID..."
                            class="w-full pl-9 pr-4 py-2 rounded-lg border-2 border-slate-200 bg-white text-slate-900 text-sm font-medium placeholder:text-slate-400 focus:border-blue-400 focus:bg-blue-50/30 focus:outline-none focus:ring-4 focus:ring-blue-400/10 hover:border-slate-300 transition-all duration-200" />
                    </div>

                    <!-- Right: Filter Toggle + Refresh -->
                    <div class="flex items-center space-x-3">
                        <!-- Filter Toggle Button -->
                        <button
                            :class="[
                                'inline-flex items-center px-4 py-2 border-2 rounded-xl text-sm font-medium transition-all duration-200',
                                showFilters
                                    ? 'border-blue-400 bg-blue-50 text-blue-700'
                                    : 'border-slate-200 bg-white text-slate-700 hover:bg-slate-50 hover:border-slate-300',
                            ]"
                            @click="toggleFilters">
                            <Filter class="h-4 w-4 mr-2" />
                            Filters
                            <ChevronDown
                                :class="[
                                    'h-4 w-4 ml-2 transition-transform duration-200',
                                    showFilters ? 'rotate-180' : '',
                                ]" />
                        </button>

                        <!-- Refresh Button -->
                        <button
                            :disabled="loading"
                            class="inline-flex items-center px-4 py-2 border border-transparent rounded-xl text-sm font-medium text-white bg-blue-500 hover:bg-blue-600 hover:shadow-lg hover:shadow-blue-500/30 disabled:opacity-50 disabled:hover:shadow-none transition-all duration-200"
                            @click="refreshApplications">
                            <RefreshCw
                                :class="[
                                    'h-4 w-4 mr-2',
                                    loading ? 'animate-spin' : '',
                                ]" />
                            Refresh
                        </button>
                    </div>
                </div>

                <!-- Advanced Filters Panel -->
                <div
                    v-if="showFilters"
                    class="bg-slate-50/50 border border-slate-200/60 rounded-2xl p-5 backdrop-blur-sm">
                    <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
                        <!-- Status Filter -->
                        <div>
                            <label
                                class="block text-xs font-semibold text-slate-700 mb-2 uppercase tracking-wider"
                                >Status</label
                            >
                            <select
                                v-model="selectedStatus"
                                class="w-full px-4 py-2 rounded-lg border-2 border-slate-200 bg-white text-slate-900 text-sm font-medium focus:border-blue-400 focus:bg-blue-50/30 focus:outline-none focus:ring-4 focus:ring-blue-400/10 hover:border-slate-300 transition-all duration-200">
                                <option value="">All Status</option>
                                <option value="PENDING">Pending</option>
                                <option value="APPROVED">Approved</option>
                                <option value="REJECTED">Rejected</option>
                            </select>
                        </div>

                        <!-- Crop Filter -->
                        <div>
                            <label
                                class="block text-xs font-semibold text-slate-700 mb-2 uppercase tracking-wider"
                                >Crop Type</label
                            >
                            <select
                                v-model="selectedCrop"
                                class="w-full px-4 py-2 rounded-lg border-2 border-slate-200 bg-white text-slate-900 text-sm font-medium focus:border-blue-400 focus:bg-blue-50/30 focus:outline-none focus:ring-4 focus:ring-blue-400/10 hover:border-slate-300 transition-all duration-200">
                                <option value="">All Crops</option>
                                <option
                                    v-for="crop in availableCrops"
                                    :key="crop"
                                    :value="crop">
                                    {{ crop }}
                                </option>
                            </select>
                        </div>

                        <!-- Location Filter -->
                        <div>
                            <label
                                class="block text-xs font-semibold text-slate-700 mb-2 uppercase tracking-wider"
                                >Location</label
                            >
                            <select
                                v-model="selectedLocation"
                                class="w-full px-4 py-2 rounded-lg border-2 border-slate-200 bg-white text-slate-900 text-sm font-medium focus:border-blue-400 focus:bg-blue-50/30 focus:outline-none focus:ring-4 focus:ring-blue-400/10 hover:border-slate-300 transition-all duration-200">
                                <option value="">All Locations</option>
                                <option
                                    v-for="location in availableLocations"
                                    :key="location"
                                    :value="location">
                                    {{ location }}
                                </option>
                            </select>
                        </div>

                        <!-- Date From Filter -->
                        <div>
                            <label
                                class="block text-xs font-semibold text-slate-700 mb-2 uppercase tracking-wider"
                                >Verified From</label
                            >
                            <input
                                v-model="dateFrom"
                                type="date"
                                class="w-full px-4 py-2 rounded-lg border-2 border-slate-200 bg-white text-slate-900 text-sm font-medium focus:border-blue-400 focus:bg-blue-50/30 focus:outline-none focus:ring-4 focus:ring-blue-400/10 hover:border-slate-300 transition-all duration-200" />
                        </div>

                        <!-- Date To Filter -->
                        <div>
                            <label
                                class="block text-xs font-semibold text-slate-700 mb-2 uppercase tracking-wider"
                                >Verified To</label
                            >
                            <input
                                v-model="dateTo"
                                type="date"
                                class="w-full px-4 py-2 rounded-lg border-2 border-slate-200 bg-white text-slate-900 text-sm font-medium focus:border-blue-400 focus:bg-blue-50/30 focus:outline-none focus:ring-4 focus:ring-blue-400/10 hover:border-slate-300 transition-all duration-200" />
                        </div>

                        <!-- Clear Filters Button -->
                        <div class="flex items-end">
                            <button
                                class="w-full px-4 py-2 border-2 border-slate-200 rounded-lg text-sm font-medium text-slate-700 bg-white hover:bg-slate-50 hover:border-slate-300 transition-all duration-200"
                                @click="clearAllFilters">
                                Clear All
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            <!-- Applications List - Takes remaining space -->
            <div class="bg-white/70 backdrop-blur-sm rounded-2xl border border-slate-200/60 shadow-sm flex flex-col flex-1 min-h-0 overflow-hidden">
                <!-- Header -->
                <div
                    class="flex items-center justify-between px-6 py-4 border-b border-slate-100/80 flex-shrink-0">
                    <div class="flex items-center space-x-3">
                        <h2
                            class="text-sm font-medium text-slate-700">
                            Applications
                        </h2>
                        <!-- Delete button - shown when items are selected -->
                        <button
                            v-if="selectedInsuranceIds.length > 0"
                            @click="handleBulkDelete"
                            :disabled="deleting"
                            class="inline-flex items-center px-3 py-1.5 border border-transparent rounded-lg text-xs font-medium text-white bg-red-500 hover:bg-red-600 hover:shadow-lg hover:shadow-red-500/30 disabled:opacity-50 disabled:hover:shadow-none transition-all duration-200">
                            <Trash2 class="h-3.5 w-3.5 mr-1.5" />
                            Delete {{ selectedInsuranceIds.length }}
                            {{
                                selectedInsuranceIds.length === 1 ? 'item' : 'items'
                            }}
                        </button>
                    </div>
                    <span class="text-xs text-slate-600 font-medium"
                        >{{ filteredApplications.length }} total</span
                    >
                </div>

                <!-- Table Container - Scrollable -->
                <div class="flex-1 min-h-0 overflow-hidden">
                    <div class="overflow-y-auto h-full">
                        <table class="min-w-full">
                            <thead
                                class="bg-slate-50 border-b border-slate-200 sticky top-0 z-10">
                                <tr>
                                    <th class="px-6 py-3 text-left">
                                        <input
                                            type="checkbox"
                                            :checked="allSelected"
                                            @change="toggleSelectAll"
                                            class="h-4 w-4 text-blue-500 focus:ring-blue-400 focus:ring-2 focus:ring-blue-400/20 border-slate-300 rounded cursor-pointer transition-all duration-200" />
                                    </th>
                                    <th
                                        class="px-6 py-3 text-left text-xs font-semibold text-slate-600 uppercase tracking-wider">
                                        Application
                                    </th>
                                    <th
                                        class="px-6 py-3 text-left text-xs font-semibold text-slate-600 uppercase tracking-wider">
                                        Applicant Name
                                    </th>
                                    <th
                                        class="px-6 py-3 text-left text-xs font-semibold text-slate-600 uppercase tracking-wider">
                                        Location
                                    </th>
                                    <th
                                        class="px-6 py-3 text-left text-xs font-semibold text-slate-600 uppercase tracking-wider">
                                        Status
                                    </th>
                                    <th
                                        class="px-6 py-3 text-left text-xs font-semibold text-slate-600 uppercase tracking-wider">
                                        Verified Date
                                    </th>
                                    <th
                                        class="px-6 py-3 text-left text-xs font-semibold text-slate-600 uppercase tracking-wider">
                                        Area (Ha)
                                    </th>
                                </tr>
                            </thead>
                            <tbody class="bg-white/50 divide-y divide-slate-100">
                        <tr v-if="loading">
                            <td
                                colspan="7"
                                class="px-6 py-8 text-center text-sm text-slate-500">
                                <RefreshCw
                                    class="h-5 w-5 animate-spin inline mr-2" />
                                Loading insurance applications...
                            </td>
                        </tr>
                        <tr v-else-if="filteredApplications.length === 0">
                            <td
                                colspan="7"
                                class="px-6 py-8 text-center text-sm text-slate-500">
                                No insurance applications found matching your
                                criteria.
                            </td>
                        </tr>
                        <template v-else>
                            <tr
                                v-for="insurance in filteredApplications"
                                :key="insurance.insuranceId"
                                :class="[
                                    'hover:bg-blue-50/50 transition-all duration-200',
                                    selectedInsuranceIds.includes(
                                        insurance.insuranceId,
                                    )
                                        ? 'bg-blue-50/50'
                                        : '',
                                ]">
                                <!-- Checkbox -->
                                <td class="px-6 py-4" @click.stop>
                                    <input
                                        type="checkbox"
                                        :checked="
                                            selectedInsuranceIds.includes(
                                                insurance.insuranceId,
                                            )
                                        "
                                        class="h-4 w-4 text-blue-500 focus:ring-blue-400 focus:ring-2 focus:ring-blue-400/20 border-slate-300 rounded cursor-pointer transition-all duration-200"
                                        @change="
                                            toggleSelection(
                                                insurance.insuranceId,
                                            )
                                        " />
                                </td>
                                <!-- Insurance ID -->
                                <td
                                    class="px-6 py-4 text-xs font-medium text-blue-600 hover:text-blue-800 cursor-pointer"
                                    @click="navigateToDetail(insurance)">
                                    <div class="flex flex-col">
                                        <span class="underline"
                                            >{{
                                                insurance.applicationName
                                            }}...</span
                                        >
                                    
                                    </div>
                                </td>
                                <!-- Applicant Name -->
                                <td
                                    class="px-6 py-4 text-xs text-slate-800 cursor-pointer"
                                    @click="navigateToDetail(insurance)">
                                    <div class="flex flex-col">
                                        <span class="font-semibold">{{
                                            extractApplicantName(insurance)
                                        }}</span>
                                        <span
                                            class="text-xs text-slate-500"
                                            >{{
                                                extractContactInfo(insurance)
                                            }}</span
                                        >
                                    </div>
                                </td>
                                <!-- Location -->
                                <td
                                    class="px-6 py-4 text-xs text-slate-700 cursor-pointer"
                                    @click="navigateToDetail(insurance)">
                                    {{ extractLocation(insurance) }}
                                </td>

                                <!-- Status -->
                                <td
                                    class="px-6 py-4 cursor-pointer"
                                    @click="navigateToDetail(insurance)">
                                    <span
                                        :class="[
                                            'inline-flex items-center px-2.5 py-1 rounded-lg text-xs font-medium',
                                            insurance.status === 'APPROVED'
                                                ? 'bg-emerald-100 text-emerald-700'
                                                : insurance.status === 'PENDING'
                                                ? 'bg-amber-100 text-amber-700'
                                                : 'bg-red-100 text-red-700',
                                        ]">
                                        {{ insurance.status }}
                                    </span>
                                </td>
                                <!-- Verified Date -->
                                <td
                                    class="px-6 py-4 text-xs text-slate-600 cursor-pointer"
                                    @click="navigateToDetail(insurance)">
                                    <div class="flex flex-col">
                                        <span class="font-medium">{{
                                            formatDate(
                                                insurance.verification
                                                    ?.verifiedAt,
                                            )
                                        }}</span>
                                        <span
                                            class="text-xs text-slate-500"
                                            >{{
                                                insurance.verification
                                                    ?.verifierName || 'N/A'
                                            }}</span
                                        >
                                    </div>
                                </td>
                                <!-- Area -->
                                <td
                                    class="px-6 py-4 text-xs text-slate-700 font-medium cursor-pointer"
                                    @click="navigateToDetail(insurance)">
                                    {{ extractAreaInsured(insurance) }} ha
                                </td>
                            </tr>
                        </template>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </AuthenticatedLayout>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { RefreshCw, Search, Filter, ChevronDown, Trash2 } from 'lucide-vue-next'
import AuthenticatedLayout from '@/layouts/AuthenticatedLayout.vue'
import { UNDERWRITER_NAVIGATION } from '@/lib/navigation'
import { useInsuranceStore } from '@/stores/insurance'

const underwriterNavigation = UNDERWRITER_NAVIGATION
const router = useRouter()
const insuranceStore = useInsuranceStore()

// State
const applications = ref([])
const selectedApplication = ref(null)
const loading = ref(false)
const showFilters = ref(false)
const selectedInsuranceIds = ref([])
const deleting = ref(false)

// Filters
const searchQuery = ref('')
const selectedStatus = ref('')
const selectedCrop = ref('')
const selectedLocation = ref('')
const dateFrom = ref('')
const dateTo = ref('')

// Helper functions to extract information from field values in a type-safe way
const extractApplicantName = insurance => {
    const fieldValues = insurance.verification?.fieldValues || {}

    // Try different naming patterns for applicant name
    if (fieldValues.farmer_name) return fieldValues.farmer_name
    if (fieldValues.first_name && fieldValues.last_name) {
        return `${fieldValues.first_name} ${fieldValues.middle_name || ''} ${
            fieldValues.last_name
        }`.trim()
    }
    if (fieldValues.applicant_name) return fieldValues.applicant_name
    if (fieldValues.name) return fieldValues.name

    return 'N/A'
}

const extractCropType = insurance => {
    const fieldValues = insurance.verification?.fieldValues || {}

    if (fieldValues.insured_crops) return fieldValues.insured_crops
    if (fieldValues.crop_type) return fieldValues.crop_type
    if (fieldValues.crop) return fieldValues.crop

    return 'N/A'
}

const extractLocation = insurance => {
    const fieldValues = insurance.verification?.fieldValues || {}

    if (fieldValues.farm_location) return fieldValues.farm_location
    if (fieldValues.address) return fieldValues.address
    if (fieldValues.location) return fieldValues.location

    // Try to construct from lot location if available
    if (fieldValues.lot_1_location) {
        const loc = fieldValues.lot_1_location
        if (typeof loc === 'object') {
            return `${loc.barangay || ''}, ${loc.city || ''}, ${
                loc.province || ''
            }`
                .trim()
                .replace(/^,|,$/, '')
        }
    }

    return 'N/A'
}

const extractAreaInsured = insurance => {
    const fieldValues = insurance.verification?.fieldValues || {}

    if (fieldValues.area_insured)
        return parseFloat(fieldValues.area_insured) || 0
    if (fieldValues.lot_1_area) return parseFloat(fieldValues.lot_1_area) || 0
    if (fieldValues.total_area) return parseFloat(fieldValues.total_area) || 0

    return 0
}

const extractContactInfo = insurance => {
    const fieldValues = insurance.verification?.fieldValues || {}
    return (
        fieldValues.cell_phone_number ||
        fieldValues.phone ||
        fieldValues.contact ||
        'N/A'
    )
}

// Computed
const filteredApplications = computed(() => {
    let filtered = [...applications.value]

    if (searchQuery.value) {
        const query = searchQuery.value.toLowerCase()
        filtered = filtered.filter(insurance => {
            const name = extractApplicantName(insurance).toLowerCase()
            const crop = extractCropType(insurance).toLowerCase()
            const location = extractLocation(insurance).toLowerCase()
            const id = insurance.insuranceId?.toLowerCase() || ''

            return (
                name.includes(query) ||
                crop.includes(query) ||
                location.includes(query) ||
                id.includes(query)
            )
        })
    }

    if (selectedStatus.value) {
        filtered = filtered.filter(
            insurance =>
                insurance.status?.toLowerCase() ===
                selectedStatus.value.toLowerCase(),
        )
    }

    if (selectedCrop.value) {
        filtered = filtered.filter(insurance =>
            extractCropType(insurance)
                .toLowerCase()
                .includes(selectedCrop.value.toLowerCase()),
        )
    }

    if (selectedLocation.value) {
        filtered = filtered.filter(insurance =>
            extractLocation(insurance)
                .toLowerCase()
                .includes(selectedLocation.value.toLowerCase()),
        )
    }

    if (dateFrom.value) {
        filtered = filtered.filter(insurance => {
            const verifiedDate = insurance.verification?.verifiedAt
            if (!verifiedDate) return false
            return new Date(verifiedDate) >= new Date(dateFrom.value)
        })
    }

    if (dateTo.value) {
        filtered = filtered.filter(insurance => {
            const verifiedDate = insurance.verification?.verifiedAt
            if (!verifiedDate) return false
            return new Date(verifiedDate) <= new Date(dateTo.value)
        })
    }

    return filtered
})

// Available filter options computed from current data
const availableCrops = computed(() => {
    const crops = new Set()
    applications.value.forEach(insurance => {
        const crop = extractCropType(insurance)
        if (crop !== 'N/A') crops.add(crop)
    })
    return Array.from(crops).sort()
})

const availableLocations = computed(() => {
    const locations = new Set()
    applications.value.forEach(insurance => {
        const location = extractLocation(insurance)
        if (location !== 'N/A') {
            // Extract city/municipality from location
            const parts = location.split(',')
            if (parts.length > 1) {
                locations.add(parts[1].trim())
            } else {
                locations.add(location)
            }
        }
    })
    return Array.from(locations).sort()
})

// Check if all visible applications are selected
const allSelected = computed(() => {
    if (filteredApplications.value.length === 0) return false
    return filteredApplications.value.every(insurance =>
        selectedInsuranceIds.value.includes(insurance.insuranceId),
    )
})

// Methods
const fetchApplications = async () => {
    loading.value = true
    try {
        const result = await insuranceStore.fetchVerifiedInsurance()
        if (result.success && result.data) {
            applications.value = result.data
        } else {
            console.warn('No verified insurance found or failed to fetch')
            applications.value = []
        }
    } catch (error) {
        console.error('Failed to fetch applications:', error)
        applications.value = []
    } finally {
        loading.value = false
    }
}

const refreshApplications = () => {
    fetchApplications()
}

const navigateToDetail = insurance => {
    router.push({
        name: 'underwriter-applications-detail',
        params: {
            insuranceId: insurance.insuranceId,
            submissionId: insurance.submissionId,
        },
    })
}

const toggleSelection = insuranceId => {
    const index = selectedInsuranceIds.value.indexOf(insuranceId)
    if (index > -1) {
        selectedInsuranceIds.value.splice(index, 1)
    } else {
        selectedInsuranceIds.value.push(insuranceId)
    }
}

const toggleSelectAll = () => {
    if (allSelected.value) {
        // Deselect all
        selectedInsuranceIds.value = []
    } else {
        // Select all filtered applications
        selectedInsuranceIds.value = filteredApplications.value.map(
            insurance => insurance.insuranceId,
        )
    }
}

const handleBulkDelete = async () => {
    if (selectedInsuranceIds.value.length === 0) return

    const confirmMessage = `Are you sure you want to delete ${
        selectedInsuranceIds.value.length
    } application${
        selectedInsuranceIds.value.length === 1 ? '' : 's'
    }? This action cannot be undone.`

    if (!confirm(confirmMessage)) return

    deleting.value = true
    let successCount = 0
    let failCount = 0

    try {
        // Delete each selected insurance
        for (const insuranceId of selectedInsuranceIds.value) {
            try {
                const result = await insuranceStore.deleteInsurance(insuranceId)
                if (result.success === 'true') {
                    successCount++
                    // Remove from local applications array
                    const index = applications.value.findIndex(
                        app => app.insuranceId === insuranceId,
                    )
                    if (index > -1) {
                        applications.value.splice(index, 1)
                    }
                } else {
                    failCount++
                    console.error(
                        `Failed to delete ${insuranceId}:`,
                        result.message,
                    )
                }
            } catch (error) {
                failCount++
                console.error(`Error deleting ${insuranceId}:`, error)
            }
        }

        // Clear selection
        selectedInsuranceIds.value = []

        // Show result message
        if (successCount > 0 && failCount === 0) {
            alert(
                `Successfully deleted ${successCount} application${
                    successCount === 1 ? '' : 's'
                }.`,
            )
        } else if (successCount > 0 && failCount > 0) {
            alert(
                `Deleted ${successCount} application${
                    successCount === 1 ? '' : 's'
                }. Failed to delete ${failCount} application${
                    failCount === 1 ? '' : 's'
                }.`,
            )
        } else {
            alert(`Failed to delete all selected applications.`)
        }

        // Refresh the list
        await fetchApplications()
    } catch (error) {
        console.error('Error in bulk delete:', error)
        alert('An error occurred while deleting applications.')
    } finally {
        deleting.value = false
    }
}

const toggleFilters = () => {
    showFilters.value = !showFilters.value
}

const clearAllFilters = () => {
    searchQuery.value = ''
    selectedStatus.value = ''
    selectedCrop.value = ''
    selectedLocation.value = ''
    dateFrom.value = ''
    dateTo.value = ''
}

const formatDate = dateString => {
    if (!dateString) return 'N/A'
    return new Date(dateString).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'short',
        day: 'numeric',
    })
}

// Initialize
onMounted(() => {
    fetchApplications()
})
</script>

<style scoped>
/* Component-specific styles can be added here if needed */
</style>
