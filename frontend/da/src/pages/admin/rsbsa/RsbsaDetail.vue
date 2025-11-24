<template>
    <AuthenticatedLayout :navigation="navigation">
        <div class="h-full overflow-y-auto">
            <!-- Loading State -->
            <div v-if="loading" class="flex items-center justify-center py-12">
                <div class="text-center">
                    <div class="inline-block animate-spin rounded-full h-12 w-12 border-b-2 border-green-600 mb-4">
                    </div>
                    <p class="text-gray-600">Loading RSBSA details...</p>
                </div>
            </div>

            <!-- Error State -->
            <div v-else-if="error" class="flex items-center justify-center py-12">
                <div class="text-center">
                    <AlertTriangle class="h-12 w-12 text-red-600 mx-auto mb-4" />
                    <p class="text-gray-800 text-lg font-medium mb-2">Failed to load RSBSA details</p>
                    <p class="text-gray-600 mb-4">{{ error }}</p>
                    <button @click="router.push({ name: 'rsbsa-management' })"
                        class="px-4 py-2 bg-green-600 hover:bg-green-700 text-white rounded-lg transition-colors">
                        Back to RSBSA Management
                    </button>
                </div>
            </div>

            <!-- Content -->
            <div v-else-if="rsbsaDetail" class="pb-6">
                <!-- Header -->
                <div class="mb-6">
                    <div class="flex items-center justify-between">
                        <div class="flex items-center space-x-4">
                            <button @click="router.push({ name: 'rsbsa-management' })"
                                class="p-2 hover:bg-gray-100 rounded-lg transition-colors">
                                <ArrowLeft class="h-5 w-5 text-gray-600" />
                            </button>
                            <div>
                                <h1 class="text-2xl font-bold text-gray-900">RSBSA Details</h1>
                                <p class="text-sm text-gray-600 mt-1">RSBSA ID: {{ rsbsaDetail.rsbsaId }}</p>
                            </div>
                        </div>

                        <div class="flex items-center space-x-3">
                            <button @click="handleEdit"
                                class="flex items-center px-4 py-2 bg-green-600 hover:bg-green-700 text-white rounded-lg transition-colors">
                                <Edit class="h-4 w-4 mr-2" />
                                Edit
                            </button>
                        </div>
                    </div>
                </div>

                <!-- Personal Information -->
                <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
                    <div class="flex items-center mb-6">
                        <User class="h-5 w-5 text-green-600 mr-2" />
                        <h2 class="text-lg font-semibold text-gray-900">Personal Information</h2>
                    </div>

                    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
                        <div>
                            <label class="block text-sm font-medium text-gray-500 mb-1">RSBSA ID</label>
                            <p class="text-gray-900 font-medium">{{ rsbsaDetail.rsbsaId }}</p>
                        </div>

                        <div>
                            <label class="block text-sm font-medium text-gray-500 mb-1">First Name</label>
                            <p class="text-gray-900">{{ rsbsaDetail.firstName || 'N/A' }}</p>
                        </div>

                        <div>
                            <label class="block text-sm font-medium text-gray-500 mb-1">Middle Name</label>
                            <p class="text-gray-900">{{ rsbsaDetail.middleName || 'N/A' }}</p>
                        </div>

                        <div>
                            <label class="block text-sm font-medium text-gray-500 mb-1">Last Name</label>
                            <p class="text-gray-900">{{ rsbsaDetail.lastName || 'N/A' }}</p>
                        </div>

                        <div>
                            <label class="block text-sm font-medium text-gray-500 mb-1">Gender</label>
                            <p class="text-gray-900">{{ rsbsaDetail.gender || 'N/A' }}</p>
                        </div>

                        <div>
                            <label class="block text-sm font-medium text-gray-500 mb-1">Date of Birth</label>
                            <p class="text-gray-900">{{ formatDate(rsbsaDetail.dateOfBirth) }}</p>
                        </div>

                        <div>
                            <label class="block text-sm font-medium text-gray-500 mb-1">Civil Status</label>
                            <p class="text-gray-900">{{ rsbsaDetail.civilStatus || 'N/A' }}</p>
                        </div>

                        <div>
                            <label class="block text-sm font-medium text-gray-500 mb-1">Education Level</label>
                            <p class="text-gray-900">{{ rsbsaDetail.educationLevel || 'N/A' }}</p>
                        </div>

                        <div>
                            <label class="block text-sm font-medium text-gray-500 mb-1">Household Size</label>
                            <p class="text-gray-900">{{ rsbsaDetail.householdSize || 'N/A' }}</p>
                        </div>

                        <div>
                            <label class="block text-sm font-medium text-gray-500 mb-1">Person with Disability</label>
                            <p class="text-gray-900">{{ rsbsaDetail.withDisability ? 'Yes' : 'No' }}</p>
                        </div>
                    </div>
                </div>

                <!-- Contact Information -->
                <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
                    <div class="flex items-center mb-6">
                        <Phone class="h-5 w-5 text-green-600 mr-2" />
                        <h2 class="text-lg font-semibold text-gray-900">Contact Information</h2>
                    </div>

                    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
                        <div>
                            <label class="block text-sm font-medium text-gray-500 mb-1">Contact Number</label>
                            <p class="text-gray-900">{{ rsbsaDetail.contactNumber || 'N/A' }}</p>
                        </div>

                        <div>
                            <label class="block text-sm font-medium text-gray-500 mb-1">Email Address</label>
                            <p class="text-gray-900">{{ rsbsaDetail.email || 'N/A' }}</p>
                        </div>
                    </div>
                </div>

                <!-- Address Information -->
                <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
                    <div class="flex items-center mb-6">
                        <MapPin class="h-5 w-5 text-green-600 mr-2" />
                        <h2 class="text-lg font-semibold text-gray-900">Address Information</h2>
                    </div>

                    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
                        <div>
                            <label class="block text-sm font-medium text-gray-500 mb-1">Province</label>
                            <p class="text-gray-900">{{ rsbsaDetail.province || 'N/A' }}</p>
                        </div>

                        <div>
                            <label class="block text-sm font-medium text-gray-500 mb-1">Municipality</label>
                            <p class="text-gray-900">{{ rsbsaDetail.municipality || 'N/A' }}</p>
                        </div>

                        <div>
                            <label class="block text-sm font-medium text-gray-500 mb-1">Barangay</label>
                            <p class="text-gray-900">{{ rsbsaDetail.barangay || 'N/A' }}</p>
                        </div>

                        <div class="md:col-span-3">
                            <label class="block text-sm font-medium text-gray-500 mb-1">Street/Purok/Sitio</label>
                            <p class="text-gray-900">{{ rsbsaDetail.address || 'N/A' }}</p>
                        </div>
                    </div>
                </div>

                <!-- Farm Information -->
                <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
                    <div class="flex items-center mb-6">
                        <Sprout class="h-5 w-5 text-green-600 mr-2" />
                        <h2 class="text-lg font-semibold text-gray-900">Farm Information</h2>
                    </div>

                    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
                        <div>
                            <label class="block text-sm font-medium text-gray-500 mb-1">Farming Type</label>
                            <p class="text-gray-900">{{ rsbsaDetail.farmingType || 'N/A' }}</p>
                        </div>

                        <div>
                            <label class="block text-sm font-medium text-gray-500 mb-1">Primary Crop</label>
                            <p class="text-gray-900">{{ rsbsaDetail.primaryCrop || 'N/A' }}</p>
                        </div>

                        <div>
                            <label class="block text-sm font-medium text-gray-500 mb-1">Secondary Crop</label>
                            <p class="text-gray-900">{{ rsbsaDetail.secondaryCrop || 'N/A' }}</p>
                        </div>

                        <div>
                            <label class="block text-sm font-medium text-gray-500 mb-1">Farm Area (hectares)</label>
                            <p class="text-gray-900">{{ rsbsaDetail.farmArea ? `${rsbsaDetail.farmArea} ha` : 'N/A' }}
                            </p>
                        </div>

                        <div>
                            <label class="block text-sm font-medium text-gray-500 mb-1">Farm Location</label>
                            <p class="text-gray-900">{{ rsbsaDetail.farmLocation || 'N/A' }}</p>
                        </div>

                        <div>
                            <label class="block text-sm font-medium text-gray-500 mb-1">Tenure Status</label>
                            <p class="text-gray-900">{{ rsbsaDetail.tenureStatus || 'N/A' }}</p>
                        </div>
                    </div>
                </div>

                <!-- Income Information -->
                <div class="bg-white rounded-lg shadow-sm border border-gray-200 p-6 mb-6">
                    <div class="flex items-center mb-6">
                        <Wallet class="h-5 w-5 text-green-600 mr-2" />
                        <h2 class="text-lg font-semibold text-gray-900">Income Information</h2>
                    </div>

                    <div class="grid grid-cols-1 md:grid-cols-3 gap-6">
                        <div>
                            <label class="block text-sm font-medium text-gray-500 mb-1">Source of Income</label>
                            <p class="text-gray-900">{{ rsbsaDetail.sourceOfIncome || 'N/A' }}</p>
                        </div>

                        <div>
                            <label class="block text-sm font-medium text-gray-500 mb-1">Estimated Annual Income</label>
                            <p class="text-gray-900">
                                {{ rsbsaDetail.estimatedIncome ? `₱${formatCurrency(rsbsaDetail.estimatedIncome)}` :
                                    'N/A' }}
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- Delete Confirmation Modal -->
        <transition enter-active-class="transition ease-out duration-300" enter-from-class="opacity-0"
            enter-to-class="opacity-100" leave-active-class="transition ease-in duration-200"
            leave-from-class="opacity-100" leave-to-class="opacity-0">
            <div v-if="showDeleteModal"
                class="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
                @click="showDeleteModal = false">
                <div class="bg-white rounded-lg p-6 max-w-md w-full mx-4" @click.stop>
                    <div class="flex items-center mb-4">
                        <div class="flex-shrink-0 flex items-center justify-center h-12 w-12 rounded-full bg-red-100">
                            <AlertTriangle class="h-6 w-6 text-red-600" />
                        </div>
                        <h3 class="ml-4 text-lg font-medium text-gray-900">
                            Confirm Deletion
                        </h3>
                    </div>

                    <p class="text-sm text-gray-500 mb-6">
                        Are you sure you want to delete this RSBSA record? This action cannot be undone.
                    </p>

                    <div class="flex justify-end space-x-3">
                        <button @click="showDeleteModal = false"
                            class="px-4 py-2 bg-white border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors">
                            Cancel
                        </button>
                        <button @click="executeDelete"
                            class="px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg transition-colors"
                            :disabled="deleting">
                            {{ deleting ? 'Deleting...' : 'Delete' }}
                        </button>
                    </div>
                </div>
            </div>
        </transition>
    </AuthenticatedLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useRsbsaStore } from '@/stores/rsbsa'
