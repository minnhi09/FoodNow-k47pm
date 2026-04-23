package com.example.foodnow.models;

/** Khớp với collection Favorites trong Firestore. */
public class Favorite {
    private String id;        // document ID (set thủ công sau khi đọc)
    private String userId;
    private String type;      // "store" hoặc "food"
    private String itemId;    // storeId hoặc foodId
    private String name;
    private String imageUrl;

    // Firestore yêu cầu constructor rỗng
    public Favorite() {}

    public Favorite(String userId, String type, String itemId, String name, String imageUrl) {
        this.userId   = userId;
        this.type     = type;
        this.itemId   = itemId;
        this.name     = name;
        this.imageUrl = imageUrl;
    }

    public String getId()       { return id; }
    public String getUserId()   { return userId; }
    public String getType()     { return type; }
    public String getItemId()   { return itemId; }
    public String getName()     { return name; }
    public String getImageUrl() { return imageUrl; }

    public void setId(String id)             { this.id       = id; }
    public void setUserId(String userId)     { this.userId   = userId; }
    public void setType(String type)         { this.type     = type; }
    public void setItemId(String itemId)     { this.itemId   = itemId; }
    public void setName(String name)         { this.name     = name; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
