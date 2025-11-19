<template>
  <div class="fixed top-4 right-4 z-[60] space-y-2 pointer-events-none">
    <TransitionGroup name="toast">
      <div
        v-for="toast in toastStore.toasts"
        :key="toast.id"
        class="pointer-events-auto flex items-center gap-3 min-w-80 max-w-md px-4 py-3 rounded-lg shadow-lg border"
        :class="toastClasses(toast.type)"
      >
        <!-- Icon -->
        <div class="flex-shrink-0">
          <CheckCircleIcon v-if="toast.type === 'success'" class="h-5 w-5" />
          <ExclamationTriangleIcon v-else-if="toast.type === 'warning'" class="h-5 w-5" />
          <ExclamationCircleIcon v-else-if="toast.type === 'error'" class="h-5 w-5" />
          <InformationCircleIcon v-else class="h-5 w-5" />
        </div>

        <!-- Message -->
        <div class="flex-1 text-sm font-medium">
          {{ toast.message }}
        </div>

        <!-- Close Button -->
        <button
          @click="toastStore.removeToast(toast.id)"
          class="flex-shrink-0 hover:opacity-70 transition-opacity"
        >
          <XMarkIcon class="h-4 w-4" />
        </button>
      </div>
    </TransitionGroup>
  </div>
</template>

<script setup>
import { useToastStore } from '@/stores/toast'
import {
  CheckCircleIcon,
  ExclamationTriangleIcon,
  ExclamationCircleIcon,
  InformationCircleIcon,
  XMarkIcon
} from '@heroicons/vue/24/outline'

const toastStore = useToastStore()

const toastClasses = (type) => {
  const classes = {
    success: 'bg-green-50 border-green-200 text-green-800',
    error: 'bg-red-50 border-red-200 text-red-800',
    warning: 'bg-yellow-50 border-yellow-200 text-yellow-800',
    info: 'bg-blue-50 border-blue-200 text-blue-800'
  }
  return classes[type] || classes.info
}
</script>

<style scoped>
/* Toast animation */
.toast-enter-active,
.toast-leave-active {
  transition: all 0.3s ease;
}

.toast-enter-from {
  opacity: 0;
  transform: translateX(100%);
}

.toast-leave-to {
  opacity: 0;
  transform: translateX(100%) scale(0.9);
}

.toast-move {
  transition: transform 0.3s ease;
}
</style>
