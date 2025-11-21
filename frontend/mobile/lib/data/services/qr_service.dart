import 'dart:typed_data';
import 'dart:ui' as ui;
import 'dart:convert';
import 'dart:io';
import 'package:flutter/material.dart';
import 'package:flutter/rendering.dart';
import 'package:qr_flutter/qr_flutter.dart';
import 'package:path_provider/path_provider.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:image/image.dart' as img;

/// QR Code generation options
class QRCodeOptions {
  final double width;
  final double height;
  final int margin;
  final Color darkColor;
  final Color lightColor;
  final String? embeddedImagePath;
  final double? embeddedImageSizeRatio;

  const QRCodeOptions({
    this.width = 256.0,
    this.height = 256.0,
    this.margin = 2,
    this.darkColor = Colors.black,
    this.lightColor = Colors.white,
    this.embeddedImagePath,
    this.embeddedImageSizeRatio = 0.3,
  });

  QRCodeOptions copyWith({
    double? width,
    double? height,
    int? margin,
    Color? darkColor,
    Color? lightColor,
    String? embeddedImagePath,
    double? embeddedImageSizeRatio,
  }) {
    return QRCodeOptions(
      width: width ?? this.width,
      height: height ?? this.height,
      margin: margin ?? this.margin,
      darkColor: darkColor ?? this.darkColor,
      lightColor: lightColor ?? this.lightColor,
      embeddedImagePath: embeddedImagePath ?? this.embeddedImagePath,
      embeddedImageSizeRatio: embeddedImageSizeRatio ?? this.embeddedImageSizeRatio,
    );
  }
}

/// QR Code generation result
class QRCodeResult {
  final bool success;
  final String message;
  final dynamic data;

  const QRCodeResult({
    required this.success,
    required this.message,
    this.data,
  });

  factory QRCodeResult.success(String message, dynamic data) {
    return QRCodeResult(
      success: true,
      message: message,
      data: data,
    );
  }

  factory QRCodeResult.error(String message) {
    return QRCodeResult(
      success: false,
      message: message,
      data: null,
    );
  }
}

/// QR Code Service
class QRService {
  /// Generate QR code widget
  ///
  /// Returns a widget that can be displayed in the UI
  Widget generateQRWidget(
    String data, {
    QRCodeOptions options = const QRCodeOptions(),
  }) {
    return QrImageView(
      data: data,
      version: QrVersions.auto,
      size: options.width,
      backgroundColor: options.lightColor,
      eyeStyle: QrEyeStyle(
        eyeShape: QrEyeShape.square,
        color: options.darkColor,
      ),
      dataModuleStyle: QrDataModuleStyle(
        dataModuleShape: QrDataModuleShape.square,
        color: options.darkColor,
      ),
      embeddedImage: options.embeddedImagePath != null
          ? AssetImage(options.embeddedImagePath!)
          : null,
      embeddedImageStyle: QrEmbeddedImageStyle(
        size: Size(
          options.width * (options.embeddedImageSizeRatio ?? 0.3),
          options.height * (options.embeddedImageSizeRatio ?? 0.3),
        ),
      ),
    );
  }

  /// Generate QR code as image bytes
  ///
  /// Returns Uint8List of PNG image data
  Future<QRCodeResult> generateQRBytes(
    String data, {
    QRCodeOptions options = const QRCodeOptions(),
  }) async {
    try {
      // Create QR painter
      final qrPainter = QrPainter(
        data: data,
        version: QrVersions.auto,
        errorCorrectionLevel: QrErrorCorrectLevel.H,
        color: options.darkColor,
        emptyColor: options.lightColor,
      );

      // Convert to image
      final image = await qrPainter.toImage(options.width);
      final byteData = await image.toByteData(format: ui.ImageByteFormat.png);

      if (byteData == null) {
        return QRCodeResult.error('Failed to convert QR code to image');
      }

      final bytes = byteData.buffer.asUint8List();

      return QRCodeResult.success(
        'QR code generated successfully',
        bytes,
      );
    } catch (e) {
      return QRCodeResult.error(
        e.toString(),
      );
    }
  }

