import { createWebHistory, createRouter } from "vue-router"
import { useAuthStore } from "@/stores/auth"
import { ROUTES } from "@/lib/routes"

const APP_NAME = import.meta.env.VITE_APP_NAME

const routes = [
  ...ROUTES,

  // Login
  {
    path: "/",
    name: "login",
    component: () => import("@/pages/auth/Login.vue"),
    query: {
      reset: "reset",
    },
    meta: {
      title: "PCIC Staff Login",
      guard: "guest",
    },
  },
   {
    path: "/register",
    name: "register",
    component: () => import("@/pages/auth/Registration.vue"),
    meta: {
      title: "PCIC Staff Register",
      guard: "guest",
      hasToken: true,
    },
  },

  // Error Pages
  {
    path: "/access-denied",
    name: "access-denied",
    component: () => import("@/pages/errors/AccessDenied.vue"),
    meta: {
      title: "Access Denied",
    },
  },
  {
    path: "/page-not-found",
    name: "page-not-found",
    component: () => import("@/pages/errors/404.vue"),
    meta: {
      title: "Page Not Found",
    },
  },
  {
    path: "/:pathMatch(.*)*",
    redirect: "/page-not-found",
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

// Main navigation guard
router.beforeEach(async (to, from, next) => {
  const store = useAuthStore()

  const requiresAuth = to.matched.some((route) => route.meta.guard === "auth")
  const requiresGuest = to.matched.some((route) => route.meta.guard === "guest")
  const requiredRoles = to.meta.roles

  // Fetch user data if route requires auth and user is not authenticated
  if (requiresAuth && !store.isAuthenticate) {
    try {
      await store.getAuthenticated()
    } catch (error) {
      console.error("Auth check failed:", error)
      store.$reset()
      return next({ name: "login" })
    }
  }

  // Check authentication and role-based access
  if (requiresAuth) {
    if (!store.isAuthenticate) {
      return next({ name: "login" })
    }

    // Validate user has at least one role
    if (!store.userRoles || store.userRoles.length === 0) {
      return next({ name: "access-denied" })
    }

    // Check role-based access
    if (requiredRoles && requiredRoles.length > 0) {
      const hasRequiredRole = store.userRoles.some((role) =>
        requiredRoles.includes(role)
      )

      if (!hasRequiredRole) {
        // Redirect to dashboard if user doesn't have required role
        return next({ name: "dashboard" })
      }
    }
  }

  // Guest routes (login, register, etc.)
  if (requiresGuest && store.isAuthenticate) {
    return next({ name: "dashboard" })
  }

  next()
})

// Page Title and Metadata
router.beforeEach((to, from, next) => {
  const nearestWithTitle = to.matched
    .slice()
    .reverse()
    .find((r) => r.meta && r.meta.title)

  const nearestWithMeta = to.matched
    .slice()
    .reverse()
    .find((r) => r.meta && r.meta.metaTags)

  if (nearestWithTitle) {
    document.title = nearestWithTitle.meta.title + " - " + APP_NAME
  } else {
    document.title = APP_NAME
  }

  Array.from(
    document.querySelectorAll("[data-vue-router-controlled]")
  ).map((el) => el.parentNode.removeChild(el))

  if (!nearestWithMeta) return next()

  nearestWithMeta.meta.metaTags
    .map((tagDef) => {
      const tag = document.createElement("meta")

      Object.keys(tagDef).forEach((key) => {
        tag.setAttribute(key, tagDef[key])
      })

      tag.setAttribute("data-vue-router-controlled", "")

      return tag
    })
    .forEach((tag) => document.head.appendChild(tag))

  next()
})

export default router
