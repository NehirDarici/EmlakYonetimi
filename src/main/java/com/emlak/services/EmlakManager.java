package com.emlak.services;

import com.emlak.datastructures.KDTree;
import com.emlak.models.Property;
import java.util.*;

public class EmlakManager {

    private List<Property> mevcutVeri = new ArrayList<>();
    private KDTree kdTree = new KDTree();
    private TreeSet<Property> fiyatAgaciBST = new TreeSet<>(Comparator.comparingDouble(Property::getPrice).thenComparingInt(Property::getId));
    private Map<Integer, List<String>> komsuGraf = new HashMap<>();

    private HashSet<Integer> favoriIdSet = new HashSet<>();
    private Queue<String> randevuKuyrugu = new LinkedList<>();

    // 1. Veri Üretimi ve Yapıların Doldurulması
    public void sistemVerileriniUret(int count) {
        mevcutVeri = MockDataGenerator.generateData(count);
        kdTree = new KDTree();
        fiyatAgaciBST.clear();
        komsuGraf.clear();

        String[] mekanlar = {"🚇 Metro İstasyonu (200m)", "🏥 Şehir Hastanesi (1km)", "🏫 İlkokul (400m)", "🛒 AVM (2km)", "🌳 Şehir Parkı (100m)", "🚌 Otobüs Durağı (50m)"};
        Random rand = new Random();

        for (Property p : mevcutVeri) {
            kdTree.insert(p);
            fiyatAgaciBST.add(p);

            List<String> komsular = new ArrayList<>();
            komsular.add(mekanlar[rand.nextInt(mekanlar.length)]);
            komsular.add(mekanlar[rand.nextInt(mekanlar.length)]);
            komsuGraf.put(p.getId(), komsular);
        }
    }

    // 2. Sadece Fiyata Göre Arama (BST)
    public List<Property> fiyataGoreAra(double minFiyat, double maxFiyat) {
        Property minProp = new Property(0, 0.0, 0.0, minFiyat, "", 0);
        Property maxProp = new Property(Integer.MAX_VALUE, 0.0, 0.0, maxFiyat, "", 0);
        return new ArrayList<>(fiyatAgaciBST.subSet(minProp, maxProp));
    }

    // 3. Sadece Konuma Göre Arama (KD-Tree)
    public List<Property> konumaGoreAra(double tx, double ty, double radius) {
        return kdTree.findInRadius(tx, ty, radius);
    }

    // 4. Hem Konum Hem Fiyat Arama (Kesişim)
    public List<Property> kapsamliAra(double tx, double ty, double radius, double minFiyat, double maxFiyat) {
        List<Property> bolgedekiEvler = kdTree.findInRadius(tx, ty, radius);

        Property minProp = new Property(0, 0.0, 0.0, minFiyat, "", 0);
        Property maxProp = new Property(Integer.MAX_VALUE, 0.0, 0.0, maxFiyat, "", 0);
        SortedSet<Property> fiyatAraligi = fiyatAgaciBST.subSet(minProp, maxProp);

        List<Property> sonuclar = new ArrayList<>();
        for (Property p : bolgedekiEvler) {
            if (fiyatAraligi.contains(p)) {
                sonuclar.add(p);
            }
        }
        return sonuclar;
    }

    // 5. Lineer Arama (Kıyaslama İçin O(N))
    public List<Property> lineerAra(double tx, double ty, double tr, double minFiyat, double maxFiyat, boolean fiyatAktif) {
        List<Property> sonuclar = new ArrayList<>();
        for (Property p : mevcutVeri) {
            boolean fiyatUygun = !fiyatAktif || (p.getPrice() >= minFiyat && p.getPrice() <= maxFiyat);
            if (fiyatUygun && Math.sqrt(Math.pow(p.getX() - tx, 2) + Math.pow(p.getY() - ty, 2)) <= tr) {
                sonuclar.add(p);
            }
        }
        return sonuclar;
    }

    // 6. En Ucuz Fırsatları Bul (BST üzerinden)
    public List<Property> ucuzFirsatlariBul(Collection<Property> evler) {
        TreeSet<Property> ucuzEvlerBST = new TreeSet<>(Comparator.comparingDouble(Property::getPrice).thenComparingInt(Property::getId));
        ucuzEvlerBST.addAll(evler);
        List<Property> firsatlar = new ArrayList<>();
        Iterator<Property> bstIterator = ucuzEvlerBST.iterator();
        for (int i = 0; i < 3 && bstIterator.hasNext(); i++) {
            firsatlar.add(bstIterator.next());
        }
        return firsatlar;
    }

    // 7. Diğer Veri Yapısı İşlemleri (HashSet, Queue, Graph)
    public boolean favoriEkle(int id) { return favoriIdSet.add(id); }
    public boolean favoriSil(int id) { return favoriIdSet.remove(id); }
    public void randevuTalebiEkle(String req) { randevuKuyrugu.offer(req); }
    public String siradakiRandevuyuCagir() { return randevuKuyrugu.poll(); }
    public List<String> getKomsular(int id) { return komsuGraf.getOrDefault(id, new ArrayList<>()); }
}
