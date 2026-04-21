package com.emlak.gui;

import com.emlak.datastructures.KDTree;
import com.emlak.models.Property;
import com.emlak.services.MockDataGenerator;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.*;

public class EmlakApp extends Application {

    // --- KİŞİ 1'İN VERİ YAPILARI (ÇEKİRDEK) ---
    private KDTree kdTree = new KDTree();
    private List<Property> tumIlanlar;

    // --- KİŞİ 2'NİN VERİ YAPILARI (ARAYÜZ İÇİN) ---
    private Stack<List<Property>> tabloGecmisi = new Stack<>();
    private Queue<String> randevuKuyrugu = new LinkedList<>();
    private HashSet<Integer> favoriIlanlar = new HashSet<>();
    // BST (İkili Arama Ağacı) için Java'nın TreeSet yapısını kullanıyoruz (Fiyata göre sıralı)
    private TreeSet<Property> bstFiyatAgaci = new TreeSet<>(Comparator.comparingDouble(Property::getPrice));

    private ObservableList<Property> tabloVerisi = FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage) {
        System.out.println("Arayüz Yükleniyor... 500.000 Veri Üretiliyor...");
        tumIlanlar = MockDataGenerator.generateData(500000);
        for (Property p : tumIlanlar) {
            kdTree.insert(p);
        }
        System.out.println("Veriler KD-Tree'ye başarıyla yüklendi!");

        primaryStage.setTitle("Akıllı Şehir Gayrimenkul Bilgi Sistemi");
        BorderPane anaPanel = new BorderPane();

        // ==========================================
        // SOL PANEL (Filtreler, Stack ve Queue)
        // ==========================================
        VBox solPanel = new VBox(15);
        solPanel.setStyle("-fx-padding: 20; -fx-background-color: #f4f4f4; -fx-border-color: #cccccc; -fx-border-width: 0 1 0 0;");
        solPanel.setPrefWidth(260);

        Label lblFiltre = new Label("Filtreleme & İşlemler");
        lblFiltre.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

        // 1. Stack İşlemi İçin Buton
        Button btnGeriAl = new Button("Son İşlemi Geri Al (Stack)");
        btnGeriAl.setMaxWidth(Double.MAX_VALUE);

        // 2. BST İşlemi İçin Fiyat Kutuları
        TextField txtMinFiyat = new TextField(); txtMinFiyat.setPromptText("Min Fiyat (TL)");
        TextField txtMaxFiyat = new TextField(); txtMaxFiyat.setPromptText("Max Fiyat (TL)");
        Button btnFiyatFiltrele = new Button("Fiyata Göre Filtrele (BST)");
        btnFiyatFiltrele.setMaxWidth(Double.MAX_VALUE);

        // 3. Queue İşlemi İçin Randevu Listesi
        Label lblRandevu = new Label("Gelen Randevular (Queue):");
        ListView<String> listRandevular = new ListView<>();
        listRandevular.setPrefHeight(150);

        solPanel.getChildren().addAll(
                lblFiltre, btnGeriAl, new Separator(),
                new Label("Fiyat Aralığı:"), txtMinFiyat, txtMaxFiyat, btnFiyatFiltrele,
                new Separator(), lblRandevu, listRandevular
        );
        anaPanel.setLeft(solPanel);

        // ==========================================
        // ÜST PANEL (Hızlı Arama Çubuğu)
        // ==========================================
        HBox ustPanel = new HBox(10);
        ustPanel.setStyle("-fx-padding: 15; -fx-background-color: #2c3e50;");

        Label lblArama = new Label("Mekansal Arama:");
        lblArama.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14;");
        TextField txtX = new TextField(); txtX.setPromptText("X Koordinatı"); txtX.setPrefWidth(100);
        TextField txtY = new TextField(); txtY.setPromptText("Y Koordinatı"); txtY.setPrefWidth(100);
        TextField txtCap = new TextField(); txtCap.setPromptText("Yarıçap (km)"); txtCap.setPrefWidth(100);
        Button btnMekansalAra = new Button("KD-Tree ile Ara");
        btnMekansalAra.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");

        ustPanel.getChildren().addAll(lblArama, txtX, txtY, txtCap, btnMekansalAra);
        anaPanel.setTop(ustPanel);

        // ==========================================
        // ORTA PANEL (Sonuç Tablosu ve HashSet/Queue Menüsü)
        // ==========================================
        TableView<Property> sonucTablosu = new TableView<>();
        TableColumn<Property, Integer> colId = new TableColumn<>("İlan ID"); colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Property, Double> colX = new TableColumn<>("X"); colX.setCellValueFactory(new PropertyValueFactory<>("x"));
        TableColumn<Property, Double> colY = new TableColumn<>("Y"); colY.setCellValueFactory(new PropertyValueFactory<>("y"));
        TableColumn<Property, Double> colFiyat = new TableColumn<>("Fiyat (TL)"); colFiyat.setCellValueFactory(new PropertyValueFactory<>("price")); colFiyat.setPrefWidth(120);
        TableColumn<Property, String> colTip = new TableColumn<>("Tip"); colTip.setCellValueFactory(new PropertyValueFactory<>("type"));
        TableColumn<Property, Integer> colOda = new TableColumn<>("Oda"); colOda.setCellValueFactory(new PropertyValueFactory<>("rooms"));

        sonucTablosu.getColumns().addAll(colId, colX, colY, colFiyat, colTip, colOda);
        sonucTablosu.setItems(tabloVerisi);

        // SAĞ TIK MENÜSÜ (Queue ve HashSet tetikleyicileri)
        ContextMenu contextMenu = new ContextMenu();
        MenuItem menuRandevu = new MenuItem("Randevu Talep Et (Queue)");
        MenuItem menuFavori = new MenuItem("Favorilere Ekle (HashSet)");
        contextMenu.getItems().addAll(menuRandevu, menuFavori);
        sonucTablosu.setContextMenu(contextMenu);

        VBox ortaPanel = new VBox(10);
        ortaPanel.setStyle("-fx-padding: 10;");
        Label lblTabloBaslik = new Label("Bulunan İlanlar");
        lblTabloBaslik.setStyle("-fx-font-weight: bold;");
        VBox.setVgrow(sonucTablosu, Priority.ALWAYS);
        ortaPanel.getChildren().addAll(lblTabloBaslik, sonucTablosu);
        anaPanel.setCenter(ortaPanel);

        // ==========================================
        // SAĞ PANEL (Performans Analizi)
        // ==========================================
        VBox sagPanel = new VBox(10);
        sagPanel.setStyle("-fx-padding: 20; -fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-width: 0 0 0 1;");
        sagPanel.setPrefWidth(300);

        Label lblPerformans = new Label("Performans Analizi (Süre)");
        lblPerformans.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");
        Label lblSureKD = new Label("KD-Tree Süresi: Bekleniyor..."); lblSureKD.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
        Label lblSureDizi = new Label("Dizi (Array) Süresi: Bekleniyor..."); lblSureDizi.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");

        sagPanel.getChildren().addAll(lblPerformans, lblSureKD, lblSureDizi);
        anaPanel.setRight(sagPanel);

        // ==========================================
        // OLAYLAR (EVENTS) VE ALGORİTMALAR
        // ==========================================

        // 1. MEKANSAL ARAMA (KD-TREE)
        btnMekansalAra.setOnAction(e -> {
            try {
                double targetX = Double.parseDouble(txtX.getText());
                double targetY = Double.parseDouble(txtY.getText());
                double radius = Double.parseDouble(txtCap.getText());

                long startDizi = System.nanoTime();
                List<Property> diziSonuclari = new ArrayList<>();
                for (Property p : tumIlanlar) {
                    double dist = Math.sqrt(Math.pow(p.getX() - targetX, 2) + Math.pow(p.getY() - targetY, 2));
                    if (dist <= radius) diziSonuclari.add(p);
                }
                double diziSureMs = (System.nanoTime() - startDizi) / 1_000_000.0;

                long startKD = System.nanoTime();
                List<Property> kdSonuclari = kdTree.findInRadius(targetX, targetY, radius);
                double kdSureMs = (System.nanoTime() - startKD) / 1_000_000.0;

                tabloVerisi.setAll(kdSonuclari);
                lblSureKD.setText("KD-Tree Süresi: " + String.format("%.4f", kdSureMs) + " ms");
                lblSureDizi.setText("Dizi Süresi: " + String.format("%.4f", diziSureMs) + " ms");
                lblTabloBaslik.setText("Bulunan İlanlar (" + kdSonuclari.size() + " adet ev listelendi)");

                tabloGecmisi.push(new ArrayList<>(tabloVerisi));
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Lütfen koordinatları sayısal değer olarak girin!").show();
            }
        });

        // 2. FİYATA GÖRE FİLTRELEME (İkili Arama Ağacı - BST)
        btnFiyatFiltrele.setOnAction(e -> {
            try {
                double minFiyat = Double.parseDouble(txtMinFiyat.getText());
                double maxFiyat = Double.parseDouble(txtMaxFiyat.getText());

                // Sisteme BST'yi yüklüyoruz (Normalde başlangıçta yüklenir ama hafıza için burada güncelliyoruz)
                bstFiyatAgaci.clear();
                bstFiyatAgaci.addAll(tabloVerisi); // Sadece tablodaki (bulunan) evleri fiyata göre ağaca diz

                // Ağacın subSet (Alt Küme) özelliğini kullanarak O(log N) hızında aralığı buluyoruz
                Property dummyMin = new Property(0, 0, 0, minFiyat, "", 0);
                Property dummyMax = new Property(0, 0, 0, maxFiyat, "", 0);

                // Ağaçtan filtrelenmiş verileri çek
                SortedSet<Property> filtrelenmis = bstFiyatAgaci.subSet(dummyMin, true, dummyMax, true);

                tabloVerisi.setAll(filtrelenmis);
                lblTabloBaslik.setText("Bulunan İlanlar (" + filtrelenmis.size() + " adet ev listelendi)");
                tabloGecmisi.push(new ArrayList<>(tabloVerisi));
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Lütfen geçerli fiyat aralıkları girin!").show();
            }
        });

        // 3. STACK (Gerçek Geri Al) İŞLEMİ
        btnGeriAl.setOnAction(e -> {
            if (!tabloGecmisi.isEmpty()) {
                // Yığından bir önceki tablo durumunu çıkar (LIFO)
                List<Property> oncekiDurum = tabloGecmisi.pop();

                // Tabloyu fiziksel olarak o eski duruma geri getir
                tabloVerisi.setAll(oncekiDurum);
                lblTabloBaslik.setText("Bulunan İlanlar (" + oncekiDurum.size() + " adet ev listelendi)");

            } else {
                new Alert(Alert.AlertType.WARNING, "Geri alınacak bir işlem bulunmuyor! (Stack Boş)").show();
            }
        });

        // 4. QUEUE (Randevu Kuyruğu) İŞLEMİ
        menuRandevu.setOnAction(e -> {
            Property secilenEv = sonucTablosu.getSelectionModel().getSelectedItem();
            if (secilenEv != null) {
                String talep = "Randevu: ID " + secilenEv.getId() + " - " + secilenEv.getPrice() + " TL";
                randevuKuyrugu.add(talep); // Arkadaki kuyruğa (Queue) ekle (FIFO)
                listRandevular.getItems().add(talep); // Ekrandaki listeye ekle
            }
        });

        // 5. HASHSET (Favorilere Ekleme) İŞLEMİ
        menuFavori.setOnAction(e -> {
            Property secilenEv = sonucTablosu.getSelectionModel().getSelectedItem();
            if (secilenEv != null) {
                // HashSet'in add metodu veri zaten varsa "false" döner (Tekrarsız veri)
                boolean eklendiMi = favoriIlanlar.add(secilenEv.getId());
                if (eklendiMi) {
                    new Alert(Alert.AlertType.INFORMATION, "İlan " + secilenEv.getId() + " favorilerinize eklendi!").show();
                } else {
                    new Alert(Alert.AlertType.WARNING, "Bu ilan zaten favorilerinizde ekli! (HashSet kuralı)").show();
                }
            }
        });

        Scene scene = new Scene(anaPanel, 1200, 700);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}