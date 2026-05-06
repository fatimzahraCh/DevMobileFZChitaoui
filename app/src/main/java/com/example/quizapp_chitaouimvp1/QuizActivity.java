package com.example.quizapp_chitaouimvp1;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public class QuizActivity extends AppCompatActivity {

    private static final long TIMER_DURATION_MS = 20000;

    private TextView tvQuestionCount, tvTimer, tvQuestionText;
    private ImageView ivQuestionImage;
    private RadioGroup rgChoices;
    private RadioButton rbChoice1, rbChoice2, rbChoice3, rbChoice4;

    private int currentQuestionIndex = 0;
    private int score = 0;
    private CountDownTimer countDownTimer;

    // Variables pour la synthèse vocale
    private TextToSpeech textToSpeech;
    private boolean isTtsReady = false;

    // La liste pour stocker les questions téléchargées depuis Supabase
    private List<Question> questionList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        // Liaison avec l'interface XML
        tvQuestionCount = findViewById(R.id.tvQst);
        tvTimer = findViewById(R.id.tvTimer);
        tvQuestionText = findViewById(R.id.tvQuestionText);
        ivQuestionImage = findViewById(R.id.ivQuestionImage);
        rgChoices = findViewById(R.id.rgChoices);
        rbChoice1 = findViewById(R.id.rbChoice1);
        rbChoice2 = findViewById(R.id.rbChoice2);
        rbChoice3 = findViewById(R.id.rbChoice3);
        rbChoice4 = findViewById(R.id.rbChoice4);
        Button bNext = findViewById(R.id.bNext);
        Button btnSpeak = findViewById(R.id.btnSpeak);

        // Initialisation du TextToSpeech
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(Locale.FRENCH);
                if (result != TextToSpeech.LANG_MISSING_DATA && result != TextToSpeech.LANG_NOT_SUPPORTED) {
                    isTtsReady = true;
                    // Si la première question est déjà chargée, on la lit immédiatement
                    if (!questionList.isEmpty() && currentQuestionIndex == 0) {
                        lireQuestionEtReponses();
                    }
                } else {
                    Toast.makeText(this, "Langue vocale (FR) non installée sur cet appareil", Toast.LENGTH_LONG).show();
                }
            }
        });

        // Action du bouton vocal (Lecture manuelle)
        if (btnSpeak != null) {
            btnSpeak.setOnClickListener(v -> lireQuestionEtReponses());
        }

        Toast.makeText(this, R.string.loading_questions, Toast.LENGTH_SHORT).show();
        fetchQuestionsFromSupabase();

        bNext.setOnClickListener(v -> {
            if (rgChoices.getCheckedRadioButtonId() == -1) {
                Toast.makeText(QuizActivity.this, R.string.select_answer, Toast.LENGTH_SHORT).show();
            } else {
                checkAnswer();
            }
        });
    }

    private void fetchQuestionsFromSupabase() {
        Request request = SupabaseManager.getInstance()
                .getAuthenticatedRequestBuilder("/rest/v1/questions?select=*")
                .build();

        SupabaseManager.getInstance().getClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(QuizActivity.this, R.string.no_internet, Toast.LENGTH_LONG).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody body = response.body();
                    if (body != null) {
                        String jsonResponse = body.string();
                        Gson gson = new Gson();
                        Type listType = new TypeToken<ArrayList<Question>>(){}.getType();
                        questionList = gson.fromJson(jsonResponse, listType);

                        runOnUiThread(() -> {
                            if (questionList != null && !questionList.isEmpty()) {
                                loadNextQuestion();
                            } else {
                                Toast.makeText(QuizActivity.this, R.string.no_questions, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }
        });
    }

    private void loadNextQuestion() {
        if (countDownTimer != null) countDownTimer.cancel();

        // On vérifie qu'on n'a pas dépassé la taille de la liste
        if (currentQuestionIndex < questionList.size()) {
            Question currentQuestion = questionList.get(currentQuestionIndex);

            // Mise à jour UI
            tvQuestionCount.setText(getString(R.string.question_count, currentQuestionIndex + 1, questionList.size()));
            rgChoices.clearCheck();
            tvQuestionText.setText(currentQuestion.getQuestionText());
            rbChoice1.setText(currentQuestion.getOption1());
            rbChoice2.setText(currentQuestion.getOption2());
            rbChoice3.setText(currentQuestion.getOption3());
            rbChoice4.setText(currentQuestion.getOption4());

            // Image avec Glide
            String imageUrl = currentQuestion.getImageUrl();
            if (imageUrl != null && !imageUrl.trim().isEmpty()) {
                ivQuestionImage.setVisibility(View.VISIBLE);
                Glide.with(this).load(imageUrl).into(ivQuestionImage);
            } else {
                ivQuestionImage.setVisibility(View.GONE);
            }

            // LECTURE AUTOMATIQUE DE LA QUESTION
            lireQuestionEtReponses();

            startTimer();
        } else {
            finishQuiz();
        }
    }

    private void lireQuestionEtReponses() {
        if (isTtsReady && questionList != null && currentQuestionIndex < questionList.size()) {
            Question q = questionList.get(currentQuestionIndex);

            String texteALire = q.getQuestionText() + ". "
                    + "Choix 1 : " + q.getOption1() + ". "
                    + "Choix 2 : " + q.getOption2() + ". "
                    + "Choix 3 : " + q.getOption3() + ". "
                    + "Choix 4 : " + q.getOption4();

            // QUEUE_FLUSH coupe la parole précédente pour commencer la nouvelle immédiatement
            textToSpeech.speak(texteALire, TextToSpeech.QUEUE_FLUSH, null, "QuestionID");
        } else if (!isTtsReady) {
            Toast.makeText(this, "La voix n'est pas encore prête ou activée.", Toast.LENGTH_SHORT).show();
        }
    }

    private void startTimer() {
        countDownTimer = new CountDownTimer(TIMER_DURATION_MS, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                tvTimer.setText(getString(R.string.timer_format, seconds));
            }

            @Override
            public void onFinish() {
                tvTimer.setText(R.string.timer_zero);
                Toast.makeText(QuizActivity.this, R.string.time_up, Toast.LENGTH_SHORT).show();
                currentQuestionIndex++;
                loadNextQuestion();
            }
        }.start();
    }

    private void checkAnswer() {
        Question currentQuestion = questionList.get(currentQuestionIndex);
        int selectedAnswerIndex = 0;

        int checkedId = rgChoices.getCheckedRadioButtonId();
        if (checkedId == R.id.rbChoice1) selectedAnswerIndex = 1;
        else if (checkedId == R.id.rbChoice2) selectedAnswerIndex = 2;
        else if (checkedId == R.id.rbChoice3) selectedAnswerIndex = 3;
        else if (checkedId == R.id.rbChoice4) selectedAnswerIndex = 4;

        if (selectedAnswerIndex == currentQuestion.getCorrectAnswerIndex()) {
            score++;
            Toast.makeText(this, R.string.correct_answer, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.wrong_answer, Toast.LENGTH_SHORT).show();
        }

        currentQuestionIndex++;
        loadNextQuestion();
    }

    private void finishQuiz() {
        Intent intent = new Intent(this, ResultActivity.class);
        intent.putExtra("SCORE_FINAL", score);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        super.onDestroy();
    }
}