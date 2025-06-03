package com.example.dogpedia;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class DogBreedAdapter extends RecyclerView.Adapter<DogBreedAdapter.BreedViewHolder>implements Filterable {

    private List<Breed> breedList;
    private List<Breed> breedListFull;
    private OnItemClickListener listener;
    private Context context;

    public DogBreedAdapter(Context context,List<Breed> breedList){
        this.context = context;
        this.breedList = breedList;
        this.breedListFull = new ArrayList<>(breedList);

    }

    public interface OnItemClickListener {
        void onItemClick(Breed breed);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.listener = listener;
    }

    @NonNull
    @Override
    public BreedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dog_breed_item, parent, false);
        return new BreedViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BreedViewHolder holder, int position) {
        Breed breed = breedList.get(position);
        holder.textView.setText(breed.getName());

        Glide.with(holder.itemView.getContext())
                .load(breed.getImageUrl())
                //.override(400,200)
                .fitCenter()
                .into(holder.imageView);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DetailBreedActivity.class);
            intent.putExtra("reference_id", breed.getReferenceImageId());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return breedList.size();
    }

    public class BreedViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;
        public BreedViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageViewDog);
            textView = itemView.findViewById(R.id.textViewName);
        }
    }
    @Override
    public Filter getFilter() {
        return breedFilter;
    }

    private Filter breedFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            List<Breed> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(breedListFull);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Breed item : breedListFull) {
                    if (item.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(item);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            breedList.clear();
            breedList.addAll((List) results.values);
            Log.d("DogBreedAdapter", "Filtered list size: " + breedList.size());
            notifyDataSetChanged();
        }
    };

    public void updateFullList(List<Breed> newFullList) {
        breedListFull.clear();
        breedListFull.addAll(newFullList);
    }


}
