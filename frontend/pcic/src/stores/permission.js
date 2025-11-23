import axios from "@/lib/axios"
import { defineStore } from "pinia"
import { ref, computed } from "vue"

export const usePermissionStore = defineStore("permission", () => {
  const permissions = ref([])
  const loading = ref(false)
  const error = ref(null)

  const availablePermissions = computed(() => permissions.value)
  const getPermissionById = (id) => permissions.value.find(permission => permission.id === id)
  const getPermissionByName = (name) => permissions.value.find(permission => permission.name === name)
  const groupedPermissions = computed(() => {
    const grouped = {}
    permissions.value.forEach(permission => {
      const category = permission.name.split('_')[1] || 'Other'
      if (!grouped[category]) grouped[category] = []
      grouped[category].push(permission)
    })
    return grouped
  })

  async function fetchPermissions() {
    try {
      loading.value = true
      error.value = null
      const response = await axios.get("/api/v1/pcic/permissions")
      permissions.value = response.data
      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.message || err.message
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  return {
    permissions,
    loading,
    error,
    availablePermissions,
    getPermissionById,
    getPermissionByName,
    groupedPermissions,
    fetchPermissions,
  }
})
