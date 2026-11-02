package com.example.mini;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "ShowTime.db";
    private static final int DATABASE_VERSION = 4;

    public static final String TABLE_USERS = "users";
    public static final String TABLE_EVENTS = "events";
    public static final String TABLE_SHOWTIMES = "showtimes";
    public static final String TABLE_SEATS = "seats";
    public static final String TABLE_BOOKINGS = "bookings";
    public static final String TABLE_WAITLIST = "waitlist";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "email TEXT UNIQUE, " +
                "phone TEXT, " +
                "password TEXT, " +
                "is_admin INTEGER DEFAULT 0)");

        db.execSQL("CREATE TABLE " + TABLE_EVENTS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT, " +
                "category TEXT, " + // Movies, Music, Comedy, Cultural, Plays, Workshops
                "genre TEXT, " +
                "city TEXT, " +
                "description TEXT, " +
                "venue TEXT, " +
                "price REAL, " +
                "seat_tier TEXT)"); // Regular, VIP, Balcony

        db.execSQL("CREATE TABLE " + TABLE_SHOWTIMES + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "event_id INTEGER, " +
                "date TEXT, " +
                "time TEXT, " +
                "FOREIGN KEY(event_id) REFERENCES events(id))");

        db.execSQL("CREATE TABLE " + TABLE_SEATS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "showtime_id INTEGER, " +
                "seat_number TEXT, " +
                "status TEXT)"); // AVAILABLE, LOCKED, BOOKED

        db.execSQL("CREATE TABLE " + TABLE_BOOKINGS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_email TEXT, " +
                "event_title TEXT, " +
                "showtime TEXT, " +
                "seats TEXT, " +
                "total_fare REAL, " +
                "payment_method TEXT, " +
                "booking_date TEXT, " +
                "status TEXT DEFAULT 'CONFIRMED', " +
                "qr_code TEXT)");

        db.execSQL("CREATE TABLE " + TABLE_WAITLIST + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "event_id INTEGER, " +
                "showtime_id INTEGER, " +
                "user_email TEXT, " +
                "requested_seats TEXT, " +
                "request_date TEXT)");

        // Default Admin
        ContentValues adminValues = new ContentValues();
        adminValues.put("name", "Administrator");
        adminValues.put("email", "admin@showtime.com");
        adminValues.put("phone", "9999999999");
        adminValues.put("password", "admin123");
        adminValues.put("is_admin", 1);
        db.insert(TABLE_USERS, null, adminValues);

        // Default User
        ContentValues userValues = new ContentValues();
        userValues.put("name", "John Doe");
        userValues.put("email", "user@showtime.com");
        userValues.put("phone", "8888888888");
        userValues.put("password", "password123");
        userValues.put("is_admin", 0);
        db.insert(TABLE_USERS, null, userValues);

        // Insert 6 shows per category across 6 categories (36+ events)
        insertHardcodedEvents(db);
    }

    private void insertHardcodedEvents(SQLiteDatabase db) {
        // --- MOVIES (6) ---
        addEventWithShowtimes(db, "Inception - SciFi IMAX", "Movies", "SciFi", "Mumbai", "A thief who steals corporate secrets through dream-sharing technology.", "PVR Icon IMAX", 350.0, "VIP");
        addEventWithShowtimes(db, "Interstellar Re-Release", "Movies", "SciFi", "Bengaluru", "A team of explorers travel through a wormhole in space.", "PVR Orion Mall", 300.0, "Regular");
        addEventWithShowtimes(db, "The Dark Knight", "Movies", "Action", "Delhi", "When the menace known as the Joker wreaks havoc on Gotham.", "Cinepolis DLF", 280.0, "Balcony");
        addEventWithShowtimes(db, "Avatar: Way of Water", "Movies", "Fantasy", "Pune", "Jake Sully lives with his newfound family on Pandora.", "E-Square", 320.0, "VIP");
        addEventWithShowtimes(db, "Spider-Man: Across the Spider-Verse", "Movies", "Animation", "Hyderabad", "Miles Morales catapults across the Multiverse.", "AMB Cinemas", 250.0, "Regular");
        addEventWithShowtimes(db, "Jailer - Action Drama", "Movies", "Action", "Chennai", "A jailer goes on a manhunt to find his son's killers.", "SPI Cinemas", 290.0, "VIP");

        // --- MUSIC (6) ---
        addEventWithShowtimes(db, "Coldplay Music of the Spheres", "Music", "Rock", "Mumbai", "Spectacular world tour with dazzling lights and timeless anthems.", "DY Patil Stadium", 2500.0, "VIP");
        addEventWithShowtimes(db, "Arijit Singh Live Symphony", "Music", "Melody", "Bengaluru", "An unforgettable soulful evening with India's most beloved voice.", "Palace Grounds", 1800.0, "Balcony");
        addEventWithShowtimes(db, "Sunburn Arena EDM Fest", "Music", "Electronic", "Goa", "Massive electronic dance music festival featuring top global DJs.", "Vagator Beach", 2000.0, "Regular");
        addEventWithShowtimes(db, "Prateek Kuhad Winter Tour", "Music", "Indie", "Delhi", "Intimate indie folk melodies and warm acoustic storytelling.", "Jawaharlal Nehru Stadium", 999.0, "Regular");
        addEventWithShowtimes(db, "Carnatic Fusion Night", "Music", "Classical", "Chennai", "Blending traditional Carnatic ragas with contemporary jazz and rock.", "Music Academy", 600.0, "Balcony");
        addEventWithShowtimes(db, "AR Rahman Encore Live", "Music", "Bollywood", "Hyderabad", "The Mozart of Madras performing iconic chartbusters live.", "Gachibowli Stadium", 2200.0, "VIP");

        // --- COMEDY (6) ---
        addEventWithShowtimes(db, "Zakir Khan - Tathastu", "Comedy", "Standup", "Mumbai", "Raw, honest, and deeply relatable comedy by the Sakht Launda.", "Shanmukhananda Hall", 999.0, "VIP");
        addEventWithShowtimes(db, "Bass<bos>2.0 by Anubhav Singh Bassi", "Comedy", "Standup", "Bengaluru", "Hilarious anecdotal comedy about life, hostel days, and adventures.", "Good Shepherd Auditorium", 899.0, "Regular");
        addEventWithShowtimes(db, "Kenny Sebastian - Special", "Comedy", "Standup", "Delhi", "Observational humor, musical comedy, and witty timing.", "Sirifort Auditorium", 799.0, "Balcony");
        addEventWithShowtimes(db, "Rahul Subramanian Live", "Comedy", "Standup", "Pune", "Spontaneous crowd work and hilarious corporate observations.", "Balewadi Stadium", 699.0, "Regular");
        addEventWithShowtimes(db, "Urooj Ashfaq - Comedy Special", "Comedy", "Standup", "Hyderabad", "Wry, quirky, and side-splitting humour on modern relationships.", "Shilpakala Vedika", 599.0, "Regular");
        addEventWithShowtimes(db, "Biswa Kalyan Rath - Live", "Comedy", "Standup", "Kolkata", "Absurd, intellectual, and hyperactive comedy special.", "Kalamandir Arena", 650.0, "Regular");

        // --- CULTURAL (6) ---
        addEventWithShowtimes(db, "Nrityagram Odissi Ensemble", "Cultural", "Dance", "Bengaluru", "Exquisite classical Odissi dance performance celebrating art and devotion.", "Nrityagram Dance Village", 500.0, "Balcony");
        addEventWithShowtimes(db, "Sufi & Qawwali Evening", "Cultural", "Sufi", "Delhi", "Enchanting spiritual qawwali performances under the stars.", "Siri Fort Grounds", 750.0, "VIP");
        addEventWithShowtimes(db, "Rajasthan Folk Festival", "Cultural", "Folk", "Jaipur", "Vibrant folk music, dance, and puppet art of the Thar desert.", "Amber Fort", 450.0, "Regular");
        addEventWithShowtimes(db, "Kathak Symphony & Tabla Tarang", "Cultural", "Classical", "Lucknow", "Graceful footwork synchronized with intricate rhythmic beats.", "Sangeet Natak Akademi", 400.0, "Regular");
        addEventWithShowtimes(db, "Classical Carnatic Vocal Recital", "Cultural", "Carnatic", "Chennai", "Masterpieces of Carnatic composition performed by legendary maestros.", "Narada Gana Sabha", 350.0, "Balcony");
        addEventWithShowtimes(db, "Bihu & Sattriya Folk Showcase", "Cultural", "Folk", "Guwahati", "Traditional northeastern spring festival dance and music gala.", "Srimanta Sankaradeva Kalakshetra", 300.0, "Regular");

        // --- PLAYS (6) ---
        addEventWithShowtimes(db, "The Merchant of Venice - Play", "Plays", "Drama", "Mumbai", "Classic Shakespearean drama exploring justice, mercy, and prejudice.", "Prithvi Theatre", 600.0, "VIP");
        addEventWithShowtimes(db, "Taj Express - The Musical", "Plays", "Musical", "Delhi", "High-energy theatrical journey through modern and retro Bollywood.", "Kingdom of Dreams", 1200.0, "VIP");
        addEventWithShowtimes(db, "Chanakya - Hindi Historical Play", "Plays", "Historical", "Pune", "Political brilliance and statecraft of Acharya Chanakya on stage.", "Balgandharva Rangmandir", 500.0, "Balcony");
        addEventWithShowtimes(db, "God of Carnage - Dark Comedy Play", "Plays", "Comedy", "Bengaluru", "Two sets of parents meet after their sons have a fight.", "Ranga Shankara", 550.0, "Regular");
        addEventWithShowtimes(db, "The Mousetrap - Agatha Christie", "Plays", "Mystery", "Kolkata", "The world-famous murder mystery thriller play.", "Academy of Fine Arts", 650.0, "VIP");
        addEventWithShowtimes(db, "Sakharam Binder - Classic Play", "Plays", "Drama", "Mumbai", "Groundbreaking socio-political Marathi theatre classic.", "NCPA Experimental Theatre", 500.0, "Regular");

        // --- WORKSHOPS (6) ---
        addEventWithShowtimes(db, "Improv Comedy Workshop", "Workshops", "Interactive", "Bengaluru", "Learn the fundamentals of spontaneous improvisation and stage confidence.", "The Comedy Studio", 999.0, "Regular");
        addEventWithShowtimes(db, "Pottery & Ceramic Masterclass", "Workshops", "Art", "Mumbai", "Hands-on pottery workshop with expert ceramic artists and wheel throwing.", "Studio Potter's Field", 1500.0, "Regular");
        addEventWithShowtimes(db, "Creative Writing & Storytelling", "Workshops", "Literature", "Pune", "Master narrative arcs, character building, and dramatic scriptwriting.", "Kitab Khana Lounge", 800.0, "Regular");
        addEventWithShowtimes(db, "Photography & Lighting Masterclass", "Workshops", "Visuals", "Delhi", "Professional DSLR and mobile photography composition workshop.", "India Habitat Centre", 1200.0, "VIP");
        addEventWithShowtimes(db, "Salsa & Bachata Dance Bootcamp", "Workshops", "Dance", "Hyderabad", "Learn Latin social dancing steps from professional choreographers.", "HEART Dance Studio", 1100.0, "Regular");
        addEventWithShowtimes(db, "Gourmet Baking & Pastry Masterclass", "Workshops", "Culinary", "Chennai", "Master artisanal sourdough bread, croissants, and patisserie.", "The Culinary Lab", 1800.0, "VIP");
    }

    private void addEventWithShowtimes(SQLiteDatabase db, String title, String category, String genre, String city, String description, String venue, double price, String seatTier) {
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("category", category);
        cv.put("genre", genre);
        cv.put("city", city);
        cv.put("description", description);
        cv.put("venue", venue);
        cv.put("price", price);
        cv.put("seat_tier", seatTier);
        long eventId = db.insert(TABLE_EVENTS, null, cv);

        for (int i = 1; i <= 2; i++) {
            ContentValues stCv = new ContentValues();
            stCv.put("event_id", eventId);
            stCv.put("date", "2026-11-0" + i);
            stCv.put("time", i == 1 ? "11:00 AM" : "07:00 PM");
            long showtimeId = db.insert(TABLE_SHOWTIMES, null, stCv);

            char row = 'A';
            for (int r = 0; r < 4; r++) {
                for (int s = 1; s <= 4; s++) {
                    ContentValues seatCv = new ContentValues();
                    seatCv.put("showtime_id", showtimeId);
                    seatCv.put("seat_number", "" + (char)(row + r) + s);
                    seatCv.put("status", "AVAILABLE");
                    db.insert(TABLE_SEATS, null, seatCv);
                }
            }
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EVENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SHOWTIMES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SEATS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BOOKINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WAITLIST);
        onCreate(db);
    }

    public boolean registerUser(String name, String email, String phone, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("email", email);
        cv.put("phone", phone);
        cv.put("password", password);
        cv.put("is_admin", 0);
        long result = db.insert(TABLE_USERS, null, cv);
        return result != -1;
    }

    public Cursor checkUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE email=? AND password=?", new String[]{email, password});
    }

    public Cursor getUserProfile(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE email=?", new String[]{email});
    }

    public boolean updatePassword(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("password", newPassword);
        int rows = db.update(TABLE_USERS, cv, "email=?", new String[]{email});
        return rows > 0;
    }

    public boolean updateUserProfile(String email, String name, String phone) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("phone", phone);
        int rows = db.update(TABLE_USERS, cv, "email=?", new String[]{email});
        return rows > 0;
    }

    // ✅ NEW METHOD: fetch a single booking row by its ID
    // Used by BookingHistoryActivity when the user taps a booking card,
    // so the same TicketActivity can be reopened with all details.
    public Cursor getBookingById(int bookingId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM " + TABLE_BOOKINGS + " WHERE id = ?",
                new String[]{String.valueOf(bookingId)}
        );
    }

    public boolean deleteEventCompletely(int eventId) {
        SQLiteDatabase db = this.getWritableDatabase();
        try (Cursor c = db.query(TABLE_SHOWTIMES, new String[]{"id"}, "event_id=?", new String[]{String.valueOf(eventId)}, null, null, null)) {
            if (c != null) {
                while (c.moveToNext()) {
                    int stId = c.getInt(0);
                    db.delete(TABLE_SEATS, "showtime_id=?", new String[]{String.valueOf(stId)});
                }
            }
        }
        db.delete(TABLE_SHOWTIMES, "event_id=?", new String[]{String.valueOf(eventId)});
        db.delete(TABLE_WAITLIST, "event_id=?", new String[]{String.valueOf(eventId)});
        int rows = db.delete(TABLE_EVENTS, "id=?", new String[]{String.valueOf(eventId)});
        return rows > 0;
    }
}