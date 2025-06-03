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
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.format.WeekDayFormatter;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormatSymbols;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.prolificinteractive.materialcalendarview.format.TitleFormatter;


public class CalendarFragment extends Fragment implements OnEventDeleteListener {
    private MaterialCalendarView calendarView;
    private RecyclerView eventsRecyclerView;
    private FloatingActionButton addEventButton;
    private EventAdapter eventAdapter;
    private List<VetEvent> eventList = new ArrayList<>();
    private String sessionToken;

    private static final String URL = "http://10.0.2.2:8000/vetevent/";
    private static final String DATES_URL = "http://10.0.2.2:8000/vetevent/dates/";
    private final Set<CalendarDay> markedDates = new HashSet<>();

    @Override
    public void onEventDelete(int eventId, String date) {
        deleteEvent(eventId, date);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_calendar, container, false);

        calendarView = view.findViewById(R.id.materialCalendarView);
        eventsRecyclerView = view.findViewById(R.id.eventsRV);
        addEventButton = view.findViewById(R.id.AddEvent);

        //Asignamos formateadores para que el calendario esté en inglés
        calendarView.setTitleFormatter(new EnglishTitleFormatter());
        calendarView.setWeekDayFormatter(new EnglishWeekDayFormatter());

        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        eventAdapter = new EventAdapter(eventList, this);
        eventsRecyclerView.setAdapter(eventAdapter);


        addEventButton.setOnClickListener(v -> showAddEventDialog());

        sessionToken = getSessionToken();

        loadMarkedDates();  // carga fechas marcadas con círculos
        loadEvents(LocalDate.now().toString());

        calendarView.setOnDateChangedListener((widget, date, selected) -> {
            LocalDate selectedDate = LocalDate.of(date.getYear(), date.getMonth() + 1, date.getDay());
            loadEvents(selectedDate.toString());
        });

        return view;
    }

    // Formatter para título (Mes Año) en inglés
    private static class EnglishTitleFormatter implements TitleFormatter {
        @Override
        public CharSequence format(CalendarDay day) {
            String month = new DateFormatSymbols(Locale.ENGLISH).getMonths()[day.getMonth()];
            int year = day.getYear();
            return month + " " + year;
        }
    }

    // Formatter para días de la semana abreviados en inglés
    private static class EnglishWeekDayFormatter implements WeekDayFormatter {
        @Override
        public CharSequence format(int dayOfWeek) {
            return new DateFormatSymbols(Locale.ENGLISH).getShortWeekdays()[dayOfWeek];
        }
    }

    private void loadMarkedDates() {
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, DATES_URL, null,
                response -> {
                    markedDates.clear();
                    for (int i = 0; i < response.length(); i++) {
                        String dateStr = null;
                        try {
                            dateStr = response.getString(i);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        LocalDate date = LocalDate.parse(dateStr);
                        CalendarDay day = CalendarDay.from(date.getYear(), date.getMonthValue() - 1, date.getDayOfMonth());
                        markedDates.add(day);
                    }
                    calendarView.removeDecorators();
                    calendarView.addDecorator(new BorderDecorator(markedDates, getResources().getColor(R.color.primary_blue)));
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

    private void loadEvents(String date) {
        eventList.clear();
        String url = URL + "?date=" + date;

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    if (response.length() == 0) {
                        android.util.Log.d("CalendarFragment", "Empty response for date: " + date);
                    } else {
                        android.util.Log.d("CalendarFragment", "Events received for date: " + date + ", amount : " + response.length());
                    }
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            int id = obj.getInt("id");
                            String type = obj.getString("type");
                            String title = obj.getString("title");
                            String description = obj.getString("description");
                            String eventDate = obj.getString("date");

                            eventList.add(new VetEvent(title, description, eventDate, type, id));

                        } catch (JSONException e) {
                            e.printStackTrace();
                            android.util.Log.e("CalendarFragment", "Error parsing JSON: " + e.getMessage());
                        }
                    }
                    eventAdapter.notifyDataSetChanged();
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(getContext(), "Error cargando eventos: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    android.util.Log.e("CalendarFragment", "Error in request volley: " + error.toString());
                }
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

    public void deleteEvent(int eventId, String date) {
        String deleteUrl = URL + eventId + "/";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.DELETE, deleteUrl, null,
                response -> {
                    loadEvents(date);
                    loadMarkedDates();
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
        EditText noteInput = dialogView.findViewById(R.id.editNote);
        Button dateButton = dialogView.findViewById(R.id.btnPickDate);

        final String[] selectedDate = {LocalDate.now().toString()};
        dateButton.setText(selectedDate[0]);

        dateButton.setOnClickListener(v -> {
            MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Select date").build();
            datePicker.show(getParentFragmentManager(), "DATE_PICKER");
            datePicker.addOnPositiveButtonClickListener(selection -> {
                selectedDate[0] = Instant.ofEpochMilli(selection).atZone(ZoneId.systemDefault()).toLocalDate().toString();
                dateButton.setText(selectedDate[0]);
            });
        });

        new AlertDialog.Builder(getContext())
                .setTitle("Add Veterinary Event")
                .setView(dialogView)
                .setPositiveButton("Save event", (dialog, which) -> {
                    String eventType = typeSpinner.getSelectedItem().toString().toLowerCase();
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
                            response -> {
                                loadEvents(selectedDate[0]);
                                loadMarkedDates();
                            },
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
                .setNegativeButton("Cancel", null)
                .show();
    }

    private String getSessionToken() {
        SharedPreferences prefs = requireContext().getSharedPreferences("DOGPEDIA_APP_PREFS", Context.MODE_PRIVATE);
        return prefs.getString("SessionToken", null);
    }

}

