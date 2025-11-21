import 'package:flutter_local_notifications/flutter_local_notifications.dart';
import 'package:permission_handler/permission_handler.dart';

class LocalNotificationService {
  static final LocalNotificationService _instance =
      LocalNotificationService._internal();

  factory LocalNotificationService() => _instance;

  LocalNotificationService._internal();

  final FlutterLocalNotificationsPlugin _notificationsPlugin =
      FlutterLocalNotificationsPlugin();

  bool _initialized = false;

  /// Initialize the notification service
  Future<void> initialize() async {
    if (_initialized) return;

    // Android initialization settings
    const AndroidInitializationSettings androidSettings =
        AndroidInitializationSettings('@mipmap/ic_launcher');

    // iOS initialization settings
    const DarwinInitializationSettings iosSettings =
        DarwinInitializationSettings(
      requestAlertPermission: true,
      requestBadgePermission: true,
      requestSoundPermission: true,
    );

    // Combined initialization settings
    const InitializationSettings initSettings = InitializationSettings(
      android: androidSettings,
      iOS: iosSettings,
    );

    // Initialize the plugin
    await _notificationsPlugin.initialize(
      initSettings,
      onDidReceiveNotificationResponse: _onNotificationTapped,
    );

    // Request permissions for Android 13+
    await _requestPermissions();

    _initialized = true;
    print('✅ [LocalNotificationService] Initialized successfully');
  }

  /// Request notification permissions
  Future<void> _requestPermissions() async {
    if (await Permission.notification.isDenied) {
      final status = await Permission.notification.request();
      if (status.isGranted) {
        print('✅ [LocalNotificationService] Notification permission granted');
      } else if (status.isDenied) {
        print('❌ [LocalNotificationService] Notification permission denied');
      } else if (status.isPermanentlyDenied) {
        print(
            '❌ [LocalNotificationService] Notification permission permanently denied');
        // You can open app settings here if needed
        // await openAppSettings();
      }
    }
  }

  /// Handle notification tap
  void _onNotificationTapped(NotificationResponse response) {
    print(
        '🔔 [LocalNotificationService] Notification tapped: ${response.payload}');
    // TODO: Navigate to appropriate screen based on payload
    // You can use go_router or your navigation solution here
  }

  /// Show a notification
  Future<void> showNotification({
    required int id,
    required String title,
    required String body,
    String? payload,
  }) async {
    if (!_initialized) {
      print('⚠️ [LocalNotificationService] Not initialized, initializing now...');
      await initialize();
    }

    // Android notification details
    const AndroidNotificationDetails androidDetails =
        AndroidNotificationDetails(
      'application_notifications', // Channel ID
      'Application Notifications', // Channel name
      channelDescription: 'Notifications for application status updates',
      importance: Importance.high,
      priority: Priority.high,
      showWhen: true,
      enableVibration: true,
      playSound: true,
      icon: '@mipmap/ic_launcher',
    );

    // iOS notification details
    const DarwinNotificationDetails iosDetails = DarwinNotificationDetails(
      presentAlert: true,
      presentBadge: true,
      presentSound: true,
    );

    // Combined notification details
    const NotificationDetails notificationDetails = NotificationDetails(
      android: androidDetails,
      iOS: iosDetails,
    );

    try {
      await _notificationsPlugin.show(
        id,
        title,
        body,
        notificationDetails,
        payload: payload,
      );
      print('✅ [LocalNotificationService] Notification shown: $title');
    } catch (e) {
      print('❌ [LocalNotificationService] Error showing notification: $e');
    }
  }

  /// Show notification for WebSocket message
  Future<void> showWebSocketNotification({
    required String notificationId,
    required String title,
    required String message,
  }) async {
    // Generate a unique integer ID from the string ID
    final int id = notificationId.hashCode;

    await showNotification(
      id: id,
      title: title,
      body: message,
      payload: notificationId,
    );
  }

  /// Cancel a specific notification
  Future<void> cancelNotification(int id) async {
    await _notificationsPlugin.cancel(id);
  }

  /// Cancel all notifications
  Future<void> cancelAllNotifications() async {
    await _notificationsPlugin.cancelAll();
  }

  /// Create notification channel (for Android 8.0+)
  Future<void> createNotificationChannel() async {
    const AndroidNotificationChannel channel = AndroidNotificationChannel(
      'application_notifications', // Channel ID
      'Application Notifications', // Channel name
      description: 'Notifications for application status updates',
      importance: Importance.high,
      playSound: true,
      enableVibration: true,
      showBadge: true,
    );

    await _notificationsPlugin
        .resolvePlatformSpecificImplementation<
            AndroidFlutterLocalNotificationsPlugin>()
        ?.createNotificationChannel(channel);

    print('✅ [LocalNotificationService] Notification channel created');
  }

  /// Get pending notifications
  Future<List<PendingNotificationRequest>> getPendingNotifications() async {
    return await _notificationsPlugin.pendingNotificationRequests();
  }

  /// Check if notifications are enabled
  Future<bool> areNotificationsEnabled() async {
    final status = await Permission.notification.status;
    return status.isGranted;
  }
}
