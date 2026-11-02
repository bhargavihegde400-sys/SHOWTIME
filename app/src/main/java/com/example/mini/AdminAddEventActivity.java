package com.example.mini;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class AdminAddEventActivity extends AppCompatActivity {

    private EditText etTitle, etCategory, etVenue, etPrice, etDescription, etCity, etGenre, etSeatTier;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_event);

        dbHelper = new DatabaseHelper(this);

        etTitle = findViewById(R.id.etAdminTitle);
        etCategory = findViewById(R.id.etAdminCategory);
        etVenue = findViewById(R.id.etAdminVenue);
        etPrice = findViewById(R.id.etAdminPrice);
        etDescription = findViewById(R.id.etAdminDescription);
        etCity = findViewById(R.id.etAdminCity);
        etGenre = findViewById(R.id.etAdminGenre);
        etSeatTier = findViewById(R.id.etAdminSeatTier);
        Button btnSaveEvent = findViewById(R.id.btnSaveEvent);

        btnSaveEvent.setOnClickListener(v -> {
            String title = etTitle.getText().toString().trim();
            String category = etCategory.getText().toString().trim();
            String venue = etVenue.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String city = etCity.getText().toString().trim();
            String genre = etGenre.getText().toString().trim();
            String seatTier = etSeatTier.getText().toString().trim();

            if (title.isEmpty() || category.isEmpty() || venue.isEmpty() || priceStr.isEmpty()) {
                Toast.makeText(AdminAddEventActivity.this, "Please fill required fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (city.isEmpty()) city = "Noida";
            if (genre.isEmpty()) genre = "Action";
            if (seatTier.isEmpty()) seatTier = "VIP";

            double price = Double.parseDouble(priceStr);

            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues cv = new ContentValues();
            cv.put("title", title);
            cv.put("category", category);
            cv.put("venue", venue);
            cv.put("price", price);
            cv.put("description", description);
            cv.put("city", city);
            cv.put("genre", genre);
            cv.put("seat_tier", seatTier);

            long eventId = db.insert(DatabaseHelper.TABLE_EVENTS, null, cv);

            if (eventId != -1) {
                // Add default showtime and seats
                ContentValues stCv = new ContentValues();
                stCv.put("event_id", eventId);
                stCv.put("date", "2026-11-05");
                stCv.put("time", "07:00 PM");
                long showtimeId = db.insert(DatabaseHelper.TABLE_SHOWTIMES, null, stCv);

                char row = 'A';
                for (int r = 0; r < 3; r++) {
                    for (int s = 1; s <= 6; s++) {
                        ContentValues seatCv = new ContentValues();
                        seatCv.put("showtime_id", showtimeId);
                        seatCv.put("seat_number", "" + (char)(row + r) + s);
                        seatCv.put("status", "AVAILABLE");
                        db.insert(DatabaseHelper.TABLE_SEATS, null, seatCv);
                    }
                }

                Toast.makeText(AdminAddEventActivity.this, "Event added successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(AdminAddEventActivity.this, "Failed to add event", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
