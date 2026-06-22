package autoservis.servis.controller;

import autoservis.servis.dao.*;
import autoservis.servis.model.*;
import autoservis.servis.util.AppIkona;
import autoservis.servis.util.FakturaPdfPreviewController;
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
import java.util.logging.Logger;

public class FakturaListaController {

    private static final Logger logger = Logger.getLogger(FakturaListaController.class.getName());

    private BorderPane view;
    private TableView<Faktura> tabela;
    private FakturaDao fakturaDao;
    private KlijentDao klijentDao;
    private ObservableList<Faktura> fakture;
    private TextField searchField;
    private ComboBox<String> filterStatus;
    private boolean firmaNijePodesen = true;
    private boolean prikazujeArhivu = false;
    private int trenutnaStranica = 0;
    private static final int VELICINA_STRANICE = 100;
    private Label lblStranica;
    private Button btnPret;
    private Button btnSled;

    private static final java.util.Map<String, Integer> STATUS_RED = java.util.Map.of(
            "Kreirana",   0,
            "Poslata",    1,
            "Plaćena",    2,
            "Stornirana", 3
    );

    public FakturaListaController() {
        this.fakturaDao = new FakturaDao();
        this.klijentDao = new KlijentDao();
        this.fakture = FXCollections.observableArrayList();
        try {
            autoservis.servis.model.Podesavanja p = new PodesavanjaDao().vratiPodesavanja();
            firmaNijePodesen = p == null || p.getNazivFirme() == null || p.getNazivFirme().isBlank();
        } catch (Exception ignored) {}
        this.view = new BorderPane();
        kreirajUI();
        ucitajFakture();
    }


    public BorderPane getView() {
        return view;
    }

