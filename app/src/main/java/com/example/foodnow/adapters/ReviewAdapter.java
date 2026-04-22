package com.example.foodnow.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodnow.R;
import com.example.foodnow.models.Review;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ViewHolder> {

    private final Context context;
    private final List<Review> reviewList;

    public ReviewAdapter(Context context, List<Review> reviewList) {
        this.context = context;
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_review, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review review = reviewList.get(position);

        holder.tvName.setText(review.getReviewerName());
        holder.tvTime.setText(review.getTimeAgo());
        holder.tvComment.setText(review.getComment());
        holder.tvLikes.setText(review.getLikes() + " thích");

        // Hiển thị số sao theo rating (1-5)
        // Sao đầy = cam, sao trống = xám
        ImageView[] stars = {holder.star1, holder.star2, holder.star3, holder.star4, holder.star5};
        int rating = review.getRating();
        for (int i = 0; i < 5; i++) {
            stars[i].setColorFilter(context.getColor(
                    i < rating ? R.color.home_primary_orange : R.color.home_border));
        }
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTime, tvComment, tvLikes;
        ImageView star1, star2, star3, star4, star5;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName    = itemView.findViewById(R.id.tv_reviewer_name);
            tvTime    = itemView.findViewById(R.id.tv_review_time);
            tvComment = itemView.findViewById(R.id.tv_review_comment);
            tvLikes   = itemView.findViewById(R.id.tv_review_likes);
            star1 = itemView.findViewById(R.id.star1);
            star2 = itemView.findViewById(R.id.star2);
            star3 = itemView.findViewById(R.id.star3);
            star4 = itemView.findViewById(R.id.star4);
            star5 = itemView.findViewById(R.id.star5);
        }
    }
}
