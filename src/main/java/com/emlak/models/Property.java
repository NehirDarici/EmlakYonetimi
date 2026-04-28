package com.emlak.models;

public class Property {
    private int id;
    private double x;
    private double y;
    private double price;
    private String type;

    // Sadece gerçekten kullanacağımız verilerle oluşturulan Constructor (Yapıcı Metot)
    public Property(int id, double x, double y, double price, String type) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.price = price;
        this.type = type;
    }

    // Getters (Arayüzdeki tabloların ve ağaçların verilere erişebilmesi için)
    public int getId() { return id; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getPrice() { return price; }
    public String getType() { return type; }

    // Java bu objeyi ekrana yazdırırken bellek adresini değil, bu metni kullanacak
    @Override
    public String toString() {
        return "İlan ID: " + id + " | " + type + " | " + price + " TL";
    }
}