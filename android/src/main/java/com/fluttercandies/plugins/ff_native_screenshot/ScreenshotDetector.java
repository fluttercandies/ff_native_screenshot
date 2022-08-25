package com.fluttercandies.plugins.ff_native_screenshot;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import java.util.LinkedHashMap;

public class ScreenshotDetector {
    private static final long SCREENSHOT_TIMEOUT = 10000L;

    private static final String[] KEYWORDS = {
            "screenshot", "screen_shot", "screen-shot", "screen shot",
            "screencapture", "screen_capture", "screen-capture", "screen capture",
            "screencap", "screen_cap", "screen-cap", "screen cap", "截屏"};

    private static final String[] MEDIA_PROJECTIONS = {
            MediaStore.Images.ImageColumns.DATA,
            MediaStore.Images.ImageColumns.DATE_TAKEN,
            MediaStore.Images.ImageColumns.DATE_ADDED,
            MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME};

    private final DataCache lastDatas = new DataCache(4);

    private ContentResolver contentResolver;

    ScreenshotDetector(Context context, Runnable onScreenShot) {
        this.context = context;
        this.onScreenShot = onScreenShot;
    }

    private Context context;
    private Runnable onScreenShot;
    private ContentObserver internalObserver;
    private ContentObserver externalObserver;
    private boolean isRegistered;

    void start() {
        if (!isRegistered) {
            try {
                contentResolver = context.getContentResolver();
                if (internalObserver == null) {
                    internalObserver = new ScreenshotDetector.MediaContentObserver(
                            MediaStore.Images.Media.INTERNAL_CONTENT_URI, null, onScreenShot);
                }

                if (externalObserver == null) {
                    externalObserver = new ScreenshotDetector.MediaContentObserver(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null, onScreenShot);
                }

                contentResolver.registerContentObserver(
                        MediaStore.Images.Media.INTERNAL_CONTENT_URI,
                        true,
                        internalObserver);

                contentResolver.registerContentObserver(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        true,
                        externalObserver);
                isRegistered = true;
            } catch (Exception e) {
                Log.e("startListening", e.getMessage());
            }
        }
    }


    void stop() {
        if (isRegistered) {
            try {
                if (internalObserver != null) {
                    contentResolver.unregisterContentObserver(internalObserver);
                }

                if (externalObserver != null) {
                    contentResolver.unregisterContentObserver(externalObserver);
                }
                contentResolver = null;
                isRegistered = false;
            } catch (Exception e) {
                Log.e("stopListening", e.getMessage());
            }
        }
    }

    private class MediaContentObserver extends ContentObserver {
        private final Uri uri;
        private Runnable onScreenShot;

        MediaContentObserver(Uri uri, Handler handler, Runnable onScreenShot) {
            super(handler);
            this.uri = uri;
            this.onScreenShot = onScreenShot;
        }

        @Override
        public void onChange(boolean selfChange) {
            new Thread(() -> {
                Cursor cursor = null;
                try {

                    boolean success = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        final Bundle queryArgs = new Bundle();
                        queryArgs.putInt(ContentResolver.QUERY_ARG_LIMIT, 1);
                        queryArgs.putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER, MediaStore.Images.ImageColumns.DATE_ADDED + " DESC");
                        cursor = contentResolver.query(
                                uri,
                                MEDIA_PROJECTIONS,
                                queryArgs,
                                null);
                    } else {
                        cursor = contentResolver.query(
                                uri,
                                MEDIA_PROJECTIONS,
                                null,
                                null,
                                MediaStore.Images.ImageColumns.DATE_ADDED + " desc limit 1");
                    }

                    if (cursor != null && cursor.moveToFirst()) {
                        @SuppressLint("Range") final String data = cursor.getString(
                                cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
                        @SuppressLint("Range") final long dateTaken = cursor.getLong(
                                cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN));
                        success = handleScreenshot(data, dateTaken);
                    }


                    if (!success) {
                        if (cursor != null && !cursor.isClosed()) {
                            cursor.close();
                        }
                        final String where = MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME + " like?";
                        final String[] whereArgs = new String[]{"%screen%shot%"};
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            final Bundle queryArgs = new Bundle();
                            queryArgs.putInt(ContentResolver.QUERY_ARG_SQL_LIMIT, 1);
                            queryArgs.putString(ContentResolver.QUERY_ARG_SQL_SELECTION, where);
                            queryArgs.putStringArray(ContentResolver.QUERY_ARG_SQL_SELECTION_ARGS, whereArgs);
                            queryArgs.putString(ContentResolver.QUERY_ARG_SQL_SORT_ORDER, MediaStore.Images.ImageColumns.DATE_ADDED + " DESC");
                            cursor = contentResolver.query(
                                    uri,
                                    MEDIA_PROJECTIONS,
                                    queryArgs,
                                    null);
                        } else {
                            cursor = contentResolver.query(
                                    uri,
                                    MEDIA_PROJECTIONS,
                                    where,
                                    whereArgs,
                                    MediaStore.Images.ImageColumns.DATE_ADDED + " desc limit 1");
                        }


                        if (cursor != null && cursor.moveToFirst()) {
                            @SuppressLint("Range") final String data = cursor.getString(
                                    cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA));
                            @SuppressLint("Range") final long dateTaken = cursor.getLong(
                                    cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN));
                            success = handleScreenshot(data, dateTaken);
                        }
                    }
                } catch (Throwable e) {
                    Log.e("onTakeScreenshotEvent", e.getMessage());
                } finally {
                    if (cursor != null && !cursor.isClosed()) {
                        try {
                            cursor.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }).start();

        }

        private boolean handleScreenshot(final String data, long dateTaken) {
            if (isScreenshot(data, dateTaken)) {
                lastDatas.put(data);
                onScreenShot.run();
                return true;
            }
            return false;
        }

        private boolean isScreenshot(String data, long dateTaken) {
            final Long time = System.currentTimeMillis() - dateTaken;
            if (time > SCREENSHOT_TIMEOUT) {
                return false;
            }

            if (!TextUtils.isEmpty(data)) {
                if (lastDatas.containsKey(data)) {
                    return false;
                }
                data = data.toLowerCase();

                for (String keyWork : KEYWORDS) {
                    if (data.contains(keyWork)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }


    private class DataCache extends LinkedHashMap<String, Boolean> {
        private int cacheSize;

        private DataCache(int cacheSize) {
            this.cacheSize = cacheSize;
        }

        private void put(String data) {
            put(data, true);
        }

        @Override
        protected boolean removeEldestEntry(Entry<String, Boolean> eldest) {
            return size() > this.cacheSize;
        }
    }

}



