<template>
  <!-- Backdrop -->
  <Transition name="fade">
    <div
      v-if="isOpen"
      @click="closeModal"
      class="fixed inset-0 bg-black bg-opacity-25 z-40"
    ></div>
  </Transition>

  <Transition name="slide-right">
    <div
      v-if="isOpen"
      class="fixed inset-y-0 right-0 z-50 w-96 bg-white shadow-2xl overflow-y-auto"
    >
      <!-- Modal Header -->
      <div class="sticky top-0 bg-white border-b border-gray-200 px-6 py-4">
        <div class="flex items-center justify-between">
          <h3 class="text-lg font-semibold text-gray-900">Schedule Inspection</h3>
          <button
            @click="closeModal"
            class="text-gray-400 hover:text-gray-600 transition-colors"
            :disabled="isSubmitting"
          >
            <XMarkIcon class="h-5 w-5" />
          </button>
        </div>
      </div>

      <!-- Modal Body -->
      <div class="px-6 py-4 space-y-5">
        <!-- Type Field -->
        <div>
          <label for="type" class="block text-sm font-medium text-gray-700 mb-1">
            Schedule Type <span class="text-red-500">*</span>
          </label>
          <select
            id="type"
            v-model="formData.type"
            class="w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 text-sm"
            :disabled="isSubmitting"
          >
            <option value="">Select type</option>
            <option value="INSPECTION">Inspection</option>
            <option value="VISIT">Visit</option>
            <option value="TRAINING">Training</option>
            <option value="MEETING">Meeting</option>
            <option value="WORKSHOP">Workshop</option>
          </select>
          <p v-if="errors.type" class="mt-1 text-xs text-red-600">{{ errors.type }}</p>
        </div>

        <!-- Schedule Date Field -->
        <div>
          <label for="scheduleDate" class="block text-sm font-medium text-gray-700 mb-1">
            Schedule Date <span class="text-red-500">*</span>
          </label>
          <input
            id="scheduleDate"
            type="datetime-local"
            v-model="formData.scheduleDate"
            class="w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 text-sm"
            :disabled="isSubmitting"
          />
          <p v-if="errors.scheduleDate" class="mt-1 text-xs text-red-600">{{ errors.scheduleDate }}</p>
        </div>

        <!-- Priority Field -->
        <div>
          <label for="priority" class="block text-sm font-medium text-gray-700 mb-1">
            Priority <span class="text-red-500">*</span>
          </label>
          <select
            id="priority"
            v-model="formData.priority"
            class="w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 text-sm"
            :disabled="isSubmitting"
          >
            <option value="">Select priority</option>
            <option value="LOW">Low</option>
            <option value="MEDIUM">Medium</option>
            <option value="HIGH">High</option>
          </select>
          <p v-if="errors.priority" class="mt-1 text-xs text-red-600">{{ errors.priority }}</p>
        </div>

        <!-- Notes Field -->
        <div>
          <label for="notes" class="block text-sm font-medium text-gray-700 mb-1">
            Notes
          </label>
          <textarea
            id="notes"
            v-model="formData.notes"
            rows="4"
            placeholder="Add any additional notes or instructions for the inspection..."
            class="w-full rounded-md border-gray-300 shadow-sm focus:border-blue-500 focus:ring-blue-500 text-sm"
            :disabled="isSubmitting"
          ></textarea>
          <p v-if="errors.notes" class="mt-1 text-xs text-red-600">{{ errors.notes }}</p>
        </div>

        <!-- Error Message -->
        <div v-if="errorMessage" class="rounded-md bg-red-50 border border-red-200 p-3">
          <p class="text-sm text-red-800">{{ errorMessage }}</p>
        </div>
      </div>

      <!-- Modal Footer -->
      <div class="sticky bottom-0 bg-gray-50 border-t border-gray-200 px-6 py-4">
        <div class="flex gap-3">
          <button
            @click="submitSchedule"
            :disabled="isSubmitting"
            class="flex-1 inline-flex justify-center items-center px-4 py-2 text-sm font-medium text-white bg-blue-600 rounded-md hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            <span v-if="!isSubmitting">Schedule Inspection</span>
            <span v-else class="flex items-center gap-2">
              <svg class="animate-spin h-4 w-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
                <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
              </svg>
              Scheduling...
            </span>
          </button>
          <button
            @click="closeModal"
            :disabled="isSubmitting"
            class="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
          >
            Cancel
          </button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<script setup>
import { ref, watch } from 'vue'
import { XMarkIcon } from '@heroicons/vue/24/outline'
import { useInspectionStore } from '@/stores/inspection'

const props = defineProps({
  isOpen: {
    type: Boolean,
    default: false
  },
  insuranceId: {
    type: [String, Number],
    required: true
  }
})

const emit = defineEmits(['close', 'success'])

const inspectionStore = useInspectionStore()

const formData = ref({
  type: 'INSPECTION',
  scheduleDate: '',
  priority: 'MEDIUM',
  notes: ''
})

const errors = ref({
  type: '',
  scheduleDate: '',
  priority: '',
  notes: ''
})

const isSubmitting = ref(false)
const errorMessage = ref('')

// Reset form when modal opens
watch(() => props.isOpen, (newVal) => {
  if (newVal) {
    resetForm()
  }
})

function resetForm() {
  formData.value = {
    type: 'INSPECTION',
    scheduleDate: '',
    priority: 'MEDIUM',
    notes: ''
  }
  errors.value = {
    type: '',
    scheduleDate: '',
    priority: '',
    notes: ''
  }
  errorMessage.value = ''
}

function validateForm() {
  let isValid = true
  errors.value = {
    type: '',
    scheduleDate: '',
    priority: '',
    notes: ''
  }

  if (!formData.value.type) {
    errors.value.type = 'Schedule type is required'
    isValid = false
  }

  if (!formData.value.scheduleDate) {
    errors.value.scheduleDate = 'Schedule date is required'
    isValid = false
  }

  if (!formData.value.priority) {
    errors.value.priority = 'Priority is required'
    isValid = false
  }

  return isValid
}

async function submitSchedule() {
  errorMessage.value = ''

  if (!validateForm()) {
    return
  }

  try {
    isSubmitting.value = true

    // Prepare the data
    const scheduleData = {
      type: formData.value.type,
      scheduleDate: formData.value.scheduleDate,
      priority: formData.value.priority,
      notes: formData.value.notes || null
    }

    const result = await inspectionStore.scheduleInspection(props.insuranceId, scheduleData)

    if (result.success) {
      // Emit success immediately and close modal
      emit('success', result.data)
      closeModal()
    } else {
      errorMessage.value = result.message || result.error || 'Failed to schedule inspection'
    }
  } catch (error) {
    errorMessage.value = error.message || 'An unexpected error occurred'
  } finally {
    isSubmitting.value = false
  }
}

function closeModal() {
  if (!isSubmitting.value) {
    emit('close')
  }
}
</script>

<style scoped>
/* Slide from right animation */
.slide-right-enter-active,
.slide-right-leave-active {
  transition: transform 0.3s ease-out;
}

.slide-right-enter-from {
  transform: translateX(100%);
}

.slide-right-leave-to {
  transform: translateX(100%);
}

/* Fade animation for backdrop */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
