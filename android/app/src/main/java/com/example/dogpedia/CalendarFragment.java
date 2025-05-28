package com.example.dogpedia;


import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CalendarFragment extends Fragment {
    private MaterialCalendarView calendarView;
    private RecyclerView eventsRecyclerView;
    private FloatingActionButton addEventButton;
    private EventAdapter eventAdapter;
    private List<VetEvent> eventList = new ArrayList<>();
    private String sessionToken;

    private static final String URL = "http://10.0.2.2:8000/vetevent/";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = view.findViewById(R.id.materialCalendarView);
        eventsRecyclerView = view.findViewById(R.id.eventsRV);
        addEventButton = view.findViewById(R.id.AddEvent);

        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventAdapter = new EventAdapter(eventList);
        eventsRecyclerView.setAdapter(eventAdapter);

        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            LocalDate selectedDate = LocalDate.of(date.getYear(), date.getMonth() + 1, date.getDay());
            loadEvents(selectedDate.toString());
        });

        addEventButton.setOnClickListener(v -> showAddEventDialog());

        sessionToken = getSessionToken();
        loadEvents(LocalDate.now().toString());

        return view;
    }

    private void loadEvents(String date) {
        eventList.clear();
        String url = URL + "?date=" + date;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            String type = obj.getString("type");
                            String title = type.substring(0,1).toUpperCase() + type.substring(1);
                            String description = obj.getString("description");
                            String eventDate = obj.getString("date");


                            eventList.add(new VetEvent(title, description, eventDate, type));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    eventAdapter.notifyDataSetChanged();
                },
                error -> error.printStackTrace()
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("SessionToken", sessionToken);
                return headers;
            }
        };

        Volley.newRequestQueue(requireContext()).add(request);
    }

    private void showAddEventDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_event, null);
        Spinner typeSpinner = dialogView.findViewById(R.id.spinnerType);
        EditText titleInput = dialogView.findViewById(R.id.editTitle);
        EditText noteInput = dialogView.findViewById(R.id.editNote);
        Button dateButton = dialogView.findViewById(R.id.btnPickDate);

        final String[] selectedDate = {LocalDate.now().toString()};
        dateButton.setText(selectedDate[0]);

        dateButton.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Seleccionar fecha").build();
            datePicker.show(getParentFragmentManager(), "DATE_PICKER");
            datePicker.addOnPositiveButtonClickListener(selection -> {
                selectedDate[0] = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate().toString();
                dateButton.setText(selectedDate[0]);
            });
        });

        new AlertDialog.Builder(getContext())
                .setTitle("Agregar Evento Veterinario")
                .setView(dialogView)
                .setPositiveButton("Guardar", (dialog, which) -> {
                    String eventType = typeSpinner.getSelectedItem().toString().toLowerCase();
                    String title = titleInput.getText().toString();
                    String note = noteInput.getText().toString();

                    JSONObject jsonBody = new JSONObject();
                    try {
                        jsonBody.put("type", eventType);
                        jsonBody.put("date", selectedDate[0]);
                        jsonBody.put("description", note);
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }

                    JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL, jsonBody,
                            response -> loadEvents(selectedDate[0]),
                            error -> error.printStackTrace()
                    ) {
                        @Override
                        public Map<String, String> getHeaders() {
                            Map<String, String> headers = new HashMap<>();
                            headers.put("Content-Type", "application/json");
                            headers.put("SessionToken", sessionToken);
                            return headers;
                        }
                    };

                    Volley.newRequestQueue(requireContext()).add(request);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
    private String getSessionToken() {
        SharedPreferences prefs = requireContext().getSharedPreferences("DOGPEDIA_APP_PREFS", Context.MODE_PRIVATE);
        return prefs.getString("SessionToken", null);
    }

}

