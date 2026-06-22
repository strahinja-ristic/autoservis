package autoservis.servis.controller;

import autoservis.servis.dao.*;
import autoservis.servis.model.*;
import autoservis.servis.util.AppIkona;
import autoservis.servis.util.FinansijskiPdfPreviewController;
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

public class PredracunListaController {

    private static final Logger logger = Logger.getLogger(PredracunListaController.class.getName());

    private BorderPane view;
    private TableView<Predracun> tabela;
    private PredracunDao predracunDao;
    private KlijentDao klijentDao;
    private ObservableList<Predracun> predracuni;
    private TextField searchField;
    private ComboBox<String> filterStatus;
    private boolean firmaNijePodesen = true;
    private boolean prikazujeArhivu = false;
    private int trenutnaStranica = 0;
    private static final int VELICINA_STRANICE = 100;
    private Label lblStranica;
    private Button btnPret;
    private Button btnSled;

    public PredracunListaController() {
        this.predracunDao = new PredracunDao();
        this.klijentDao = new KlijentDao();
        this.predracuni = FXCollections.observableArrayList();
        try {
            Podesavanja p = new PodesavanjaDao().vratiPodesavanja();
            firmaNijePodesen = p == null || p.getNazivFirme() == null || p.getNazivFirme().isBlank();
        } catch (Exception ignored) {}
        this.view = new BorderPane();
        kreirajUI();
        ucitajPredracune();
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
        Label bold = new Label("Predračuni");
        bold.getStyleClass().add("topbar-title-bold");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        searchField = new TextField();
        searchField.setPromptText("Pretraži predračune...");
        searchField.getStyleClass().add("search-field");
        searchField.textProperty().addListener((obs, old, novo) -> pretrazi(novo));

        filterStatus = new ComboBox<>();
        filterStatus.getItems().addAll("Svi statusi", "Kreiran", "Poslat", "Prihvaćen", "Odbijen", "Istekao", "Fakturisan");
        filterStatus.setValue("Svi statusi");
        filterStatus.getStyleClass().add("search-field");
        filterStatus.setPrefWidth(145);
        filterStatus.setOnAction(e -> pretrazi(searchField.getText()));

        Button btnNovi = new Button("+ Novi predračun");
        btnNovi.getStyleClass().add("btn-primary");
        btnNovi.setOnAction(e -> otvoriFormu(null));

        Button btnArhiva = new Button("Arhivirani");
        btnArhiva.getStyleClass().add("btn-secondary");
        btnArhiva.setOnAction(e -> {
            prikazujeArhivu = !prikazujeArhivu;
            trenutnaStranica = 0;
            btnArhiva.setText(prikazujeArhivu ? "Aktivni" : "Arhivirani");
            if (prikazujeArhivu) ucitajArhivirane(); else ucitajPredracune();
        });

        topbar.getChildren().addAll(breadcrumb, bold, spacer, searchField, filterStatus, btnArhiva, btnNovi);

        // Tabela
        tabela = new TableView<>(predracuni);
        tabela.getStyleClass().add("table-view");
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabela.setPlaceholder(new Label("Nema predračuna"));

        TableColumn<Predracun, String> colBroj = new TableColumn<>("Broj");
        colBroj.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBrojPredracuna()));
        colBroj.setPrefWidth(110);

        TableColumn<Predracun, String> colKlijent = new TableColumn<>("Klijent");
        colKlijent.setCellValueFactory(c -> {
            try {
                Klijent k = klijentDao.vratiPoId(c.getValue().getKlijentId());
                return new SimpleStringProperty(k != null ? k.getPunoIme() : "—");
            } catch (SQLException e) { return new SimpleStringProperty("—"); }
        });

        TableColumn<Predracun, String> colDatum = new TableColumn<>("Datum");
        colDatum.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDatumKreiranja()));
        colDatum.setPrefWidth(100);

        TableColumn<Predracun, String> colVazenje = new TableColumn<>("Važi do");
        colVazenje.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getDatumVazenja() != null ? c.getValue().getDatumVazenja() : "—"));
        colVazenje.setPrefWidth(100);

        TableColumn<Predracun, String> colStatus = new TableColumn<>("Status");
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStatus()));
        colStatus.setPrefWidth(110);
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

        TableColumn<Predracun, String> colIznos = new TableColumn<>("Iznos");
        colIznos.setCellValueFactory(c -> {
            try {
                Predracun p = predracunDao.vratiPoId(c.getValue().getId());
                return new SimpleStringProperty(p != null ? fmtCena(p.zaUplatu()) : "—");
            } catch (SQLException e) { return new SimpleStringProperty("—"); }
        });
        colIznos.setPrefWidth(110);

        TableColumn<Predracun, Void> colAkcije = new TableColumn<>("Akcije");
        colAkcije.setPrefWidth(320);
        colAkcije.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("Izmeni");
            private final Button btnPdf = new Button("PDF");
            private final Button btnFaktura = new Button("Napravi prilog");
            private final Button btnNalog = new Button("Nalog");
            private final Button btnArhiviraj = new Button("Arhiviraj");
            private final Button btnVrati = new Button("Vrati iz arhive");
            {
                btnEdit.getStyleClass().add("btn-secondary");
                btnPdf.getStyleClass().add("btn-secondary");
                btnFaktura.getStyleClass().add("btn-primary");
                btnNalog.getStyleClass().add("btn-secondary");
                btnArhiviraj.getStyleClass().add("btn-danger");
                btnVrati.getStyleClass().add("btn-secondary");

                btnPdf.setDisable(firmaNijePodesen);
                if (firmaNijePodesen)
                    btnPdf.setTooltip(new Tooltip("Popunite podatke o firmi u Podešavanjima"));

                btnEdit.setOnAction(e -> otvoriFormu(getTableView().getItems().get(getIndex())));
                btnPdf.setOnAction(e -> otvoriPdfPreview(getTableView().getItems().get(getIndex())));
                btnFaktura.setOnAction(e -> kreirajFakturuIzPredracuna(getTableView().getItems().get(getIndex())));
                btnNalog.setOnAction(e -> kreirajRadniNalogIzPredracuna(getTableView().getItems().get(getIndex())));
                btnArhiviraj.setOnAction(e -> arhiviraj(getTableView().getItems().get(getIndex())));
                btnVrati.setOnAction(e -> vratiIzArhive(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                if (empty) { setGraphic(null); return; }
                HBox box = prikazujeArhivu
                        ? new HBox(4, btnVrati)
                        : new HBox(4, btnEdit, btnPdf, btnFaktura, btnNalog, btnArhiviraj);
                box.setAlignment(Pos.CENTER_LEFT);
                setGraphic(box);
            }
        });

        tabela.getColumns().addAll(colBroj, colKlijent, colDatum, colVazenje, colStatus, colIznos, colAkcije);

        tabela.setRowFactory(tv -> {
            TableRow<Predracun> row = new TableRow<>();
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

    private static final java.util.Map<String, Integer> STATUS_RED = java.util.Map.of(
            "Kreiran",    0,
            "Poslat",     1,
            "Prihvaćen",  2,
            "Odbijen",    3,
            "Istekao",    4,
            "Fakturisan", 5
    );


    private void ucitajPredracune() {
        try { predracunDao.azurirajIstekle(); } catch (SQLException ignored) {}
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
            int ukupno = predracunDao.broji(upit, status, prikazujeArhivu);
            int ukupnoStranica = Math.max(1, (int) Math.ceil((double) ukupno / VELICINA_STRANICE));
            if (trenutnaStranica >= ukupnoStranica) trenutnaStranica = ukupnoStranica - 1;
            predracuni.setAll(predracunDao.vratiStranicu(upit, status, prikazujeArhivu, trenutnaStranica * VELICINA_STRANICE, VELICINA_STRANICE));
            lblStranica.setText("Stranica " + (trenutnaStranica + 1) + " od " + ukupnoStranica + " (" + ukupno + ")");
            btnPret.setDisable(trenutnaStranica == 0);
            btnSled.setDisable(trenutnaStranica >= ukupnoStranica - 1);
        } catch (SQLException e) {
            logger.warning("Greška: " + e.getMessage());
        }
    }

    private void otvoriFormu(Predracun predracun) {
        new PredracunFormaController(predracun, this::ucitajPredracune).show();
    }

    private void otvoriPdfPreview(Predracun predracun) {
        try {
            Predracun pun = predracunDao.vratiPoId(predracun.getId());
            Podesavanja firma = new PodesavanjaDao().vratiPodesavanja();
            Klijent klijent = klijentDao.vratiPoId(predracun.getKlijentId());
            Vozilo vozilo = predracun.getVoziloId() != null ? new VoziloDao().vratiPoId(predracun.getVoziloId()) : null;
            new FinansijskiPdfPreviewController(pun, klijent, vozilo, firma, this::ucitajPredracune).show();
        } catch (Exception e) {
            prikaziGresku("Greška pri otvaranju preview-a: " + e.getMessage());
        }
    }

    private void prikaziDetalje(Predracun predracun) {
        try {
            Predracun pun = predracunDao.vratiPoId(predracun.getId());
            if (pun == null) return;
            Klijent klijent = klijentDao.vratiPoId(predracun.getKlijentId());
            Vozilo vozilo = predracun.getVoziloId() != null ? new VoziloDao().vratiPoId(predracun.getVoziloId()) : null;

            Stage stage = new Stage();
            AppIkona.postavi(stage);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Predračun — " + predracun.getBrojPredracuna());

            VBox root = new VBox(14);
            root.setPadding(new Insets(20));
            root.setStyle("-fx-background-color: #f8fafd;");

            // Header
            HBox header = new HBox(10);
            header.setStyle("-fx-background-color: #0a1628; -fx-padding: 14 16 14 16; -fx-background-radius: 8;");
            header.setAlignment(Pos.CENTER_LEFT);
            Label lblBroj = new Label("Predračun " + predracun.getBrojPredracuna());
            lblBroj.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: white;");
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            Label badge = new Label(predracun.getStatus());
            badge.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 12 4 12; " +
                    "-fx-background-radius: 20; " + statusBoja(predracun.getStatus()));
            header.getChildren().addAll(lblBroj, spacer, badge);

            // Kartice klijent / vozilo / datum
            HBox karticeSred = new HBox(12);
            VBox kCard = detaljiKartica("Klijent",
                    klijent != null ? klijent.getPunoIme() : "—",
                    klijent != null && klijent.getTelefon() != null ? "Tel: " + klijent.getTelefon() : "");
            VBox vCard = detaljiKartica("Vozilo",
                    vozilo != null ? vozilo.getMarka() + " " + vozilo.getModel() : "—",
                    vozilo != null ? "Reg: " + vozilo.getRegistracija() : "");
            VBox dCard = detaljiKartica("Datumi",
                    "Kreiran: " + (predracun.getDatumKreiranja() != null ? predracun.getDatumKreiranja() : "—"),
                    "Važi do: " + (predracun.getDatumVazenja() != null ? predracun.getDatumVazenja() : "—"));
            HBox.setHgrow(kCard, Priority.ALWAYS);
            HBox.setHgrow(vCard, Priority.ALWAYS);
            HBox.setHgrow(dCard, Priority.ALWAYS);
            karticeSred.getChildren().addAll(kCard, vCard, dCard);

            // Stavke tabela
            TableView<PredracunStavka> tbl = new TableView<>();
            tbl.getStyleClass().add("table-view");
            tbl.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
            tbl.setPrefHeight(180);
            tbl.setItems(javafx.collections.FXCollections.observableArrayList(pun.getStavke()));

            TableColumn<PredracunStavka, String> cTip = new TableColumn<>("Tip");
            cTip.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTip()));
            cTip.setPrefWidth(70);
            TableColumn<PredracunStavka, String> cNaz = new TableColumn<>("Naziv");
            cNaz.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNaziv()));
            TableColumn<PredracunStavka, String> cKol = new TableColumn<>("Kol.");
            cKol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getKolicina() + " " + c.getValue().getJedinicaMere()));
            cKol.setPrefWidth(70);
            TableColumn<PredracunStavka, String> cCena = new TableColumn<>("Cena");
            cCena.setCellValueFactory(c -> new SimpleStringProperty(fmtCena(c.getValue().getCenaBezPdv())));
            cCena.setPrefWidth(100);
            TableColumn<PredracunStavka, String> cUkupno = new TableColumn<>("Ukupno");
            cUkupno.setCellValueFactory(c -> new SimpleStringProperty(fmtCena(c.getValue().iznosUkupno())));
            cUkupno.setPrefWidth(100);
            tbl.getColumns().addAll(cTip, cNaz, cKol, cCena, cUkupno);

            // Rekapitulacija
            double zaUplatu = pun.zaUplatu();
            Label lblZaUplatu = new Label("Za uplatu: " + fmtCena(zaUplatu));
            lblZaUplatu.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0057b7;");
            HBox rekap = new HBox(lblZaUplatu);
            rekap.setAlignment(Pos.CENTER_RIGHT);

            // Napomena
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

    private void kreirajFakturuIzPredracuna(Predracun predracun) {
        try {
            String postojecaBroj = new FakturaDao().postojiZaPredracun(predracun.getId());
            if (postojecaBroj != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Prilog uz račun već postoji");
                alert.setHeaderText(null);
                alert.setContentText("Za predračun " + predracun.getBrojPredracuna() +
                        " već postoji prilog uz račun " + postojecaBroj + ". Da li želiš da kreiras novi?");
                if (alert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
            }
            Predracun pun = predracunDao.vratiPoId(predracun.getId());
            new FakturaFormaController(null, pun, this::ucitajPredracune).show();
        } catch (SQLException e) {
            prikaziGresku("Greška: " + e.getMessage());
        }
    }

    private void kreirajRadniNalogIzPredracuna(Predracun predracun) {
        try {
            Predracun pun = predracunDao.vratiPoId(predracun.getId());
            if (pun.getRadniNalogId() != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Radni nalog već postoji");
                alert.setHeaderText(null);
                alert.setContentText("Za predračun " + predracun.getBrojPredracuna() +
                        " već je kreiran radni nalog. Da li želiš da kreiras novi?");
                if (alert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
            }
            new RadniNalogFormaController(pun, this::ucitajPredracune).show();
        } catch (Exception e) {
            prikaziGresku("Greška: " + e.getMessage());
        }
    }

    private void arhiviraj(Predracun predracun) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Arhiviranje");
        confirm.setHeaderText(null);
        confirm.setContentText("Arhivirati predračun " + predracun.getBrojPredracuna() + "?");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    predracunDao.arhiviraj(predracun.getId());
                    ucitajPredracune();
                } catch (SQLException e) {
                    prikaziGresku("Greška: " + e.getMessage());
                }
            }
        });
    }

    private void vratiIzArhive(Predracun predracun) {
        try {
            predracunDao.vratiIzArhive(predracun.getId());
            ucitajArhivirane();
        } catch (SQLException e) {
            prikaziGresku("Greška pri vraćanju iz arhive: " + e.getMessage());
        }
    }

    private String statusBoja(String status) {
        return switch (status) {
            case "Kreiran"    -> "-fx-background-color: #dbeafe; -fx-text-fill: #1e40af;";
            case "Poslat"     -> "-fx-background-color: #e0f2fe; -fx-text-fill: #0369a1;";
            case "Prihvaćen"  -> "-fx-background-color: #dcfce7; -fx-text-fill: #166534;";
            case "Odbijen"    -> "-fx-background-color: #fee2e2; -fx-text-fill: #991b1b;";
            case "Istekao"    -> "-fx-background-color: #fef3c7; -fx-text-fill: #92400e;";
            case "Fakturisan" -> "-fx-background-color: #f3e8ff; -fx-text-fill: #6b21a8;";
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
