package com.example.mini;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** Admin Dashboard merged with admin module analytics and management, without date/time editing duplication. */
public class AdminHomeActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    private List<EventModel> eventList;
    private RecyclerView rvAdminEvents;
    private TextView tvStatsSummary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_home);

        dbHelper = new DatabaseHelper(this);
        rvAdminEvents = findViewById(R.id.rvAdminEvents);
        rvAdminEvents.setLayoutManager(new LinearLayoutManager(this));

        tvStatsSummary = findViewById(R.id.tvStatsSummary);

        eventList = new ArrayList<>();
        loadEvents();
        updateStats();

        Button btnAddEvent = findViewById(R.id.btnAddEvent);
        Button btnAdminReports = findViewById(R.id.btnAdminReports);
        Button btnAdminLogout = findViewById(R.id.btnAdminLogout);

        btnAddEvent.setText(R.string.add_new_event);
        btnAddEvent.setOnClickListener(v -> {
            Intent intent = new Intent(AdminHomeActivity.this, AdminAddEventActivity.class);
            startActivity(intent);
        });

        btnAdminReports.setOnClickListener(v -> {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            Cursor c1 = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_EVENTS, null);
            c1.moveToFirst();
            int totalEvents = c1.getInt(0);
            c1.close();

            Cursor c2 = db.rawQuery("SELECT COUNT(*), COALESCE(SUM(total_fare), 0) FROM " + DatabaseHelper.TABLE_BOOKINGS, null);
            c2.moveToFirst();
            int totalBookings = c2.getInt(0);
            double totalRevenue = c2.getDouble(1);
            c2.close();

            View reportView = getLayoutInflater().inflate(R.layout.dialog_admin_report, null);
            TextView tvReportEvents = reportView.findViewById(R.id.tvReportEvents);
            TextView tvReportBookings = reportView.findViewById(R.id.tvReportBookings);
            TextView tvReportRevenue = reportView.findViewById(R.id.tvReportRevenue);
            Button btnReportBack = reportView.findViewById(R.id.btnReportBack);

            tvReportEvents.setText(String.valueOf(totalEvents));
            tvReportBookings.setText(String.valueOf(totalBookings));
            tvReportRevenue.setText(String.format(Locale.getDefault(), "₹%.2f", totalRevenue));

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setView(reportView)
                    .create();

            if (btnReportBack != null) {
                btnReportBack.setOnClickListener(btnView -> dialog.dismiss());
            }
            dialog.show();
        });

        btnAdminLogout.setOnClickListener(v -> {
            startActivity(new Intent(AdminHomeActivity.this, MainActivity.class));
            finishAffinity();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadEvents();
        updateStats();
    }

    private void updateStats() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c1 = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_EVENTS, null);
        c1.moveToFirst();
        int totalEvents = c1.getInt(0);
        c1.close();

        Cursor c2 = db.rawQuery("SELECT COUNT(*) FROM " + DatabaseHelper.TABLE_BOOKINGS, null);
        c2.moveToFirst();
        int totalBookings = c2.getInt(0);
        c2.close();

        if (tvStatsSummary != null) {
            tvStatsSummary.setText(String.format(Locale.getDefault(), "Dashboard: %d Events | %d Bookings", totalEvents, totalBookings));
        }
    }

    private void loadEvents() {
        eventList.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_EVENTS, null, null, null, null, null, null);

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
            cursor.close();
        }

        EventAdapter adapter = new EventAdapter(this, eventList, "admin@showtime.com");
        rvAdminEvents.setAdapter(adapter);
    }
}
