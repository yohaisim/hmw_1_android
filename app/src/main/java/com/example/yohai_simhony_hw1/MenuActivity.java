package com.example.yohai_simhony_hw1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        Button fastButton = findViewById(R.id.menu_BTN_fast);
        Button slowButton = findViewById(R.id.menu_BTN_slow);
        Button sensorButton = findViewById(R.id.menu_BTN_sensor);
        Button highScoresButton = findViewById(R.id.menu_BTN_highscores);

        fastButton.setOnClickListener(v -> startGame(100));
        slowButton.setOnClickListener(v -> startGame(200));
        sensorButton.setOnClickListener(v -> startSensorMode());
        highScoresButton.setOnClickListener(v -> showHighScores());

    }

    private void startGame(int delayMillis) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("DELAY_MILLIS", delayMillis);
        startActivity(intent);
    }

    private void startSensorMode() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("SENSOR_MODE", true);
        startActivity(intent);
    }

    private void showHighScores() {
        Intent intent = new Intent(this, HighScoresActivity.class);
        startActivity(intent);
    }

}
