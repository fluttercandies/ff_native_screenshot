package com.fluttercandies.plugins.ff_native_screenshot;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.PixelCopy;
import android.view.View;
import android.view.Window;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;

import io.flutter.embedding.engine.plugins.FlutterPlugin;
import kotlin.jvm.internal.Intrinsics;

/**
 * FfNativeScreenshotPlugin
 */
public class FfNativeScreenshotPlugin implements FlutterPlugin, ScreenshotApi.ScreenshotHostApi {

    private ScreenshotApi.ScreenshotFlutterApi screenshotFlutterApi;
    private Context context;
    private ActivityLifecycleCallbacks callbacks = new ActivityLifecycleCallbacks();
    private Handler handler;
    //private FileObserver fileObserver;
    private ScreenshotDetector detector;
    //private String lastScreenshotFileName;

    @Override
    public void onAttachedToEngine(@NonNull FlutterPluginBinding flutterPluginBinding) {
        ScreenshotApi.ScreenshotHostApi.setup(flutterPluginBinding.getBinaryMessenger(), this);
        screenshotFlutterApi = new ScreenshotApi.ScreenshotFlutterApi(flutterPluginBinding.getBinaryMessenger());
        context = flutterPluginBinding.getApplicationContext();
        if (context instanceof Application) {
            Application application = (Application) context;
            application.registerActivityLifecycleCallbacks(callbacks);
        }
    }


    @Override
    public void onDetachedFromEngine(@NonNull FlutterPluginBinding binding) {
        ScreenshotApi.ScreenshotHostApi.setup(binding.getBinaryMessenger(), null);
        screenshotFlutterApi = null;
    }


    @Override
    public void takeScreenshot(ScreenshotApi.Result<byte[]> result) {

//    ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
//    ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        if (callbacks.currentActivity != null) {
            Activity activity = callbacks.currentActivity;
            Window window = activity.getWindow();
            View view = window.getDecorView();
            final Bitmap bitmap;
            try {
                if (Build.VERSION.SDK_INT >= 26) {
                    bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
                    int[] location = new int[2];
                    view.getLocationInWindow(location);
                    PixelCopy.request(window, new Rect(location[0], location[1], location[0] + view.getWidth(), location[1] + view.getHeight()), bitmap, (PixelCopy.OnPixelCopyFinishedListener) (new PixelCopy.OnPixelCopyFinishedListener() {
                        public final void onPixelCopyFinished(int it) {
                            if (it == 0) {
                                takeScreenshotResult(bitmap, result);
                            } else {
                                result.error(new Exception("fail to take screenshot"));
                            }
                        }
                    }), new Handler(Looper.getMainLooper()));
                } else {
                    bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.RGB_565);
                    Canvas canvas = new Canvas(bitmap);
                    view.draw(canvas);
                    canvas.setBitmap((Bitmap) null);
                    Intrinsics.checkNotNullExpressionValue(bitmap, "tBitmap");
                    takeScreenshotResult(bitmap, result);
                }

            } catch (Exception e) {
                Log.e("takeScreenshot", e.getMessage());
                result.error(e);
            } finally {

            }
        } else {
            result.success(null);
        }
    }

    @Override
    public void startListeningScreenshot() {
        handler = new Handler(Looper.getMainLooper());

        detector = new ScreenshotDetector(context, () -> {
            handler.post(() -> {
                onTakeScreenshot();
            });
        });
        detector.start();
//
//       if (Build.VERSION.SDK_INT >= 29) {
//           final List<File> files = new ArrayList<>();
//           final List<String> paths = new ArrayList<>();
//           for (Path path : Path.values()) {
//               files.add(new File(path.getPath()));
//               paths.add(path.getPath());
//           }
//           fileObserver = new FileObserver(files) {
//               @Override
//               public void onEvent(int event, final String filename) {
//                   if (event == FileObserver.CREATE) {
//                       handler.post(() -> {
//                           for (String fullPath : paths) {
//                               File file = new File(fullPath + filename);
//                               handleScreenshot(file);
//                           }
//                       });
//                   }
//               }
//           };
//           fileObserver.startWatching();
//       } else {
//           for (final Path path : Path.values()) {
//               fileObserver = new FileObserver(path.getPath()) {
//                   @Override
//                   public void onEvent(int event, final String filename) {
//
//                       File file = new File(path.getPath() + filename);
//                       if (event == FileObserver.CREATE) {
//                           handler.post(() -> {
//                               handleScreenshot(file);
//                           });
//                       }
//                   }
//               };
//               fileObserver.startWatching();
//           }
//       }
    }

//    private void handleScreenshot(File file) {
//        if (file.exists()) {
//            String path = file.getPath();
//            if (lastScreenshotFileName!=path && getMimeType(file.getPath()).contains("image")) {
//                lastScreenshotFileName = path;
//                onTakeScreenshot();
//            }
//        }
//    }

    @Override
    public void stopListeningScreenshot() {

//        if (fileObserver != null) fileObserver.stopWatching();
//        lastScreenshotFileName = null;
        if (detector != null) {
            detector.stop();
            detector = null;
        }
    }

    private void takeScreenshotResult(Bitmap bitmap, ScreenshotApi.Result<byte[]> result) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] imageInByte = stream.toByteArray();
        result.success(imageInByte);
    }

    private void onTakeScreenshot(String path) {
        try {
            File imageFile = new File(path);
            FileInputStream inputStream = new FileInputStream(imageFile);
            byte[] buffer = new byte[1024];
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            int len = 0;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            byte[] imageInByte = outputStream.toByteArray();
            if (screenshotFlutterApi != null) {
                screenshotFlutterApi.onTakeScreenshot(imageInByte, (v) -> {
                });
            }
        } catch (Exception e) {
            Log.e("onTakeScreenshot", e.getMessage());
        } finally {

        }
    }


    private void onTakeScreenshot() {
        takeScreenshot(new TakeScreenshotResult());
    }

    class TakeScreenshotResult implements ScreenshotApi.Result<byte[]> {
        @Override
        public void success(byte[] result) {
            if (screenshotFlutterApi != null) {
                screenshotFlutterApi.onTakeScreenshot(result, (v) -> {
                });
            }
        }

        @Override
        public void error(Throwable error) {
            Log.e("takeScreenshot", error.getMessage());
        }
    }

    class ActivityLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

        private Activity currentActivity;

        @Override
        public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(@NonNull Activity activity) {

        }

        @Override
        public void onActivityResumed(@NonNull Activity activity) {
            currentActivity = activity;
        }

        @Override
        public void onActivityPaused(@NonNull Activity activity) {

        }

        @Override
        public void onActivityStopped(@NonNull Activity activity) {
            if (currentActivity == activity) {
                currentActivity = null;
            }
        }

        @Override
        public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(@NonNull Activity activity) {

        }
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public enum Path {
        DCIM(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + File.separator + "Screenshots" + File.separator),
        PICTURES(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + File.separator + "Screenshots" + File.separator);

        final private String path;

        public String getPath() {
            return path;
        }

        Path(String path) {
            this.path = path;
        }
    }
}

