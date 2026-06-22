package autoservis.servis.controller;

import autoservis.servis.dao.*;
import autoservis.servis.model.*;
import autoservis.servis.util.AppIkona;
import autoservis.servis.util.AutoCompleteField;
import autoservis.servis.util.EmailService;
import autoservis.servis.util.FinansijskiPdfGenerator;
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

public class PredracunFormaController {

    private static final Logger logger = Logger.getLogger(PredracunFormaController.class.getName());
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final Predracun predracun;
    private final Runnable onSave;
    private Stage stage;

    private PredracunDao predracunDao;
    private KlijentDao klijentDao;
    private VoziloDao voziloDao;
    private PodesavanjaDao podesavanjaDao;

    private Label lblBrojPredracuna;
    private AutoCompleteField<Klijent> acKlijent;
    private AutoCompleteField<Vozilo> acVozilo;
    private DatePicker dpDatumKreiranja;
    private DatePicker dpDatumVazenja;
    private ComboBox<String> cbNacinPlacanja;
    private TextField tfRokPlacanja;
    private TextField tfMestoIzdavanja;
    private TextField tfPopust;
    private TextArea taNapomena;
    private ComboBox<String> cbStatus;

    private ObservableList<PredracunStavka> stavke = FXCollections.observableArrayList();
    private TableView<PredracunStavka> tabelaStavki;

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

    public PredracunFormaController(Predracun predracun, Runnable onSave) {
        this.predracun = predracun;
        this.onSave = onSave;
        this.predracunDao = new PredracunDao();
        this.klijentDao = new KlijentDao();
        this.voziloDao = new VoziloDao();
        this.podesavanjaDao = new PodesavanjaDao();
    }

    public void show() {
        stage = new Stage();
        AppIkona.postavi(stage);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(predracun == null ? "Novi predračun" : "Izmena — " + predracun.getBrojPredracuna());
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

        lblBrojPredracuna = new Label("...");
        lblBrojPredracuna.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: white;");
        try {
            String br = predracun == null ? predracunDao.generisiBroj() : predracun.getBrojPredracuna();
            lblBrojPredracuna.setText(br);
        } catch (SQLException ignored) {}

        header.getChildren().add(lblBrojPredracuna);

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
                try {
                    acVozilo.setElementi(voziloDao.vratiPoKlijentu(k.getId()));
                } catch (SQLException ignored) {}
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

        dpDatumVazenja = new DatePicker();
        dpDatumVazenja.getStyleClass().add("form-field");
        dpDatumVazenja.setMaxWidth(Double.MAX_VALUE);
        try {
            int rok = podesavanjaDao.vratiPodesavanja().getDefaultRokPlacanja();
            dpDatumVazenja.setValue(LocalDate.now().plusDays(rok));
        } catch (Exception ignored) { dpDatumVazenja.setValue(LocalDate.now().plusDays(15)); }

        cbNacinPlacanja = new ComboBox<>(FXCollections.observableArrayList("Gotovina", "Kartica", "Prenos"));
        cbNacinPlacanja.setValue("Prenos");
        cbNacinPlacanja.getStyleClass().add("search-field");
        cbNacinPlacanja.setMaxWidth(Double.MAX_VALUE);

        tfRokPlacanja = new TextField();
        tfRokPlacanja.setPromptText("npr. 15 dana");
        tfRokPlacanja.getStyleClass().add("form-field");
        tfRokPlacanja.setMaxWidth(Double.MAX_VALUE);
        if (predracun == null) {
            try {
                int rok = podesavanjaDao.vratiPodesavanja().getDefaultRokPlacanja();
                tfRokPlacanja.setText(rok + " dana od dana izdavanja računa");
            } catch (Exception ignored) {}
        }

        tfMestoIzdavanja = new TextField();
        tfMestoIzdavanja.setPromptText("Grad...");
        tfMestoIzdavanja.getStyleClass().add("form-field");
        tfMestoIzdavanja.setMaxWidth(Double.MAX_VALUE);
        if (predracun == null) {
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

        cbStatus = new ComboBox<>(FXCollections.observableArrayList("Kreiran", "Poslat", "Prihvaćen", "Odbijen", "Istekao", "Fakturisan"));
        cbStatus.setValue("Kreiran");
        cbStatus.getStyleClass().add("search-field");
        cbStatus.setMaxWidth(Double.MAX_VALUE);

        GridPane gDatumi = new GridPane();
        gDatumi.setHgap(12); gDatumi.setVgap(10);
        gDatumi.getColumnConstraints().addAll(kolona(33), kolona(33), kolona(34));
        gDatumi.add(kreirajRedNode("Datum kreiranja", dpDatumKreiranja), 0, 0);
        gDatumi.add(kreirajRedNode("Datum važenja", dpDatumVazenja), 1, 0);
        gDatumi.add(kreirajRedNode("Status", cbStatus), 2, 0);
        gDatumi.add(kreirajRedTF("Način plaćanja", cbNacinPlacanja), 0, 1);
        gDatumi.add(kreirajRedNode("Rok plaćanja", tfRokPlacanja), 1, 1);
        gDatumi.add(kreirajRedNode("Mesto izdavanja", tfMestoIzdavanja), 2, 1);
        karticaDatumi.getChildren().add(gDatumi);

        // Stavke
        VBox karticaStavke = kreirajKartica("Stavke predračuna");
        karticaStavke.getChildren().add(kreirajStavkePanel());

        // Rekapitulacija
        VBox karticaRekap = kreirajKartica("Rekapitulacija");
        karticaRekap.getChildren().add(kreirajRekapPanel());

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

        // Dugmad
        Button btnSacuvaj = new Button("Sačuvaj predračun");
        btnSacuvaj.getStyleClass().add("btn-primary");
        btnSacuvaj.setOnAction(e -> sacuvaj());
        Button btnOdustani = new Button("Odustani");
        btnOdustani.getStyleClass().add("btn-secondary");
        btnOdustani.setOnAction(e -> stage.close());
        HBox dugmad = new HBox(8, btnOdustani, btnSacuvaj);
        dugmad.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(header, karticaKV, karticaDatumi, karticaStavke, karticaNap, karticaRekap, dugmad);

        if (predracun != null) popuniFormu();

        return root;
    }

    private VBox kreirajStavkePanel() {
        tabelaStavki = new TableView<>(stavke);
        tabelaStavki.getStyleClass().add("table-view");
        tabelaStavki.setPrefHeight(200);
        tabelaStavki.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabelaStavki.setPlaceholder(new Label("Nema stavki"));

        TableColumn<PredracunStavka, String> colTip = new TableColumn<>("Tip");
        colTip.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTip()));
        colTip.setPrefWidth(70);

