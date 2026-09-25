# Emlak Yönetim Sistemi - Gelişmiş Veri Yapıları ve Algoritma Analizi 🏢

## 📌 Genel Bakış
Bu proje, 500.000 adet rastgele üretilmiş emlak ilanını bellekte minimum zaman ve alan karmaşıklığı ile yönetmek amacıyla geliştirilmiştir. Sistem, büyük veri kümeleri üzerinde x-y koordinatlarına göre dairesel arama veya fiyat aralığı sorgusu gibi karmaşık işlemleri milisaniyeler içinde gerçekleştirebilmektedir. Standart doğrusal veri yapılarının (diziler) sebep olduğu arayüz donmaları ve işlemci darboğazları, projede uygulanan ağaç tabanlı algoritmalar ile aşılmıştır.

## 🏗️ Sistem Mimarisi
Uygulama, "Gevşek Bağlılık" (Loose Coupling) ve "Sorumlulukların Ayrılığı" (Separation of Concerns) ilkelerine tam uyumlu olarak MVC (Model-View-Controller) mimarisiyle tasarlanmıştır.
*   **Model Katmanı (Property):** İlan ID'si, x-y koordinatları, fiyat, tip ve oda sayısı gibi temel nesne verilerini tutar.
*   **Controller Katmanı (EmlakManager):** KD-Tree tabanlı mekansal aramalar, BST üzerinden yapılan fiyat filtrelemeleri ve randevu kuyruğu yönetimi (Queue) bu merkezde işlenir.
*   **View Katmanı (EmlakArayuz):** JavaFX ile tasarlanan kullanıcı arayüzüdür. Controller katmanına sadece komutlar gönderir ve dönen yanıtları ekrana yansıtır.
*   **Yardımcı Servis (MockDataGenerator):** Sistemin performansını test etmek için 1.000, 50.000 ve 500.000 boyutlarında gerçekçi sahte ilan nesneleri (Mock Data) üreterek bir veritabanı simülasyonu sağlar.

## 🧱 Kullanılan Veri Yapıları ve Zaman Karmaşıklıkları
*   **K-Boyutlu Ağaç (KD-Tree):** İlanların uzayda x-y koordinatlarına göre hiyerarşik konumlandırılmasını sağlar. Belirli bir yarıçap içindeki evleri bulurken bölgesel budama (pruning) uygular. Bu sayede arama maliyeti O(log N) seviyesine düşürülmüştür.
*   **İkili Arama Ağacı (BST / TreeSet):** Belirli fiyat aralığındaki evlerin filtrelenmesi için kullanılmıştır. Verilerin kendiliğinden sıralı tutulması sayesinde "En Ucuz 3 Fırsat Evi"nin bulunmasında Doğal Min-Heap ve Öncelikli Kuyruk gibi davranır. Arama ve ekleme işlemleri O(log N) karmaşıklığıyla çalışır.
*   **Çizge (Graph / HashMap):** İlanlar ile çevresindeki önemli merkezler (Metro, AVM, Hastane) arasındaki mekansal ilişkileri tutmak için tasarlanmıştır. Matris yerine HashMap kullanılarak düğümlere O(1) süresinde erişim sağlanmıştır.
*   **Yığın (Stack):** LIFO (Son Giren İlk Çıkar) mantığı ile kullanıcıların filtre hatalarını geri alması (Undo) amacıyla uygulanmıştır. Ekleme ve silme işlemleri O(1) maliyetindedir.
*   **Kuyruk (Queue):** FIFO (İlk Giren İlk Çıkar) prensibiyle adaletli bir emlak randevu sistemi yönetimi için kullanılmıştır. Ekleme ve çıkarma maliyeti O(1)'dir.
*   **Küme (HashSet):** "Favorilerim" özelliğinde bir evin birden fazla kez eklenmesini önlemek için tercih edilmiştir. Liste taramadan, hash tabanlı çakışma kontrolü sayesinde işlem O(1) sürede tamamlanır.
*   **Bağlı Liste (LinkedList):** "Son Gezilen İlanlar" (5 ilan limiti) geçmişini tutmak için kullanılmıştır. Dizilerin yarattığı kaydırma maliyeti olmadan baştan/sondan eleman silinmesi işlemleri O(1) karmaşıklığında gerçekleştirilir.
*   **Dinamik Dizi (ArrayList):** Ağaç tabanlı algoritmaların başarısını test etmek için kıyaslama (Lineer arama: O(N)) ortamı oluşturmak amacıyla kullanılmıştır.

## ⚙️ Algoritmik Yaklaşım
1.  **Mekansal Arama:** KD-Tree algoritması kullanılarak kök düğümden itibaren merkez noktaya olan Öklid uzaklığı hesaplanır ve yarıçap dışı kalan bölgeler budanır.
2.  **Fiyat Filtreleme:** İlanlar fiyata göre sıralanıp BST yapısına yerleştirilir, böylece minimum ve maksimum fiyat aralığındaki evler logaritmik sürede bulunur.
3.  **Kapsamlı Arama (Kesişim):** Hem konum hem fiyat filtresi uygulandığında, ilk olarak KD-Tree ile bölgedeki evler bulunur. Ardından bu evlerin BST'de var olup olmadığı kontrol edilerek kesişim hızlıca alınır.
4.  **Önceliklendirme:** Filtrelenen evler arasından "En Ucuz 3 Fırsat"ı listelemek için TreeSet tabanlı önceliklendirme ile kayıtlar küçükten büyüğe sıralanır ve iteratör ile O(1) süresinde çekilir.

## 🚀 Performans Analizi ve Önbellek Yerelliği (Cache Locality)
Sistemin algoritmik performansı üç farklı senaryo altında analiz edilmiştir:
*   **Küçük Veri (1.000 Kayıt):** KD-Tree ve BST hibrit modeli (0.089 ms), lineer diziden (0.512 ms) 5.7 kat daha hızlı sonuç vermiştir.
*   **Orta Veri (50.000 Kayıt):** Ağaç yapısı (1.756 ms), dizilere (5.766 ms) karşı 3.3 kat hız avantajı sağlamıştır.
*   **Büyük Veri (500.000 Kayıt):** Ağaç yapısının (16.824 ms), diziye (25.807 ms) kıyasla avantajı 1.5 kata düşmüştür.

**Performans Farkının Azalma Sebebi (Önbellek Yerelliği):**
Kayıt sayısı 500.000'e ulaştığında ağaç yapılarının teorik avantajının azalmasının sebebi donanımsal CPU "Önbellek Yerelliği" (Cache Locality) prensibidir.
*   ArrayList verileri bellekte ardışık bloklar halinde tuttuğu için CPU diziyi okurken verileri toplu halde önbelleğine alır ve matematiksel işlemleri çok hızlı yapar.
*   KD-Tree ve BST düğümleri ise bellekte rastgele adreslerde tutulduğundan, büyük veride ağaç yapısı önbelleğe sığmaz. Bu durum sürekli "Cache Miss" (Önbellek İsabet Hatası) yaşanmasına sebep olarak ağaç tabanlı algoritmaların süresini uzatır.

