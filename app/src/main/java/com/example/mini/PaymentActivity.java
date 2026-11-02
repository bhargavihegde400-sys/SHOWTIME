package com.example.mini;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PaymentActivity extends AppCompatActivity {

    private String eventTitle, showtimeText, userEmail, seats;
    private double totalFare;
    private int showtimeId;
    private ArrayList<String> selectedSeatsList;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        eventTitle = getIntent().getStringExtra("event_title");
        showtimeText = getIntent().getStringExtra("showtime_text");
        userEmail = getIntent().getStringExtra("user_email");
        seats = getIntent().getStringExtra("seats");
        totalFare = getIntent().getDoubleExtra("total_fare", 0.0);
        showtimeId = getIntent().getIntExtra("showtime_id", -1);
        selectedSeatsList = getIntent().getStringArrayListExtra("selected_seats_list");

        if (userEmail == null || userEmail.isEmpty()) {
            userEmail = "user@showtime.com";
        }
        if (eventTitle == null) eventTitle = "ShowTime Event";
        if (showtimeText == null) showtimeText = "2026-11-01 | 10:00 AM";
        if (seats == null) seats = "A1";

        dbHelper = new DatabaseHelper(this);

        TextView tvPaymentSummary = findViewById(R.id.tvPaymentSummary);
        tvPaymentSummary.setText("Total Amount: ₹" + totalFare);

        RadioButton rbCard = findViewById(R.id.rbCard);
        RadioButton rbNetBanking = findViewById(R.id.rbNetBanking);
        RadioButton rbWallet = findViewById(R.id.rbWallet);
        Button btnPayNow = findViewById(R.id.btnPayNow);
        ImageView btnBackPayment = findViewById(R.id.btnBackPayment);

        btnBackPayment.setOnClickListener(v -> finish());

        btnPayNow.setOnClickListener(v -> {
            try {
                String paymentMethod = "UPI";
                if (rbCard.isChecked()) paymentMethod = "Credit/Debit Card";
                else if (rbNetBanking.isChecked()) paymentMethod = "Net Banking";
                else if (rbWallet.isChecked()) paymentMethod = "Wallet";

                SQLiteDatabase db = dbHelper.getWritableDatabase();
                String bookingDate = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());
                String qrCodeData = "SHOWTIME-" + System.currentTimeMillis() + "-" + userEmail.hashCode();

                ContentValues cv = new ContentValues();
                cv.put("user_email", userEmail);
                cv.put("event_title", eventTitle);
                cv.put("showtime", showtimeText);
                cv.put("seats", seats);
                cv.put("total_fare", totalFare);
                cv.put("payment_method", paymentMethod);
                cv.put("booking_date", bookingDate);
                cv.put("qr_code", qrCodeData);

                long bookingId = db.insert(DatabaseHelper.TABLE_BOOKINGS, null, cv);

                if (bookingId == -1) {
                    Toast.makeText(PaymentActivity.this, "Database Error: Failed to save booking", Toast.LENGTH_LONG).show();
                    return;
                }

                if (selectedSeatsList != null) {
                    for (String seatNum : selectedSeatsList) {
                        ContentValues seatCv = new ContentValues();
                        seatCv.put("status", "BOOKED");
                        db.update(DatabaseHelper.TABLE_SEATS, seatCv, "showtime_id=? AND seat_number=?", new String[]{String.valueOf(showtimeId), seatNum});
                    }
                }

                Toast.makeText(PaymentActivity.this, "Payment Verified & Confirmed!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(PaymentActivity.this, TicketActivity.class);
                intent.putExtra("booking_id", (int) bookingId);
                intent.putExtra("event_title", eventTitle);
                intent.putExtra("showtime", showtimeText);
                intent.putExtra("seats", seats);
                intent.putExtra("total_fare", totalFare);
                intent.putExtra("payment_method", paymentMethod);
                intent.putExtra("booking_date", bookingDate);
                intent.putExtra("qr_code", qrCodeData);
                intent.putExtra("user_email", userEmail);
                startActivity(intent);
                finishAffinity();

            } catch (Exception e) {
                Log.e("PaymentActivity", "Payment processing error", e);
                Toast.makeText(PaymentActivity.this, "Error processing payment: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
