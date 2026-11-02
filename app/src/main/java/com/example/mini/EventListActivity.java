package com.example.mini;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EventListActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private List<EventModel> eventList;
    private RecyclerView rvEventList;
    private String userEmail, categoryFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_list);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rvEventList).getRootView(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        userEmail = getIntent().getStringExtra("user_email");
        categoryFilter = getIntent().getStringExtra("category");
        if (userEmail == null) userEmail = "user@showtime.com";
        if (categoryFilter == null) categoryFilter = "All";

        dbHelper = new DatabaseHelper(this);

        TextView tvTitle = findViewById(R.id.tvEventListTitle);
        tvTitle.setText(Objects.equals(categoryFilter, "All") ? "All Events" : categoryFilter + " Events");

        ImageView btnBack = findViewById(R.id.btnBackEventList);
        btnBack.setOnClickListener(v -> finish());

        rvEventList = findViewById(R.id.rvEventList);
        rvEventList.setLayoutManager(new LinearLayoutManager(this));

        eventList = new ArrayList<>();
        loadEvents();
    }

    private void loadEvents() {
        eventList.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor cursor = Objects.equals(categoryFilter, "All") ?
                db.query(DatabaseHelper.TABLE_EVENTS, null, null, null, null, null, null) :
                db.query(DatabaseHelper.TABLE_EVENTS, null, "category=?", new String[]{categoryFilter}, null, null, null)) {
            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                    String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
                    String genre = cursor.getString(cursor.getColumnIndexOrThrow("genre"));
                    String city = cursor.getString(cursor.getColumnIndexOrThrow("city"));
                    String desc = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                    String venue = cursor.getString(cursor.getColumnIndexOrThrow("venue"));
                    double price = cursor.getDouble(cursor.getColumnIndexOrThrow("price"));
                    String seatTier = cursor.getString(cursor.getColumnIndexOrThrow("seat_tier"));

                    eventList.add(new EventModel(id, title, category, genre, city, desc, venue, price, seatTier));
                } while (cursor.moveToNext());
            }
        }

        EventAdapter adapter = new EventAdapter(this, eventList, userEmail);
        rvEventList.setAdapter(adapter);
    }
}
