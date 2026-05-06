package com.example.quizapp_chitaouimvp1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class ResultActivity extends AppCompatActivity {

    private TextView tvScore, tvIQEstimate;
    private Button bRestart, bLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        tvScore = findViewById(R.id.tvScore);
        tvIQEstimate = findViewById(R.id.tvIQ);
        bRestart = findViewById(R.id.bRestart);
        bLogout = findViewById(R.id.bLogout);

        int scoreFinal = getIntent().getIntExtra("SCORE_FINAL", 0);

        tvScore.setText("Votre score : " + scoreFinal + " / 30");

        // Calculer une estimation basique du QI (juste pour l'exemple du MVP)
        // Par exemple : un score de 0 = QI de 70. Chaque point rapporte 3 points de QI. (30 * 3 = 90 + 70 = 160 Max)
        int qiEstime = 70 + (scoreFinal * 3);
        tvIQEstimate.setText("QI Estimé : " + qiEstime);

        // Action pour refaire le test
        bRestart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResultActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Action pour se déconnecter
        bLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ResultActivity.this, MainActivity.class);
                // Effacer l'historique de navigation pour ne pas pouvoir faire "Retour"
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }
}