# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Vue.js 3 frontend application for an agricultural management system, based on Laravel Breeze authentication patterns. The application serves multiple user roles (Admin, Municipal Agriculturist, Agricultural Extension Worker) with role-based access control and real-time messaging capabilities.

## Development Commands

### Running the Application
```bash
npm run dev          # Start development server on port 5174
npm run build        # Build for production
npm run preview      # Preview production build
```

### Code Quality
```bash
npm run lint         # Lint .js and .vue files in src/
npm run lint:fix     # Auto-fix linting issues
npm run tier:check   # Check code formatting with Prettier
npm run tier:write   # Format code with Prettier
```

## Environment Configuration

The application requires a `.env` file with:
- `VITE_APP_NAME`: Application name
- `VITE_PUBLIC_BACKEND_URL`: Backend API URL (default: http://localhost:9001)

Copy `.env.example` to `.env` and configure for your environment.

## Architecture

### Authentication & Authorization
- **Cookie-based authentication** via Laravel Sanctum (configured in `src/lib/axios.js`)
- **WebSocket token** stored in localStorage for real-time messaging
- **Role-based routing** with three role types:
  - `ADMIN`: Full system access
  - Municipal Agriculturist: Application and claims management
  - Agricultural Extension Worker: Field operations and data submission
- **Auth store** (`src/stores/auth.js`) manages user state, roles, and permissions
- **Route guards** in `src/router/index.js` enforce authentication and role-based access

### State Management (Pinia)
All stores are in `src/stores/`:
- `auth.js`: User authentication, roles, permissions
- `websocket.js`: Real-time messaging with STOMP over WebSocket
- `applications.js`: Application submission and management
- `message.js`: Private messaging between users
- `notification.js`: Application notifications
- `dashboard.js`: Dashboard statistics
- Additional domain stores: `agriculture.js`, `farmer.js`, `insurance.js`, `program.js`, `transaction.js`, `verification.js`, `document.js`, `post.js`, `role.js`, `permission.js`, `schedule.js`, `psgc.js`, `ai.js`

### Routing Structure
Routes are organized by role in `src/lib/route.js`:
- `ADMIN_ROUTES`: User management, roles, applications, approvals, analytics, reports
- `MUNICIPALITY_ROUTES`: Agricultural operations, vouchers, claims, messaging
- `AGRICULTURAL_EXTENSION_WORKER_ROUTES`: Field operations, training, diagnostics

Navigation configs are in `src/lib/navigation.js` with corresponding sidebar items per role.

### Component Organization
```
src/components/
├── buttons/         # Reusable button components
├── cards/           # Card components (Stats, Application, Post, Program, Financial)
├── forms/           # Form components (e.g., PsgcLocationSelector)
├── layouts/         # SidebarNavigation
├── logo-icon/       # ApplicationLogo
├── messages/        # ChatInterface, MessageList
├── modals/          # Modal dialogs (Application details, filters, roles, etc.)
├── others/          # Utilities (LoadingSpinner, Toast, StatusBadge, PermissionGuard, RoleBasedComponent)
└── tables/          # Table components (BaseTable, ApplicationTable, ApplicationRow)
```

### Layouts
- `AuthenticatedLayout.vue`: Main layout for authenticated users with sidebar navigation
- `GuestLayout.vue`: Layout for login/register pages

### Real-time Features
WebSocket implementation using STOMP (`@stomp/stompjs`):
- Private messaging: `/user/queue/private.messages`
- Application notifications: `/user/queue/application.notifications`
- Auto-reconnection logic with exponential backoff
- Token-based authentication for WebSocket connections

### Utilities
- `src/utils/formatAddress.js`: Philippine address formatting helpers
- `src/utils/useApplicationActions.js`: Composable for application actions
- `src/utils/useApplicationFilters.js`: Composable for application filtering logic

### HTTP Client Configuration
`src/lib/axios.js`:
- Base URL from environment variable
- Credentials included on all requests (`withCredentials: true`)
- Automatic 401/403 error handling with redirect to login
- Excludes login requests from automatic 401 redirect

### Philippine Standard Geographic Code (PSGC)
`src/lib/psgcAxios.js` provides a separate axios instance for PSGC location data (regions, provinces, municipalities, barangays).

## Key Technical Patterns

### Permission & Role Checks
- Use `PermissionGuard` component or `RoleBasedComponent` for conditional rendering
- Auth store provides `hasRole(roleName)` and `hasPermission(permissionName)` methods
- Route meta includes `role` field for route-level access control

### Route Navigation Guards
The router performs:
1. Page title updates based on route meta
2. Guest route handling (redirects if authenticated)
3. Authentication initialization for protected routes
4. Role-based access checks
5. Token validation for registration routes

### Axios Interceptors
- Request: Placeholder for adding auth tokens if needed
- Response: Handles 401 (redirect to login) and 403 (access denied) errors

### Alias Configuration
`@` is aliased to `./src` in `vite.config.js` for cleaner imports.

## Styling
- **TailwindCSS** with custom Nunito font family
- **@tailwindcss/forms** plugin for form styling
- Lucide icons (`lucide-vue-next`) and Heroicons (`@heroicons/vue`)

## Testing & Debugging
- Vue DevTools plugin enabled in Vite config
- Development server runs on port 5174
- Console logging present in auth flows and WebSocket connections for debugging
