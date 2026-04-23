package com.example.foodnow.models;

public class Favorite {
    private String id;
    private String userId;
    private String type;
    private String itemId;
    private String name;
    private String imageUrl;

    public Favorite() {
    }

    public Favorite(String id, String userId, String type, String itemId, String name, String imageUrl) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.itemId = itemId;
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getItemId() { return itemId; }
    public void setItemId(String itemId) { this.itemId = itemId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
