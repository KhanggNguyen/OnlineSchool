package com.example.onlineschool.Models;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

import java.util.List;

public class Quiz {
    private String documentId;
    private DocumentReference sheetRef;
    private List<QuestionQuiz> Questions;
    private Timestamp timeQuizDone;
    public Quiz() {
    }

    public Quiz(DocumentReference sheetRef, List<QuestionQuiz> Questions) {
        this.sheetRef = sheetRef;
        this.Questions = Questions;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public DocumentReference getSheetRef() {
        return sheetRef;
    }

    public void setSheetRef(DocumentReference sheetRef) {
        this.sheetRef = sheetRef;
    }

    public List<QuestionQuiz> getQuestions() {
        return Questions;
    }

    public void setQuestions(List<QuestionQuiz> Questions) {
        this.Questions = Questions;
    }

    public Timestamp getTimeQuizDone() {
        return timeQuizDone;
    }

    public void setTimeQuizDone(Timestamp timeQuizDone) {
        this.timeQuizDone = timeQuizDone;
    }
}

