package com.example.quizapp_chitaouimvp1;

import com.google.gson.JsonObject;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Callback;

public class SupabaseManager {

    public static final String SUPABASE_URL = "https://ipdlqfienufupexflqfr.supabase.co";
    public static final String SUPABASE_KEY = "sb_publishable_TaPLxnLBgZ3QKpM6Nz0jdw_VZXwLtjS";

    private static SupabaseManager instance;
    private final OkHttpClient client;

    private SupabaseManager() {
        client = new OkHttpClient();
    }

    public static SupabaseManager getInstance() {
        if (instance == null) {
            instance = new SupabaseManager();
        }
        return instance;
    }

    public Request.Builder getAuthenticatedRequestBuilder(String endpoint) {
        return new Request.Builder()
                .url(SUPABASE_URL + endpoint)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .addHeader("Content-Type", "application/json");
    }

    public OkHttpClient getClient() {
        return client;
    }

    public void signUp(String email, String password, String name, String age, Callback callback) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        
        JsonObject json = new JsonObject();
        json.addProperty("email", email);
        json.addProperty("password", password);
        
        JsonObject metadata = new JsonObject();
        metadata.addProperty("full_name", name);
        metadata.addProperty("age", age);
        json.add("data", metadata);

        RequestBody body = RequestBody.create(json.toString(), JSON);
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/auth/v1/signup")
                .addHeader("apikey", SUPABASE_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public void insertUserRecord(String uid, String email, String name, String age, Callback callback) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        
        JsonObject json = new JsonObject();
        json.addProperty("id", uid);
        json.addProperty("email", email);
        json.addProperty("full_name", name);
        json.addProperty("age", age);

        RequestBody body = RequestBody.create(json.toString(), JSON);
        Request request = getAuthenticatedRequestBuilder("/rest/v1/users")
                .post(body)
                .build();

        client.newCall(request).enqueue(callback);
    }

    public void signIn(String email, String password, Callback callback) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        JsonObject json = new JsonObject();
        json.addProperty("email", email);
        json.addProperty("password", password);

        RequestBody body = RequestBody.create(json.toString(), JSON);
        Request request = new Request.Builder()
                .url(SUPABASE_URL + "/auth/v1/token?grant_type=password")
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(callback);
    }
}