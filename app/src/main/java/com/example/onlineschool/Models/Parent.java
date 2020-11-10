package com.example.onlineschool.Models;

import com.google.firebase.firestore.DocumentReference;

import java.lang.annotation.Documented;
import java.util.List;

public class Parent {
    private String documentId;
    private List<DocumentReference> studentsRef;
    private int totalStudent;

    public Parent() {
    }

    public Parent(List<DocumentReference> studentsRef, int totalStudent) {
        this.studentsRef = studentsRef;
        this.totalStudent = totalStudent;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public List<DocumentReference> getStudentsRef() {
        return studentsRef;
    }

    public void setStudentsRef(List<DocumentReference> studentsRef) {
        this.studentsRef = studentsRef;
    }

    public int getTotalStudent() {
        return totalStudent;
    }

    public void setTotalStudent(int totalStudent) {
        this.totalStudent = totalStudent;
    }
}
