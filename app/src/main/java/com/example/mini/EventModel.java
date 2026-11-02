package com.example.mini;

public class EventModel {
    private final int id;
    private final String title;
    private final String category;
    private final String genre;
    private final String city;
    private final String description;
    private final String venue;
    private final double price;
    private final String seatTier;

    public EventModel(int id, String title, String category, String genre, String city, String description, String venue, double price, String seatTier) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.genre = genre;
        this.city = city;
        this.description = description;
        this.venue = venue;
        this.price = price;
        this.seatTier = seatTier;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getCategory() { return category; }
    public String getGenre() { return genre; }
    public String getCity() { return city; }
    public String getDescription() { return description; }
    public String getVenue() { return venue; }
    public double getPrice() { return price; }
    public String getSeatTier() { return seatTier; }
}
