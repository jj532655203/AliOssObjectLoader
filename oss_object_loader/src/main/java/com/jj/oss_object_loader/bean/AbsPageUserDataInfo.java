package com.jj.oss_object_loader.bean;

import com.blankj.utilcode.util.ObjectUtils;
import com.jj.scribble_sdk_pen.data.TouchPoint;
import com.jj.scribble_sdk_pen.data.TouchPointList;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;


public abstract class AbsPageUserDataInfo implements Serializable, Cloneable {

    private static final long serialVersionUID = -3122640755970995885L;

    private long taskFinishId;
    private long pageIndex;
    private int bookContentPage;
    private String myOssUrl;
    private int[] createTimeYmd;

    private List<TouchPointList> studentDrawPathList;
    private List<TouchPointList> teacherDrawPathList;
    private int originalScribbleViewWidth;
    private int teacherOriginalScribbleViewWidth;

    public static void convertPathListByWidthRatio(List<TouchPointList> studentDrawPathList, int originalScribbleViewWidth, int newScribbleViewWidth) {
        if (ObjectUtils.isEmpty(studentDrawPathList)) return;
        if (originalScribbleViewWidth <= 0 || newScribbleViewWidth <= 0) return;
        float ratio = (float) newScribbleViewWidth / originalScribbleViewWidth;
        for (TouchPointList path : studentDrawPathList) {
            for (TouchPoint point : path.getPoints()) {
                point.x *= ratio;
                point.x *= ratio;
            }
        }
    }

    public AbsPageUserDataInfo(long taskFinishId, long page, int bookContentPage, String myOssUrl, int[] createTimeYmd) {
        this.taskFinishId = taskFinishId;
        this.pageIndex = page;
        this.bookContentPage = bookContentPage;
        this.myOssUrl = myOssUrl;
        this.createTimeYmd = createTimeYmd;
    }

    @Override
    public Object clone() {
        AbsPageUserDataInfo pageUserDataInfo = null;
        try {
            //淺拷貝
            pageUserDataInfo = (AbsPageUserDataInfo) super.clone();
        } catch (CloneNotSupportedException e) {
//            return new AbsPageUserDataInfo();
            throw new RuntimeException("拷贝用户笔记数据失败");
        }
        //支持深度克隆
//        pageUserDataInfo.address=address.clone();
        return pageUserDataInfo;
    }

    @Override
    public String toString() {
        return "AbsPageUserDataInfo{" +
                "taskFinishId=" + taskFinishId +
                ", pageIndex=" + pageIndex +
                ", bookContentPage=" + bookContentPage +
                ", myOssUrl='" + myOssUrl + '\'' +
                ", createTimeYmd=" + Arrays.toString(createTimeYmd) +
                ", studentDrawPathList=" + studentDrawPathList +
                ", teacherDrawPathList=" + teacherDrawPathList +
                ", originalScribbleViewWidth=" + originalScribbleViewWidth +
                ", teacherOriginalScribbleViewWidth=" + teacherOriginalScribbleViewWidth +
                '}';
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public long getTaskFinishId() {
        return taskFinishId;
    }

    public void setTaskFinishId(long taskFinishId) {
        this.taskFinishId = taskFinishId;
    }

    public long getPageIndex() {
        return pageIndex;
    }

    public void setPageIndex(long pageIndex) {
        this.pageIndex = pageIndex;
    }

    public int getBookContentPage() {
        return bookContentPage;
    }

    public void setBookContentPage(int bookContentPage) {
        this.bookContentPage = bookContentPage;
    }

    public String getMyOssUrl() {
        return myOssUrl;
    }

    public void setMyOssUrl(String myOssUrl) {
        this.myOssUrl = myOssUrl;
    }

    public int[] getCreateTimeYmd() {
        return createTimeYmd;
    }

    public void setCreateTimeYmd(int[] createTimeYmd) {
        this.createTimeYmd = createTimeYmd;
    }

    public List<TouchPointList> getStudentDrawPathList() {
        return studentDrawPathList;
    }

    public void setStudentDrawPathList(List<TouchPointList> studentDrawPathList) {
        this.studentDrawPathList = studentDrawPathList;
    }

    public List<TouchPointList> getTeacherDrawPathList() {
        return teacherDrawPathList;
    }

    public void setTeacherDrawPathList(List<TouchPointList> teacherDrawPathList) {
        this.teacherDrawPathList = teacherDrawPathList;
    }

    public int getOriginalScribbleViewWidth() {
        return originalScribbleViewWidth;
    }

    public void setOriginalScribbleViewWidth(int originalScribbleViewWidth) {
        this.originalScribbleViewWidth = originalScribbleViewWidth;
    }

    public int getTeacherOriginalScribbleViewWidth() {
        return teacherOriginalScribbleViewWidth;
    }

    public void setTeacherOriginalScribbleViewWidth(int teacherOriginalScribbleViewWidth) {
        this.teacherOriginalScribbleViewWidth = teacherOriginalScribbleViewWidth;
    }
}
