import 'package:pigeon/pigeon.dart';

/// Flutter call Native
@HostApi()
abstract class FlutterScreenshotApi {
  @async
  Uint8List? takeScreenshot();

  void startListeningScreenshot();
  void stopListeningScreenshot();
}

/// Native call Flutter
@FlutterApi()
abstract class NativeScreenshotApi {
  void onTakeScreenshot(Uint8List? data);
}
