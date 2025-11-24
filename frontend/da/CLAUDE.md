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
- `VITE_PUBLIC_BACKEND_URL`: Backend API URL (defaults to http://localhost:9001 if not set)

Copy `.env.example` to `.env` and configure for your environment.

**Important**: The backend default is port 9001 (not 8000 as shown in `.env.example`). The WebSocket URL in `src/stores/websocket.js` is hardcoded to `ws://localhost:9001/ws`. Update both if using a different port.

## Additional Libraries

- **QR Code**: `qrcode` for generation, `html5-qrcode` for scanning (used in voucher features)
- **Animations**: `vue3-lottie` for Lottie animations
- **PSGC**: `psgc` npm package for Philippine Standard Geographic Code utilities

## Architecture

### Authentication & Authorization
- **Cookie-based authentication** via Laravel Sanctum (configured in `src/lib/axios.js`)
  - Cookies automatically sent with every request (`withCredentials: true`)
  - Login endpoint: `/api/v1/agriculture/auth/login`
  - Logout endpoint: `/api/v1/agriculture/auth/logout`
  - Current user endpoint: `/api/v1/agriculture/auth/me`
- **WebSocket token** returned during login and stored in localStorage for real-time messaging
- **Role-based routing** with primary role types:
  - `ADMIN`: Full system access (user management, analytics, reports)
  - `PERSOR`: Personnel/registration role (user registration)
  - Municipal Agriculturist: Application and claims management, voucher generation
  - Agricultural Extension Worker: Field operations and data submission
- **Auth store** (`src/stores/auth.js`) manages user state, roles, and permissions
  - User data includes nested roles and permissions structure
  - `hasRole(roleName)` method for role checks (case-insensitive)
  - `hasPermission(permissionName)` method for permission checks (supports string or array)
  - `defaultRoute` computed property returns unified dashboard (`/agriculturist/dashboard`) for all authenticated users
- **Route guards** in `src/router/index.js` enforce authentication and role-based access
  - Authenticated users redirected from login to unified dashboard (`/agriculturist/dashboard`)
  - Protected routes require authentication initialization
  - Registration route requires valid token in query parameter
  - Guest routes (login/register) redirect authenticated users to dashboard

### State Management (Pinia)
All stores are in `src/stores/`:
- `auth.js`: User authentication, roles, permissions
- `websocket.js`: Real-time messaging with STOMP over WebSocket
- `applications.js`: Application submission and management
- `message.js`: Private messaging between users
- `notification.js`: Application notifications
- `dashboard.js`: Dashboard statistics
- Additional domain stores: `agriculture.js`, `farmer.js`, `insurance.js`, `program.js`, `transaction.js`, `verification.js`, `document.js`, `post.js`, `role.js`, `permission.js`, `schedule.js`, `psgc.js`, `ai.js`, `claim.js`, `voucher.js`

### Routing Structure
Routes are organized by role in `src/lib/route.js`:
- `DASHBOARD_ROUTE`: Unified dashboard for all authenticated users (`/agriculturist/dashboard`)
- `ADMIN_ROUTES`: User management, roles, applications, approvals, analytics, reports
- `MUNICIPALITY_ROUTES`: Agricultural operations, vouchers, claims, messaging, AI damage analysis
- `AGRICULTURAL_EXTENSION_WORKER_ROUTES`: Field operations, training, diagnostics

Navigation config in `src/lib/navigation.js` provides `UNIFIED_NAVIGATION` - a single navigation structure where access is controlled by roles and permissions rather than separate navigations per role. Navigation items can specify:
- `roles`: Array of role names that can see this navigation item
- `permissions`: Array of permissions required to access this navigation item
- Children items inherit parent constraints and can add additional restrictions

Key route patterns:
- Application detail pages: `/role/submit-crop-data/application-type/submission-detail/:id/:applicationTypeId`
- Application location map: `/agriculturist/submit-crop-data/application/:applicationId/map`
- Application verification: `/agriculturist/application/verification/:applicationId/:applicationTypeId`
- Voucher management: `/agriculturist/voucher/*` for generation, listing, and details
- AI damage analysis: `/agriculturist/ai/damage-result/:applicationId/:applicationTypeId`
- Claims detail: `/agriculturist/process-claims/:id/detail`
- Transaction detail: `/agriculturist/transactions/:id/detail`
- Program detail: `/agriculturist/monitor-programs/:id/detail`

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
├── others/          # Utilities (LoadingSpinner, Toast, NotificationToast, StatusBadge, PermissionGuard, RoleBasedComponent)
└── tables/          # Table components (BaseTable, ApplicationTable, ApplicationRow)
```

### Layouts
- `AuthenticatedLayout.vue`: Main layout for authenticated users with sidebar navigation
- `GuestLayout.vue`: Layout for login/register pages

### Real-time Features
WebSocket implementation using STOMP (`@stomp/stompjs`) in `src/stores/websocket.js`:
- Connection URL: `ws://localhost:9001/ws?token={webSocketToken}`
- WebSocket token obtained during login and stored in localStorage as `webSocketToken`
- Subscription topics:
  - Private messaging: `/user/queue/private.messages`
  - Application notifications: `/user/queue/application.notifications`
- Auto-reconnection logic with 10-second delay (max 5 attempts)
- Connection state tracked with `connected` ref
- Subscriptions managed with cleanup on disconnect
- Message store (`src/stores/message.js`) handles incoming messages
- Notification store (`src/stores/notification.js`) handles incoming notifications

### Utilities
- `src/utils/formatAddress.js`: Philippine address formatting helpers
- `src/utils/useApplicationActions.js`: Composable for application actions
- `src/utils/useApplicationFilters.js`: Composable for application filtering logic

### HTTP Client Configuration
`src/lib/axios.js`:
- Base URL from environment variable (defaults to `http://localhost:9001`)
- Credentials included on all requests (`withCredentials: true`)
- Automatic error handling:
  - **401 Unauthorized**: Redirects to login (excludes login requests)
  - **403 Forbidden**: Logs access denied errors
  - **503 Service Unavailable**: Shows toast notification and redirects to role-appropriate dashboard

### Philippine Standard Geographic Code (PSGC)
`src/lib/psgcAxios.js` provides a separate axios instance for PSGC location data (regions, provinces, municipalities, barangays).

## Key Technical Patterns

### Permission & Role Checks
- Use `PermissionGuard` component or `RoleBasedComponent` for conditional rendering in templates
- Auth store provides helper methods:
  - `hasRole(roleName)`: Case-insensitive role check
  - `hasPermission(permissionName)`: Permission check (accepts string or array)
- Route meta includes `permissions` field for route-level access control
- Navigation items can specify `roles` and `permissions` arrays to control visibility
- Role names are case-insensitive in checks but stored as-is from backend
- Common roles: `ADMIN`, `PERSOR`
- Check `src/lib/route.js` for role-specific route definitions

### Route Navigation Guards
The router performs:
1. Page title updates based on route meta
2. Guest route handling (redirects if authenticated)
3. Authentication initialization for protected routes
4. Permission-based access checks
5. Token validation for registration routes

### API Communication Patterns
- All API calls go through the configured axios instance (`src/lib/axios.js`)
- Store actions consistently return objects with `{ success: boolean, data/error, message }` structure
- Base API paths:
  - Authentication: `/api/v1/agriculture/auth/*`
  - Applications: `/api/v1/applications/*`
  - Other resources follow `/api/v1/{resource}` pattern
- PSGC location data fetched from external API: `https://psgc.gitlab.io/api`
- Registration requires a token query parameter: `/api/v1/agriculture/auth/registration?token={token}`

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
