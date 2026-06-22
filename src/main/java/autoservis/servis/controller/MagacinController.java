package autoservis.servis.controller;

import autoservis.servis.dao.ArtikalDao;
import autoservis.servis.dao.PodesavanjaDao;
import autoservis.servis.model.Artikal;
import autoservis.servis.model.Podesavanja;
import autoservis.servis.util.PopisPdfPreviewController;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.util.List;

public class MagacinController {

    private BorderPane view;
    private TableView<Artikal> tabela;
    private ArtikalDao artikalDao;
    private ObservableList<Artikal> artikli;
    private TextField searchField;
    private boolean prikazujeArhivu = false;

    public MagacinController() {
        this.artikalDao = new ArtikalDao();
        this.artikli = FXCollections.observableArrayList();
        this.view = new BorderPane();
        kreirajUI();
        ucitajArtikle();
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
        Label bold = new Label("Magacin");
        bold.getStyleClass().add("topbar-title-bold");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        searchField = new TextField();
        searchField.setPromptText("Pretraži artikle...");
        searchField.getStyleClass().add("search-field");
        searchField.textProperty().addListener((obs, old, novo) -> pretrazi(novo));

        Button btnNovi = new Button("+ Novi artikal");
        btnNovi.getStyleClass().add("btn-primary");
        btnNovi.setOnAction(e -> otvoriFormu(null));

        Button btnArhiva = new Button("Arhivirani");
        btnArhiva.getStyleClass().add("btn-secondary");
        btnArhiva.setOnAction(e -> {
            prikazujeArhivu = !prikazujeArhivu;
            btnArhiva.setText(prikazujeArhivu ? "Aktivni" : "Arhivirani");
            if (prikazujeArhivu) ucitajArhivirane(); else ucitajArtikle();
        });

        Button btnPopis = new Button("Popis");
        btnPopis.getStyleClass().add("btn-secondary");
        btnPopis.setOnAction(e -> otvoriPopis());

        Button btnSabloniPopisa = new Button("Šabloni popisa");
        btnSabloniPopisa.getStyleClass().add("btn-secondary");
        btnSabloniPopisa.setOnAction(e -> otvoriSablonePopisa());

        if (!autoservis.servis.util.FeatureFlagService.getInstance().isEnabled("feature_popis")) {
            btnPopis.setVisible(false);
            btnPopis.setManaged(false);
            btnSabloniPopisa.setVisible(false);
            btnSabloniPopisa.setManaged(false);
        }

        topbar.getChildren().addAll(breadcrumb, bold, spacer, searchField, btnArhiva, btnPopis, btnSabloniPopisa, btnNovi);

        // Tabela
        tabela = new TableView<>();
        tabela.getStyleClass().add("table-view");
        tabela.setItems(artikli);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<Artikal, String> colVrsta = new TableColumn<>("Vrsta");
        colVrsta.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getVrsta()));
        colVrsta.setPrefWidth(80);

        TableColumn<Artikal, String> colSifra = new TableColumn<>("Šifra");
        colSifra.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getSifra() != null ? String.valueOf(c.getValue().getSifra()) : ""));
        colSifra.setPrefWidth(100);

        TableColumn<Artikal, String> colNaziv = new TableColumn<>("Naziv");
        colNaziv.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNaziv()));

        TableColumn<Artikal, String> colJM = new TableColumn<>("Jed. mere");
        colJM.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getJedinicaMere()));
        colJM.setPrefWidth(90);

        TableColumn<Artikal, String> colKolicina = new TableColumn<>("Na stanju");
        colKolicina.setPrefWidth(90);
        colKolicina.setCellValueFactory(c -> {
            Artikal a = c.getValue();
            return new SimpleStringProperty(a.isUsluga() || a.getKolicina() == null ? "—" : String.valueOf(a.getKolicina()));
        });
        colKolicina.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    Artikal a = getTableView().getItems().get(getIndex());
                    Label lbl = new Label(item);
                    if (a.isIspodMinimuma()) {
                        lbl.getStyleClass().add("badge-upozorenje");
                    }
                    setGraphic(lbl);
                }
            }
        });

        TableColumn<Artikal, String> colMin = new TableColumn<>("Min. stanje");
        colMin.setCellValueFactory(c -> {
            Artikal a = c.getValue();
            return new SimpleStringProperty(a.isUsluga() || a.getMinimalnaKolicina() == null ? "—" : String.valueOf(a.getMinimalnaKolicina()));
        });
        colMin.setPrefWidth(100);

        TableColumn<Artikal, String> colNabavna = new TableColumn<>("Nab. cena");
        colNabavna.setCellValueFactory(c -> {
            Artikal a = c.getValue();
            return new SimpleStringProperty(a.isUsluga() || a.getNabavnaCena() == null ? "—" : String.format("%.2f", a.getNabavnaCena()));
        });
        colNabavna.setPrefWidth(100);

        TableColumn<Artikal, String> colProdajna = new TableColumn<>("Prod. cena");
        colProdajna.setCellValueFactory(c -> new SimpleStringProperty(
                String.format("%.2f", c.getValue().getProdajnaCena())));
        colProdajna.setPrefWidth(100);

        TableColumn<Artikal, String> colAkcije = new TableColumn<>("Akcije");
        colAkcije.setPrefWidth(230);
        colAkcije.setCellFactory(col -> new TableCell<>() {
            private final Button btnIzmeni = new Button("Izmeni");
            private final Button btnArhiviraj = new Button("Arhiviraj");
            private final Button btnVrati = new Button("Vrati iz arhive");
            private final HBox box = new HBox(6, btnIzmeni, btnArhiviraj);
            private final HBox boxArhiva = new HBox(6, btnVrati);

            {
                btnIzmeni.getStyleClass().add("btn-secondary");
                btnArhiviraj.getStyleClass().add("btn-danger");
                btnVrati.getStyleClass().add("btn-secondary");
                box.setAlignment(Pos.CENTER);
                boxArhiva.setAlignment(Pos.CENTER);

                btnIzmeni.setOnAction(e -> otvoriFormu(getTableView().getItems().get(getIndex())));
                btnArhiviraj.setOnAction(e -> arhivirajArtikal(getTableView().getItems().get(getIndex())));
                btnVrati.setOnAction(e -> vratiIzArhive(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : (prikazujeArhivu ? boxArhiva : box));
            }
        });

        tabela.getColumns().addAll(colVrsta, colSifra, colNaziv, colJM, colKolicina, colMin, colNabavna, colProdajna, colAkcije);

        VBox content = new VBox(0);
        content.getStyleClass().add("content-area");
        VBox.setVgrow(tabela, Priority.ALWAYS);
        content.getChildren().add(tabela);

        view.setTop(topbar);
        view.setCenter(content);
    }

    private void ucitajArtikle() {
        try {
            List<Artikal> lista = artikalDao.vratiSve();
            artikli.setAll(lista);
        } catch (SQLException e) {
            prikaziGresku("Greška pri učitavanju artikala: " + e.getMessage());
        }
    }

    private void pretrazi(String upit) {
        try {
            if (upit == null || upit.isBlank()) {
                artikli.setAll(artikalDao.vratiSve());
            } else {
                artikli.setAll(artikalDao.pretrazi(upit));
            }
        } catch (SQLException e) {
            prikaziGresku("Greška pri pretrazi: " + e.getMessage());
        }
    }

    private void otvoriFormu(Artikal artikal) {
        ArtikalFormaController forma = new ArtikalFormaController(artikal, () -> ucitajArtikle());
        forma.show();
    }

    private void arhivirajArtikal(Artikal artikal) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Potvrda");
        alert.setHeaderText("Arhiviranje artikla");
        alert.setContentText("Da li ste sigurni da želite da arhivirate: " + artikal.getNaziv() + "?");

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    artikalDao.arhiviraj(artikal.getId());
                    ucitajArtikle();
                } catch (SQLException e) {
                    prikaziGresku("Greška pri arhiviranju: " + e.getMessage());
                }
            }
        });
    }

    private void ucitajArhivirane() {
        try {
            artikli.setAll(artikalDao.vratiArhivirane());
        } catch (SQLException e) {
            prikaziGresku("Greška pri učitavanju arhive: " + e.getMessage());
        }
    }

    private void vratiIzArhive(Artikal artikal) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Potvrda");
        alert.setHeaderText("Vraćanje iz arhive");
        alert.setContentText("Da li ste sigurni da želite da vratite: " + artikal.getNaziv() + "?");
        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    artikalDao.vratiIzArhive(artikal.getId());
                    ucitajArhivirane();
                } catch (SQLException e) {
                    prikaziGresku("Greška pri vraćanju iz arhive: " + e.getMessage());
                }
            }
        });
    }

    private void otvoriSablonePopisa() {
        javafx.stage.Stage stage = new javafx.stage.Stage();
        autoservis.servis.util.AppIkona.postavi(stage);
        stage.setTitle("Šabloni popisa");
        javafx.scene.Scene scene = new javafx.scene.Scene(new PopisSabloniController().getView(), 900, 620);
        scene.getStylesheets().add(getClass().getResource("/style/style.css").toExternalForm());
        stage.setScene(scene);
        stage.show();
    }

    private void otvoriPopis() {
        try {
            List<Artikal> lista = artikalDao.vratiSve();
            Podesavanja firma = new PodesavanjaDao().vratiPodesavanja();
            new PopisPdfPreviewController(lista, firma).show();
        } catch (Exception e) {
            prikaziGresku("Greška pri generisanju popisa: " + e.getMessage());
        }
    }

    private void prikaziGresku(String poruka) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greška");
        alert.setContentText(poruka);
        alert.showAndWait();
    }
}
