package com.example.foodnow.models;

public class Category {

    private String id;
    private String name;
    private String imageUrl;

    // Constructor rỗng — bắt buộc cho Firestore
    public Category() {}

    public Category(String id, String name, String imageUrl) {
        this.id       = id;
        this.name     = name;
        this.imageUrl = imageUrl;
    }

    // Getter
    public String getId()       { return id; }
    public String getName()     { return name; }
    public String getImageUrl() { return imageUrl; }

    // Setter
    public void setId(String id)             { this.id = id; }
    public void setName(String name)         { this.name = name; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
