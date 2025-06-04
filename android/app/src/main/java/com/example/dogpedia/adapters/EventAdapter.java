package com.example.dogpedia.adapters;

import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dogpedia.utils.OnEventDeleteListener;
import com.example.dogpedia.R;
import com.example.dogpedia.models.VetEvent;

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
                    .setTitle("Delete event")
                    .setMessage("Are you sure you want to delete this event?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        deleteListener.onEventDelete(event.getId(), event.getDate());
                    })
                    .setNegativeButton("No", null)
                    .show();
        });


    }

    private String getReadableType(String type) {
        switch (type) {
            case "vaccination":
                return "Vaccination";
            case "deworming":
                return "Deworming";
            case "visit":
                return "Visit to the vet";
            default:
                return "Other";
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
