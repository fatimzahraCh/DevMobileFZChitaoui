package com.example.quizapp_chitaouimvp1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.safetynet.SafetyNet;

public class VerificationActivity extends AppCompatActivity {

    // Your reCAPTCHA Site Key
    private static final String SITE_KEY = "6LfD1NMsAAAAAKfXd2G5ocT-lLcchurA1qky8X0t";

    private Button btnSkip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        Button btnVerify = findViewById(R.id.btnVerify);
        btnSkip = findViewById(R.id.btnSkip);

        // Standard click for reCAPTCHA
        btnVerify.setOnClickListener(v -> verifyWithRecaptcha());

        // Skip button click: goes directly to login
        btnSkip.setOnClickListener(v -> {
            Toast.makeText(this, "Redirection vers le login...", Toast.LENGTH_SHORT).show();
            goToLogin();
        });

        // DEVELOPER BYPASS: Long click on verify button to skip immediately
        btnVerify.setOnLongClickListener(v -> {
            Toast.makeText(this, "Bypass mode activé (Dev)", Toast.LENGTH_SHORT).show();
            goToLogin();
            return true;
        });
    }

    private void verifyWithRecaptcha() {
        Toast.makeText(this, "Vérification en cours...", Toast.LENGTH_SHORT).show();
        
        SafetyNet.getClient(this).verifyWithRecaptcha(SITE_KEY)
                .addOnSuccessListener(this, response -> {
                    String userResponseToken = response.getTokenResult();
                    if (userResponseToken != null && !userResponseToken.isEmpty()) {
                        Toast.makeText(VerificationActivity.this, R.string.verification_success, Toast.LENGTH_SHORT).show();
                        goToLogin();
                    }
                })
                .addOnFailureListener(this, e -> {
                    // Important: Show the skip button if it fails (e.g. TIMEOUT on emulator)
                    btnSkip.setVisibility(View.VISIBLE);
                    
                    if (e instanceof ApiException) {
                        ApiException apiException = (ApiException) e;
                        int statusCode = apiException.getStatusCode();
                        String errorMsg = getString(R.string.error_recaptcha, CommonStatusCodes.getStatusCodeString(statusCode));
                        Toast.makeText(VerificationActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    } else {
                        String errorMsg = getString(R.string.error_generic, e.getMessage());
                        Toast.makeText(VerificationActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void goToLogin() {
        // Redirect to Login screen (MainActivity)
        Intent intent = new Intent(VerificationActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
