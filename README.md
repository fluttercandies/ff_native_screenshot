# ff_native_screenshot

A Flutter plugin to take or listen screenshot(support Platform Views) for Android and iOS with native code.

It's a workaround for the issue [RepaintBoundary can't take screenshot of Platform Views](https://github.com/flutter/flutter/issues/102866) .

[![pub package](https://img.shields.io/pub/v/ff_native_screenshot.svg)](https://pub.dartlang.org/packages/ff_native_screenshot) [![GitHub stars](https://img.shields.io/github/stars/fluttercandies/ff_native_screenshot)](https://github.com/fluttercandies/ff_native_screenshot/stargazers) [![GitHub forks](https://img.shields.io/github/forks/fluttercandies/ff_native_screenshot)](https://github.com/fluttercandies/ff_native_screenshot/network) [![GitHub license](https://img.shields.io/github/license/fluttercandies/ff_native_screenshot)](https://github.com/fluttercandies/ff_native_screenshot/blob/master/LICENSE) [![GitHub issues](https://img.shields.io/github/issues/fluttercandies/ff_native_screenshot)](https://github.com/fluttercandies/ff_native_screenshot/issues) <a href="https://qm.qq.com/q/ZyJbSVjfSU"><img src="https://img.shields.io/badge/dynamic/yaml?url=https%3A%2F%2Fraw.githubusercontent.com%2Ffluttercandies%2F.github%2Frefs%2Fheads%2Fmain%2Fdata.yml&query=%24.qq_group_number&style=for-the-badge&label=QQ%E7%BE%A4&logo=qq&color=1DACE8" /></a>

## Usage

``` yaml
dependencies:
  ff_native_screenshot: any
  # only for android
  permission_handler: any
```

## Take Screenshot

``` dart
Uint8List? data = await FfNativeScreenshot().takeScreenshot();
```

## Listen Screenshot

``` dart

  @override
  void initState() {
    super.initState();
    init();
  }

  Future<void> init() async {
    if (Platform.isAndroid) {
      await Permission.storage.request();
    }
    FfNativeScreenshot().setup(ScreenshotFlutterApiImplements(context));
    await FfNativeScreenshot().startListeningScreenshot();

    if (mounted) {
      setState(() {});
    }
  }

  @override
  void dispose() {
    FfNativeScreenshot().stopListeningScreenshot();
    super.dispose();
  }

  bool? listening;
  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    super.didChangeAppLifecycleState(state);
    switch (state) {
      case AppLifecycleState.resumed:
        if (listening == true && !FfNativeScreenshot().listening) {
          FfNativeScreenshot().startListeningScreenshot();
        }
        break;
      case AppLifecycleState.paused:
        listening = FfNativeScreenshot().listening;
        if (listening == true) {
          FfNativeScreenshot().stopListeningScreenshot();
        }

        break;
      default:
    }
  }

  class ScreenshotFlutterApiImplements extends ScreenshotFlutterApi {
    ScreenshotFlutterApiImplements();
    @override
    Future<void> onTakeScreenshot(Uint8List? data) async {
     // if it has something error
     // you can call takeScreenshot 
     data ??= await FfNativeScreenshot().takeScreenshot();
    }
  }

```

