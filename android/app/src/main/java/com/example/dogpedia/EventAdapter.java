package com.example.dogpedia;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final List<VetEvent> eventList;
    private OnEventDeleteListener deleteListener;

    public EventAdapter(List<VetEvent> eventList,OnEventDeleteListener deleteListener) {
        this.deleteListener = deleteListener;
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
        holder.deleteButton.setOnClickListener(v -> {
            new AlertDialog.Builder(v.getContext())
                    .setTitle("Eliminar Evento")
                    .setMessage("¿Estás seguro de que quieres eliminar este evento?")
                    .setPositiveButton("Sí", (dialog, which) -> {
                        deleteListener.onEventDelete(event.getId(), event.getDate());
                    })
                    .setNegativeButton("No", null)
                    .show();
        });


    }

    private String getReadableType(String type) {
        switch (type) {
            case "vacunación":
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
        ImageButton deleteButton;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.eventTitle);
            dateText = itemView.findViewById(R.id.eventDate);
            typeText = itemView.findViewById(R.id.eventType);
            noteText = itemView.findViewById(R.id.eventNote);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }
}
