import 'package:pigeon/pigeon.dart';

/// Flutter call Native
@HostApi()
abstract class ScreenshotHostApi {
  @async
  Uint8List? takeScreenshot();

  void startListeningScreenshot();
  void stopListeningScreenshot();
}

/// Native call Flutter
@FlutterApi()
abstract class ScreenshotFlutterApi {
  void onTakeScreenshot(Uint8List? data);
}
