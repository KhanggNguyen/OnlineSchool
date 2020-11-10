package com.example.onlineschool.Models;

import com.google.firebase.firestore.DocumentReference;

import java.util.List;

public class Lesson {
    private String documentId;
    private String content;
    private DocumentReference courseRef;
    private int lesson;
    private String title;
    private List<String> videos;

    public Lesson() {
    }

    public Lesson(String content, DocumentReference courseRef, int lesson, String title) {
        this.lesson = lesson;
        this.content = content;
        this.courseRef = courseRef;
        this.title = title;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public int getLesson() {
        return lesson;
    }

    public void setLesson(int lesson) {
        this.lesson = lesson;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public DocumentReference getCourseRef() {
        return courseRef;
    }

    public void setCourseRef(DocumentReference courseRef) {
        this.courseRef = courseRef;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getVideos() {
        return videos;
    }

    public void setVideos(List<String> videos) {
        this.videos = videos;
    }
}
