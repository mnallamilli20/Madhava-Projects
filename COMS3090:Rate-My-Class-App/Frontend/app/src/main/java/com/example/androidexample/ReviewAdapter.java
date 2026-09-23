package com.example.androidexample;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    public interface OnReactionClickListener {
        void onLikeClicked(int position);
        void onDislikeClicked(int position);
        void onFlagClicked(int position);
        void onDeleteClicked(int position);
    }

    private final List<Review> reviews;
    private final long currentUserId;
    private final OnReactionClickListener listener;

    public ReviewAdapter(List<Review> reviews, long currentUserId, OnReactionClickListener listener) {
        this.reviews = reviews;
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvItemUsername, tvItemCreatedOn, tvItemOverall,
                tvItemDifficulty, tvItemWorkload,
                tvItemRecommendation, tvItemGrade,
                tvItemComment, tvItemLikes, tvItemDislikes;

        Button btnLike, btnDislike, btnFlagReview, btnDeleteReview;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);

            tvItemUsername = itemView.findViewById(R.id.tvItemUsername);
            tvItemCreatedOn = itemView.findViewById(R.id.tvItemCreatedOn);
            tvItemOverall = itemView.findViewById(R.id.tvItemOverall);
            tvItemDifficulty = itemView.findViewById(R.id.tvItemDifficulty);
            tvItemWorkload = itemView.findViewById(R.id.tvItemWorkload);
            tvItemRecommendation = itemView.findViewById(R.id.tvItemRecommendation);
            tvItemGrade = itemView.findViewById(R.id.tvItemGrade);
            tvItemComment = itemView.findViewById(R.id.tvItemComment);
            tvItemLikes = itemView.findViewById(R.id.tvItemLikes);
            tvItemDislikes = itemView.findViewById(R.id.tvItemDislikes);

            btnLike = itemView.findViewById(R.id.btnLike);
            btnDislike = itemView.findViewById(R.id.btnDislike);
            btnFlagReview = itemView.findViewById(R.id.btnFlagReview);
            btnDeleteReview = itemView.findViewById(R.id.btnDeleteReview);
        }
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_review, parent, false);

        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviews.get(position);

        holder.tvItemUsername.setText(review.username);
        holder.tvItemCreatedOn.setText("Created On: " + review.created_on);
        holder.tvItemOverall.setText("Overall: " + review.overall_rating + "/5");
        holder.tvItemDifficulty.setText("Difficulty: " + review.difficulty_rating + "/5");
        holder.tvItemWorkload.setText("Workload: " + review.workload_rating + "/5");
        holder.tvItemRecommendation.setText("Recommended: " + (review.recommendation ? "Yes" : "No"));
        holder.tvItemGrade.setText("Grade Received: " + review.grade_received);
        holder.tvItemComment.setText(review.comment);
        holder.tvItemLikes.setText("Likes: " + review.like_count);
        holder.tvItemDislikes.setText("Dislikes: " + review.dislike_count);

        if (review.user_id == currentUserId) {
            holder.btnDeleteReview.setVisibility(View.VISIBLE);
            holder.btnFlagReview.setVisibility(View.GONE);
        } else {
            holder.btnDeleteReview.setVisibility(View.GONE);
            holder.btnFlagReview.setVisibility(View.VISIBLE);
        }

        holder.btnLike.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION && listener != null) {
                listener.onLikeClicked(adapterPosition);
            }
        });

        holder.btnDislike.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION && listener != null) {
                listener.onDislikeClicked(adapterPosition);
            }
        });

        holder.btnFlagReview.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION && listener != null) {
                listener.onFlagClicked(adapterPosition);
            }
        });

        holder.btnDeleteReview.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();
            if (adapterPosition != RecyclerView.NO_POSITION && listener != null) {
                listener.onDeleteClicked(adapterPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return reviews.size();
    }
}