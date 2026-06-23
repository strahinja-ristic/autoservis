package autoservis.servis.util;

import autoservis.servis.dao.FakturaDao;
import autoservis.servis.dao.RadniNalogDao;
import autoservis.servis.model.*;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class FakturaPdfPreviewController {

    private static final Logger logger = Logger.getLogger(FakturaPdfPreviewController.class.getName());

    private final Faktura faktura;
    private final Klijent klijent;
    private final Vozilo vozilo;
    private final Podesavanja firma;
    private final Runnable onStatusChange;

    private String privremeniPdf;
    private final List<javafx.scene.image.Image> stranice = new ArrayList<>();
    private int trenutnaStrana = 0;
    private ImageView imageView;
    private Label lblStrana;

    public FakturaPdfPreviewController(Faktura faktura, Klijent klijent, Vozilo vozilo,
                                       Podesavanja firma, Runnable onStatusChange) {
        this.faktura = faktura;
        this.klijent = klijent;
        this.vozilo = vozilo;
        this.firma = firma;
        this.onStatusChange = onStatusChange;
    }

    public void show() {
        Stage stage = new Stage();
        AppIkona.postavi(stage);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Prilog uz račun — " + faktura.getBrojFakture());

        try {
            privremeniPdf = System.getProperty("java.io.tmpdir") +
                    "/preview_fakt_" + faktura.getBrojFakture().replace("-", "_") + ".pdf";
            java.util.List<String> usluge = ucitajUslugeIzNaloga();
            FinansijskiPdfGenerator.generisiFakturuNaPutanju(faktura, klijent, vozilo, firma, usluge, privremeniPdf);
            ucitajStranice();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Greška");
            alert.setContentText("Greška pri generisanju preview-a: " + e.getMessage());
            alert.showAndWait();
            return;
        }

        HBox topbar = new HBox(10);
        topbar.setStyle("-fx-background-color: #0a1628; -fx-padding: 10 16 10 16;");
        topbar.setAlignment(Pos.CENTER_LEFT);

        Label lblNaslov = new Label("Prilog uz račun — " + faktura.getBrojFakture());
        lblNaslov.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnSacuvaj = new Button("Sačuvaj PDF");
        btnSacuvaj.setStyle("-fx-background-color: #0057b7; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-padding: 7 16 7 16; -fx-background-radius: 6; -fx-cursor: hand;");
        btnSacuvaj.setOnAction(e -> sacuvajPdf(stage));

        Button btnStampaj = new Button("Štampaj");
        btnStampaj.setStyle("-fx-background-color: #374151; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-padding: 7 16 7 16; -fx-background-radius: 6; -fx-cursor: hand;");
        btnStampaj.setOnAction(e -> stampaj());

        Button btnEmail = new Button("Pošalji email");
        btnEmail.setStyle("-fx-background-color: #374151; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-padding: 7 16 7 16; -fx-background-radius: 6; -fx-cursor: hand;");
        btnEmail.setDisable(firma.getGmailAdresa() == null || firma.getGmailAdresa().isBlank());
        btnEmail.setOnAction(e -> posaljiEmail(stage));

        Button btnZatvori = new Button("Zatvori");
        btnZatvori.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.7); " +
                "-fx-padding: 7 16 7 16; -fx-background-radius: 6; -fx-cursor: hand; " +
                "-fx-border-color: rgba(255,255,255,0.3); -fx-border-radius: 6; -fx-border-width: 1;");
        btnZatvori.setOnAction(e -> stage.close());

        topbar.getChildren().addAll(lblNaslov, spacer, btnSacuvaj, btnStampaj, btnEmail, btnZatvori);

        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(750);

        ScrollPane scroll = new ScrollPane(imageView);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #6b7280;");

        Button btnPrethodna = new Button("◀ Prethodna");
        btnPrethodna.setStyle("-fx-background-color: #374151; -fx-text-fill: white; " +
                "-fx-padding: 6 14 6 14; -fx-background-radius: 6; -fx-cursor: hand;");
        btnPrethodna.setOnAction(e -> {
            if (trenutnaStrana > 0) { trenutnaStrana--; prikaziStranu(); }
        });

        lblStrana = new Label();
        lblStrana.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

        Button btnSledeca = new Button("Sledeća ▶");
        btnSledeca.setStyle("-fx-background-color: #374151; -fx-text-fill: white; " +
                "-fx-padding: 6 14 6 14; -fx-background-radius: 6; -fx-cursor: hand;");
        btnSledeca.setOnAction(e -> {
            if (trenutnaStrana < stranice.size() - 1) { trenutnaStrana++; prikaziStranu(); }
        });

        HBox navBar = new HBox(12, btnPrethodna, lblStrana, btnSledeca);
        navBar.setAlignment(Pos.CENTER);
        navBar.setStyle("-fx-background-color: #1f2937; -fx-padding: 8 16 8 16;");

        BorderPane root = new BorderPane();
        root.setTop(topbar);
        root.setCenter(scroll);
        root.setBottom(navBar);

        prikaziStranu();

        Scene scene = new Scene(root, 820, 750);
        stage.setScene(scene);
        stage.setMaximized(false);
        stage.showAndWait();
    }

    private java.util.List<String> ucitajUslugeIzNaloga() {
        if (faktura.getRadniNalogId() == null) return java.util.Collections.emptyList();
        try {
            RadniNalog nalog = new RadniNalogDao().vratiPoId(faktura.getRadniNalogId());
            if (nalog == null || nalog.getUsluge() == null || nalog.getUsluge().isEmpty())
                return java.util.Collections.emptyList();
            return nalog.getUsluge().stream()
                    .map(NalogUsluga::getNaziv)
                    .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            logger.warning("Greška pri učitavanju usluga iz naloga: " + e.getMessage());
            return java.util.Collections.emptyList();
        }
    }

    private void ucitajStranice() throws Exception {
        PDDocument pdDoc = org.apache.pdfbox.Loader.loadPDF(new File(privremeniPdf));
        PDFRenderer renderer = new PDFRenderer(pdDoc);
        for (int i = 0; i < pdDoc.getNumberOfPages(); i++) {
            BufferedImage img = renderer.renderImageWithDPI(i, 150);
            stranice.add(SwingFXUtils.toFXImage(img, null));
        }
        pdDoc.close();
    }

    private void prikaziStranu() {
        if (stranice.isEmpty()) return;
        imageView.setImage(stranice.get(trenutnaStrana));
        lblStrana.setText("Strana " + (trenutnaStrana + 1) + " od " + stranice.size());
    }

    private void sacuvajPdf(Stage stage) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Sačuvaj prilog uz račun");
        fc.setInitialFileName("FAKT_" + faktura.getBrojFakture().replace("-", "_") + ".pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF fajlovi", "*.pdf"));
        fc.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));
        File dest = fc.showSaveDialog(stage);
        if (dest != null) {
            try {
                Files.copy(new File(privremeniPdf).toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Uspešno");
                info.setHeaderText(null);
                info.setContentText("PDF je sačuvan:\n" + dest.getAbsolutePath());
                info.showAndWait();
                stage.close();
            } catch (Exception e) {
                prikaziGresku("Greška pri čuvanju: " + e.getMessage());
            }
        }
    }

    private void stampaj() {
        try {
            java.io.File pdfFajl = new java.io.File(privremeniPdf);
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                if (desktop.isSupported(java.awt.Desktop.Action.PRINT)) {
                    desktop.print(pdfFajl);
                } else {
                    desktop.open(pdfFajl);
                }
            }
        } catch (Exception e) {
            prikaziGresku("Greška pri štampanju: " + e.getMessage());
        }
    }

    private void posaljiEmail(Stage stage) {
        String emailKlijenta = klijent != null ? klijent.getEmail() : null;
        if (emailKlijenta == null || emailKlijenta.isBlank()) {
            prikaziGresku("Klijent nema upisanu email adresu.");
            return;
        }
        try {
            String naslov = firma.getEmailNaslovFaktura();
            if (naslov == null || naslov.isBlank())
                naslov = "Prilog uz račun " + faktura.getBrojFakture();

            String telo = firma.getEmailTekstFaktura();
            if (telo == null || telo.isBlank())
                telo = "U prilogu se nalazi prilog uz račun " + faktura.getBrojFakture() + ".";

            StringBuilder poruka = new StringBuilder();
            if (firma.getEmailZaglavlje() != null && !firma.getEmailZaglavlje().isBlank())
                poruka.append(firma.getEmailZaglavlje()).append("\n\n");
            poruka.append(telo);
            if (firma.getEmailFooter() != null && !firma.getEmailFooter().isBlank())
                poruka.append("\n\n").append(firma.getEmailFooter());

            EmailService.posaljiPdf(firma,
                    emailKlijenta,
                    naslov,
                    poruka.toString(),
                    privremeniPdf);

            new FakturaDao().promeniStatus(faktura.getId(), "Poslata");
            if (onStatusChange != null) onStatusChange.run();

            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Uspešno");
            info.setHeaderText(null);
            info.setContentText("Email je poslat na: " + emailKlijenta);
            info.showAndWait();
            stage.close();
        } catch (Exception e) {
            prikaziGresku("Greška pri slanju email-a: " + e.getMessage());
        }
    }

    private void prikaziGresku(String poruka) {
        Alert a = new Alert(Alert.AlertType.ERROR);
        a.setTitle("Greška");
        a.setHeaderText(null);
        a.setContentText(poruka);
        a.showAndWait();
    }
}
