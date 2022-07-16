
# run_pigeon.h
flutter pub run pigeon \
  --input pigeons/ff_native_screenshot.dart \
  --dart_out lib/src/ff_native_screenshot.dart \
  --objc_header_out ios/Classes/ScreenshotApi.h \
  --objc_source_out ios/Classes/ScreenshotApi.m \
  --objc_prefix FLT \
  --java_out android/src/main/java/com/fluttercandies/plugins/ff_native_screenshot/ScreenshotApi.java \
  --java_package "com.fluttercandies.plugins.ff_native_screenshot"


