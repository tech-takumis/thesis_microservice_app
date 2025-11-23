<template>
  <!-- Overlay -->
  <div
    v-if="show"
    class="fixed inset-0 z-50 flex justify-end bg-white/10 backdrop-blur-sm transition-all"
    aria-labelledby="modal-title"
    role="dialog"
    aria-modal="true"
  >
    <!-- Modal Container -->
    <div
      class="bg-white h-full w-full max-w-lg border border-gray-300 transform transition-all translate-x-0 overflow-y-auto"
    >
      <!-- Header -->
      <div class="flex items-center justify-between px-6 py-4 border-b border-gray-200">
        <h3 class="text-lg font-semibold text-gray-800">Create New Batch</h3>
        <button
          @click="close"
          class="text-gray-400 hover:text-gray-600 transition"
        >
          <X class="h-5 w-5" />
        </button>
      </div>

      <!-- Form -->
      <form @submit.prevent="handleSubmit" class="px-6 py-5 space-y-4">
        <!-- Batch Name -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Batch Name</label>
          <div class="relative">
            <Tag class="absolute left-3 top-2.5 h-4 w-4 text-gray-400" />
            <input
              v-model="form.name"
              type="text"
              class="w-full pl-9 pr-3 py-2 text-sm border border-gray-300 rounded-lg placeholder-gray-400 
                      focus:ring-1 focus:ring-green-500 focus:border-transparent"
              placeholder="Enter batch name"
              required
            />
          </div>
        </div>

        <!-- Description -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Description</label>
          <div class="relative">
            <FileText class="absolute left-3 top-2.5 h-4 w-4 text-gray-400" />
            <textarea
              v-model="form.description"
              rows="2"
              class="w-full pl-9 pr-3 py-2 text-sm border border-gray-300 rounded-lg placeholder-gray-400 
                      focus:ring-1 focus:ring-green-500 focus:border-transparent resize-none"
              placeholder="Briefly describe the batch"
              required
            ></textarea>
          </div>
        </div>

<!-- Dates -->
<div class="flex items-center justify-between gap-4 text-center">
  <!-- Start Date -->
  <div class="flex flex-col flex-1">
    <label class="text-xs font-medium text-gray-700 mb-1 text-center">
      Start Date
    </label>
    <input
      v-model="form.startDate"
      type="datetime-local"
      class="w-full text-center text-xs border border-gray-300 rounded-md 
             px-2 py-1.5  focus:ring-1 focus:ring-green-500 focus:border-transparent"
      required
    />
  </div>

  <!-- End Date -->
  <div class="flex flex-col flex-1">
    <label class="text-xs font-medium text-gray-700 mb-1 text-center">
      End Date
    </label>
    <input
      v-model="form.endDate"
      type="datetime-local"
      class="w-full text-center text-xs border border-gray-300 rounded-md 
             px-2 py-1.5  focus:ring-1 focus:ring-green-500 focus:border-transparent"
      required
    />
  </div>