import { useNotificationStore } from '@/stores/notification'
import AuthenticatedLayout from '@/layouts/AuthenticatedLayout.vue'
import { UNIFIED_NAVIGATION } from '@/lib/navigation'
import {
    ArrowLeft,
    Edit,
    Trash2,
    User,
    Phone,
    MapPin,
    Sprout,
    Wallet,
    AlertTriangle
} from 'lucide-vue-next'

const router = useRouter()
const route = useRoute()
const rsbsaStore = useRsbsaStore()
const notificationStore = useNotificationStore()

const navigation = UNIFIED_NAVIGATION

// State
const loading = ref(true)
const error = ref(null)
const rsbsaDetail = ref(null)
const showDeleteModal = ref(false)
const deleting = ref(false)

// Methods
const formatDate = (date) => {
    if (!date) return 'N/A'
    return new Date(date).toLocaleDateString('en-US', {
        year: 'numeric',
        month: 'long',
        day: 'numeric'
    })
}

const formatCurrency = (amount) => {
    if (!amount) return '0.00'
    return new Intl.NumberFormat('en-PH', {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
    }).format(amount)
}

const handleEdit = () => {
    // Navigate to edit page or show edit modal
    notificationStore.showInfo('Edit functionality coming soon')
}


const executeDelete = async () => {
    deleting.value = true
    const result = await rsbsaStore.deleteRsbsa(rsbsaDetail.value.rsbsaId)

    if (result.success) {
        notificationStore.showSuccess(result.message)
        router.push({ name: 'rsbsa-management' })
    } else {
        notificationStore.showError(result.message)
        showDeleteModal.value = false
    }

    deleting.value = false
}

// Lifecycle
onMounted(async () => {
    const rsbsaId = route.params.id

    if (!rsbsaId) {
        error.value = 'RSBSA ID is required'
        loading.value = false
        return
    }

    const result = await rsbsaStore.fetchRsbsaById(rsbsaId)

    if (result.success) {
        rsbsaDetail.value = result.data
    } else {
        error.value = result.message
    }

    loading.value = false
})
</script>

<style scoped>
/* Add any custom styles if needed */
</style>
