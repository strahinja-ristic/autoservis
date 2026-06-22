package autoservis.servis.controller;

import autoservis.servis.dao.KlijentDao;
import autoservis.servis.model.Klijent;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.util.List;

public class KlijentiController {

    private BorderPane view;
    private TableView<Klijent> tabela;
    private KlijentDao klijentDao;
    private ObservableList<Klijent> klijenti;
    private TextField searchField;
    private boolean prikazujeArhivu = false;

    public KlijentiController() {
        this.klijentDao = new KlijentDao();
        this.klijenti = FXCollections.observableArrayList();
        this.view = new BorderPane();
        kreirajUI();
        ucitajKlijente();
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
        Label bold = new Label("Klijenti");
        bold.getStyleClass().add("topbar-title-bold");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        searchField = new TextField();
        searchField.setPromptText("Pretraži klijente...");
        searchField.getStyleClass().add("search-field");
        searchField.textProperty().addListener((obs, old, novo) -> pretrazi(novo));

        Button btnNovi = new Button("+ Novi klijent");
        btnNovi.getStyleClass().add("btn-primary");
        btnNovi.setOnAction(e -> otvoriFormu(null));

        Button btnArhiva = new Button("Arhivirani");
        btnArhiva.getStyleClass().add("btn-secondary");
        btnArhiva.setOnAction(e -> {
            prikazujeArhivu = !prikazujeArhivu;
            btnArhiva.setText(prikazujeArhivu ? "Aktivni" : "Arhivirani");
            if (prikazujeArhivu) ucitajArhivirane(); else ucitajKlijente();
        });

        topbar.getChildren().addAll(breadcrumb, bold, spacer, searchField, btnArhiva, btnNovi);

        // Tabela
        tabela = new TableView<>();
        tabela.getStyleClass().add("table-view");
        tabela.setItems(klijenti);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Klijent, String> colTip = new TableColumn<>("Tip");
        colTip.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTip()));
        colTip.setPrefWidth(80);

        TableColumn<Klijent, String> colIme = new TableColumn<>("Ime / Naziv firme");
        colIme.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPunoIme()));

        TableColumn<Klijent, String> colTelefon = new TableColumn<>("Telefon");
        colTelefon.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTelefon()));
        colTelefon.setPrefWidth(130);

        TableColumn<Klijent, String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));

        TableColumn<Klijent, String> colAkcije = new TableColumn<>("Akcije");
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
                btnObrisi.setOnAction(e -> arhivirajKlijenta(getTableView().getItems().get(getIndex())));
                btnVrati.setOnAction(e -> vratiIzArhive(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : (prikazujeArhivu ? boxArhiva : box));
            }
        });

        tabela.getColumns().addAll(colTip, colIme, colTelefon, colEmail, colAkcije);

        VBox content = new VBox(0);
        content.getStyleClass().add("content-area");
        VBox.setVgrow(tabela, Priority.ALWAYS);
        content.getChildren().add(tabela);

        view.setTop(topbar);
        view.setCenter(content);
    }

    private void ucitajKlijente() {
        try {
            List<Klijent> lista = klijentDao.vratiSve();
            klijenti.setAll(lista);
        } catch (SQLException e) {
            prikaziGresku("Greška pri učitavanju klijenata: " + e.getMessage());
        }
    }

    private void pretrazi(String upit) {
        try {
            if (upit == null || upit.isBlank()) {
                klijenti.setAll(klijentDao.vratiSve());
            } else {
                klijenti.setAll(klijentDao.pretrazi(upit));
            }
        } catch (SQLException e) {
            prikaziGresku("Greška pri pretrazi: " + e.getMessage());
        }
    }

    private void otvoriFormu(Klijent klijent) {
        KlijentFormaController forma = new KlijentFormaController(klijent, () -> ucitajKlijente());
        forma.show();
    }

    private void arhivirajKlijenta(Klijent klijent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Potvrda");
        alert.setHeaderText("Arhiviranje klijenta");
        alert.setContentText("Da li ste sigurni da želite da arhivirate klijenta: " + klijent.getPunoIme() + "?");

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    klijentDao.arhiviraj(klijent.getId());
                    ucitajKlijente();
                } catch (SQLException e) {
                    prikaziGresku("Greška pri arhiviranju: " + e.getMessage());
                }
            }
        });
    }

    private void ucitajArhivirane() {
        try {
            klijenti.setAll(klijentDao.vratiArhivirane());
        } catch (SQLException e) {
            prikaziGresku("Greška pri učitavanju arhive: " + e.getMessage());
        }
    }

    private void vratiIzArhive(Klijent klijent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Potvrda");
        alert.setHeaderText("Vraćanje iz arhive");
        alert.setContentText("Da li ste sigurni da želite da vratite klijenta: " + klijent.getPunoIme() + "?");
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    klijentDao.vratiIzArhive(klijent.getId());
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