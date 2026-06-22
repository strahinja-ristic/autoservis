package autoservis.servis.controller;

import autoservis.servis.dao.*;
import autoservis.servis.model.*;
import autoservis.servis.util.AppIkona;
import autoservis.servis.util.AutoCompleteField;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.logging.Logger;

public class FakturaFormaController {

    private static final Logger logger = Logger.getLogger(FakturaFormaController.class.getName());
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final Faktura faktura;
    private final Predracun izPredracuna;
    private final RadniNalog izNaloga;
    private final Runnable onSave;
    private Stage stage;

    private FakturaDao fakturaDao;
    private PredracunDao predracunDao;
    private KlijentDao klijentDao;
    private VoziloDao voziloDao;
    private PodesavanjaDao podesavanjaDao;

    private Label lblBrojFakture;
    private AutoCompleteField<Klijent> acKlijent;
    private AutoCompleteField<Vozilo> acVozilo;
    private DatePicker dpDatumKreiranja;
    private DatePicker dpDatumPlacanja;
    private ComboBox<String> cbNacinPlacanja;
    private TextField tfRokPlacanja;
    private TextField tfMestoIzdavanja;
    private TextField tfMestoIsporuke;
    private TextField tfBrojRacuna;
    private TextField tfPfrBroj;
    private TextField tfPopust;
    private TextArea taNapomena;
    private ComboBox<String> cbStatus;

    private ObservableList<FakturaStavka> stavke = FXCollections.observableArrayList();
    private TableView<FakturaStavka> tabelaStavki;

    private AutoCompleteField<Artikal> acArtikal;
    private TextField tfStavkaKolicina;
    private TextField tfStavkaJedinica;
    private TextField tfStavkaCena;
    private static final javafx.collections.ObservableList<String> JEDINICE =
        javafx.collections.FXCollections.observableArrayList(
            "kom", "l", "ml", "kg", "g", "h", "m", "m²", "set", "par", "paket"
        );

    private ComboBox<String> cbStavkaJedinica;
    private TextField tfStavkaPdv;
    private TextField tfStavkaPopust;

    private Label lblUkupnoBezPdv;
    private Label lblUkupniPdv;
    private Label lblGlobalniPopust;
    private Label lblZaUplatu;

    private boolean pdvObveznik;

    public FakturaFormaController(Faktura faktura, Predracun izPredracuna, Runnable onSave) {
        this.faktura = faktura;
        this.izPredracuna = izPredracuna;
        this.izNaloga = null;
        this.onSave = onSave;
        this.fakturaDao = new FakturaDao();
        this.predracunDao = new PredracunDao();
        this.klijentDao = new KlijentDao();
        this.voziloDao = new VoziloDao();
        this.podesavanjaDao = new PodesavanjaDao();
    }

    public FakturaFormaController(RadniNalog izNaloga, Runnable onSave) {
        this.faktura = null;
        this.izPredracuna = null;
        this.izNaloga = izNaloga;
        this.onSave = onSave;
        this.fakturaDao = new FakturaDao();
        this.predracunDao = new PredracunDao();
        this.klijentDao = new KlijentDao();
        this.voziloDao = new VoziloDao();
        this.podesavanjaDao = new PodesavanjaDao();
    }

    public void show() {
        stage = new Stage();
        AppIkona.postavi(stage);
        stage.initModality(Modality.APPLICATION_MODAL);
        String naslov = faktura == null
                ? (izPredracuna != null ? "Novi prilog uz račun iz predračuna " + izPredracuna.getBrojPredracuna() : "Novi prilog uz račun")
                : "Izmena — " + faktura.getBrojFakture();
        stage.setTitle(naslov);
        stage.setMinWidth(1250);
        stage.setMinHeight(720);

        try {
            Podesavanja p = podesavanjaDao.vratiPodesavanja();
            pdvObveznik = p.isPdvObveznik();
        } catch (Exception ignored) {}

        ScrollPane scroll = new ScrollPane(kreirajFormu());
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #f8fafd;");

        Scene scene = new Scene(scroll, 1280, 780);
        scene.getStylesheets().add(getClass().getResource("/style/style.css").toExternalForm());
        stage.setScene(scene);
        stage.showAndWait();
    }

