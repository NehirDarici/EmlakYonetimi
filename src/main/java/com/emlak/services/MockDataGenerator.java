package com.emlak.services;

import com.emlak.models.Property;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MockDataGenerator {

    public static List<Property> generateData(int count) {
        List<Property> properties = new ArrayList<>();
        Random random = new Random();

        // Emlak tipleri dizisi
        String[] types = {"Satılık", "Kiralık", "Günlük Kiralık"};

        for (int i = 1; i <= count; i++) {
            int id = i;

            // X ve Y Koordinatları
            double x = Math.round(random.nextDouble() * 100.0 * 100.0) / 100.0;
            double y = Math.round(random.nextDouble() * 100.0 * 100.0) / 100.0;

            // Fiyat hesaplaması
            double price = 20000 + (random.nextDouble() * 90000);
            price = Math.round(price / 10000) * 10000;

            // Rastgele tip seçimi
            String type = types[random.nextInt(types.length)];

            // Rastgele oda sayısı üretimi (1 ile 5 arası)
            int rooms = random.nextInt(5) + 1;

            Property newProperty = new Property(id, x, y, price, type, rooms);

            properties.add(newProperty);
        }

        return properties;
    }
}