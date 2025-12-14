package com.example.prsupabase1;

public class SupabaseConfig {
    // URL проекта Supabase
    public static final String SUPABASE_URL = "https://tmvfvbbizublqurthjfb.supabase.co";
    // Публичный anon-ключ проекта
    public static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InRtdmZ2YmJpenVibHF1cnRoamZiIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjU2ODk0MzMsImV4cCI6MjA4MTI2NTQzM30.Mr1dVn7bKQ4uIPi_6AV71YVYDeSpJr016Tk_jo0KYNg";
    // Эндпоинты аутентификации
    public static final String AUTH_SIGNUP_URL = SUPABASE_URL + "/auth/v1/signup";
    public static final String AUTH_SIGNIN_URL = SUPABASE_URL + "/auth/v1/token?grant_type=password";
    // Эндпоинт таблицы по варианту
    public static final String TABLE_URL = SUPABASE_URL + "/rest/v1/tasks";
}