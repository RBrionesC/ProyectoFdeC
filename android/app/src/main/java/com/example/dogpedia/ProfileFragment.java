package com.example.dogpedia;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ProfileFragment extends Fragment {
    private RequestQueue requestQueue;
    private static final int PICK_IMAGE_REQUEST = 1;
    private String base64Image;
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private ImageView ivProfile;
    private EditText etDogName, etBirthdate, etBreed, etWeight;
    private Button btnEdit, btnSave;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        this.requestQueue = Volley.newRequestQueue(view.getContext());


        ivProfile = view.findViewById(R.id.ivProfile);
        etDogName = view.findViewById(R.id.etDogName);
        etBirthdate = view.findViewById(R.id.etBirthdate);
        etBreed = view.findViewById(R.id.etBreed);
        etWeight = view.findViewById(R.id.etWeight);
        btnEdit = view.findViewById(R.id.Edit);
        btnSave = view.findViewById(R.id.Save);

        TextView name = view.findViewById(R.id.ivName);
        name.setText(getContext().getSharedPreferences("DOGPEDIA_APP_PREFS", Context.MODE_PRIVATE).getString("NAME", null));

        TextView email = view.findViewById(R.id.ivEmail);
        email.setText(getContext().getSharedPreferences("DOGPEDIA_APP_PREFS", Context.MODE_PRIVATE).getString("EMAIL",null));
        loadUserProfile();

        toggleEditing(false);
        btnEdit.setOnClickListener(v -> toggleEditing(true));
        btnSave.setOnClickListener(v -> {
            toggleEditing(false);
            saveChanges();
        });

        view.findViewById(R.id.Plogout).setOnClickListener(view1 -> {
            String url = "http://10.0.2.2:8000/sessions/";
            SharedPreferences prefs = view.getContext().getSharedPreferences("DOGPEDIA_APP_PREFS", Context.MODE_PRIVATE);
            String sessionToken = prefs.getString("SessionToken", null);

            if (sessionToken == null) {
                Toast.makeText(view1.getContext(), "No session token found", Toast.LENGTH_SHORT).show();
                return;
            }

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.DELETE,
                    url,
                    null,
                    succes -> {
                        Toast.makeText(view1.getContext(), "Closed session", Toast.LENGTH_SHORT).show();
                        prefs.edit().clear().apply();
                        startActivity(new Intent(view1.getContext(), LoginActivity.class));
                    },
                    error -> {
                        if (error.networkResponse != null) {
                            int statusCode = error.networkResponse.statusCode;
                            Log.e("API_ERROR", "Error code:" + statusCode);
                            if (error.networkResponse.data != null) {
                                String responseBody = new String(error.networkResponse.data);
                                Log.e("API_ERROR", "Erro details: " + responseBody);
                            }
                        }
                        Toast.makeText(view1.getContext(), "Could not log out", Toast.LENGTH_SHORT).show();
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("SessionToken", sessionToken);
                    return headers;
                }
            };
            requestQueue.add(request);
        });


        ImageButton chooseImage = view.findViewById(R.id.ChooseImage);
        chooseImage.setOnClickListener(v -> openImageChooser());

        // Registrar el launcher para seleccionar imagen
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                        Uri uri = result.getData().getData();
                        try {
                            Bitmap bmp = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), uri);
                            ivProfile.setImageBitmap(bmp);
                            convertImageToBase64(bmp);
                            uploadProfileImage(base64Image);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Error loading image.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );


        return view;


    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        imagePickerLauncher.launch(Intent.createChooser(intent, "Select an image"));
    }


    private void convertImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    private void uploadProfileImage(String base64Image) {
        String url = "http://10.0.2.2:8000/users/upload_avatar/";
        SharedPreferences prefs = requireContext().getSharedPreferences("DOGPEDIA_APP_PREFS", Context.MODE_PRIVATE);
        String sessionToken = prefs.getString("SessionToken", null);

        if (sessionToken == null) {
            Toast.makeText(getContext(), "Session token not found", Toast.LENGTH_SHORT).show();
            return;
        }

        JSONObject body = new JSONObject();
        try {
            body.put("imagen", "data:image/jpeg;base64," + base64Image);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> {
                    Toast.makeText(getContext(), "Image uploaded successfully", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    Log.e("UPLOAD_ERROR", "Error uploading image: " + error.toString());
                    Toast.makeText(getContext(), "Error uploading image", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("SessionToken", sessionToken);
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(request);
    }

    private void loadUserProfile() {
        String url = "http://10.0.2.2:8000/users/profile/";
        SharedPreferences prefs = requireContext().getSharedPreferences("DOGPEDIA_APP_PREFS", Context.MODE_PRIVATE);
        String sessionToken = prefs.getString("SessionToken", null);

        if (sessionToken == null) {
            Toast.makeText(getContext(), "Session token not found", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        String imageUrl = response.getString("avatar");
                        String dogName = response.optString("dog_name", "");
                        String birthdate = response.optString("birthdate", "");
                        String breed = response.optString("breed", "");
                        String weight = response.optString("weight", "");

                        if (etDogName != null) etDogName.setText(dogName);
                        if (etBirthdate != null) etBirthdate.setText(birthdate);
                        if (etBreed != null) etBreed.setText(breed);
                        if (etWeight != null)
                            etWeight.setText(weight + (weight.isEmpty() ? "" : " kg"));

                        loadImageFromUrl(imageUrl);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error processing profile response", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    Log.e("PROFILE_LOAD_ERROR", "Error loading profile: " + error.toString());
                    Toast.makeText(getContext(), "Error loading profile", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("SessionToken", sessionToken);
                return headers;
            }
        };

        requestQueue.add(request);
    }

    private void loadImageFromUrl(String url) {
        com.android.volley.toolbox.ImageRequest imageRequest = new com.android.volley.toolbox.ImageRequest(
                url,
                bitmap -> ivProfile.setImageBitmap(bitmap),
                0, 0, ImageView.ScaleType.CENTER_CROP, Bitmap.Config.RGB_565,
                error -> Log.e("IMAGE_LOAD_ERROR", "Could not load profile picture: " + error.toString())
        );

        requestQueue.add(imageRequest);
    }

    private void toggleEditing(boolean enabled) {
        etDogName.setEnabled(enabled);
        etBirthdate.setEnabled(enabled);
        etBreed.setEnabled(enabled);
        etWeight.setEnabled(enabled);
    }

    private void saveChanges() {
        String url = "http://10.0.2.2:8000/users/profile/";
        SharedPreferences prefs = requireContext().getSharedPreferences("DOGPEDIA_APP_PREFS", Context.MODE_PRIVATE);
        String sessionToken = prefs.getString("SessionToken", null);

        if (sessionToken == null) {
            Toast.makeText(getContext(), "Session token not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String dogName = etDogName.getText().toString();
        String birthdate = etBirthdate.getText().toString();
        String breed = etBreed.getText().toString();
        String weight = etWeight.getText().toString();

        JSONObject body = new JSONObject();
        try {
            body.put("dog_name", dogName);
            body.put("birthdate", birthdate);
            body.put("breed", breed);
            body.put("weight", weight.replace(" kg", ""));
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getContext(), "Error creating request body", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                body,
                response -> {
                    Toast.makeText(getContext(), "Data saved successfully", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    Log.e("SAVE_PROFILE_ERROR", "Error saving data: " + error.toString());
                    Toast.makeText(getContext(), "Error saving data", Toast.LENGTH_SHORT).show();
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("SessionToken", sessionToken);
                headers.put("Content-Type", "application/json");
                return headers;
            }

        };
        requestQueue.add(request);
    }
}
