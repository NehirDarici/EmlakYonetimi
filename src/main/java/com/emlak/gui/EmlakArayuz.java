package com.emlak.gui;

import com.emlak.datastructures.KDTree;
import com.emlak.models.Property;
import com.emlak.services.MockDataGenerator;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.*;

public class EmlakArayuz extends Application {

    private List<Property> mevcutVeri = new ArrayList<>();
    private KDTree kdTree = new KDTree();

    // Veri Yapıları
    private HashSet<Integer> favoriIdSet = new HashSet<>();
    private ObservableList<Property> favoriGosterimListesi = FXCollections.observableArrayList();

    private LinkedList<Property> sonGezilenler = new LinkedList<>();
    private ObservableList<Property> gecmisGosterimListesi = FXCollections.observableArrayList();

    private Stack<List<Property>> filtreGecmisi = new Stack<>();

    private Queue<String> randevuKuyrugu = new LinkedList<>();
    private ObservableList<String> randevuGosterimListesi = FXCollections.observableArrayList();

    private TreeSet<Property> fiyatAgaciBST = new TreeSet<>(Comparator.comparingDouble(Property::getPrice).thenComparingInt(Property::getId));

    // GRAF (Çevredeki Yerler) İçin Değişkenler
    private Map<Integer, List<String>> komsuGraf = new HashMap<>();
    private ObservableList<String> komsuGosterimListesi = FXCollections.observableArrayList();

    // Priority Queue (Heap) Listesi
    private ObservableList<Property> firsatEvlerListesi = FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Emlak 360 - Profesyonel Veri Yapıları Paneli (11/11)");

        // --- SOL PANEL: KONTROLLER ---
        VBox leftPanel = new VBox(10);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #ccc; -fx-border-width: 0 1 0 0;");
        leftPanel.setPrefWidth(220);

        ComboBox<Integer> dataSizeBox = new ComboBox<>();
        dataSizeBox.getItems().addAll(1000, 50000, 500000);
        dataSizeBox.setValue(50000);
        Button btnUret = new Button("Veri Üret");
        btnUret.setMaxWidth(Double.MAX_VALUE);

        Separator s1 = new Separator();
        TextField txtMin = new TextField("2000000"); TextField txtMax = new TextField("4000000");
        Button btnBST = new Button("BST Fiyat Filtre");
        btnBST.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white;");
        btnBST.setMaxWidth(Double.MAX_VALUE);

        Button btnUndo = new Button("⟲ Geri Al (Stack)");
        btnUndo.setMaxWidth(Double.MAX_VALUE);

        Separator s2 = new Separator();
        TextField txtX = new TextField("50.0"); TextField txtY = new TextField("50.0"); TextField txtR = new TextField("5.0");
        Button btnAra = new Button("Hız Karşılaştırmalı Ara");
        btnAra.setMaxWidth(Double.MAX_VALUE);
        btnAra.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;");

        leftPanel.getChildren().addAll(
                new Label("1. Veri Boyutu:"), dataSizeBox, btnUret, s1,
                new Label("2. Fiyat (BST):"), txtMin, txtMax, btnBST, btnUndo, s2,
                new Label("3. Mekansal (KD-Tree):"), new HBox(5, txtX, txtY), new Label("Çap:"), txtR, btnAra
        );

        // --- ORTA PANEL: TABLO VE KUYRUK ---
        VBox centerPanel = new VBox(10);
        centerPanel.setPadding(new Insets(10));

        TableView<Property> table = new TableView<>();
        TableColumn<Property, Integer> idCol = new TableColumn<>("ID"); idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Property, Double> priceCol = new TableColumn<>("Fiyat"); priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        table.getColumns().addAll(idCol, priceCol);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        HBox favActionBox = new HBox(10);
        Button btnFavoriEkle = new Button("★ Favoriye Ekle"); btnFavoriEkle.setStyle("-fx-background-color: #E91E63; -fx-text-fill: white;");
        Button btnFavoriSil = new Button("Favoriden Sil");
        favActionBox.getChildren().addAll(btnFavoriEkle, btnFavoriSil);

