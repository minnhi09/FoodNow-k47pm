package com.example.foodnow.models;

/** Model cho đánh giá món ăn — dùng mock data, chưa lưu Firestore */
public class Review {

    private String reviewerName;
    private String timeAgo;
    private int rating;   // 1 – 5
    private String comment;
    private int likes;

    public Review() {}

    public Review(String reviewerName, String timeAgo, int rating, String comment, int likes) {
        this.reviewerName = reviewerName;
        this.timeAgo = timeAgo;
        this.rating = rating;
        this.comment = comment;
        this.likes = likes;
    }

    // Getters
    public String getReviewerName() { return reviewerName; }
    public String getTimeAgo()      { return timeAgo; }
    public int getRating()          { return rating; }
    public String getComment()      { return comment; }
    public int getLikes()           { return likes; }

    // Setters
    public void setReviewerName(String reviewerName) { this.reviewerName = reviewerName; }
    public void setTimeAgo(String timeAgo)           { this.timeAgo = timeAgo; }
    public void setRating(int rating)                { this.rating = rating; }
    public void setComment(String comment)           { this.comment = comment; }
    public void setLikes(int likes)                  { this.likes = likes; }
}
