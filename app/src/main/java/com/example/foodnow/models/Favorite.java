package com.example.foodnow.models;

public class Favorite {

    private String id;
    private String userId;
    private String type;      // "store" hoặc "food"
    private String itemId;    // id của quán hoặc món
    private String name;
    private String imageUrl;

    // Constructor rỗng — bắt buộc cho Firestore
    public Favorite() {}

    public Favorite(String id, String userId, String type,
                    String itemId, String name, String imageUrl) {
        this.id       = id;
        this.userId   = userId;
        this.type     = type;
        this.itemId   = itemId;
        this.name     = name;
        this.imageUrl = imageUrl;
    }

    // Getter
    public String getId()       { return id; }
    public String getUserId()   { return userId; }
    public String getType()     { return type; }
    public String getItemId()   { return itemId; }
    public String getName()     { return name; }
    public String getImageUrl() { return imageUrl; }

    // Setter
    public void setId(String id)             { this.id = id; }
    public void setUserId(String userId)     { this.userId = userId; }
    public void setType(String type)         { this.type = type; }
    public void setItemId(String itemId)     { this.itemId = itemId; }
    public void setName(String name)         { this.name = name; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
