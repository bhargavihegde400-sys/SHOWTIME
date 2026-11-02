package com.example.mini;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class BookingHistoryActivity extends AppCompatActivity {

    private String userEmail;
    private DatabaseHelper dbHelper;
    private List<BookingModel> bookingList;
    private BookingAdapter adapter;
    private int currentLimit = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_history);

        userEmail = getIntent().getStringExtra("user_email");
        dbHelper = new DatabaseHelper(this);

        RecyclerView rvBookings = findViewById(R.id.rvBookings);
        rvBookings.setLayoutManager(new LinearLayoutManager(this));

        bookingList = new ArrayList<>();
        adapter = new BookingAdapter(this, bookingList, dbHelper, this::cancelBooking);
        rvBookings.setAdapter(adapter);

        Spinner spinnerLimit = findViewById(R.id.spinnerHistoryLimit);
        List<String> limits = new ArrayList<>();
        limits.add("Last 10");
        limits.add("Last 30");
        limits.add("All");
        ArrayAdapter<String> limitAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, limits);
        spinnerLimit.setAdapter(limitAdapter);

        spinnerLimit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String sel = limits.get(position);
                if (sel.equals("Last 10")) currentLimit = 10;
                else if (sel.equals("Last 30")) currentLimit = 30;
                else currentLimit = 1000;
                loadBookings();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        Button btnClearHistory = findViewById(R.id.btnClearHistory);
        btnClearHistory.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Clear History")
                    .setMessage("Are you sure you want to clear all booking history?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        SQLiteDatabase db = dbHelper.getWritableDatabase();
                        db.delete(DatabaseHelper.TABLE_BOOKINGS, "user_email=?", new String[]{userEmail});
                        loadBookings();
                        Toast.makeText(this, "Booking history cleared", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBookings();  // refresh so status stays in sync if ticket page changed anything
    }

    private void loadBookings() {
        bookingList.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_BOOKINGS, null, "user_email=?", new String[]{userEmail}, null, null, "id DESC", String.valueOf(currentLimit));

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String title = cursor.getString(cursor.getColumnIndexOrThrow("event_title"));
                String showtime = cursor.getString(cursor.getColumnIndexOrThrow("showtime"));
                String seats = cursor.getString(cursor.getColumnIndexOrThrow("seats"));
                double fare = cursor.getDouble(cursor.getColumnIndexOrThrow("total_fare"));
                String date = cursor.getString(cursor.getColumnIndexOrThrow("booking_date"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                bookingList.add(new BookingModel(id, title, showtime, seats, fare, date, status));
            } while (cursor.moveToNext());
            cursor.close();
        }
        adapter.notifyDataSetChanged();
    }


    private void cancelBooking(int bookingId, String seats, String showtime) {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Booking")
                .setMessage("Are you sure you want to cancel this booking?")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> {

                    SQLiteDatabase db = dbHelper.getWritableDatabase();

                    db.beginTransaction();
                    try {
                        // Get booking details before updating status
                        Cursor cursor = db.query(
                                DatabaseHelper.TABLE_BOOKINGS,
                                new String[]{"event_title", "showtime", "seats"},
                                "id=?",
                                new String[]{String.valueOf(bookingId)},
                                null, null, null
                        );

                        if (cursor != null && cursor.moveToFirst()) {

                            String eventTitle = cursor.getString(0);
                            String bookingShowtime = cursor.getString(1);
                            String bookedSeats = cursor.getString(2);

                            cursor.close();

                            // Find the matching event
                            Cursor eventCursor = db.query(
                                    "events",
                                    new String[]{"id"},
                                    "title=?",
                                    new String[]{eventTitle},
                                    null, null, null
                            );

                            if (eventCursor != null && eventCursor.moveToFirst()) {
                                int eventId = eventCursor.getInt(0);
                                eventCursor.close();

                                // Find the matching showtime
                                Cursor showtimeCursor = db.query(
                                        DatabaseHelper.TABLE_SHOWTIMES,
                                        new String[]{"id"},
                                        "event_id=?",
                                        new String[]{String.valueOf(eventId)},
                                        null, null, null
                                );

                                if (showtimeCursor != null) {
                                    while (showtimeCursor.moveToNext()) {
                                        int showtimeId = showtimeCursor.getInt(0);

                                        // Release the cancelled seats
                                        String[] seatArray = bookedSeats.split(",\\s*");

                                        for (String seatNumber : seatArray) {
                                            ContentValues seatValues = new ContentValues();
                                            seatValues.put("status", "AVAILABLE");

                                            db.update(
                                                    DatabaseHelper.TABLE_SEATS,
                                                    seatValues,
                                                    "showtime_id=? AND seat_number=? AND status='BOOKED'",
                                                    new String[]{
                                                            String.valueOf(showtimeId),
                                                            seatNumber.trim()
                                                    }
                                            );
                                        }
                                    }
                                    showtimeCursor.close();
                                }
                            }

                            // Update booking status
                            ContentValues bookingValues = new ContentValues();
                            bookingValues.put("status", "CANCELLED");

                            db.update(
                                    DatabaseHelper.TABLE_BOOKINGS,
                                    bookingValues,
                                    "id=?",
                                    new String[]{String.valueOf(bookingId)}
                            );
                        } else if (cursor != null) {
                            cursor.close();
                        }

                        db.setTransactionSuccessful();

                    } catch (Exception e) {
                        Toast.makeText(this,
                                "Cancellation failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    } finally {
                        db.endTransaction();
                    }

                    Toast.makeText(this,
                            "Booking cancelled successfully!",
                            Toast.LENGTH_SHORT).show();

                    loadBookings();
                })
                .setNegativeButton("No", null)
                .show();
    }


    public static class BookingModel {
        private final int id;
        private final String title, showtime, seats, date, status;
        private final double fare;

        public BookingModel(int id, String title, String showtime, String seats, double fare, String date, String status) {
            this.id = id;
            this.title = title;
            this.showtime = showtime;
            this.seats = seats;
            this.fare = fare;
            this.date = date;
            this.status = status;
        }

        public int getId() { return id; }
        public String getTitle() { return title; }
        public String getShowtime() { return showtime; }
        public String getSeats() { return seats; }
        public double getFare() { return fare; }
        public String getDate() { return date; }
        public String getStatus() { return status; }
    }

    private static class BookingAdapter extends RecyclerView.Adapter<BookingAdapter.BookingViewHolder> {
        private final Context context;
        private final List<BookingModel> list;
        private final CancelCallback cancelCallback;
        private final DatabaseHelper dbHelper;    // ✅ pass through

        public interface CancelCallback {
            void onCancel(int bookingId, String seats, String showtime);
        }

        public BookingAdapter(Context context, List<BookingModel> list,
                              DatabaseHelper dbHelper, CancelCallback cancelCallback) {
            this.context = context;
            this.list = list;
            this.dbHelper = dbHelper;
            this.cancelCallback = cancelCallback;
        }

        @NonNull
        @Override
        public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_booking, parent, false);
            return new BookingViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
            BookingModel b = list.get(position);
            holder.tvTitle.setText(b.getTitle() + " (" + b.getStatus() + ")");
            holder.tvShowtime.setText("Showtime: " + b.getShowtime());
            holder.tvSeats.setText("Seats: " + b.getSeats());
            holder.tvFare.setText("₹" + b.getFare());
            holder.tvDate.setText(b.getDate());

            if (b.getStatus().equals("CANCELLED")) {
                holder.btnCancel.setVisibility(View.GONE);
            } else {
                holder.btnCancel.setVisibility(View.VISIBLE);
                holder.btnCancel.setOnClickListener(v ->
                        cancelCallback.onCancel(b.getId(), b.getSeats(), b.getShowtime()));
            }

            // ✅ NEW: tap the card to open the ticket
            holder.itemView.setOnClickListener(v -> {
                if ("CANCELLED".equals(b.getStatus())) {
                    Toast.makeText(context, "Cancelled bookings can't be opened", Toast.LENGTH_SHORT).show();
                    return;
                }
                Cursor c = dbHelper.getBookingById(b.getId());
                if (c == null || !c.moveToFirst()) {
                    Toast.makeText(context, "Booking not found", Toast.LENGTH_SHORT).show();
                    if (c != null) c.close();
                    return;
                }
                int    id            = c.getInt(c.getColumnIndexOrThrow("id"));
                String userEmail     = c.getString(c.getColumnIndexOrThrow("user_email"));
                String title         = c.getString(c.getColumnIndexOrThrow("event_title"));
                String showtime      = c.getString(c.getColumnIndexOrThrow("showtime"));
                String seats         = c.getString(c.getColumnIndexOrThrow("seats"));
                double fare          = c.getDouble(c.getColumnIndexOrThrow("total_fare"));
                String paymentMethod = c.getString(c.getColumnIndexOrThrow("payment_method"));
                String qrCode        = c.getString(c.getColumnIndexOrThrow("qr_code"));
                c.close();

                Intent intent = new Intent(context, TicketActivity.class);
                intent.putExtra("booking_id", id);
                intent.putExtra("event_title", title);
                intent.putExtra("showtime", showtime);
                intent.putExtra("seats", seats);
                intent.putExtra("total_fare", fare);
                intent.putExtra("payment_method", paymentMethod);
                intent.putExtra("qr_code", qrCode);
                intent.putExtra("user_email", userEmail);
                context.startActivity(intent);
            });
        }

        @Override
        public int getItemCount() { return list.size(); }

        public static class BookingViewHolder extends RecyclerView.ViewHolder {
            TextView tvTitle, tvShowtime, tvSeats, tvFare, tvDate;
            Button btnCancel;

            public BookingViewHolder(@NonNull View itemView) {
                super(itemView);
                tvTitle = itemView.findViewById(R.id.tvBookingEventTitle);
                tvShowtime = itemView.findViewById(R.id.tvBookingShowtime);
                tvSeats = itemView.findViewById(R.id.tvBookingSeats);
                tvFare = itemView.findViewById(R.id.tvBookingFare);
                tvDate = itemView.findViewById(R.id.tvBookingDate);
                btnCancel = itemView.findViewById(R.id.btnCancelBooking);
            }
        }
    }
}