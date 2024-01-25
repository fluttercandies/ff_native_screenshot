import 'package:pigeon/pigeon.dart';

// flutter create -i objc -a java --org com.fluttercandies.plugins --template plugin --platforms ios,android  ff_native_screenshot
// dart run pigeon --input pigeons/ff_native_screenshot.dart
@ConfigurePigeon(PigeonOptions(
  input: 'pigeons/ff_native_screenshot.dart',
  dartOut: 'lib/src/ff_native_screenshot.dart',
  objcHeaderOut: 'ios/Classes/ScreenshotApi.h',
  objcSourceOut: 'ios/Classes/ScreenshotApi.m',
  objcOptions: ObjcOptions(prefix: 'FLT'),
  javaOut:
      'android/src/main/java/com/fluttercandies/plugins/ff_native_screenshot/ScreenshotApi.java',
  javaOptions: JavaOptions(
    package: 'com.fluttercandies.plugins.ff_native_screenshot',
  ),
  arkTSOut:
      'ohos/ff_native_screenshot/src/main/ets/components/plugin/ScreenshotApi.ets',
  arkTSOptions: ArkTSOptions(),
))

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
