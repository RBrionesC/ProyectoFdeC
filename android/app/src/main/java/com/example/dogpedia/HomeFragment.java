package com.example.dogpedia;


import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.widget.SearchView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    private RecyclerView recyclerView;
    private DogBreedAdapter adapter;
    private ArrayList<Breed> dogBreed = new ArrayList<>();
    private List<Breed> breedList = new ArrayList<>();
    private RequestQueue queue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        Toolbar toolbar = view.findViewById(R.id.toolbar);
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        if (activity != null) {
            activity.setSupportActionBar(toolbar);
            activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
        }



        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);

        int spanCount = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? 3 : 2;
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), spanCount);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new DogBreedAdapter(getContext(),dogBreed);
        adapter.setOnItemClickListener(breed -> {

        });

        recyclerView.setAdapter(adapter);
        queue = Volley.newRequestQueue(getContext());

        loadBreedDogs();

        return view;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    private void loadBreedDogs() {
        String url = "https://api.thedogapi.com/v1/breeds";

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET,
                url,
                null,
                response -> {
                    dogBreed.clear();
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
                    adapter.updateFullList(new ArrayList<>(dogBreed));
                    adapter.notifyDataSetChanged();

                },
                error -> error.printStackTrace()
        );
        queue.add(request);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint("Search breed...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false; // Oculta teclado si quieres
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d("HomeFragment", "Search text changed: " + newText);
                if (adapter != null) {
                    adapter.getFilter().filter(newText);
                }
                return true;
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

}
