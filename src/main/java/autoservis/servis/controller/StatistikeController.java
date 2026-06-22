package autoservis.servis.controller;

import autoservis.servis.dao.ArtikalDao;
import autoservis.servis.dao.KlijentDao;
import autoservis.servis.dao.RadniNalogDao;
import autoservis.servis.dao.VoziloDao;
import autoservis.servis.model.Klijent;
import autoservis.servis.model.NalogArtikal;
import autoservis.servis.model.RadniNalog;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class StatistikeController {

    private static final Logger logger = Logger.getLogger(StatistikeController.class.getName());

    private BorderPane view;
    private RadniNalogDao nalogDao;
    private KlijentDao klijentDao;
    private ArtikalDao artikalDao;
    private VoziloDao voziloDao;

    public StatistikeController() {
        this.nalogDao = new RadniNalogDao();
        this.klijentDao = new KlijentDao();
        this.artikalDao = new ArtikalDao();
        this.voziloDao = new VoziloDao();
        this.view = new BorderPane();
        kreirajUI();
    }

    public BorderPane getView() {
        return view;
    }

    private void kreirajUI() {
        HBox topbar = new HBox(10);
        topbar.getStyleClass().add("topbar");
        topbar.setAlignment(Pos.CENTER_LEFT);

        Label breadcrumb = new Label("Početna / ");
        breadcrumb.getStyleClass().add("topbar-title");
        Label bold = new Label("Statistike");
        bold.getStyleClass().add("topbar-title-bold");
        topbar.getChildren().addAll(breadcrumb, bold);

        // Ucitaj naloge jednom, koristi u svim sekcijama
        List<RadniNalog> sviNalozi = new ArrayList<>();
        try {
            sviNalozi = nalogDao.vratiSve();
        } catch (SQLException e) {
            logger.warning("Greška pri učitavanju naloga za statistike: " + e.getMessage());
            prikaziGresku("Greška pri učitavanju podataka. Statistike mogu biti nepotpune.");
        }

        VBox content = new VBox(20);
        content.getStyleClass().add("content-area");

        // ── STAT KARTICE ──────────────────────────────────────
        int ukupnoNaloga = sviNalozi.size();
        int aktivnih = 0, zavrsenih = 0;
        for (RadniNalog rn : sviNalozi) {
            if ("Završeno".equals(rn.getStatus())) zavrsenih++;
            else aktivnih++;
        }

        int ukupnoKlijenata = 0;
        try {
            ukupnoKlijenata = klijentDao.vratiSve().size();
        } catch (SQLException e) {
            logger.warning("Greška pri učitavanju klijenata: " + e.getMessage());
        }

        int ukupnoVozila = 0;
        try {
            ukupnoVozila = voziloDao.brojiAktivna();
        } catch (SQLException e) {
            logger.warning("Greška pri učitavanju vozila: " + e.getMessage());
        }

        HBox statovi = new HBox(14);
        statovi.getChildren().addAll(
                kreirajStatKarticu("Ukupno naloga", String.valueOf(ukupnoNaloga), "stat-card-blue"),
                kreirajStatKarticu("Aktivnih naloga", String.valueOf(aktivnih), "stat-card-orange"),
                kreirajStatKarticu("Završenih naloga", String.valueOf(zavrsenih), "stat-card-green"),
                kreirajStatKarticu("Ukupno klijenata", String.valueOf(ukupnoKlijenata), "stat-card-blue"),
                kreirajStatKarticu("Ukupno vozila", String.valueOf(ukupnoVozila), "stat-card-blue")
        );

        // ── GRAFIKON PO MESECIMA ──────────────────────────────
        Label lblGrafikon = new Label("Nalozi po mesecima");
        lblGrafikon.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0a1628;");

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Broj naloga");
        xAxis.setLabel("Mesec");

        BarChart<String, Number> grafikon = new BarChart<>(xAxis, yAxis);
        grafikon.setLegendVisible(false);
        grafikon.setPrefHeight(280);
        grafikon.setStyle("-fx-background-color: white; -fx-border-color: #d0d9e6; " +
                "-fx-border-radius: 8; -fx-background-radius: 8;");

        XYChart.Series<String, Number> serija = new XYChart.Series<>();
        try {
            Map<String, Long> poMesecima = new LinkedHashMap<>();
            java.time.LocalDate danas = java.time.LocalDate.now();
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            DateTimeFormatter mesecFmt = DateTimeFormatter.ofPattern("MM/yyyy");

            for (int i = 11; i >= 0; i--) {
                poMesecima.put(danas.minusMonths(i).format(mesecFmt), 0L);
            }
            for (RadniNalog rn : sviNalozi) {
                if (rn.getDatumPrijema() != null && !rn.getDatumPrijema().isBlank()) {
                    try {
                        String kljuc = java.time.LocalDate.parse(rn.getDatumPrijema(), fmt).format(mesecFmt);
                        poMesecima.computeIfPresent(kljuc, (k, v) -> v + 1);
                    } catch (Exception ignored) {}
                }
            }
            for (Map.Entry<String, Long> entry : poMesecima.entrySet()) {
                serija.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
            }
        } catch (Exception e) {
            logger.warning("Greška pri kreiranju grafikona: " + e.getMessage());
        }
        grafikon.getData().add(serija);

        // ── NAJAKTIVNIJI KLIJENTI ─────────────────────────────
        Label lblKlijenti = new Label("Najaktivniji klijenti");
        lblKlijenti.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0a1628;");

        TableView<Map.Entry<String, Long>> tabelaKlijenti = new TableView<>();
        tabelaKlijenti.getStyleClass().add("table-view");
        tabelaKlijenti.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabelaKlijenti.setPrefHeight(200);

        TableColumn<Map.Entry<String, Long>, String> colKlijent = new TableColumn<>("Klijent");
        colKlijent.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKey()));

        TableColumn<Map.Entry<String, Long>, String> colBrojNaloga = new TableColumn<>("Broj naloga");
        colBrojNaloga.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getValue())));
        colBrojNaloga.setPrefWidth(120);

        tabelaKlijenti.getColumns().addAll(colKlijent, colBrojNaloga);

        try {
            Map<Integer, Long> poKlijentima = sviNalozi.stream()
                    .collect(Collectors.groupingBy(RadniNalog::getKlijentId, Collectors.counting()));

            List<Map.Entry<String, Long>> sortirana = new ArrayList<>();
            for (Map.Entry<Integer, Long> entry : poKlijentima.entrySet()) {
                Klijent k = klijentDao.vratiPoId(entry.getKey());
                if (k != null) sortirana.add(Map.entry(k.getPunoIme(), entry.getValue()));
            }
            sortirana.sort((a, b) -> Long.compare(b.getValue(), a.getValue()));
            tabelaKlijenti.setItems(FXCollections.observableArrayList(
                    sortirana.stream().limit(10).collect(Collectors.toList())));
        } catch (SQLException e) {
            logger.warning("Greška pri statistikama klijenata: " + e.getMessage());
            tabelaKlijenti.setPlaceholder(new Label("Greška pri učitavanju podataka"));
        }

        // ── NAJCESCI ARTIKLI ──────────────────────────────────
        Label lblArtikli = new Label("Najčešće korišćeni artikli");
        lblArtikli.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0a1628;");

        TableView<Map.Entry<String, Double>> tabelaArtikli = new TableView<>();
        tabelaArtikli.getStyleClass().add("table-view");
        tabelaArtikli.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabelaArtikli.setPrefHeight(200);

        TableColumn<Map.Entry<String, Double>, String> colArtikal = new TableColumn<>("Artikal");
        colArtikal.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKey()));

        TableColumn<Map.Entry<String, Double>, String> colUkupno = new TableColumn<>("Ukupno utrošeno");
        colUkupno.setCellValueFactory(c -> new SimpleStringProperty(String.format("%.2f", c.getValue().getValue())));
        colUkupno.setPrefWidth(140);

        tabelaArtikli.getColumns().addAll(colArtikal, colUkupno);

        try {
            Map<String, Double> artikalMap = new HashMap<>();
            for (RadniNalog rn : sviNalozi) {
                RadniNalog pun = nalogDao.vratiPoId(rn.getId());
                if (pun != null) {
                    for (NalogArtikal na : pun.getArtikli()) {
                        artikalMap.merge(na.getNazivArtikla(), na.getKolicina(), Double::sum);
                    }
                }
            }
            List<Map.Entry<String, Double>> sortirani = new ArrayList<>(artikalMap.entrySet());
            sortirani.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
            tabelaArtikli.setItems(FXCollections.observableArrayList(
                    sortirani.stream().limit(10).collect(Collectors.toList())));
        } catch (SQLException e) {
            logger.warning("Greška pri statistikama artikala: " + e.getMessage());
            tabelaArtikli.setPlaceholder(new Label("Greška pri učitavanju podataka"));
        }

        // ── LAYOUT ────────────────────────────────────────────
        HBox tabelePored = new HBox(16);
        VBox klijentiBox = new VBox(8, lblKlijenti, tabelaKlijenti);
        VBox artikliBox = new VBox(8, lblArtikli, tabelaArtikli);
        HBox.setHgrow(klijentiBox, Priority.ALWAYS);
        HBox.setHgrow(artikliBox, Priority.ALWAYS);
        tabelePored.getChildren().addAll(klijentiBox, artikliBox);

        content.getChildren().addAll(statovi, lblGrafikon, grafikon, tabelePored);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");

        view.setTop(topbar);
        view.setCenter(scroll);
    }

    private VBox kreirajStatKarticu(String label, String value, String styleClass) {
        VBox card = new VBox(4);
        card.getStyleClass().addAll("stat-card", styleClass);
        card.setPrefWidth(180);
        Label lbl = new Label(label);
        lbl.getStyleClass().add("stat-label");
        Label val = new Label(value);
        val.getStyleClass().add("stat-value");
        card.getChildren().addAll(lbl, val);
        return card;
    }

    private void prikaziGresku(String poruka) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greška");
        alert.setContentText(poruka);
        alert.showAndWait();
    }
}
