package com.emlak.gui;

import com.emlak.models.Property;
import com.emlak.services.EmlakManager;
import javafx.application.Application;
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

    // İŞTE BÜYÜ TIK BURADA: Tüm backend işlemleri bu objeye devredildi
    private EmlakManager backendManager = new EmlakManager();

    // Sadece Arayüzde (Ekranda) gösterilecek listeler
    private ObservableList<Property> favoriGosterimListesi = FXCollections.observableArrayList();
    private LinkedList<Property> sonGezilenler = new LinkedList<>();
    private ObservableList<Property> gecmisGosterimListesi = FXCollections.observableArrayList();
    private Stack<List<Property>> filtreGecmisi = new Stack<>();
    private ObservableList<String> randevuGosterimListesi = FXCollections.observableArrayList();
    private ObservableList<String> komsuGosterimListesi = FXCollections.observableArrayList();
    private ObservableList<Property> firsatEvlerListesi = FXCollections.observableArrayList();

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Emlak 360 - MVC Mimari Paneli");

        // --- SOL PANEL ---
        VBox leftPanel = new VBox(10);
        leftPanel.setPadding(new Insets(10));
        leftPanel.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #ccc; -fx-border-width: 0 1 0 0;");
        leftPanel.setPrefWidth(260);

        ComboBox<Integer> dataSizeBox = new ComboBox<>();
        dataSizeBox.getItems().addAll(1000, 50000, 500000);
        dataSizeBox.setValue(50000);
        Button btnUret = new Button("Veri Üret");
        btnUret.setMaxWidth(Double.MAX_VALUE);

        TextField txtMin = new TextField(); txtMin.setPromptText("Min Fiyat");
        TextField txtMax = new TextField(); txtMax.setPromptText("Max Fiyat");

        ComboBox<String> cmbMekan = new ComboBox<>();
        cmbMekan.getItems().addAll("Şehir Merkezi (50.0, 50.0)", "🚇 Metro İstasyonu (30.0, 70.0)", "🛒 AVM (80.0, 20.0)");
        cmbMekan.setValue("Şehir Merkezi (50.0, 50.0)");
        cmbMekan.setMaxWidth(Double.MAX_VALUE);
        TextField txtR = new TextField(); txtR.setPromptText("Yarıçap (km)");

        Button btnFiyatAra = new Button("1. Sadece Fiyat (BST)"); btnFiyatAra.setStyle("-fx-background-color: #FF9800; -fx-text-fill: white; -fx-font-weight: bold;"); btnFiyatAra.setMaxWidth(Double.MAX_VALUE); btnFiyatAra.setDisable(true);
        Button btnMekanAra = new Button("2. Sadece Konum (KD-Tree)"); btnMekanAra.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-font-weight: bold;"); btnMekanAra.setMaxWidth(Double.MAX_VALUE); btnMekanAra.setDisable(true);
        Button btnKapsamliAra = new Button("3. Konum + Fiyat (İkisi Birlikte)"); btnKapsamliAra.setStyle("-fx-background-color: #2196F3; -fx-text-fill: white; -fx-font-weight: bold;"); btnKapsamliAra.setMaxWidth(Double.MAX_VALUE); btnKapsamliAra.setDisable(true);
        Button btnUndo = new Button("⟲ Tabloyu Geri Al (Stack)"); btnUndo.setMaxWidth(Double.MAX_VALUE);

        leftPanel.getChildren().addAll(new Label("Veri Boyutu:"), dataSizeBox, btnUret, new Separator(), new Label("A. Fiyat Aralığı:"), new HBox(5, txtMin, txtMax), new Label("B. Merkez Noktası:"), cmbMekan, new Label("C. Arama Çapı (km):"), txtR, new Separator(), new Label("Arama Seçenekleri:"), btnFiyatAra, btnMekanAra, btnKapsamliAra, new Separator(), btnUndo);

        // --- ORTA PANEL ---
        VBox centerPanel = new VBox(10); centerPanel.setPadding(new Insets(10));
        TableView<Property> table = new TableView<>();
        TableColumn<Property, Integer> idCol = new TableColumn<>("İlan ID"); idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        TableColumn<Property, String> typeCol = new TableColumn<>("Tip"); typeCol.setCellValueFactory(new PropertyValueFactory<>("type"));
        TableColumn<Property, Integer> roomCol = new TableColumn<>("Oda"); roomCol.setCellValueFactory(new PropertyValueFactory<>("rooms"));
        TableColumn<Property, Double> priceCol = new TableColumn<>("Fiyat (TL)"); priceCol.setCellValueFactory(new PropertyValueFactory<>("price"));
        table.getColumns().addAll(idCol, typeCol, roomCol, priceCol); table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        HBox favActionBox = new HBox(10);
        Button btnFavoriEkle = new Button("★ Favoriye Ekle"); btnFavoriEkle.setStyle("-fx-background-color: #E91E63; -fx-text-fill: white;");
        Button btnFavoriSil = new Button("Favoriden Sil");
        favActionBox.getChildren().addAll(btnFavoriEkle, btnFavoriSil);

        HBox appointmentBox = new HBox(10);
        Button btnRandevuAl = new Button("Randevu Al (Queue)"); Button btnSirdaki = new Button("Sıradakini Çağır");
        appointmentBox.getChildren().addAll(btnRandevuAl, btnSirdaki);

        Label lblBST = new Label("Listelenen Evler Arasındaki En Ucuz 3 Fırsat (BST İle):"); lblBST.setStyle("-fx-text-fill: #D32F2F; -fx-font-weight: bold;");
        ListView<Property> firsatView = new ListView<>(firsatEvlerListesi); firsatView.setPrefHeight(80);
        centerPanel.getChildren().addAll(new Label("Arama Sonuçları"), table, favActionBox, appointmentBox, new Separator(), lblBST, firsatView);

        // --- SAĞ PANEL ---
        VBox rightPanel = new VBox(8); rightPanel.setPadding(new Insets(10)); rightPanel.setPrefWidth(300); rightPanel.setStyle("-fx-background-color: #2b2b2b;");
        Label lblPerf = new Label("PERFORMANS ANALİZİ"); lblPerf.setStyle("-fx-text-fill: #FFD700; -fx-font-weight: bold;");
        Label lblLinear = new Label("Dizi (Linear) Süresi: - ms"); lblLinear.setStyle("-fx-text-fill: #ff5252;");
        Label lblTree = new Label("Ağaç (Tree) Süresi: - ms"); lblTree.setStyle("-fx-text-fill: #69f0ae;");
        Label lblFark = new Label("Kullanılan Algoritma: -"); lblFark.setStyle("-fx-text-fill: white;");
        Label lblGraph = new Label("Çevredeki Yerler (Graph):"); lblGraph.setStyle("-fx-text-fill: #00E5FF; -fx-font-weight: bold;");
        ListView<String> graphView = new ListView<>(komsuGosterimListesi); graphView.setPrefHeight(60);
        Label lblQueue = new Label("Randevu Sırası (Queue):"); lblQueue.setStyle("-fx-text-fill: #00E5FF;");
        ListView<String> queueView = new ListView<>(randevuGosterimListesi); queueView.setPrefHeight(60);
        Label lblFav = new Label("Favoriler (HashSet):"); lblFav.setStyle("-fx-text-fill: #E91E63;");
        ListView<Property> favView = new ListView<>(favoriGosterimListesi); favView.setPrefHeight(90);
        Label lblRec = new Label("Geçmiş (LinkedList):"); lblRec.setStyle("-fx-text-fill: white;");
        ListView<Property> recView = new ListView<>(gecmisGosterimListesi); recView.setPrefHeight(90);
        rightPanel.getChildren().addAll(lblPerf, lblLinear, lblTree, lblFark, new Separator(), lblGraph, graphView, lblQueue, queueView, lblFav, favView, lblRec, recView);

        // ==========================================
        // BUTON AKSİYONLARI (Sadece Manager'ı Çağırır)
        // ==========================================

        btnUret.setOnAction(e -> {
            backendManager.sistemVerileriniUret(dataSizeBox.getValue());
            btnUret.setText("Hazır ✓");
            btnFiyatAra.setDisable(false); btnMekanAra.setDisable(false); btnKapsamliAra.setDisable(false);
        });

        btnFiyatAra.setOnAction(e -> {
            try {
                filtreGecmisi.push(new ArrayList<>(table.getItems()));
                double minF = Double.parseDouble(txtMin.getText()); double maxF = Double.parseDouble(txtMax.getText());

                long start = System.nanoTime();
                List<Property> sonuclar = backendManager.fiyataGoreAra(minF, maxF); // BACKEND'DEN ÇAĞIR
                double sure = (System.nanoTime() - start) / 1_000_000.0;

                table.setItems(FXCollections.observableArrayList(sonuclar));
                lblLinear.setText("Dizi: Hesaplanmadı"); lblTree.setText(String.format("BST Süresi: %.4f ms", sure)); lblFark.setText("Algoritma: Sadece İkili Arama Ağacı");
                firsatEvlerListesi.setAll(backendManager.ucuzFirsatlariBul(sonuclar));
            } catch (Exception ex) { new Alert(Alert.AlertType.ERROR, "Geçerli fiyat girin!").show(); }
        });

        btnMekanAra.setOnAction(e -> {
            try {
                filtreGecmisi.push(new ArrayList<>(table.getItems()));
                double tx = cmbMekan.getValue().contains("Metro") ? 30.0 : cmbMekan.getValue().contains("AVM") ? 80.0 : 50.0;
                double ty = cmbMekan.getValue().contains("Metro") ? 70.0 : cmbMekan.getValue().contains("AVM") ? 20.0 : 50.0;
                double tr = Double.parseDouble(txtR.getText());

                long start = System.nanoTime();
                List<Property> sonuclar = backendManager.konumaGoreAra(tx, ty, tr); // BACKEND'DEN ÇAĞIR
                double sure = (System.nanoTime() - start) / 1_000_000.0;

                table.setItems(FXCollections.observableArrayList(sonuclar));
                lblLinear.setText("Dizi: Hesaplanmadı"); lblTree.setText(String.format("KD-Tree Süresi: %.4f ms", sure)); lblFark.setText("Algoritma: Sadece KD-Tree");
                firsatEvlerListesi.setAll(backendManager.ucuzFirsatlariBul(sonuclar));
            } catch (Exception ex) { new Alert(Alert.AlertType.ERROR, "Geçerli çap girin!").show(); }
        });

        btnKapsamliAra.setOnAction(e -> {
            try {
                filtreGecmisi.push(new ArrayList<>(table.getItems()));
                double tx = cmbMekan.getValue().contains("Metro") ? 30.0 : cmbMekan.getValue().contains("AVM") ? 80.0 : 50.0;
                double ty = cmbMekan.getValue().contains("Metro") ? 70.0 : cmbMekan.getValue().contains("AVM") ? 20.0 : 50.0;
                double tr = Double.parseDouble(txtR.getText());
                double minF = Double.parseDouble(txtMin.getText()); double maxF = Double.parseDouble(txtMax.getText());

                long startLinear = System.nanoTime();
                backendManager.lineerAra(tx, ty, tr, minF, maxF, true);
                double sureLinear = (System.nanoTime() - startLinear) / 1_000_000.0;

                long startTree = System.nanoTime();
                List<Property> sonuclar = backendManager.kapsamliAra(tx, ty, tr, minF, maxF); // BACKEND'DEN ÇAĞIR
                double sureTree = (System.nanoTime() - startTree) / 1_000_000.0;

                table.setItems(FXCollections.observableArrayList(sonuclar));
                lblLinear.setText(String.format("Dizi Süresi: %.3f ms", sureLinear)); lblTree.setText(String.format("KD+BST Süresi: %.3f ms", sureTree));
                if(sureTree > 0) lblFark.setText(String.format("Kesişim, Diziden %.1f kat daha hızlı!", sureLinear/sureTree));
                firsatEvlerListesi.setAll(backendManager.ucuzFirsatlariBul(sonuclar));
            } catch (Exception ex) { new Alert(Alert.AlertType.ERROR, "Lütfen kutuları doldurun!").show(); }
        });

        btnUndo.setOnAction(e -> { if (!filtreGecmisi.isEmpty()) table.setItems(FXCollections.observableArrayList(filtreGecmisi.pop())); });

        btnFavoriEkle.setOnAction(e -> {
            Property selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (backendManager.favoriEkle(selected.getId())) favoriGosterimListesi.add(selected);
                else new Alert(Alert.AlertType.WARNING, "Zaten favorilerde!").show();
            }
        });

        btnFavoriSil.setOnAction(e -> {
            Property selected = favView.getSelectionModel().getSelectedItem();
            if (selected != null) { backendManager.favoriSil(selected.getId()); favoriGosterimListesi.remove(selected); }
        });

        btnRandevuAl.setOnAction(e -> {
            Property p = table.getSelectionModel().getSelectedItem();
            if (p != null) {
                String req = "İlan #" + p.getId() + " - " + p.getType() + " Randevu";
                backendManager.randevuTalebiEkle(req); randevuGosterimListesi.add(req);
            }
        });

        btnSirdaki.setOnAction(e -> {
            String s = backendManager.siradakiRandevuyuCagir();
            if (s != null) { randevuGosterimListesi.remove(0); new Alert(Alert.AlertType.INFORMATION, s + " çağrıldı!").show(); }
        });

        table.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                sonGezilenler.remove(newV); sonGezilenler.addFirst(newV); if (sonGezilenler.size() > 5) sonGezilenler.removeLast();
                gecmisGosterimListesi.setAll(sonGezilenler);
                komsuGosterimListesi.setAll(backendManager.getKomsular(newV.getId())); // BACKEND'DEN KOMSULARI ÇEK
            }
        });

        primaryStage.setScene(new Scene(new BorderPane(centerPanel, null, rightPanel, null, leftPanel), 1250, 750));
        primaryStage.show();
    }

    public static void main(String[] args) { launch(args); }
}