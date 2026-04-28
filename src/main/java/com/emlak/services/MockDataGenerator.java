package com.emlak.services;

import com.emlak.models.Property;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MockDataGenerator {

    public static List<Property> generateData(int count) {
        List<Property> properties = new ArrayList<>();
        Random random = new Random();

        // Emlak tipleri dizisi (Zorunlu "Dizi / Array" veri yapısı kullanımı)
        String[] types = {"Satılık", "Kiralık", "Günlük Kiralık"};

        for (int i = 1; i <= count; i++) {
            int id = i;

            // X ve Y Koordinatları (0.0 ile 100.0 km arası, virgülden sonra 2 hane)
            double x = Math.round(random.nextDouble() * 100.0 * 100.0) / 100.0;
            double y = Math.round(random.nextDouble() * 100.0 * 100.0) / 100.0;

            // Fiyat (1.000.000 TL ile 10.000.000 TL arası)
            // Daha gerçekçi durması için son 4 hanesini sıfırlıyoruz (Örn: 2.450.000 TL)
            double price = 20000 + (random.nextDouble() * 90000);
            price = Math.round(price / 10000) * 10000;

            // Rastgele tip seçimi
            String type = types[random.nextInt(types.length)];

            // TERTEMİZ NESNE ÜRETİMİ (Sadece 5 parametre: id, x, y, price, type)
            Property newProperty = new Property(id, x, y, price, type);

            properties.add(newProperty);
        }

        return properties;
    }
}