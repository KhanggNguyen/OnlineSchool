package com.example.onlineschool.Models;

public class Grade {
    private int documentId;
    private String grade;

    public Grade() {
    }

    public Grade(String grade) {
        this.grade = grade;
    }

    public int getId() {
        return documentId;
    }

    public String getGrade() {
        return grade;
    }

    public void setId(int id) {
        this.documentId = id;
    }

    public void setGrade(String grade) {
        this.grade = grade;
    }
}