  /// Generate QR code as base64 data URL (similar to JavaScript version)
  ///
  /// Returns data URL string that can be used in web contexts
  Future<QRCodeResult> generateQRDataURL(
    String data, {
    QRCodeOptions options = const QRCodeOptions(),
  }) async {
    try {
      final result = await generateQRBytes(data, options: options);

      if (!result.success || result.data == null) {
        return QRCodeResult.error(result.message);
      }

      final bytes = result.data as Uint8List;
      final base64String = base64Encode(bytes);
      final dataURL = 'data:image/png;base64,$base64String';

      return QRCodeResult.success(
        'QR code generated successfully',
        dataURL,
      );
    } catch (e) {
      return QRCodeResult.error(
        e.toString(),
      );
    }
  }

  /// Save QR code to device storage
  ///
  /// Saves the QR code image to the Downloads folder on Android
  /// or to the app documents directory on iOS
  Future<QRCodeResult> saveQRToDevice(
    String data,
    String fileName, {
    QRCodeOptions options = const QRCodeOptions(),
  }) async {
    try {
      // Generate QR code bytes
      final result = await generateQRBytes(data, options: options);

      if (!result.success || result.data == null) {
        return QRCodeResult.error(result.message);
      }

      final bytes = result.data as Uint8List;

      // Request storage permission
      if (Platform.isAndroid) {
        final status = await Permission.storage.request();
        if (!status.isGranted) {
          return QRCodeResult.error('Storage permission denied');
        }
      }

      // Get the appropriate directory
      Directory? directory;
      if (Platform.isAndroid) {
        // For Android, save to Downloads folder
        directory = Directory('/storage/emulated/0/Download');
        if (!await directory.exists()) {
          directory = await getExternalStorageDirectory();
        }
      } else {
        // For iOS, save to documents directory
        directory = await getApplicationDocumentsDirectory();
      }

      if (directory == null) {
        return QRCodeResult.error('Could not access storage directory');
      }

      // Ensure file has .png extension
      final fullFileName = fileName.endsWith('.png') ? fileName : '$fileName.png';
      final filePath = '${directory.path}/$fullFileName';

      // Write file
      final file = File(filePath);
      await file.writeAsBytes(bytes);

      return QRCodeResult.success(
        'QR code saved successfully to ${file.path}',
        filePath,
      );
    } catch (e) {
      return QRCodeResult.error(
        'Failed to save QR code: ${e.toString()}',
      );
    }
  }

  /// Download QR code (wrapper for saveQRToDevice with auto-generated filename)
  ///
  /// Automatically generates filename based on data and timestamp
  Future<QRCodeResult> downloadQR(
    String data, {
    QRCodeOptions options = const QRCodeOptions(),
    String? customFileName,
  }) async {
    try {
      final fileName = customFileName ??
          'qr_code_${DateTime.now().millisecondsSinceEpoch}';

      return await saveQRToDevice(data, fileName, options: options);
    } catch (e) {
      return QRCodeResult.error(
        'Failed to download QR code: ${e.toString()}',
      );
    }
  }

  /// Share QR code bytes
  ///
  /// Returns bytes that can be used with share packages
  Future<QRCodeResult> getQRForSharing(
    String data, {
    QRCodeOptions options = const QRCodeOptions(),
  }) async {
    return await generateQRBytes(data, options: options);
  }

  /// Validate QR data
  ///
  /// Checks if the data is valid for QR code generation
  bool isValidQRData(String data) {
    if (data.isEmpty) return false;
    // QR codes can typically handle up to 4,296 alphanumeric characters
    if (data.length > 4296) return false;
    return true;
  }

  /// Get optimal QR version for data
  ///
  /// Returns recommended QR version based on data length
  int getOptimalQRVersion(String data) {
    final length = data.length;

    if (length <= 25) return 1;
    if (length <= 47) return 2;
    if (length <= 77) return 3;
    if (length <= 114) return 4;
    if (length <= 154) return 5;
    if (length <= 195) return 6;
    if (length <= 224) return 7;
    if (length <= 279) return 8;
    if (length <= 335) return 9;
    if (length <= 395) return 10;

    return QrVersions.auto;
  }
}
