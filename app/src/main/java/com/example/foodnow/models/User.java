package com.example.foodnow.models;

import com.google.firebase.Timestamp;

public class User {

    private String id;
    private String email;
    private String name;
    private String phone;
    private String address;
    private String imageUrl;
    private Timestamp createdAt;

    // Constructor rỗng — bắt buộc cho Firestore
    public User() {}

    public User(String id, String email, String name, String phone,
                String address, String imageUrl) {
        this.id       = id;
        this.email    = email;
        this.name     = name;
        this.phone    = phone;
        this.address  = address;
        this.imageUrl = imageUrl;
    }

    // Getter
    public String    getId()        { return id; }
    public String    getEmail()     { return email; }
    public String    getName()      { return name; }
    public String    getPhone()     { return phone; }
    public String    getAddress()   { return address; }
    public String    getImageUrl()  { return imageUrl; }
    public Timestamp getCreatedAt() { return createdAt; }

    // Setter
    public void setId(String id)              { this.id = id; }
    public void setEmail(String email)        { this.email = email; }
    public void setName(String name)          { this.name = name; }
    public void setPhone(String phone)        { this.phone = phone; }
    public void setAddress(String address)    { this.address = address; }
    public void setImageUrl(String imageUrl)  { this.imageUrl = imageUrl; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}