    private VBox kreirajFormu() {
        VBox root = new VBox(16);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #f8fafd;");

        // Zaglavlje
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #0a1628; -fx-padding: 14 18 14 18; -fx-background-radius: 8;");

        lblBrojFakture = new Label("...");
        lblBrojFakture.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: white;");
        try {
            String br = faktura == null ? fakturaDao.generisiBroj() : faktura.getBrojFakture();
            lblBrojFakture.setText(br);
        } catch (SQLException ignored) {}

        header.getChildren().add(lblBrojFakture);

        // Klijent i vozilo
        VBox karticaKV = kreirajKartica("Klijent i vozilo");
        try {
            List<Klijent> klijenti = klijentDao.vratiSve();
            acKlijent = new AutoCompleteField<>(klijenti, Klijent::getPunoIme);
            acKlijent.getTextField().getStyleClass().add("form-field");
            acKlijent.getTextField().setMaxWidth(Double.MAX_VALUE);
            acKlijent.getTextField().setPromptText("Pretraži klijente...");

            acKlijent.setOnOdabrano(k -> {
                if (k == null) { acVozilo.setElementi(List.of()); return; }
                try { acVozilo.setElementi(voziloDao.vratiPoKlijentu(k.getId())); }
                catch (SQLException ignored) {}
            });

            List<Vozilo> svaVozila = voziloDao.pretrazi("");
            acVozilo = new AutoCompleteField<>(svaVozila, Vozilo::toString);
            acVozilo.setFilterFunkcija(v -> v.getMarka() + " " + v.getModel() + " " +
                    (v.getRegistracija() != null ? v.getRegistracija() : "") + " " +
                    (v.getBrojSasije() != null ? v.getBrojSasije() : ""));
            acVozilo.setOnOdabrano(v -> {
                try {
                    Klijent k = klijentDao.vratiPoId(v.getKlijentId());
                    if (k != null) acKlijent.postavi(k);
                } catch (SQLException ignored) {}
            });
            acVozilo.getTextField().getStyleClass().add("form-field");
            acVozilo.getTextField().setMaxWidth(Double.MAX_VALUE);
            acVozilo.getTextField().setPromptText("Vozilo (opcionalno)...");

            GridPane gKV = new GridPane();
            gKV.setHgap(12); gKV.setVgap(10);
            gKV.getColumnConstraints().addAll(kolona(50), kolona(50));
            gKV.add(kreirajRedNode("Klijent *", acKlijent.getTextField()), 0, 0);
            gKV.add(kreirajRedNode("Vozilo", acVozilo.getTextField()), 1, 0);
            karticaKV.getChildren().add(gKV);
        } catch (SQLException e) {
            karticaKV.getChildren().add(new Label("Greška pri učitavanju klijenata."));
        }

        // Datumi i plaćanje
        VBox karticaDatumi = kreirajKartica("Datumi i plaćanje");
        dpDatumKreiranja = new DatePicker(LocalDate.now());
        dpDatumKreiranja.getStyleClass().add("form-field");
        dpDatumKreiranja.setMaxWidth(Double.MAX_VALUE);

        dpDatumPlacanja = new DatePicker();
        dpDatumPlacanja.getStyleClass().add("form-field");
        dpDatumPlacanja.setMaxWidth(Double.MAX_VALUE);
        try {
            int rok = podesavanjaDao.vratiPodesavanja().getDefaultRokPlacanja();
            dpDatumPlacanja.setValue(LocalDate.now().plusDays(rok));
        } catch (Exception ignored) { dpDatumPlacanja.setValue(LocalDate.now().plusDays(15)); }

        cbNacinPlacanja = new ComboBox<>(FXCollections.observableArrayList("Gotovina", "Kartica", "Prenos"));
        cbNacinPlacanja.setValue("Prenos");
        cbNacinPlacanja.getStyleClass().add("search-field");
        cbNacinPlacanja.setMaxWidth(Double.MAX_VALUE);

        tfRokPlacanja = new TextField();
        tfRokPlacanja.setPromptText("npr. 15 dana");
        tfRokPlacanja.getStyleClass().add("form-field");
        tfRokPlacanja.setMaxWidth(Double.MAX_VALUE);
        if (faktura == null) {
            try {
                int rok = podesavanjaDao.vratiPodesavanja().getDefaultRokPlacanja();
                tfRokPlacanja.setText(rok + " dana od dana izdavanja računa");
            } catch (Exception ignored) {}
        }

        tfMestoIzdavanja = new TextField();
        tfMestoIzdavanja.setPromptText("Grad...");
        tfMestoIzdavanja.getStyleClass().add("form-field");
        tfMestoIzdavanja.setMaxWidth(Double.MAX_VALUE);
        if (faktura == null) {
            try {
                String adresa = podesavanjaDao.vratiPodesavanja().getAdresa();
                if (adresa != null && !adresa.isBlank()) {
                    String grad = adresa.contains(",")
                            ? adresa.substring(adresa.lastIndexOf(',') + 1).trim()
                            : adresa.trim();
                    tfMestoIzdavanja.setText(grad);
                }
            } catch (Exception ignored) {}
        }

        tfMestoIsporuke = new TextField();
        tfMestoIsporuke.setPromptText("Mesto isporuke...");
        tfMestoIsporuke.getStyleClass().add("form-field");
        tfMestoIsporuke.setMaxWidth(Double.MAX_VALUE);

        tfBrojRacuna = new TextField();
        tfBrojRacuna.setPromptText("Broj fiskalnog računa...");
        tfBrojRacuna.getStyleClass().add("form-field");
        tfBrojRacuna.setMaxWidth(Double.MAX_VALUE);

        tfPfrBroj = new TextField();
        tfPfrBroj.setPromptText("PFR broj...");
        tfPfrBroj.getStyleClass().add("form-field");
        tfPfrBroj.setMaxWidth(Double.MAX_VALUE);

        cbStatus = new ComboBox<>(FXCollections.observableArrayList("Kreirana", "Poslata", "Plaćena", "Stornirana"));
        cbStatus.setValue("Kreirana");
        cbStatus.getStyleClass().add("search-field");
        cbStatus.setMaxWidth(Double.MAX_VALUE);

        GridPane gDatumi = new GridPane();
        gDatumi.setHgap(12); gDatumi.setVgap(10);
        gDatumi.getColumnConstraints().addAll(kolona(33), kolona(33), kolona(34));
        gDatumi.add(kreirajRedNode("Datum kreiranja", dpDatumKreiranja), 0, 0);
        gDatumi.add(kreirajRedNode("Datum plaćanja (planirano)", dpDatumPlacanja), 1, 0);
        gDatumi.add(kreirajRedNode("Status", cbStatus), 2, 0);
        gDatumi.add(kreirajRedCB("Način plaćanja", cbNacinPlacanja), 0, 1);
        gDatumi.add(kreirajRedNode("Rok plaćanja", tfRokPlacanja), 1, 1);
        gDatumi.add(kreirajRedNode("Mesto izdavanja", tfMestoIzdavanja), 2, 1);
        gDatumi.add(kreirajRedNode("Mesto isporuke", tfMestoIsporuke), 0, 2, 3, 1);
        gDatumi.add(kreirajRedNode("Broj računa *", tfBrojRacuna), 0, 3);
        gDatumi.add(kreirajRedNode("PFR broj *", tfPfrBroj), 1, 3);
        karticaDatumi.getChildren().add(gDatumi);

        // Stavke
        VBox karticaStavke = kreirajKartica("Stavke");
        karticaStavke.getChildren().add(kreirajStavkePanel());

        // Napomena i popust
        VBox karticaNap = kreirajKartica("Napomena i globalni popust");
        taNapomena = new TextArea();
        taNapomena.setPromptText("Napomena za klijenta...");
        taNapomena.getStyleClass().add("form-textarea");
        taNapomena.setPrefRowCount(3);
        taNapomena.setWrapText(true);

        tfPopust = new TextField("0");
        tfPopust.getStyleClass().add("form-field");
        tfPopust.setPrefWidth(80);
        tfPopust.textProperty().addListener((obs, old, novo) -> osvezi());

        GridPane gNap = new GridPane();
        gNap.setHgap(12); gNap.setVgap(10);
        gNap.getColumnConstraints().addAll(kolona(75), kolona(25));
        gNap.add(kreirajRedTA("Napomena", taNapomena), 0, 0);
        gNap.add(kreirajRedNode("Globalni popust (%)", tfPopust), 1, 0);
        karticaNap.getChildren().add(gNap);

        // Rekapitulacija
        VBox karticaRekap = kreirajKartica("Rekapitulacija");
        karticaRekap.getChildren().add(kreirajRekapPanel());

        // Dugmad
        Button btnSacuvaj = new Button("Sačuvaj prilog uz račun");
        btnSacuvaj.getStyleClass().add("btn-primary");
        btnSacuvaj.setOnAction(e -> sacuvaj());
        Button btnOdustani = new Button("Odustani");
        btnOdustani.getStyleClass().add("btn-secondary");
        btnOdustani.setOnAction(e -> stage.close());

        HBox dugmad = new HBox(8, btnOdustani, btnSacuvaj);

        if (faktura != null && !"Stornirana".equals(faktura.getStatus())) {
            Button btnStorno = new Button("Storniraj");
            btnStorno.getStyleClass().add("btn-danger");
            btnStorno.setOnAction(e -> storniraj());
            Region sp = new Region(); HBox.setHgrow(sp, Priority.ALWAYS);
            dugmad.getChildren().addAll(0, List.of(btnStorno, sp));
        }
        dugmad.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(header, karticaKV, karticaDatumi, karticaStavke, karticaNap, karticaRekap, dugmad);

        if (faktura != null) popuniIzFakture();
        else if (izPredracuna != null) popuniIzPredracuna();
        else if (izNaloga != null) popuniIzNaloga();

        return root;
    }

