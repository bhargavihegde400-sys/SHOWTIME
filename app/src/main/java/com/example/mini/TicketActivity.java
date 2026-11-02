package com.example.mini;

import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Locale;

public class TicketActivity extends AppCompatActivity {

    private String eventTitle, showtime, seats, paymentMethod, qrCode, userEmail;
    private int bookingId;
    private double totalFare;
    private Bitmap qrBitmap;
    private Uri lastPdfUri; // saved so Share button can reuse the same file

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ticket);

        bookingId    = getIntent().getIntExtra("booking_id", -1);
        eventTitle   = getIntent().getStringExtra("event_title");
        showtime     = getIntent().getStringExtra("showtime");
        seats        = getIntent().getStringExtra("seats");
        totalFare    = getIntent().getDoubleExtra("total_fare", 0.0);
        paymentMethod= getIntent().getStringExtra("payment_method");
        qrCode       = getIntent().getStringExtra("qr_code");
        userEmail    = getIntent().getStringExtra("user_email");

        TextView tvTicketTitle   = findViewById(R.id.tvTicketTitle);
        TextView tvTicketShowtime= findViewById(R.id.tvTicketShowtime);
        TextView tvTicketSeats   = findViewById(R.id.tvTicketSeats);
        TextView tvTicketFare    = findViewById(R.id.tvTicketFare);
        TextView tvTicketPayment = findViewById(R.id.tvTicketPayment);
        TextView tvTicketId      = findViewById(R.id.tvTicketId);
        TextView tvQrString      = findViewById(R.id.tvQrString);
        ImageView ivQrCode       = findViewById(R.id.ivQrCode);

        Button btnDownload  = findViewById(R.id.btnDownload);
        Button btnShare     = findViewById(R.id.btnShare);
        Button btnBackHome  = findViewById(R.id.btnBackHome);

        tvTicketTitle.setText(eventTitle);
        tvTicketShowtime.setText(showtime);
        tvTicketSeats.setText(seats);
        tvTicketFare.setText(String.format(Locale.getDefault(), "₹%.2f", totalFare));
        tvTicketPayment.setText(paymentMethod);
        tvTicketId.setText(String.format(Locale.getDefault(), "#%d", bookingId));
        tvQrString.setText(qrCode);

        // ✅ Real ZXing QR instead of the fake pattern
        qrBitmap = generateQrBitmap(qrCode);
        ivQrCode.setImageBitmap(qrBitmap);

        // ✅ Download: builds a real PDF and saves to Downloads
        btnDownload.setOnClickListener(v -> {
            boolean ok = exportTicketPdf();
            if (ok) Toast.makeText(this, "Ticket PDF saved to Downloads", Toast.LENGTH_LONG).show();
        });

        // ✅ Share: sends the actual PDF file (not just text)
        btnShare.setOnClickListener(v -> shareTicketPdf());

        btnBackHome.setOnClickListener(v -> {
            Intent intent = new Intent(TicketActivity.this, UserHomeActivity.class);
            intent.putExtra("user_email", userEmail);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finishAffinity();
        });
    }

    /** Generates a real QR bitmap using ZXing. */
    private Bitmap generateQrBitmap(String data) {
        try {
            BitMatrix m = new MultiFormatWriter().encode(data, BarcodeFormat.QR_CODE, 500, 500);
            Bitmap bmp = Bitmap.createBitmap(500, 500, Bitmap.Config.RGB_565);
            for (int x = 0; x < 500; x++)
                for (int y = 0; y < 500; y++)
                    bmp.setPixel(x, y, m.get(x, y) ? Color.BLACK : Color.WHITE);
            return bmp;
        } catch (Exception e) {
            return null;
        }
    }

    /** Builds a PDF and writes it to Downloads. Returns true on success. */
    private boolean exportTicketPdf() {
        PdfDocument doc = new PdfDocument();
        PdfDocument.PageInfo info = new PdfDocument.PageInfo.Builder(320, 600, 1).create();
        PdfDocument.Page page = doc.startPage(info);
        Canvas cv = page.getCanvas();
        Paint p = new Paint();
        p.setColor(Color.BLACK);
        cv.drawColor(Color.WHITE);

        int y = 40;
        p.setTextSize(22); p.setFakeBoldText(true); p.setTextAlign(Paint.Align.CENTER);
        cv.drawText(eventTitle, 160, y, p); y += 30;
        p.setTextSize(13); p.setFakeBoldText(false); p.setColor(Color.DKGRAY);
        cv.drawText(showtime, 160, y, p); y += 18;
        cv.drawText("Seats: " + seats, 160, y, p); y += 18;
        cv.drawText("Fare: ₹" + totalFare + "  •  " + paymentMethod, 160, y, p); y += 26;
        p.setColor(Color.BLACK); p.setTextSize(14);
        cv.drawText("Booking ID: #" + bookingId, 160, y, p); y += 22;
        if (qrBitmap != null) {
            Bitmap qr = Bitmap.createScaledBitmap(qrBitmap, 180, 180, true);
            cv.drawBitmap(qr, (320 - 180) / 2f, y, p);
            y += 200;
        }
        p.setTextSize(11); p.setColor(Color.GRAY);
        cv.drawText("Show this QR at the venue", 160, y, p);

        doc.finishPage(page);

        String fileName = "ShowTime_Ticket_" + bookingId + ".pdf";
        try {
            OutputStream os;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues cv2 = new ContentValues();
                cv2.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                cv2.put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf");
                cv2.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                Uri u = getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv2);
                os = getContentResolver().openOutputStream(u);
                lastPdfUri = u;
            } else {
                File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File file = new File(dir, fileName);
                os = new FileOutputStream(file);
                lastPdfUri = FileProvider.getUriForFile(this,
                        getPackageName() + ".fileprovider", file);
            }
            doc.writeTo(os);
            if (os != null) os.close();
            doc.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            doc.close();
            Toast.makeText(this, "PDF export failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            return false;
        }
    }

    /** Shares the generated PDF via ACTION_SEND (WhatsApp, Gmail, etc.). */
    private void shareTicketPdf() {
        // Make sure a file exists first
        if (lastPdfUri == null) {
            boolean ok = exportTicketPdf();
            if (!ok) return;
        }
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("application/pdf");
        share.putExtra(Intent.EXTRA_STREAM, lastPdfUri);
        share.putExtra(Intent.EXTRA_SUBJECT, "My ShowTime Ticket #" + bookingId);
        share.putExtra(Intent.EXTRA_TEXT,
                "Ticket for " + eventTitle + " (" + showtime + ")\nSeats: " + seats);
        share.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(share, "Share Ticket via"));
    }
}