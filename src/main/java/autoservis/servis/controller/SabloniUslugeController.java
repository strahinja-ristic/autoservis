package autoservis.servis.controller;

import autoservis.servis.dao.SablonUslugeDao;
import autoservis.servis.util.AppIkona;
import autoservis.servis.model.SablonUsluge;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class SabloniUslugeController {

    private BorderPane view;
    private TableView<SablonUsluge> tabela;
    private SablonUslugeDao sablonDao;
    private ObservableList<SablonUsluge> sabloni;
    private boolean prikazujeArhivu = false;

    public SabloniUslugeController() {
        this.sablonDao = new SablonUslugeDao();
        this.sabloni = FXCollections.observableArrayList();
        this.view = new BorderPane();
        kreirajUI();
        ucitajSablone();
    }

    public BorderPane getView() {
        return view;
    }

    private void kreirajUI() {
        // Topbar
        HBox topbar = new HBox(10);
        topbar.getStyleClass().add("topbar");
        topbar.setAlignment(Pos.CENTER_LEFT);

        Label breadcrumb = new Label("Početna / ");
        breadcrumb.getStyleClass().add("topbar-title");
        Label bold = new Label("Šabloni usluga");
        bold.getStyleClass().add("topbar-title-bold");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        TextField searchField = new TextField();
        searchField.setPromptText("Pretraži šablone...");
        searchField.getStyleClass().add("search-field");
        searchField.textProperty().addListener((obs, old, novo) -> {
            try {
                if (novo == null || novo.isBlank()) {
                    sabloni.setAll(sablonDao.vratiSve());
                } else {
                    String filter = novo.toLowerCase();
                    sabloni.setAll(sablonDao.vratiSve().stream()
                            .filter(s -> s.getNaziv().toLowerCase().contains(filter))
                            .toList());
                }
            } catch (Exception e) {
                prikaziGresku("Greška pri pretrazi: " + e.getMessage());
            }
        });

        Button btnNovi = new Button("+ Novi šablon");
        btnNovi.getStyleClass().add("btn-primary");
        btnNovi.setOnAction(e -> otvoriFormu(null));

        Button btnArhiva = new Button("Arhivirani");
        btnArhiva.getStyleClass().add("btn-secondary");
        btnArhiva.setOnAction(e -> {
            prikazujeArhivu = !prikazujeArhivu;
            btnArhiva.setText(prikazujeArhivu ? "Aktivni" : "Arhivirani");
            if (prikazujeArhivu) ucitajArhivirane(); else ucitajSablone();
        });

        topbar.getChildren().addAll(breadcrumb, bold, spacer, searchField, btnArhiva, btnNovi);

        // Tabela
        tabela = new TableView<>();
        tabela.getStyleClass().add("table-view");
        tabela.setItems(sabloni);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<SablonUsluge, String> colNaziv = new TableColumn<>("Naziv usluge");
        colNaziv.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNaziv()));

        TableColumn<SablonUsluge, String> colAkcije = new TableColumn<>("Akcije");
        colAkcije.setPrefWidth(160);
        colAkcije.setCellFactory(col -> new TableCell<>() {
            private final Button btnIzmeni = new Button("Izmeni");
            private final Button btnObrisi = new Button("Arhiviraj");
            private final Button btnVrati = new Button("Vrati iz arhive");
            private final HBox box = new HBox(6, btnIzmeni, btnObrisi);
            private final HBox boxArhiva = new HBox(6, btnVrati);

            {
                btnIzmeni.getStyleClass().add("btn-secondary");
                btnObrisi.getStyleClass().add("btn-danger");
                btnVrati.getStyleClass().add("btn-secondary");
                box.setAlignment(Pos.CENTER);
                boxArhiva.setAlignment(Pos.CENTER);

                btnIzmeni.setOnAction(e -> otvoriFormu(getTableView().getItems().get(getIndex())));
                btnObrisi.setOnAction(e -> arhiviraj(getTableView().getItems().get(getIndex())));
                btnVrati.setOnAction(e -> vratiIzArhive(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : (prikazujeArhivu ? boxArhiva : box));
            }
        });

        tabela.getColumns().addAll(colNaziv, colAkcije);

        VBox content = new VBox(0);
        content.getStyleClass().add("content-area");
        VBox.setVgrow(tabela, Priority.ALWAYS);
        content.getChildren().add(tabela);

        view.setTop(topbar);
        view.setCenter(content);
    }

    private void ucitajSablone() {
        try {
            List<SablonUsluge> lista = sablonDao.vratiSve();
            sabloni.setAll(lista);
        } catch (SQLException e) {
            prikaziGresku("Greška pri učitavanju šablona: " + e.getMessage());
        }
    }

    private void otvoriFormu(SablonUsluge sablon) {
        Stage stage = new Stage();
        AppIkona.postavi(stage);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(sablon == null ? "Novi šablon" : "Izmena šablona");

        VBox root = new VBox(12);
        root.getStyleClass().add("form-card");
        root.setPadding(new Insets(20));

        TextField tfNaziv = new TextField();
        tfNaziv.setPromptText("Naziv usluge");
        tfNaziv.getStyleClass().add("form-field");
        tfNaziv.setMaxWidth(Double.MAX_VALUE);

        if (sablon != null) {
            tfNaziv.setText(sablon.getNaziv());
        }

        VBox nazívBox = new VBox(4, new Label("Naziv usluge *") {{
            getStyleClass().add("form-label"); }}, tfNaziv);

        Button btnSacuvaj = new Button("Sačuvaj");
        btnSacuvaj.getStyleClass().add("btn-primary");
        btnSacuvaj.setOnAction(e -> {
            if (tfNaziv.getText().isBlank()) {
                prikaziGresku("Naziv je obavezan.");
                return;
            }
            SablonUsluge s = sablon != null ? sablon : new SablonUsluge();
            s.setNaziv(tfNaziv.getText().trim());
            try {
                if (sablon == null) sablonDao.dodaj(s);
                else sablonDao.izmeni(s);
                ucitajSablone();
                stage.close();
            } catch (SQLException ex) {
                prikaziGresku("Greška: " + ex.getMessage());
            }
        });

        Button btnOtkazi = new Button("Otkaži");
        btnOtkazi.getStyleClass().add("btn-secondary");
        btnOtkazi.setOnAction(e -> stage.close());

        HBox dugmad = new HBox(8, btnSacuvaj, btnOtkazi);
        dugmad.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(nazívBox, new Separator(), dugmad);

        Scene scene = new Scene(root);
        stage.setWidth(400);
        scene.getStylesheets().add(getClass().getResource("/style/style.css").toExternalForm());
        stage.setScene(scene);
        stage.showAndWait();
    }

    private void arhiviraj(SablonUsluge sablon) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Potvrda");
        alert.setHeaderText("Arhiviranje šablona");
        alert.setContentText("Da li ste sigurni da želite da arhivirate: " + sablon.getNaziv() + "?");
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    sablonDao.arhiviraj(sablon.getId());
                    ucitajSablone();
                } catch (SQLException e) {
                    prikaziGresku("Greška: " + e.getMessage());
                }
            }
        });
    }

    private void ucitajArhivirane() {
        try {
            sabloni.setAll(sablonDao.vratiArhivirane());
        } catch (SQLException e) {
            prikaziGresku("Greška pri učitavanju arhive: " + e.getMessage());
        }
    }

    private void vratiIzArhive(SablonUsluge sablon) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Potvrda");
        alert.setHeaderText("Vraćanje iz arhive");
        alert.setContentText("Da li ste sigurni da želite da vratite: " + sablon.getNaziv() + "?");
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    sablonDao.vratiIzArhive(sablon.getId());
                    ucitajArhivirane();
                } catch (SQLException e) {
                    prikaziGresku("Greška pri vraćanju iz arhive: " + e.getMessage());
                }
            }
        });
    }

    private void prikaziGresku(String poruka) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greška");
        alert.setContentText(poruka);
        alert.showAndWait();
    }
}