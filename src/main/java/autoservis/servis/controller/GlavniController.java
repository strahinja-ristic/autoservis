package autoservis.servis.controller;

import autoservis.servis.MainApp;
import autoservis.servis.dao.*;
import autoservis.servis.model.*;
import autoservis.servis.util.AppIkona;
import autoservis.servis.util.FeatureFlagService;
import autoservis.servis.util.LicenseService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class GlavniController {

    private static final Logger logger = Logger.getLogger(GlavniController.class.getName());

    private BorderPane root;
    private BorderPane pageArea;
    private HBox licenseBanner;
    private Label licenseBannerLabel;
    private Stage stage;
    private Button activeButton;

    public GlavniController(Stage stage) {
        this.stage = stage;
        this.root  = new BorderPane();

        // Banner + page area wrapperi
        licenseBannerLabel = new Label();
        licenseBannerLabel.setMaxWidth(Double.MAX_VALUE);
        licenseBannerLabel.setPadding(new Insets(8, 16, 8, 16));
        HBox.setHgrow(licenseBannerLabel, Priority.ALWAYS);

        licenseBanner = new HBox(licenseBannerLabel);
        licenseBanner.setVisible(false);
        licenseBanner.setManaged(false);

        pageArea = new BorderPane();

        VBox centerWrapper = new VBox(licenseBanner, pageArea);
        VBox.setVgrow(pageArea, Priority.ALWAYS);

        this.root.setLeft(kreirajSidebar());
        this.root.setCenter(centerWrapper);
        prikaziPocetak();
    }

    public BorderPane getView() {
        return root;
    }

    public void applyLicenseBanner() {
        LicenseService.PingResult result = MainApp.getLastLicenseResult();
        if (result == null) return;

        if (result.status == LicenseService.LicenseStatus.EXPIRING_SOON) {
            licenseBannerLabel.setText(
                "Licenca ističe za " + result.daysLeft + " dana. Obnovite što pre.");
            licenseBannerLabel.setStyle(
                "-fx-background-color: #FEF3C7; -fx-text-fill: #92400E;" +
                "-fx-font-size: 12px; -fx-font-weight: bold;");
            licenseBanner.setStyle("-fx-background-color: #FEF3C7;");
            licenseBanner.setVisible(true);
            licenseBanner.setManaged(true);

        } else if (result.status == LicenseService.LicenseStatus.GRACE_PERIOD) {
            licenseBannerLabel.setText(
                "Licenca istekla! Imate još " + result.daysLeft + " dana grace perioda.");
            licenseBannerLabel.setStyle(
                "-fx-background-color: #FEE2E2; -fx-text-fill: #991B1B;" +
                "-fx-font-size: 12px; -fx-font-weight: bold;");
            licenseBanner.setStyle("-fx-background-color: #FEE2E2;");
            licenseBanner.setVisible(true);
            licenseBanner.setManaged(true);
        }
    }

    // ----------------------------------------------------------------
    // SIDEBAR
    // ----------------------------------------------------------------
    private VBox kreirajSidebar() {
        VBox sb = new VBox();
        sb.getStyleClass().add("sidebar");

        // Header
        VBox header = new VBox(4);
        header.getStyleClass().add("sidebar-header");
        header.setAlignment(Pos.CENTER);

        String nazivFirme = "Moj Servis";
        String logoPutanja = null;
        try {
            Podesavanja p = new PodesavanjaDao().vratiPodesavanja();
            if (p.getNazivFirme() != null && !p.getNazivFirme().isBlank())
                nazivFirme = p.getNazivFirme();
            logoPutanja = p.getLogoPutanja();
        } catch (Exception ignored) {}

        if (logoPutanja != null && !logoPutanja.isBlank()) {
            try {
                javafx.scene.image.Image img = new javafx.scene.image.Image(
                        new java.io.FileInputStream(logoPutanja));
                javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(img);
                iv.setFitWidth(130);
                iv.setFitHeight(64);
                iv.setPreserveRatio(true);
                iv.setSmooth(true);
                HBox logoBox = new HBox(iv);
                logoBox.setAlignment(Pos.CENTER);
                logoBox.setPadding(new Insets(0, 0, 2, 0));
                header.getChildren().add(logoBox);
            } catch (Exception ignored) {}
        }

        Label logo = new Label(nazivFirme);
        logo.getStyleClass().add("sidebar-logo");
        logo.setWrapText(true);
        logo.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
        Label sub = new Label("Upravljanje servisom");
        sub.getStyleClass().add("sidebar-logo-sub");
        header.getChildren().addAll(logo, sub);

        // Sekcije i dugmad
        Label sekcija1 = new Label("GLAVNI MENI");
        sekcija1.getStyleClass().add("sidebar-section-label");

        Button btnPocetna    = kreirajNavBtn("  Početna");
        Button btnKlijenti   = kreirajNavBtn("  Klijenti");
        Button btnVozila     = kreirajNavBtn("  Vozila");
        Button btnNalozi     = kreirajNavBtn("  Radni nalozi");

        Label sekcija2 = new Label("MAGACIN");
        sekcija2.getStyleClass().add("sidebar-section-label");

        Button btnSkladiste  = kreirajNavBtn("  Magacin");
        Button btnUlaz       = kreirajNavBtn("  Ulaz robe");
        Button btnNivelacija = kreirajNavBtn("  Nivelacija cena");
        Button btnSabloni    = kreirajNavBtn("  Šabloni usluga");
        Button btnDobavljaci = kreirajNavBtn("  Dobavljači");

        boolean statistikeEnabled = FeatureFlagService.getInstance().isEnabled("feature_statistike");

        Label sekcija3 = new Label("ANALITIKA");
        sekcija3.getStyleClass().add("sidebar-section-label");

        Button btnStatistike = kreirajNavBtn("  Statistike");

        Label sekcija4 = new Label("FINANSIJE");
        sekcija4.getStyleClass().add("sidebar-section-label");

        Button btnPredracuni = kreirajNavBtn("  Predračuni");
        Button btnFakture    = kreirajNavBtn("  Prilog uz račun");

        Label sekcija5 = new Label("SISTEM");
        sekcija5.getStyleClass().add("sidebar-section-label");

        Button btnPodesavanja = kreirajNavBtn("  Podešavanja");

        // Akcije
        btnPocetna.setOnAction(e -> { setActiveButton(btnPocetna); prikaziPocetak(); });
        btnKlijenti.setOnAction(e -> { setActiveButton(btnKlijenti); pageArea.setCenter(new KlijentiController().getView()); });
        btnVozila.setOnAction(e -> { setActiveButton(btnVozila); pageArea.setCenter(new VozilaController().getView()); });
        btnNalozi.setOnAction(e -> { setActiveButton(btnNalozi); pageArea.setCenter(new RadniNaloziController().getView()); });
        btnSkladiste.setOnAction(e -> { setActiveButton(btnSkladiste); pageArea.setCenter(new MagacinController().getView()); });
        btnDobavljaci.setOnAction(e -> { setActiveButton(btnDobavljaci); pageArea.setCenter(new DobavljaciController().getView()); });
        btnUlaz.setOnAction(e -> { setActiveButton(btnUlaz); pageArea.setCenter(new UlazRobeController().getView()); });
        btnNivelacija.setOnAction(e -> { setActiveButton(btnNivelacija); pageArea.setCenter(new NivelacijaController().getView()); });
        btnPredracuni.setOnAction(e -> { setActiveButton(btnPredracuni); pageArea.setCenter(new PredracunListaController().getView()); });
        btnFakture.setOnAction(e -> { setActiveButton(btnFakture); pageArea.setCenter(new FakturaListaController().getView()); });
        btnPodesavanja.setOnAction(e -> { setActiveButton(btnPodesavanja); pageArea.setCenter(new PodesavanjaController().getView()); });
        btnSabloni.setOnAction(e -> { setActiveButton(btnSabloni); pageArea.setCenter(new SabloniUslugeController().getView()); });
        btnStatistike.setOnAction(e -> { setActiveButton(btnStatistike); pageArea.setCenter(new StatistikeController().getView()); });

        setActiveButton(btnPocetna);

        sb.getChildren().addAll(
                header,
                sekcija1,
                btnPocetna, btnKlijenti, btnVozila, btnDobavljaci, btnNalozi,
                sekcija2,
                btnSkladiste, btnSabloni, btnNivelacija, btnUlaz,
                sekcija4,
                btnPredracuni, btnFakture,
                sekcija5,
                btnPodesavanja
        );

        if (statistikeEnabled) {
            sb.getChildren().add(sb.getChildren().indexOf(sekcija4), sekcija3);
            sb.getChildren().add(sb.getChildren().indexOf(sekcija4), btnStatistike);
        }

        return sb;
    }

    private Button kreirajNavBtn(String tekst) {
        Button btn = new Button(tekst);
        btn.getStyleClass().add("nav-button");
        btn.setMaxWidth(Double.MAX_VALUE);
        return btn;
    }

    private void setActiveButton(Button btn) {
        if (activeButton != null) {
            activeButton.getStyleClass().remove("nav-button-active");
            if (!activeButton.getStyleClass().contains("nav-button")) {
                activeButton.getStyleClass().add("nav-button");
            }
        }
        activeButton = btn;
        btn.getStyleClass().remove("nav-button");
        btn.getStyleClass().add("nav-button-active");
    }

    // ----------------------------------------------------------------
    // EKRANI
    // ----------------------------------------------------------------

    private void prikaziPocetak() {
        BorderPane page = new BorderPane();

        // Topbar
        HBox topbar = new HBox();
        topbar.getStyleClass().add("topbar");
        Label breadcrumb = new Label("Početna / ");
        breadcrumb.getStyleClass().add("topbar-title");
        Label bold = new Label("Pregled");
        bold.getStyleClass().add("topbar-title-bold");
        topbar.getChildren().addAll(breadcrumb, bold);

        // Content
        VBox content = new VBox(20);
        content.getStyleClass().add("content-area");

        // Stat kartice
        HBox statovi = new HBox(14);
        int aktivnih = 0, niskaZaliha = 0, aktivnihPredracuna = 0, neplacenihFaktura = 0;
        try {
            RadniNalogDao nalogDao = new RadniNalogDao();
            ArtikalDao artikalDao = new ArtikalDao();
            List<RadniNalog> sviNalozi = nalogDao.vratiSve();
            for (RadniNalog rn : sviNalozi) {
                if ("Primljeno".equals(rn.getStatus()) || "U radu".equals(rn.getStatus())) aktivnih++;
            }
            niskaZaliha = artikalDao.vratiIspodMinimuma().size();
        } catch (Exception e) {
            logger.warning("Greška pri učitavanju dashboard podataka: " + e.getMessage());
        }
        try {
            PredracunDao predracunDao = new PredracunDao();
            predracunDao.azurirajIstekle();
            List<Predracun> sviPredracuni = predracunDao.vratiSve();
            for (Predracun p : sviPredracuni) {
                String s = p.getStatus();
                if ("Kreiran".equals(s) || "Poslat".equals(s) || "Prihvaćen".equals(s)) aktivnihPredracuna++;
            }
        } catch (Exception e) {
            logger.warning("Greška pri učitavanju predračuna: " + e.getMessage());
        }
        try {
            FakturaDao fakturaDao = new FakturaDao();
            neplacenihFaktura = fakturaDao.broji("Kreirana") + fakturaDao.broji("Poslata");
        } catch (Exception e) {
            logger.warning("Greška pri učitavanju faktura: " + e.getMessage());
        }

        statovi.getChildren().addAll(
                kreirajStatKarticu("Aktivni nalozi", String.valueOf(aktivnih), "naloga u toku", "stat-card-blue"),
                kreirajStatKarticu("Niska zaliha", String.valueOf(niskaZaliha), "artikala ispod minimuma", "stat-card-green"),
                kreirajStatKarticu("Aktivni predračuni", String.valueOf(aktivnihPredracuna), "čekaju odgovor", "stat-card-blue"),
                kreirajStatKarticu("Neplaćeni prilog uz račun", String.valueOf(neplacenihFaktura), "prilog uz račun čeka uplatu", "stat-card-green")
        );

        // Aktivni nalozi kao kartice
        Label lblAktivni = new Label("Nalozi koji nisu završeni");
        lblAktivni.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0a1628;");

        FlowPane karticPanel = new FlowPane();
        karticPanel.setHgap(12);
        karticPanel.setVgap(12);

        try {
            RadniNalogDao nalogDao = new RadniNalogDao();
            KlijentDao klijentDao2 = new KlijentDao();
            VoziloDao voziloDao2 = new VoziloDao();

            java.util.Map<Integer, Klijent> klijentiMap = new java.util.HashMap<>();
            for (Klijent kl : klijentDao2.vratiSve()) klijentiMap.put(kl.getId(), kl);
            java.util.Map<Integer, Vozilo> vozilaMap = new java.util.HashMap<>();
            for (Vozilo vl : voziloDao2.pretrazi("")) vozilaMap.put(vl.getId(), vl);

            List<RadniNalog> svi = nalogDao.vratiSve();
            for (RadniNalog rn : svi) {
                if ("Završeno".equals(rn.getStatus())) continue;

                Klijent k = klijentiMap.get(rn.getKlijentId());
                Vozilo v = vozilaMap.get(rn.getVoziloId());

                VBox kartica = new VBox(6);
                kartica.setPrefWidth(230);
                kartica.setPadding(new Insets(12));
                kartica.setStyle(
                        "-fx-background-color: #ffffff;" +
                                "-fx-border-color: " + ("U radu".equals(rn.getStatus()) ? "#f59e0b" : "#0057b7") + " #d0d9e6 #d0d9e6 #d0d9e6;" +
                                "-fx-border-width: 0 0 0 4;" +
                                "-fx-border-radius: 0 8 8 0;" +
                                "-fx-background-radius: 8;" +
                                "-fx-cursor: hand;"
                );

                Label lblBroj = new Label(rn.getBrojNaloga());
                lblBroj.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: " +
                        ("U radu".equals(rn.getStatus()) ? "#92400e;" : "#0057b7;"));

                Label lblKlijent = new Label(k != null ? k.getPunoIme() : "—");
                lblKlijent.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0a1628;");

                Label lblVozilo = new Label(v != null ? v.toString() : "—");
                lblVozilo.setStyle("-fx-font-size: 12px; -fx-text-fill: #8a9ab5;");

                HBox footer = new HBox();
                footer.setAlignment(Pos.CENTER_LEFT);
                footer.setStyle("-fx-border-color: #d0d9e6; -fx-border-width: 1 0 0 0; -fx-padding: 6 0 0 0;");

                Label lblDatum = new Label(rn.getDatumPrijema() != null ? rn.getDatumPrijema() : "—");
                lblDatum.setStyle("-fx-font-size: 11px; -fx-text-fill: #adb5bd;");

                Region footerSpacer = new Region();
                HBox.setHgrow(footerSpacer, Priority.ALWAYS);

                Label badge = new Label(rn.getStatus());
                badge.setStyle(
                        "-fx-font-size: 10px; -fx-font-weight: bold; -fx-padding: 3 8 3 8; -fx-background-radius: 20;" +
                                ("U radu".equals(rn.getStatus())
                                        ? "-fx-background-color: #fef3c7; -fx-text-fill: #92400e;"
                                        : "-fx-background-color: #dbeafe; -fx-text-fill: #1e40af;")
                );

                footer.getChildren().addAll(lblDatum, footerSpacer, badge);
                kartica.getChildren().addAll(lblBroj, lblKlijent, lblVozilo, footer);

                final RadniNalog finalRn = rn;
                final Klijent finalK = k;
                final Vozilo finalV = v;
                kartica.setOnMouseClicked(e -> prikaziDetaljeNaloga(finalRn, finalK, finalV, nalogDao));

                kartica.setOnMouseEntered(e -> kartica.setStyle(
                        "-fx-background-color: #f8fafd;" +
                                "-fx-border-color: " + ("U radu".equals(finalRn.getStatus()) ? "#f59e0b" : "#0057b7") + " #d0d9e6 #d0d9e6 #d0d9e6;" +
                                "-fx-border-width: 0 0 0 4;" +
                                "-fx-border-radius: 0 8 8 0;" +
                                "-fx-background-radius: 8;" +
                                "-fx-cursor: hand;"
                ));
                kartica.setOnMouseExited(e -> kartica.setStyle(
                        "-fx-background-color: #ffffff;" +
                                "-fx-border-color: " + ("U radu".equals(finalRn.getStatus()) ? "#f59e0b" : "#0057b7") + " #d0d9e6 #d0d9e6 #d0d9e6;" +
                                "-fx-border-width: 0 0 0 4;" +
                                "-fx-border-radius: 0 8 8 0;" +
                                "-fx-background-radius: 8;" +
                                "-fx-cursor: hand;"
                ));

                karticPanel.getChildren().add(kartica);
            }

            if (karticPanel.getChildren().isEmpty()) {
                Label prazno = new Label("Nema aktivnih naloga");
                prazno.setStyle("-fx-text-fill: #8a9ab5; -fx-font-size: 12px;");
                karticPanel.getChildren().add(prazno);
            }

        } catch (Exception e) {
            logger.warning("Greška pri učitavanju aktivnih naloga: " + e.getMessage());
        }

        VBox aktivniBox = new VBox(10, lblAktivni, karticPanel);

        // Podsetnici - sledeci servis
        VBox podsetniciBox = new VBox(10);
        Label lblPodsetnici = new Label("Vozila kojima se bliži servis");
        lblPodsetnici.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0a1628;");

        TableView<RadniNalog> tabelaPodsetnici = new TableView<>();
        tabelaPodsetnici.getStyleClass().add("table-view");
        tabelaPodsetnici.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabelaPodsetnici.setPrefHeight(200);

        TableColumn<RadniNalog, String> colBroj = new TableColumn<>("Nalog");
        colBroj.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getBrojNaloga()));
        colBroj.setPrefWidth(90);

        java.util.Map<Integer, String> klijentImeCache;
        java.util.Map<Integer, String> voziloStrCache;
        try {
            klijentImeCache = new KlijentDao().vratiSve().stream()
                    .collect(java.util.stream.Collectors.toMap(Klijent::getId, Klijent::getPunoIme));
            voziloStrCache = new VoziloDao().pretrazi("").stream()
                    .collect(java.util.stream.Collectors.toMap(Vozilo::getId, Vozilo::toString));
        } catch (Exception e) {
            klijentImeCache = new java.util.HashMap<>();
            voziloStrCache = new java.util.HashMap<>();
        }
        final java.util.Map<Integer, String> klijentImeFinal = klijentImeCache;
        final java.util.Map<Integer, String> voziloStrFinal = voziloStrCache;

        TableColumn<RadniNalog, String> colKlijent = new TableColumn<>("Klijent");
        colKlijent.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                klijentImeFinal.getOrDefault(c.getValue().getKlijentId(), "")));

        TableColumn<RadniNalog, String> colVozilo = new TableColumn<>("Vozilo");
        colVozilo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                voziloStrFinal.getOrDefault(c.getValue().getVoziloId(), "")));

        TableColumn<RadniNalog, String> colSledeciKm = new TableColumn<>("Sledeći servis km");
        colSledeciKm.setCellValueFactory(c -> {
            Integer km = c.getValue().getSledeciServisKm();
            return new javafx.beans.property.SimpleStringProperty(km != null && km > 0 ? km + " km" : "—");
        });
        colSledeciKm.setPrefWidth(130);

        TableColumn<RadniNalog, String> colSledeciDatum = new TableColumn<>("Sledeći servis datum");
        colSledeciDatum.setCellValueFactory(c -> {
            String datum = c.getValue().getSledeciServisDatum();
            return new javafx.beans.property.SimpleStringProperty(datum != null && !datum.isBlank() ? datum : "—");
        });
        colSledeciDatum.setPrefWidth(150);

        tabelaPodsetnici.getColumns().addAll(colBroj, colKlijent, colVozilo, colSledeciKm, colSledeciDatum);

        try {
            RadniNalogDao nalogDao = new RadniNalogDao();
            List<RadniNalog> svi = nalogDao.vratiSve();
            List<RadniNalog> saPodsetnikom = new ArrayList<>();

            java.time.LocalDate danas = java.time.LocalDate.now();
            java.time.LocalDate granica = danas.plusDays(7);
            java.time.format.DateTimeFormatter fmt = java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy");

            for (RadniNalog rn : svi) {
                if (rn.getSledeciServisDatum() != null && !rn.getSledeciServisDatum().isBlank()) {
                    try {
                        java.time.LocalDate datumServisa = java.time.LocalDate.parse(rn.getSledeciServisDatum(), fmt);
                        if (!datumServisa.isAfter(granica)) {
                            saPodsetnikom.add(rn);
                        }
                    } catch (Exception ignored) {}
                }
            }

            tabelaPodsetnici.setItems(javafx.collections.FXCollections.observableArrayList(saPodsetnikom));
            if (saPodsetnikom.isEmpty()) {
                tabelaPodsetnici.setPlaceholder(new Label("Nema vozila sa servisom u narednih 7 dana"));
            }
        } catch (Exception e) {
            logger.warning("Greška pri učitavanju podsetnika: " + e.getMessage());
        }

        podsetniciBox.getChildren().addAll(lblPodsetnici, tabelaPodsetnici);

        // Niska zaliha
        VBox zalihaBox = new VBox(10);
        Label lblZaliha = new Label("Artikli ispod minimalnog stanja");
        lblZaliha.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0a1628;");

        TableView<Artikal> tabelaZaliha = new TableView<>();
        tabelaZaliha.getStyleClass().add("table-view");
        tabelaZaliha.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabelaZaliha.setPrefHeight(160);

        TableColumn<Artikal, String> colNaziv = new TableColumn<>("Artikal");
        colNaziv.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getNaziv()));

        TableColumn<Artikal, String> colKolicina = new TableColumn<>("Na stanju");
        colKolicina.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getKolicina() + " " + c.getValue().getJedinicaMere()));
        colKolicina.setPrefWidth(100);

        TableColumn<Artikal, String> colMin = new TableColumn<>("Minimum");
        colMin.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getMinimalnaKolicina() + " " + c.getValue().getJedinicaMere()));
        colMin.setPrefWidth(100);

        tabelaZaliha.getColumns().addAll(colNaziv, colKolicina, colMin);

        try {
            ArtikalDao artikalDao = new ArtikalDao();
            List<Artikal> ispodMin = artikalDao.vratiIspodMinimuma();
            tabelaZaliha.setItems(javafx.collections.FXCollections.observableArrayList(ispodMin));
            if (ispodMin.isEmpty()) {
                tabelaZaliha.setPlaceholder(new Label("Svi artikli su na zadovoljavajućem stanju"));
            }
        } catch (Exception e) {
            logger.warning("Greška pri učitavanju zaliha: " + e.getMessage());
        }

        zalihaBox.getChildren().addAll(lblZaliha, tabelaZaliha);

        // Predračuni koji ističu
        VBox predracunIsticeBox = new VBox(10);
        Label lblPredracunIstice = new Label("Predračuni koji ističu u narednih 7 dana");
        lblPredracunIstice.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0a1628;");

        TableView<Predracun> tabelaPredracuni = new TableView<>();
        tabelaPredracuni.getStyleClass().add("table-view");
        tabelaPredracuni.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tabelaPredracuni.setPrefHeight(140);

        TableColumn<Predracun, String> colPrBroj = new TableColumn<>("Broj");
        colPrBroj.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getBrojPredracuna()));
        colPrBroj.setPrefWidth(100);

        TableColumn<Predracun, String> colPrKlijent = new TableColumn<>("Klijent");
        colPrKlijent.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                klijentImeFinal.getOrDefault(c.getValue().getKlijentId(), "")));

        TableColumn<Predracun, String> colPrVazenje = new TableColumn<>("Važi do");
        colPrVazenje.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(
                c.getValue().getDatumVazenja() != null ? c.getValue().getDatumVazenja() : "—"));
        colPrVazenje.setPrefWidth(100);

        TableColumn<Predracun, String> colPrStatus = new TableColumn<>("Status");
        colPrStatus.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getStatus()));
        colPrStatus.setPrefWidth(90);

        tabelaPredracuni.getColumns().addAll(colPrBroj, colPrKlijent, colPrVazenje, colPrStatus);

        try {
            PredracunDao pDao = new PredracunDao();
            java.util.List<Predracun> isticuSutra = pDao.vratiKojiIsticuZaDana(1);
            java.util.List<Predracun> isticuZa7 = pDao.vratiKojiIsticuZaDana(7);
            java.util.Set<Integer> videni = new java.util.HashSet<>();
            java.util.List<Predracun> kombinovana = new java.util.ArrayList<>();
            for (Predracun p : isticuSutra) { kombinovana.add(p); videni.add(p.getId()); }
            for (Predracun p : isticuZa7) { if (!videni.contains(p.getId())) kombinovana.add(p); }
            tabelaPredracuni.setItems(javafx.collections.FXCollections.observableArrayList(kombinovana));
            if (kombinovana.isEmpty()) {
                tabelaPredracuni.setPlaceholder(new Label("Nema predračuna koji ističu u narednih 7 dana"));
            }
        } catch (Exception e) {
            logger.warning("Greška pri učitavanju predračuna koji ističu: " + e.getMessage());
        }

        predracunIsticeBox.getChildren().addAll(lblPredracunIstice, tabelaPredracuni);

        content.getChildren().addAll(statovi, aktivniBox, predracunIsticeBox, podsetniciBox, zalihaBox);

        page.setTop(topbar);
        page.setCenter(new ScrollPane(content) {{
            setFitToWidth(true);
            setStyle("-fx-background-color: transparent;");
        }});

        pageArea.setCenter(page);
    }

    private void prikaziDetaljeNaloga(RadniNalog nalog, Klijent klijent, Vozilo vozilo, RadniNalogDao nalogDao) {
        try {
            RadniNalog pun = nalogDao.vratiPoId(nalog.getId());

            Stage stage = new Stage();
            AppIkona.postavi(stage);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Detalji naloga — " + nalog.getBrojNaloga());

            VBox root = new VBox(14);
            root.setPadding(new Insets(20));
            root.setStyle("-fx-background-color: #f8fafd;");

            // Header
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
                                    : "-fx-background-color: #dbeafe; -fx-text-fill: #1e40af;")
            );

            header.getChildren().addAll(lblBroj, spacer, badge);

            // Klijent i vozilo
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

            // Opis kvara
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

            // Usluge
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

            // Artikli
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

            // Zatvori dugme
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
            logger.warning("Greška pri prikazu detalja: " + e.getMessage());
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Greška");
            alert.setContentText("Greška pri prikazu detalja naloga: " + e.getMessage());
            alert.showAndWait();
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

    private void prikaziPlaceholder(String naziv) {
        VBox content = new VBox();
        content.getStyleClass().add("content-area");
        content.setAlignment(Pos.CENTER);
        Label lbl = new Label(naziv + " — ekran dolazi uskoro");
        lbl.setStyle("-fx-font-size: 16px; -fx-text-fill: #868e96;");
        content.getChildren().add(lbl);
        pageArea.setCenter(kreirajPage(naziv, content));
    }

    // ----------------------------------------------------------------
    // POMOCNE METODE
    // ----------------------------------------------------------------
    private BorderPane kreirajPage(String naziv, VBox content) {
        HBox topbar = new HBox();
        topbar.getStyleClass().add("topbar");
        Label breadcrumb = new Label("Početna / ");
        breadcrumb.getStyleClass().add("topbar-title");
        Label bold = new Label(naziv);
        bold.getStyleClass().add("topbar-title-bold");
        topbar.getChildren().addAll(breadcrumb, bold);

        BorderPane page = new BorderPane();
        page.setTop(topbar);
        page.setCenter(content);
        return page;
    }

    private VBox kreirajStatKarticu(String label, String value, String sub, String styleClass) {
        VBox card = new VBox(4);
        card.getStyleClass().addAll("stat-card", styleClass);
        card.setPrefWidth(220);
        Label lbl = new Label(label);
        lbl.getStyleClass().add("stat-label");
        Label val = new Label(value);
        val.getStyleClass().add("stat-value");
        Label s = new Label(sub);
        s.getStyleClass().add("stat-sub");
        card.getChildren().addAll(lbl, val, s);
        return card;
    }
}