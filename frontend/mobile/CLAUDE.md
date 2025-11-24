# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Flutter mobile application built for a thesis microservices architecture. The app communicates with a Spring Boot backend at `http://localhost:9001` and features real-time messaging, application submissions, and social posts.

## Development Commands

### Setup and Installation
```bash
# Install dependencies (run after cloning or updating pubspec.yaml)
flutter pub get

# Generate Hive type adapters (required before first run)
flutter pub run build_runner build --delete-conflicting-outputs
```

### Running the Application
```bash
# Run on emulator/device (debug mode)
flutter run

# Run on specific device
flutter devices          # List available devices
flutter run -d <device_id>

# Hot reload: Press 'r' in terminal
# Hot restart: Press 'R' in terminal
# Quit: Press 'q' in terminal
```

### Building
```bash
# Build APK (Android)
flutter build apk

# Build app bundle (Android - for Play Store)
flutter build appbundle

# Build iOS (requires macOS)
flutter build ios

# Clean build artifacts
flutter clean
```

### Code Generation
```bash
# Generate Hive type adapters (required after modifying @HiveType models)
flutter pub run build_runner build

# Watch mode (auto-regenerate on changes)
flutter pub run build_runner watch

# Clean previous builds before regenerating
flutter pub run build_runner build --delete-conflicting-outputs
```

### Dependencies
```bash
# Install dependencies
flutter pub get

# Update dependencies
flutter pub upgrade

# Check for outdated packages
flutter pub outdated
```

### Testing
```bash
# Run all tests
flutter test

# Run specific test file
flutter test test/widget_test.dart

# Run tests with coverage
flutter test --coverage
```

### Code Analysis
```bash
# Run static analysis (linting)
flutter analyze

# Fix auto-fixable lint issues
dart fix --apply
```

## Architecture

### Layered Architecture Pattern

The app follows a **3-layer architecture** with feature-based organization:

```
lib/
├── data/                           # Data layer
│   ├── models/                     # DTOs and response models
│   ├── services/                   # API services and external integrations
│   └── utils/                      # Helper utilities
├── presentation/                   # Presentation layer
│   ├── pages/                      # Screen widgets
│   ├── widgets/                    # Reusable UI components
│   └── controllers/                # Business logic (State Notifiers & GetX)
├── features/                       # Feature modules
│   └── messages/
│       └── providers/              # Riverpod providers
├── injection_container.dart        # Dependency injection setup
└── main.dart                       # App entry point
```

### State Management (Hybrid Approach)

The app uses **three state management solutions**:

1. **Riverpod** (Primary) - For complex state and routing
   - Router: `goRouterProvider`
   - Auth: `authProvider` (AuthNotifier)
   - Messages: `messagesProvider` (StreamProvider)
   - Notifications: `notificationProvider` (StateNotifierProvider)

2. **GetX** (Secondary) - For observable values in controllers
   - `ApplicationController.applications.obs`
   - `MessageService.to` singleton pattern

3. **ValueNotifier** - For local widget state
   - `MultiStepApplicationController` loading/error states

### Dependency Injection (GetIt)

All services are registered in `lib/injection_container.dart`:

```dart
// Access services throughout the app
final storage = getIt<StorageService>();
final authApi = getIt<AuthApiService>();
final appApi = getIt<ApplicationApiService>();
```

**Important**:
- `StorageService` uses `registerSingletonAsync` and must be awaited during initialization
- `AuthApiService` is attached to `StorageService` after registration to avoid circular dependencies
- Different Dio instances are created for different services to isolate interceptor configurations:
  - `authDio` - Used by `AuthApiService`, `PostApiService`, `NotificationApiService`, `InsuranceApiService`, `VoucherApiService` (with auth interceptor)
  - `appDio` - Used by `ApplicationApiService`
  - `psgcDio` - Used by `PSGCService` (external API)
- Registered controllers: `ApplicationController`, `InsuranceController`, `VoucherController`

### Routing (go_router)

Router configured in `lib/features/messages/providers/router_provider.dart`:
- Auth-based redirection (logged-out users → `/login`)
- Initial location determined by `authProvider.isLoggedIn`
- Routes: `/login`, `/register`, `/home`, `/profile`, `/my-vouchers`

## API Services & Backend Integration

### Base URL Configuration

All API services use `http://localhost:9001/api/v1` as the base URL. Update this in `lib/injection_container.dart` when deploying or changing backend location.

### Key Services

