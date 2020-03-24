package com.jj.oss_object_loader;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.sdk.android.oss.common.utils.IOUtils;
import com.blankj.utilcode.util.ObjectUtils;
import com.blankj.utilcode.util.ScreenUtils;
import com.blankj.utilcode.util.Utils;
import com.fronttcapital.imageloader.AliOssResponse;
import com.fronttcapital.imageloader.AliOssUtils;
import com.fronttcapital.imageloader.ImgLoaderExecutor;
import com.google.gson.Gson;
import com.jj.fst_disk_lru.disk_lru_cache.DiskLruCacheUtils;
import com.jj.fst_disk_lru.memory_lru_cache.MemoryLruCacheUtils;
import com.jj.oss_object_loader.bean.AbsPageUserDataInfo;
import com.jj.oss_object_loader.view.DrawScribblesView;
import com.jj.scribble_sdk_pen.data.TouchPointList;

import java.lang.ref.WeakReference;
import java.util.List;

public class AliOssScribblesLoader {
    private static final String TAG = "AliOssScribblesLoader";

    public static void loadScribbles(final String ossPath, final DrawScribblesView drawScribblesView, final int mPosition) {
        Log.d(TAG, "loadScribbles ossPath=" + ossPath);

        if (drawScribblesView == null || TextUtils.isEmpty(ossPath)) {
            Log.e(TAG, "loadShowPathView 传参异常 return");
            return;
        }

        drawScribblesView.clearData();

        //内存获取
        AbsPageUserDataInfo _pageUserDataInfo = (AbsPageUserDataInfo) MemoryLruCacheUtils.getInstance().get(ossPath);
        if (!ObjectUtils.isEmpty(_pageUserDataInfo)) {
            List<TouchPointList> touchPointListList = _pageUserDataInfo.getStudentDrawPathList();
            List<TouchPointList> teacherDrawPathList = _pageUserDataInfo.getTeacherDrawPathList();
            AbsPageUserDataInfo.convertPathListByWidthRatio(touchPointListList, _pageUserDataInfo.getOriginalScribbleViewWidth(), ScreenUtils.getScreenWidth());
            AbsPageUserDataInfo.convertPathListByWidthRatio(teacherDrawPathList, _pageUserDataInfo.getTeacherOriginalScribbleViewWidth(), ScreenUtils.getScreenWidth());
            drawScribblesView.setPathList(touchPointListList, teacherDrawPathList, mPosition);
            Log.d(TAG, "loadShowPathView 内存获取到了");
            return;
        }


        drawScribblesView.setTag(ossPath);
        final WeakReference<DrawScribblesView> showPathViewRef = new WeakReference<>(drawScribblesView);

        ImgLoaderExecutor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "loadShowPathView 开启一个加载页用户数据任务 ossPath=" + ossPath);

                //致读者:不建议用内存缓存bitmap,建议使用前后条目预加载来加快图片显示

                //从本地获取
                AbsPageUserDataInfo pageUserDataInfo = DiskLruCacheUtils.getInstance().getObject(ossPath, AbsPageUserDataInfo.class);
                if (ObjectUtils.isEmpty(pageUserDataInfo)) {
                    //从网络获取
                    try {
                        AliOssResponse ossResponse = AliOssUtils.getObjectRequest(Utils.getApp(), ossPath);
                        if (ossResponse == null || ossResponse.getStatusCode() != 200) {
                            Log.w(TAG, "loadShowPathView 从网络下载失败 ossResponse异常 或许该页未曾产生页用户数据");
                            return;
                        }
                        String jsonStr = IOUtils.readStreamAsString(ossResponse.getInputStream(), "UTF-8");
                        pageUserDataInfo = new Gson().fromJson(jsonStr, AbsPageUserDataInfo.class);
                        if (ObjectUtils.isEmpty(pageUserDataInfo)) {
                            Log.e(TAG, "loadShowPathView 从网络下载下来为空文件");
                            return;
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "loadShowPathView 下载失败 e=" + Log.getStackTraceString(e));
                        return;
                    }

                    DiskLruCacheUtils.getInstance().put(ossPath, pageUserDataInfo, AbsPageUserDataInfo.class);

                }

                MemoryLruCacheUtils.getInstance().put(ossPath, pageUserDataInfo);

                DrawScribblesView pathView = showPathViewRef.get();
                if (ObjectUtils.isNotEmpty(pathView)) {
                    Object tag = drawScribblesView.getTag();
                    if (ObjectUtils.isNotEmpty(tag)) {
                        if (tag instanceof String && TextUtils.equals((CharSequence) tag, ossPath)) {
                            List<TouchPointList> touchPointListList = pageUserDataInfo.getStudentDrawPathList();
                            List<TouchPointList> teacherDrawPathList = pageUserDataInfo.getTeacherDrawPathList();
                            AbsPageUserDataInfo.convertPathListByWidthRatio(touchPointListList, pageUserDataInfo.getOriginalScribbleViewWidth(), ScreenUtils.getScreenWidth());
                            AbsPageUserDataInfo.convertPathListByWidthRatio(teacherDrawPathList, pageUserDataInfo.getOriginalScribbleViewWidth(), ScreenUtils.getScreenWidth());
                            pathView.setPathList(touchPointListList, teacherDrawPathList, mPosition);
                        }
                    }
                }


                Log.d(TAG, "loadShowPathView 下载成功 path=" + ossPath);
            }
        });


    }
}
