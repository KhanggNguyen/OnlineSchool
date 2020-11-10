package com.example.onlineschool.Models;

import com.google.firebase.firestore.DocumentReference;

import java.util.List;

public class Exercice {
    private String documentId;
    private String title;
    private DocumentReference sheetRef;
    private List<String> question;
    private List<String> response;
    private List<String> enonce;
    private List<String> imageName;

    public Exercice() {
    }

    public Exercice(String title, DocumentReference sheetRef, List<String> question, List<String> response, List<String> enonce, List<String> imageName) {
        this.title = title;
        this.sheetRef = sheetRef;
        this.question = question;
        this.response = response;
        this.enonce = enonce;
        this.imageName = imageName;
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

    public DocumentReference getSheetRef() {
        return sheetRef;
    }

    public void setSheetRef(DocumentReference sheetRef) {
        this.sheetRef = sheetRef;
    }

    public List<String> getQuestion() {
        return question;
    }

    public void setQuestion(List<String> question) {
        this.question = question;
    }

    public List<String> getResponse() {
        return response;
    }

    public void setResponse(List<String> response) {
        this.response = response;
    }

    public List<String> getEnonce() {
        return enonce;
    }

    public void setEnonce(List<String> enonce) {
        this.enonce = enonce;
    }

    public List<String> getImageName() {
        return imageName;
    }

    public void setImageName(List<String> imageName) {
        this.imageName = imageName;
    }
}
