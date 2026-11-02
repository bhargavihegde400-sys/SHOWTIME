
package com.example.mini;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class UserHomeActivity extends AppCompatActivity {

    private RecyclerView rvEvents;
    private DatabaseHelper dbHelper;
    private String userEmail;
    private String currentCategory = "All";
    private List<EventModel> eventList;

    private EditText etSearch;

    private String selectedCity = "All Cities";
    private String selectedPrice = "All Prices";

    private Button btnAll, btnMovies, btnMusic, btnComedy,
            btnCultural, btnPlays, btnWorkshops;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_home);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.userHomeLayout), (v, insets) -> {
                    Insets systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                    );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );

        userEmail = getIntent().getStringExtra("user_email");

        if (userEmail == null || userEmail.isEmpty()) {
            userEmail = "user@showtime.com";
        }

        dbHelper = new DatabaseHelper(this);

        rvEvents = findViewById(R.id.rvEvents);
        rvEvents.setLayoutManager(new LinearLayoutManager(this));

        eventList = new ArrayList<>();

        etSearch = findViewById(R.id.etSearch);

        // Load all events initially.
        loadEvents("");

        // Search events as the user types.
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(
                    CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s, int start, int before, int count) {
                loadEvents(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Filter button inside the search bar.
        ImageButton btnFilter = findViewById(R.id.btnFilter);

        btnFilter.setOnClickListener(v -> showFilterBottomSheet());

        // Hamburger Menu.
        ImageView ivHamburger = findViewById(R.id.ivHamburger);

        ivHamburger.setOnClickListener(v -> {
            PopupMenu popup = new PopupMenu(
                    UserHomeActivity.this, ivHamburger
            );

            popup.getMenu().add("Profile & Settings");
            popup.getMenu().add("My Bookings");
            popup.getMenu().add("Logout");

            popup.setOnMenuItemClickListener(item -> {
                String title = item.getTitle() != null
                        ? item.getTitle().toString()
                        : "";

                switch (title) {
                    case "Profile & Settings": {
                        Intent intentProfile = new Intent(
                                UserHomeActivity.this,
                                SettingsActivity.class
                        );

                        intentProfile.putExtra("user_email", userEmail);
                        startActivity(intentProfile);
                        break;
                    }

                    case "My Bookings": {
                        Intent intentBookings = new Intent(
                                UserHomeActivity.this,
                                BookingHistoryActivity.class
                        );

                        intentBookings.putExtra("user_email", userEmail);
                        startActivity(intentBookings);
                        break;
                    }

                    case "Logout":
                        startActivity(new Intent(
                                UserHomeActivity.this,
                                MainActivity.class
                        ));

                        finishAffinity();
                        break;
                }

                return true;
            });

            popup.show();
        });

        // Profile icon.
        ImageView ivProfileIcon = findViewById(R.id.ivProfileIcon);

        ivProfileIcon.setOnClickListener(v -> {
            Intent intent = new Intent(
                    UserHomeActivity.this,
                    SettingsActivity.class
            );

            intent.putExtra("user_email", userEmail);
            startActivity(intent);
        });

        // Category filter buttons.
        btnAll = findViewById(R.id.filterAll);
        btnMovies = findViewById(R.id.filterMovies);
        btnMusic = findViewById(R.id.filterMusic);
        btnComedy = findViewById(R.id.filterComedy);
        btnCultural = findViewById(R.id.filterCultural);
        btnPlays = findViewById(R.id.filterPlays);
        btnWorkshops = findViewById(R.id.filterWorkshops);

        btnAll.setOnClickListener(v -> {
            currentCategory = "All";
            updateCategoryButtonStyles(btnAll);
            loadEvents(etSearch.getText().toString());
        });

        btnMovies.setOnClickListener(v -> {
            currentCategory = "Movies";
            updateCategoryButtonStyles(btnMovies);
            loadEvents(etSearch.getText().toString());
        });

        btnMusic.setOnClickListener(v -> {
            currentCategory = "Music";
            updateCategoryButtonStyles(btnMusic);
            loadEvents(etSearch.getText().toString());
        });

        btnComedy.setOnClickListener(v -> {
            currentCategory = "Comedy";
            updateCategoryButtonStyles(btnComedy);
            loadEvents(etSearch.getText().toString());
        });

        btnCultural.setOnClickListener(v -> {
            currentCategory = "Cultural";
            updateCategoryButtonStyles(btnCultural);
            loadEvents(etSearch.getText().toString());
        });

        btnPlays.setOnClickListener(v -> {
            currentCategory = "Plays";
            updateCategoryButtonStyles(btnPlays);
            loadEvents(etSearch.getText().toString());
        });

        btnWorkshops.setOnClickListener(v -> {
            currentCategory = "Workshops";
            updateCategoryButtonStyles(btnWorkshops);
            loadEvents(etSearch.getText().toString());
        });

        // Bottom Navigation.
        Button navHome = findViewById(R.id.navHome);
        Button navBookings = findViewById(R.id.navBookings);
        Button navProfile = findViewById(R.id.navProfile);

        navHome.setOnClickListener(v -> {
            etSearch.setText("");
            currentCategory = "All";
            updateCategoryButtonStyles(btnAll);
            loadEvents("");
        });

        navBookings.setOnClickListener(v -> {
            Intent intent = new Intent(
                    UserHomeActivity.this,
                    BookingHistoryActivity.class
            );

            intent.putExtra("user_email", userEmail);
            startActivity(intent);
        });

        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(
                    UserHomeActivity.this,
                    SettingsActivity.class
            );

            intent.putExtra("user_email", userEmail);
            startActivity(intent);
        });
    }

    // Show city and price filters in a bottom sheet.
    private void showFilterBottomSheet() {

        BottomSheetDialog dialog = new BottomSheetDialog(this);

        View sheet = LayoutInflater.from(this).inflate(
                R.layout.bottom_sheet_filters, null
        );

        dialog.setContentView(sheet);

        Spinner spinnerCity =
                sheet.findViewById(R.id.spinnerFilterCity);

        RadioGroup rgPrice =
                sheet.findViewById(R.id.rgFilterPrice);

        Button btnReset =
                sheet.findViewById(R.id.btnResetFilters);

        Button btnApply =
                sheet.findViewById(R.id.btnApplyFilters);

        // Get available cities from the database.
        List<String> cities = new ArrayList<>();
        cities.add("All Cities");

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        try (Cursor cursor = db.query(
                true,
                DatabaseHelper.TABLE_EVENTS,
                new String[]{"city"},
                "city IS NOT NULL AND city != ''",
                null,
                null,
                null,
                "city ASC",
                null
        )) {
            while (cursor.moveToNext()) {
                String city = cursor.getString(0);

                if (city != null && !cities.contains(city)) {
                    cities.add(city);
                }
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                cities
        );

        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item
        );

        spinnerCity.setAdapter(adapter);

        // Restore the previously selected city.
        int cityPosition = cities.indexOf(selectedCity);

        spinnerCity.setSelection(
                cityPosition >= 0 ? cityPosition : 0
        );

        // Restore the previously selected price range.
        switch (selectedPrice) {
            case "Under ₹500":
                rgPrice.check(R.id.rbUnder500);
                break;

            case "₹500–₹1,000":
                rgPrice.check(R.id.rb500To1000);
                break;

            case "Above ₹1,000":
                rgPrice.check(R.id.rbAbove1000);
                break;

            default:
                rgPrice.check(R.id.rbAllPrices);
                break;
        }

        // Reset selections in the bottom sheet.
        btnReset.setOnClickListener(v -> {
            spinnerCity.setSelection(0);
            rgPrice.check(R.id.rbAllPrices);
        });

        // Apply selected filters.
        btnApply.setOnClickListener(v -> {
            Object cityItem = spinnerCity.getSelectedItem();

            selectedCity = cityItem != null
                    ? cityItem.toString()
                    : "All Cities";

            int checkedId = rgPrice.getCheckedRadioButtonId();

            if (checkedId == R.id.rbUnder500) {
                selectedPrice = "Under ₹500";
            } else if (checkedId == R.id.rb500To1000) {
                selectedPrice = "₹500–₹1,000";
            } else if (checkedId == R.id.rbAbove1000) {
                selectedPrice = "Above ₹1,000";
            } else {
                selectedPrice = "All Prices";
            }

            loadEvents(etSearch.getText().toString());
            dialog.dismiss();
        });

        dialog.show();
    }

    // Update the appearance of the selected category button.
    private void updateCategoryButtonStyles(Button activeBtn) {

        Button[] allButtons = {
                btnAll, btnMovies, btnMusic, btnComedy,
                btnCultural, btnPlays, btnWorkshops
        };

        int terracottaColor = ContextCompat.getColor(
                this, R.color.terracotta
        );

        int cardDarkColor = ContextCompat.getColor(
                this, R.color.card_dark
        );

        for (Button btn : allButtons) {
            if (btn != null) {
                if (btn == activeBtn) {
                    btn.setBackgroundTintList(
                            ColorStateList.valueOf(terracottaColor)
                    );
                } else {
                    btn.setBackgroundTintList(
                            ColorStateList.valueOf(cardDarkColor)
                    );
                }
            }
        }
    }

    // Load events using search, category, city and price filters.
    private void loadEvents(String query) {

        eventList.clear();

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        StringBuilder selection = new StringBuilder(
                "(title LIKE ? OR city LIKE ? OR genre LIKE ?)"
        );

        List<String> args = new ArrayList<>();

        String search = "%" + query + "%";

        args.add(search);
        args.add(search);
        args.add(search);

        // Apply the selected category.
        if (!Objects.equals(currentCategory, "All")) {
            selection.append(" AND category = ?");
            args.add(currentCategory);
        }

        // Apply the selected city.
        if (!Objects.equals(selectedCity, "All Cities")) {
            selection.append(" AND city = ?");
            args.add(selectedCity);
        }

        // Apply the selected price range.
        switch (selectedPrice) {
            case "Under ₹500":
                selection.append(" AND price < ?");
                args.add("500");
                break;

            case "₹500–₹1,000":
                selection.append(" AND price >= ? AND price <= ?");
                args.add("500");
                args.add("1000");
                break;

            case "Above ₹1,000":
                selection.append(" AND price > ?");
                args.add("1000");
                break;
        }

        String[] selectionArgs = args.toArray(new String[0]);

        try (Cursor cursor = db.query(
                DatabaseHelper.TABLE_EVENTS,
                null,
                selection.toString(),
                selectionArgs,
                null,
                null,
                null
        )) {

            if (cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(
                            cursor.getColumnIndexOrThrow("id")
                    );

                    String title = cursor.getString(
                            cursor.getColumnIndexOrThrow("title")
                    );

                    String category = cursor.getString(
                            cursor.getColumnIndexOrThrow("category")
                    );

                    String genre = cursor.getString(
                            cursor.getColumnIndexOrThrow("genre")
                    );

                    String city = cursor.getString(
                            cursor.getColumnIndexOrThrow("city")
                    );

                    String desc = cursor.getString(
                            cursor.getColumnIndexOrThrow("description")
                    );

                    String venue = cursor.getString(
                            cursor.getColumnIndexOrThrow("venue")
                    );

                    double price = cursor.getDouble(
                            cursor.getColumnIndexOrThrow("price")
                    );

                    String seatTier = cursor.getString(
                            cursor.getColumnIndexOrThrow("seat_tier")
                    );

                    eventList.add(new EventModel(
                            id,
                            title,
                            category,
                            genre,
                            city,
                            desc,
                            venue,
                            price,
                            seatTier
                    ));

                } while (cursor.moveToNext());
            }
        }

        EventAdapter eventAdapter = new EventAdapter(
                this, eventList, userEmail
        );

        rvEvents.setAdapter(eventAdapter);
    }
}