    private VBox kreirajStavkePanel() {
        tabelaStavki = new TableView<>(stavke);
        tabelaStavki.getStyleClass().add("table-view");
        tabelaStavki.setPrefHeight(200);
        tabelaStavki.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabelaStavki.setPlaceholder(new Label("Nema stavki"));

        TableColumn<FakturaStavka, String> colTip = new TableColumn<>("Tip");
        colTip.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTip()));
        colTip.setPrefWidth(70);

        TableColumn<FakturaStavka, String> colNaziv = new TableColumn<>("Naziv");
        colNaziv.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNaziv()));

        TableColumn<FakturaStavka, String> colKol = new TableColumn<>("Kol.");
        colKol.setCellValueFactory(c -> new SimpleStringProperty(fmt(c.getValue().getKolicina())));
        colKol.setPrefWidth(55);

        TableColumn<FakturaStavka, String> colJm = new TableColumn<>("J.M.");
        colJm.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getJedinicaMere()));
        colJm.setPrefWidth(50);

        TableColumn<FakturaStavka, String> colCena = new TableColumn<>("Cena");
        colCena.setCellValueFactory(c -> new SimpleStringProperty(fmtCena(c.getValue().getCenaBezPdv())));
        colCena.setPrefWidth(90);

        if (pdvObveznik) {
            TableColumn<FakturaStavka, String> colPdv = new TableColumn<>("PDV%");
            colPdv.setCellValueFactory(c -> new SimpleStringProperty(fmt(c.getValue().getPdvStopa()) + "%"));
            colPdv.setPrefWidth(60);
            tabelaStavki.getColumns().add(colPdv);
        }

        TableColumn<FakturaStavka, String> colPopust = new TableColumn<>("Pop%");
        colPopust.setCellValueFactory(c -> new SimpleStringProperty(fmt(c.getValue().getPopustProcenat()) + "%"));
        colPopust.setPrefWidth(60);

        TableColumn<FakturaStavka, String> colUkupno = new TableColumn<>("Ukupno");
        colUkupno.setCellValueFactory(c -> new SimpleStringProperty(fmtCena(c.getValue().iznosUkupno())));
        colUkupno.setPrefWidth(90);

        TableColumn<FakturaStavka, Void> colDel = new TableColumn<>("");
        colDel.setPrefWidth(40);
        colDel.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("✕");
            { btn.setStyle("-fx-font-size: 10px; -fx-padding: 2 6 2 6;");
              btn.getStyleClass().add("btn-danger");
              btn.setOnAction(e -> {
                  stavke.remove(getTableView().getItems().get(getIndex()));
                  osvezi();
              });
            }
            @Override protected void updateItem(Void v, boolean empty) {
                super.updateItem(v, empty);
                setGraphic(empty ? null : btn);
            }
        });

        tabelaStavki.getColumns().addAll(colTip, colNaziv, colKol, colJm, colCena, colPopust, colUkupno, colDel);

        try {
            List<Artikal> artikalLista = new ArtikalDao().vratiSve();
            acArtikal = new AutoCompleteField<>(artikalLista, Artikal::getNaziv);
        } catch (Exception e) {
            acArtikal = new AutoCompleteField<>(List.of(), Artikal::getNaziv);
        }
        acArtikal.setFilterFunkcija(a -> a.getNaziv() + " " + (a.getSifra() != null ? String.valueOf(a.getSifra()) : ""));
        acArtikal.setPriorityFunkcija((a, q) -> {
            String s = a.getSifra() != null ? String.valueOf(a.getSifra()) : "";
            if (s.equals(q)) return 0;
            if (s.startsWith(q)) return 1;
            if (s.contains(q)) return 2;
            return 3;
        });
        acArtikal.getTextField().setPromptText("Naziv artikla...");
        acArtikal.getTextField().getStyleClass().add("form-field");
        HBox.setHgrow(acArtikal.getTextField(), Priority.ALWAYS);

        HBox.setHgrow(acArtikal.getTextField(), Priority.ALWAYS);

        tfStavkaKolicina = new TextField("1");
        tfStavkaKolicina.getStyleClass().add("form-field");
        tfStavkaKolicina.setPrefWidth(55);

        cbStavkaJedinica = new ComboBox<>(JEDINICE);
        cbStavkaJedinica.setValue("kom");
        cbStavkaJedinica.getStyleClass().add("search-field");
        cbStavkaJedinica.setPrefWidth(80);

        tfStavkaCena = new TextField("0");
        tfStavkaCena.getStyleClass().add("form-field");
        tfStavkaCena.setPrefWidth(80);

        double defaultPdv = 20.0;
        try { defaultPdv = podesavanjaDao.vratiPodesavanja().getPdvStopa(); } catch (Exception ignored) {}

        tfStavkaPdv = new TextField(fmt(defaultPdv));
        tfStavkaPdv.getStyleClass().add("form-field");
        tfStavkaPdv.setPrefWidth(55);
        tfStavkaPdv.setVisible(pdvObveznik);
        tfStavkaPdv.setManaged(pdvObveznik);

        tfStavkaPopust = new TextField("0");
        tfStavkaPopust.getStyleClass().add("form-field");
        tfStavkaPopust.setPrefWidth(55);

        acArtikal.setOnOdabrano(a -> {
            tfStavkaCena.setText(fmt(a.getProdajnaCena()));
            String jm = a.getJedinicaMere();
            cbStavkaJedinica.setValue(jm != null && JEDINICE.contains(jm) ? jm : "kom");
        });

        Button btnDodajStavku = new Button("+ Dodaj");
        btnDodajStavku.getStyleClass().add("btn-primary");
        btnDodajStavku.setOnAction(e -> dodajStavku());

        HBox unosStavke = new HBox(6);
        unosStavke.setAlignment(Pos.CENTER_LEFT);
        Label lblN = new Label("Artikal:"); lblN.setStyle("-fx-font-size: 11px;");
        Label lblK = new Label("Kol:"); lblK.setStyle("-fx-font-size: 11px;");
        Label lblJm = new Label("JM:"); lblJm.setStyle("-fx-font-size: 11px;");
        Label lblC = new Label("Cena:"); lblC.setStyle("-fx-font-size: 11px;");
        Label lblPop = new Label("Pop%:"); lblPop.setStyle("-fx-font-size: 11px;");

        unosStavke.getChildren().addAll(lblN, acArtikal.getTextField(), lblK, tfStavkaKolicina,
                lblJm, cbStavkaJedinica, lblC, tfStavkaCena);

        if (pdvObveznik) {
            Label lblPdvL = new Label("PDV%:"); lblPdvL.setStyle("-fx-font-size: 11px;");
            unosStavke.getChildren().addAll(lblPdvL, tfStavkaPdv);
        }

        unosStavke.getChildren().addAll(lblPop, tfStavkaPopust, btnDodajStavku);

        return new VBox(8, tabelaStavki, unosStavke);
    }

    private VBox kreirajRekapPanel() {
        lblUkupnoBezPdv = new Label("0,00 RSD");
        lblUkupniPdv = new Label("0,00 RSD");
        lblGlobalniPopust = new Label("0,00 RSD");
        lblZaUplatu = new Label("0,00 RSD");
        lblZaUplatu.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #0057b7;");

        GridPane g = new GridPane();
        g.setHgap(20); g.setVgap(6);
        g.add(new Label("Ukupno bez PDV:"), 0, 0); g.add(lblUkupnoBezPdv, 1, 0);
        if (pdvObveznik) {
            g.add(new Label("PDV:"), 0, 1); g.add(lblUkupniPdv, 1, 1);
            g.add(new Label("Globalni popust:"), 0, 2); g.add(lblGlobalniPopust, 1, 2);
            g.add(new Label("ZA UPLATU:"), 0, 3); g.add(lblZaUplatu, 1, 3);
        } else {
            g.add(new Label("Globalni popust:"), 0, 1); g.add(lblGlobalniPopust, 1, 1);
            g.add(new Label("ZA UPLATU:"), 0, 2); g.add(lblZaUplatu, 1, 2);
        }
        g.getChildren().stream()
                .filter(n -> n instanceof Label && g.getColumnIndex(n) == 0)
                .forEach(n -> ((Label) n).setStyle("-fx-text-fill: #4a6080; -fx-font-size: 12px;"));
        g.getChildren().stream()
                .filter(n -> n instanceof Label && g.getColumnIndex(n) == 1)
                .forEach(n -> ((Label) n).setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0a1628;"));

        VBox box = new VBox(4, g);
        box.setAlignment(Pos.CENTER_RIGHT);
        return box;
    }

    private void dodajStavku() {
        String naziv = acArtikal.getTextField().getText().trim();
        if (naziv.isBlank()) return;
        try {
            double kol = Double.parseDouble(tfStavkaKolicina.getText().replace(',', '.'));
            double cena = Double.parseDouble(tfStavkaCena.getText().replace(',', '.'));
            double pdv = pdvObveznik ? Double.parseDouble(tfStavkaPdv.getText().replace(',', '.')) : 0;
            double pop = Double.parseDouble(tfStavkaPopust.getText().replace(',', '.'));

            FakturaStavka s = new FakturaStavka();
            s.setTip("Artikal");
            s.setNaziv(naziv);
            s.setKolicina(kol);
            s.setJedinicaMere(cbStavkaJedinica.getValue());
            s.setCenaBezPdv(cena);
            s.setPdvStopa(pdv);
            s.setPopustProcenat(pop);
            s.setRedniBroj(stavke.size() + 1);
            stavke.add(s);
            acArtikal.getTextField().clear();
            tfStavkaKolicina.setText("1");
            tfStavkaCena.setText("0");
            tfStavkaPopust.setText("0");
            osvezi();
        } catch (NumberFormatException e) {
            prikaziGresku("Proverite numeričke vrednosti (količina, cena, PDV, popust).");
        }
    }

    private void osvezi() {
        double popust = 0;
        try { popust = Double.parseDouble(tfPopust.getText().replace(',', '.')); } catch (Exception ignored) {}

        double bezPdv = stavke.stream().mapToDouble(FakturaStavka::iznosBezPdv).sum();
        double pdvIznos = stavke.stream().mapToDouble(FakturaStavka::iznosPdv).sum();
        double globalPop = (bezPdv + pdvIznos) * popust / 100.0;
        double zaUplatu = bezPdv + pdvIznos - globalPop;

        lblUkupnoBezPdv.setText(fmtCena(bezPdv));
        lblUkupniPdv.setText(fmtCena(pdvIznos));
        lblGlobalniPopust.setText(fmtCena(globalPop));
        lblZaUplatu.setText(fmtCena(zaUplatu));
    }

    private void popuniIzFakture() {
        try {
            Faktura pun = fakturaDao.vratiPoId(faktura.getId());
            if (pun == null) return;

            Klijent k = klijentDao.vratiPoId(pun.getKlijentId());
            if (k != null) acKlijent.postavi(k);

            if (pun.getVoziloId() != null) {
                List<Vozilo> vozila = voziloDao.vratiPoKlijentu(pun.getKlijentId());
                acVozilo.setElementi(vozila);
                Vozilo v = voziloDao.vratiPoId(pun.getVoziloId());
                if (v != null) acVozilo.postavi(v);
            }

            if (pun.getDatumKreiranja() != null && !pun.getDatumKreiranja().isBlank())
                dpDatumKreiranja.setValue(LocalDate.parse(pun.getDatumKreiranja(), FMT));
            if (pun.getDatumPlacanja() != null && !pun.getDatumPlacanja().isBlank())
                dpDatumPlacanja.setValue(LocalDate.parse(pun.getDatumPlacanja(), FMT));
            if (pun.getNacinPlacanja() != null) cbNacinPlacanja.setValue(pun.getNacinPlacanja());
            if (pun.getRokPlacanja() != null) tfRokPlacanja.setText(pun.getRokPlacanja());
            if (pun.getMestoIzdavanja() != null) tfMestoIzdavanja.setText(pun.getMestoIzdavanja());
            if (pun.getMestoIsporuke() != null) tfMestoIsporuke.setText(pun.getMestoIsporuke());
            if (pun.getStatus() != null) cbStatus.setValue(pun.getStatus());
            if (pun.getNapomena() != null) taNapomena.setText(pun.getNapomena());
            tfPopust.setText(fmt(pun.getPopustProcenat()));
            if (pun.getBrojRacuna() != null) tfBrojRacuna.setText(pun.getBrojRacuna());
            if (pun.getPfrBroj() != null) tfPfrBroj.setText(pun.getPfrBroj());

            stavke.setAll(pun.getStavke());
            osvezi();
        } catch (SQLException e) {
            logger.warning("Greška pri popunjavanju forme iz fakture: " + e.getMessage());
        }
    }

    private void popuniIzPredracuna() {
        try {
            Predracun pun = predracunDao.vratiPoId(izPredracuna.getId());
            if (pun == null) return;

            Klijent k = klijentDao.vratiPoId(pun.getKlijentId());
            if (k != null) acKlijent.postavi(k);

            if (pun.getVoziloId() != null) {
                List<Vozilo> vozila = voziloDao.vratiPoKlijentu(pun.getKlijentId());
                acVozilo.setElementi(vozila);
                Vozilo v = voziloDao.vratiPoId(pun.getVoziloId());
                if (v != null) acVozilo.postavi(v);
            }

            if (pun.getNacinPlacanja() != null) cbNacinPlacanja.setValue(pun.getNacinPlacanja());
            if (pun.getRokPlacanja() != null) tfRokPlacanja.setText(pun.getRokPlacanja());
            if (pun.getMestoIzdavanja() != null) tfMestoIzdavanja.setText(pun.getMestoIzdavanja());
            if (pun.getNapomena() != null) taNapomena.setText(pun.getNapomena());
            tfPopust.setText(fmt(pun.getPopustProcenat()));

            // Pretvori PredracunStavka → FakturaStavka
            for (PredracunStavka ps : pun.getStavke()) {
                FakturaStavka fs = new FakturaStavka();
                fs.setTip(ps.getTip());
                fs.setNaziv(ps.getNaziv());
                fs.setKolicina(ps.getKolicina());
                fs.setJedinicaMere(ps.getJedinicaMere());
                fs.setCenaBezPdv(ps.getCenaBezPdv());
                fs.setPdvStopa(ps.getPdvStopa());
                fs.setPopustProcenat(ps.getPopustProcenat());
                fs.setRedniBroj(ps.getRedniBroj());
                stavke.add(fs);
            }
            osvezi();
        } catch (SQLException e) {
            logger.warning("Greška pri popunjavanju forme iz predračuna: " + e.getMessage());
        }
    }

    private void popuniIzNaloga() {
        try {
            RadniNalog pun = new RadniNalogDao().vratiPoId(izNaloga.getId());
            if (pun == null) return;

            Klijent k = klijentDao.vratiPoId(pun.getKlijentId());
            if (k != null) acKlijent.postavi(k);

            List<Vozilo> vozila = voziloDao.vratiPoKlijentu(pun.getKlijentId());
            acVozilo.setElementi(vozila);
            Vozilo v = voziloDao.vratiPoId(pun.getVoziloId());
            if (v != null) acVozilo.postavi(v);

            if (pun.getNapomena() != null) taNapomena.setText(pun.getNapomena());

            try {
                Predracun vezaniPredracun = predracunDao.vratiPoRadnomNalogu(pun.getId());
                if (vezaniPredracun != null && vezaniPredracun.getPopustProcenat() > 0)
                    tfPopust.setText(fmt(vezaniPredracun.getPopustProcenat()));
            } catch (Exception ignored) {}

            double defaultPdv = pdvObveznik ? 20.0 : 0.0;
            try { defaultPdv = podesavanjaDao.vratiPodesavanja().getPdvStopa(); } catch (Exception ignored) {}

            int rb = 1;
            for (NalogArtikal na : pun.getArtikli()) {
                FakturaStavka fs = new FakturaStavka();
                fs.setTip("Artikal");
                fs.setNaziv(na.getNazivArtikla());
                fs.setKolicina(na.getKolicina());
                fs.setJedinicaMere(na.getJedinicaMere() != null ? na.getJedinicaMere() : "kom");
                double cena = 0;
                try {
                    Artikal a = new ArtikalDao().vratiPoId(na.getArtikalId());
                    if (a != null) cena = a.getProdajnaCena();
                } catch (Exception ignored) {}
                fs.setCenaBezPdv(cena);
                fs.setPdvStopa(pdvObveznik ? defaultPdv : 0);
                fs.setPopustProcenat(0);
                fs.setRedniBroj(rb++);
                stavke.add(fs);
            }
            osvezi();
        } catch (Exception e) {
            logger.warning("Greška pri popunjavanju forme iz naloga: " + e.getMessage());
        }
    }

    private void sacuvaj() {
        Klijent odabraniKlijent = acKlijent.getOdabraniElement();
        if (odabraniKlijent == null) {
            prikaziGresku("Klijent je obavezan.");
            return;
        }
        if (stavke.isEmpty()) {
            prikaziGresku("Dodajte bar jednu stavku.");
            return;
        }
        if (tfBrojRacuna.getText().trim().isBlank()) {
            prikaziGresku("Broj računa je obavezan.");
            return;
        }
        if (tfPfrBroj.getText().trim().isBlank()) {
            prikaziGresku("PFR broj je obavezan.");
            return;
        }

        double popust = 0;
        try { popust = Double.parseDouble(tfPopust.getText().replace(',', '.')); } catch (Exception ignored) {}

        Faktura f = faktura != null ? faktura : new Faktura();
        f.setKlijentId(odabraniKlijent.getId());
        Vozilo v = acVozilo.getOdabraniElement();
        f.setVoziloId(v != null ? v.getId() : null);
        if (izPredracuna != null && faktura == null) f.setPredracunId(izPredracuna.getId());
        if (izNaloga != null && faktura == null) f.setRadniNalogId(izNaloga.getId());
        f.setDatumKreiranja(dpDatumKreiranja.getValue() != null ? dpDatumKreiranja.getValue().format(FMT) : LocalDate.now().format(FMT));
        f.setDatumPlacanja(dpDatumPlacanja.getValue() != null ? dpDatumPlacanja.getValue().format(FMT) : null);
        f.setNacinPlacanja(cbNacinPlacanja.getValue());
        f.setRokPlacanja(tfRokPlacanja.getText().trim());
        f.setMestoIzdavanja(tfMestoIzdavanja.getText().trim());
        f.setMestoIsporuke(tfMestoIsporuke.getText().trim());
        f.setStatus(cbStatus.getValue());
        f.setNapomena(taNapomena.getText());
        f.setPopustProcenat(popust);
        f.setBrojRacuna(tfBrojRacuna.getText().trim());
        f.setPfrBroj(tfPfrBroj.getText().trim());

        try {
            if (faktura == null) {
                f.setBrojFakture(fakturaDao.generisiBroj());
                int id = fakturaDao.dodaj(f);
                for (int i = 0; i < stavke.size(); i++) {
                    stavke.get(i).setRedniBroj(i + 1);
                    fakturaDao.dodajStavku(id, stavke.get(i));
                }
                // Ažuriraj predračun kao fakturisan
                if (izPredracuna != null) {
                    predracunDao.promeniStatus(izPredracuna.getId(), "Fakturisan");
                }
                if (izNaloga != null) {
                    try {
                        Predracun vezani = predracunDao.vratiPoRadnomNalogu(izNaloga.getId());
                        if (vezani != null) predracunDao.promeniStatus(vezani.getId(), "Fakturisan");
                    } catch (Exception ignored) {}
                }
            } else {
                fakturaDao.izmeni(f);
                fakturaDao.obrisiStavke(f.getId());
                for (int i = 0; i < stavke.size(); i++) {
                    stavke.get(i).setRedniBroj(i + 1);
                    fakturaDao.dodajStavku(f.getId(), stavke.get(i));
                }
            }
            if (onSave != null) onSave.run();
            stage.close();
        } catch (SQLException e) {
            prikaziGresku("Greška pri čuvanju: " + e.getMessage());
        }
    }

    private void storniraj() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Storniranje");
        confirm.setHeaderText(null);
        confirm.setContentText("Stornirati prilog uz račun " + faktura.getBrojFakture() + "? Ova akcija se ne može poništiti.");
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    fakturaDao.promeniStatus(faktura.getId(), "Stornirana");
                    if (onSave != null) onSave.run();
                    stage.close();
                } catch (SQLException e) {
                    prikaziGresku("Greška pri storniranju: " + e.getMessage());
                }
            }
        });
    }

    private VBox kreirajKartica(String naslov) {
        VBox k = new VBox(10);
        k.getStyleClass().add("form-card");
        k.setPadding(new Insets(16));
        Label lbl = new Label(naslov);
        lbl.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0a1628;");
        k.getChildren().addAll(lbl, new Separator());
        return k;
    }

    private VBox kreirajRedNode(String label, javafx.scene.Node node) {
        VBox red = new VBox(4);
        Label lbl = new Label(label);
        lbl.getStyleClass().add("form-label");
        red.getChildren().addAll(lbl, node);
        return red;
    }

    private VBox kreirajRedCB(String label, ComboBox<String> node) {
        VBox red = new VBox(4);
        Label lbl = new Label(label);
        lbl.getStyleClass().add("form-label");
        red.getChildren().addAll(lbl, node);
        return red;
    }

    private VBox kreirajRedTA(String label, TextArea ta) {
        VBox red = new VBox(4);
        Label lbl = new Label(label);
        lbl.getStyleClass().add("form-label");
        red.getChildren().addAll(lbl, ta);
        return red;
    }

    private ColumnConstraints kolona(double pct) {
        ColumnConstraints cc = new ColumnConstraints();
        cc.setPercentWidth(pct);
        return cc;
    }

    private String fmt(double d) {
        return d == (long) d ? String.valueOf((long) d) : String.valueOf(d);
    }

    private String fmtCena(double iznos) {
        return String.format("%,.2f RSD", iznos).replace(',', 'X').replace('.', ',').replace('X', '.');
    }

    private void prikaziGresku(String poruka) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Greška"); a.setHeaderText(null); a.setContentText(poruka); a.showAndWait();
    }
}
