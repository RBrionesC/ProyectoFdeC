package com.example.dogpedia;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;

import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {
    private EditText Name, Email, Password, ConPassword;
    private RequestQueue queue;
    private static final String BASE_URL = "http://10.0.2.2:8000";
    private static final String TAG = "RegisterActivity";
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        queue = Volley.newRequestQueue(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Registering User...");
        progressDialog.setCancelable(false);
    }

    private void initViews() {
        Name = findViewById(R.id.Name);
        Email = findViewById(R.id.Email);
        Password = findViewById(R.id.Password);
        ConPassword = findViewById(R.id.ConPassword);

        findViewById(R.id.ButtonRegister).setOnClickListener(v -> registerUser());
        findViewById(R.id.text2).setOnClickListener(v -> {
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void registerUser() {
        if (!validateFields()) return;

        progressDialog.show();
        String url = BASE_URL + "/user/";

        JSONObject requestBody = createRequestBody();
        if (requestBody == null) {
            progressDialog.dismiss();
            return;
        }

        Log.d(TAG, "Enviando petición a: " + url);
        Log.d(TAG, "Body: " + requestBody.toString());

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                requestBody,
                response -> {
                    progressDialog.dismiss();
                    try {
                        Log.d(TAG, "Respuesta del servidor: " + response.toString());
                        SharedPreferences prefs = getSharedPreferences("DOGPEDIA_APP_PREFS", MODE_PRIVATE);
                        prefs.edit().putString("EMAIL", requestBody.getString("email")).apply();
                        prefs.edit().putString("NAME", requestBody.getString("name")).apply();
                        //Save the user
                        String token = response.getString("token");
                        //Save token
                        prefs.edit().putString("SessionToken", token).apply();

                        Toast.makeText(this, "¡Successful registration!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(this, MainActivity.class));
                        finish();
                    } catch (JSONException e) {
                        Log.e(TAG, "Error procesando la respuesta", e);
                        Toast.makeText(this, "Server response failed", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    progressDialog.dismiss();
                    String errorMsg = "Error en la solicitud: ";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        errorMsg += new String(error.networkResponse.data);
                    } else {
                        errorMsg += error.toString();
                    }
                    Log.e(TAG, errorMsg);
                    Toast.makeText(this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json; charset=UTF-8");
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                15000,
                1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        queue.add(request);
    }

    private JSONObject createRequestBody() {
        try {
            JSONObject body = new JSONObject();
            body.put("name", Name.getText().toString().trim());
            body.put("email", Email.getText().toString().trim());
            body.put("password", Password.getText().toString().trim());
            Log.d(TAG, "Request body: " + body);
            return body;
        } catch (JSONException e) {
            Log.e(TAG, "Error creando JSON", e);
            Toast.makeText(this, "Error processing data", Toast.LENGTH_SHORT).show();
            return null;
        }

    }

    private boolean validateFields() {
        String name = Name.getText().toString().trim();
        String email = Email.getText().toString().trim();
        String password = Password.getText().toString().trim();
        String conPassword = ConPassword.getText().toString().trim();

        if (name.isEmpty()) {
            Name.setError("The name is required");
            return false;
        }

        if (email.isEmpty()) {
            Email.setError("The email is required");
            return false;
        }

        if (password.isEmpty()) {
            Password.setError("The password is required");
            return false;
        }

        if (password.length() < 6) {
            Password.setError("The password must be at least 6 characters long.");
            return false;
        }

        if (!password.equals(conPassword)) {
            ConPassword.setError("The passwords do not match ");
            return false;
        }

        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
        if (queue != null) {
            queue.cancelAll(TAG);
        }
    }

}
