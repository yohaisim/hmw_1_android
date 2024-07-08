package com.example.yohai_simhony_hw1;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.ArrayList;
import java.util.List;
import android.content.SharedPreferences;
import java.util.Collections;
import java.util.Comparator;


public class HighScoresActivity extends FragmentActivity implements OnMapReadyCallback, HighScoresAdapter.OnItemClickListener {

    private GoogleMap map;
    private MapFragment mapFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_high_scores);

        RecyclerView recyclerView = findViewById(R.id.recycler_view_high_scores);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<HighScore> highScores = getHighScores();
        highScores.sort((h1, h2) -> Integer.compare(h2.getScore(), h1.getScore()));

        HighScoresAdapter adapter = new HighScoresAdapter(highScores, this);
        recyclerView.setAdapter(adapter);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private List<HighScore> getHighScores() {
        SharedPreferences prefs = getSharedPreferences("high_scores", MODE_PRIVATE);
        List<HighScore> highScores = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String scoreStr = prefs.getString("score_" + i, null);
            if (scoreStr != null) {
                String[] parts = scoreStr.split(",");
                if (parts.length == 5) {
                    int score = Integer.parseInt(parts[0]);
                    String location = parts[1];
                    String date = parts[2];
                    double lat = Double.parseDouble(parts[3]);
                    double lng = Double.parseDouble(parts[4]);
                    highScores.add(new HighScore(score, location, date, new LatLng(lat, lng)));
                }
            }
        }
        return highScores;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
    }

    @Override
    public void onItemClick(LatLng location) {
        if (map != null) {
            map.clear();
            map.addMarker(new MarkerOptions().position(location).title("High Score Location"));
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15));
        } else {
            Toast.makeText(this, "Map is not ready", Toast.LENGTH_SHORT).show();
        }
    }
}
