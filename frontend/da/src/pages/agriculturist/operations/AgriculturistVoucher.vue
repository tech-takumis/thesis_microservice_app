<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import AuthenticatedLayout from '@/layouts/AuthenticatedLayout.vue'
import { MUNICIPAL_AGRICULTURIST_NAVIGATION } from '@/lib/navigation'
import { useRouter } from 'vue-router'
import { useFarmerStore } from '@/stores/farmer'
import { useVoucherStore } from '@/stores/voucher'
import { Search, RotateCcw, Check, FileText, User } from 'lucide-vue-next'
import { ChevronRightIcon, TicketIcon } from '@heroicons/vue/24/outline'

const navigation = MUNICIPAL_AGRICULTURIST_NAVIGATION
const router = useRouter()
const farmerStore = useFarmerStore()
const voucherStore = useVoucherStore()

// State
const selectedFarmers = ref([])
const searchQuery = ref('')
const isSubmitting = ref(false)
const notification = ref(null)

// Voucher Information (matches CreateVoucherRequestDto)
const voucherInfo = ref({
  title: '',
  voucherType: 'SEEDS',
  unit: 'bags',
  quantity: '',
  issueDate: new Date().toISOString().split('T')[0],
  expiryDate: '',
  referenceNumber: ''
})

// Voucher type options
const voucherTypes = [
  'SEEDS',
  'FERTILIZER',
  'EQUIPMENT',
  'CASH',
  'OTHER'
]

// Unit options based on voucher type
const unitOptions = computed(() => {
  const units = {
    SEEDS: ['bags', 'kg', 'sacks'],
    FERTILIZER: ['bags', 'kg', 'liters'],
    EQUIPMENT: ['pieces', 'units', 'sets'],
    CASH: ['PHP', 'USD'],
    OTHER: ['items', 'units']
  }
  return units[voucherInfo.value.voucherType] || ['units']
})

// Computed
const filteredFarmers = computed(() => {
  if (!searchQuery.value) return farmerStore.allFarmers
  const query = searchQuery.value.toLowerCase()
  return farmerStore.allFarmers.filter(farmer => 
    farmer.name?.toLowerCase().includes(query) ||
    farmer.firstName?.toLowerCase().includes(query) ||
    farmer.lastName?.toLowerCase().includes(query)
  )
})

const isFormValid = computed(() => {
  return voucherInfo.value.title && 
         voucherInfo.value.voucherType && 
         voucherInfo.value.unit &&
         voucherInfo.value.quantity &&
         selectedFarmers.value.length > 0
})

// Custom dropdown state for voucher type
const isVoucherDropdownOpen = ref(false)
const highlightedIndex = ref(-1)

const toggleVoucherDropdown = () => {
  isVoucherDropdownOpen.value = !isVoucherDropdownOpen.value
  if (isVoucherDropdownOpen.value) highlightedIndex.value = voucherTypes.indexOf(voucherInfo.value.voucherType)
}

const closeVoucherDropdown = () => {
  isVoucherDropdownOpen.value = false
  highlightedIndex.value = -1
}

const selectVoucherType = (type) => {
  voucherInfo.value.voucherType = type
  voucherInfo.value.unit = unitOptions.value[0]
  closeVoucherDropdown()
  closeUnitDropdown()
}

const onVoucherKeyDown = (e) => {
  if (!isVoucherDropdownOpen.value && (e.key === 'Enter' || e.key === ' ')) {
    e.preventDefault()
    toggleVoucherDropdown()
    return
  }

  if (isVoucherDropdownOpen.value) {
    if (e.key === 'ArrowDown') {
      e.preventDefault()
      highlightedIndex.value = Math.min(highlightedIndex.value + 1, voucherTypes.length - 1)
    } else if (e.key === 'ArrowUp') {
      e.preventDefault()
      highlightedIndex.value = Math.max(highlightedIndex.value - 1, 0)
    } else if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault()
      if (highlightedIndex.value >= 0) selectVoucherType(voucherTypes[highlightedIndex.value])
    } else if (e.key === 'Escape') {
      e.preventDefault()
      closeVoucherDropdown()
    }
  }
}

// Custom dropdown state for unit
const isUnitDropdownOpen = ref(false)
const highlightedUnitIndex = ref(-1)

