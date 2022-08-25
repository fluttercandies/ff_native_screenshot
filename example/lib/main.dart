import 'dart:io';
import 'dart:typed_data';

import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:ff_native_screenshot/ff_native_screenshot.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:webview_flutter/webview_flutter.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({Key? key}) : super(key: key);

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return const MaterialApp(home: HomePage());
  }
}

class HomePage extends StatefulWidget {
  const HomePage({Key? key}) : super(key: key);

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> with WidgetsBindingObserver {
  @override
  void initState() {
    super.initState();
    init();
    WidgetsBinding.instance.addObserver(this);
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
    WidgetsBinding.instance.removeObserver(this);
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

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Native Screenshot'),
        actions: <Widget>[
          IconButton(
            onPressed: () {
              takeScreenshot(context);
            },
            icon: const Icon(
              Icons.screenshot,
            ),
          ),
          IconButton(
              onPressed: () {
                _listeningScreenshotTapped();
              },
              icon: FfNativeScreenshot().listening
                  ? const Icon(
                      Icons.stop,
                    )
                  : const Icon(Icons.start))
        ],
      ),
      body: WebView(
        initialUrl: 'https://flutter.cn',
        //
        // android
        // initExpensiveAndroidView
        // On some Android devices, transparent backgrounds can cause
        // rendering issues on the non hybrid composition
        // AndroidViewSurface. This switches the WebView to Hybrid
        // Composition when the background color is not 100% opaque.
        // hybridComposition:
        // backgroundColor != null && backgroundColor.opacity < 1.0,

        // if we don't use initExpensiveAndroidView, can't get shotshot from currentActivity
        backgroundColor:
            Platform.isAndroid ? Colors.white.withOpacity(0.99) : null,
      ),
    );
  }

  Future<void> _listeningScreenshotTapped() async {
    if (FfNativeScreenshot().listening) {
      await FfNativeScreenshot().stopListeningScreenshot();
    } else {
      await FfNativeScreenshot().startListeningScreenshot();
    }
    if (mounted) {
      setState(() {});
    }
  }

  Future<void> takeScreenshot(BuildContext context) async {
    Uint8List? data = await FfNativeScreenshot().takeScreenshot();
    showScreenshotDialog(data, context);
  }

  static void showScreenshotDialog(Uint8List? bytes, BuildContext context) {
    if (bytes != null) {
      showDialog(
        context: context,
        builder: (context) {
          return GestureDetector(
            behavior: HitTestBehavior.translucent,
            onTap: () {
              Navigator.of(context).pop();
            },
            child: Padding(
              padding: const EdgeInsets.all(50),
              child: Image.memory(
                bytes,
                fit: BoxFit.contain,
              ),
            ),
          );
        },
      );
    }
  }
}

class ScreenshotFlutterApiImplements extends ScreenshotFlutterApi {
  ScreenshotFlutterApiImplements(this.context);
  final BuildContext context;
  @override
  Future<void> onTakeScreenshot(Uint8List? data) async {
    if (kDebugMode) {
      print('onTakeScreenshot:${data?.length}');
    }
    data ??= await FfNativeScreenshot().takeScreenshot();
    _HomePageState.showScreenshotDialog(data, context);
  }
}
