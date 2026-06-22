package autoservis.servis.controller;

import autoservis.servis.dao.ArtikalDao;
import autoservis.servis.dao.NivelacijaDao;
import autoservis.servis.model.Artikal;
import autoservis.servis.model.Nivelacija;
import autoservis.servis.model.NivelacijaStavka;
import autoservis.servis.util.AutoCompleteField;
import autoservis.servis.util.NivelacijaPdfPreviewController;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class NivelacijaController {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static class StavkaUnos {
        Artikal artikal;
        double novaCena;

        StavkaUnos(Artikal a, double novaCena) {
            this.artikal = a;
            this.novaCena = novaCena;
        }

        boolean isUsluga() { return "Usluga".equals(artikal.getVrsta()); }

        Double getVrednostPoStaroj() {
            if (isUsluga() || artikal.getKolicina() == null) return null;
            return artikal.getKolicina() * artikal.getProdajnaCena();
        }

        Double getVrednostPoNovoj() {
            if (isUsluga() || artikal.getKolicina() == null) return null;
            return artikal.getKolicina() * novaCena;
        }

        Double getRazlika() {
            Double stara = getVrednostPoStaroj();
            Double nova = getVrednostPoNovoj();
            if (stara == null || nova == null) return null;
            return nova - stara;
        }
    }

    private BorderPane view;
    private ArtikalDao artikalDao;
    private NivelacijaDao nivelacijaDao;

    private Label lblBroj;
    private Label lblStaraCenaPrikaz;
    private DatePicker dpDatum;
    private TextField tfNapomena;
    private AutoCompleteField<Artikal> acArtikal;
    private TextField tfNovaCena;

    private final ObservableList<StavkaUnos> stavkeUnos = FXCollections.observableArrayList();
    private final ObservableList<Nivelacija> nivelacije = FXCollections.observableArrayList();

    public NivelacijaController() {
        artikalDao = new ArtikalDao();
        nivelacijaDao = new NivelacijaDao();
        view = new BorderPane();
        ucitajArtikle();
        kreirajUI();
        generirajBroj();
        ucitajNivelacije();
    }

    public BorderPane getView() { return view; }

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
            acArtikal = new AutoCompleteField<>(List.of(), Artikal::getNaziv);
        }
    }

    private void kreirajUI() {
        HBox topbar = new HBox(10);
        topbar.getStyleClass().add("topbar");
        topbar.setAlignment(Pos.CENTER_LEFT);
        Label breadcrumb = new Label("Početna / ");
        breadcrumb.getStyleClass().add("topbar-title");
        Label bold = new Label("Nivelacija cena");
        bold.getStyleClass().add("topbar-title-bold");
        topbar.getChildren().addAll(breadcrumb, bold);

        // === LEFT: forma ===
        VBox formaPanel = new VBox(10);
        formaPanel.getStyleClass().add("form-card");
        formaPanel.setPadding(new Insets(16));
        formaPanel.setMinWidth(560);

        Label lblForma = new Label("Nova nivelacija cena");
        lblForma.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0a1628;");

        lblBroj = new Label();
        lblBroj.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1a4fa0;");

        dpDatum = new DatePicker(LocalDate.now());
        dpDatum.setMaxWidth(Double.MAX_VALUE);

        tfNapomena = new TextField();
        tfNapomena.setPromptText("Napomena (opciono)");
        tfNapomena.getStyleClass().add("form-field");
        tfNapomena.setMaxWidth(Double.MAX_VALUE);

        acArtikal.getTextField().setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(acArtikal.getTextField(), Priority.ALWAYS);

        tfNovaCena = new TextField();
        tfNovaCena.setPromptText("Nova cena");
        tfNovaCena.getStyleClass().add("form-field");
        tfNovaCena.setPrefWidth(110);

        Button btnDodaj = new Button("+ Dodaj");
        btnDodaj.getStyleClass().add("btn-secondary");
        btnDodaj.setOnAction(e -> dodajStavku());

        lblStaraCenaPrikaz = new Label();
        lblStaraCenaPrikaz.setStyle("-fx-text-fill: #4a6080; -fx-font-size: 12px;");

        acArtikal.setOnOdabrano(a -> {
            lblStaraCenaPrikaz.setText("Stara cena: " + String.format("%.2f", a.getProdajnaCena()));
            tfNovaCena.requestFocus();
        });
        acArtikal.getTextField().textProperty().addListener((obs, old, novo) -> {
            if (novo == null || novo.isBlank()) lblStaraCenaPrikaz.setText("");
        });

        HBox redArtikal = new HBox(8, acArtikal.getTextField(), tfNovaCena, btnDodaj, lblStaraCenaPrikaz);
        redArtikal.setAlignment(Pos.CENTER_LEFT);

        TableView<StavkaUnos> tabelaStavke = new TableView<>(stavkeUnos);
        tabelaStavke.getStyleClass().add("table-view");
        tabelaStavke.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabelaStavke.setPrefHeight(200);
        tabelaStavke.setPlaceholder(new Label("Nema dodanih stavki"));
        VBox.setVgrow(tabelaStavke, Priority.ALWAYS);

        TableColumn<StavkaUnos, String> colNaziv = new TableColumn<>("Artikal");
        colNaziv.setCellValueFactory(c -> {
            StavkaUnos s = c.getValue();
            String sifra = s.artikal.getSifra() != null ? " [" + s.artikal.getSifra() + "]" : "";
            return new SimpleStringProperty(s.artikal.getNaziv() + sifra);
        });

        TableColumn<StavkaUnos, String> colKol = new TableColumn<>("Na stanju");
        colKol.setCellValueFactory(c -> {
            StavkaUnos s = c.getValue();
            if (s.isUsluga() || s.artikal.getKolicina() == null) return new SimpleStringProperty("—");
            return new SimpleStringProperty(fmt(s.artikal.getKolicina()));
        });
        colKol.setPrefWidth(80);

        TableColumn<StavkaUnos, String> colStara = new TableColumn<>("Stara cena");
        colStara.setCellValueFactory(c -> new SimpleStringProperty(
                String.format("%.2f", c.getValue().artikal.getProdajnaCena())));
        colStara.setPrefWidth(85);

        TableColumn<StavkaUnos, String> colVrStara = new TableColumn<>("Vred. stara");
        colVrStara.setCellValueFactory(c -> {
            Double v = c.getValue().getVrednostPoStaroj();
            return new SimpleStringProperty(v == null ? "—" : String.format("%.2f", v));
        });
        colVrStara.setPrefWidth(90);

        TableColumn<StavkaUnos, String> colNova = new TableColumn<>("Nova cena");
        colNova.setCellValueFactory(c -> new SimpleStringProperty(
                String.format("%.2f", c.getValue().novaCena)));
        colNova.setPrefWidth(85);

        TableColumn<StavkaUnos, String> colVrNova = new TableColumn<>("Vred. nova");
        colVrNova.setCellValueFactory(c -> {
            Double v = c.getValue().getVrednostPoNovoj();
            return new SimpleStringProperty(v == null ? "—" : String.format("%.2f", v));
        });
        colVrNova.setPrefWidth(90);

        TableColumn<StavkaUnos, String> colRazlika = new TableColumn<>("Razlika");
        colRazlika.setCellValueFactory(c -> {
            Double r = c.getValue().getRazlika();
            return new SimpleStringProperty(r == null ? "—" : String.format("%.2f", r));
        });
        colRazlika.setPrefWidth(80);

        TableColumn<StavkaUnos, Void> colUkloni = new TableColumn<>("");
        colUkloni.setPrefWidth(75);
        colUkloni.setMaxWidth(80);
        colUkloni.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Ukloni");
            {
                btn.getStyleClass().add("btn-secondary");
                btn.setOnAction(e -> stavkeUnos.remove(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        tabelaStavke.getColumns().addAll(colNaziv, colKol, colStara, colVrStara, colNova, colVrNova, colRazlika, colUkloni);

        Button btnPotvrdri = new Button("Potvrdi nivelaciju");
        btnPotvrdri.getStyleClass().add("btn-primary");
        btnPotvrdri.setMaxWidth(Double.MAX_VALUE);
        btnPotvrdri.setOnAction(e -> potvrdiNivelaciju());

        Label lblStavke = new Label("Stavke");
        lblStavke.getStyleClass().add("form-label");

        formaPanel.getChildren().addAll(
                lblForma,
                new Separator(),
                kreirajRed("Broj dokumenta", lblBroj),
                kreirajRed("Datum", dpDatum),
                kreirajRed("Napomena", tfNapomena),
                new Separator(),
                lblStavke,
                redArtikal,
                tabelaStavke,
                btnPotvrdri
        );

        // === RIGHT: istorija ===
        Label lblIstorija = new Label("Istorija nivelacija");
        lblIstorija.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0a1628;");

        TableView<Nivelacija> tabelaNiv = new TableView<>(nivelacije);
        tabelaNiv.getStyleClass().add("table-view");
        tabelaNiv.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tabelaNiv, Priority.ALWAYS);

        TableColumn<Nivelacija, String> colBroj = new TableColumn<>("Broj");
        colBroj.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBroj()));

        TableColumn<Nivelacija, String> colDatum = new TableColumn<>("Datum");
        colDatum.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDatum()));

        TableColumn<Nivelacija, Void> colPdf = new TableColumn<>("");
        colPdf.setPrefWidth(65);
        colPdf.setMaxWidth(70);
        colPdf.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("PDF");
            {
                btn.getStyleClass().add("btn-secondary");
                btn.setOnAction(e -> new NivelacijaPdfPreviewController(
                        getTableView().getItems().get(getIndex())).show());
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        tabelaNiv.getColumns().addAll(colBroj, colDatum, colPdf);

        VBox desniPanel = new VBox(12, lblIstorija, tabelaNiv);
        VBox.setVgrow(desniPanel, Priority.ALWAYS);
        desniPanel.setMinWidth(260);
        desniPanel.setMaxWidth(300);

        HBox content = new HBox(16, formaPanel, desniPanel);
        content.getStyleClass().add("content-area");
        HBox.setHgrow(formaPanel, Priority.ALWAYS);

        view.setTop(topbar);
        view.setCenter(content);
    }

    private void generirajBroj() {
        try {
            lblBroj.setText(nivelacijaDao.generisiBroj());
        } catch (SQLException e) {
            lblBroj.setText("NIV-?");
        }
    }

    private void ucitajNivelacije() {
        try {
            nivelacije.setAll(nivelacijaDao.vratiSve());
        } catch (SQLException e) {
            prikaziGresku("Greška pri učitavanju istorije.");
        }
    }

    private void dodajStavku() {
        if (acArtikal.getTextField().getText().isBlank()) {
            prikaziGresku("Odaberi artikal.");
            return;
        }
        Artikal odabran = acArtikal.getOdabraniElement();
        if (odabran == null) {
            prikaziGresku("Odaberi artikal iz padajuće liste.");
            return;
        }
        // Proveri duplikat
        boolean vecPostoji = stavkeUnos.stream().anyMatch(s -> s.artikal.getId() == odabran.getId());
        if (vecPostoji) {
            prikaziGresku("Artikal \"" + odabran.getNaziv() + "\" je već dodat.");
            return;
        }
        if (tfNovaCena.getText().isBlank()) {
            prikaziGresku("Upiši novu cenu.");
            return;
        }
        double novaCena;
        try {
            novaCena = Double.parseDouble(tfNovaCena.getText().trim().replace(",", "."));
            if (novaCena < 0) {
                prikaziGresku("Cena ne može biti negativna.");
                return;
            }
        } catch (NumberFormatException e) {
            prikaziGresku("Nova cena mora biti broj.");
            return;
        }
        stavkeUnos.add(new StavkaUnos(odabran, novaCena));
        acArtikal.getTextField().clear();
        tfNovaCena.clear();
    }

    private void potvrdiNivelaciju() {
        if (stavkeUnos.isEmpty()) {
            prikaziGresku("Dodaj bar jednu stavku pre potvrde.");
            return;
        }

        Nivelacija niv = new Nivelacija();
        niv.setBroj(lblBroj.getText());
        niv.setDatum(dpDatum.getValue().format(FORMATTER));
        niv.setNapomena(tfNapomena.getText().trim().isEmpty() ? null : tfNapomena.getText().trim());

        List<NivelacijaStavka> stavke = new ArrayList<>();
        for (StavkaUnos s : stavkeUnos) {
            NivelacijaStavka st = new NivelacijaStavka();
            st.setArtikalId(s.artikal.getId());
            st.setNazivArtikla(s.artikal.getNaziv());
            st.setSifraArtikla(s.artikal.getSifra());
            st.setJedinicaMere(s.artikal.getJedinicaMere());
            st.setVrsta(s.artikal.getVrsta());
            st.setKolicinaStanju(s.artikal.getKolicina());
            st.setStaraCena(s.artikal.getProdajnaCena());
            st.setNovaCena(s.novaCena);
            stavke.add(st);
        }
        niv.setStavke(stavke);

        try {
            nivelacijaDao.dodaj(niv);
            stavkeUnos.clear();
            dpDatum.setValue(LocalDate.now());
            tfNapomena.clear();
            generirajBroj();
            ucitajNivelacije();

            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Uspešno");
            info.setHeaderText(null);
            info.setContentText("Nivelacija cena je uspešno evidentirana!");
            info.showAndWait();
        } catch (SQLException e) {
            prikaziGresku("Greška pri evidenciji nivelacije: " + e.getMessage());
        }
    }

    private VBox kreirajRed(String labelTekst, javafx.scene.Node polje) {
        VBox red = new VBox(4);
        Label lbl = new Label(labelTekst);
        lbl.getStyleClass().add("form-label");
        red.getChildren().addAll(lbl, polje);
        return red;
    }

    private static String fmt(double v) {
        return v == (long) v ? String.valueOf((long) v) : String.valueOf(v);
    }

    private void prikaziGresku(String poruka) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greška");
        alert.setContentText(poruka);
        alert.showAndWait();
    }
}
