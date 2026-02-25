package com.example.foodnow.models;

/**
 * Món trong giỏ hàng — chỉ lưu local (không đẩy lên Firestore)
 */
public class CartItem {

    private String foodId;
    private String title;
    private long price;
    private int quantity;
    private String imageUrl;
    private String storeId;
    private String storeName;

    public CartItem() {}

    public CartItem(String foodId, String title, long price,
                    int quantity, String imageUrl,
                    String storeId, String storeName) {
        this.foodId    = foodId;
        this.title     = title;
        this.price     = price;
        this.quantity  = quantity;
        this.imageUrl  = imageUrl;
        this.storeId   = storeId;
        this.storeName = storeName;
    }

    // Getter
    public String getFoodId()    { return foodId; }
    public String getTitle()     { return title; }
    public long   getPrice()     { return price; }
    public int    getQuantity()  { return quantity; }
    public String getImageUrl()  { return imageUrl; }
    public String getStoreId()   { return storeId; }
    public String getStoreName() { return storeName; }

    // Setter
    public void setFoodId(String foodId)       { this.foodId = foodId; }
    public void setTitle(String title)         { this.title = title; }
    public void setPrice(long price)           { this.price = price; }
    public void setQuantity(int quantity)      { this.quantity = quantity; }
    public void setImageUrl(String imageUrl)   { this.imageUrl = imageUrl; }
    public void setStoreId(String storeId)     { this.storeId = storeId; }
    public void setStoreName(String storeName) { this.storeName = storeName; }

    /** Tổng tiền = đơn giá × số lượng */
    public long getTotalPrice() {
        return price * quantity;
    }
}
