package com.emlak.models;

import java.util.LinkedList;
//Emlak ilanının şablonu oluşturulur.
public class Property {
    private int id;               // İlanın benzersiz numarası (Hash Table için kilit olacak)
    private double x;             // Harita X koordinatı (KD-Tree için)
    private double y;             // Harita Y koordinatı (KD-Tree için)
    private double price;         // Fiyat (BST ve Heap sıralaması için)
    private String type;          // "Satılık", "Kiralık" (Dizi ile sınıflandırma için)
    private int rooms;            // Oda sayısı (Örn: 3, 4, 5)

    // Yönerge İsteri: Dinamik veri kümesi için Bağlı Liste kullanımı
    private LinkedList<String> photoUrls;

    // Constructor (Yapıcı Metot)
    public Property(int id, double x, double y, double price, String type, int rooms) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.price = price;
        this.type = type;
        this.rooms = rooms;
        this.photoUrls = new LinkedList<>();
    }

    // Fotoğraf Ekleme Metodu
    public void addPhoto(String url) {
        this.photoUrls.add(url);
    }

    // Getters
    public int getId() { return id; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getPrice() { return price; }
    public String getType() { return type; }
    public int getRooms() { return rooms; }
    public LinkedList<String> getPhotoUrls() { return photoUrls; }

    // Konsolda test ederken veriyi düzgün görmek için
    @Override
    public String toString() {
        return "İlan ID: " + id + " | Koordinat: (" + x + ", " + y + ") | Fiyat: " + price + " TL | Tip: " + type + " | Odalar: " + rooms;
    }
}
