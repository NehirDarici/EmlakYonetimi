package com.emlak.models;

public class Property {
    private int id;
    private double x;
    private double y;
    private double price;
    private String type;
    private int rooms;

    // Sadece gerçekten kullanacağımız verilerle oluşturulan Constructor (Yapıcı Metot)
    public Property(int id, double x, double y, double price, String type, int rooms) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.price = price;
        this.type = type;
        this.rooms = rooms;
    }

    // --- GETTER METOTLARI ---
    public int getId() {
        return id;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getPrice() {
        return price;
    }

    public String getType() {
        return type;
    }

    public int getRooms() {
        return rooms;
    }


    // Java bu objeyi ekrana yazdırırken bellek adresini değil, bu metni kullanacak
    @Override
    public String toString() {
        return "İlan ID: " + id + " | " + type + " | " + price + " TL";
    }
}