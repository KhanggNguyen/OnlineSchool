package com.example.onlineschool.Models;

public class Course {
    private int imageResource;
    private String courseName;
    private String courseDescription;

    public Course() {
    }

    public Course(int imageResource, String courseName, String courseDescription) {
        this.imageResource = imageResource;
        this.courseName = courseName;
        this.courseDescription = courseDescription;
    }

    public int getImageResource() {
        return imageResource;
    }

    public void setImageResource(int imageResource) {
        this.imageResource = imageResource;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseDescription() {
        return courseDescription;
    }

    public void setCourseDescription(String courseDescription) {
        this.courseDescription = courseDescription;
    }
}

