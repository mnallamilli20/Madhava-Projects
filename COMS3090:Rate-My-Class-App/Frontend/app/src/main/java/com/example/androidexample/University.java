package com.example.androidexample;

public class University {
    public final long university_id;
    public final String name;
    public final String location;
    public final String website;
    public final String description;
    public final String logoUrl;
    public final int editor; //user_id
    public final String createdOn; //when created

    public University(long university_id, String name, String location, String website, String description, String logoUrl, int editor, String createdOn) {
        this.university_id = university_id;
        this.name = name;
        this.location = location;
        this.website = website;
        this.description = description;
        this.logoUrl = logoUrl;
        this.editor = editor;
        this.createdOn = createdOn;
    }
    //THIS BELOW SHOWS WHAT IS IN DROPDOWN MENU
    @Override
    public String toString() {
        return name;
        //return name + " - " + location;
    }
}