        HBox appointmentBox = new HBox(10);
        Button btnRandevuAl = new Button("Randevu Al (Queue)");
        Button btnSirdaki = new Button("Sıradakini Çağır");
        appointmentBox.getChildren().addAll(btnRandevuAl, btnSirdaki);

        Label lblPQ = new Label("Bölgedeki En Ucuz 3 Fırsat (Priority Queue / Heap):");
        lblPQ.setStyle("-fx-text-fill: #D32F2F; -fx-font-weight: bold;");
        ListView<Property> firsatView = new ListView<>(firsatEvlerListesi); firsatView.setPrefHeight(80);

        centerPanel.getChildren().addAll(new Label("Arama Sonuçları"), table, favActionBox, appointmentBox, new Separator(), lblPQ, firsatView);

        // --- SAĞ PANEL: GRAF, ANALİZ VE GEÇMİŞ ---
        VBox rightPanel = new VBox(8);
        rightPanel.setPadding(new Insets(10));
        rightPanel.setPrefWidth(300);
        rightPanel.setStyle("-fx-background-color: #2b2b2b;");

        Label lblPerf = new Label("PERFORMANS ANALİZİ"); lblPerf.setStyle("-fx-text-fill: #FFD700; -fx-font-weight: bold;");
        Label lblLinear = new Label("Dizi (Linear) Süresi: - ms"); lblLinear.setStyle("-fx-text-fill: #ff5252;");
        Label lblTree = new Label("KD-Tree Süresi: - ms"); lblTree.setStyle("-fx-text-fill: #69f0ae;");
        Label lblFark = new Label("Hız Farkı: -"); lblFark.setStyle("-fx-text-fill: white;");

        // YENİDEN EKLENEN GRAF BÖLÜMÜ
        Label lblGraph = new Label("Çevredeki Yerler (Graph Kenarları):"); lblGraph.setStyle("-fx-text-fill: #00E5FF; -fx-font-weight: bold;");
        ListView<String> graphView = new ListView<>(komsuGosterimListesi); graphView.setPrefHeight(60);

        Label lblQueue = new Label("Randevu Sırası (Queue):"); lblQueue.setStyle("-fx-text-fill: #00E5FF;");
        ListView<String> queueView = new ListView<>(randevuGosterimListesi); queueView.setPrefHeight(60);

        Label lblFav = new Label("Favoriler (HashSet):"); lblFav.setStyle("-fx-text-fill: #E91E63;");
        ListView<Property> favView = new ListView<>(favoriGosterimListesi); favView.setPrefHeight(90);

        Label lblRec = new Label("Geçmiş (LinkedList):"); lblRec.setStyle("-fx-text-fill: white;");
        ListView<Property> recView = new ListView<>(gecmisGosterimListesi); recView.setPrefHeight(90);

        // TÜM LİSTELER SAĞ PANELE EKLENDİ
        rightPanel.getChildren().addAll(lblPerf, lblLinear, lblTree, lblFark, new Separator(), lblGraph, graphView, lblQueue, queueView, lblFav, favView, lblRec, recView);

        // ==========================================
        // BUTON AKSİYONLARI VE ETKİLEŞİMLER
        // ==========================================

        btnUret.setOnAction(e -> {
            mevcutVeri = MockDataGenerator.generateData(dataSizeBox.getValue());
            kdTree = new KDTree();
            fiyatAgaciBST.clear();
            komsuGraf.clear(); // Grafı temizle

            // Graf için rastgele mekanlar
            String[] mekanlar = {"🚇 Metro İstasyonu (200m)", "🏥 Şehir Hastanesi (1km)", "🏫 İlkokul (400m)", "🛒 AVM (2km)", "🌳 Şehir Parkı (100m)", "🚌 Otobüs Durağı (50m)"};
            Random rand = new Random();

            for (Property p : mevcutVeri) {
                kdTree.insert(p);
                fiyatAgaciBST.add(p);

                // Grafa komşuları ekle
                List<String> komsular = new ArrayList<>();
                komsular.add(mekanlar[rand.nextInt(mekanlar.length)]);
                komsular.add(mekanlar[rand.nextInt(mekanlar.length)]);
                komsuGraf.put(p.getId(), komsular);
            }
            btnUret.setText("Hazır ✓");
            btnAra.setDisable(false);
        });

