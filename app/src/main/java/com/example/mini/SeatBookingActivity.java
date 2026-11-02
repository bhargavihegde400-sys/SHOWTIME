package com.example.mini;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class SeatBookingActivity extends AppCompatActivity {

    private int showtimeId;
    private double eventPrice;
    private String eventTitle, showtimeText, userEmail;
    private DatabaseHelper dbHelper;
    private List<SeatModel> seatList;
    private SeatAdapter seatAdapter;
    private TextView tvSelectedSeatsInfo, tvTotalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_booking);

        showtimeId = getIntent().getIntExtra("showtime_id", -1);
        eventPrice = getIntent().getDoubleExtra("event_price", 250.0);
        eventTitle = getIntent().getStringExtra("event_title");
        showtimeText = getIntent().getStringExtra("showtime_text");
        userEmail = getIntent().getStringExtra("user_email");
        if (userEmail == null || userEmail.isEmpty()) {
            userEmail = "user@showtime.com";
        }

        dbHelper = new DatabaseHelper(this);

        GridView gridViewSeats = findViewById(R.id.gridViewSeats);
        tvSelectedSeatsInfo = findViewById(R.id.tvSelectedSeatsInfo);
        tvTotalPrice = findViewById(R.id.tvTotalPrice);
        Button btnProceedPayment = findViewById(R.id.btnProceedPayment);
        ImageView btnBackSeat = findViewById(R.id.btnBackSeat);

        btnBackSeat.setOnClickListener(v -> finish());

        seatList = new ArrayList<>();
        loadSeats();

        seatAdapter = new SeatAdapter();
        gridViewSeats.setAdapter(seatAdapter);

        btnProceedPayment.setOnClickListener(v -> {
            List<String> selectedSeats = new ArrayList<>();
            for (SeatModel seat : seatList) {
                if (seat.isSelected()) {
                    selectedSeats.add(seat.getSeatNumber());
                }
            }

            if (selectedSeats.isEmpty()) {
                Toast.makeText(SeatBookingActivity.this, "Please select at least one seat", Toast.LENGTH_SHORT).show();
                return;
            }

            double totalFare = selectedSeats.size() * eventPrice;
            String seatsString = String.join(", ", selectedSeats);

            Intent intent = new Intent(SeatBookingActivity.this, PaymentActivity.class);
            intent.putExtra("event_title", eventTitle);
            intent.putExtra("showtime_text", showtimeText);
            intent.putExtra("user_email", userEmail);
            intent.putExtra("seats", seatsString);
            intent.putExtra("total_fare", totalFare);
            intent.putExtra("showtime_id", showtimeId);
            intent.putStringArrayListExtra("selected_seats_list", (ArrayList<String>) selectedSeats);
            startActivity(intent);
        });
    }

    private void loadSeats() {
        seatList.clear();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(DatabaseHelper.TABLE_SEATS, null, "showtime_id=?", new String[]{String.valueOf(showtimeId)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String seatNumber = cursor.getString(cursor.getColumnIndexOrThrow("seat_number"));
                String status = cursor.getString(cursor.getColumnIndexOrThrow("status"));
                seatList.add(new SeatModel(id, seatNumber, status));
            } while (cursor.moveToNext());
            cursor.close();
        }
    }

    private void updateSummary() {
        List<String> selected = new ArrayList<>();
        for (SeatModel s : seatList) {
            if (s.isSelected()) selected.add(s.getSeatNumber());
        }
        if (selected.isEmpty()) {
            tvSelectedSeatsInfo.setText("Selected: None");
            tvTotalPrice.setText("₹0");
        } else {
            tvSelectedSeatsInfo.setText("Selected: " + String.join(", ", selected));
            tvTotalPrice.setText("₹" + (selected.size() * eventPrice));
        }
    }

    public static class SeatModel {
        private final int id;
        private final String seatNumber;
        private final String status;
        private boolean isSelected = false;

        public SeatModel(int id, String seatNumber, String status) {
            this.id = id;
            this.seatNumber = seatNumber;
            this.status = status;
        }

        public String getSeatNumber() { return seatNumber; }
        public String getStatus() { return status; }
        public boolean isSelected() { return isSelected; }
        public void setSelected(boolean selected) { isSelected = selected; }
    }

    private class SeatAdapter extends BaseAdapter {
        @Override
        public int getCount() { return seatList.size(); }

        @Override
        public Object getItem(int position) { return seatList.get(position); }

        @Override
        public long getItemId(int position) { return position; }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TextView view;
            if (convertView == null) {
                view = new TextView(SeatBookingActivity.this);
                view.setLayoutParams(new GridView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 120));
                view.setGravity(android.view.Gravity.CENTER);
                view.setTextSize(14f);
                view.setTypeface(null, android.graphics.Typeface.BOLD);
            } else {
                view = (TextView) convertView;
            }

            SeatModel seat = seatList.get(position);
            view.setText(seat.getSeatNumber());

            if (seat.getStatus().equals("BOOKED")) {
                view.setBackgroundColor(getResources().getColor(R.color.seat_booked));
                view.setTextColor(getResources().getColor(R.color.white));
                view.setEnabled(false);
            } else if (seat.isSelected()) {
                view.setBackgroundColor(getResources().getColor(R.color.seat_selected));
                view.setTextColor(getResources().getColor(R.color.white));
                view.setEnabled(true);
            } else {
                view.setBackgroundColor(getResources().getColor(R.color.seat_available));
                view.setTextColor(getResources().getColor(R.color.white));
                view.setEnabled(true);
            }

            view.setOnClickListener(v -> {
                if (seat.getStatus().equals("BOOKED")) return;
                seat.setSelected(!seat.isSelected());
                notifyDataSetChanged();
                updateSummary();
            });

            return view;
        }
    }
}
