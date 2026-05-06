package com.example.quizapp_chitaouimvp1;

public class Question {

    private String questionText;
    private String imageUrl; // L'URL de l'image stockée sur Firebase (peut être vide si pas d'image)
    private String option1;
    private String option2;
    private String option3;
    private String option4;
    private int correctAnswerIndex; // 1, 2, 3 ou 4 pour identifier la bonne réponse

    // Constructeur vide (Obligatoire pour récupérer des données depuis Supabase/Firebase plus tard)
    public Question() {
    }

    // Constructeur complet pour créer une question facilement en Java
    public Question(String questionText, String imageUrl, String option1, String option2, String option3, String option4, int correctAnswerIndex) {
        this.questionText = questionText;
        this.imageUrl = imageUrl;
        this.option1 = option1;
        this.option2 = option2;
        this.option3 = option3;
        this.option4 = option4;
        this.correctAnswerIndex = correctAnswerIndex;
    }

    // --- GETTERS (Pour lire les données) ---

    public String getQuestionText() {
        return questionText;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getOption1() {
        return option1;
    }

    public String getOption2() {
        return option2;
    }

    public String getOption3() {
        return option3;
    }

    public String getOption4() {
        return option4;
    }

    public int getCorrectAnswerIndex() {
        return correctAnswerIndex;
    }

    // --- SETTERS (Pour modifier les données si besoin) ---

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setOption1(String option1) {
        this.option1 = option1;
    }

    public void setOption2(String option2) {
        this.option2 = option2;
    }

    public void setOption3(String option3) {
        this.option3 = option3;
    }

    public void setOption4(String option4) {
        this.option4 = option4;
    }

    public void setCorrectAnswerIndex(int correctAnswerIndex) {
        this.correctAnswerIndex = correctAnswerIndex;
    }
}