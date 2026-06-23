package autoservis.servis.controller;

import autoservis.servis.dao.KlijentDao;
import autoservis.servis.dao.VoziloDao;
import autoservis.servis.model.Klijent;
import autoservis.servis.model.Vozilo;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.util.List;

public class VozilaController {

    private BorderPane view;
    private TableView<Vozilo> tabela;
    private VoziloDao voziloDao;
    private KlijentDao klijentDao;
    private ObservableList<Vozilo> vozila;
    private TextField searchField;
    private boolean prikazujeArhivu = false;
    private Map<Integer, String> klijentImeMap = new HashMap<>();

    public VozilaController() {
        this.voziloDao = new VoziloDao();
        this.klijentDao = new KlijentDao();
        this.vozila = FXCollections.observableArrayList();
        this.view = new BorderPane();
        kreirajUI();
        ucitajVozila();
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
        Label bold = new Label("Vozila");
        bold.getStyleClass().add("topbar-title-bold");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        searchField = new TextField();
        searchField.setPromptText("Pretraži vozila...");
        searchField.getStyleClass().add("search-field");
        searchField.textProperty().addListener((obs, old, novo) -> pretrazi(novo));

        Button btnNovo = new Button("+ Novo vozilo");
        btnNovo.getStyleClass().add("btn-primary");
        btnNovo.setOnAction(e -> otvoriFormu(null));

        Button btnArhiva = new Button("Arhivirani");
        btnArhiva.getStyleClass().add("btn-secondary");
        btnArhiva.setOnAction(e -> {
            prikazujeArhivu = !prikazujeArhivu;
            btnArhiva.setText(prikazujeArhivu ? "Aktivni" : "Arhivirani");
            if (prikazujeArhivu) ucitajArhivirane(); else ucitajVozila();
        });

        topbar.getChildren().addAll(breadcrumb, bold, spacer, searchField, btnArhiva, btnNovo);

        // Tabela
        tabela = new TableView<>();
        tabela.getStyleClass().add("table-view");
        tabela.setItems(vozila);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Vozilo, String> colKlijent = new TableColumn<>("Klijent");
        colKlijent.setCellValueFactory(c ->
                new SimpleStringProperty(klijentImeMap.getOrDefault(c.getValue().getKlijentId(), "")));

        TableColumn<Vozilo, String> colMarka = new TableColumn<>("Marka");
        colMarka.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMarka()));
        colMarka.setPrefWidth(100);

        TableColumn<Vozilo, String> colModel = new TableColumn<>("Model");
        colModel.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getModel()));
        colModel.setPrefWidth(100);

        TableColumn<Vozilo, String> colGodiste = new TableColumn<>("Godište");
        colGodiste.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getGodiste() != null ? String.valueOf(c.getValue().getGodiste()) : "—"));
        colGodiste.setPrefWidth(70);

        TableColumn<Vozilo, String> colReg = new TableColumn<>("Registracija");
        colReg.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRegistracija()));
        colReg.setPrefWidth(110);

        TableColumn<Vozilo, String> colKm = new TableColumn<>("Kilometraža");
        colKm.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKilometraza() + " km"));
        colKm.setPrefWidth(100);

        TableColumn<Vozilo, String> colUredjaj = new TableColumn<>("Napomena");
        colUredjaj.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getNapomena() != null ? c.getValue().getNapomena() : ""));
        colUredjaj.setPrefWidth(120);

        TableColumn<Vozilo, String> colAkcije = new TableColumn<>("Akcije");
        colAkcije.setPrefWidth(220);
        colAkcije.setCellFactory(col -> new TableCell<>() {
            private final Button btnIzmeni = new Button("Izmeni");
            private final Button btnIstorija = new Button("Istorija");
            private final Button btnObrisi = new Button("Arhiviraj");
            private final Button btnVrati = new Button("Vrati iz arhive");
            private final HBox box = new HBox(6, btnIzmeni, btnIstorija, btnObrisi);
            private final HBox boxArhiva = new HBox(6, btnVrati);

            {
                btnIzmeni.getStyleClass().add("btn-secondary");
                btnObrisi.getStyleClass().add("btn-danger");
                btnIstorija.getStyleClass().add("btn-secondary");
                btnVrati.getStyleClass().add("btn-secondary");
                box.setAlignment(Pos.CENTER);
                boxArhiva.setAlignment(Pos.CENTER);

                btnIstorija.setOnAction(e -> otvoriIstoriju(getTableView().getItems().get(getIndex())));
                btnIzmeni.setOnAction(e -> otvoriFormu(getTableView().getItems().get(getIndex())));
                btnObrisi.setOnAction(e -> arhivirajVozilo(getTableView().getItems().get(getIndex())));
                btnVrati.setOnAction(e -> vratiIzArhive(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : (prikazujeArhivu ? boxArhiva : box));
            }
        });

        tabela.getColumns().addAll(colKlijent, colMarka, colModel, colGodiste, colReg, colKm, colUredjaj, colAkcije);

        VBox content = new VBox(0);
        content.getStyleClass().add("content-area");
        VBox.setVgrow(tabela, Priority.ALWAYS);
        content.getChildren().add(tabela);

        view.setTop(topbar);
        view.setCenter(content);
    }

    private void ucitajKlijentImeMap() {
        try {
            klijentImeMap = klijentDao.vratiSve().stream()
                    .collect(Collectors.toMap(Klijent::getId, Klijent::getPunoIme));
        } catch (SQLException e) {
            klijentImeMap = new HashMap<>();
        }
    }

    private void ucitajVozila() {
        ucitajKlijentImeMap();
        try {
            List<Vozilo> lista = voziloDao.pretrazi("");
            vozila.setAll(lista);
        } catch (SQLException e) {
            prikaziGresku("Greška pri učitavanju vozila: " + e.getMessage());
        }
    }

    private void pretrazi(String upit) {
        ucitajKlijentImeMap();
        try {
            if (upit == null || upit.isBlank()) {
                vozila.setAll(voziloDao.pretrazi(""));
            } else {
                vozila.setAll(voziloDao.pretrazi(upit));
            }
        } catch (SQLException e) {
            prikaziGresku("Greška pri pretrazi: " + e.getMessage());
        }
    }

    private void otvoriFormu(Vozilo vozilo) {
        VoziloFormaController forma = new VoziloFormaController(vozilo, () -> ucitajVozila());
        forma.show();
    }

    private void arhivirajVozilo(Vozilo vozilo) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Potvrda");
        alert.setHeaderText("Arhiviranje vozila");
        alert.setContentText("Da li ste sigurni da želite da arhivirate vozilo: " + vozilo + "?");

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    voziloDao.arhiviraj(vozilo.getId());
                    ucitajVozila();
                } catch (SQLException e) {
                    prikaziGresku("Greška pri arhiviranju: " + e.getMessage());
                }
            }
        });
    }

    private void ucitajArhivirane() {
        ucitajKlijentImeMap();
        try {
            vozila.setAll(voziloDao.vratiArhivirane());
        } catch (SQLException e) {
            prikaziGresku("Greška pri učitavanju arhive: " + e.getMessage());
        }
    }

    private void vratiIzArhive(Vozilo vozilo) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Potvrda");
        alert.setHeaderText("Vraćanje iz arhive");
        alert.setContentText("Da li ste sigurni da želite da vratite vozilo: " + vozilo + "?");
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    voziloDao.vratiIzArhive(vozilo.getId());
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

    private void otvoriIstoriju(Vozilo vozilo) {
        IstorijaServisaController istorija = new IstorijaServisaController(vozilo);
        istorija.show();
    }
}