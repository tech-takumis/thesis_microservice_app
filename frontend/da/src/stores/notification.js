import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import axios from '@/lib/axios'

export const useNotificationStore = defineStore('notification', () => {
    // State for local notifications (existing functionality)
    const notifications = ref([])
    const nextId = ref(0)

    // State for API notifications
    const apiNotifications = ref([])
    const loading = ref(false)
    const error = ref(null)

    // Computed
    const isLoading = computed(() => loading.value)
    const hasError = computed(() => error.value != null)
    const errorMessage = computed(() => error.value)

    const addNotification = (type, message, duration = 5000) => {
        const id = nextId.value++
        const notification = {
            id,
            type, // 'success', 'error', 'info', 'warning'
            message,
            duration,
            visible: true,
            timestamp: Date.now()
        }

        notifications.value.push(notification)

        // Auto remove notification after duration
        if (duration > 0) {
            setTimeout(() => {
                removeNotification(id)
            }, duration)
        }

        return id
    }

    const removeNotification = (id) => {
        const index = notifications.value.findIndex(n => n.id === id)
        if (index > -1) {
            notifications.value[index].visible = false
            // Remove from array after animation
            setTimeout(() => {
                notifications.value.splice(index, 1)
            }, 300)
        }
    }

    const clearAllNotifications = () => {
        notifications.value.forEach(n => n.visible = false)
        setTimeout(() => {
            notifications.value.length = 0
        }, 300)
    }

    // Convenience methods
    const showSuccess = (message, duration = 5000) => {
        return addNotification('success', message, duration)
    }

    const showError = (message, duration = 7000) => {
        return addNotification('error', message, duration)
    }

    const showInfo = (message, duration = 5000) => {
        return addNotification('info', message, duration)
    }

    const showWarning = (message, duration = 6000) => {
        return addNotification('warning', message, duration)
    }

    // API Methods for backend notification management
    const createNotification = async (notificationData) => {
        try {
            loading.value = true
            error.value = null

            const response = await axios.post('/api/v1/notifications', notificationData)

            return {
                success: true,
                message: "Notification created successfully",
                data: response.data
            }
        } catch (err) {
            const errorMessage = err.response?.data?.message || err.message || "Failed to create notification"
            error.value = errorMessage
            return {
                success: false,
                message: errorMessage,
                data: null
            }
        } finally {
            loading.value = false
        }
    }

    const createFarmersNotification = async (farmersNotificationData) => {
        try {
            loading.value = true
            error.value = null

            const response = await axios.post('/api/v1/notifications/farmers', farmersNotificationData)

            return {
                success: true,
                message: response.data || "Farmers notifications sent successfully",
                data: response.data
            }
        } catch (err) {
            const errorMessage = err.response?.data?.message || err.message || "Failed to send farmers notifications"
            error.value = errorMessage
            return {
                success: false,
                message: errorMessage,
                data: null
            }
        } finally {
            loading.value = false
        }
    }

    const getNotificationsForUser = async (recipientId) => {
        try {
            loading.value = true
            error.value = null

            const response = await axios.get(`/api/v1/notifications/${recipientId}`)
            const notifications = Array.isArray(response.data) ? response.data : []
            
            apiNotifications.value = notifications

            return {
                success: true,
                message: "Notifications retrieved successfully",
                data: notifications
            }
        } catch (err) {
            const errorMessage = err.response?.data?.message || err.message || "Failed to retrieve notifications"
            error.value = errorMessage
            apiNotifications.value = []
            return {
                success: false,
                message: errorMessage,
                data: []
            }
        } finally {
            loading.value = false
        }
    }

    const markNotificationAsRead = async (notificationId) => {
        try {
            loading.value = true
            error.value = null

            await axios.put(`/api/v1/notifications/${notificationId}/read`)

            // Update local state if notification exists
            const notification = apiNotifications.value.find(n => n.id === notificationId)
            if (notification) {
                notification.read = true
            }

            return {
                success: true,
                message: "Notification marked as read"
            }
        } catch (err) {
            const errorMessage = err.response?.data?.message || err.message || "Failed to mark notification as read"
            error.value = errorMessage
            return {
                success: false,
                message: errorMessage
            }
        } finally {
            loading.value = false
        }
    }

    const deleteNotification = async (notificationId) => {
        try {
            loading.value = true
            error.value = null

            await axios.delete(`/api/v1/notifications/${notificationId}`)

            // Remove from local state
            apiNotifications.value = apiNotifications.value.filter(n => n.id !== notificationId)

            return {
                success: true,
                message: "Notification deleted successfully"
            }
        } catch (err) {
            const errorMessage = err.response?.data?.message || err.message || "Failed to delete notification"
            error.value = errorMessage
            return {
                success: false,
                message: errorMessage
            }
        } finally {
            loading.value = false
        }
    }

    return {
        // Existing local notification state
        notifications,
        
        // API notification state
        apiNotifications,
        loading,
        error,
        isLoading,
        hasError,
        errorMessage,

        // Existing local notification methods (for components)
        addNotification,
        removeNotification,
        clearAllNotifications,
        showSuccess,
        showError,
        showInfo,
        showWarning,

        // New API methods (for backend integration)
        createNotification,
        createFarmersNotification,
        getNotificationsForUser,
        markNotificationAsRead,
        deleteNotification
    }
})
