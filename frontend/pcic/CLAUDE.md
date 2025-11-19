# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Vue.js 3 frontend for a Philippine Crop Insurance Corporation (PCIC) staff portal, based on Laravel Breeze authentication patterns. The application is a role-based multi-user system for managing insurance applications, claims, and risk assessment.

## Development Commands

```bash
# Install dependencies
npm install

# Run development server (port 3000)
npm run dev

# Build for production
npm run build

# Preview production build
npm run preview

# Lint check
npm run lint

# Lint with auto-fix
npm run lint:fix

# Format check with Prettier
npm run tier:check

# Format with Prettier
npm run tier:write
```

## Environment Setup

Copy `.env.example` to `.env` and configure:
- `VITE_APP_NAME`: Application name
- `VITE_PUBLIC_BACKEND_URL`: Backend API URL (example default: http://localhost:8000, but configured to http://localhost:9001 in axios.js)

The application expects a Laravel backend with Sanctum authentication at the configured backend URL.

## Architecture

### Authentication & Authorization

**Authentication Pattern**: Cookie-based sessions via Laravel Sanctum with credential mode enabled (`withCredentials: true` in axios config at `src/lib/axios.js:9`).

**Authorization System**: Role-based access control (RBAC) with hierarchical permissions:
- Roles: ADMIN, UNDERWRITER, CLAIM_PROCESSOR, TELLER
- Each role has associated permissions stored in the auth store
- Route guards check both authentication (`guard: 'auth'`) and role requirements (`roles: ['ROLE_NAME']`)
- Navigation guards in `src/router/index.js:74-143` handle authentication checks and role-based redirects

**Auth Store** (`src/stores/auth.js`):
- Manages user data, roles, and permissions
- Persisted to localStorage via `pinia-plugin-persistedstate`
- Contains role hierarchy and permission maps
- Provides computed properties: `isAuthenticate`, `userRoles`, `userPermissions`, `userPrimaryRole`
- WebSocket token stored in localStorage for real-time features

**Authentication Flow**:
1. Login (`src/stores/auth.js:62-98`):
   - POST to `/api/v1/pcic/auth/login` with credentials
   - Receives `websocketToken` and `user` object
   - Stores websocketToken in localStorage
   - Validates user has at least one role
   - Redirects to role's `defaultRoute` property
2. Session Persistence (`src/stores/auth.js:101-113`):
   - GET `/api/v1/pcic/auth/me` to fetch current user
   - Called on page refresh if user data not in store
   - Redirects to login on error
3. Logout (`src/stores/auth.js:124-135`):
   - POST to `/api/v1/pcic/auth/logout`
   - Clears store state via `$reset()`
   - Removes websocketToken from localStorage
   - Redirects to login page

### Routing Architecture

Routes are organized by role in `src/lib/routes.js`:
- `ADMIN_ROUTES`: Staff management, application types, system settings
- `UNDERWRITER_ROUTES`: Risk assessment, application review, guidelines
- `CLAIMS_PROCESSOR_ROUTES`: Claims processing dashboard
- `TELLER_ROUTES`: Payment processing dashboard

**Route Protection**: All protected routes have:
```javascript
meta: {
  title: "Page Title",
  guard: "auth",          // Requires authentication
  roles: ["ROLE_NAME"]    // Required roles (user needs at least one)
}
```

**Navigation Guards**:
1. Auth check: Fetches user data if not authenticated (`src/router/index.js:82-92`)
2. Role validation: Ensures user has required role (`src/router/index.js:110-122`)
3. Dashboard redirect: Routes to role-specific dashboard based on primary role (`src/router/index.js:126-132`)

### State Management (Pinia)

Stores in `src/stores/`:
- `auth.js`: Authentication, roles, permissions (persisted)
- `authorization.js`: Authorization helpers
- `claim.js`: Claims data
- `inspection.js`: Inspection data
- `insurance.js`: Insurance application data
- `application.js`: Application management
- `applications.js`: Multiple applications handling
- `user.js`: User management
- `role.js`: Role management
- `permission.js`: Permission management

**Store Configuration**:
- All stores use Composition API pattern (`defineStore` with setup function)
- Auth store is persisted to localStorage via `pinia-plugin-persistedstate` (`src/main.js:13`)
- All stores have access to Vue Router via plugin injection (`src/main.js:16-18`)
- HMR (Hot Module Replacement) enabled for stores in development

### Navigation System

Role-specific navigation menus defined in `src/lib/navigation.js`:
- `ADMIN_NAVIGATION`: Staff management, insurance management, system settings
- `UNDERWRITER_NAVIGATION`: Applications, risk assessment, guidelines, reports
- Each navigation item supports icons (lucide-vue-next) and nested children

Navigation is rendered via `SidebarNavigation.vue` component within `AuthenticatedLayout.vue`.

### API Integration

**Axios Instance** (`src/lib/axios.js`):
- Base URL from environment variable or defaults to `http://localhost:9001`
- Credentials mode enabled for cookie-based auth
- Request interceptor: Can add bearer tokens if needed
- Response interceptor:
  - 401 errors redirect to login (`src/lib/axios.js:34`)
  - 403 errors log access denial (`src/lib/axios.js:37-39`)

### Component Organization

```
src/components/
├── admin/           # Admin-specific components
│   └── Charts/      # Chart components for analytics
├── layouts/         # Layout components
│   ├── AuthenticatedLayout.vue
│   └── SidebarNavigation.vue
├── modals/          # Modal dialogs
├── teller/          # Teller-specific components
└── underwriter/     # Underwriter-specific components

src/pages/
├── admin/           # Admin pages
│   ├── applications/
│   ├── roles/
│   └── staff/
├── auth/            # Login page
├── claims-processor/
├── errors/          # Error pages (404, Access Denied)
├── teller/
│   └── payments/
└── underwriter/
    └── applications/
```

### Styling

- **Tailwind CSS** with custom configuration
- `@tailwindcss/forms` plugin for form styling
- Custom font: Nunito (`tailwind.config.js:8`)
- Path alias `@` resolves to `src/` directory (`vite.config.js:13`)

## Key Implementation Patterns

### Adding a New Role-Based Route

1. Define route in `src/lib/routes.js` under appropriate `*_ROUTES` array:
```javascript
{
  path: '/role/page',
  name: 'role-page-name',
  component: () => import('@/pages/role/Page.vue'),
  meta: {
    title: 'Page Title',
    guard: 'auth',
    roles: ['ROLE_NAME']
  }
}
```

2. Add navigation item in `src/lib/navigation.js` under appropriate `*_NAVIGATION` array

3. Create component in `src/pages/role/` directory

### Adding a New Pinia Store

Store pattern with composition API:
```javascript
import { defineStore, acceptHMRUpdate } from 'pinia'
import { ref, computed } from 'vue'
import axios from '@/lib/axios'

export const useMyStore = defineStore('myStore', () => {
  // State
  const data = ref([])

  // Getters
  const count = computed(() => data.value.length)

  // Actions
  async function fetchData() {
    const response = await axios.get('/api/endpoint')
    data.value = response.data
  }

  return { data, count, fetchData }
})

// Enable HMR (Hot Module Replacement) in development
if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useMyStore, import.meta.hot))
}
```

### Accessing Current User Role

```javascript
import { useAuthStore } from '@/stores/auth'

const authStore = useAuthStore()
const primaryRole = authStore.userPrimaryRole // First role
const allRoles = authStore.userRoles // Array of role names
const permissions = authStore.userPermissions // Array of permission names
```

### Role-Based Dashboard Redirect

The auth store's `getRedirectPath()` method (used in `src/router/index.js:23,120,130,138`) determines the appropriate dashboard based on user's primary role.

### Application Field Data Types

Available data types for dynamic application forms (`src/lib/navigation.js:99`):
- `TEXT`: Text input fields
- `NUMBER`: Numeric input fields
- `DATE`: Date picker fields
- `BOOLEAN`: Checkbox/toggle fields
- `FILE`: File upload fields
- `ENUM`: Dropdown/select fields
- `GEOLOCATION`: Geographic location fields

## Technology Stack

- **Framework**: Vue.js 3 with Composition API
- **Build Tool**: Vite 7.2
- **State Management**: Pinia 3.0 with persistence
- **Routing**: Vue Router 4.1
- **HTTP Client**: Axios 1.3
- **Styling**: Tailwind CSS 3.2
- **Icons**: lucide-vue-next
- **Charts**: Chart.js 4.5 + vue-chartjs 5.3
- **Forms**: @tailwindcss/forms plugin

## Recent Development Focus

Based on recent commits:
- Program monitoring for Department of Agriculture frontend
- Transaction page implementation
- Damage claim review with AI damage analysis
- Application submission details and verification workflows

## Branch Strategy

- Main branch: `master`
- Development branch: `development` (current)
- Feature branches merged via pull requests
