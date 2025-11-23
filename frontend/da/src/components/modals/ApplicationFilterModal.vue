<template>
  <div
    v-if="show"
    class="fixed inset-0 z-50 flex justify-end bg-white/10 backdrop-blur-sm transition-all"
    aria-labelledby="modal-title"
    role="dialog"
    aria-modal="true"
  >
    <div
      class="bg-white h-full w-full max-w-md border border-gray-300 transform transition-all translate-x-0"
    >
      <!-- Header -->
      <div class="flex items-center justify-between px-6 py-4 border-b border-gray-200">
        <h3 class="text-lg font-semibold text-gray-800">Filter Applications</h3>
        <button
          @click="closeModal"
          class="text-gray-400 hover:text-gray-600 transition"
        >
          <X class="h-5 w-5" />
        </button>
      </div>

      <!-- Body -->
      <div class="px-6 py-5 space-y-4">
        <!-- Batch Name -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Batch Name</label>
          <div id="batch-dropdown" class="relative">
            <button
              type="button"
              @click="toggleBatchDropdown"
              @keydown.stop.prevent="onBatchKeyDown"
              :aria-expanded="isBatchDropdownOpen"
              aria-haspopup="listbox"
              class="w-full flex items-center justify-between px-3 py-2 text-sm border border-gray-300 rounded-xl bg-white shadow-sm focus:ring-1 focus:ring-green-500 focus:border-transparent"
            >
              <span class="text-gray-900">{{ selectedBatchDisplay }}</span>
              <svg
                class="w-4 h-4 text-green-600 transform transition-transform duration-200"
                :class="isBatchDropdownOpen ? 'rotate-180' : ''"
                viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg"
                aria-hidden="true"
              >
                <path d="M6 8l4 4 4-4" stroke="currentColor" stroke-width="1.75" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>
            <ul
              v-show="isBatchDropdownOpen"
              role="listbox"
              tabindex="-1"
              class="origin-top-right absolute right-0 left-0 mt-2 bg-white rounded-xl shadow-lg ring-1 ring-black ring-opacity-5 overflow-auto max-h-56 py-1 focus:outline-none z-50 transition-transform duration-150"
            >
              <li
                v-for="(batch, idx) in batchOptions"
                :key="batch.id || 'all'"
                role="option"
                :aria-selected="(batch.name === 'All Batches' && !localFilters.batchName) || batch.name === localFilters.batchName"
                @mouseenter="highlightedBatchIndex = idx"
                @mouseleave="highlightedBatchIndex = -1"
                @click="selectBatch(batch)"
                :class=" [
                  'px-3 py-2 cursor-pointer flex items-center justify-between text-sm',
                  highlightedBatchIndex === idx ? 'bg-green-50' : 'hover:bg-green-50',
                  ((batch.name === 'All Batches' && !localFilters.batchName) || batch.name === localFilters.batchName) ? 'font-semibold text-green-700' : 'text-gray-700'
                ]"
              >
                <span>{{ batch.name }}</span>
                <svg v-if="(batch.name === 'All Batches' && !localFilters.batchName) || batch.name === localFilters.batchName" class="w-4 h-4 text-green-600" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M20 6L9 17l-5-5" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </li>
            </ul>
          </div>
        </div>

        <!-- Location -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">Location</label>
          <input
            v-model="localFilters.location"
            type="text"
            placeholder="Search by barangay, city, or province"
            class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg  focus:ring-1 focus:ring-green-500 focus:border-transparent"
          />
        </div>

        <!-- Submitted Date Range -->
        <div class="grid grid-cols-2 gap-3">
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Date From</label>
            <input
              v-model="localFilters.dateFrom"
              type="date"
              class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg  focus:ring-1 focus:ring-green-500 focus:border-transparent"
            />
          </div>
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-1">Date To</label>
            <input
              v-model="localFilters.dateTo"
              type="date"
              class="w-full px-3 py-2 text-sm border border-gray-300 rounded-lg  focus:ring-1 focus:ring-green-500 focus:border-transparent"
            />
          </div>
        </div>
      </div>

      <!-- Footer -->
      <div class="bg-white px-6 py-4 flex justify-end gap-3 border-t border-gray-200">
        <button
          @click="handleReset"
          class="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg shadow-sm hover:bg-gray-100 transition"
        >
          Reset
        </button>
        <button
          @click="handleApply"
          class="px-4 py-2 text-sm font-medium text-white bg-green-600 rounded-lg shadow-sm hover:bg-green-700 focus:ring-2 focus:ring-offset-2 focus:ring-green-500 transition"
        >
          Apply Filters
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Custom dropdown & UI polish */
::selection {
  background-color: rgba(16, 185, 129, 0.12); /* soft green selection */
}

