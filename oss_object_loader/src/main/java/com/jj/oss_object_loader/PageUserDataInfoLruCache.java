package com.jj.oss_object_loader;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.collection.LruCache;

;

/**
 * Jay
 * 内存缓存(每页笔迹)
 */
public class PageUserDataInfoLruCache {

    private static final String TAG = "PageUserDataInfoLru";
    private static LruCache<String, String> lruCache;
    private static PageUserDataInfoLruCache instance;

    private PageUserDataInfoLruCache() {
        try {
            lruCache = new LruCache<String, String>(getMemoryCacheSize()) {
                protected int sizeOf(@NonNull String paramString, @NonNull String drawPathJson) {
                    return drawPathJson.length() / 1024;
                }
            };
        } catch (Exception e) {
            Log.e(TAG, "getInstance new LruCache 异常" + Log.getStackTraceString(e));
        }
    }

    public static PageUserDataInfoLruCache getInstance() {
        if (instance == null) {
            synchronized (PageUserDataInfoLruCache.class) {
                if (instance == null) instance = new PageUserDataInfoLruCache();
            }
        }
        return instance;
    }

    private static int getMemoryCacheSize() {
        int memorySize = (int) (Runtime.getRuntime().maxMemory() / 1024L / 32L);
        Log.d(TAG, "getMemoryCacheSize memorySize=" + memorySize);
        return memorySize;
    }

    public synchronized void put(String key, String drawPathJson) {
        if (drawPathJson == null) drawPathJson = "";
        lruCache.put(key, drawPathJson);
    }

    public synchronized String get(String key) {
        if (TextUtils.isEmpty(key)) return "";
        return lruCache.get(key);
    }

    public synchronized void remove(String key) {
        if (TextUtils.isEmpty(key)) return;
        lruCache.remove(key);
    }
}

/* Location:           E:\vpanel_source\classes7-dex2jar.jar
 * Qualified Name:     com.maxnerva.smartcity.fragments.bitmap_loader.BitmapLruCache
 * JD-Core Version:    0.6.0
 */