package com.example.yohai_simhony_hw1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.maps.model.LatLng;
import java.util.List;

public class HighScoresAdapter extends RecyclerView.Adapter<HighScoresAdapter.HighScoreViewHolder> {

    private final List<HighScore> highScores;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(LatLng location);
    }

    public HighScoresAdapter(List<HighScore> highScores, OnItemClickListener listener) {
        this.highScores = highScores;
        this.listener = listener;
    }

    @NonNull
    @Override
    public HighScoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_high_score, parent, false);
        return new HighScoreViewHolder(view);
    }


    @Override
    public int getItemCount() {
        return highScores.size();
    }

    static class HighScoreViewHolder extends RecyclerView.ViewHolder {
        ImageView avatarImageView;
        TextView scoreTextView;
        TextView locationTextView;
        TextView dateTextView;
        Button showButton;

        public HighScoreViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarImageView = itemView.findViewById(R.id.image_avatar);
            scoreTextView = itemView.findViewById(R.id.text_score);
            locationTextView = itemView.findViewById(R.id.text_location);
            dateTextView = itemView.findViewById(R.id.text_date);
            showButton = itemView.findViewById(R.id.button_show);
        }
    }
    @Override
    public void onBindViewHolder(@NonNull HighScoreViewHolder holder, int position) {
        HighScore highScore = highScores.get(position);
        holder.scoreTextView.setText("Your Score: " + highScore.getScore());
        holder.locationTextView.setText(highScore.getLocation());
        holder.dateTextView.setText(highScore.getDate());

        holder.showButton.setOnClickListener(v -> listener.onItemClick(highScore.getLatLng()));
    }
}