</div>



        <!-- Max Applications -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Max Applications</label>
          <div class="relative">
            <Users class="absolute left-3 top-2.5 h-4 w-4 text-gray-400" />
            <input
              v-model.number="form.maxApplications"
              type="number"
              min="1"
              class="w-full pl-9 pr-3 py-2 text-sm border border-gray-300 rounded-lg 
                      focus:ring-1 focus:ring-green-500 focus:border-transparent"
              placeholder="Enter maximum number"
              required
            />
          </div>
        </div>

        <!-- Availability -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Available</label>
          <div class="relative">
            <ToggleLeft class="absolute left-3 top-2.5 h-4 w-4 text-gray-400 z-10" />
            <div id="availability-dropdown" class="relative">
              <button
                type="button"
                @click="toggleAvailabilityDropdown"
                @keydown.stop.prevent="onAvailabilityKeyDown"
                :aria-expanded="isAvailabilityDropdownOpen"
                aria-haspopup="listbox"
                class="w-full flex items-center justify-between pl-9 pr-3 py-2 text-sm border border-gray-300 rounded-xl bg-white shadow-sm focus:ring-1 focus:ring-green-500 focus:border-transparent"
              >
                <span class="text-gray-900">{{ form.isAvailable ? 'Yes' : 'No' }}</span>
                <svg
                  class="w-4 h-4 text-green-600 transform transition-transform duration-200"
                  :class="isAvailabilityDropdownOpen ? 'rotate-180' : ''"
                  viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg"
                  aria-hidden="true"
                >
                  <path d="M6 8l4 4 4-4" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </button>
              <ul
                v-show="isAvailabilityDropdownOpen"
                role="listbox"
                tabindex="-1"
                class="origin-top-right absolute right-0 left-0 mt-2 bg-white rounded-xl shadow-lg ring-1 ring-black ring-opacity-5 overflow-auto max-h-56 py-1 focus:outline-none z-50 transition-transform duration-150"
              >
                <li
                  v-for="(option, idx) in availabilityOptions"
                  :key="option.value"
                  role="option"
                  :aria-selected="option.value === form.isAvailable"
                  @mouseenter="highlightedAvailabilityIndex = idx"
                  @mouseleave="highlightedAvailabilityIndex = -1"
                  @click="selectAvailability(option.value)"
                  :class=" [
                    'px-3 py-2 cursor-pointer flex items-center justify-between text-sm',
                    highlightedAvailabilityIndex === idx ? 'bg-green-50' : 'hover:bg-green-50',
                    option.value === form.isAvailable ? 'font-semibold text-green-700' : 'text-gray-700'
                  ]"
                >
                  <span>{{ option.label }}</span>
                  <svg v-if="option.value === form.isAvailable" class="w-4 h-4 text-green-600" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </li>
              </ul>
            </div>
          </div>
        </div>

        <!-- Application Type -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Application Type <span class="text-red-500">*</span></label>
          <div class="relative">
            <Layers class="absolute left-3 top-2.5 h-4 w-4 text-gray-400 z-10" />
            <div id="application-type-dropdown" class="relative">
              <button
                type="button"
                @click="toggleApplicationTypeDropdown"
                @keydown.stop.prevent="onApplicationTypeKeyDown"
                :aria-expanded="isApplicationTypeDropdownOpen"
                aria-haspopup="listbox"
                class="w-full flex items-center justify-between pl-9 pr-3 py-2 text-sm border border-gray-300 rounded-xl bg-white shadow-sm focus:ring-1 focus:ring-green-500 focus:border-transparent"
              >
                <span class="text-gray-900">{{ selectedApplicationTypeDisplay }}</span>
                <svg
                  class="w-4 h-4 text-green-600 transform transition-transform duration-200"
                  :class="isApplicationTypeDropdownOpen ? 'rotate-180' : ''"
                  viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg"
                  aria-hidden="true"
                >
                  <path d="M6 8l4 4 4-4" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </button>
              <ul
                v-show="isApplicationTypeDropdownOpen"
                role="listbox"
                tabindex="-1"
                class="origin-top-right absolute right-0 left-0 mt-2 bg-white rounded-xl shadow-lg ring-1 ring-black ring-opacity-5 overflow-auto max-h-56 py-1 focus:outline-none z-50 transition-transform duration-150"
              >
                <li
                  v-for="(type, idx) in applicationTypes"
                  :key="type.id"
                  role="option"
                  :aria-selected="type.id === form.applicationTypeId"
                  @mouseenter="highlightedApplicationTypeIndex = idx"
                  @mouseleave="highlightedApplicationTypeIndex = -1"
                  @click="selectApplicationType(type.id)"
                  :class=" [
                    'px-3 py-2 cursor-pointer flex items-center justify-between text-sm',
                    highlightedApplicationTypeIndex === idx ? 'bg-green-50' : 'hover:bg-green-50',
                    type.id === form.applicationTypeId ? 'font-semibold text-green-700' : 'text-gray-700'
                  ]"
                >
                  <span>{{ type.name }}</span>
                  <svg v-if="type.id === form.applicationTypeId" class="w-4 h-4 text-green-600" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                    <path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  </svg>
                </li>
              </ul>
            </div>
          </div>
        </div>

        <!-- Error Message -->
        <div v-if="error" class="text-red-600 text-sm">{{ error }}</div>

        <!-- Actions -->
        <div class="bg-white px-6 py-4 flex justify-end gap-3 border-t border-gray-200">
          <button
            type="button"
            class="px-4 py-2 rounded-lg text-sm font-medium text-gray-700 bg-gray-100 border border-gray-300 
                   hover:bg-gray-200 focus:ring-2 focus:ring-offset-2 focus:ring-gray-400 transition"
            @click="close"
          >
           Cancel
          </button>
          <button
            type="submit"
            class="px-4 py-2 rounded-lg text-sm font-medium text-white bg-green-600 hover:bg-green-700 
                   focus:ring-2 focus:ring-offset-2 focus:ring-green-500 transition flex items-center"
          >
           Create Batch
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, onMounted, onBeforeUnmount, computed } from 'vue'
import { useApplicationBatchStore } from '@/stores/applications'
import { useApplicationTypeStore } from '@/stores/applications'
import {
  X,
  Tag,
  FileText,
  Users,
  ToggleLeft,
  Layers,
} from 'lucide-vue-next'
const props = defineProps({
  show: Boolean
})
const emit = defineEmits(['close', 'created'])

