package com.example.onlineschool.Models;

import java.lang.reflect.Array;
import java.util.List;

public class Subject {
    private String documentId;
    private String title;
    private String description;
    private List<Grade> gradesRef;

    public Subject() {
    }

    public Subject(String title, String description, List<Grade> gradesRef) {
        this.title = title;
        this.description = description;
        this.gradesRef = gradesRef;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Grade> getGradesRef() {
        return gradesRef;
    }

    public void setGradesRef(List<Grade> gradesRef) {
        this.gradesRef = gradesRef;
    }
}