const toggleUnitDropdown = () => {
  isUnitDropdownOpen.value = !isUnitDropdownOpen.value
  if (isUnitDropdownOpen.value) highlightedUnitIndex.value = unitOptions.value.indexOf(voucherInfo.value.unit)
}

const closeUnitDropdown = () => {
  isUnitDropdownOpen.value = false
  highlightedUnitIndex.value = -1
}

const selectUnit = (unit) => {
  voucherInfo.value.unit = unit
  closeUnitDropdown()
}

const onUnitKeyDown = (e) => {
  if (!isUnitDropdownOpen.value && (e.key === 'Enter' || e.key === ' ')) {
    e.preventDefault()
    toggleUnitDropdown()
    return
  }

  if (isUnitDropdownOpen.value) {
    if (e.key === 'ArrowDown') {
      e.preventDefault()
      highlightedUnitIndex.value = Math.min(highlightedUnitIndex.value + 1, unitOptions.value.length - 1)
    } else if (e.key === 'ArrowUp') {
      e.preventDefault()
      highlightedUnitIndex.value = Math.max(highlightedUnitIndex.value - 1, 0)
    } else if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault()
      if (highlightedUnitIndex.value >= 0) selectUnit(unitOptions.value[highlightedUnitIndex.value])
    } else if (e.key === 'Escape') {
      e.preventDefault()
      closeUnitDropdown()
    }
  }
}

// Close on outside click
const onClickOutside = (e) => {
  const voucherEl = document.querySelector('#voucher-type-dropdown')
  const unitEl = document.querySelector('#unit-dropdown')
  if (voucherEl && !voucherEl.contains(e.target)) closeVoucherDropdown()
  if (unitEl && !unitEl.contains(e.target)) closeUnitDropdown()
}

