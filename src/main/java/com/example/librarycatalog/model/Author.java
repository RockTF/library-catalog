package com.example.librarycatalog.model;

import java.util.UUID;

public class Author {
    private String id;
    private String name;
    private String biography;

    public Author() {
        this.id = UUID.randomUUID().toString();
    }

    // Getters и Setters

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getBiography() {
        return biography;
    }
    public void setBiography(String biography) {
        this.biography = biography;
    }
}
