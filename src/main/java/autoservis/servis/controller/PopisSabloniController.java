package autoservis.servis.controller;

import autoservis.servis.dao.ArtikalDao;
import autoservis.servis.dao.PodesavanjaDao;
import autoservis.servis.dao.PopisSablonDao;
import autoservis.servis.model.Artikal;
import autoservis.servis.model.Podesavanja;
import autoservis.servis.model.PopisSablon;
import autoservis.servis.util.AutoCompleteField;
import autoservis.servis.util.PopisPdfPreviewController;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;
import java.util.List;

public class PopisSabloniController {

    private BorderPane view;
    private PopisSablonDao sablonDao;
    private ArtikalDao artikalDao;

    private PopisSablon odabraniSablon = null;

    private final ObservableList<PopisSablon> sabloni = FXCollections.observableArrayList();
    private final ObservableList<Artikal> artikliUSablonu = FXCollections.observableArrayList();

    private VBox listaSablona;
    private Label lblDesnoNaslov;
    private TableView<Artikal> tabelaArtikala;
    private AutoCompleteField<Artikal> acArtikal;
    private VBox desniPanel;
    private Label lblPrazno;

    public PopisSabloniController() {
        sablonDao = new PopisSablonDao();
        artikalDao = new ArtikalDao();
        view = new BorderPane();
        kreirajUI();
        ucitajSablone();
    }

    public BorderPane getView() { return view; }

