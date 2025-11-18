import { defineStore } from "pinia"
import { computed } from "vue"
import { useRoleStore } from "@/stores/role"
import { usePermissionStore } from "@/stores/permission"

// Authorization Store - Composition API style using composite pattern
export const useAuthorizationStore = defineStore("authorization", () => {
  const roleStore = useRoleStore()
  const permissionStore = usePermissionStore()

  // Computed properties that combine role and permission data
  const allRolesWithPermissions = computed(() => {
    return roleStore.allRoles.map(role => ({
      ...role,
      permissionDetails: role.permissions?.map(permissionId =>
        permissionStore.getPermissionById(permissionId)
      ).filter(Boolean) || []
    }))
  })

  const userHasPermission = (userRoles, permissionName) => {
    if (!Array.isArray(userRoles)) return false
    return userRoles.some(roleId =>
      roleStore.roleHasPermission(roleId, permissionName)
    )
  }

  const getUserPermissions = (userRoles) => {
    if (!Array.isArray(userRoles)) return []
    const allPermissions = new Set()
    userRoles.forEach(roleId => {
      const permissions = roleStore.getRolePermissions(roleId)
      permissions.forEach(permission => allPermissions.add(permission))
    })
    return Array.from(allPermissions)
  }

  // Initialize both stores
  const initializeStores = async () => {
    const [roleResult, permissionResult] = await Promise.all([
      roleStore.fetchRoles(),
      permissionStore.fetchPermissions()
    ])

    return {
      roles: roleResult,
      permissions: permissionResult,
      success: roleResult.success && permissionResult.success
    }
  }

  return {
    // Expose role store properties and methods
    ...roleStore,
    // Expose permission store properties and methods with prefixes to avoid conflicts
    permissions: permissionStore.permissions,
    permissionLoading: permissionStore.loading,
    permissionError: permissionStore.error,
    availablePermissions: permissionStore.availablePermissions,
    getPermissionById: permissionStore.getPermissionById,
    getPermissionByName: permissionStore.getPermissionByName,
    groupedPermissions: permissionStore.groupedPermissions,
    fetchPermissions: permissionStore.fetchPermissions,

    // Combined computed properties and methods
    allRolesWithPermissions,
    userHasPermission,
    getUserPermissions,
    initializeStores,
  }
})

// Re-export individual stores for direct access if needed
export { useRoleStore } from "@/stores/role"
export { usePermissionStore } from "@/stores/permission"

