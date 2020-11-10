package com.example.onlineschool.Models;

import com.google.firebase.Timestamp;
import java.util.Date;

public class Chat {
    private String documentId;
    private String sender;
    private String receiver;
    private String message;
    private Timestamp timeSent;
    private boolean isSeen;
    public Chat() {
    }

    public Chat(String sender, String receiver, String message, boolean isSeen, Timestamp timeSent) {
        this.sender = sender;
        this.receiver = receiver;
        this.message = message;
        this.isSeen = isSeen;
        this.timeSent = timeSent;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getMessage() {
        return message;
    }


    public Timestamp getTimeSent() {
        return timeSent;
    }

    public void setTimeSent(Timestamp timeSent) {
        this.timeSent = timeSent;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isIsSeen() {
        return this.isSeen;
    }

    public void setIsSeen(boolean isSeen) {
        this.isSeen = isSeen;
    }
}
