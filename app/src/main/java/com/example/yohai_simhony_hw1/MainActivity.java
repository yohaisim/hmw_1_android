package com.example.yohai_simhony_hw1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.gms.maps.model.LatLng;
import java.util.Random;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.location.Address;
import java.io.IOException;







public class MainActivity extends AppCompatActivity {

    private ImageView spaceshipLeft, spaceshipMiddleLeft, spaceshipMiddle, spaceshipMiddleRight, spaceshipRight;
    private ImageView[][] obstacleMatrix;
    private ImageView[][] coinMatrix;
    private final Handler handler = new Handler();
    private final Random random = new Random();
    private Runnable runnable;
    private ImageView[] lifeImages;
    private int lives = 3;
    private int score = 0;
    private Vibrator vibrator;
    private boolean isGameActive = true;
    private ImageButton buttonLeft, buttonRight;
    private int delayMillis = 200; // default delay
    private boolean sensorMode = false;
    private SensorManager sensorManager;
    private SensorEventListener sensorEventListener;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private LatLng currentLocation;
    private MediaPlayer collisionSound, coinSound;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        collisionSound = MediaPlayer.create(this, R.raw.hitsound);
        coinSound = MediaPlayer.create(this, R.raw.coin_sound);
        Intent intent = getIntent();
        delayMillis = intent.getIntExtra("DELAY_MILLIS", 200);
        sensorMode = intent.getBooleanExtra("SENSOR_MODE", false);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        spaceshipLeft = findViewById(R.id.main_IMG_spaceshipLeft);
        spaceshipMiddleLeft = findViewById(R.id.main_IMG_spaceshipMiddleLeft);
        spaceshipMiddle = findViewById(R.id.main_IMG_spaceshipMiddle);
        spaceshipMiddleRight = findViewById(R.id.main_IMG_spaceshipMiddleRight);
        spaceshipRight = findViewById(R.id.main_IMG_spaceshipRight);
        buttonLeft = findViewById(R.id.main_BTN_left);
        buttonRight = findViewById(R.id.main_BTN_right);

        if (sensorMode) {
            buttonLeft.setVisibility(View.GONE);
            buttonRight.setVisibility(View.GONE);
            setupSensor();
        } else {
            buttonLeft.setVisibility(View.VISIBLE);
            buttonRight.setVisibility(View.VISIBLE);
            buttonLeft.setOnClickListener(v -> {
                if (isGameActive) moveSpaceshipLeft();
            });
            buttonRight.setOnClickListener(v -> {
                if (isGameActive) moveSpaceshipRight();
            });
        }

        spaceshipLeft.setVisibility(View.INVISIBLE);
        spaceshipMiddleLeft.setVisibility(View.INVISIBLE);
        spaceshipMiddle.setVisibility(View.VISIBLE);
        spaceshipMiddleRight.setVisibility(View.INVISIBLE);
        spaceshipRight.setVisibility(View.INVISIBLE);

        lifeImages = new ImageView[]{
                findViewById(R.id.main_IMG_life1),
                findViewById(R.id.main_IMG_life2),
                findViewById(R.id.main_IMG_life3)
        };

