<template>
  <div class="flex flex-col h-screen w-full bg-green-600 border border-gray-300">
    <!-- Logo / Header -->
    <div class="relative flex flex-col items-center py-4 px-4 bg-green-600 border-b border-green-700 flex-shrink-0">
      <!-- Toggle Button (Top Right when expanded, Center when collapsed) -->
      <button
        @click="$emit('toggle-sidebar')"
        :class="[
          'p-2 rounded-lg text-white hover:bg-green-700 transition-all duration-200',
          isCollapsed
            ? 'mb-3'
            : 'absolute top-3 right-3'
        ]"
        :title="isCollapsed ? 'Expand sidebar' : 'Collapse sidebar'"
      >
        <Menu v-if="isCollapsed" class="h-5 w-5" />
        <X v-else class="h-5 w-5" />
      </button>

      <!-- Logo -->
      <img src="@/assets/pcic.svg" alt="PCIC Logo" :class="[isCollapsed ? 'h-10 w-8 mb-2' : 'h-12 w-10 mb-2']" />

      <!-- Title (Hidden when collapsed) -->
      <h1 v-if="!isCollapsed" class="text-2xl font-bold text-white leading-tight">PCIC</h1>
    </div>

    <!-- Navigation -->
    <nav
      ref="navMenu"
      class="flex-1 px-4 py-4 space-y-1 bg-green-600 overflow-y-auto min-h-0"
    >
      <template v-for="item in navigation" :key="item.name || item.title">
        <!-- Single Navigation Item -->
        <router-link
          v-if="!item.children && item.to"
          :to="item.to"
          :class="[
            isActive(item.to, { exact: item.exact })
              ? 'bg-white text-green-700 font-semibold shadow-sm'
              : 'text-white hover:bg-green-700 hover:text-white',
            'group flex items-center px-3 py-2.5 text-sm rounded-md transition-colors duration-200',
            isCollapsed ? 'justify-center' : ''
          ]"
          :title="isCollapsed ? item.title || item.name : ''"
        >
          <component
            :is="item.icon"
            :class="[
              isActive(item.to, { exact: item.exact })
                ? 'text-yellow-500'
                : 'text-white group-hover:text-yellow-300',
              'h-5 w-5 transition-colors duration-200',
              isCollapsed ? '' : 'mr-3'
            ]"
          />
          <span v-if="!isCollapsed">{{ item.title || item.name }}</span>
        </router-link>

        <!-- Disabled Item -->
        <span
          v-else-if="!item.children && !item.to"
          :class="[
            'group flex items-center px-3 py-2.5 text-sm rounded-md transition-colors duration-200 text-green-100 opacity-50 cursor-not-allowed',
            isCollapsed ? 'justify-center' : ''
          ]"
          :title="isCollapsed ? item.title || item.name : ''"
        >
          <component
            :is="item.icon"
            :class="[
              'h-5 w-5 text-green-200',
              isCollapsed ? '' : 'mr-3'
            ]"
          />
          <span v-if="!isCollapsed">{{ item.title || item.name }}</span>
        </span>

        <!-- Navigation Group with Children -->
        <div v-else class="relative">
          <button
            @click.stop="handleGroupClick(item)"
            :class="[
              'text-white hover:bg-green-700 hover:text-white',
              'group w-full flex items-center justify-between px-3 py-2.5 text-sm rounded-md transition-colors duration-200',
              isCollapsed ? 'justify-center' : ''
            ]"
            :title="isCollapsed ? item.title || item.name : ''"
          >
            <div :class="['flex items-center', isCollapsed ? '' : '']">
              <component
                :is="item.icon"
                :class="[
                  'text-white group-hover:text-yellow-300 h-5 w-5 transition-colors duration-200',
                  isCollapsed ? '' : 'mr-3'
                ]"
              />
              <span v-if="!isCollapsed">{{ item.title || item.name }}</span>
            </div>
            <ChevronDown
              v-if="!isCollapsed"
              :class="[
                expandedGroups.includes(item.title || item.name)
                  ? 'rotate-180 text-yellow-300'
                  : 'text-white',
                'h-4 w-4 transition-transform duration-200'
              ]"
            />
          </button>

          <!-- Submenu (Hidden when collapsed) -->
          <div
            v-if="!isCollapsed"
            v-show="expandedGroups.includes(item.title || item.name)"
            class="mt-1 pl-3 border-l-2 border-green-700 space-y-1 transition-all duration-300 ease-in-out"
          >
            <template v-for="child in getChildren(item)" :key="child.name || child.title">
              <router-link
                v-if="child.to"
                :to="child.to"
                @click.stop="handleChildRouteClick(item)"
                :class="[
                  isActive(child.to)
                    ? 'bg-white text-green-700 font-semibold shadow-sm'
                    : 'text-white hover:bg-green-700 hover:text-yellow-300',
                  'group relative flex items-center pl-5 pr-3 py-2 text-sm rounded-md transition-colors duration-200'
                ]"
              >
                <span
                  :class="[
                    'absolute -left-2 top-1/2 -translate-y-1/2 w-2 h-0.5 rounded-full',
                    isActive(child.to)
                      ? 'bg-green-700'
                      : 'bg-green-700 group-hover:bg-green-400'
                  ]"
                ></span>
                {{ child.title || child.name }}
              </router-link>

              <span
                v-else
                class="group relative flex items-center pl-6 pr-3 py-2 text-sm rounded-md transition-colors duration-200 text-green-100 opacity-50 cursor-not-allowed"
              >
                {{ child.title || child.name }}
              </span>
            </template>
          </div>
        </div>
      </template>
    </nav>

    <!-- Logout Section -->
    <div class="flex-shrink-0 px-4 py-4 bg-green-600">
      <button
        @click="$emit('logout')"
        :class="[
          'group flex w-full items-center px-4 py-2.5 text-sm font-semibold bg-white text-red-600 rounded-lg transition-all duration-200 hover:bg-red-500 hover:text-white hover:shadow-md active:scale-95',
          isCollapsed ? 'justify-center' : 'justify-center'
        ]"
        :title="isCollapsed ? 'Logout' : ''"
      >
        <LogOut :class="['h-4 w-4', isCollapsed ? '' : 'mr-2']" />
        <span v-if="!isCollapsed">Logout</span>
      </button>
    </div>
  </div>
