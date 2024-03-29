library ff_native_screenshot;

import 'dart:typed_data';
import 'src/ff_native_screenshot.dart';

export 'src/ff_native_screenshot.dart';

/// The util of NativeScreenshot
class FfNativeScreenshot {
  factory FfNativeScreenshot() => _ffNativeScreenshot;
  FfNativeScreenshot._();
  static final FfNativeScreenshot _ffNativeScreenshot = FfNativeScreenshot._();
  final ScreenshotHostApi _flutterScreenshotApi = ScreenshotHostApi();

  /// take screenshot by native
  Future<Uint8List?> takeScreenshot() => _flutterScreenshotApi.takeScreenshot();

  /// ScreenshotFlutterApi setup
  void setup(ScreenshotFlutterApi api) => ScreenshotFlutterApi.setup(api);

  bool _listening = false;

  /// whether is listening Screenshot
  bool get listening => _listening;

  /// start listening Screenshot
  Future<void> startListeningScreenshot() async {
    try {
      if (!_listening) {
        _listening = true;
        _flutterScreenshotApi.startListeningScreenshot();
      }
    } catch (e) {
      _listening = false;
    }
  }

  /// stop listening Screenshot
  Future<void> stopListeningScreenshot() async {
    try {
      if (_listening) {
        _listening = false;
        _flutterScreenshotApi.stopListeningScreenshot();
      }
    } catch (e) {
      _listening = true;
    }
  }
}
