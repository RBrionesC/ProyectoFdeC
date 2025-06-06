package com.example.dogpedia.fragments;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.dogpedia.R;
import com.example.dogpedia.utils.ThemeHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class SettingsFragment extends Fragment {

    private Switch switchDarkMode;
    private Button buttonChangePassword;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        switchDarkMode = view.findViewById(R.id.switchDarkMode);

        // Configura el switch según la preferencia actual
        switchDarkMode.setChecked(ThemeHelper.isDarkMode(requireContext()));

        // Cambia el modo oscuro/claro al activar/desactivar
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            ThemeHelper.saveThemePreference(requireContext(), isChecked);
            ThemeHelper.applyTheme(requireContext());
            requireActivity().recreate();  // Reinicia la activity para aplicar el cambio
        });

        buttonChangePassword = view.findViewById(R.id.btnPassword);
        buttonChangePassword.setOnClickListener(v -> showChangePasswordDialog());

        return view;
    }

    private void showChangePasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Change password");

        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_change_password, null);
        builder.setView(dialogView);

        EditText currentPassword = dialogView.findViewById(R.id.editCurrentPassword);
        EditText newPassword = dialogView.findViewById(R.id.editNewPassword);
        EditText confirmPassword = dialogView.findViewById(R.id.editConfirmPassword);

        builder.setPositiveButton("Change", (dialog, which) -> {
            String current = currentPassword.getText().toString();
            String newPass = newPassword.getText().toString();
            String confirm = confirmPassword.getText().toString();

            if (!newPass.equals(confirm)) {
                Toast.makeText(getContext(), "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                changePasswordInBackend(current, newPass);
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void changePasswordInBackend(String currentPassword, String newPassword) {
        String url = "http://10.0.2.2:8000/change_password/";

        JSONObject jsonBody = new JSONObject();
        try {
            jsonBody.put("current_password", currentPassword);
            jsonBody.put("new_password", newPassword);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, url, jsonBody,
                response -> {
                    Toast.makeText(getContext(), "Password changed successfully", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    String errorMsg = "Error changing password";
                    if (error.networkResponse != null && error.networkResponse.statusCode == 400) {
                        errorMsg = "Current password incorrect";
                    }
                    Toast.makeText(getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("SessionToken", getToken());
                return headers;
            }

        };

        Volley.newRequestQueue(requireContext()).add(request);
    }
    private String getToken() {
        SharedPreferences preferences = requireContext().getSharedPreferences("DOGPEDIA_APP_PREFS", MODE_PRIVATE);
        return preferences.getString("SessionToken", null);
    }
}
