package org.example;

import com.emlak.datastructures.KDTree;
import com.emlak.models.Property;
import com.emlak.services.MockDataGenerator;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;

public class Main {
    public static void main(String[] args) {
        System.out.println("Sistem Başlatılıyor... 500.000 veri üretiliyor...");
        List<Property> propertyList = MockDataGenerator.generateData(500000);

        // --- VERİ YAPILARINI BAŞLATMA (İndeksleme) ---
        KDTree kdTree = new KDTree();
        // ID ile hızlı arama (O(1)) için Hash Map oluşturuyoruz
        HashMap<Integer, Property> propertyMap = new HashMap<>();

        System.out.println("Veriler KD-Tree ve Hash Map'e yükleniyor...");
        for (Property p : propertyList) {
            kdTree.insert(p);                // Harita araması için ağaca ekle
            propertyMap.put(p.getId(), p);   // ID araması için Hash Table'a ekle
        }
        System.out.println("Yükleme Tamamlandı!\n--------------------------------------------------");


        // ==========================================
        // TEST 1: HASH MAP İLE O(1) ID ARAMASI
        // ==========================================
        int arananID = 450000; // Sonlara doğru bir ID seçelim
        System.out.println("TEST 1: İlan No (" + arananID + ") ile Arama");

        // A) Kötü Yaklaşım: Dizi içinde arama O(N)
        long startLoop = System.nanoTime();
        Property loopResult = null;
        for (Property p : propertyList) {
            if (p.getId() == arananID) {
                loopResult = p;
                break;
            }
        }
        long endLoop = System.nanoTime();

        // B) İyi Yaklaşım: Hash Map ile arama O(1)
        long startHash = System.nanoTime();
        Property hashResult = propertyMap.get(arananID);
        long endHash = System.nanoTime();

        System.out.println("Dizi/Döngü ile Bulma Süresi : " + (endLoop - startLoop) / 1_000_000.0 + " ms");
        System.out.println("Hash Map ile Bulma Süresi   : " + (endHash - startHash) / 1_000_000.0 + " ms");
        System.out.println("Bulunan Ev: " + hashResult.getType() + " - " + hashResult.getPrice() + " TL");
        System.out.println("--------------------------------------------------");


        // ==========================================
        // TEST 2: KD-TREE + PRIORITY QUEUE (HEAPSORT)
        // ==========================================
        double targetX = 50.0;
        double targetY = 50.0;
        double radius = 2.0;

        System.out.println("TEST 2: Merkez(" + targetX + ", " + targetY + ") - " + radius + " km Çapındaki En Ucuz Evler");

        // 1. Adım: Bölgedeki evleri KD-Tree ile hızlıca bul
        List<Property> bolgedekiEvler = kdTree.findInRadius(targetX, targetY, radius);
        System.out.println("Bölgede toplam " + bolgedekiEvler.size() + " ev bulundu.");

        // 2. Adım: Bulunan evleri fiyata göre sıralamak için Min-Heap (Priority Queue) kullan
        // Comparator ile fiyata göre küçükten büyüğe sıralamasını söylüyoruz
        PriorityQueue<Property> ucuzEvlerHeap = new PriorityQueue<>(Comparator.comparingDouble(Property::getPrice));

        // Evleri Heap'e doldur (Otomatik olarak en ucuzlar tepeye çıkacak)
        ucuzEvlerHeap.addAll(bolgedekiEvler);

        System.out.println("\n*** BÖLGEDEKİ EN UCUZ 3 EV ***");
        for (int i = 0; i < 3; i++) {
            if (!ucuzEvlerHeap.isEmpty()) {
                Property enUcuz = ucuzEvlerHeap.poll(); // poll() metodu en tepedekini (en ucuzu) çeker ve kuyruktan atar
                System.out.println((i+1) + ". Seçenek: " + enUcuz.getPrice() + " TL | Koordinat: (" + enUcuz.getX() + ", " + enUcuz.getY() + ")");
            }
        }
        System.out.println("--------------------------------------------------");
    }
}