    private void kreirajUI() {
        HBox topbar = new HBox(10);
        topbar.getStyleClass().add("topbar");
        topbar.setAlignment(Pos.CENTER_LEFT);
        Label breadcrumb = new Label("Finansije / ");
        breadcrumb.getStyleClass().add("topbar-title");
        Label bold = new Label("Prilog uz račun");
        bold.getStyleClass().add("topbar-title-bold");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        searchField = new TextField();
        searchField.setPromptText("Pretraži...");
        searchField.getStyleClass().add("search-field");
        searchField.textProperty().addListener((obs, old, novo) -> pretrazi(novo));

        filterStatus = new ComboBox<>();
        filterStatus.getItems().addAll("Svi statusi", "Kreirana", "Poslata", "Plaćena", "Stornirana");
        filterStatus.setValue("Svi statusi");
        filterStatus.getStyleClass().add("search-field");
        filterStatus.setPrefWidth(140);
        filterStatus.setOnAction(e -> pretrazi(searchField.getText()));

        Button btnNova = new Button("+ Novi prilog uz račun");
        btnNova.getStyleClass().add("btn-primary");
        btnNova.setOnAction(e -> otvoriFormu(null));

        Button btnArhiva = new Button("Arhivirane");
        btnArhiva.getStyleClass().add("btn-secondary");
        btnArhiva.setOnAction(e -> {
            prikazujeArhivu = !prikazujeArhivu;
            trenutnaStranica = 0;
            btnArhiva.setText(prikazujeArhivu ? "Aktivne" : "Arhivirane");
            if (prikazujeArhivu) ucitajArhivirane(); else ucitajFakture();
        });

        topbar.getChildren().addAll(breadcrumb, bold, spacer, searchField, filterStatus, btnArhiva, btnNova);

        tabela = new TableView<>(fakture);
        tabela.getStyleClass().add("table-view");
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabela.setPlaceholder(new Label("Nema dokumenata"));

        TableColumn<Faktura, String> colBroj = new TableColumn<>("Broj");
        colBroj.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBrojFakture()));
        colBroj.setPrefWidth(110);

        TableColumn<Faktura, String> colKlijent = new TableColumn<>("Klijent");
        colKlijent.setCellValueFactory(c -> {
            try {
                Klijent k = klijentDao.vratiPoId(c.getValue().getKlijentId());
                return new SimpleStringProperty(k != null ? k.getPunoIme() : "—");
            } catch (SQLException e) { return new SimpleStringProperty("—"); }
        });

        TableColumn<Faktura, String> colDatum = new TableColumn<>("Datum");
        colDatum.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDatumKreiranja()));
        colDatum.setPrefWidth(100);

        TableColumn<Faktura, String> colRok = new TableColumn<>("Rok plaćanja");
        colRok.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getRokPlacanja() != null ? c.getValue().getRokPlacanja() : "—"));
        colRok.setPrefWidth(110);

        TableColumn<Faktura, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        colStatus.setPrefWidth(100);
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setGraphic(null); return; }
                Label badge = new Label(status);
                badge.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 3 8 3 8; -fx-background-radius: 20; " +
                        statusBoja(status));
                setGraphic(badge);
                setText(null);
            }
        });

        TableColumn<Faktura, String> colIznos = new TableColumn<>("Iznos");
        colIznos.setCellValueFactory(c -> {
            try {
                Faktura f = fakturaDao.vratiPoId(c.getValue().getId());
                return new SimpleStringProperty(f != null ? fmtCena(f.zaUplatu()) : "—");
            } catch (SQLException e) { return new SimpleStringProperty("—"); }
        });
        colIznos.setPrefWidth(110);

        TableColumn<Faktura, Void> colAkcije = new TableColumn<>("Akcije");
        colAkcije.setPrefWidth(240);
        colAkcije.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("Izmeni");
            private final Button btnPdf = new Button("PDF");
            private final Button btnPlacena = new Button("Plaćena");
            private final Button btnArhiviraj = new Button("Arhiviraj");
            private final Button btnVrati = new Button("Vrati iz arhive");
            {
                btnEdit.getStyleClass().add("btn-secondary");
                btnPdf.getStyleClass().add("btn-secondary");
                btnPlacena.getStyleClass().add("btn-primary");
                btnArhiviraj.getStyleClass().add("btn-danger");
                btnVrati.getStyleClass().add("btn-secondary");

                btnPdf.setDisable(firmaNijePodesen);
                if (firmaNijePodesen)
                    btnPdf.setTooltip(new Tooltip("Popunite podatke o firmi u Podešavanjima"));

                btnEdit.setOnAction(e -> otvoriFormu(getTableView().getItems().get(getIndex())));
                btnPdf.setOnAction(e -> otvoriPdfPreview(getTableView().getItems().get(getIndex())));
                btnPlacena.setOnAction(e -> oznaciosPlacenu(getTableView().getItems().get(getIndex())));
                btnArhiviraj.setOnAction(e -> arhiviraj(getTableView().getItems().get(getIndex())));
                btnVrati.setOnAction(e -> vratiIzArhive(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                if (prikazujeArhivu) {
                    HBox box = new HBox(4, btnVrati);
                    box.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(box);
                    return;
                }
                Faktura f = getTableView().getItems().get(getIndex());
                btnPlacena.setVisible(!"Plaćena".equals(f.getStatus()) && !"Stornirana".equals(f.getStatus()));
                btnPlacena.setManaged(btnPlacena.isVisible());
                HBox box = new HBox(4, btnEdit, btnPdf, btnPlacena, btnArhiviraj);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box);
            }
        });

        tabela.getColumns().addAll(colBroj, colKlijent, colDatum, colRok, colStatus, colIznos, colAkcije);

        tabela.setRowFactory(tv -> {
            TableRow<Faktura> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    prikaziDetalje(row.getItem());
                }
            });
            return row;
        });

        VBox content = new VBox(0, tabela);
        content.getStyleClass().add("content-area");
        VBox.setVgrow(tabela, Priority.ALWAYS);

        btnPret = new Button("← Prethodna");
        btnPret.getStyleClass().add("btn-secondary");
        btnPret.setDisable(true);
        btnPret.setOnAction(e -> { trenutnaStranica--; reload(); });

        btnSled = new Button("Sledeća →");
        btnSled.getStyleClass().add("btn-secondary");
        btnSled.setOnAction(e -> { trenutnaStranica++; reload(); });

        lblStranica = new Label("Stranica 1 od 1");

        HBox pagination = new HBox(12, btnPret, lblStranica, btnSled);
        pagination.setAlignment(Pos.CENTER);
        pagination.setStyle("-fx-padding: 8 16 8 16; -fx-background-color: #f8fafd; -fx-border-color: #e3eaf3; -fx-border-width: 1 0 0 0;");

        view.setTop(topbar);
        view.setCenter(content);
        view.setBottom(pagination);
    }

    private void ucitajFakture() {
        reload();
    }

    private void ucitajArhivirane() {
        reload();
    }

    private void pretrazi(String upit) {
        trenutnaStranica = 0;
        reload();
    }

    private void reload() {
        try {
            String upit = searchField.getText();
            String status = "Svi statusi".equals(filterStatus.getValue()) ? null : filterStatus.getValue();
            int ukupno = fakturaDao.broji(upit, status, prikazujeArhivu);
            int ukupnoStranica = Math.max(1, (int) Math.ceil((double) ukupno / VELICINA_STRANICE));
            if (trenutnaStranica >= ukupnoStranica) trenutnaStranica = ukupnoStranica - 1;
            fakture.setAll(fakturaDao.vratiStranicu(upit, status, prikazujeArhivu, trenutnaStranica * VELICINA_STRANICE, VELICINA_STRANICE));
            lblStranica.setText("Stranica " + (trenutnaStranica + 1) + " od " + ukupnoStranica + " (" + ukupno + ")");
            btnPret.setDisable(trenutnaStranica == 0);
            btnSled.setDisable(trenutnaStranica >= ukupnoStranica - 1);
        } catch (SQLException e) {
            logger.warning("Greška: " + e.getMessage());
        }
    }

    private void otvoriFormu(Faktura faktura) {
        new FakturaFormaController(faktura, null, this::ucitajFakture).show();
    }

    private void otvoriPdfPreview(Faktura faktura) {
        try {
            Faktura pun = fakturaDao.vratiPoId(faktura.getId());
            Podesavanja firma = new PodesavanjaDao().vratiPodesavanja();
            Klijent klijent = klijentDao.vratiPoId(faktura.getKlijentId());
            Vozilo vozilo = faktura.getVoziloId() != null ? new VoziloDao().vratiPoId(faktura.getVoziloId()) : null;
            new FakturaPdfPreviewController(pun, klijent, vozilo, firma, this::ucitajFakture).show();
        } catch (Exception e) {
            prikaziGresku("Greška pri otvaranju preview-a: " + e.getMessage());
        }
    }

    private void prikaziDetalje(Faktura faktura) {
        try {
            Faktura pun = fakturaDao.vratiPoId(faktura.getId());
            if (pun == null) return;
            Klijent klijent = klijentDao.vratiPoId(faktura.getKlijentId());
            Vozilo vozilo = faktura.getVoziloId() != null ? new VoziloDao().vratiPoId(faktura.getVoziloId()) : null;

            Stage stage = new Stage();
            AppIkona.postavi(stage);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Prilog uz račun — " + faktura.getBrojFakture());

            VBox root = new VBox(14);
            root.setPadding(new Insets(20));
            root.setStyle("-fx-background-color: #f8fafd;");

            HBox header = new HBox(10);
            header.setStyle("-fx-background-color: #0a1628; -fx-padding: 14 16 14 16; -fx-background-radius: 8;");
            header.setAlignment(Pos.CENTER_LEFT);
            Label lblBroj = new Label("Prilog uz račun " + faktura.getBrojFakture());
            lblBroj.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: white;");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Label badge = new Label(faktura.getStatus());
            badge.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 12 4 12; " +
                    "-fx-background-radius: 20; " + statusBoja(faktura.getStatus()));
            header.getChildren().addAll(lblBroj, spacer, badge);

            HBox karticeSred = new HBox(12);
            VBox kCard = detaljiKartica("Klijent",
                    klijent != null ? klijent.getPunoIme() : "—",
                    klijent != null && klijent.getTelefon() != null ? "Tel: " + klijent.getTelefon() : "");
            VBox vCard = detaljiKartica("Vozilo",
                    vozilo != null ? vozilo.getMarka() + " " + vozilo.getModel() : "—",
                    vozilo != null ? "Reg: " + vozilo.getRegistracija() : "");
            VBox dCard = detaljiKartica("Datumi",
                    "Kreirana: " + (faktura.getDatumKreiranja() != null ? faktura.getDatumKreiranja() : "—"),
                    "Plaćanje: " + (faktura.getDatumPlacanja() != null ? faktura.getDatumPlacanja() : "—"));
            HBox.setHgrow(kCard, Priority.ALWAYS);
            HBox.setHgrow(vCard, Priority.ALWAYS);
            HBox.setHgrow(dCard, Priority.ALWAYS);
            karticeSred.getChildren().addAll(kCard, vCard, dCard);

            TableView<FakturaStavka> tbl = new TableView<>();
            tbl.getStyleClass().add("table-view");
            tbl.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            tbl.setPrefHeight(180);
            tbl.setItems(javafx.collections.FXCollections.observableArrayList(pun.getStavke()));

            TableColumn<FakturaStavka, String> cTip = new TableColumn<>("Tip");
            cTip.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTip()));
            cTip.setPrefWidth(70);
            TableColumn<FakturaStavka, String> cNaz = new TableColumn<>("Naziv");
            cNaz.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNaziv()));
            TableColumn<FakturaStavka, String> cKol = new TableColumn<>("Kol.");
            cKol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKolicina() + " " + c.getValue().getJedinicaMere()));
            cKol.setPrefWidth(70);
            TableColumn<FakturaStavka, String> cCena = new TableColumn<>("Cena");
            cCena.setCellValueFactory(c -> new SimpleStringProperty(fmtCena(c.getValue().getCenaBezPdv())));
            cCena.setPrefWidth(100);
            TableColumn<FakturaStavka, String> cUkupno = new TableColumn<>("Ukupno");
            cUkupno.setCellValueFactory(c -> new SimpleStringProperty(fmtCena(c.getValue().iznosUkupno())));
            cUkupno.setPrefWidth(100);
            tbl.getColumns().addAll(cTip, cNaz, cKol, cCena, cUkupno);

            double zaUplatu = pun.zaUplatu();
            Label lblZaUplatu = new Label("Za uplatu: " + fmtCena(zaUplatu));
            lblZaUplatu.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0057b7;");
            HBox rekap = new HBox(lblZaUplatu);
            rekap.setAlignment(Pos.CENTER_RIGHT);

            VBox napBox = new VBox();
            if (pun.getNapomena() != null && !pun.getNapomena().isBlank()) {
                Label lblNap = new Label("Napomena");
                lblNap.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #4a6080;");
                Label txtNap = new Label(pun.getNapomena());
                txtNap.setWrapText(true);
                txtNap.setStyle("-fx-font-size: 12px; -fx-text-fill: #0a1628; " +
                        "-fx-background-color: white; -fx-padding: 8; -fx-background-radius: 6;");
                txtNap.setMaxWidth(Double.MAX_VALUE);
                napBox.getChildren().addAll(lblNap, txtNap);
            }

            Button btnZatvori = new Button("Zatvori");
            btnZatvori.getStyleClass().add("btn-secondary");
            btnZatvori.setOnAction(e -> stage.close());
            HBox bottom = new HBox(btnZatvori);
            bottom.setAlignment(Pos.CENTER_RIGHT);

            root.getChildren().addAll(header, karticeSred, tbl, rekap, napBox, new Separator(), bottom);

            Scene scene = new Scene(root, 660, 560);
            scene.getStylesheets().add(getClass().getResource("/style/style.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();

        } catch (Exception e) {
            prikaziGresku("Greška pri prikazu detalja: " + e.getMessage());
        }
    }

    private VBox detaljiKartica(String naslov, String linija1, String linija2) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: white; -fx-border-color: #d0d9e6; " +
                "-fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");
        Label lblN = new Label(naslov);
        lblN.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #8a9ab5;");
        Label lbl1 = new Label(linija1);
        lbl1.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #0a1628;");
        lbl1.setWrapText(true);
        box.getChildren().addAll(lblN, lbl1);
        if (linija2 != null && !linija2.isBlank()) {
            Label lbl2 = new Label(linija2);
            lbl2.setStyle("-fx-font-size: 11px; -fx-text-fill: #8a9ab5;");
            lbl2.setWrapText(true);
            box.getChildren().add(lbl2);
        }
        return box;
    }

    private void oznaciosPlacenu(Faktura faktura) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Potvrda");
        confirm.setHeaderText(null);
        confirm.setContentText("Označiti prilog uz račun " + faktura.getBrojFakture() + " kao plaćen?");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    fakturaDao.promeniStatus(faktura.getId(), "Plaćena");
                    ucitajFakture();
                } catch (SQLException e) {
                    prikaziGresku("Greška: " + e.getMessage());
                }
            }
        });
    }

    private void arhiviraj(Faktura faktura) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Arhiviranje");
        confirm.setHeaderText(null);
        confirm.setContentText("Arhivirati prilog uz račun " + faktura.getBrojFakture() + "?");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    fakturaDao.arhiviraj(faktura.getId());
                    ucitajFakture();
                } catch (SQLException e) {
                    prikaziGresku("Greška: " + e.getMessage());
                }
            }
        });
    }

    private void vratiIzArhive(Faktura faktura) {
        try {
            fakturaDao.vratiIzArhive(faktura.getId());
            ucitajArhivirane();
        } catch (SQLException e) {
            prikaziGresku("Greška pri vraćanju iz arhive: " + e.getMessage());
        }
    }

    private String statusBoja(String status) {
        return switch (status) {
            case "Kreirana"   -> "-fx-background-color: #dbeafe; -fx-text-fill: #1e40af;";
            case "Poslata"    -> "-fx-background-color: #e0f2fe; -fx-text-fill: #0369a1;";
            case "Plaćena"    -> "-fx-background-color: #dcfce7; -fx-text-fill: #166534;";
            case "Stornirana" -> "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b;";
            default           -> "-fx-background-color: #f1f5f9; -fx-text-fill: #475569;";
        };
    }

    private String fmtCena(double iznos) {
        return String.format("%,.2f RSD", iznos).replace(',', 'X').replace('.', ',').replace('X', '.');
    }

    private void prikaziGresku(String poruka) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Greška"); a.setHeaderText(null); a.setContentText(poruka); a.showAndWait();
    }
}
