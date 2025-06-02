package com.example.dogpedia;

public class VetEvent {
    public String title;
    public String description;
    public String date;
    public String type;

    public int id;

    public VetEvent(String title, String description, String date, String type, int id) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.type = type;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
}
