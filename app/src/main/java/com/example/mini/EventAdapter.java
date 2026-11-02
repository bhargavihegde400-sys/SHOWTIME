package com.example.mini;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Objects;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private final Context context;
    private final List<EventModel> eventList;
    private final String userEmail;

    public EventAdapter(Context context, List<EventModel> eventList, String userEmail) {
        this.context = context;
        this.eventList = eventList;
        this.userEmail = userEmail;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        EventModel event = eventList.get(position);
        holder.tvTitle.setText(event.getTitle());
        holder.tvCategory.setText(event.getCategory());
        holder.tvCityGenre.setText(event.getCity() + " | " + event.getGenre());
        holder.tvVenue.setText("Venue: " + event.getVenue());
        holder.tvPrice.setText("₹" + event.getPrice() + " (" + event.getSeatTier() + ")");

        // Set image based on category
        String cat = event.getCategory();
        if (cat.equalsIgnoreCase("Movies")) {
            holder.ivEventImage.setImageResource(R.drawable.img_movie);
        } else if (cat.equalsIgnoreCase("Music")) {
            holder.ivEventImage.setImageResource(R.drawable.img_concert);
        } else if (cat.equalsIgnoreCase("Comedy")) {
            holder.ivEventImage.setImageResource(R.drawable.img_comedy);
        } else if (cat.equalsIgnoreCase("Plays")) {
            holder.ivEventImage.setImageResource(R.drawable.img_play);
        } else if (cat.equalsIgnoreCase("Workshops")) {
            holder.ivEventImage.setImageResource(R.drawable.img_workshop);
        } else {
            holder.ivEventImage.setImageResource(R.drawable.img_cultural);
        }

        boolean isAdmin = Objects.equals(userEmail, "admin@showtime.com");

        if (isAdmin) {
            holder.btnDeleteEvent.setVisibility(View.VISIBLE);
            holder.btnDeleteEvent.setOnClickListener(v -> {
                int pos = holder.getBindingAdapterPosition();
                if (pos == RecyclerView.NO_POSITION) return;
                new AlertDialog.Builder(context)
                        .setTitle("Confirm Deletion")
                        .setMessage("Are you sure you want to delete event '" + event.getTitle() + "'? This will permanently delete the event from the database.")
                        .setPositiveButton("Confirm", (dialog, which) -> {
                            try (DatabaseHelper dbHelper = new DatabaseHelper(context)) {
                                boolean success = dbHelper.deleteEventCompletely(event.getId());
                                if (success) {
                                    Toast.makeText(context, "Event deleted permanently", Toast.LENGTH_SHORT).show();
                                    int currentPos = holder.getBindingAdapterPosition();
                                    if (currentPos != RecyclerView.NO_POSITION && currentPos < eventList.size()) {
                                        eventList.remove(currentPos);
                                        notifyItemRemoved(currentPos);
                                        notifyItemRangeChanged(currentPos, eventList.size());
                                    }
                                } else {
                                    Toast.makeText(context, "Failed to delete event", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                        .setNegativeButton("Close", null)
                        .show();
            });

            holder.btnBook.setText("Edit Showtime");
            holder.btnBook.setOnClickListener(v -> showEditShowtimeDialog(context, event.getId(), event.getTitle()));
            holder.itemView.setOnClickListener(v -> showEditShowtimeDialog(context, event.getId(), event.getTitle()));
        } else {
            holder.btnDeleteEvent.setVisibility(View.GONE);
            holder.btnBook.setText("Book");
            holder.btnBook.setOnClickListener(v -> {
                Intent intent = new Intent(context, EventDetailActivity.class);
                intent.putExtra("event_id", event.getId());
                intent.putExtra("user_email", userEmail);
                context.startActivity(intent);
            });

            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, EventDetailActivity.class);
                intent.putExtra("event_id", event.getId());
                intent.putExtra("user_email", userEmail);
                context.startActivity(intent);
            });
        }
    }

    private void showEditShowtimeDialog(Context ctx, int eventId, String eventTitle) {
        try (DatabaseHelper dbHelper = new DatabaseHelper(ctx)) {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            String currentDate = "2026-11-05";
            String currentTime = "07:00 PM";
            int showtimeId = -1;

            try (Cursor cursor = db.query(DatabaseHelper.TABLE_SHOWTIMES, null, "event_id=?", new String[]{"" + eventId}, null, null, null)) {
                if (cursor.moveToFirst()) {
                    showtimeId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    currentDate = cursor.getString(cursor.getColumnIndexOrThrow("date"));
                    currentTime = cursor.getString(cursor.getColumnIndexOrThrow("time"));
                }
            }

            final int stId = showtimeId;
            LinearLayout layout = new LinearLayout(ctx);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(40, 20, 40, 20);

            final EditText inputDate = new EditText(ctx);
            inputDate.setHint("Date (YYYY-MM-DD)");
            inputDate.setText(currentDate);
            layout.addView(inputDate);

            final EditText inputTime = new EditText(ctx);
            inputTime.setHint("Time (e.g. 07:00 PM)");
            inputTime.setText(currentTime);
            layout.addView(inputTime);

            new AlertDialog.Builder(ctx)
                    .setTitle("Edit Showtime: " + eventTitle)
                    .setView(layout)
                    .setPositiveButton("Update", (dialog, which) -> {
                        String newDate = inputDate.getText().toString().trim();
                        String newTime = inputTime.getText().toString().trim();

                        if (newDate.isEmpty() || newTime.isEmpty()) {
                            Toast.makeText(ctx, "Date and Time cannot be empty", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        SQLiteDatabase wdb = dbHelper.getWritableDatabase();
                        ContentValues cv = new ContentValues();
                        cv.put("date", newDate);
                        cv.put("time", newTime);

                        if (stId == -1) {
                            Toast.makeText(ctx, "This event has no existing showtime to edit.", Toast.LENGTH_LONG).show();
                            return;
                        }
                        int updated = wdb.update(DatabaseHelper.TABLE_SHOWTIMES, cv, "id=?", new String[]{String.valueOf(stId)});
                        if (updated > 0) {
                            Toast.makeText(ctx, "Showtime date and time updated.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ctx, "Could not update showtime. Please try again.", Toast.LENGTH_SHORT).show();
                        }
                        if (ctx instanceof AdminHomeActivity) {
                            ((AdminHomeActivity) ctx).recreate();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }

    @Override
    public int getItemCount() {
        return eventList.size();
    }

    public static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvCategory, tvCityGenre, tvVenue, tvPrice, btnDeleteEvent;
        ImageView ivEventImage;
        Button btnBook;

        public EventViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvEventTitle);
            tvCategory = itemView.findViewById(R.id.tvEventCategory);
            tvCityGenre = itemView.findViewById(R.id.tvEventCityGenre);
            tvVenue = itemView.findViewById(R.id.tvEventVenue);
            tvPrice = itemView.findViewById(R.id.tvEventPrice);
            ivEventImage = itemView.findViewById(R.id.ivEventImage);
            btnBook = itemView.findViewById(R.id.btnBookEvent);
            btnDeleteEvent = itemView.findViewById(R.id.btnDeleteEvent);
        }
    }
}
