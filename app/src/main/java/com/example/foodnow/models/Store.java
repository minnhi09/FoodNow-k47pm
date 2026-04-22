package com.example.foodnow.models;

public class Store {
    private String id;
    private String name;
    private String description;
    private String address;
    private String phone;
    private String imageUrl;
    private float rating;
    private String deliveryTime;
    private long deliveryFee;
    private boolean isOpen;
    private String storeOwnerId;
    private String categoryId;
    public Store() {}
    public Store(String id, String name, String description, String address, String phone, String imageUrl, float rating, String deliveryTime, long deliveryFee, boolean isOpen, String storeOwnerId) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.address = address;
        this.phone = phone;
        this.imageUrl = imageUrl;
        this.rating = rating;
        this.deliveryTime = deliveryTime;
        this.deliveryFee = deliveryFee;
        this.isOpen = isOpen;
        this.storeOwnerId = storeOwnerId;
    }
    //Getter
    public String getId() {
        return id;
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public String getAddress() {
        return address;
    }
    public String getPhone() {
        return phone;
    }
    public String getImageUrl() {
        return imageUrl;
    }
    public float getRating() {
        return rating;
    }
    public String getDeliveryTime() {
        return deliveryTime;
    }
    public long getDeliveryFee() {
        return deliveryFee;
    }
    public boolean isOpen() {
        return isOpen;
    }
    public String getStoreOwnerId() {
        return storeOwnerId;
    }
    //Setter
    public void setId(String id) {
        this.id = id;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public void setAddress(String address) {
        this.address = address;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    public void setRating(float rating) {
        this.rating = rating;
    }
    public void setDeliveryTime(String deliveryTime) {
        this.deliveryTime = deliveryTime;
    }
    public void setDeliveryFee(long deliveryFee) {
        this.deliveryFee = deliveryFee;
    }
    public void setOpen(boolean open) {
        isOpen = open;
    }
    public void setIsOpen(boolean open) {
        isOpen = open;
    }
    public void setStoreOwnerId(String storeOwnerId) {
        this.storeOwnerId = storeOwnerId;
    }
    public String getCategoryId() {
        return categoryId;
    }
    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

}