        initializeObstacleMatrix();
        initializeCoinMatrix();
        startObstacleMovement();
        requestLocationUpdates();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationUpdates();
            } else {
                Toast.makeText(this, "Location permission is required to play this game", Toast.LENGTH_SHORT).show();
            }
        }
    }



    private void initializeObstacleMatrix() {
        obstacleMatrix = new ImageView[][]{
                {findViewById(R.id.obstacle_1_1), findViewById(R.id.obstacle_1_2), findViewById(R.id.obstacle_1_3), findViewById(R.id.obstacle_1_4), findViewById(R.id.obstacle_1_5)},
                {findViewById(R.id.obstacle_2_1), findViewById(R.id.obstacle_2_2), findViewById(R.id.obstacle_2_3), findViewById(R.id.obstacle_2_4), findViewById(R.id.obstacle_2_5)},
                {findViewById(R.id.obstacle_3_1), findViewById(R.id.obstacle_3_2), findViewById(R.id.obstacle_3_3), findViewById(R.id.obstacle_3_4), findViewById(R.id.obstacle_3_5)},
                {findViewById(R.id.obstacle_4_1), findViewById(R.id.obstacle_4_2), findViewById(R.id.obstacle_4_3), findViewById(R.id.obstacle_4_4), findViewById(R.id.obstacle_4_5)},
                {findViewById(R.id.obstacle_5_1), findViewById(R.id.obstacle_5_2), findViewById(R.id.obstacle_5_3), findViewById(R.id.obstacle_5_4), findViewById(R.id.obstacle_5_5)},
                {findViewById(R.id.obstacle_6_1), findViewById(R.id.obstacle_6_2), findViewById(R.id.obstacle_6_3), findViewById(R.id.obstacle_6_4), findViewById(R.id.obstacle_6_5)},
                {findViewById(R.id.obstacle_7_1), findViewById(R.id.obstacle_7_2), findViewById(R.id.obstacle_7_3), findViewById(R.id.obstacle_7_4), findViewById(R.id.obstacle_7_5)},
                {findViewById(R.id.obstacle_8_1), findViewById(R.id.obstacle_8_2), findViewById(R.id.obstacle_8_3), findViewById(R.id.obstacle_8_4), findViewById(R.id.obstacle_8_5)},
                {findViewById(R.id.obstacle_9_1), findViewById(R.id.obstacle_9_2), findViewById(R.id.obstacle_9_3), findViewById(R.id.obstacle_9_4), findViewById(R.id.obstacle_9_5)},
                {findViewById(R.id.obstacle_10_1), findViewById(R.id.obstacle_10_2), findViewById(R.id.obstacle_10_3), findViewById(R.id.obstacle_10_4), findViewById(R.id.obstacle_10_5)}
        };

        for (ImageView[] row : obstacleMatrix) {
            for (ImageView imageView : row) {
                imageView.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void initializeCoinMatrix() {
        coinMatrix = new ImageView[][]{
                {findViewById(R.id.coin_1_1), findViewById(R.id.coin_1_2), findViewById(R.id.coin_1_3), findViewById(R.id.coin_1_4), findViewById(R.id.coin_1_5)},
                {findViewById(R.id.coin_2_1), findViewById(R.id.coin_2_2), findViewById(R.id.coin_2_3), findViewById(R.id.coin_2_4), findViewById(R.id.coin_2_5)},
                {findViewById(R.id.coin_3_1), findViewById(R.id.coin_3_2), findViewById(R.id.coin_3_3), findViewById(R.id.coin_3_4), findViewById(R.id.coin_3_5)},
                {findViewById(R.id.coin_4_1), findViewById(R.id.coin_4_2), findViewById(R.id.coin_4_3), findViewById(R.id.coin_4_4), findViewById(R.id.coin_4_5)},
                {findViewById(R.id.coin_5_1), findViewById(R.id.coin_5_2), findViewById(R.id.coin_5_3), findViewById(R.id.coin_5_4), findViewById(R.id.coin_5_5)},
                {findViewById(R.id.coin_6_1), findViewById(R.id.coin_6_2), findViewById(R.id.coin_6_3), findViewById(R.id.coin_6_4), findViewById(R.id.coin_6_5)},
                {findViewById(R.id.coin_7_1), findViewById(R.id.coin_7_2), findViewById(R.id.coin_7_3), findViewById(R.id.coin_7_4), findViewById(R.id.coin_7_5)},
                {findViewById(R.id.coin_8_1), findViewById(R.id.coin_8_2), findViewById(R.id.coin_8_3), findViewById(R.id.coin_8_4), findViewById(R.id.coin_8_5)},
                {findViewById(R.id.coin_9_1), findViewById(R.id.coin_9_2), findViewById(R.id.coin_9_3), findViewById(R.id.coin_9_4), findViewById(R.id.coin_9_5)},
                {findViewById(R.id.coin_10_1), findViewById(R.id.coin_10_2), findViewById(R.id.coin_10_3), findViewById(R.id.coin_10_4), findViewById(R.id.coin_10_5)}
        };

        for (ImageView[] row : coinMatrix) {
            for (ImageView imageView : row) {
                imageView.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void startObstacleMovement() {
        runnable = new Runnable() {
            @Override
            public void run() {
                if (isGameActive) {
                    moveObstaclesAndCoinsDown();
                    spawnNewObstacles();
                    spawnNewCoins();
                    checkCollision();
                    handler.postDelayed(this, delayMillis); // Use delayMillis from the intent
                }
            }
        };
        handler.post(runnable);
    }

    private void moveObstaclesAndCoinsDown() {
        for (int row = obstacleMatrix.length - 1; row >= 0; row--) {
            for (int col = 0; col < obstacleMatrix[row].length; col++) {
                if (row == obstacleMatrix.length - 1) {
                    obstacleMatrix[row][col].setVisibility(View.INVISIBLE);
                    coinMatrix[row][col].setVisibility(View.INVISIBLE);
                } else if (obstacleMatrix[row][col].getVisibility() == View.VISIBLE) {
                    obstacleMatrix[row + 1][col].setVisibility(View.VISIBLE);
                    obstacleMatrix[row][col].setVisibility(View.INVISIBLE);
                }
                if (coinMatrix[row][col].getVisibility() == View.VISIBLE) {
                    coinMatrix[row + 1][col].setVisibility(View.VISIBLE);
                    coinMatrix[row][col].setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    private void spawnNewObstacles() {
        int column = random.nextInt(5);


        if (coinMatrix[0][column].getVisibility() == View.VISIBLE) {
            return;
        }

        for (ImageView[] matrix : obstacleMatrix) {
            for (int col = 0; col < matrix.length; col++) {
                if (matrix[col].getVisibility() == View.VISIBLE) {
                    if (col == column || col == column - 1 || col == column + 1) {
                        return;
                    }
                }
            }
        }

        obstacleMatrix[0][column].setVisibility(View.VISIBLE);
    }

    private void spawnNewCoins() {
        int column = random.nextInt(5);

        if (obstacleMatrix[0][column].getVisibility() == View.VISIBLE) {
            return;
        }

        for (ImageView[] matrix : coinMatrix) {
            for (int col = 0; col < matrix.length; col++) {
                if (matrix[col].getVisibility() == View.VISIBLE) {
                    if (col == column || col == column - 1 || col == column + 1) {
                        return;
                    }
                }
            }
        }

        coinMatrix[0][column].setVisibility(View.VISIBLE);
    }


    private void checkCollision() {
        int spaceshipPosition = getSpaceshipPosition();
        if (spaceshipPosition != -1 && obstacleMatrix[obstacleMatrix.length - 1][spaceshipPosition].getVisibility() == View.VISIBLE) {
            loseLife();
        }
        if (spaceshipPosition != -1 && coinMatrix[coinMatrix.length - 1][spaceshipPosition].getVisibility() == View.VISIBLE) {
            collectCoin(spaceshipPosition);
        }
    }

    private int getSpaceshipPosition() {
        if (spaceshipLeft.getVisibility() == View.VISIBLE) return 0;
        if (spaceshipMiddleLeft.getVisibility() == View.VISIBLE) return 1;
        if (spaceshipMiddle.getVisibility() == View.VISIBLE) return 2;
        if (spaceshipMiddleRight.getVisibility() == View.VISIBLE) return 3;
        if (spaceshipRight.getVisibility() == View.VISIBLE) return 4;
        return -1;
    }

    private void loseLife() {
        lives--;
        updateLifeDisplay();
        vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
        collisionSound.start();
        if (lives == 0) {
            gameOver();
        }
    }


    private void collectCoin(int position) {
        score += 10;
        updateScoreDisplay();
        coinSound.start();

        coinMatrix[coinMatrix.length - 1][position].setVisibility(View.INVISIBLE);
    }

    private void updateLifeDisplay() {
        for (int i = 0; i < lifeImages.length; i++) {
            lifeImages[i].setVisibility(i < lives ? View.VISIBLE : View.INVISIBLE);
        }
    }

    @SuppressLint("DefaultLocale")
    private void updateScoreDisplay() {
        AppCompatTextView scoreTextView = findViewById(R.id.textView);
        scoreTextView.setText(String.format("%03d", score));
    }

    private void gameOver() {
        isGameActive = false;
        handler.removeCallbacks(runnable);

        // Show toast message
        Toast.makeText(this, "Game Over!", Toast.LENGTH_LONG).show();

        // Vibrate
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE));
        }

        // Hide all obstacles and coins
        for (ImageView[] row : obstacleMatrix) {
            for (ImageView imageView : row) {
                imageView.setVisibility(View.INVISIBLE);
            }
        }
        for (ImageView[] row : coinMatrix) {
            for (ImageView imageView : row) {
                imageView.setVisibility(View.INVISIBLE);
            }
        }

        // Save high score and location
        saveHighScore();

        Intent intent = new Intent(MainActivity.this, HighScoresActivity.class);
        startActivity(intent);
        finish();
        // Reset spaceship position
        spaceshipLeft.setVisibility(View.INVISIBLE);
        spaceshipMiddleLeft.setVisibility(View.INVISIBLE);
        spaceshipMiddle.setVisibility(View.VISIBLE);
        spaceshipMiddleRight.setVisibility(View.INVISIBLE);
        spaceshipRight.setVisibility(View.INVISIBLE);

        // Disable movement buttons
        buttonLeft.setEnabled(false);
        buttonRight.setEnabled(false);
    }

    private void saveHighScore() {
        SharedPreferences prefs = getSharedPreferences("high_scores", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        List<HighScore> highScores = getHighScores(prefs);
        highScores.add(new HighScore(score, getCurrentLocation(), getCurrentDate(), currentLocation));

        highScores.sort((o1, o2) -> Integer.compare(o2.getScore(), o1.getScore()));

        if (highScores.size() > 10) {
            highScores = highScores.subList(0, 10);
        }
        String cityName = getCityName(currentLocation);
        highScores.add(new HighScore(score, cityName, getCurrentDate(), currentLocation));
        editor.clear(); // Clear existing scores

        for (int i = 0; i < highScores.size(); i++) {
            HighScore highScore = highScores.get(i);
            editor.putString("score_" + i,
                    highScore.getScore() + "," +
                            highScore.getLocation() + "," +
                            highScore.getDate() + "," +
                            highScore.getLatLng().latitude + "," +
                            highScore.getLatLng().longitude);
        }

        editor.apply();
    }
    private String getCityName(LatLng location) {
        Geocoder geocoder = new Geocoder(this);
        String cityName = "Unknown Location";
        try {
            List<Address> addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                cityName = addresses.get(0).getLocality();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cityName;
    }




    private String getCurrentLocation() {
        return currentLocation != null ?
                "Lat: " + currentLocation.latitude + ", Lng: " + currentLocation.longitude :
                "Unknown Location";
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }


    private List<HighScore> getHighScores(SharedPreferences prefs) {
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



    private void moveSpaceshipLeft() {
        if (spaceshipMiddleRight.getVisibility() == View.VISIBLE) {
            spaceshipMiddleRight.setVisibility(View.INVISIBLE);
            spaceshipMiddle.setVisibility(View.VISIBLE);
        } else if (spaceshipRight.getVisibility() == View.VISIBLE) {
            spaceshipRight.setVisibility(View.INVISIBLE);
            spaceshipMiddleRight.setVisibility(View.VISIBLE);
        } else if (spaceshipMiddle.getVisibility() == View.VISIBLE) {
            spaceshipMiddle.setVisibility(View.INVISIBLE);
            spaceshipMiddleLeft.setVisibility(View.VISIBLE);
        } else if (spaceshipMiddleLeft.getVisibility() == View.VISIBLE) {
            spaceshipMiddleLeft.setVisibility(View.INVISIBLE);
            spaceshipLeft.setVisibility(View.VISIBLE);
        }
    }

    private void moveSpaceshipRight() {
        if (spaceshipMiddleLeft.getVisibility() == View.VISIBLE) {
            spaceshipMiddleLeft.setVisibility(View.INVISIBLE);
            spaceshipMiddle.setVisibility(View.VISIBLE);
        } else if (spaceshipMiddle.getVisibility() == View.VISIBLE) {
            spaceshipMiddle.setVisibility(View.INVISIBLE);
            spaceshipMiddleRight.setVisibility(View.VISIBLE);
        } else if (spaceshipMiddleRight.getVisibility() == View.VISIBLE) {
            spaceshipMiddleRight.setVisibility(View.INVISIBLE);
            spaceshipRight.setVisibility(View.VISIBLE);
        } else if (spaceshipLeft.getVisibility() == View.VISIBLE) {
            spaceshipLeft.setVisibility(View.INVISIBLE);
            spaceshipMiddleLeft.setVisibility(View.VISIBLE);
        }
    }

    private void setupSensor() {
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorEventListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                if (isGameActive) {
                    float x = event.values[0];
                    if (x > 2) {
                        moveSpaceshipLeft();
                    } else if (x < -2) {
                        moveSpaceshipRight();
                    }
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        };
        sensorManager.registerListener(sensorEventListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
        if (sensorMode) {
            sensorManager.unregisterListener(sensorEventListener);
        }
        if (collisionSound != null) {
            collisionSound.release();
            collisionSound = null;
        }
    }

    private void requestLocationUpdates() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(@NonNull String provider) {}

            @Override
            public void onProviderDisabled(@NonNull String provider) {}
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

}