</template>
<script setup>
import { ref, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ChevronDown, LogOut, Menu, X } from 'lucide-vue-next'

const route = useRoute()
const router = useRouter()

const props = defineProps({
  navigation: {
    type: Array,
    default: () => []
  },
  roleTitle: {
    type: String,
    default: 'Staff Portal'
  },
  isCollapsed: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['logout', 'toggle-sidebar'])
const expandedGroups = ref([])
const navMenu = ref(null) // sidebar reference

const ensureGroupExpanded = (groupName) => {
  if (!groupName) return
  if (!expandedGroups.value.includes(groupName)) {
    // Close all other groups and open only this one (accordion behavior)
    expandedGroups.value = [groupName]
  }
}

const normalizePath = (path) => {
  if (!path) return path
  if (path.length > 1 && path.endsWith('/')) {
    return path.slice(0, -1)
  }
  return path
}

const resolveTarget = (href) => {
  if (!href) return {}
  if (typeof href === 'string') {
    return { path: href }
  }

  if (typeof href === 'object') {
    const { name, path } = href
    let resolved
    try {
      resolved = router.resolve(href)
    } catch (error) {
      console.warn('[SidebarNavigation] Unable to resolve route', href, error)
    }

    return {
      path: path || resolved?.path,
      name: name || resolved?.name
    }
  }

  return {}
}

// --- Active route detection ---
const isActive = (href, options = {}) => {
  if (!href) return false

  const { exact = false } = options
  const target = resolveTarget(href)
  const currentPath = normalizePath(route.path)
  const currentName = route.name

  if (target.name && target.name === currentName) {
    return true
  }

  if (target.path) {
    const targetPath = normalizePath(target.path)

    if (exact) {
      return currentPath === targetPath
    }

    if (currentPath === targetPath) return true
    return currentPath.startsWith(`${targetPath}/`)
  }

  return false
}

// --- Detect if any child route is active ---
const hasActiveChild = (item) => {
  if (!item.children || !Array.isArray(item.children)) return false
  return item.children.some(child => child.to && isActive(child.to))
}

// --- Auto-expand groups that have active children ---
const expandActiveGroups = () => {
  // Don't auto-expand when collapsed
  if (props.isCollapsed) return

  props.navigation.forEach(item => {
    if (item.children && hasActiveChild(item)) {
      const groupName = item.title || item.name
      ensureGroupExpanded(groupName)
    }
  })
}

// --- Expand active groups initially and on route change ---
watch(
  () => [route.path, route.name, props.isCollapsed],
  () => {
    expandActiveGroups()
  },
  { immediate: true }
)

// --- Handle group click (with children) ---
const handleGroupClick = (item) => {
  const groupName = item.title || item.name

  // When collapsed, expand the sidebar first
  if (props.isCollapsed) {
    emit('toggle-sidebar')
    // After expanding, ensure this group is expanded
    nextTick(() => {
      ensureGroupExpanded(groupName)
    })
    return
  }

  // When expanded, toggle the group normally
  toggleGroup(groupName)
}

// --- Toggle group open/close (Accordion style - only one group open at a time) ---
const toggleGroup = (groupName) => {
  const index = expandedGroups.value.indexOf(groupName)
  if (index > -1) {
    // Close the group
    expandedGroups.value.splice(index, 1)
  } else {
    // Close all other groups and open this one (accordion behavior)
    expandedGroups.value = [groupName]
  }
}

const handleChildRouteClick = (parentItem) => {
  const groupName = parentItem.title || parentItem.name
  ensureGroupExpanded(groupName)
}

// --- Handle outside clicks ---
// This version ensures internal router-link clicks don't trigger close
const handleOutsideClick = (e) => {
  if (!navMenu.value) return

  // If click is inside sidebar, do nothing
  if (navMenu.value.contains(e.target)) return

  // Otherwise, collapse all dropdowns
  expandedGroups.value = []
}

// --- Reopen dropdown after internal navigation ---
watch(
  () => [route.path, route.name],
  async () => {
    await nextTick()
    props.navigation.forEach(item => {
      if (item.children) {
        const groupName = item.title || item.name
        const isChildActive = item.children.some(child => child.to && isActive(child.to))
        if (isChildActive) {
          ensureGroupExpanded(groupName)
        }
      }
    })
  }
)

onMounted(() => {
  document.addEventListener('click', handleOutsideClick)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleOutsideClick)
})

// --- Safe getChildren ---
function getChildren(item) {
  return Array.isArray(item.children) ? item.children : []
}
</script>