| Service | Purpose | File |
|---------|---------|------|
| `AuthApiService` | Login, registration, token refresh, logout | `lib/data/services/auth_api_service.dart` |
| `ApplicationApiService` | Application submissions, file uploads | `lib/data/services/application_api_service.dart` |
| `PostApiService` | Social posts with attachments | `lib/data/services/post_api_service.dart` |
| `MessageService` | Messaging with WebSocket integration | `lib/data/services/message_service.dart` |
| `NotificationApiService` | In-app notifications | `lib/data/services/notification_api.dart` |
| `WebSocketService` | STOMP WebSocket for real-time updates | `lib/data/services/websocket.dart` |
| `StorageService` | Local Hive storage for tokens/credentials | `lib/data/services/storage_service.dart` |
| `LocalNotificationService` | Phone notifications (notification tray) | `lib/data/services/local_notification_service.dart` |
| `InsuranceApiService` | Insurance record management and retrieval | `lib/data/services/insurance_api_service.dart` |
| `VoucherApiService` | Voucher management (fetch, delete by code/status) | `lib/data/services/voucher_api_service.dart` |
| `PSGCService` | Philippine geographical data (provinces, cities, barangays) | `lib/data/services/psgc_service.dart` |
| `LocationService` | GPS location fetching | `lib/data/services/location_service.dart` |
| `DocumentService` | File picking and image processing | `lib/data/services/document_service.dart` |
| `QRService` | QR code generation and handling | `lib/data/services/qr_service.dart` |

### HTTP Client (Dio)

- **Framework**: Dio v5.8.0 with custom interceptors
- **Auth Interceptor**: Automatically adds `Authorization: Bearer <token>` header
- **Token Refresh**: On 401 responses, attempts token refresh and retries request
- **Logging**: Enabled via `LogInterceptor` for debugging

### WebSocket Communication

- **Protocol**: STOMP over WebSocket
- **URL**: `ws://localhost:9001/ws`
- **Subscriptions**:
  - `/user/queue/private.messages` - Private messages (shows local notification)
  - `/user/queue/application.notifications` - Application status updates (shows local notification)
- **Authentication**: Bearer token passed during connection

### Local Notifications (Phone Notification Tray)

The app uses `flutter_local_notifications` to show notifications in the phone's notification tray:

- **Initialization**: `LocalNotificationService` is initialized during app startup in `injection_container.dart`
- **Permissions**: Automatically requests notification permissions on Android 13+ (POST_NOTIFICATIONS)
- **Notification Channel**: `application_notifications` channel with high priority
- **Triggers**: Automatically shows notifications when:
  - WebSocket receives application notifications (`/user/queue/application.notifications`)
  - WebSocket receives private messages (`/user/queue/private.messages`)
- **Works when app is closed**: Notifications appear even when the app is in background or closed (as long as WebSocket connection is active)
- **Configuration**: AndroidManifest.xml includes required permissions and receivers for boot completion and scheduled notifications

## App Initialization Sequence

The app follows this initialization sequence in `main.dart`:

1. **Flutter Binding**: `WidgetsFlutterBinding.ensureInitialized()`
2. **Dependency Setup**: `setupDependencies()` from `injection_container.dart`
   - Registers Dio instances
   - Registers and awaits `StorageService.init()` (loads tokens from Hive)
   - Registers `AuthApiService` and attaches it to `StorageService`
   - Registers all other API services and controllers
3. **Hive Initialization**: `Hive.initFlutter()` and adapter registration
4. **App Launch**: `ProviderScope` wraps `AppEntry` widget
5. **Auth Check**: `authProvider` validates tokens and determines initial route
6. **Router Setup**: `goRouterProvider` creates router with appropriate initial location

## Authentication Flow

1. **App Launch**: `StorageService.init()` loads cached tokens from Hive
2. **Token Validation**: `AuthNotifier._checkLoginStatus()` verifies JWT expiry
3. **Auto-Refresh**: Tokens refresh automatically when:
   - 401 error received from API
   - Token expires within 10 minutes
4. **Login**: Credentials stored in `StorageService`, WebSocket connected
5. **Logout**: All storage cleared, WebSocket disconnected

## Local Storage (Hive)

**Boxes**:
- Main box: Auth tokens, user credentials
- `saved_credentials`: Login credentials for autofill (uses `SavedCredential` adapter)

**Stored Keys** (in `StorageService`):
- `access_token` - JWT access token
- `refresh_token` - JWT refresh token
- `websocket_token` - WebSocket authentication token
- `user_credentials` - Current user data

**Code Generation**: After modifying any `@HiveType` models, run:
```bash
flutter pub run build_runner build --delete-conflicting-outputs
```

## Error Handling

The app has comprehensive error handling via `ApiErrorHandler` (`lib/data/utils/api_error_handler.dart`):

- **Parsing**: Extracts backend `ApiErrorResponse` structure
- **User-Friendly Messages**: Converts technical errors to actionable messages
- **Error Classification**: Determines if errors are recoverable
- **Suggested Actions**: Provides context-aware recommendations

