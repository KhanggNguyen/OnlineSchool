package com.example.onlineschool.Models;

public class QuestionResponse {
    private String question;
    private String response;
    private String enonce;
    private String image;

    public QuestionResponse() {
    }

    public QuestionResponse(String question, String response, String enonce, String image) {
        this.question = question;
        this.response = response;
        this.enonce = enonce;
        this.image = image;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getEnonce() {
        return enonce;
    }

    public void setEnonce(String enonce) {
        this.enonce = enonce;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
