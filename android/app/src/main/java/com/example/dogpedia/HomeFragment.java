package com.example.dogpedia;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.ArrayList;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private DogBreedAdapter adapter;
    private ArrayList<Breed> dogBreed = new ArrayList<>();

    private RequestQueue queue;

    private static final int NUMBER_OF_DOGS = 6; // Límite de razas que se ven.

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        adapter = new DogBreedAdapter(getContext(),dogBreed);
        adapter.setOnItemClickListener(breed -> {
            //definimos lo que hacemos cuando se haga clic en una raza de perro

        });

        recyclerView.setAdapter(adapter);
        queue = Volley.newRequestQueue(getContext());

        loadBreedDogs();

        return view;
    }

    private void loadBreedDogs() {
        String url = "https://api.thedogapi.com/v1/breeds";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET,
                url,
                null,
                response -> {
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject breedJson = response.getJSONObject(i);
                            String name = breedJson.getString("name");
                            String referenceId = null;
                            String imageUrl = null;
                            if (breedJson.has("image") && breedJson.getJSONObject("image").has("url")) {
                                imageUrl = breedJson.getJSONObject("image").getString("url");
                            } else if (breedJson.has("reference_image_id") && !breedJson.isNull("reference_image_id")) {
                                referenceId = breedJson.getString("reference_image_id");
                                imageUrl = "https://cdn2.thedogapi.com/images/" + referenceId + ".jpg";
                            }

                            if (imageUrl != null && !imageUrl.isEmpty()) {
                                dogBreed.add(new Breed(name, imageUrl, referenceId));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                    }
                    adapter.notifyDataSetChanged();
                },
                error -> error.printStackTrace()
        );
        queue.add(request);
    }
}
