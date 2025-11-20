import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useToastStore = defineStore('toast', () => {
  const toasts = ref([])
  let toastId = 0

  const addToast = ({ message, type = 'success', duration = 3000 }) => {
    const id = ++toastId
    const toast = {
      id,
      message,
      type, // 'success', 'error', 'warning', 'info'
      duration
    }

    toasts.value.push(toast)

    // Auto remove after duration
    if (duration > 0) {
      setTimeout(() => {
        removeToast(id)
      }, duration)
    }

    return id
  }

  const removeToast = (id) => {
    const index = toasts.value.findIndex((t) => t.id === id)
    if (index > -1) {
      toasts.value.splice(index, 1)
    }
  }

  const success = (message, duration = 3000) => {
    return addToast({ message, type: 'success', duration })
  }

  const error = (message, duration = 4000) => {
    return addToast({ message, type: 'error', duration })
  }

  const warning = (message, duration = 3500) => {
    return addToast({ message, type: 'warning', duration })
  }

  const info = (message, duration = 3000) => {
    return addToast({ message, type: 'info', duration })
  }

  return {
    toasts,
    addToast,
    removeToast,
    success,
    error,
    warning,
    info
  }
})
