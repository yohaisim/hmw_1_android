package com.example.yohai_simhony_hw1;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private ImageView spaceshipLeft, spaceshipMiddle, spaceshipRight;
    private ImageView[][] obstacleMatrix;
    private final Handler handler = new Handler();
    private final Random random = new Random();
    private Runnable runnable;
    private ImageView[] lifeImages;
    private int lives = 3;
    private Vibrator vibrator;
    private boolean isGameActive = true;
    private ImageButton buttonLeft, buttonRight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        spaceshipLeft = findViewById(R.id.main_IMG_spaceshipLeft);
        spaceshipMiddle = findViewById(R.id.main_IMG_spaceshipMiddle);
        spaceshipRight = findViewById(R.id.main_IMG_spaceshipRight);
        buttonLeft = findViewById(R.id.main_BTN_left);
        buttonRight = findViewById(R.id.main_BTN_right);

        spaceshipLeft.setVisibility(View.INVISIBLE);
        spaceshipMiddle.setVisibility(View.VISIBLE);
        spaceshipRight.setVisibility(View.INVISIBLE);

        buttonLeft.setOnClickListener(v -> {
            if (isGameActive) moveSpaceshipLeft();
        });
        buttonRight.setOnClickListener(v -> {
            if (isGameActive) moveSpaceshipRight();
        });

        lifeImages = new ImageView[]{
                findViewById(R.id.main_IMG_life1),
                findViewById(R.id.main_IMG_life2),
                findViewById(R.id.main_IMG_life3)
        };

        initializeObstacleMatrix();
        startObstacleMovement();
    }

    private void initializeObstacleMatrix() {
        obstacleMatrix = new ImageView[][]{
                {findViewById(R.id.obstacle_1_1), findViewById(R.id.obstacle_1_2), findViewById(R.id.obstacle_1_3)},
                {findViewById(R.id.obstacle_2_1), findViewById(R.id.obstacle_2_2), findViewById(R.id.obstacle_2_3)},
                {findViewById(R.id.obstacle_3_1), findViewById(R.id.obstacle_3_2), findViewById(R.id.obstacle_3_3)},
                {findViewById(R.id.obstacle_4_1), findViewById(R.id.obstacle_4_2), findViewById(R.id.obstacle_4_3)},
                {findViewById(R.id.obstacle_5_1), findViewById(R.id.obstacle_5_2), findViewById(R.id.obstacle_5_3)}
        };

        for (ImageView[] row : obstacleMatrix) {
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
                    moveObstaclesDown();
                    spawnNewObstacles();
                    checkCollision();
                    handler.postDelayed(this, 500); // Move obstacles every 0.5 second
                }
            }
        };
        handler.post(runnable);
    }

    private void moveObstaclesDown() {
        for (int row = obstacleMatrix.length - 1; row >= 0; row--) {
            for (int col = 0; col < obstacleMatrix[row].length; col++) {
                if (row == obstacleMatrix.length - 1) {
                    obstacleMatrix[row][col].setVisibility(View.INVISIBLE);
                } else if (obstacleMatrix[row][col].getVisibility() == View.VISIBLE) {
                    obstacleMatrix[row + 1][col].setVisibility(View.VISIBLE);
                    obstacleMatrix[row][col].setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    private void spawnNewObstacles() {
        int column = random.nextInt(3);

        for (ImageView[] matrix : obstacleMatrix) {
            for (int col = 0; col < matrix.length; col++) {
                if (matrix[col].getVisibility() == View.VISIBLE) {
                    if (col == column || col == column - 1 || col == column + 1) {
                        return; // Prevent spawning in adjacent columns
                    }
                }
            }
        }

        obstacleMatrix[0][column].setVisibility(View.VISIBLE);
    }

    private void checkCollision() {
        int spaceshipPosition = getSpaceshipPosition();
        if (spaceshipPosition != -1 && obstacleMatrix[obstacleMatrix.length - 1][spaceshipPosition].getVisibility() == View.VISIBLE) {
            loseLife();
        }
    }

    private int getSpaceshipPosition() {
        if (spaceshipLeft.getVisibility() == View.VISIBLE) return 0;
        if (spaceshipMiddle.getVisibility() == View.VISIBLE) return 1;
        if (spaceshipRight.getVisibility() == View.VISIBLE) return 2;
        return -1;
    }

    private void loseLife() {
        lives--;
        updateLifeDisplay();
        if (lives == 0) {
            gameOver();
        }
    }

    private void updateLifeDisplay() {
        for (int i = 0; i < lifeImages.length; i++) {
            lifeImages[i].setVisibility(i < lives ? View.VISIBLE : View.INVISIBLE);
        }
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

        // Hide all obstacles
        for (ImageView[] row : obstacleMatrix) {
            for (ImageView imageView : row) {
                imageView.setVisibility(View.INVISIBLE);
            }
        }

        // Reset spaceship position
        spaceshipLeft.setVisibility(View.INVISIBLE);
        spaceshipMiddle.setVisibility(View.VISIBLE);
        spaceshipRight.setVisibility(View.INVISIBLE);

        // Disable movement buttons
        buttonLeft.setEnabled(false);
        buttonRight.setEnabled(false);
    }

    private void moveSpaceshipLeft() {
        if (spaceshipMiddle.getVisibility() == View.VISIBLE) {
            spaceshipMiddle.setVisibility(View.INVISIBLE);
            spaceshipLeft.setVisibility(View.VISIBLE);
        } else if (spaceshipRight.getVisibility() == View.VISIBLE) {
            spaceshipRight.setVisibility(View.INVISIBLE);
            spaceshipMiddle.setVisibility(View.VISIBLE);
        }
    }

    private void moveSpaceshipRight() {
        if (spaceshipMiddle.getVisibility() == View.VISIBLE) {
            spaceshipMiddle.setVisibility(View.INVISIBLE);
            spaceshipRight.setVisibility(View.VISIBLE);
        } else if (spaceshipLeft.getVisibility() == View.VISIBLE) {
            spaceshipLeft.setVisibility(View.INVISIBLE);
            spaceshipMiddle.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable);
    }
}