        // FAVORİ EKLEME VE SİLME
        btnFavoriEkle.setOnAction(e -> {
            Property selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (favoriIdSet.add(selected.getId())) {
                    favoriGosterimListesi.add(selected);
                } else {
                    new Alert(Alert.AlertType.WARNING, "Zaten favorilerde!").show();
                }
            }
        });

        btnFavoriSil.setOnAction(e -> {
            Property selected = favView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                favoriIdSet.remove(selected.getId());
                favoriGosterimListesi.remove(selected);
            }
        });

        // HIZ KARŞILAŞTIRMALI ARAMA (Dizi vs KD-Tree)
        btnAra.setOnAction(e -> {
            double tx = Double.parseDouble(txtX.getText());
            double ty = Double.parseDouble(txtY.getText());
            double tr = Double.parseDouble(txtR.getText());

            long startLinear = System.nanoTime();
            List<Property> r1 = new ArrayList<>();
            for (Property p : mevcutVeri) {
                if (Math.sqrt(Math.pow(p.getX()-tx,2) + Math.pow(p.getY()-ty,2)) <= tr) r1.add(p);
            }
            double timeLinear = (System.nanoTime() - startLinear) / 1_000_000.0;

            long startTree = System.nanoTime();
            List<Property> r2 = kdTree.findInRadius(tx, ty, tr);
            double timeTree = (System.nanoTime() - startTree) / 1_000_000.0;

            table.setItems(FXCollections.observableArrayList(r2));
            lblLinear.setText(String.format("Dizi (Linear) Süresi: %.3f ms", timeLinear));
            lblTree.setText(String.format("KD-Tree Süresi: %.3f ms", timeTree));
            if (timeTree > 0) lblFark.setText(String.format("KD-Tree, %.1f kat daha hızlı!", timeLinear/timeTree));

            // PRIORITY QUEUE (En Ucuz 3 Fırsat)
            PriorityQueue<Property> pq = new PriorityQueue<>(Comparator.comparingDouble(Property::getPrice));
            pq.addAll(r2);
            firsatEvlerListesi.clear();
            for(int i=0; i<3 && !pq.isEmpty(); i++) firsatEvlerListesi.add(pq.poll());
        });

        // RANDEVU (Queue) İŞLEMLERİ
        btnRandevuAl.setOnAction(e -> {
            Property p = table.getSelectionModel().getSelectedItem();
            if (p != null) {
                String req = "İlan #" + p.getId() + " için Randevu";
                randevuKuyrugu.offer(req);
                randevuGosterimListesi.add(req);
            }
        });

        btnSirdaki.setOnAction(e -> {
            String s = randevuKuyrugu.poll();
            if (s != null) {
                randevuGosterimListesi.remove(0);
                new Alert(Alert.AlertType.INFORMATION, s + " çağrıldı!").show();
            }
        });

        // TIKLAMA İŞLEMLERİ: GEÇMİŞ (LinkedList) VE GRAF (Graph)
        table.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                // LinkedList Geçmiş
                sonGezilenler.remove(newV);
                sonGezilenler.addFirst(newV);
                if (sonGezilenler.size() > 5) sonGezilenler.removeLast();
                gecmisGosterimListesi.setAll(sonGezilenler);

                // Graf Komşuları Güncelle
                if (komsuGraf.containsKey(newV.getId())) {
                    komsuGosterimListesi.setAll(komsuGraf.get(newV.getId()));
                }
            }
        });

        btnBST.setOnAction(e -> {
            filtreGecmisi.push(new ArrayList<>(table.getItems()));
            double min = Double.parseDouble(txtMin.getText()); double max = Double.parseDouble(txtMax.getText());
            table.setItems(FXCollections.observableArrayList(fiyatAgaciBST.subSet(new Property(0,0,0,min,""), new Property(999999,0,0,max,""))));
        });

        btnUndo.setOnAction(e -> { if (!filtreGecmisi.isEmpty()) table.setItems(FXCollections.observableArrayList(filtreGecmisi.pop())); });

        primaryStage.setScene(new Scene(new BorderPane(centerPanel, null, rightPanel, null, leftPanel), 1250, 750));
        primaryStage.show();
    }

    public static void main(String[] args) { launch(args); }
}