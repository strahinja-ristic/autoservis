package autoservis.servis.controller;

import autoservis.servis.dao.FakturaDao;
import autoservis.servis.dao.KlijentDao;
import autoservis.servis.dao.PodesavanjaDao;
import autoservis.servis.dao.RadniNalogDao;
import autoservis.servis.dao.VoziloDao;
import autoservis.servis.model.*;
import autoservis.servis.util.PdfGenerator;
import autoservis.servis.util.AppIkona;
import autoservis.servis.util.PdfPreviewController;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.util.List;

public class RadniNaloziController {

    private BorderPane view;
    private TableView<RadniNalog> tabela;
    private RadniNalogDao nalogDao;
    private KlijentDao klijentDao;
    private VoziloDao voziloDao;
    private ObservableList<RadniNalog> nalozi;
    private TextField searchField;
    private ComboBox<String> filterStatus;
    private boolean prikazujeArhivu = false;
    private int trenutnaStranica = 0;
    private static final int VELICINA_STRANICE = 100;
    private Label lblStranica;
    private Button btnPret;
    private Button btnSled;

    public RadniNaloziController() {
        this.nalogDao = new RadniNalogDao();
        this.klijentDao = new KlijentDao();
        this.voziloDao = new VoziloDao();
        this.nalozi = FXCollections.observableArrayList();
        this.view = new BorderPane();
        kreirajUI();
        ucitajNaloge();
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
        Label bold = new Label("Radni nalozi");
        bold.getStyleClass().add("topbar-title-bold");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        searchField = new TextField();
        searchField.setPromptText("Pretraži naloge...");
        searchField.getStyleClass().add("search-field");
        searchField.textProperty().addListener((obs, old, novo) -> pretrazi(novo));

        filterStatus = new ComboBox<>();
        filterStatus.getItems().addAll("Svi statusi", "Primljeno", "U radu", "Završeno");
        filterStatus.setValue("Svi statusi");
        filterStatus.getStyleClass().add("search-field");
        filterStatus.setPrefWidth(140);
        filterStatus.setOnAction(e -> pretrazi(searchField.getText()));

        Button btnNovi = new Button("+ Novi nalog");
        btnNovi.getStyleClass().add("btn-primary");
        btnNovi.setOnAction(e -> otvoriFormu(null));

        Button btnArhiva = new Button("Prikaži arhivirane");
        btnArhiva.getStyleClass().add("btn-secondary");
        btnArhiva.setOnAction(e -> {
            prikazujeArhivu = !prikazujeArhivu;
            trenutnaStranica = 0;
            btnArhiva.setText(prikazujeArhivu ? "Prikaži aktivne" : "Prikaži arhivirane");
            if (prikazujeArhivu) ucitajArhivirane(); else ucitajNaloge();
        });

        topbar.getChildren().addAll(breadcrumb, bold, spacer, searchField, filterStatus, btnArhiva, btnNovi);

        // Tabela
        tabela = new TableView<>();
        tabela.getStyleClass().add("table-view");
        tabela.setItems(nalozi);
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<RadniNalog, String> colBroj = new TableColumn<>("Broj naloga");
        colBroj.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBrojNaloga()));
        colBroj.setPrefWidth(100);

        TableColumn<RadniNalog, String> colKlijent = new TableColumn<>("Klijent");
        colKlijent.setCellValueFactory(c -> {
            try {
                Klijent k = klijentDao.vratiPoId(c.getValue().getKlijentId());
                return new SimpleStringProperty(k != null ? k.getPunoIme() : "");
            } catch (SQLException e) {
                return new SimpleStringProperty("");
            }
        });

        TableColumn<RadniNalog, String> colVozilo = new TableColumn<>("Vozilo");
        colVozilo.setCellValueFactory(c -> {
            try {
                Vozilo v = voziloDao.vratiPoId(c.getValue().getVoziloId());
                return new SimpleStringProperty(v != null ? v.toString() : "");
            } catch (SQLException e) {
                return new SimpleStringProperty("");
            }
        });

        TableColumn<RadniNalog, String> colDatum = new TableColumn<>("Datum prijema");
        colDatum.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDatumPrijema()));
        colDatum.setPrefWidth(110);

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

        TableColumn<RadniNalog, String> colAkcije = new TableColumn<>("Akcije");
        colAkcije.setMinWidth(340);
        colAkcije.setPrefWidth(Region.USE_COMPUTED_SIZE);
        colAkcije.setCellFactory(col -> new TableCell<>() {
            private final Button btnIzmeni = new Button("Izmeni");
            private final Button btnStatus = new Button("Status");
            private final Button btnPdf = new Button("PDF");
            private final Button btnFaktura = new Button("Napravi prilog");
            private final Button btnArhiviraj = new Button("Arhiviraj");
            private final Button btnVrati = new Button("Vrati iz arhive");
            private final HBox box = new HBox(4, btnIzmeni, btnStatus, btnPdf, btnFaktura, btnArhiviraj);
            private final HBox boxArhiva = new HBox(4, btnVrati);

            {
                btnIzmeni.getStyleClass().add("btn-secondary");
                btnStatus.getStyleClass().add("btn-secondary");
                btnPdf.getStyleClass().add("btn-secondary");
                btnFaktura.getStyleClass().add("btn-primary");
                btnArhiviraj.getStyleClass().add("btn-danger");
                btnVrati.getStyleClass().add("btn-secondary");

                box.setAlignment(Pos.CENTER);
                boxArhiva.setAlignment(Pos.CENTER);

                btnIzmeni.setOnAction(e -> {
                    RadniNalog rn = getTableView().getItems().get(getIndex());
                    otvoriFormu(rn);
                });

                btnStatus.setOnAction(e -> {
                    RadniNalog rn = getTableView().getItems().get(getIndex());
                    promeniStatus(rn);
                });

                btnPdf.setOnAction(e -> {
                    RadniNalog rn = getTableView().getItems().get(getIndex());
                    generisiPdf(rn);
                });

                btnFaktura.setOnAction(e -> {
                    RadniNalog rn = getTableView().getItems().get(getIndex());
                    kreirajFakturuIzNaloga(rn);
                });

                btnArhiviraj.setOnAction(e -> {
                    RadniNalog rn = getTableView().getItems().get(getIndex());
                    arhivirajNalog(rn);
                });

                btnVrati.setOnAction(e -> {
                    RadniNalog rn = getTableView().getItems().get(getIndex());
                    vratiIzArhive(rn);
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : (prikazujeArhivu ? boxArhiva : box));
            }
        });

        tabela.getColumns().addAll(colBroj, colKlijent, colVozilo, colDatum, colStatus, colAkcije);

        tabela.setRowFactory(tv -> {
            TableRow<RadniNalog> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (e.getClickCount() == 2 && !row.isEmpty()) {
                    prikaziDetaljeNaloga(row.getItem());
                }
            });
            return row;
        });

        VBox content = new VBox(0);
        content.getStyleClass().add("content-area");
        VBox.setVgrow(tabela, Priority.ALWAYS);
        content.getChildren().add(tabela);

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

    private void prikaziDetaljeNaloga(RadniNalog nalog) {
        try {
            RadniNalog pun = nalogDao.vratiPoId(nalog.getId());
            Klijent klijent = klijentDao.vratiPoId(nalog.getKlijentId());
            Vozilo vozilo = voziloDao.vratiPoId(nalog.getVoziloId());

            Stage stage = new Stage();
            AppIkona.postavi(stage);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Detalji naloga — " + nalog.getBrojNaloga());

            VBox root = new VBox(14);
            root.setPadding(new Insets(20));
            root.setStyle("-fx-background-color: #f8fafd;");

            HBox header = new HBox(10);
            header.setStyle("-fx-background-color: #0a1628; -fx-padding: 14 16 14 16; -fx-background-radius: 8;");
            header.setAlignment(Pos.CENTER_LEFT);

            Label lblBroj = new Label("Nalog " + nalog.getBrojNaloga());
            lblBroj.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: white;");

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label badge = new Label(nalog.getStatus());
            badge.setStyle(
                    "-fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 12 4 12; -fx-background-radius: 20;" +
                            ("U radu".equals(nalog.getStatus())
                                    ? "-fx-background-color: #fef3c7; -fx-text-fill: #92400e;"
                                    : "Završeno".equals(nalog.getStatus())
                                    ? "-fx-background-color: #dcfce7; -fx-text-fill: #14532d;"
                                    : "-fx-background-color: #dbeafe; -fx-text-fill: #1e40af;")
            );
            header.getChildren().addAll(lblBroj, spacer, badge);

            HBox kvGrid = new HBox(12);
            VBox klijentBox = kreirajDetaljiKartica("Klijent",
                    klijent != null ? klijent.getPunoIme() : "—",
                    klijent != null && klijent.getTelefon() != null ? "Tel: " + klijent.getTelefon() : "");
            VBox voziloBox = kreirajDetaljiKartica("Vozilo",
                    vozilo != null ? vozilo.getMarka() + " " + vozilo.getModel() : "—",
                    vozilo != null ? "Reg: " + vozilo.getRegistracija() + " | " + nalog.getKilometrazaPrijema() + " km" : "");
            VBox datumBox = kreirajDetaljiKartica("Datumi",
                    "Prijem: " + (nalog.getDatumPrijema() != null ? nalog.getDatumPrijema() : "—"),
                    "Završetak: " + (nalog.getDatumZavrsetka() != null && !nalog.getDatumZavrsetka().isBlank()
                            ? nalog.getDatumZavrsetka() : "—"));
            HBox.setHgrow(klijentBox, Priority.ALWAYS);
            HBox.setHgrow(voziloBox, Priority.ALWAYS);
            HBox.setHgrow(datumBox, Priority.ALWAYS);
            kvGrid.getChildren().addAll(klijentBox, voziloBox, datumBox);

            VBox opisBox = new VBox(6);
            if (nalog.getOpisKvara() != null && !nalog.getOpisKvara().isBlank()) {
                Label lblOpis = new Label("Opis kvara");
                lblOpis.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #4a6080;");
                Label txtOpis = new Label(nalog.getOpisKvara());
                txtOpis.setWrapText(true);
                txtOpis.setStyle("-fx-font-size: 12px; -fx-text-fill: #0a1628; " +
                        "-fx-background-color: white; -fx-padding: 8; -fx-background-radius: 6;");
                txtOpis.setMaxWidth(Double.MAX_VALUE);
                opisBox.getChildren().addAll(lblOpis, txtOpis);
            }

            VBox uslugeBox = new VBox(6);
            Label lblUsluge = new Label("Izvršene usluge");
            lblUsluge.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #4a6080;");
            uslugeBox.getChildren().add(lblUsluge);
            if (pun.getUsluge().isEmpty()) {
                Label prazno = new Label("—");
                prazno.setStyle("-fx-font-size: 12px; -fx-text-fill: #8a9ab5;");
                uslugeBox.getChildren().add(prazno);
            } else {
                for (NalogUsluga u : pun.getUsluge()) {
                    Label lbl = new Label("• " + u.getNaziv());
                    lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #0a1628;");
                    uslugeBox.getChildren().add(lbl);
                }
            }

            VBox artikliBox = new VBox(6);
            Label lblArtikli = new Label("Ugrađeni artikli");
            lblArtikli.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #4a6080;");
            artikliBox.getChildren().add(lblArtikli);
            if (pun.getArtikli().isEmpty()) {
                Label prazno = new Label("—");
                prazno.setStyle("-fx-font-size: 12px; -fx-text-fill: #8a9ab5;");
                artikliBox.getChildren().add(prazno);
            } else {
                for (var a : pun.getArtikli()) {
                    Label lbl = new Label("• " + a.getNazivArtikla() + " — " +
                            a.getKolicina() + " " + a.getJedinicaMere());
                    lbl.setStyle("-fx-font-size: 12px; -fx-text-fill: #0a1628;");
                    artikliBox.getChildren().add(lbl);
                }
            }

            HBox uslugeArtikliGrid = new HBox(16, uslugeBox, artikliBox);
            HBox.setHgrow(uslugeBox, Priority.ALWAYS);
            HBox.setHgrow(artikliBox, Priority.ALWAYS);

            Button btnZatvori = new Button("Zatvori");
            btnZatvori.getStyleClass().add("btn-secondary");
            btnZatvori.setOnAction(e -> stage.close());
            HBox bottom = new HBox(btnZatvori);
            bottom.setAlignment(Pos.CENTER_RIGHT);

            root.getChildren().addAll(header, kvGrid, opisBox, uslugeArtikliGrid, new Separator(), bottom);

            Scene scene = new Scene(root, 620, 500);
            scene.getStylesheets().add(getClass().getResource("/style/style.css").toExternalForm());
            stage.setScene(scene);
            stage.showAndWait();

        } catch (Exception e) {
            prikaziGresku("Greška pri prikazu detalja: " + e.getMessage());
        }
    }

    private VBox kreirajDetaljiKartica(String naslov, String linija1, String linija2) {
        VBox box = new VBox(4);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: white; -fx-border-color: #d0d9e6; " +
                "-fx-border-width: 1; -fx-border-radius: 8; -fx-background-radius: 8;");
        Label lblNaslov = new Label(naslov);
        lblNaslov.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #8a9ab5;");
        Label lblL1 = new Label(linija1);
        lblL1.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #0a1628;");
        lblL1.setWrapText(true);
        box.getChildren().addAll(lblNaslov, lblL1);
        if (linija2 != null && !linija2.isBlank()) {
            Label lblL2 = new Label(linija2);
            lblL2.setStyle("-fx-font-size: 11px; -fx-text-fill: #8a9ab5;");
            lblL2.setWrapText(true);
            box.getChildren().add(lblL2);
        }
        return box;
    }

    private void ucitajArhivirane() {
        reload();
    }

    private void ucitajNaloge() {
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
            int ukupno = nalogDao.broji(upit, status, prikazujeArhivu);
            int ukupnoStranica = Math.max(1, (int) Math.ceil((double) ukupno / VELICINA_STRANICE));
            if (trenutnaStranica >= ukupnoStranica) trenutnaStranica = ukupnoStranica - 1;
            nalozi.setAll(nalogDao.vratiStranicu(upit, status, prikazujeArhivu, trenutnaStranica * VELICINA_STRANICE, VELICINA_STRANICE));
            lblStranica.setText("Stranica " + (trenutnaStranica + 1) + " od " + ukupnoStranica + " (" + ukupno + ")");
            btnPret.setDisable(trenutnaStranica == 0);
            btnSled.setDisable(trenutnaStranica >= ukupnoStranica - 1);
        } catch (SQLException e) {
            prikaziGresku("Greška: " + e.getMessage());
        }
    }

    private void otvoriFormu(RadniNalog nalog) {
        RadniNalogFormaController forma = new RadniNalogFormaController(nalog, () -> ucitajNaloge());
        forma.show();
    }

    private void kreirajFakturuIzNaloga(RadniNalog nalog) {
        try {
            String postojecaBroj = new FakturaDao().postojiZaNalog(nalog.getId());
            if (postojecaBroj != null) {
                Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                alert.setTitle("Prilog uz račun već postoji");
                alert.setHeaderText(null);
                alert.setContentText("Za radni nalog " + nalog.getBrojNaloga() +
                        " već postoji prilog uz račun " + postojecaBroj + ". Da li želiš da kreiras novi?");
                if (alert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
            }
            RadniNalog pun = nalogDao.vratiPoId(nalog.getId());
            new FakturaFormaController(pun, this::ucitajNaloge).show();
        } catch (Exception e) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setTitle("Greška"); a.setHeaderText(null);
            a.setContentText("Greška: " + e.getMessage());
            a.showAndWait();
        }
    }

    private void promeniStatus(RadniNalog nalog) {
        String[] statusi = {"Primljeno", "U radu", "Završeno"};
        ChoiceDialog<String> dialog = new ChoiceDialog<>(nalog.getStatus(), statusi);
        dialog.setTitle("Promena statusa");
        dialog.setHeaderText("Nalog: " + nalog.getBrojNaloga());
        dialog.setContentText("Odaberi novi status:");

        dialog.showAndWait().ifPresent(noviStatus -> {
            try {
                if ("Završeno".equals(noviStatus) && !"Završeno".equals(nalog.getStatus())) {
                    nalogDao.zavrsiBNalog(nalog.getId());
                } else {
                    nalogDao.promeniStatus(nalog.getId(), noviStatus);
                }
                ucitajNaloge();
            } catch (SQLException e) {
                prikaziGresku("Greška pri promeni statusa: " + e.getMessage());
            }
        });
    }

    private void generisiPdf(RadniNalog nalog) {
        try {
            RadniNalog pun = nalogDao.vratiPoId(nalog.getId());
            Klijent klijent = klijentDao.vratiPoId(nalog.getKlijentId());
            Vozilo vozilo = voziloDao.vratiPoId(nalog.getVoziloId());
            PodesavanjaDao podesavanjaDao = new PodesavanjaDao();
            Podesavanja firma = podesavanjaDao.vratiPodesavanja();

            List<String> uslugeNazivi = pun.getUsluge().stream()
                    .map(autoservis.servis.model.NalogUsluga::getNaziv)
                    .collect(java.util.stream.Collectors.toList());
            PdfPreviewController preview = new PdfPreviewController(
                    pun, klijent, vozilo, firma,
                    uslugeNazivi, pun.getArtikli()
            );
            preview.show();

        } catch (Exception e) {
            prikaziGresku("Greška: " + e.getMessage());
        }
    }

    private void arhivirajNalog(RadniNalog nalog) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Potvrda");
        alert.setHeaderText("Arhiviranje naloga");
        alert.setContentText("Da li ste sigurni da želite da arhivirate nalog: " + nalog.getBrojNaloga() + "?");

        alert.showAndWait().ifPresent(result -> {
            if (result == ButtonType.OK) {
                try {
                    nalogDao.arhiviraj(nalog.getId());
                    ucitajNaloge();
                } catch (SQLException e) {
                    prikaziGresku("Greška pri arhiviranju: " + e.getMessage());
                }
            }
        });
    }

    private void vratiIzArhive(RadniNalog nalog) {
        try {
            nalogDao.vratiIzArhive(nalog.getId());
            ucitajArhivirane();
        } catch (SQLException e) {
            prikaziGresku("Greška pri vraćanju iz arhive: " + e.getMessage());
        }
    }

    private void prikaziGresku(String poruka) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greška");
        alert.setContentText(poruka);
        alert.showAndWait();
    }
}