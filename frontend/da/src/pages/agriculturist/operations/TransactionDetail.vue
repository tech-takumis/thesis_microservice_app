<template>
  <AuthenticatedLayout
    :navigation="navigation"
    role-title="Municipal Agriculturist"
    page-title="Transaction Detail">
    <div class="h-full flex flex-col min-h-0 overflow-hidden">
      <!-- Fixed Header Section -->
      <div class="flex-shrink-0 mb-4">
        <!-- Header with breadcrumb -->
        <nav class="flex mb-4" aria-label="Breadcrumb">
          <ol class="flex items-center space-x-4">
            <li>
              <div>
                <router-link
                  :to="{ name: 'agriculturist-dashboard' }"
                  class="text-gray-400 hover:text-gray-500"
                >
                  <HomeIcon class="flex-shrink-0 h-5 w-5" />
                  <span class="sr-only">Dashboard</span>
                </router-link>
              </div>
            </li>
            <li>
              <div class="flex items-center">
                <ChevronRightIcon class="flex-shrink-0 h-5 w-5 text-gray-400" />
                <router-link
                  :to="{ name: 'agriculturist-transaction' }"
                  class="ml-4 text-sm font-medium text-gray-500 hover:text-gray-700"
                >
                  Transactions
                </router-link>
              </div>
            </li>
            <li>
              <div class="flex items-center">
                <ChevronRightIcon class="flex-shrink-0 h-5 w-5 text-gray-400" />
                <span class="ml-4 text-sm font-medium text-black">
                  Transaction Details
                </span>
              </div>
            </li>
          </ol>
        </nav>

        <div class="flex items-center justify-between ml-5">
          <div>
            <h1 class="text-3xl font-bold text-green-600">Transaction Details</h1>
            <p class="mt-1 text-sm text-gray-600">
              {{ transaction ? transaction.description : 'Loading...' }}
            </p>
          </div>
        </div>
      </div>

      <!-- Main Content Area -->
      <div class="flex-1 min-h-0 overflow-hidden">
        <div class="h-full">
          <!-- Loading State -->
          <div v-if="loading" class="flex justify-center items-center flex-1 h-full">
            <LoadingSpinner />
          </div>

          <!-- Error State -->
          <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-md p-4 mb-4">
            <div class="flex">
              <div class="flex-shrink-0">
                <ExclamationTriangleIcon class="h-5 w-5 text-red-400" />
              </div>
              <div class="ml-3">
                <h3 class="text-sm font-medium text-red-800">Error Loading Transaction Details</h3>
                <div class="mt-2 text-sm text-red-700">{{ error }}</div>
              </div>
            </div>
          </div>

          <!-- Transaction Details -->
          <div v-else-if="transaction" class="bg-gray-50 rounded-xl shadow-sm p-3 h-full overflow-auto">
            <div class="space-y-4">
              <!-- Transaction Information Card -->
              <div class="bg-gray-100 rounded-xl border border-gray-300 shadow-sm">
                <div class="px-4 py-4">
                  <h2 class="text-lg font-bold text-gray-900 mb-4">Transaction Information</h2>

                  <div class="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-x-6 gap-y-3">
                    <!-- Transaction ID -->
                    <div class="flex flex-col">
                      <span class="text-[10px] font-semibold text-green-600 uppercase tracking-wider mb-1">
                        Transaction ID
                      </span>
                      <div class="w-full bg-white rounded-md px-3 py-2.5 border border-gray-200 shadow-sm hover:shadow transition-all duration-150">
                        <span class="text-sm text-gray-900 font-medium break-words font-mono">
                          {{ transaction.id }}
                        </span>
                      </div>
                    </div>

                    <!-- Name/Description -->
                    <div class="flex flex-col">
                      <span class="text-[10px] font-semibold text-green-600 uppercase tracking-wider mb-1">
                        Description
                      </span>
                      <div class="w-full bg-white rounded-md px-3 py-2.5 border border-gray-200 shadow-sm hover:shadow transition-all duration-150">
                        <span class="text-sm text-gray-900 font-medium break-words">
                          {{ transaction.description }}
                        </span>
                      </div>
                    </div>

                    <!-- Type -->
                    <div class="flex flex-col">
                      <span class="text-[10px] font-semibold text-green-600 uppercase tracking-wider mb-1">
                        Type
                      </span>
                      <div class="w-full bg-white rounded-md px-3 py-2.5 border border-gray-200 shadow-sm hover:shadow transition-all duration-150">
                        <span :class="{
                          'bg-green-100 text-green-800': transaction.type === 'INCOME',
                          'bg-red-100 text-red-800': transaction.type === 'EXPENSE'
                        }" class="px-2 py-1 text-xs font-medium rounded-full">
                          {{ transaction.type }}
                        </span>
                      </div>
                    </div>

                    <!-- Amount -->
                    <div class="flex flex-col">
                      <span class="text-[10px] font-semibold text-green-600 uppercase tracking-wider mb-1">
                        Amount
                      </span>
                      <div class="w-full bg-white rounded-md px-3 py-2.5 border border-gray-200 shadow-sm hover:shadow transition-all duration-150">
                        <span :class="{
                          'text-green-600': transaction.positive,
                          'text-red-600': !transaction.positive
                        }" class="text-sm font-bold">
                          {{ formatCurrency(transaction.amount) }}
                        </span>
                      </div>
                    </div>

                    <!-- Status -->
                    <div class="flex flex-col">
                      <span class="text-[10px] font-semibold text-green-600 uppercase tracking-wider mb-1">
                        Status
                      </span>
                      <div class="w-full bg-white rounded-md px-3 py-2.5 border border-gray-200 shadow-sm hover:shadow transition-all duration-150">
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

                    <!-- Date -->
                    <div class="flex flex-col">
                      <span class="text-[10px] font-semibold text-green-600 uppercase tracking-wider mb-1">
                        Transaction Date
                      </span>
                      <div class="w-full bg-white rounded-md px-3 py-2.5 border border-gray-200 shadow-sm hover:shadow transition-all duration-150">
                        <span class="text-sm text-gray-900 font-medium break-words">
                          {{ formatDate(transaction.date) }}
                        </span>
                      </div>
                    </div>

                    <!-- Positive/Negative Indicator -->
                    <div class="flex flex-col">
                      <span class="text-[10px] font-semibold text-green-600 uppercase tracking-wider mb-1">
                        Impact
                      </span>
                      <div class="w-full bg-white rounded-md px-3 py-2.5 border border-gray-200 shadow-sm hover:shadow transition-all duration-150">
                        <span :class="{
                          'bg-green-100 text-green-800': transaction.positive,
                          'bg-red-100 text-red-800': !transaction.positive
                        }" class="px-2 py-1 text-xs font-medium rounded-full">
                          {{ transaction.positive ? 'Positive' : 'Negative' }}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

            </div>
          </div>
        </div>
      </div>
    </div>
  </AuthenticatedLayout>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { HomeIcon, ChevronRightIcon, ExclamationTriangleIcon } from '@heroicons/vue/24/outline'