See `API_ERROR_HANDLING_SUMMARY.md` for detailed documentation.

## Multi-Step Application System

`MultiStepApplicationController` handles complex form submissions:
- Dynamic form generation from backend application definitions
- Multiple field types: text, select, file upload, location (PSGC), boundary coordinates
- Per-step validation using `GlobalKey<FormState>`
- File upload with progress tracking
- Auto-fill from user profile

## Key Models

Located in `lib/data/models/`:

- `UserCredentials` - User data with auth tokens
- `SavedCredential` - Cached login credentials (Hive adapter)
- `ApplicationData` - Application forms and responses
- `Message` - Chat messages
- `ApplicationNotification` - In-app notifications
- `PostModels` - Social posts with pagination
- `AuthResponse` / `RegistrationRequest` - Auth DTOs
- `PsgcModels` - Philippine geographical hierarchy
- `InsuranceModels` - Insurance records and responses
- `VoucherModels` - Voucher DTOs with status enums

## Code Style & Linting

- **Linter**: `flutter_lints` package
- **Configuration**: `analysis_options.yaml`
- **Note**: `avoid_print` is ignored (prints allowed for debugging)

## Important Implementation Notes

### Circular Dependency Resolution

`StorageService` and `AuthApiService` have a circular dependency:
- `AuthApiService` needs `StorageService` for token management
- `StorageService` needs `AuthApiService` for token refresh

**Solution** (implemented in `injection_container.dart`):
```dart
// 1. Register StorageService first (async) without AuthApiService
getIt.registerSingletonAsync<StorageService>(() async {
  final storageService = StorageService();
  await storageService.init();
  return storageService;
});

// 2. Wait for StorageService to be ready
final storage = await getIt.getAsync<StorageService>();

// 3. Create and register AuthApiService
final authApi = AuthApiService(authDio, baseUrl: '...', storageService: storage);
getIt.registerSingleton<AuthApiService>(authApi);

// 4. Attach AuthApiService to StorageService
await storage.attachAuthApiService(authApi);
```

### State Management Fragmentation

The app currently uses Riverpod, GetX, and ValueNotifier together. When extending state management:
- **Prefer Riverpod** for new features
- Use `StateNotifierProvider` for complex state
- Use `StreamProvider` for real-time data streams
- Avoid adding more GetX controllers

### Feature Organization

Only the `messages` feature is organized in a feature folder. Other features (auth, applications, posts) are organized by layer. When adding new features, consider migrating to feature-based structure:

```
features/
└── <feature_name>/
    ├── data/          # Models, services
    ├── presentation/  # Pages, widgets, controllers
    └── providers/     # Riverpod providers
```

### WebSocket Initialization

WebSocket connection is initialized after successful login in `AuthNotifier`. Ensure token is available before calling `WebSocketService.connect()`.

### File Uploads

When uploading files via `ApplicationApiService`:
- Use `MultipartFile.fromBytes()` for cross-platform compatibility
- Include MIME type detection via `mime` package
- Handle progress callbacks for large file uploads
- Maximum file size enforced by backend (handle 413 errors)

## Backend Assumptions

This app assumes the backend provides:
1. JWT-based authentication with access/refresh tokens
2. `ApiErrorResponse` structure: `{success, message, status, timestamp, details}`
3. Application form definitions with sections and field specifications
4. STOMP WebSocket endpoint at `/ws`
5. Philippine PSGC data for location selection

## Key Pages and Features

The app includes these main screens (in `lib/presentation/pages/`):
- `login_page.dart` / `multi_step_register_page.dart` - Authentication
- `home_page.dart` - Main feed with social posts
- `application_page.dart` / `multi_step_application_page.dart` - Application forms
- `application_tracker_page.dart` / `application_detail_page.dart` - Application status
- `contact_department_page.dart` - Messaging interface
- `notification_page.dart` - In-app notifications
- `profile_page.dart` - User profile
- `my_voucher_page.dart` - User vouchers management

## Troubleshooting

### "Hive box not found" error
Run code generation: `flutter pub run build_runner build --delete-conflicting-outputs`

### "401 Unauthorized" loop
Clear app storage and re-login. Token refresh might be failing.

### WebSocket not connecting
Verify backend is running at `http://localhost:9001` and WebSocket endpoint is accessible.

### Location permission denied
Check `android/app/src/main/AndroidManifest.xml` and `ios/Runner/Info.plist` for location permissions.

### Hot reload not working after changing models
Run `flutter pub run build_runner build --delete-conflicting-outputs` to regenerate Hive adapters, then hot restart (press 'R').

### Notifications not showing on Android 13+
Ensure notification permissions are granted. The app requests permissions automatically, but if denied, you need to manually enable them in:
Settings > Apps > mobile > Notifications
