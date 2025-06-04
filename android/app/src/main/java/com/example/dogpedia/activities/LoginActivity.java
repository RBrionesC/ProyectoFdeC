package com.example.dogpedia.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.dogpedia.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private EditText Email, Password;
    private static final String URL = "http://10.0.2.2:8000/sessions/";
    private RequestQueue queue;
    private Context context = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        queue = Volley.newRequestQueue(this);

        try {
            Email = findViewById(R.id.EmailL);
            Password = findViewById(R.id.PasswordL);
            Button btnLogin = findViewById(R.id.ButtonLogin);
            TextView register = findViewById(R.id.RegisterL);

            if (btnLogin != null) {
                btnLogin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        validateData();

                    }
                });
            }
            if (register != null) {
                register.setOnClickListener(v -> {
                    Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                    startActivity(intent);
                });
            }

        } catch (Exception e) {
            Log.e("LoginActivity", "Error en onCreate: " + e.getMessage());
            Toast.makeText(this, "Error initializing the application", Toast.LENGTH_LONG).show();
        }
    }

    private void validateData() {
        if (Email == null || Password == null) return;

        String email = Objects.requireNonNull(Email.getText().toString().trim());
        String password = Objects.requireNonNull(Password.getText().toString().trim());

        if (email.isEmpty()) {
            Email.setError("Enter a Email");
            return;
        }

        if (password.isEmpty()) {
            Password.setError("Enter password");
        }

        login(email, password);
    }

    private void login(String email, String password) {
        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("email", email);
            requestBody.put("password", password);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    URL,
                    requestBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                saveToken(response);
                                Toast.makeText(context, "¡Successful login!", Toast.LENGTH_SHORT).show();
                                startMainActivity();
                            } catch (JSONException e) {
                                Toast.makeText(context, "Login error", Toast.LENGTH_SHORT).show();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            String message = "Connection error";
                            if (error.networkResponse != null) {
                                if (error.networkResponse.statusCode == 401) {
                                    message = "Incredible data";
                                }
                            }
                            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
                        }
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-type", "application/json");
                    String token = getToken();
                    if (token != null) {
                        headers.put("SessionToken", token);
                    }

                    return headers;
                }

            };

            queue.add(request);
        } catch (JSONException e) {
            Toast.makeText(context, "Error creating request", Toast.LENGTH_LONG).show();
        }
    }

    private String getToken() {
        SharedPreferences preferences = getSharedPreferences("DOGPEDIA_APP_PREFS", MODE_PRIVATE);
        return preferences.getString("SessionToken", null);
    }

    private void saveToken(JSONObject requestBody) throws JSONException{
        SharedPreferences preferences = getSharedPreferences("DOGPEDIA_APP_PREFS", MODE_PRIVATE);
        SharedPreferences.Editor edit = preferences.edit();
        Log.i("SAVE TOKEN","saveToken: "+ requestBody.toString());
        edit.putString("SessionToken", requestBody.getString("token"));
        edit.putString("NAME", requestBody.getString("name"));
        edit.putString("EMAIL", requestBody.getString("email"));

        edit.apply();
    }

    private void startMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

}
