library ff_native_screenshot;

import 'dart:typed_data';
import 'src/ff_native_screenshot.dart';

export 'src/ff_native_screenshot.dart';

class FfNativeScreenshot {
  factory FfNativeScreenshot() => _ffNativeScreenshot;
  FfNativeScreenshot._();
  static final FfNativeScreenshot _ffNativeScreenshot = FfNativeScreenshot._();
  final FlutterScreenshotApi _flutterScreenshotApi = FlutterScreenshotApi();

  /// take screenshot by native
  Future<Uint8List?> takeScreenshot() => _flutterScreenshotApi.takeScreenshot();

  void setup(NativeScreenshotApi api) => NativeScreenshotApi.setup(api);

  bool _listening = false;

  /// whether is listening Screenshot
  bool get listening => _listening;

  Future<void> startListeningScreenshot() async {
    await _flutterScreenshotApi.startListeningScreenshot();
    _listening = true;
  }

  Future<void> stopListeningScreenshot() async {
    await _flutterScreenshotApi.stopListeningScreenshot();
    _listening = false;
  }
}
