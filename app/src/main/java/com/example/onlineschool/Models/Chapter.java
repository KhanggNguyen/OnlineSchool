package com.example.onlineschool.Models;

import com.google.firebase.firestore.DocumentReference;

public class Chapter {
    private String documentId;
    private int chapter;
    private DocumentReference gradeRef;
    private DocumentReference subjectRef;
    private String title;

    public Chapter() {
    }

    public Chapter(int chapter, DocumentReference gradeRef, DocumentReference subjectRef, String title) {
        this.chapter = chapter;
        this.gradeRef = gradeRef;
        this.subjectRef = subjectRef;
        this.title = title;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public int getChapter() {
        return chapter;
    }

    public void setChapter(int chapter) {
        this.chapter = chapter;
    }

    public DocumentReference getGradeRef() {
        return gradeRef;
    }

    public void setGradeRef(DocumentReference gradeRef) {
        this.gradeRef = gradeRef;
    }

    public DocumentReference getSubjectRef() {
        return subjectRef;
    }

    public void setSubjectRef(DocumentReference subjectRef) {
        this.subjectRef = subjectRef;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
