package com.example.onlineschool.Models;

import java.util.List;

public class User {
    private String documentId;
    private String name;
    private String username;
    private String search;
    private Integer age;
    private String address;
    private String email;
    private List<Grade> grades;

    public User() {

    }

    public User(String name, String username, String search, Integer age, String address, String email, List<Grade> grades) {
        this.name = name;
        this.username = username;
        this.age = age;
        this.address = address;
        this.email = email;
        this.grades = grades;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getSearch() {
        return search;
    }

    public void setSearch(String search) {
        this.search = search;
    }

    public Integer getAge() {
        return age;
    }

    public String getAddress() {
        return address;
    }

    public String getEmail() {
        return email;
    }

    public List<Grade> getGrades() {
        return grades;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setGrades(List<Grade> grades) {
        this.grades = grades;
    }
}
