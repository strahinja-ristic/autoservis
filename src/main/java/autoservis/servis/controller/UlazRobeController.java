package autoservis.servis.controller;

import autoservis.servis.dao.ArtikalDao;
import autoservis.servis.dao.DobavljacDao;
import autoservis.servis.dao.UlazDokumentDao;
import autoservis.servis.model.Artikal;
import autoservis.servis.model.Dobavljac;
import autoservis.servis.model.UlazDokument;
import autoservis.servis.model.UlazMagacin;
import autoservis.servis.util.AutoCompleteField;
import autoservis.servis.util.UlazRobePdfPreviewController;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class UlazRobeController {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private static class StavkaUnos {
        Artikal artikal;
        double kolicina;
        StavkaUnos(Artikal a, double k) { this.artikal = a; this.kolicina = k; }
    }

    private BorderPane view;
    private ArtikalDao artikalDao;
    private UlazDokumentDao ulazDokumentDao;
    private DobavljacDao dobavljacDao;

    private Label lblBroj;
    private DatePicker dpDatum;
    private ComboBox<Dobavljac> cbDobavljac;
    private TextField tfBrojOtpremnice;
    private TextField tfNapomenaDok;
    private AutoCompleteField<Artikal> acArtikal;
    private TextField tfKolicina;

    private final ObservableList<StavkaUnos> stavkeUnos = FXCollections.observableArrayList();
    private final ObservableList<UlazDokument> dokumenti = FXCollections.observableArrayList();

    public UlazRobeController() {
        artikalDao = new ArtikalDao();
        ulazDokumentDao = new UlazDokumentDao();
        dobavljacDao = new DobavljacDao();
        view = new BorderPane();
        ucitajArtikle();
        kreirajUI();
        generirajBroj();
        ucitajDokumente();
        ucitajDobavljace();
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
        Label bold = new Label("Ulaz robe");
        bold.getStyleClass().add("topbar-title-bold");
        topbar.getChildren().addAll(breadcrumb, bold);

        // === LEFT: form ===
        VBox formaPanel = new VBox(10);
        formaPanel.getStyleClass().add("form-card");
        formaPanel.setPadding(new Insets(16));
        formaPanel.setMinWidth(440);
        formaPanel.setMaxWidth(520);

        Label lblForma = new Label("Novi prijem robe");
        lblForma.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0a1628;");

        lblBroj = new Label();
        lblBroj.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #1a4fa0;");

        dpDatum = new DatePicker(LocalDate.now());
        dpDatum.setMaxWidth(Double.MAX_VALUE);

        cbDobavljac = new ComboBox<>();
        cbDobavljac.setPromptText("Odaberi dobavljača...");
        cbDobavljac.setMaxWidth(Double.MAX_VALUE);

        tfBrojOtpremnice = new TextField();
        tfBrojOtpremnice.setPromptText("Broj otpremnice (opciono)");
        tfBrojOtpremnice.getStyleClass().add("form-field");
        tfBrojOtpremnice.setMaxWidth(Double.MAX_VALUE);

        tfNapomenaDok = new TextField();
        tfNapomenaDok.setPromptText("Napomena dokumenta (opciono)");
        tfNapomenaDok.getStyleClass().add("form-field");
        tfNapomenaDok.setMaxWidth(Double.MAX_VALUE);

        acArtikal.getTextField().setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(acArtikal.getTextField(), Priority.ALWAYS);

        tfKolicina = new TextField();
        tfKolicina.setPromptText("Kol.");
        tfKolicina.getStyleClass().add("form-field");
        tfKolicina.setPrefWidth(80);

        Button btnDodajStavku = new Button("+ Dodaj");
        btnDodajStavku.getStyleClass().add("btn-secondary");
        btnDodajStavku.setOnAction(e -> dodajStavku());

        HBox redArtikal = new HBox(8, acArtikal.getTextField(), tfKolicina, btnDodajStavku);
        redArtikal.setAlignment(Pos.CENTER_LEFT);

        TableView<StavkaUnos> tabelaStavke = new TableView<>(stavkeUnos);
        tabelaStavke.getStyleClass().add("table-view");
        tabelaStavke.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabelaStavke.setPrefHeight(180);
        tabelaStavke.setPlaceholder(new Label("Nema dodanih stavki"));
        VBox.setVgrow(tabelaStavke, Priority.ALWAYS);

        TableColumn<StavkaUnos, String> colNaziv = new TableColumn<>("Artikal");
        colNaziv.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().artikal.getNaziv()));

        TableColumn<StavkaUnos, String> colKol = new TableColumn<>("Količina");
        colKol.setCellValueFactory(c -> new SimpleStringProperty(fmt(c.getValue().kolicina)));
        colKol.setPrefWidth(80);
        colKol.setMaxWidth(90);

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

        tabelaStavke.getColumns().addAll(colNaziv, colKol, colUkloni);

        Button btnPotvrdri = new Button("Potvrdi prijem");
        btnPotvrdri.getStyleClass().add("btn-primary");
        btnPotvrdri.setMaxWidth(Double.MAX_VALUE);
        btnPotvrdri.setOnAction(e -> potvrdiPrijem());

        Label lblStavke = new Label("Stavke");
        lblStavke.getStyleClass().add("form-label");

        formaPanel.getChildren().addAll(
                lblForma,
                new Separator(),
                kreirajRed("Broj dokumenta", lblBroj),
                kreirajRed("Datum prijema", dpDatum),
                kreirajRed("Dobavljač *", cbDobavljac),
                kreirajRed("Broj otpremnice", tfBrojOtpremnice),
                kreirajRed("Napomena dokumenta", tfNapomenaDok),
                new Separator(),
                lblStavke,
                redArtikal,
                tabelaStavke,
                btnPotvrdri
        );

        // === RIGHT: history ===
        Label lblIstorija = new Label("Istorija prijema robe");
        lblIstorija.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0a1628;");

        TableView<UlazDokument> tabelaDokumenti = new TableView<>(dokumenti);
        tabelaDokumenti.getStyleClass().add("table-view");
        tabelaDokumenti.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tabelaDokumenti, Priority.ALWAYS);

        TableColumn<UlazDokument, String> colBroj = new TableColumn<>("Broj");
        colBroj.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getBroj()));
        colBroj.setPrefWidth(100);

        TableColumn<UlazDokument, String> colDatum = new TableColumn<>("Datum");
        colDatum.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDatum()));
        colDatum.setPrefWidth(110);

        TableColumn<UlazDokument, String> colNapDok = new TableColumn<>("Napomena");
        colNapDok.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNapomena()));

        TableColumn<UlazDokument, String> colBrojStavki = new TableColumn<>("Stavki");
        colBrojStavki.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getBrojStavki())));
        colBrojStavki.setPrefWidth(60);
        colBrojStavki.setMaxWidth(70);

        TableColumn<UlazDokument, Void> colPdf = new TableColumn<>("");
        colPdf.setPrefWidth(65);
        colPdf.setMaxWidth(70);
        colPdf.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("PDF");
            {
                btn.getStyleClass().add("btn-secondary");
                btn.setOnAction(e -> new UlazRobePdfPreviewController(
                        getTableView().getItems().get(getIndex())).show());
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        tabelaDokumenti.getColumns().addAll(colBroj, colDatum, colNapDok, colBrojStavki, colPdf);

        VBox desniPanel = new VBox(12, lblIstorija, tabelaDokumenti);
        VBox.setVgrow(desniPanel, Priority.ALWAYS);

        HBox content = new HBox(16, formaPanel, desniPanel);
        content.getStyleClass().add("content-area");
        HBox.setHgrow(desniPanel, Priority.ALWAYS);

        view.setTop(topbar);
        view.setCenter(content);
    }

    private void generirajBroj() {
        try {
            lblBroj.setText(ulazDokumentDao.generisiBroj());
        } catch (SQLException e) {
            lblBroj.setText("UD-?");
        }
    }

    private void ucitajDokumente() {
        try {
            dokumenti.setAll(ulazDokumentDao.vratiSve());
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
        if (tfKolicina.getText().isBlank()) {
            prikaziGresku("Upiši količinu.");
            return;
        }
        double kolicina;
        try {
            kolicina = Double.parseDouble(tfKolicina.getText().trim().replace(",", "."));
            if (kolicina <= 0) {
                prikaziGresku("Količina mora biti veća od nule.");
                return;
            }
        } catch (NumberFormatException e) {
            prikaziGresku("Količina mora biti broj.");
            return;
        }
        stavkeUnos.add(new StavkaUnos(odabran, kolicina));
        acArtikal.getTextField().clear();
        tfKolicina.clear();
    }

    private void potvrdiPrijem() {
        if (stavkeUnos.isEmpty()) {
            prikaziGresku("Dodaj bar jednu stavku pre potvrde.");
            return;
        }
        if (cbDobavljac.getValue() == null) {
            prikaziGresku("Dobavljač je obavezan.");
            return;
        }

        UlazDokument dok = new UlazDokument();
        dok.setBroj(lblBroj.getText());
        dok.setDatum(dpDatum.getValue().format(FORMATTER));
        Dobavljac odabraniDob = cbDobavljac.getValue();
        dok.setDobavljacId(odabraniDob != null ? odabraniDob.getId() : null);
        String otpremnica = tfBrojOtpremnice.getText().trim();
        dok.setBrojOtpremnice(otpremnica.isEmpty() ? null : otpremnica);
        dok.setNapomena(tfNapomenaDok.getText().trim());

        try {
            Connection conn = autoservis.servis.util.DatabaseManager.getConnection();
            conn.setAutoCommit(false);
            try {
                int dokId = ulazDokumentDao.dodaj(dok);
                if (dokId < 0) {
                    conn.rollback();
                    prikaziGresku("Greška pri kreiranju dokumenta.");
                    return;
                }
                for (StavkaUnos s : stavkeUnos) {
                    UlazMagacin ulaz = new UlazMagacin();
                    ulaz.setArtikalId(s.artikal.getId());
                    ulaz.setKolicina(s.kolicina);
                    ulaz.setDatum(dok.getDatum());
                    ulaz.setNapomena(dok.getNapomena());
                    ulaz.setDokumentId(dokId);
                    artikalDao.dodajUlaz(ulaz);
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }

            stavkeUnos.clear();
            dpDatum.setValue(LocalDate.now());
            cbDobavljac.setValue(null);
            tfBrojOtpremnice.clear();
            tfNapomenaDok.clear();
            generirajBroj();
            ucitajDokumente();

            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Uspešno");
            info.setHeaderText(null);
            info.setContentText("Prijem robe je uspešno evidentiran!");
            info.showAndWait();
        } catch (SQLException e) {
            prikaziGresku("Greška pri evidenciji prijema: " + e.getMessage());
        }
    }

    private void ucitajDobavljace() {
        try {
            cbDobavljac.getItems().setAll(dobavljacDao.vratiSve());
        } catch (SQLException e) {
            // leave empty
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
