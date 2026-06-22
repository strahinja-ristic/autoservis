package autoservis.servis.controller;

import autoservis.servis.dao.RadniNalogDao;
import autoservis.servis.util.AppIkona;
import autoservis.servis.model.NalogUsluga;
import autoservis.servis.model.RadniNalog;
import autoservis.servis.model.Vozilo;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class IstorijaServisaController {

    private Vozilo vozilo;
    private RadniNalogDao nalogDao;

    public IstorijaServisaController(Vozilo vozilo) {
        this.vozilo = vozilo;
        this.nalogDao = new RadniNalogDao();
    }

    public void show() {
        Stage stage = new Stage();
        AppIkona.postavi(stage);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Istorija servisa — " + vozilo.toString());
        stage.setMinWidth(750);
        stage.setMinHeight(500);

        // Topbar
        HBox topbar = new HBox(10);
        topbar.setStyle("-fx-background-color: #0a1628; -fx-padding: 12 16 12 16;");
        topbar.setAlignment(Pos.CENTER_LEFT);

        Label lblNaslov = new Label("Istorija servisa — " + vozilo.getMarka() + " " +
                vozilo.getModel() + " (" + vozilo.getRegistracija() + ")");
        lblNaslov.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label lblKm = new Label("Trenutna km: " + vozilo.getKilometraza());
        lblKm.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 12px;");

        topbar.getChildren().addAll(lblNaslov, spacer, lblKm);

        if (vozilo.getNapomena() != null && !vozilo.getNapomena().isBlank()) {
            Label lblUredjaj = new Label("  |  Uređaj: " + vozilo.getNapomena());
            lblUredjaj.setStyle("-fx-text-fill: rgba(255,255,255,0.7); -fx-font-size: 12px;");
            topbar.getChildren().add(lblUredjaj);
        }

        // Tabela naloga
        TableView<RadniNalog> tabela = new TableView<>();
        tabela.getStyleClass().add("table-view");
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<RadniNalog, String> colBroj = new TableColumn<>("Broj naloga");
        colBroj.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBrojNaloga()));
        colBroj.setPrefWidth(100);

        TableColumn<RadniNalog, String> colPrijem = new TableColumn<>("Datum prijema");
        colPrijem.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDatumPrijema()));
        colPrijem.setPrefWidth(110);

        TableColumn<RadniNalog, String> colZavrsetak = new TableColumn<>("Datum završetka");
        colZavrsetak.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDatumZavrsetka() != null ? c.getValue().getDatumZavrsetka() : "—"));
        colZavrsetak.setPrefWidth(120);

        TableColumn<RadniNalog, String> colKm = new TableColumn<>("Km pri prijemu");
        colKm.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getKilometrazaPrijema() + " km"));
        colKm.setPrefWidth(110);

        TableColumn<RadniNalog, String> colStatus = new TableColumn<>("Status");
        colStatus.setPrefWidth(100);
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Label badge = new Label(item);
                    badge.getStyleClass().add(switch (item) {
                        case "U radu" -> "badge-u-radu";
                        case "Završeno" -> "badge-zavrseno";
                        default -> "badge-primljeno";
                    });
                    setGraphic(badge);
                }
            }
        });

        TableColumn<RadniNalog, String> colOpis = new TableColumn<>("Opis kvara");
        colOpis.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getOpisKvara() != null ? c.getValue().getOpisKvara() : "—"));

        tabela.getColumns().addAll(colBroj, colPrijem, colZavrsetak, colKm, colStatus, colOpis);

        // Detalji selektovanog naloga
        VBox detaljiPanel = new VBox(10);
        detaljiPanel.setPadding(new Insets(12));
        detaljiPanel.setStyle("-fx-background-color: #f8fafd; -fx-border-color: #d0d9e6; -fx-border-width: 1 0 0 0;");
        detaljiPanel.setPrefHeight(180);

        Label lblDetalji = new Label("Odaberite nalog za detalje");
        lblDetalji.setStyle("-fx-text-fill: #8a9ab5; -fx-font-size: 12px;");
        detaljiPanel.getChildren().add(lblDetalji);

        tabela.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
            if (selected == null) return;
            detaljiPanel.getChildren().clear();

            try {
                RadniNalog pun = nalogDao.vratiPoId(selected.getId());

                Label lblBroj = new Label("Nalog: " + pun.getBrojNaloga());
                lblBroj.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0057b7;");

                // Usluge
                VBox uslugeBox = new VBox(4);
                Label lblUsluge = new Label("Izvršene usluge:");
                lblUsluge.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #4a6080;");
                uslugeBox.getChildren().add(lblUsluge);

                if (pun.getUsluge().isEmpty()) {
                    uslugeBox.getChildren().add(new Label("—"));
                } else {
                    for (NalogUsluga u : pun.getUsluge()) {
                        Label lbl = new Label("• " + u.getNaziv());
                        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #0a1628;");
                        uslugeBox.getChildren().add(lbl);
                    }
                }

                // Artikli
                VBox artikliBox = new VBox(4);
                Label lblArtikli = new Label("Ugrađeni artikli:");
                lblArtikli.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #4a6080;");
                artikliBox.getChildren().add(lblArtikli);

                if (pun.getArtikli().isEmpty()) {
                    artikliBox.getChildren().add(new Label("—"));
                } else {
                    for (var a : pun.getArtikli()) {
                        Label lbl = new Label("• " + a.getNazivArtikla() + " — " +
                                a.getKolicina() + " " + a.getJedinicaMere());
                        lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #0a1628;");
                        artikliBox.getChildren().add(lbl);
                    }
                }

                HBox detaljiGrid = new HBox(30);
                detaljiGrid.getChildren().addAll(uslugeBox, artikliBox);

                detaljiPanel.getChildren().addAll(lblBroj, detaljiGrid);

            } catch (SQLException e) {
                detaljiPanel.getChildren().add(new Label("Greška pri učitavanju detalja."));
            }
        });

        // Ucitaj naloge
        try {
            List<RadniNalog> nalozi = nalogDao.vratiPoVozilu(vozilo.getId());
            tabela.setItems(FXCollections.observableArrayList(nalozi));
            if (nalozi.isEmpty()) {
                tabela.setPlaceholder(new Label("Nema servisnih zapisa za ovo vozilo"));
            }
        } catch (SQLException e) {
            tabela.setPlaceholder(new Label("Greška pri učitavanju istorije"));
        }

        VBox.setVgrow(tabela, Priority.ALWAYS);

        VBox center = new VBox(0);
        center.getChildren().addAll(tabela, detaljiPanel);
        VBox.setVgrow(tabela, Priority.ALWAYS);

        // Zatvori dugme
        Button btnZatvori = new Button("Zatvori");
        btnZatvori.getStyleClass().add("btn-secondary");
        btnZatvori.setOnAction(e -> stage.close());

        HBox bottom = new HBox(btnZatvori);
        bottom.setAlignment(Pos.CENTER_RIGHT);
        bottom.setPadding(new Insets(10, 16, 10, 16));
        bottom.setStyle("-fx-background-color: #ffffff; -fx-border-color: #d0d9e6; -fx-border-width: 1 0 0 0;");

        BorderPane root = new BorderPane();
        root.setTop(topbar);
        root.setCenter(center);
        root.setBottom(bottom);

        Scene scene = new Scene(root, 800, 600);
        scene.getStylesheets().add(getClass().getResource("/style/style.css").toExternalForm());
        stage.setScene(scene);
        stage.showAndWait();
    }
}