import axios from "@/lib/axios"
import { defineStore, acceptHMRUpdate } from "pinia"
import { ref, computed } from "vue"

export const useAuthStore = defineStore("auth", () => {
  // State
  const userData = ref({})
  const isInitialized = ref(false)

  // Getters
  const isAuthenticate = computed(() => Object.keys(userData.value).length > 0)

  // Extract role names from the roles array
  const userRoles = computed(() => {
    if (!userData.value?.roles) return []
    return userData.value.roles.map((r) => r.name)
  })

  // Extract all permissions from all roles (flattened and deduplicated)
  const userPermissions = computed(() => {
    if (!userData.value?.roles) return []
    const perms = []
    userData.value.roles.forEach((role) => {
      if (role.permissions) {
        perms.push(...role.permissions)
      }
    })
    return [...new Set(perms)]
  })

  // Get the primary role (first role)
  const userPrimaryRole = computed(() => userData.value?.roles?.[0]?.name || null)

  // Actions
  async function login(form, setErrors, processing) {
    try {
      processing.value = true
      const loginResponse = await axios.post("/api/v1/pcic/auth/login", form.value)

      const { websocketToken, user } = loginResponse.data

      if (!user || !websocketToken) {
        setErrors.value = ["Invalid login response."]
        return
      }

      // Store websocket token
      localStorage.setItem("websocketToken", websocketToken)

      // Store user data
      userData.value = user

      // Validate at least one role
      if (!user.roles || user.roles.length === 0) {
        setErrors.value = ["Access denied: No valid role assigned."]
        await logout()
        return
      }

      // Redirect to dashboard
      this.router.push({ name: "dashboard" })
    } catch (error) {
      if (error.response?.status === 422) {
        setErrors.value = Object.values(error.response.data.errors).flat()
      } else if (error.response?.status === 401) {
        setErrors.value = ["Invalid username or password."]
      } else {
        setErrors.value = ["Login failed. Please try again."]
      }
    } finally {
      processing.value = false
    }
  }

  // Fetch authenticated user on browser refresh
  async function getAuthenticated() {
    try {
      const response = await axios.get("/api/v1/pcic/auth/me")
      userData.value = response.data.user || response.data
    } catch (error) {
      $reset()
      if (this.router) {
        this.router.push({ name: "login" })
      }
      throw error
    }
  }

  // Check if user has a specific permission
  function hasPermission(permission) {
    return userPermissions.value.includes(permission)
  }

  // Check if user has any of the specified roles
  function hasRole(roles) {
    if (typeof roles === "string") {
      return userRoles.value.includes(roles)
    }
    return roles.some((role) => userRoles.value.includes(role))
  }

  async function logout() {
    try {
      await axios.post("/api/v1/pcic/auth/logout")
    } catch (error) {
      if (error.response?.status !== 422) throw error
    } finally {
      $reset()
      localStorage.removeItem("websocketToken")
      this.router.push({ name: "login" })
    }
  }

  function $reset() {
    userData.value = {}
    isInitialized.value = false
    localStorage.removeItem("websocketToken")
  }

  return {
    // state
    userData,
    isInitialized,
    // getters
    isAuthenticate,
    userRoles,
    userPermissions,
    userPrimaryRole,
    // actions
    login,
    getAuthenticated,
    hasPermission,
    hasRole,
    logout,
    $reset,
  }
},
{
  persist: true,
})

if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useAuthStore, import.meta.hot))
}
