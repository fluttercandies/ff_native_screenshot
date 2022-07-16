# ff_native_screenshot

A Flutter plugin to take or listen screenshot(support Platform Views) for Android and iOS with native code.

It's a workaround for the issue [RepaintBoundary can't take screenshot of Platform Views][https://github.com/flutter/flutter/issues/102866] .

[![pub package](https://img.shields.io/pub/v/ff_native_screenshot.svg)](https://pub.dartlang.org/packages/ff_native_screenshot) [![GitHub stars](https://img.shields.io/github/stars/fluttercandies/ff_native_screenshot)](https://github.com/fluttercandies/ff_native_screenshot/stargazers) [![GitHub forks](https://img.shields.io/github/forks/fluttercandies/ff_native_screenshot)](https://github.com/fluttercandies/ff_native_screenshot/network) [![GitHub license](https://img.shields.io/github/license/fluttercandies/ff_native_screenshot)](https://github.com/fluttercandies/ff_native_screenshot/blob/master/LICENSE) [![GitHub issues](https://img.shields.io/github/issues/fluttercandies/ff_native_screenshot)](https://github.com/fluttercandies/ff_native_screenshot/issues) <a target="_blank" href="https://jq.qq.com/?_wv=1027&k=5bcc0gy"><img border="0" src="https://pub.idqqimg.com/wpa/images/group.png" alt="flutter-candies" title="flutter-candies"></a>

## Usage

``` yaml
dependencies:
  ff_native_screenshot: any
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
    FfNativeScreenshot().setup(NativeScreenshotApiImplements(context));
    FfNativeScreenshot().startListeningScreenshot();
  }

  @override
  void dispose() {
    FfNativeScreenshot().stopListeningScreenshot();
    super.dispose();
  }

  class NativeScreenshotApiImplements extends NativeScreenshotApi {
    NativeScreenshotApiImplements();
    @override
    Future<void> onTakeScreenshot(Uint8List? data) async {
     // if it has something error
     // you can call takeScreenshot 
     data ??= await FfNativeScreenshot().takeScreenshot();
    }
  }

```

