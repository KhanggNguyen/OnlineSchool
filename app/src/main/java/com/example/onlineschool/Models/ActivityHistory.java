package com.example.onlineschool.Models;

import com.google.firebase.Timestamp;

public class ActivityHistory {
    private String documentId;
    private String lessonId;
    private String studentId;
    private boolean isLessonSheet = false;
    private boolean isQuiz = false;
    private boolean isExercice = false;
    private boolean isVideo = false;
    private Timestamp activityTime;

    public ActivityHistory() {
    }

    public ActivityHistory(String lessonId, String studentId, boolean isLessonSheet, boolean isQuiz, boolean isExercice, boolean isVideo, Timestamp activityTime) {
        this.lessonId = lessonId;
        this.studentId = studentId;
        this.isLessonSheet = isLessonSheet;
        this.isQuiz = isQuiz;
        this.isExercice = isExercice;
        this.isVideo = isVideo;
        this.activityTime = activityTime;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getLessonId() {
        return lessonId;
    }

    public void setLessonId(String lessonId) {
        this.lessonId = lessonId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public boolean isLessonSheet() {
        return isLessonSheet;
    }

    public void setLessonSheet(boolean lessonSheet) {
        isLessonSheet = lessonSheet;
    }

    public boolean isQuiz() {
        return isQuiz;
    }

    public void setQuiz(boolean quiz) {
        isQuiz = quiz;
    }

    public boolean isExercice() {
        return isExercice;
    }

    public void setExercice(boolean exercice) {
        isExercice = exercice;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public void setVideo(boolean video) {
        isVideo = video;
    }

    public Timestamp getActivityTime() {
        return activityTime;
    }

    public void setActivityTime(Timestamp activityTime) {
        this.activityTime = activityTime;
    }
}
