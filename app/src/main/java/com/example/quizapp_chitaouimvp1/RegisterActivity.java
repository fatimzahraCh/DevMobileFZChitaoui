package com.example.quizapp_chitaouimvp1;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import java.io.IOException;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName, etEmailReg, etAge, etPasswordReg, etConfirmPassword;
    private Button bRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etFullName = findViewById(R.id.etFullName);
        etEmailReg = findViewById(R.id.etEmail);
        etAge = findViewById(R.id.etAge);
        etPasswordReg = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        bRegister = findViewById(R.id.bRegister);
        TextView tvLoginLink = findViewById(R.id.tvLogin);

        bRegister.setOnClickListener(v -> {
            String nom = etFullName.getText().toString().trim();
            String email = etEmailReg.getText().toString().trim();
            String age = etAge.getText().toString().trim();
            String mdp = etPasswordReg.getText().toString().trim();
            String confirmMdp = etConfirmPassword.getText().toString().trim();

            if (nom.isEmpty() || email.isEmpty() || age.isEmpty() || mdp.isEmpty()) {
                Toast.makeText(this, "Veuillez remplir tous les champs", Toast.LENGTH_SHORT).show();
            } else if (!mdp.equals(confirmMdp)) {
                Toast.makeText(this, "Les mots de passe ne correspondent pas", Toast.LENGTH_SHORT).show();
            } else {
                bRegister.setEnabled(false);
                bRegister.setText("Création du compte...");

                SupabaseManager.getInstance().signUp(email, mdp, nom, age, new Callback() {
                    @Override
                    public void onFailure(@NonNull Call call, @NonNull IOException e) {
                        showToast("Erreur réseau : " + e.getMessage());
                    }

                    @Override
                    public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                        String body = response.body() != null ? response.body().string() : "";
                        if (response.isSuccessful()) {
                            try {
                                JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                                String uid = "";
                                if (json.has("id")) {
                                    uid = json.get("id").getAsString();
                                } else if (json.has("user")) {
                                    uid = json.getAsJsonObject("user").get("id").getAsString();
                                }
                                
                                if (!uid.isEmpty()) {
                                    insertIntoUsersTable(uid, email, nom, age);
                                } else {
                                    showToast("Erreur : Impossible de récupérer l'UID");
                                }
                            } catch (Exception e) {
                                showToast("Erreur parsing JSON");
                            }
                        } else {
                            showToast("Erreur Auth (" + response.code() + "): " + body);
                        }
                    }
                });
            }
        });

        tvLoginLink.setOnClickListener(v -> finish());
    }

    private void insertIntoUsersTable(String uid, String email, String nom, String age) {
        SupabaseManager.getInstance().insertUserRecord(uid, email, nom, age, new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                showToast("Auth OK, mais erreur insertion base");
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                final String errorBody = response.body() != null ? response.body().string() : "";
                final int code = response.code();
                
                runOnUiThread(() -> {
                    if (response.isSuccessful()) {
                        Toast.makeText(RegisterActivity.this, "Inscription réussie !", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(RegisterActivity.this, "Erreur DB (" + code + "): " + errorBody, Toast.LENGTH_LONG).show();
                        bRegister.setEnabled(true);
                        bRegister.setText("S'INSCRIRE");
                    }
                });
            }
        });
    }

    private void showToast(String message) {
        runOnUiThread(() -> {
            Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_LONG).show();
            bRegister.setEnabled(true);
            bRegister.setText("S'INSCRIRE");
        });
    }
}