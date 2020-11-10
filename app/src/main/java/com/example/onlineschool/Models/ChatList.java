package com.example.onlineschool.Models;

public class ChatList {
    public String documentId;
    public String idReceiver;
    public String idSender;

    public ChatList(String idReceiver, String idSender) {
        this.idReceiver = idReceiver;
        this.idSender = idSender;
    }

    public ChatList(){

    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getIdReceiver() {
        return idReceiver;
    }

    public void setIdReceiver(String idReceiver) {
        this.idReceiver = idReceiver;
    }

    public String getIdSender() {
        return idSender;
    }

    public void setIdSender(String idSender) {
        this.idSender = idSender;
    }
}
