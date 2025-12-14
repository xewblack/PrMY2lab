package com.example.prsupabase1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private Button signUpButton;
    private Button signInButton;

    private ExecutorService networkExecutor;
    private Handler mainHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Проверяем, есть ли сохраненная сессия
        SharedPreferences prefs = getSharedPreferences("session", MODE_PRIVATE);
        String accessToken = prefs.getString("access_token", null);

        if (accessToken != null) {
            // Если есть токен, сразу переходим в MainActivity
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        // Инициализация UI элементов
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        signUpButton = findViewById(R.id.signUpButton);
        signInButton = findViewById(R.id.signInButton);

        // Инициализация ExecutorService и Handler
        networkExecutor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        // Настройка обработчиков кнопок
        signUpButton.setOnClickListener(v -> signUp());
        signInButton.setOnClickListener(v -> signIn());
    }

    private void signUp() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        networkExecutor.execute(() -> {
            try {
                // Создаем соединение
                URL url = new URL(SupabaseConfig.AUTH_SIGNUP_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // Настраиваем запрос
                connection.setRequestMethod("POST");
                connection.setRequestProperty("apikey", SupabaseConfig.SUPABASE_ANON_KEY);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // Создаем JSON тело
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("email", email);
                jsonBody.put("password", password);

                // Отправляем данные
                OutputStream os = connection.getOutputStream();
                os.write(jsonBody.toString().getBytes());
                os.flush();
                os.close();

                // Получаем ответ
                int responseCode = connection.getResponseCode();
                String response = readStream(connection);

                // Обрабатываем ответ в главном потоке
                mainHandler.post(() -> {
                    if (responseCode == 200) {
                        Toast.makeText(LoginActivity.this,
                                "Регистрация успешна!", Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            JSONObject errorObj = new JSONObject(response);
                            String errorMsg = errorObj.optString("error", "Неизвестная ошибка");
                            Toast.makeText(LoginActivity.this,
                                    "Ошибка регистрации: " + errorMsg, Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            Toast.makeText(LoginActivity.this,
                                    "Ошибка регистрации", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                connection.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() ->
                        Toast.makeText(LoginActivity.this,
                                "Ошибка сети: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    private void signIn() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Заполните все поля", Toast.LENGTH_SHORT).show();
            return;
        }

        networkExecutor.execute(() -> {
            try {
                // Создаем соединение
                URL url = new URL(SupabaseConfig.AUTH_SIGNIN_URL);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // Настраиваем запрос
                connection.setRequestMethod("POST");
                connection.setRequestProperty("apikey", SupabaseConfig.SUPABASE_ANON_KEY);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);

                // Создаем JSON тело
                JSONObject jsonBody = new JSONObject();
                jsonBody.put("email", email);
                jsonBody.put("password", password);

                // Отправляем данные
                OutputStream os = connection.getOutputStream();
                os.write(jsonBody.toString().getBytes());
                os.flush();
                os.close();

                // Получаем ответ
                int responseCode = connection.getResponseCode();
                String response = readStream(connection);

                // Обрабатываем ответ в главном потоке
                mainHandler.post(() -> {
                    if (responseCode == 200) {
                        try {
                            // Парсим JSON ответ
                            JSONObject obj = new JSONObject(response);
                            String accessToken = obj.getString("access_token");
                            String userId = obj.getJSONObject("user").getString("id");

                            // Сохраняем в SharedPreferences
                            SharedPreferences prefs = getSharedPreferences("session", MODE_PRIVATE);
                            prefs.edit()
                                    .putString("access_token", accessToken)
                                    .putString("user_id", userId)
                                    .apply();

                            Toast.makeText(LoginActivity.this,
                                    "Вход выполнен успешно!", Toast.LENGTH_SHORT).show();

                            // Переходим в MainActivity
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();

                        } catch (JSONException e) {
                            Toast.makeText(LoginActivity.this,
                                    "Ошибка парсинга ответа", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        try {
                            JSONObject errorObj = new JSONObject(response);
                            String errorMsg = errorObj.optString("error", "Неизвестная ошибка");
                            Toast.makeText(LoginActivity.this,
                                    "Ошибка входа: " + errorMsg, Toast.LENGTH_LONG).show();
                        } catch (JSONException e) {
                            Toast.makeText(LoginActivity.this,
                                    "Ошибка входа", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                connection.disconnect();

            } catch (Exception e) {
                e.printStackTrace();
                mainHandler.post(() ->
                        Toast.makeText(LoginActivity.this,
                                "Ошибка сети: " + e.getMessage(), Toast.LENGTH_LONG).show());
            }
        });
    }

    // Метод для чтения ответа от сервера
    private String readStream(HttpURLConnection connection) throws IOException {
        BufferedReader reader;
        if (connection.getResponseCode() >= 400) {
            reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
        } else {
            reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        }

        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        return response.toString();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkExecutor != null && !networkExecutor.isShutdown()) {
            networkExecutor.shutdown();
        }
    }
}