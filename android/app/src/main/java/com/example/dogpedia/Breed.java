package com.example.dogpedia;

public class Breed {
    private String name;
    private String imageUrl;
    private String referenceImageId;

    public Breed(String name, String imageUrl, String referenceImageId) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.referenceImageId = referenceImageId;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getReferenceImageId() {
        return referenceImageId;
    }
}
