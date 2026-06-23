package autoservis.servis.controller;

import autoservis.servis.dao.*;
import autoservis.servis.model.*;
import autoservis.servis.model.NalogUsluga;
import autoservis.servis.util.AppIkona;
import autoservis.servis.util.AutoCompleteField;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RadniNalogFormaController {

    private RadniNalog nalog;
    private Predracun izPredracuna;
    private Runnable onSave;
    private RadniNalogDao nalogDao;
    private KlijentDao klijentDao;
    private VoziloDao voziloDao;
    private ArtikalDao artikalDao;
    private PredracunDao predracunDao;
    private Stage stage;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private Label lblBrojNaloga;
    private AutoCompleteField<Klijent> acKlijent;
    private AutoCompleteField<Vozilo> acVozilo;
    private AutoCompleteField<Artikal> acArtikal;
    private AutoCompleteField<SablonUsluge> acSablon;
    private TextField tfKilometraza;
    private DatePicker dpPrijem;
    private DatePicker dpZavrsetak;
    private TextArea taOpisKvara;
    private TextArea taZahtevKlijenta;
    private TextArea taOstecenja;
    private TextField tfSledeciKm;
    private TextArea taNapomena;
    private TextField tfFaktura;
    private DatePicker dpSledeciDatum;
    private VBox istorijaPanel;

    private ObservableList<NalogUsluga> usluge = FXCollections.observableArrayList();
    private ListView<NalogUsluga> lvUsluge;
    private TextField tfNovaUsluga;

    private ObservableList<NalogArtikal> artikli = FXCollections.observableArrayList();
    private TableView<NalogArtikal> tabelaArtikli;
    private TextField tfKolicina;

    public RadniNalogFormaController(RadniNalog nalog, Runnable onSave) {
        this.nalog = nalog;
        this.onSave = onSave;
        this.nalogDao = new RadniNalogDao();
        this.klijentDao = new KlijentDao();
        this.voziloDao = new VoziloDao();
        this.artikalDao = new ArtikalDao();
    }

    public RadniNalogFormaController(Predracun izPredracuna, Runnable onSave) {
        this.nalog = null;
        this.izPredracuna = izPredracuna;
        this.onSave = onSave;
        this.nalogDao = new RadniNalogDao();
        this.klijentDao = new KlijentDao();
        this.voziloDao = new VoziloDao();
        this.artikalDao = new ArtikalDao();
        this.predracunDao = new PredracunDao();
    }

    public void show() {
        stage = new Stage();
        AppIkona.postavi(stage);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(nalog == null ? "Novi radni nalog" : "Izmena naloga - " + nalog.getBrojNaloga());
        stage.setMinWidth(650);
        stage.setMinHeight(600);

        VBox root = new VBox(14);
        root.setPadding(new Insets(20));
        root.getStyleClass().add("form-card");

        // Broj naloga
        HBox brojBox = new HBox(10);
        brojBox.setAlignment(Pos.CENTER_LEFT);
        Label lblBroj = new Label("Broj naloga:");
        lblBroj.getStyleClass().add("form-label");
        lblBrojNaloga = new Label();
        lblBrojNaloga.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0057b7;");
        try {
            lblBrojNaloga.setText(nalog == null ? nalogDao.generisiBrojNaloga() : nalog.getBrojNaloga());
        } catch (SQLException e) {
            lblBrojNaloga.setText("N/A");
        }
        brojBox.getChildren().addAll(lblBroj, lblBrojNaloga);

        // Ucitaj autocomplete polja
        ucitajKlijente();
        ucitajVozila(0);
        ucitajArtikle();
        ucitajSablone();

        // Kilometraza i datumi
        tfKilometraza = new TextField();
        tfKilometraza.setPromptText("Kilometraža pri prijemu");
        tfKilometraza.getStyleClass().add("form-field");

        dpPrijem = new DatePicker(LocalDate.now());
        dpPrijem.setMaxWidth(Double.MAX_VALUE);

        dpZavrsetak = new DatePicker(nalog == null ? LocalDate.now() : null);
        dpZavrsetak.setMaxWidth(Double.MAX_VALUE);

        taOpisKvara = new TextArea();
        taOpisKvara.setPromptText("Opis kvara, simptomi...");
        taOpisKvara.setPrefRowCount(2);
        taOpisKvara.setWrapText(true);
        taOpisKvara.getStyleClass().add("form-textarea");

        taZahtevKlijenta = new TextArea();
        taZahtevKlijenta.setPromptText("Šta klijent traži / zahteva...");
        taZahtevKlijenta.setPrefRowCount(2);
        taZahtevKlijenta.setWrapText(true);
        taZahtevKlijenta.getStyleClass().add("form-textarea");

        taOstecenja = new TextArea();
        taOstecenja.setPromptText("Vidljiva oštećenja pri prijemu...");
        taOstecenja.setPrefRowCount(2);
        taOstecenja.setWrapText(true);
        taOstecenja.getStyleClass().add("form-textarea");

        taNapomena = new TextArea();
        taNapomena.setPromptText("Napomena koja će biti prikazana na PDF-u...");
        taNapomena.setPrefRowCount(2);
        taNapomena.setWrapText(true);
        taNapomena.getStyleClass().add("form-textarea");

        tfFaktura = new TextField();
        tfFaktura.setPromptText("Broj priloga uz račun (nije obavezno, ne pojavljuje se na PDF-u)");
        tfFaktura.getStyleClass().add("form-field");

        tfSledeciKm = new TextField();
        tfSledeciKm.setPromptText("Naredni servis na km");
        tfSledeciKm.getStyleClass().add("form-field");

        dpSledeciDatum = new DatePicker();
        dpSledeciDatum.setMaxWidth(Double.MAX_VALUE);
        dpSledeciDatum.setPromptText("ili na datum");

        HBox sledeciBox = new HBox(10, tfSledeciKm, dpSledeciDatum);
        HBox.setHgrow(tfSledeciKm, Priority.ALWAYS);
        HBox.setHgrow(dpSledeciDatum, Priority.ALWAYS);

        // Grid
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(10);
        grid.getColumnConstraints().addAll(kolona(50), kolona(50));

        acKlijent.getTextField().setMaxWidth(Double.MAX_VALUE);
        acVozilo.getTextField().setMaxWidth(Double.MAX_VALUE);

        grid.add(kreirajRedNode("Klijent *", acKlijent.getTextField()), 0, 0);
        grid.add(kreirajRedNode("Vozilo *", acVozilo.getTextField()), 1, 0);
        grid.add(kreirajRed("Kilometraža pri prijemu", tfKilometraza), 0, 1);
        grid.add(kreirajRed("Datum prijema", dpPrijem), 1, 1);
        grid.add(kreirajRed("Datum završetka", dpZavrsetak), 0, 2);

        // Usluge
        Label lblUsluge = new Label("Izvršene usluge");
        lblUsluge.getStyleClass().add("form-label");

        lvUsluge = new ListView<>(usluge);
        lvUsluge.setPrefHeight(120);
        lvUsluge.setCellFactory(lv -> new javafx.scene.control.ListCell<>() {
            @Override
            protected void updateItem(NalogUsluga item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getNaziv());
            }
        });

        tfNovaUsluga = new TextField();
        tfNovaUsluga.setPromptText("Upiši uslugu pa pritisni Dodaj...");
        tfNovaUsluga.getStyleClass().add("form-field");
        tfNovaUsluga.setMaxWidth(Double.MAX_VALUE);

        Button btnDodajUslugu = new Button("Dodaj");
        btnDodajUslugu.getStyleClass().add("btn-secondary");
        btnDodajUslugu.setOnAction(e -> dodajUslugu());

        Button btnUkloniUslugu = new Button("Ukloni");
        btnUkloniUslugu.getStyleClass().add("btn-danger");
        btnUkloniUslugu.setOnAction(e -> {
            NalogUsluga selected = lvUsluge.getSelectionModel().getSelectedItem();
            if (selected != null) usluge.remove(selected);
        });

        acSablon.getTextField().setMaxWidth(Double.MAX_VALUE);
        HBox uslugaBox = new HBox(8, tfNovaUsluga, btnDodajUslugu, btnUkloniUslugu);
        HBox uslugaSablonBox = new HBox(8, acSablon.getTextField());
        HBox.setHgrow(tfNovaUsluga, Priority.ALWAYS);
        HBox.setHgrow(acSablon.getTextField(), Priority.ALWAYS);

        // Artikli
        Label lblArtikli = new Label("Ugrađeni / utrošeni artikli");
        lblArtikli.getStyleClass().add("form-label");

        tabelaArtikli = new TableView<>(artikli);
        tabelaArtikli.setPrefHeight(130);
        tabelaArtikli.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        TableColumn<NalogArtikal, String> colNaziv = new TableColumn<>("Artikal");
        colNaziv.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getNazivArtikla()));

        TableColumn<NalogArtikal, String> colKol = new TableColumn<>("Količina");
        colKol.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getKolicina() + " " + c.getValue().getJedinicaMere()));
        colKol.setPrefWidth(100);

        TableColumn<NalogArtikal, String> colUkloni = new TableColumn<>("");
        colUkloni.setPrefWidth(80);
        colUkloni.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Ukloni");
            {
                btn.getStyleClass().add("btn-danger");
                btn.setOnAction(e -> artikli.remove(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        tabelaArtikli.getColumns().addAll(colNaziv, colKol, colUkloni);

        tfKolicina = new TextField("1");
        tfKolicina.setPromptText("Količina");
        tfKolicina.getStyleClass().add("form-field");
        tfKolicina.setPrefWidth(90);

        Button btnDodajArtikal = new Button("Dodaj");
        btnDodajArtikal.getStyleClass().add("btn-secondary");
        btnDodajArtikal.setOnAction(e -> dodajArtikal());

        acArtikal.getTextField().setMaxWidth(Double.MAX_VALUE);
        HBox artikalBox = new HBox(8, acArtikal.getTextField(), tfKolicina, btnDodajArtikal);
        HBox.setHgrow(acArtikal.getTextField(), Priority.ALWAYS);

        // Dugmad
        Button btnSacuvaj = new Button("Sačuvaj");
        btnSacuvaj.getStyleClass().add("btn-primary");
        btnSacuvaj.setOnAction(e -> sacuvaj());

        Button btnOtkazi = new Button("Otkaži");
        btnOtkazi.getStyleClass().add("btn-secondary");
        btnOtkazi.setOnAction(e -> stage.close());

        HBox dugmad = new HBox(8, btnSacuvaj, btnOtkazi);
        dugmad.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(
                brojBox,
                grid,
                kreirajRed("Opis kvara", taOpisKvara),
                kreirajRed("Zahtev klijenta", taZahtevKlijenta),
                kreirajRed("Vidljiva oštećenja pri prijemu", taOstecenja),
                kreirajRed("Naredni servis (km / datum)", sledeciBox),
                new Separator(),
                lblUsluge, lvUsluge, uslugaSablonBox, uslugaBox,
                new Separator(),
                lblArtikli, tabelaArtikli, artikalBox,
                new Separator(),
                kreirajRed("Napomena", taNapomena),
                kreirajRed("Prilog uz račun", tfFaktura),
                new Separator(),
                dugmad
        );

        if (nalog != null) {
            popuniFormu();
        } else if (izPredracuna != null) {
            popuniIzPredracuna();
        }

        // Leva strana
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setContent(root);
        scroll.setPrefWidth(680);

        // Desna strana - istorija
        istorijaPanel = new VBox(10);
        istorijaPanel.setPrefWidth(280);
        istorijaPanel.setMinWidth(280);
        istorijaPanel.setMaxWidth(280);
        istorijaPanel.setPadding(new Insets(16));
        istorijaPanel.setStyle(
                "-fx-background-color: #f8fafd;" +
                        "-fx-border-color: #d0d9e6;" +
                        "-fx-border-width: 0 0 0 1;"
        );

        Label lblIstorija = new Label("Istorija vozila");
        lblIstorija.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0a1628;");

        Label lblIstorijaInfo = new Label("Odaberite vozilo da vidite istoriju servisa.");
        lblIstorijaInfo.setStyle("-fx-font-size: 11px; -fx-text-fill: #8a9ab5;");
        lblIstorijaInfo.setWrapText(true);

        istorijaPanel.getChildren().addAll(lblIstorija, new Separator(), lblIstorijaInfo);

        HBox mainLayout = new HBox(0, scroll, istorijaPanel);
        HBox.setHgrow(scroll, Priority.ALWAYS);

        Scene scene = new Scene(mainLayout, 980, 700);
        scene.getStylesheets().add(getClass().getResource("/style/style.css").toExternalForm());
        stage.setScene(scene);
        stage.showAndWait();
    }

    private void ucitajKlijente() {
        acKlijent = new AutoCompleteField<>(List.of(), Klijent::getPunoIme);
        acKlijent.setPromptText("Pretraži klijenta...");
        acKlijent.setOnOdabrano(k -> ucitajVozila(k.getId()));
        try {
            acKlijent.setElementi(klijentDao.vratiSve());
        } catch (SQLException e) {
            prikaziGresku("Greška pri učitavanju klijenata.");
        }
    }

    private void ucitajVozila(int klijentId) {
        if (acVozilo == null) {
            acVozilo = new AutoCompleteField<>(List.of(), Vozilo::toString);
            acVozilo.setFilterFunkcija(v -> v.getMarka() + " " + v.getModel() + " " +
                    (v.getRegistracija() != null ? v.getRegistracija() : "") + " " +
                    (v.getBrojSasije() != null ? v.getBrojSasije() : ""));
            acVozilo.setPromptText("Pretraži vozilo...");
            acVozilo.setOnOdabrano(v -> {
                if (nalog == null) {
                    tfKilometraza.setText(String.valueOf(v.getKilometraza()));
                }
                ucitajIstorijaVozila(v);
                try {
                    Klijent k = klijentDao.vratiPoId(v.getKlijentId());
                    if (k != null) acKlijent.postavi(k);
                } catch (SQLException ignored) {}
            });
        }
        try {
            List<Vozilo> lista = klijentId == 0
                    ? voziloDao.pretrazi("")
                    : voziloDao.vratiPoKlijentu(klijentId);
            acVozilo.setElementi(lista);
            acVozilo.setPromptText(lista.isEmpty() ? "Klijent nema vozila" : "Pretraži vozilo...");
        } catch (SQLException e) {
            prikaziGresku("Greška pri učitavanju vozila.");
        }
    }

    private void ucitajArtikle() {
        try {
            List<Artikal> lista = artikalDao.vratiSve();
            acArtikal = new AutoCompleteField<>(lista, Artikal::getNaziv);
            acArtikal.setFilterFunkcija(a -> a.getNaziv() + " " + (a.getSifra() != null ? String.valueOf(a.getSifra()) : ""));
            acArtikal.setPriorityFunkcija((a, q) -> {
                String s = a.getSifra() != null ? String.valueOf(a.getSifra()) : "";
                if (s.equals(q)) return 0;
                if (s.startsWith(q)) return 1;
                if (s.contains(q)) return 2;
                return 3;
            });
            acArtikal.setPromptText("Pretraži artikal...");
        } catch (SQLException e) {
            prikaziGresku("Greška pri učitavanju artikala.");
        }
    }

    private void ucitajSablone() {
        try {
            List<SablonUsluge> lista = new SablonUslugeDao().vratiSve();
            acSablon = new AutoCompleteField<>(lista, SablonUsluge::getNaziv);
            acSablon.setPromptText("Pretraži šablon usluge...");
            acSablon.setOnOdabrano(s -> {
                lvUsluge.getSelectionModel().clearSelection();
                usluge.add(new NalogUsluga(s.getNaziv(), 0));
                javafx.application.Platform.runLater(() -> acSablon.getTextField().clear());
            });
        } catch (SQLException e) {
            prikaziGresku("Greška pri učitavanju šablona.");
        }
    }

    private void ucitajIstorijaVozila(Vozilo vozilo) {
        if (istorijaPanel == null) return;
        istorijaPanel.getChildren().clear();

        Label lblNaslov = new Label("Istorija vozila");
        lblNaslov.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0a1628;");

        Label lblVozilo = new Label(vozilo.getMarka() + " " + vozilo.getModel() + " · " + vozilo.getRegistracija());
        lblVozilo.setStyle("-fx-font-size: 11px; -fx-text-fill: #4a6080;");
        lblVozilo.setWrapText(true);

        istorijaPanel.getChildren().addAll(lblNaslov, lblVozilo, new Separator());

        try {
            RadniNalogDao dao = new RadniNalogDao();
            List<RadniNalog> nalozi = dao.vratiPoVozilu(vozilo.getId());

            if (nalozi.isEmpty()) {
                Label lblPrazno = new Label("Nema prethodnih naloga za ovo vozilo.");
                lblPrazno.setStyle("-fx-font-size: 11px; -fx-text-fill: #8a9ab5;");
                lblPrazno.setWrapText(true);
                istorijaPanel.getChildren().add(lblPrazno);
                return;
            }

            for (RadniNalog rn : nalozi) {
                VBox kartica = new VBox(5);
                kartica.setPadding(new Insets(10));
                kartica.setStyle(
                        "-fx-background-color: #ffffff;" +
                                "-fx-border-color: #d0d9e6;" +
                                "-fx-border-width: 1;" +
                                "-fx-border-radius: 6;" +
                                "-fx-background-radius: 6;"
                );

                HBox header = new HBox(6);
                header.setAlignment(Pos.CENTER_LEFT);

                Label lblBroj = new Label(rn.getBrojNaloga());
                lblBroj.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #0057b7;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Label lblStatus = new Label(rn.getStatus());
                lblStatus.setStyle(
                        "-fx-font-size: 9px; -fx-font-weight: bold; -fx-padding: 2 7 2 7; -fx-background-radius: 10;" +
                                ("Završeno".equals(rn.getStatus())
                                        ? "-fx-background-color: #dcfce7; -fx-text-fill: #14532d;"
                                        : "U radu".equals(rn.getStatus())
                                        ? "-fx-background-color: #fef3c7; -fx-text-fill: #78350f;"
                                        : "-fx-background-color: #dbeafe; -fx-text-fill: #1e40af;")
                );

                header.getChildren().addAll(lblBroj, spacer, lblStatus);

                Label lblDatum = new Label("Prijem: " + (rn.getDatumPrijema() != null ? rn.getDatumPrijema() : "—") +
                        "  |  " + rn.getKilometrazaPrijema() + " km");
                lblDatum.setStyle("-fx-font-size: 10px; -fx-text-fill: #8a9ab5;");

                kartica.getChildren().addAll(header, lblDatum);

                try {
                    RadniNalog pun = dao.vratiPoId(rn.getId());
                    if (pun != null && !pun.getUsluge().isEmpty()) {
                        VBox uslugeBox = new VBox(2);
                        uslugeBox.setPadding(new Insets(4, 0, 0, 0));
                        for (NalogUsluga u : pun.getUsluge()) {
                            Label lblU = new Label("• " + u.getNaziv());
                            lblU.setStyle("-fx-font-size: 10px; -fx-text-fill: #0a1628;");
                            lblU.setWrapText(true);
                            uslugeBox.getChildren().add(lblU);
                        }
                        kartica.getChildren().add(uslugeBox);
                    }
                } catch (Exception ignored) {}

                istorijaPanel.getChildren().add(kartica);
            }

        } catch (SQLException e) {
            Label lblGreska = new Label("Greška pri učitavanju istorije.");
            lblGreska.setStyle("-fx-font-size: 11px; -fx-text-fill: #e3342f;");
            istorijaPanel.getChildren().add(lblGreska);
        }
    }

    private void dodajUslugu() {
        String tekst = tfNovaUsluga.getText().trim();
        if (!tekst.isBlank()) {
            lvUsluge.getSelectionModel().clearSelection();
            usluge.add(new NalogUsluga(tekst, 0));
            tfNovaUsluga.clear();
        }
    }

    private void dodajArtikal() {
        Artikal a = acArtikal.getOdabraniElement();
        if (a == null) {
            prikaziGresku("Odaberi artikal.");
            return;
        }
        if (tfKolicina.getText().isBlank()) {
            prikaziGresku("Upiši količinu.");
            return;
        }
        try {
            double kol = Double.parseDouble(tfKolicina.getText().trim());
            if (kol <= 0) {
                prikaziGresku("Količina mora biti veća od nule.");
                return;
            }
            NalogArtikal na = new NalogArtikal();
            na.setArtikalId(a.getId());
            na.setNazivArtikla(a.getNaziv());
            na.setKolicina(kol);
            na.setJedinicaMere(a.getJedinicaMere());
            artikli.add(na);
            acArtikal.getTextField().clear();
            tfKolicina.setText("1");
        } catch (NumberFormatException e) {
            prikaziGresku("Količina mora biti broj.");
        }
    }

    private void popuniIzPredracuna() {
        try {
            Predracun pun = new PredracunDao().vratiPoId(izPredracuna.getId());
            if (pun == null) return;

            Klijent k = klijentDao.vratiPoId(pun.getKlijentId());
            if (k != null) {
                acKlijent.postavi(k);
                ucitajVozila(k.getId());
                if (pun.getVoziloId() != null) {
                    Vozilo v = voziloDao.vratiPoId(pun.getVoziloId());
                    if (v != null) {
                        acVozilo.postavi(v);
                        tfKilometraza.setText(String.valueOf(v.getKilometraza()));
                        ucitajIstorijaVozila(v);
                    }
                }
            }

            for (PredracunStavka s : pun.getStavke()) {
                if ("Usluga".equals(s.getTip())) {
                    usluge.add(new NalogUsluga(s.getNaziv(), 0));
                } else if ("Artikal".equals(s.getTip())) {
                    Artikal a = artikalDao.vratiPoNazivu(s.getNaziv());
                    if (a != null) {
                        NalogArtikal na = new NalogArtikal();
                        na.setArtikalId(a.getId());
                        na.setNazivArtikla(a.getNaziv());
                        na.setKolicina(s.getKolicina());
                        na.setJedinicaMere(a.getJedinicaMere());
                        artikli.add(na);
                    }
                }
            }

            if (pun.getNapomena() != null) taNapomena.setText(pun.getNapomena());
        } catch (Exception e) {
            prikaziGresku("Greška pri učitavanju predračuna: " + e.getMessage());
        }
    }

    private void popuniFormu() {
        try {
            Klijent k = klijentDao.vratiPoId(nalog.getKlijentId());
            if (k != null) {
                acKlijent.postavi(k);
                ucitajVozila(k.getId());
                Vozilo v = voziloDao.vratiPoId(nalog.getVoziloId());
                if (v != null) acVozilo.postavi(v);
            }
        } catch (SQLException e) {
            prikaziGresku("Greška pri učitavanju podataka.");
        }

        tfKilometraza.setText(String.valueOf(nalog.getKilometrazaPrijema()));

        if (nalog.getDatumPrijema() != null && !nalog.getDatumPrijema().isBlank()) {
            try { dpPrijem.setValue(LocalDate.parse(nalog.getDatumPrijema(), FORMATTER)); } catch (Exception ignored) {}
        }
        if (nalog.getDatumZavrsetka() != null && !nalog.getDatumZavrsetka().isBlank()) {
            try { dpZavrsetak.setValue(LocalDate.parse(nalog.getDatumZavrsetka(), FORMATTER)); } catch (Exception ignored) {}
        }

        taOpisKvara.setText(nalog.getOpisKvara() != null ? nalog.getOpisKvara() : "");
        taZahtevKlijenta.setText(nalog.getZahtevKlijenta() != null ? nalog.getZahtevKlijenta() : "");
        taOstecenja.setText(nalog.getOstecenja() != null ? nalog.getOstecenja() : "");
        taNapomena.setText(nalog.getNapomena() != null ? nalog.getNapomena() : "");
        tfFaktura.setText(nalog.getFaktura() != null ? nalog.getFaktura() : "");

        if (nalog.getSledeciServisKm() != null) tfSledeciKm.setText(String.valueOf(nalog.getSledeciServisKm()));
        if (nalog.getSledeciServisDatum() != null && !nalog.getSledeciServisDatum().isBlank()) {
            try { dpSledeciDatum.setValue(LocalDate.parse(nalog.getSledeciServisDatum(), FORMATTER)); } catch (Exception ignored) {}
        }

        try {
            RadniNalog pun = nalogDao.vratiPoId(nalog.getId());
            if (pun != null) {
                usluge.setAll(pun.getUsluge());
                artikli.setAll(pun.getArtikli());
            }
        } catch (SQLException e) {
            prikaziGresku("Greška pri učitavanju usluga i artikala.");
        }
    }

    private void sacuvaj() {
        if (!validiraj()) return;

        RadniNalog rn = nalog != null ? nalog : new RadniNalog();
        rn.setBrojNaloga(lblBrojNaloga.getText());
        rn.setKlijentId(acKlijent.getOdabraniElement().getId());
        rn.setVoziloId(acVozilo.getOdabraniElement().getId());

        try {
            rn.setKilometrazaPrijema(Integer.parseInt(tfKilometraza.getText().trim()));
        } catch (NumberFormatException e) {
            rn.setKilometrazaPrijema(0);
        }

        rn.setDatumPrijema(dpPrijem.getValue() != null ? dpPrijem.getValue().format(FORMATTER) : "");
        rn.setDatumZavrsetka(dpZavrsetak.getValue() != null ? dpZavrsetak.getValue().format(FORMATTER) : "");
        rn.setOpisKvara(taOpisKvara.getText().trim());
        rn.setZahtevKlijenta(taZahtevKlijenta.getText().trim());
        rn.setOstecenja(taOstecenja.getText().trim());
        rn.setNapomena(taNapomena.getText().trim());
        rn.setFaktura(tfFaktura.getText().trim());
        rn.setStatus(nalog != null ? nalog.getStatus() : "Primljeno");

        if (!tfSledeciKm.getText().isBlank()) {
            try { rn.setSledeciServisKm(Integer.parseInt(tfSledeciKm.getText().trim())); } catch (NumberFormatException ignored) {}
        }
        rn.setSledeciServisDatum(dpSledeciDatum.getValue() != null ? dpSledeciDatum.getValue().format(FORMATTER) : null);

        try {
            if (nalog == null) {
                int id = nalogDao.dodaj(rn);
                for (NalogUsluga u : usluge) nalogDao.dodajUslugu(id, u.getNaziv(), u.getCena());
                for (NalogArtikal a : artikli) nalogDao.dodajArtikal(id, a.getArtikalId(), a.getKolicina());
                voziloDao.azurirajKilometrazу(rn.getVoziloId(), rn.getKilometrazaPrijema());
                if (izPredracuna != null) predracunDao.postaviRadniNalogId(izPredracuna.getId(), id);
            } else {
                nalogDao.izmeni(rn);
                nalogDao.obrisiUsluge(rn.getId());
                nalogDao.obrisiArtikle(rn.getId());
                for (NalogUsluga u : usluge) nalogDao.dodajUslugu(rn.getId(), u.getNaziv(), u.getCena());
                for (NalogArtikal a : artikli) nalogDao.dodajArtikal(rn.getId(), a.getArtikalId(), a.getKolicina());
            }
            onSave.run();
            stage.close();
        } catch (SQLException e) {
            prikaziGresku("Greška pri čuvanju: " + e.getMessage());
        }
    }

    private boolean validiraj() {
        if (acKlijent.getOdabraniElement() == null) { prikaziGresku("Odaberi klijenta."); return false; }
        if (acVozilo.getOdabraniElement() == null) { prikaziGresku("Odaberi vozilo."); return false; }
        if (dpPrijem.getValue() == null) { prikaziGresku("Datum prijema je obavezan."); return false; }
        return true;
    }

    private ColumnConstraints kolona(double procenat) {
        ColumnConstraints cc = new ColumnConstraints();
        cc.setPercentWidth(procenat);
        return cc;
    }

    private VBox kreirajRed(String labelTekst, Control polje) {
        VBox red = new VBox(4);
        Label lbl = new Label(labelTekst);
        lbl.getStyleClass().add("form-label");
        red.getChildren().addAll(lbl, polje);
        return red;
    }

    private VBox kreirajRed(String labelTekst, Pane polje) {
        VBox red = new VBox(4);
        Label lbl = new Label(labelTekst);
        lbl.getStyleClass().add("form-label");
        red.getChildren().addAll(lbl, polje);
        return red;
    }

    private VBox kreirajRedNode(String labelTekst, javafx.scene.Node polje) {
        VBox red = new VBox(4);
        Label lbl = new Label(labelTekst);
        lbl.getStyleClass().add("form-label");
        red.getChildren().addAll(lbl, polje);
        return red;
    }

    private void prikaziGresku(String poruka) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greška");
        alert.setContentText(poruka);
        alert.showAndWait();
    }
}