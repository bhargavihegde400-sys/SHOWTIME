package com.example.mini;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class EventDetailActivity extends AppCompatActivity {

    private int eventId;
    private String userEmail;
    private DatabaseHelper dbHelper;
    private Spinner spinnerShowtimes;
    private List<Integer> showtimeIds;
    private List<String> showtimeStrings;
    private double eventPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_detail);

        eventId = getIntent().getIntExtra("event_id", -1);
        userEmail = getIntent().getStringExtra("user_email");
        if (userEmail == null || userEmail.isEmpty()) {
            userEmail = "user@showtime.com";
        }
        dbHelper = new DatabaseHelper(this);

        TextView tvTitle = findViewById(R.id.tvDetailTitle);
        TextView tvCategory = findViewById(R.id.tvDetailCategory);
        TextView tvVenue = findViewById(R.id.tvDetailVenue);
        TextView tvDescription = findViewById(R.id.tvDetailDescription);
        TextView tvPrice = findViewById(R.id.tvDetailPrice);
        spinnerShowtimes = findViewById(R.id.spinnerShowtimes);
        Button btnProceedSeats = findViewById(R.id.btnProceedSeats);
        ImageView btnBackDetail = findViewById(R.id.btnBackDetail);

        btnBackDetail.setOnClickListener(v -> finish());

        showtimeIds = new ArrayList<>();
        showtimeStrings = new ArrayList<>();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_EVENTS, null, "id=?", new String[]{String.valueOf(eventId)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
            String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
            String venue = cursor.getString(cursor.getColumnIndexOrThrow("venue"));
            String description = cursor.getString(cursor.getColumnIndexOrThrow("description"));
            eventPrice = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));
            cursor.close();

            tvTitle.setText(title);
            tvCategory.setText(category);
            tvVenue.setText("Venue: " + venue);
            tvDescription.setText(description);
            tvPrice.setText("Price: ₹" + eventPrice);
        }

        Cursor stCursor = db.query(DatabaseHelper.TABLE_SHOWTIMES, null, "event_id=?", new String[]{String.valueOf(eventId)}, null, null, null);
        if (stCursor != null && stCursor.moveToFirst()) {
            do {
                int stId = stCursor.getInt(stCursor.getColumnIndexOrThrow("id"));
                String date = stCursor.getString(stCursor.getColumnIndexOrThrow("date"));
                String time = stCursor.getString(stCursor.getColumnIndexOrThrow("time"));
                showtimeIds.add(stId);
                showtimeStrings.add(date + " | " + time);
            } while (stCursor.moveToNext());
            stCursor.close();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, showtimeStrings);
        spinnerShowtimes.setAdapter(adapter);

        btnProceedSeats.setOnClickListener(v -> {
            if (showtimeIds.isEmpty()) {
                return;
            }
            int selectedPosition = spinnerShowtimes.getSelectedItemPosition();
            int selectedShowtimeId = showtimeIds.get(selectedPosition);
            String selectedShowtimeText = showtimeStrings.get(selectedPosition);

            Intent intent = new Intent(EventDetailActivity.this, SeatBookingActivity.class);
            intent.putExtra("event_id", eventId);
            intent.putExtra("showtime_id", selectedShowtimeId);
            intent.putExtra("showtime_text", selectedShowtimeText);
            intent.putExtra("user_email", userEmail);
            intent.putExtra("event_price", eventPrice);
            intent.putExtra("event_title", tvTitle.getText().toString());
            startActivity(intent);
        });
    }
}