        TableColumn<PredracunStavka, String> colNaziv = new TableColumn<>("Naziv");
        colNaziv.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNaziv()));

        TableColumn<PredracunStavka, String> colKol = new TableColumn<>("Kol.");
        colKol.setCellValueFactory(c -> new SimpleStringProperty(fmt(c.getValue().getKolicina())));
        colKol.setPrefWidth(55);

        TableColumn<PredracunStavka, String> colJm = new TableColumn<>("J.M.");
        colJm.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getJedinicaMere()));
        colJm.setPrefWidth(50);

        TableColumn<PredracunStavka, String> colCena = new TableColumn<>("Cena");
        colCena.setCellValueFactory(c -> new SimpleStringProperty(fmtCena(c.getValue().getCenaBezPdv())));
        colCena.setPrefWidth(90);

        if (pdvObveznik) {
            TableColumn<PredracunStavka, String> colPdv = new TableColumn<>("PDV%");
            colPdv.setCellValueFactory(c -> new SimpleStringProperty(fmt(c.getValue().getPdvStopa()) + "%"));
            colPdv.setPrefWidth(60);
            tabelaStavki.getColumns().add(colPdv);
        }

        TableColumn<PredracunStavka, String> colPopust = new TableColumn<>("Pop%");
        colPopust.setCellValueFactory(c -> new SimpleStringProperty(fmt(c.getValue().getPopustProcenat()) + "%"));
        colPopust.setPrefWidth(60);

        TableColumn<PredracunStavka, String> colUkupno = new TableColumn<>("Ukupno");
        colUkupno.setCellValueFactory(c -> new SimpleStringProperty(fmtCena(c.getValue().iznosUkupno())));
        colUkupno.setPrefWidth(90);

        TableColumn<PredracunStavka, Void> colDel = new TableColumn<>("");
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

            PredracunStavka s = new PredracunStavka();
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

        double bezPdv = stavke.stream().mapToDouble(PredracunStavka::iznosBezPdv).sum();
        double pdvIznos = stavke.stream().mapToDouble(PredracunStavka::iznosPdv).sum();
        double globalPop = (bezPdv + pdvIznos) * popust / 100.0;
        double zaUplatu = bezPdv + pdvIznos - globalPop;

        lblUkupnoBezPdv.setText(fmtCena(bezPdv));
        lblUkupniPdv.setText(fmtCena(pdvIznos));
        lblGlobalniPopust.setText(fmtCena(globalPop));
        lblZaUplatu.setText(fmtCena(zaUplatu));
    }

    private void popuniFormu() {
        try {
            Predracun pun = predracunDao.vratiPoId(predracun.getId());
            if (pun == null) return;

            // Klijent
            Klijent k = klijentDao.vratiPoId(pun.getKlijentId());
            if (k != null) acKlijent.postavi(k);

            // Vozilo
            if (pun.getVoziloId() != null) {
                List<Vozilo> vozila = voziloDao.vratiPoKlijentu(pun.getKlijentId());
                acVozilo.setElementi(vozila);
                Vozilo v = voziloDao.vratiPoId(pun.getVoziloId());
                if (v != null) acVozilo.postavi(v);
            }

            if (pun.getDatumKreiranja() != null && !pun.getDatumKreiranja().isBlank())
                dpDatumKreiranja.setValue(LocalDate.parse(pun.getDatumKreiranja(), FMT));
            if (pun.getDatumVazenja() != null && !pun.getDatumVazenja().isBlank())
                dpDatumVazenja.setValue(LocalDate.parse(pun.getDatumVazenja(), FMT));
            if (pun.getNacinPlacanja() != null) cbNacinPlacanja.setValue(pun.getNacinPlacanja());
            if (pun.getRokPlacanja() != null) tfRokPlacanja.setText(pun.getRokPlacanja());
            if (pun.getMestoIzdavanja() != null) tfMestoIzdavanja.setText(pun.getMestoIzdavanja());
            if (pun.getStatus() != null) cbStatus.setValue(pun.getStatus());
            if (pun.getNapomena() != null) taNapomena.setText(pun.getNapomena());
            tfPopust.setText(fmt(pun.getPopustProcenat()));

            stavke.setAll(pun.getStavke());
            osvezi();
        } catch (SQLException e) {
            logger.warning("Greška pri popunjavanju forme: " + e.getMessage());
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

        double popust = 0;
        try { popust = Double.parseDouble(tfPopust.getText().replace(',', '.')); } catch (Exception ignored) {}

        Predracun p = predracun != null ? predracun : new Predracun();
        p.setKlijentId(odabraniKlijent.getId());
        Vozilo v = acVozilo.getOdabraniElement();
        p.setVoziloId(v != null ? v.getId() : null);
        p.setDatumKreiranja(dpDatumKreiranja.getValue() != null ? dpDatumKreiranja.getValue().format(FMT) : LocalDate.now().format(FMT));
        p.setDatumVazenja(dpDatumVazenja.getValue() != null ? dpDatumVazenja.getValue().format(FMT) : null);
        p.setNacinPlacanja(cbNacinPlacanja.getValue());
        p.setRokPlacanja(tfRokPlacanja.getText().trim());
        p.setMestoIzdavanja(tfMestoIzdavanja.getText().trim());
        p.setStatus(cbStatus.getValue());
        p.setNapomena(taNapomena.getText());
        p.setPopustProcenat(popust);

        try {
            if (predracun == null) {
                p.setBrojPredracuna(predracunDao.generisiBroj());
                int id = predracunDao.dodaj(p);
                for (int i = 0; i < stavke.size(); i++) {
                    stavke.get(i).setRedniBroj(i + 1);
                    predracunDao.dodajStavku(id, stavke.get(i));
                }
            } else {
                predracunDao.izmeni(p);
                predracunDao.obrisiStavke(p.getId());
                for (int i = 0; i < stavke.size(); i++) {
                    stavke.get(i).setRedniBroj(i + 1);
                    predracunDao.dodajStavku(p.getId(), stavke.get(i));
                }
            }
            if (onSave != null) onSave.run();
            stage.close();
        } catch (SQLException e) {
            prikaziGresku("Greška pri čuvanju: " + e.getMessage());
        }
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

    private VBox kreirajRedTF(String label, ComboBox<String> node) {
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