import AuthenticatedLayout from '@/layouts/AuthenticatedLayout.vue'
import LoadingSpinner from '@/components/others/LoadingSpinner.vue'
import { MUNICIPAL_AGRICULTURIST_NAVIGATION } from '@/lib/navigation'
import { useTransactionStore } from '@/stores/transaction.js'

const route = useRoute()
const router = useRouter()
const navigation = MUNICIPAL_AGRICULTURIST_NAVIGATION
const transactionStore = useTransactionStore()

// State
const transaction = ref(null)
const loading = ref(false)
const error = ref(null)

// Methods
const fetchTransactionDetail = async () => {
  try {
    loading.value = true
    error.value = null

    const transactionId = route.params.id
    const result = await transactionStore.getTransaction(transactionId)

    if (result.success) {
      transaction.value = result.data
    } else {
      error.value = result.message || 'Failed to load transaction details'
    }
  } catch (err) {
    console.error('Error fetching transaction:', err)
    error.value = err.message || 'An unexpected error occurred'
  } finally {
    loading.value = false
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
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    })
  } catch (error) {
    return 'Invalid Date'
  }
}

const goBack = () => {
  router.push({ name: 'agriculturist-transaction' })
}

// Lifecycle
onMounted(async () => {
  await fetchTransactionDetail()
})
</script>

<style scoped>
/* Custom scrollbar styling */
.overflow-auto::-webkit-scrollbar {
  width: 8px;
}

.overflow-auto::-webkit-scrollbar-track {
  background: #f1f5f9;
  border-radius: 4px;
}

.overflow-auto::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 4px;
}

.overflow-auto::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

.break-words {
  word-wrap: break-word;
  word-break: break-word;
}
</style>
