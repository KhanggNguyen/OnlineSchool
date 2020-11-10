package com.example.onlineschool.Models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.List;

public class Student {
    private String documentId;
    private DocumentReference parentReference;
    private List<Timestamp> studentConnection = new ArrayList<Timestamp>();
    private List<Quiz> quizDone = new ArrayList<Quiz>();
    private Subscription subscription;

    public Student() {
    }

    public Student(DocumentReference parentReference, List<Timestamp> studentConnection, List<Quiz> quizDone, Subscription subscription) {
        this.parentReference = parentReference;
        this.studentConnection = studentConnection;
        this.quizDone = quizDone;
        this.subscription = subscription;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public DocumentReference getParentReference() {
        return parentReference;
    }

    public void setParentReference(DocumentReference parentReference) {
        this.parentReference = parentReference;
    }

    public List<Quiz> getQuizDone() {
        return quizDone;
    }

    public void setQuizDone(List<Quiz> quizDone) {
        this.quizDone = quizDone;
    }

    public List<Timestamp> getStudentConnection() {
        return studentConnection;
    }

    public void setStudentConnection(List<Timestamp> studentConnection) {
        this.studentConnection = studentConnection;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public void setSubscription(Subscription subscription) {
        this.subscription = subscription;
    }
}