onMounted(() => {
  document.addEventListener('click', onClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', onClickOutside)
})

// Methods
const toggleFarmerSelection = (farmer) => {
  const index = selectedFarmers.value.findIndex(f => f.id === farmer.id)
  if (index > -1) {
    selectedFarmers.value.splice(index, 1)
  } else {
    selectedFarmers.value.push(farmer)
  }
}

const isFarmerSelected = (farmer) => {
  return selectedFarmers.value.some(f => f.id === farmer.id)
}

const selectAllFiltered = () => {
  filteredFarmers.value.forEach(farmer => {
    if (!isFarmerSelected(farmer)) {
      selectedFarmers.value.push(farmer)
    }
  })
}

const clearSelection = () => {
  selectedFarmers.value = []
}

const handleBack = () => {
  router.push({'name': "agriculturist-dashboard"})
}

const navigateToVoucherList = () => {
  router.push({ name: 'agriculturist-voucher-all' })
}

const resetForm = () => {
  voucherInfo.value = {
    title: '',
    voucherType: 'SEEDS',
    unit: 'bags',
    quantity: '',
    issueDate: new Date().toISOString().split('T')[0],
    expiryDate: '',
    referenceNumber: ''
  }
  selectedFarmers.value = []
}

// Generate reference number
const generateReferenceNumber = () => {
  const date = new Date()
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const random = Math.floor(Math.random() * 10000).toString().padStart(4, '0')
  voucherInfo.value.referenceNumber = `VCH-${year}${month}-${random}`
}

// Bulk voucher creation
const createVouchers = async () => {
  if (!isFormValid.value) return
  isSubmitting.value = true
  notification.value = null
  let successCount = 0
  let failCount = 0
  for (const farmer of selectedFarmers.value) {
    const dto = {
      ownerUserId: farmer.id,
      title: voucherInfo.value.title,
      voucherType: voucherInfo.value.voucherType,
      unit: voucherInfo.value.unit,
      quantity: Number(voucherInfo.value.quantity),
      issueDate: voucherInfo.value.issueDate,
      expiryDate: voucherInfo.value.expiryDate,
      referenceNumber: voucherInfo.value.referenceNumber
    }
    const result = await voucherStore.createVoucher(dto)
    if (result.success) {
      successCount++
    } else {
      failCount++
    }
  }
  isSubmitting.value = false
  if (successCount > 0) {
    notification.value = `${successCount} voucher(s) created successfully.`
    resetForm()
  }
  if (failCount > 0) {
    notification.value = `${failCount} voucher(s) failed to create.`
  }
}

onMounted(async () => {
  await farmerStore.fetchFarmers()
  generateReferenceNumber()
})
</script>
<template>
  <AuthenticatedLayout
    :navigation="navigation"
    role-title="Municipal Agriculturist"
    page-title="Create Voucher"
  >
    <div class="p-4 sm:p-6 h-full flex flex-col space-y-6">
      <!-- Breadcrumb Navigation -->
      <nav class="flex mb-2" aria-label="Breadcrumb">
        <ol class="flex items-center space-x-4">
          <li>
            <div>
              <button
                @click="navigateToVoucherList"
                class="text-gray-400 hover:text-gray-500 flex items-center gap-1"
              >
                <span class="text-sm font-medium">Voucher List</span>
              </button>
            </div>
          </li>
          <li>
            <div class="flex items-center">
              <ChevronRightIcon class="flex-shrink-0 h-5 w-5 text-gray-400" />
              <span class="ml-4 text-sm font-medium text-green-600">
                Voucher Generator
              </span>
            </div>
          </li>
        </ol>
      </nav>

      <!-- Header Section -->
      <div class="flex flex-col sm:flex-row sm:justify-between items-start sm:items-center border-b border-gray-300 pb-3">
        <div class="flex items-center gap-2">
          <FileText class="w-6 h-6 text-green-600" />
          <h1 class="text-2xl font-semibold text-green-600 leading-tight">
            Create Voucher
          </h1>
        </div>
        <div class="flex items-center gap-3 mt-3 sm:mt-0">
          <button
            @click="resetForm"
            class="flex items-center gap-2 px-4 py-2 text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors shadow-sm"
          >
            <RotateCcw class="w-4 h-4 text-gray-500" />
            <span>Reset</span>
          </button>
          <button
            @click="createVouchers"
            :disabled="!isFormValid || isSubmitting"
            class="flex items-center gap-2 px-5 py-2 text-white bg-green-600 rounded-lg hover:bg-green-700 transition-colors disabled:bg-gray-300 disabled:cursor-not-allowed shadow-sm"
          >
            <span v-if="isSubmitting" class="animate-spin mr-2 w-4 h-4 border-2 border-white border-t-green-600 rounded-full"></span>
            <span>Create Vouchers</span>
          </button>
        </div>
      </div>
      <div v-if="notification" class="mb-4 p-3 rounded-lg bg-green-50 text-green-800 border border-green-200">
        {{ notification }}
      </div>
      <!-- Two-Column Layout -->
      <div class="grid grid-cols-1 xl:grid-cols-3 gap-6 flex-1 h-0">
        <!-- Farmer Selection: 1/3 width -->
        <div class="bg-gray-100 border border-gray-300 rounded-2xl shadow-sm p-6 flex flex-col h-full xl:col-span-1 min-h-0">
          <div class="flex items-center justify-between mb-4">
            <h2 class="text-lg font-semibold text-gray-900 flex items-center gap-2">
              <User class="w-5 h-5 text-green-600" />
              Select Farmers
            </h2>
            <span class="text-sm text-gray-600">{{ selectedFarmers.length }} selected</span>
          </div>
          <div class="relative mb-4">
            <Search class="absolute left-3 top-1/2 transform -translate-y-1/2 w-5 h-5 text-gray-400" />
            <input
              v-model="searchQuery"
              type="text"
              placeholder="Search farmers..."
              class="w-full pl-10 pr-4 py-2.5 border border-gray-300 rounded-xl focus:ring-1 focus:ring-green-500 focus:border-transparent"
            />
          </div>
          <div class="flex gap-3 mb-4">
            <button
              @click="selectAllFiltered"
              class="flex-1 py-2 text-sm text-white bg-green-600 border border-green-200 rounded-xl hover:bg-green-700 transition-colors"
            >
              Select All
            </button>
            <button
              @click="clearSelection"
              class="flex-1 py-2 text-sm text-white bg-red-500 border border-red-200 rounded-xl hover:bg-red-600 transition-colors"
            >
              Clear
            </button>
          </div>
          <div class="border-none flex-1 overflow-y-auto divide-y min-h-0">
            <div
              v-for="farmer in filteredFarmers"
              :key="farmer.id"
              @click="toggleFarmerSelection(farmer)"
              class="flex items-center justify-between p-3 hover:bg-green-50 cursor-pointer transition-colors"
            >
              <div class="flex items-center gap-3">
                <div
                  class="w-5 h-5 rounded-md border-2 flex items-center justify-center transition-all"
                  :class="isFarmerSelected(farmer) ? 'bg-green-600 border-green-600' : 'border-gray-300'"
                >
                  <Check v-if="isFarmerSelected(farmer)" class="w-3.5 h-3.5 text-white" />
                </div>
                <p class="font-medium text-gray-900">{{ farmer.name }}</p>
              </div>
            </div>
            <div v-if="filteredFarmers.length === 0" class="p-6 text-center text-gray-500 text-sm">
              No farmers found
            </div>
          </div>
        </div>
        <!-- Voucher Information: 2/3 width -->
        <div class="bg-gray-100 border border-gray-300 rounded-2xl shadow-sm p-6 flex flex-col h-full xl:col-span-2 min-h-0">
          <div class="flex items-center gap-2 mb-5">
            <FileText class="w-5 h-5 text-green-600" />
            <h2 class="text-lg font-semibold text-gray-900">Voucher Information</h2>
          </div>


          <div class="flex-1 space-y-4 pr-1 min-h-0 overflow-y-auto p-2">
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">
                Voucher Title <span class="text-red-500">*</span>
              </label>
              <input
                v-model="voucherInfo.title"
                type="text"
                placeholder="e.g., Rice Seeds Distribution 2024"
                class="w-full px-3 py-2.5 border border-gray-300 rounded-xl focus:ring-1 focus:ring-green-500 focus:border-transparent"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">
                Voucher Type <span class="text-red-500">*</span>
              </label>
              <div id="voucher-type-dropdown" class="relative">
                <button
                  type="button"
                  @click="toggleVoucherDropdown"
                  @keydown.stop.prevent="onVoucherKeyDown"
                  :aria-expanded="isVoucherDropdownOpen"
                  aria-haspopup="listbox"
                  class="w-full flex items-center justify-between px-3 py-2.5 border border-gray-300 rounded-xl bg-white shadow-sm  focus:ring-1 focus:ring-green-500 focus:border-transparent"
                >
                  <span class="text-gray-900">{{ voucherInfo.voucherType }}</span>
                  <svg
                    class="w-4 h-4 text-green-600 transform transition-transform duration-200"
                    :class="isVoucherDropdownOpen ? 'rotate-180' : ''"
                    viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg"
                    aria-hidden="true"
                  >
                    <path d="M6 8l4 4 4-4" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </button>
                <ul
                  v-show="isVoucherDropdownOpen"
                  role="listbox"
                  tabindex="-1"
                  class="origin-top-right absolute right-0 left-0 mt-2 bg-white rounded-xl shadow-lg ring-1 ring-black ring-opacity-5 overflow-auto max-h-56 py-1 focus:outline-none z-50 transition-transform duration-150"
                >
                  <li
                    v-for="(type, idx) in voucherTypes"
                    :key="type"
                    role="option"
                    :aria-selected="type === voucherInfo.voucherType"
                    @mouseenter="highlightedIndex = idx"
                    @mouseleave="highlightedIndex = -1"
                    @click="selectVoucherType(type)"
                    :class=" [
                      'px-3 py-2 cursor-pointer flex items-center justify-between',
                      highlightedIndex === idx ? 'bg-green-50' : 'hover:bg-green-50',
                      type === voucherInfo.voucherType ? 'font-semibold text-green-700' : 'text-gray-700'
                    ]"
                  >
                    <span>{{ type }}</span>
                    <svg v-if="type === voucherInfo.voucherType" class="w-4 h-4 text-green-600" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                  </li>
                </ul>
              </div>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">
                Unit <span class="text-red-500">*</span>
              </label>
              <div id="unit-dropdown" class="relative">
                <button
                  type="button"
                  @click="toggleUnitDropdown"
                  @keydown.stop.prevent="onUnitKeyDown"
                  :aria-expanded="isUnitDropdownOpen"
                  aria-haspopup="listbox"
                  class="w-full flex items-center justify-between px-3 py-2.5 border border-gray-300 rounded-xl bg-white shadow-sm  focus:ring-1 focus:ring-green-500 focus:border-transparent"
                >
                  <span class="text-gray-900">{{ voucherInfo.unit }}</span>
                  <svg
                    class="w-4 h-4 text-green-600 transform transition-transform duration-200"
                    :class="isUnitDropdownOpen ? 'rotate-180' : ''"
                    viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg"
                    aria-hidden="true"
                  >
                    <path d="M6 8l4 4 4-4" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </button>
                <ul
                  v-show="isUnitDropdownOpen"
                  role="listbox"
                  tabindex="-1"
                  class="origin-top-right absolute right-0 left-0 mt-2 bg-white rounded-xl shadow-lg ring-1 ring-black ring-opacity-5 overflow-auto max-h-56 py-1 focus:outline-none z-50 transition-transform duration-150"
                >
                  <li
                    v-for="(unit, idx) in unitOptions"
                    :key="unit"
                    role="option"
                    :aria-selected="unit === voucherInfo.unit"
                    @mouseenter="highlightedUnitIndex = idx"
                    @mouseleave="highlightedUnitIndex = -1"
                    @click="selectUnit(unit)"
                    :class=" [
                      'px-3 py-2 cursor-pointer flex items-center justify-between',
                      highlightedUnitIndex === idx ? 'bg-green-50' : 'hover:bg-green-50',
                      unit === voucherInfo.unit ? 'font-semibold text-green-700' : 'text-gray-700'
                    ]"
                  >
                    <span>{{ unit }}</span>
                    <svg v-if="unit === voucherInfo.unit" class="w-4 h-4 text-green-600" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                      <path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                  </li>
                </ul>
              </div>
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">
                Quantity <span class="text-red-500">*</span>
              </label>
              <input
                v-model="voucherInfo.quantity"
                type="number"
                min="1"
                placeholder="e.g., 5"
                class="w-full px-3 py-2.5 border border-gray-300 rounded-xl  focus:ring-1 focus:ring-green-500 focus:border-transparent"
              />
            </div>
            <div>
              <label class="block text-sm font-medium text-gray-700 mb-1">
                Reference Number
              </label>
              <div class="flex gap-2">
                <input
                  v-model="voucherInfo.referenceNumber"
                  type="text"
                  readonly
                  class="flex-1 px-3 py-2.5 border border-gray-300 rounded-xl bg-gray-50 text-gray-700 cursor-not-allowed"
                />
                <button
                  @click="generateReferenceNumber"
                  class="px-3 py-2.5 text-sm font-medium text-white bg-green-600 border border-green-200 rounded-xl hover:bg-green-700 transition-colors"
                >
                  Generate
                </button>
              </div>
            </div>
            <div class="grid grid-cols-2 gap-4">
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">
                  Issue Date
                </label>
                <input
                  v-model="voucherInfo.issueDate"
                  type="date"
                  class="w-full px-3 py-2.5 border border-gray-300 rounded-xl focus:outline-none focus:border-green-400 focus:ring-2 focus:ring-green-300 transition duration-200"
                />
              </div>
              <div>
                <label class="block text-sm font-medium text-gray-700 mb-1">
                  Expiry Date
                </label>
                <input
                  v-model="voucherInfo.expiryDate"
                  type="date"
                  class="w-full px-3 py-2.5 border border-gray-300 rounded-xl focus:outline-none focus:border-green-400 focus:ring-2 focus:ring-green-300 transition duration-200"
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </AuthenticatedLayout>
</template>

<style scoped>
/* Custom dropdown & UI polish */
::selection {
  background-color: rgba(16, 185, 129, 0.12); /* soft green selection */
}

#voucher-type-dropdown button,
#unit-dropdown button {
  -webkit-tap-highlight-color: transparent;
}

#voucher-type-dropdown ul,
#unit-dropdown ul {
  -webkit-overflow-scrolling: touch;
}

#voucher-type-dropdown li,
#unit-dropdown li {
  transition: background-color 160ms ease, color 160ms ease;
}

#voucher-type-dropdown svg,
#unit-dropdown svg {
  transition: transform 200ms ease;
}
</style>
