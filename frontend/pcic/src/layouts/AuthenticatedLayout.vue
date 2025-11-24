<template>
  <div class="h-screen bg-white flex flex-col">
    <!-- Mobile Sidebar (Off-canvas) -->
    <div v-if="sidebarOpen" class="fixed inset-0 z-40 md:hidden">
      <!-- Overlay -->
      <div
        class="fixed inset-0 bg-gray-600 bg-opacity-75"
        @click="sidebarOpen = false"
      ></div>

      <!-- Sidebar -->
      <div class="relative flex flex-col w-64 max-w-xs bg-white h-full rounded-lg">
        <div class="absolute top-0 right-0 -mr-12 pt-2">
          <button
            @click="sidebarOpen = false"
            class="ml-1 flex items-center justify-center h-10 w-10 rounded-full focus:outline-none focus:ring-2 focus:ring-inset focus:ring-white"
          >
            <X class="h-6 w-6 text-white" />
            <span class="sr-only">Close sidebar</span>
          </button>
        </div>

        <SidebarNavigation
          :navigation="navigation"
          :role-title="roleTitle"
          :user-full-name="store.userData?.fullName"
          :user-email="store.userData?.email"
          :user-initials="userInitials"
          :is-collapsed="false"
          @logout="handleLogout"
        />
      </div>
    </div>

    <!-- Main Container -->
    <div class="flex-1 flex flex-col md:flex-row overflow-hidden px-1 md:px-2 py-2 md:py-3 gap-2 md:gap-3 rounded-lg">
      <!-- Sidebar: fixed and flush to viewport edge on md+ -->
      <aside
        :class="[
          'hidden md:flex md:flex-col md:fixed md:inset-y-0 md:left-0 md:h-screen bg-white md:rounded-none overflow-hidden transition-all duration-300 md:z-20',
          isCollapsed ? 'md:w-20' : 'md:w-72'
        ]"
      >

        <SidebarNavigation
          :navigation="navigation"
          :role-title="roleTitle"
          :user-full-name="store.userData?.fullName"
          :user-email="store.userData?.email"
          :user-initials="userInitials"
          :is-collapsed="isCollapsed"
          @logout="handleLogout"
          @toggle-sidebar="toggleSidebar"
        />
      </aside>

      <!-- Dashboard: Slight gap to right side -->
      <main
        :class="[
          'flex-1 flex flex-col bg-white rounded-lg overflow-hidden',
          isCollapsed ? 'md:ml-20' : 'md:ml-72'
        ]"
      >
        <!-- Mobile Top Bar -->
        <div
          class="md:hidden bg-white border-b border-gray-200 px-4 py-3 flex items-center justify-between"
        >
          <button
            @click="sidebarOpen = true"
            class="p-2 rounded-md text-gray-400 hover:text-gray-600 hover:bg-gray-100"
            aria-label="Open sidebar"
          >
            <Menu class="h-6 w-6" />
          </button>
          <h1 class="text-lg font-semibold text-gray-900">{{ pageTitle }}</h1>
          <div class="w-10"></div>
        </div>

        <!-- Page Header -->
        <header
          v-if="$slots.header"
          class="bg-white flex-shrink-0"
        >
          <div class="px-2 py-2">
            <slot name="header" />
          </div>
        </header>

        <!-- Dashboard Content -->
        <div class="flex-1 bg-white overflow-hidden">
          <div class="px-2 py-2 h-full overflow-y-auto scrollbar-hide">
            <slot />
          </div>
        </div>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Menu, X } from 'lucide-vue-next'
import { useAuthStore } from '@/stores/auth'
import SidebarNavigation from '@/components/layouts/SidebarNavigation.vue'
import { NAVIGATION } from '@/lib/navigation'

const store = useAuthStore()

const props = defineProps({
  roleTitle: {
    type: String,
    default: 'Staff Portal'
  },
  pageTitle: {
    type: String,
    default: 'Dashboard'
  }
})

// Filter navigation items based on user roles
const navigation = computed(() => {
  const userRoles = store.userRoles || []
  return NAVIGATION.filter((item) => {
    if (!item.roles) return true
    return item.roles.some((role) => userRoles.includes(role))
  })
})

const SIDEBAR_STORAGE_KEY = 'pcic_sidebar_collapsed'

const sidebarOpen = ref(false)

// Initialize from localStorage
const getSavedSidebarState = () => {
  try {
    const saved = localStorage.getItem(SIDEBAR_STORAGE_KEY)
    return saved ? JSON.parse(saved) : false
  } catch (error) {
    console.error('Error reading sidebar state from localStorage:', error)
    return false
  }
}

const isCollapsed = ref(getSavedSidebarState())

const userInitials = computed(() => {
  const name = store.userData?.fullName
  if (!name) return ''
  return name.split(' ').map(n => n[0]).join('').toUpperCase()
})

const toggleSidebar = () => {
  isCollapsed.value = !isCollapsed.value
}

const handleLogout = () => {
  store.logout()
}

// Watch for changes and save to localStorage
watch(isCollapsed, (newValue) => {
  try {
    localStorage.setItem(SIDEBAR_STORAGE_KEY, JSON.stringify(newValue))
  } catch (error) {
    console.error('Error saving sidebar state to localStorage:', error)
  }
})
</script>
