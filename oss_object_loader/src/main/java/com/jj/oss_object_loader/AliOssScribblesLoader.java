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
import com.fronttcapital.imageloader.utils.disk_lru_cache.DiskLruCacheUtils;
import com.google.gson.Gson;
import com.jj.oss_object_loader.bean.PageUserDataInfo;
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
        final Gson gson = new Gson();

        //内存获取
        String pageUserInfoJson = PageUserDataInfoLruCache.getInstance().get(ossPath);
        if (!TextUtils.isEmpty(pageUserInfoJson)) {
            PageUserDataInfo mPageUserDataInfo = gson.fromJson(pageUserInfoJson, PageUserDataInfo.class);
            if (!ObjectUtils.isEmpty(mPageUserDataInfo)) {
                List<TouchPointList> touchPointListList = mPageUserDataInfo.getStudentDrawPathList();
                List<TouchPointList> teacherDrawPathList = mPageUserDataInfo.getTeacherDrawPathList();
                PageUserDataInfo.convertPathListByWidthRatio(touchPointListList, mPageUserDataInfo.getOriginalScribbleViewWidth(), ScreenUtils.getScreenWidth());
                PageUserDataInfo.convertPathListByWidthRatio(teacherDrawPathList, mPageUserDataInfo.getTeacherOriginalScribbleViewWidth(), ScreenUtils.getScreenWidth());
                drawScribblesView.setPathList(touchPointListList, teacherDrawPathList, mPosition);
                Log.d(TAG, "loadShowPathView 内存获取到了");
                return;
            }
        }


        drawScribblesView.setTag(ossPath);
        final WeakReference<DrawScribblesView> showPathViewRef = new WeakReference<>(drawScribblesView);

        ImgLoaderExecutor.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "loadShowPathView 开启一个加载页用户数据任务 ossPath=" + ossPath);

                //致读者:不建议用内存缓存bitmap,建议使用前后条目预加载来加快图片显示

                //从本地获取
                String pageUserInfoJson = DiskLruCacheUtils.getInstance().getDrawPathJson(ossPath);
                if (TextUtils.isEmpty(pageUserInfoJson)) {
                    //从网络获取
                    try {
                        AliOssResponse ossResponse = AliOssUtils.getObjectRequest(Utils.getApp(), ossPath);
                        if (ossResponse == null || ossResponse.getStatusCode() != 200) {
                            Log.w(TAG, "loadShowPathView 从网络下载失败 ossResponse异常 或许该页未曾产生页用户数据");
                            return;
                        }
                        pageUserInfoJson = IOUtils.readStreamAsString(ossResponse.getInputStream(), "UTF-8");
                        if (TextUtils.isEmpty(pageUserInfoJson)) {
                            Log.e(TAG, "loadShowPathView 从网络下载下来为空文件");
                            return;
                        }

                    } catch (Exception e) {
                        Log.e(TAG, "loadShowPathView 下载失败 e=" + Log.getStackTraceString(e));
                        return;
                    }


                    //本地缓存
                    DiskLruCacheUtils.getInstance().put(ossPath, pageUserInfoJson);

                }

                PageUserDataInfo pageUserDataInfo = gson.fromJson(pageUserInfoJson, PageUserDataInfo.class);
                if (ObjectUtils.isEmpty(pageUserDataInfo)) {
                    Log.e(TAG, "loadShowPathView 从网络下载下来解析后为空文件");
                    return;
                }
                DrawScribblesView pathView = showPathViewRef.get();
                if (ObjectUtils.isNotEmpty(pathView)) {
                    Object tag = drawScribblesView.getTag();
                    if (ObjectUtils.isNotEmpty(tag)) {
                        if (tag instanceof String && TextUtils.equals((CharSequence) tag, ossPath)) {
                            List<TouchPointList> touchPointListList = pageUserDataInfo.getStudentDrawPathList();
                            List<TouchPointList> teacherDrawPathList = pageUserDataInfo.getTeacherDrawPathList();
                            PageUserDataInfo.convertPathListByWidthRatio(touchPointListList, pageUserDataInfo.getOriginalScribbleViewWidth(), ScreenUtils.getScreenWidth());
                            PageUserDataInfo.convertPathListByWidthRatio(teacherDrawPathList, pageUserDataInfo.getOriginalScribbleViewWidth(), ScreenUtils.getScreenWidth());
                            pathView.setPathList(touchPointListList, teacherDrawPathList, mPosition);
                        }
                    }
                }


                Log.d(TAG, "loadShowPathView 下载成功 path=" + ossPath);
            }
        });


    }
}
