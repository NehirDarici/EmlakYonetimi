package com.emlak.services;

import com.emlak.models.Property;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MockDataGenerator {

    // Verilen sayı kadar rastgele emlak ilanı üreten metot
    public static List<Property> generateData(int count) {
        // Performans için ArrayList'in başlangıç kapasitesini baştan veriyoruz
        List<Property> properties = new ArrayList<>(count);
        Random random = new Random();
        String[] types = {"Satılık", "Kiralık", "Günlük Kiralık"};

        for (int i = 1; i <= count; i++) {
            int id = i; // İlan numarası 1'den başlayıp count'a kadar gidecek

            // X ve Y koordinatları (0.0 ile 100.0 km arasında rastgele bir harita konumu)
            double x = Math.round((random.nextDouble() * 100) * 100.0) / 100.0;
            double y = Math.round((random.nextDouble() * 100) * 100.0) / 100.0;

            // Fiyat (1.000.000 TL ile 10.000.000 TL arası rastgele ve yuvarlanmış)
            double price = 1000000 + (random.nextDouble() * 9000000);
            price = Math.round(price / 10000) * 10000; // Küsuratları düzeltmek için

            // Tip ve Oda Sayısı (1 ile 5 arası)
            String type = types[random.nextInt(types.length)];
            int rooms = random.nextInt(5) + 1;

            // Nesneyi oluştur ve listeye ekle
            Property newProperty = new Property(id, x, y, price, type, rooms);

            // Yönergedeki Bağlı Liste (Linked List) kullanımını örneklemek için sahte fotoğraflar ekliyoruz
            newProperty.addPhoto("http://emlaksitesi.com/foto/" + id + "_1.jpg");
            newProperty.addPhoto("http://emlaksitesi.com/foto/" + id + "_2.jpg");

            properties.add(newProperty);
        }

        return properties;
    }
}
