import axios from "@/lib/axios"
import { defineStore } from "pinia"
import { ref, computed } from "vue"

export const useRoleStore = defineStore("role", () => {
  const roles = ref([])
  const loading = ref(false)
  const error = ref(null)

  const allRoles = computed(() => roles.value)
  const getRoleById = (id) => roles.value.find(role => role.id === id)
  const getRoleByName = (name) => roles.value.find(role => role.name === name)
  const getRolePermissions = (roleId) => {
    const role = roles.value.find(role => role.id === roleId)
    return role ? role.permissions || [] : []
  }
  const roleHasPermission = (roleId, permissionName) => {
    const role = roles.value.find(role => role.id === roleId)
    if (!role || !role.permissions) return false
    return role.permissions.some(permission => permission.name === permissionName)
  }
  const rolesWithPermissionCounts = computed(() => {
    return roles.value.map(role => ({
      ...role,
      permissionCount: role.permissions ? role.permissions.length : 0
    }))
  })

  async function fetchRoles() {
    try {
      loading.value = true
      error.value = null
      const response = await axios.get("/api/v1/pcic/roles")
      roles.value = response.data
      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.message || err.message
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }
  async function createRole(roleData) {
    try {
      loading.value = true
      error.value = null
      const payload = {
        name: roleData.name,
        permissionIds: roleData.permissionIds || []
      }
      const response = await axios.post("/api/v1/pcic/roles", payload)
      roles.value.push(response.data)
      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.message || err.message
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  async function updateRole(roleId, roleData) {
    try {
      loading.value = true
      error.value = null
      const payload = {
        name: roleData.name,
        permissionIds: roleData.permissionIds || []
      }
      const response = await axios.put(`/api/v1/pcic/roles/${roleId}`, payload)
      const idx = roles.value.findIndex(role => role.id === roleId)
      if (idx !== -1) roles.value[idx] = response.data
      return { success: true, data: response.data }
    } catch (err) {
      error.value = err.response?.data?.message || err.message
      return { success: false, error: error.value }
    } finally {
      loading.value = false
    }
  }

  return {
    roles,
    loading,
    error,
    allRoles,
    getRoleById,
    getRoleByName,
    getRolePermissions,
    roleHasPermission,
    rolesWithPermissionCounts,
    fetchRoles,
    createRole,
    updateRole,
  }
})
