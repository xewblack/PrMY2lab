package com.example.prsupabase1;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.prsupabase1.TasksAdapter;
import com.example.prsupabase1.Tasks;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private TasksAdapter adapter;

    private final ExecutorService networkExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Включение Edge-to-Edge режима
        androidx.activity.EdgeToEdge.enable(this);

        setContentView(R.layout.activity_main);

        // Проверяем сессию
        SharedPreferences prefs = getSharedPreferences("session", MODE_PRIVATE);
        accessToken = prefs.getString("access_token", null);

        if (accessToken == null) {
            // Если нет токена, идем на логин
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Настройка RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new TasksAdapter();
        recyclerView.setAdapter(adapter);

        // Загружаем задачи
        loadTasks();
    }

    private void loadTasks() {
        networkExecutor.execute(() -> {
            HttpURLConnection connection = null;
            try {
                URL url = new URL(SupabaseConfig.TABLE_URL + "?select=*");
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("apikey", SupabaseConfig.SUPABASE_ANON_KEY);
                connection.setRequestProperty("Authorization", "Bearer " + accessToken);

                int code = connection.getResponseCode();
                String response = readStream(connection, code);

                if (code == 200) {
                    List<Tasks> tasks = parseTasks(response);
                    mainHandler.post(() -> adapter.setTasks(tasks));
                } else {
                    mainHandler.post(() ->
                            Toast.makeText(MainActivity.this,
                                    "Ошибка загрузки: " + code, Toast.LENGTH_SHORT).show()
                    );
                }
            } catch (Exception e) {
                mainHandler.post(() ->
                        Toast.makeText(MainActivity.this,
                                "Сетевая ошибка: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private List<Tasks> parseTasks(String json) throws JSONException {
        List<Tasks> tasks = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(json);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            Tasks task = new Tasks();

            task.setId(obj.optString("id", ""));
            task.setTitle(obj.optString("title", "Без названия"));
            task.setDescription(obj.optString("description", ""));
            task.setCompleted(obj.optBoolean("completed", false));
            task.setUserId(obj.optString("user_id", ""));
            task.setCreatedAt(obj.optString("created_at", ""));

            tasks.add(task);
        }

        return tasks;
    }

    private String readStream(HttpURLConnection connection, int responseCode) {
        try {
            BufferedReader reader;
            if (responseCode >= 400) {
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
        } catch (Exception e) {
            e.printStackTrace();
            return "{\"error\":\"" + e.getMessage() + "\"}";
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (networkExecutor != null && !networkExecutor.isShutdown()) {
            networkExecutor.shutdown();
        }
    }
}