const form = ref({
  name: '',
  description: '',
  startDate: '',
  endDate: '',
  maxApplications: 1,
  isAvailable: true,
  applicationTypeId: ''
})
const error = ref('')
const applicationTypes = ref([])
const applicationTypeStore = useApplicationTypeStore()
const batchStore = useApplicationBatchStore()

// Availability options
const availabilityOptions = [
  { label: 'Yes', value: true },
  { label: 'No', value: false }
]

// Custom dropdown state for availability
const isAvailabilityDropdownOpen = ref(false)
const highlightedAvailabilityIndex = ref(-1)

const toggleAvailabilityDropdown = () => {
  isAvailabilityDropdownOpen.value = !isAvailabilityDropdownOpen.value
  if (isAvailabilityDropdownOpen.value) {
    highlightedAvailabilityIndex.value = availabilityOptions.findIndex(opt => opt.value === form.value.isAvailable)
  }
}

const closeAvailabilityDropdown = () => {
  isAvailabilityDropdownOpen.value = false
  highlightedAvailabilityIndex.value = -1
}

const selectAvailability = (value) => {
  form.value.isAvailable = value
  closeAvailabilityDropdown()
}

const onAvailabilityKeyDown = (e) => {
  if (!isAvailabilityDropdownOpen.value && (e.key === 'Enter' || e.key === ' ')) {
    e.preventDefault()
    toggleAvailabilityDropdown()
    return
  }

  if (isAvailabilityDropdownOpen.value) {
    if (e.key === 'ArrowDown') {
      e.preventDefault()
      highlightedAvailabilityIndex.value = Math.min(highlightedAvailabilityIndex.value + 1, availabilityOptions.length - 1)
    } else if (e.key === 'ArrowUp') {
      e.preventDefault()
      highlightedAvailabilityIndex.value = Math.max(highlightedAvailabilityIndex.value - 1, 0)
    } else if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault()
      if (highlightedAvailabilityIndex.value >= 0) selectAvailability(availabilityOptions[highlightedAvailabilityIndex.value].value)
    } else if (e.key === 'Escape') {
      e.preventDefault()
      closeAvailabilityDropdown()
    }
  }
}

// Custom dropdown state for application type
const isApplicationTypeDropdownOpen = ref(false)
const highlightedApplicationTypeIndex = ref(-1)

const toggleApplicationTypeDropdown = () => {
  isApplicationTypeDropdownOpen.value = !isApplicationTypeDropdownOpen.value
  if (isApplicationTypeDropdownOpen.value) {
    highlightedApplicationTypeIndex.value = applicationTypes.value.findIndex(type => type.id === form.value.applicationTypeId)
  }
}

