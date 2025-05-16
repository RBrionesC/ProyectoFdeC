package com.example.dogpedia;

import static java.security.AccessController.getContext;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;

import org.json.JSONArray;
import org.json.JSONObject;

public class DetailBreedActivity extends AppCompatActivity {

    private RequestQueue requestQueue;
    private TextView nameDog, tvBreedFor, tvBreedGroup, tvLifeSpan, tvTemperament, tvOrigin, tvHeight, tvWeight;
    private ImageView imageDog;

    private static final String BASE_URL = "http://10.0.2.2:8000";
    private ProgressDialog progressDialog;
    private String dogImageUrl;

    private boolean dataLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detailbreed);

        imageDog = findViewById(R.id.ImageDog);
        nameDog = findViewById(R.id.NameDog);
        tvBreedFor = findViewById(R.id.tvBreedFor);
        tvBreedGroup = findViewById(R.id.tvBreedGroup);
        tvLifeSpan = findViewById(R.id.tvLifeSpan);
        tvTemperament = findViewById(R.id.tvTemperament);
        tvOrigin = findViewById(R.id.tvOrigin);
        tvHeight = findViewById(R.id.tvHeight);
        tvWeight = findViewById(R.id.tvWeight);
        String referenceId = getIntent().getStringExtra("reference_id");

        if (referenceId != null && !referenceId.isEmpty()) {
            obtainDataBreeds(referenceId);
        } else {
            Toast.makeText(this, "No se pudo obtener la información de la raza", Toast.LENGTH_SHORT).show();
        }

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);



    }

    private void obtainDataBreeds(String referenceImageID) {
        String url = "https://api.thedogapi.com/v1/images/" + referenceImageID;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET,
                url,
                null,
                response -> {
            try {
                JSONArray breedsArray = response.getJSONArray("breeds");
                if (breedsArray.length()>0){
                    JSONObject breedInfo = breedsArray.getJSONObject(0);

                    String name = breedInfo.getString("name");
                    String bredFor = breedInfo.optString("bred_for", "N/A");
                    String breedGroup = breedInfo.optString("breed_group", "N/A");
                    String lifeSpan = breedInfo.optString("life_span", "N/A");
                    String temperament = breedInfo.optString("temperament", "N/A");
                    String origin = breedInfo.optString("origin" , "N/A");
                    String weight = breedInfo.getJSONObject("weight").getString("metric");
                    String height = breedInfo.getJSONObject("height").getString("metric");


                    nameDog.setText(name);
                    tvBreedFor.setText(bredFor);
                    tvBreedGroup.setText(breedGroup);
                    tvLifeSpan.setText(lifeSpan);
                    tvTemperament.setText(temperament);
                    tvOrigin.setText(origin);
                    tvWeight.setText(weight + " Kg");
                    tvHeight.setText(height+ " cm");

                    String imageUrl = response.getString("url");
                    Glide.with(this).load(imageUrl).into(imageDog);

                }
            }catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "Error al procesar la información", Toast.LENGTH_SHORT).show();
            }
                },
                error -> {
            error.printStackTrace();
                    Toast.makeText(this, "Error al obtener datos de la API", Toast.LENGTH_SHORT).show();
                }
        );
        Volley.newRequestQueue(this).add(request);
    }
}