    private void kreirajUI() {
        HBox topbar = new HBox(10);
        topbar.getStyleClass().add("topbar");
        topbar.setAlignment(Pos.CENTER_LEFT);
        Label breadcrumb = new Label("Magacin / ");
        breadcrumb.getStyleClass().add("topbar-title");
        Label bold = new Label("Šabloni popisa");
        bold.getStyleClass().add("topbar-title-bold");
        topbar.getChildren().addAll(breadcrumb, bold);

        // ── LEVI PANEL: kreiranje + lista šablona ──────────────────
        VBox leviPanel = new VBox(10);
        leviPanel.getStyleClass().add("form-card");
        leviPanel.setPadding(new Insets(16));
        leviPanel.setMinWidth(260);
        leviPanel.setMaxWidth(300);

        Label lblNovi = new Label("Novi šablon");
        lblNovi.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0a1628;");

        TextField tfNazivSablon = new TextField();
        tfNazivSablon.setPromptText("Naziv šablona...");
        tfNazivSablon.getStyleClass().add("form-field");
        tfNazivSablon.setMaxWidth(Double.MAX_VALUE);

        Button btnKreiraj = new Button("Kreiraj šablon");
        btnKreiraj.getStyleClass().add("btn-primary");
        btnKreiraj.setMaxWidth(Double.MAX_VALUE);
        btnKreiraj.setOnAction(e -> {
            String naziv = tfNazivSablon.getText().trim();
            if (naziv.isEmpty()) return;
            try {
                sablonDao.dodaj(naziv);
                tfNazivSablon.clear();
                ucitajSablone();
            } catch (SQLException ex) {
                prikaziGresku("Greška pri kreiranju: " + ex.getMessage());
            }
        });
        tfNazivSablon.setOnAction(e -> btnKreiraj.fire());

        Label lblLista = new Label("Postojeći šabloni");
        lblLista.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #4a6080;");

        listaSablona = new VBox(4);
        ScrollPane scroll = new ScrollPane(listaSablona);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        leviPanel.getChildren().addAll(lblNovi, tfNazivSablon, btnKreiraj, new Separator(), lblLista, scroll);

        // ── DESNI PANEL: artikli u šablonu ─────────────────────────
        desniPanel = new VBox(10);
        desniPanel.setPadding(new Insets(16));
        desniPanel.getStyleClass().add("form-card");
        VBox.setVgrow(desniPanel, Priority.ALWAYS);

        lblPrazno = new Label("Odaberi šablon sa liste da vidiš artikle");
        lblPrazno.setStyle("-fx-text-fill: #8a9ab5; -fx-font-size: 13px;");
        VBox praznoBox = new VBox(lblPrazno);
        praznoBox.setAlignment(Pos.CENTER);
        VBox.setVgrow(praznoBox, Priority.ALWAYS);

        lblDesnoNaslov = new Label();
        lblDesnoNaslov.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0a1628;");
        lblDesnoNaslov.setVisible(false);

        try {
            List<Artikal> sviArtikli = artikalDao.vratiSve();
            acArtikal = new AutoCompleteField<>(sviArtikli, Artikal::getNaziv);
            acArtikal.setFilterFunkcija(a -> a.getNaziv() + " " + (a.getSifra() != null ? a.getSifra() : ""));
            acArtikal.setPromptText("Pretraži artikal za dodavanje...");
        } catch (SQLException e) {
            acArtikal = new AutoCompleteField<>(List.of(), Artikal::getNaziv);
        }
        acArtikal.getTextField().setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(acArtikal.getTextField(), Priority.ALWAYS);

        Button btnDodaj = new Button("+ Dodaj");
        btnDodaj.getStyleClass().add("btn-secondary");
        btnDodaj.setOnAction(e -> dodajArtikal());
        acArtikal.getTextField().setOnAction(e -> dodajArtikal());

        HBox redDodaj = new HBox(8, acArtikal.getTextField(), btnDodaj);
        redDodaj.setAlignment(Pos.CENTER_LEFT);
        redDodaj.setVisible(false);

        tabelaArtikala = new TableView<>(artikliUSablonu);
        tabelaArtikala.getStyleClass().add("table-view");
        tabelaArtikala.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabelaArtikala.setVisible(false);
        VBox.setVgrow(tabelaArtikala, Priority.ALWAYS);

        TableColumn<Artikal, String> colSifra = new TableColumn<>("Šifra");
        colSifra.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getSifra() != null ? String.valueOf(c.getValue().getSifra()) : ""));
        colSifra.setPrefWidth(80);
        colSifra.setMaxWidth(90);

        TableColumn<Artikal, String> colNaziv = new TableColumn<>("Naziv");
        colNaziv.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNaziv()));

        TableColumn<Artikal, String> colJM = new TableColumn<>("JM");
        colJM.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getJedinicaMere() != null ? c.getValue().getJedinicaMere() : ""));
        colJM.setPrefWidth(60);
        colJM.setMaxWidth(70);

        TableColumn<Artikal, String> colKolicina = new TableColumn<>("Na stanju");
        colKolicina.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getKolicina() != null ? fmtKol(c.getValue().getKolicina()) : "—"));
        colKolicina.setPrefWidth(90);
        colKolicina.setMaxWidth(100);

        TableColumn<Artikal, Void> colUkloni = new TableColumn<>("");
        colUkloni.setPrefWidth(75);
        colUkloni.setMaxWidth(80);
        colUkloni.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Ukloni");
            {
                btn.getStyleClass().add("btn-secondary");
                btn.setOnAction(e -> {
                    Artikal a = getTableView().getItems().get(getIndex());
                    if (odabraniSablon == null) return;
                    try {
                        sablonDao.ukloniArtikal(odabraniSablon.getId(), a.getId());
                        ucitajArtikleUSablonu();
                        ucitajSablone();
                    } catch (SQLException ex) {
                        prikaziGresku("Greška: " + ex.getMessage());
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        tabelaArtikala.getColumns().addAll(colSifra, colNaziv, colJM, colKolicina, colUkloni);

        desniPanel.getChildren().addAll(lblDesnoNaslov, redDodaj, tabelaArtikala, praznoBox);

        // Čuva reference da mogu da ih show/hide
        lblDesnoNaslov.setUserData(redDodaj);
        redDodaj.setUserData(tabelaArtikala);

        // Listener za show/hide
        sabloni.addListener((javafx.collections.ListChangeListener<PopisSablon>) c -> osvjeziDesni());

        HBox content = new HBox(16, leviPanel, desniPanel);
        content.getStyleClass().add("content-area");
        HBox.setHgrow(desniPanel, Priority.ALWAYS);

        view.setTop(topbar);
        view.setCenter(content);
    }

    private void ucitajSablone() {
        try {
            sabloni.setAll(sablonDao.vratiSve());
            osvjeziListuSablona();
        } catch (SQLException e) {
            prikaziGresku("Greška pri učitavanju šablona: " + e.getMessage());
        }
    }

    private void osvjeziListuSablona() {
        listaSablona.getChildren().clear();
        for (PopisSablon s : sabloni) {
            boolean aktivan = odabraniSablon != null && odabraniSablon.getId() == s.getId();

            VBox kartica = new VBox(4);
            kartica.setPadding(new Insets(10));
            kartica.setStyle(aktivan
                    ? "-fx-background-color: #dbeafe; -fx-background-radius: 8; -fx-border-color: #1a4fa0; -fx-border-radius: 8; -fx-border-width: 1; -fx-cursor: hand;"
                    : "-fx-background-color: #f8fafd; -fx-background-radius: 8; -fx-border-color: #d0d9e6; -fx-border-radius: 8; -fx-border-width: 1; -fx-cursor: hand;");

            Label lblIme = new Label(s.getNaziv());
            lblIme.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #0a1628;");

            Label lblBroj = new Label(s.getBrojArtikala() + " artikala");
            lblBroj.setStyle("-fx-font-size: 11px; -fx-text-fill: #8a9ab5;");

            Button btnPopis = new Button("Popis");
            btnPopis.getStyleClass().add("btn-primary");
            btnPopis.setPrefWidth(80);

            Button btnObrisi = new Button("Obriši");
            btnObrisi.getStyleClass().add("btn-danger");
            btnObrisi.setPrefWidth(70);

            HBox dugmad = new HBox(6, btnPopis, btnObrisi);
            dugmad.setAlignment(Pos.CENTER_LEFT);

            kartica.getChildren().addAll(lblIme, lblBroj, dugmad);

            kartica.setOnMouseClicked(e -> odaberiSablon(s));

            btnPopis.setOnAction(e -> {
                e.consume();
                generisiPopis(s);
            });

            btnObrisi.setOnAction(e -> {
                e.consume();
                obrisiSablon(s);
            });

            listaSablona.getChildren().add(kartica);
        }

        if (sabloni.isEmpty()) {
            Label lbl = new Label("Nema kreiranih šablona");
            lbl.setStyle("-fx-text-fill: #8a9ab5; -fx-font-size: 11px;");
            listaSablona.getChildren().add(lbl);
        }
    }

    private void odaberiSablon(PopisSablon s) {
        odabraniSablon = s;
        osvjeziListuSablona();
        ucitajArtikleUSablonu();
        osvjeziDesni();
    }

    private void ucitajArtikleUSablonu() {
        if (odabraniSablon == null) return;
        try {
            artikliUSablonu.setAll(sablonDao.vratiArtikle(odabraniSablon.getId()));
        } catch (SQLException e) {
            prikaziGresku("Greška pri učitavanju artikala: " + e.getMessage());
        }
    }

    private void osvjeziDesni() {
        boolean ima = odabraniSablon != null;
        lblDesnoNaslov.setVisible(ima);
        if (ima) lblDesnoNaslov.setText(odabraniSablon.getNaziv());

        // Pronalazimo red za dodavanje i tabelu
        desniPanel.getChildren().forEach(node -> {
            if (node instanceof HBox) node.setVisible(ima);
            if (node instanceof TableView) node.setVisible(ima);
            if (node instanceof VBox vb && vb.getChildren().stream().anyMatch(n -> n instanceof Label l && l == lblDesnoNaslov)) {
                // skip — to je sam desniPanel
            }
        });

        // Prazno stanje
        desniPanel.getChildren().stream()
                .filter(n -> n instanceof VBox && ((VBox) n).getChildren().contains(lblPrazno))
                .forEach(n -> n.setVisible(!ima));
    }

    private void dodajArtikal() {
        if (odabraniSablon == null) return;
        Artikal odabran = acArtikal.getOdabraniElement();
        if (odabran == null) {
            prikaziGresku("Odaberi artikal iz padajuce liste.");
            return;
        }
        try {
            sablonDao.dodajArtikal(odabraniSablon.getId(), odabran.getId());
            acArtikal.getTextField().clear();
            ucitajArtikleUSablonu();
            ucitajSablone();
        } catch (SQLException e) {
            prikaziGresku("Greška pri dodavanju: " + e.getMessage());
        }
    }

    private void generisiPopis(PopisSablon s) {
        try {
            List<Artikal> artikli = sablonDao.vratiArtikle(s.getId());
            if (artikli.isEmpty()) {
                prikaziGresku("Sablon \"" + s.getNaziv() + "\" nema artikala.");
                return;
            }
            Podesavanja firma = new PodesavanjaDao().vratiPodesavanja();
            new PopisPdfPreviewController(artikli, firma).show();
        } catch (Exception e) {
            prikaziGresku("Greška pri generisanju popisa: " + e.getMessage());
        }
    }

    private void obrisiSablon(PopisSablon s) {
        Alert potvrda = new Alert(Alert.AlertType.CONFIRMATION);
        potvrda.setTitle("Brisanje šablona");
        potvrda.setHeaderText(null);
        potvrda.setContentText("Obrisati sablon \"" + s.getNaziv() + "\"?");
        potvrda.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    sablonDao.obrisi(s.getId());
                    if (odabraniSablon != null && odabraniSablon.getId() == s.getId()) {
                        odabraniSablon = null;
                        artikliUSablonu.clear();
                        osvjeziDesni();
                    }
                    ucitajSablone();
                } catch (SQLException e) {
                    prikaziGresku("Greška pri brisanju: " + e.getMessage());
                }
            }
        });
    }

    private static String fmtKol(double d) {
        return d == Math.floor(d) ? String.valueOf((int) d) : String.format("%.2f", d);
    }

    private void prikaziGresku(String poruka) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greška");
        alert.setContentText(poruka);
        alert.showAndWait();
    }
}