#batch-dropdown button {
  -webkit-tap-highlight-color: transparent;
}

#batch-dropdown ul {
  -webkit-overflow-scrolling: touch;
}

#batch-dropdown li {
  transition: background-color 160ms ease, color 160ms ease;
}

#batch-dropdown svg {
  transition: transform 200ms ease;
}
</style>


<script setup>
import { ref, watch, computed, onMounted, onBeforeUnmount } from 'vue'
import { X } from 'lucide-vue-next'

const props = defineProps({
    show: {
        type: Boolean,
        required: true
    },
    filters: {
        type: Object,
        required: true
    },
    batches: {
        type: Array,
        default: () => []
    }
})

const emit = defineEmits(['update:show', 'apply-filters', 'reset-filters'])

const localFilters = ref({ ...props.filters })

// Watch for external filter changes
watch(() => props.filters, (newFilters) => {
    localFilters.value = { ...newFilters }
}, { deep: true })

// Batch options with "All Batches" option
const batchOptions = computed(() => {
    return [
        { id: '', name: 'All Batches' },
        ...props.batches
    ]
})

// Custom dropdown state for batch name
const isBatchDropdownOpen = ref(false)
const highlightedBatchIndex = ref(-1)

const toggleBatchDropdown = () => {
    isBatchDropdownOpen.value = !isBatchDropdownOpen.value
    if (isBatchDropdownOpen.value) {
        const currentIndex = batchOptions.value.findIndex(b => b.name === localFilters.value.batchName || (b.name === 'All Batches' && !localFilters.value.batchName))
        highlightedBatchIndex.value = currentIndex >= 0 ? currentIndex : 0
    }
}

const closeBatchDropdown = () => {
    isBatchDropdownOpen.value = false
    highlightedBatchIndex.value = -1
}

const selectBatch = (batch) => {
    localFilters.value.batchName = batch.name === 'All Batches' ? '' : batch.name
    closeBatchDropdown()
}

const onBatchKeyDown = (e) => {
    if (!isBatchDropdownOpen.value && (e.key === 'Enter' || e.key === ' ')) {
        e.preventDefault()
        toggleBatchDropdown()
        return
    }

    if (isBatchDropdownOpen.value) {
        if (e.key === 'ArrowDown') {
            e.preventDefault()
            highlightedBatchIndex.value = Math.min(highlightedBatchIndex.value + 1, batchOptions.value.length - 1)
        } else if (e.key === 'ArrowUp') {
            e.preventDefault()
            highlightedBatchIndex.value = Math.max(highlightedBatchIndex.value - 1, 0)
        } else if (e.key === 'Enter' || e.key === ' ') {
            e.preventDefault()
            if (highlightedBatchIndex.value >= 0) selectBatch(batchOptions.value[highlightedBatchIndex.value])
        } else if (e.key === 'Escape') {
            e.preventDefault()
            closeBatchDropdown()
        }
    }
}

// Close on outside click
const onClickOutside = (e) => {
    const el = document.querySelector('#batch-dropdown')
    if (!el) return
    if (!el.contains(e.target)) closeBatchDropdown()
}

onMounted(() => {
    document.addEventListener('click', onClickOutside)
})

onBeforeUnmount(() => {
    document.removeEventListener('click', onClickOutside)
})

const closeModal = () => {
    emit('update:show', false)
}

const handleApply = () => {
    emit('apply-filters', localFilters.value)
}

const handleReset = () => {
    localFilters.value = {
        batchName: '',
        location: '',
        dateFrom: '',
        dateTo: ''
    }
    emit('reset-filters')
}

// Get display text for selected batch
const selectedBatchDisplay = computed(() => {
    if (!localFilters.value.batchName) return 'All Batches'
    return localFilters.value.batchName
})
</script>