const closeApplicationTypeDropdown = () => {
  isApplicationTypeDropdownOpen.value = false
  highlightedApplicationTypeIndex.value = -1
}

const selectApplicationType = (typeId) => {
  form.value.applicationTypeId = typeId
  closeApplicationTypeDropdown()
}

const onApplicationTypeKeyDown = (e) => {
  if (!isApplicationTypeDropdownOpen.value && (e.key === 'Enter' || e.key === ' ')) {
    e.preventDefault()
    toggleApplicationTypeDropdown()
    return
  }

  if (isApplicationTypeDropdownOpen.value) {
    if (e.key === 'ArrowDown') {
      e.preventDefault()
      highlightedApplicationTypeIndex.value = Math.min(highlightedApplicationTypeIndex.value + 1, applicationTypes.value.length - 1)
    } else if (e.key === 'ArrowUp') {
      e.preventDefault()
      highlightedApplicationTypeIndex.value = Math.max(highlightedApplicationTypeIndex.value - 1, 0)
    } else if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault()
      if (highlightedApplicationTypeIndex.value >= 0) selectApplicationType(applicationTypes.value[highlightedApplicationTypeIndex.value].id)
    } else if (e.key === 'Escape') {
      e.preventDefault()
      closeApplicationTypeDropdown()
    }
  }
}

// Get display text for selected application type
const selectedApplicationTypeDisplay = computed(() => {
  if (!form.value.applicationTypeId) return 'Select Application Type'
  const selectedType = applicationTypes.value.find(type => type.id === form.value.applicationTypeId)
  return selectedType ? selectedType.name : 'Select Application Type'
})

// Fetch application types
const fetchTypes = async () => {
  const result = await applicationTypeStore.fetchAllApplicationTypes()
  if (result.success) {
    applicationTypes.value = result.data
  }
}

// Close on outside click
const onClickOutside = (e) => {
  const availabilityEl = document.querySelector('#availability-dropdown')
  const applicationTypeEl = document.querySelector('#application-type-dropdown')
  if (availabilityEl && !availabilityEl.contains(e.target)) closeAvailabilityDropdown()
  if (applicationTypeEl && !applicationTypeEl.contains(e.target)) closeApplicationTypeDropdown()
}

onMounted(() => {
  fetchTypes()
  document.addEventListener('click', onClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', onClickOutside)
})

watch(() => props.show, (val) => {
  if (val) {
    error.value = ''
    form.value = {
      name: '',
      description: '',
      startDate: '',
      endDate: '',
      maxApplications: 1,
      isAvailable: true,
      applicationTypeId: ''
    }
    fetchTypes()
  }
})

const close = () => emit('close')

const handleSubmit = async () => {
  error.value = ''
  if (!form.value.name || !form.value.description || !form.value.startDate || !form.value.endDate || !form.value.applicationTypeId) {
    error.value = 'Please fill in all required fields.'
    return
  }
  const payload = {
    name: form.value.name,
    description: form.value.description,
    startDate: form.value.startDate,
    endDate: form.value.endDate,
    maxApplications: form.value.maxApplications,
    isAvailable: form.value.isAvailable,
    applicationTypeId: form.value.applicationTypeId
  }
  const result = await batchStore.createApplicationBatch(payload)
  if (result && result.success) {
    emit('created')
    close()
  } else {
    error.value = result?.error || 'Failed to create batch.'
  }
}
</script>

<style scoped>
/* Custom dropdown & UI polish */
::selection {
  background-color: rgba(16, 185, 129, 0.12); /* soft green selection */
}

#availability-dropdown button,
#application-type-dropdown button {
  -webkit-tap-highlight-color: transparent;
}

#availability-dropdown ul,
#application-type-dropdown ul {
  -webkit-overflow-scrolling: touch;
}

#availability-dropdown li,
#application-type-dropdown li {
  transition: background-color 160ms ease, color 160ms ease;
}

#availability-dropdown svg,
#application-type-dropdown svg {
  transition: transform 200ms ease;
}
</style>
