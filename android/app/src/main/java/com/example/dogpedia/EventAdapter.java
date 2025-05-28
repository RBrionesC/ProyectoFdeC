package com.example.dogpedia;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final List<VetEvent> eventList;

    public EventAdapter(List<VetEvent> eventList) {
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        VetEvent event = eventList.get(position);
        holder.titleText.setText(event.getTitle());
        holder.dateText.setText(event.getDate());
        holder.typeText.setText(getReadableType(event.getType()));
        holder.noteText.setText(event.getDescription());
    }

    private String getReadableType(String type) {
        switch (type) {
            case "vacunacion":
                return "Vacunación";
            case "desparasitacion":
                return "Desparasitación";
            case "visita":
                return "Visita al veterinario";
            default:
                return "Otro";
        }
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, dateText, typeText, noteText;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.eventTitle);
            dateText = itemView.findViewById(R.id.eventDate);
            typeText = itemView.findViewById(R.id.eventType);
            noteText = itemView.findViewById(R.id.eventNote);
        }
    }
}
