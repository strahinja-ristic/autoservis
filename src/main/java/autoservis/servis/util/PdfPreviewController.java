package autoservis.servis.util;

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

public class PdfPreviewController {

    private RadniNalog nalog;
    private Klijent klijent;
    private Vozilo vozilo;
    private Podesavanja firma;
    private List<String> usluge;
    private List<NalogArtikal> artikli;
    private String privremeniPdf;
    private List<javafx.scene.image.Image> stranice = new ArrayList<>();
    private int trenutnaStrana = 0;
    private ImageView imageView;
    private Label lblStrana;

    public PdfPreviewController(RadniNalog nalog, Klijent klijent, Vozilo vozilo,
                                Podesavanja firma, List<String> usluge, List<NalogArtikal> artikli) {
        this.nalog = nalog;
        this.klijent = klijent;
        this.vozilo = vozilo;
        this.firma = firma;
        this.usluge = usluge;
        this.artikli = artikli;
    }

    public void show() {
        Stage stage = new Stage();
        AppIkona.postavi(stage);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Preview — Radni nalog " + nalog.getBrojNaloga());

        // Generisi privremeni PDF
        try {
            privremeniPdf = System.getProperty("java.io.tmpdir") +
                    "/preview_rn_" + nalog.getBrojNaloga().replace("-", "_") + ".pdf";
            PdfGenerator.generisiNaPutanju(nalog, klijent, vozilo, firma, usluge, artikli, privremeniPdf);
            ucitajStranice();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Greška");
            alert.setContentText("Greška pri generisanju preview-a: " + e.getMessage());
            alert.showAndWait();
            return;
        }

        // Topbar
        HBox topbar = new HBox(10);
        topbar.setStyle("-fx-background-color: #0a1628; -fx-padding: 10 16 10 16;");
        topbar.setAlignment(Pos.CENTER_LEFT);

        Label lblNaslov = new Label("Radni nalog — " + nalog.getBrojNaloga());
        lblNaslov.setStyle("-fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button btnSacuvaj = new Button("Sačuvaj PDF");
        btnSacuvaj.setStyle("-fx-background-color: #0057b7; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-padding: 7 16 7 16; -fx-background-radius: 6; -fx-cursor: hand;");
        btnSacuvaj.setOnAction(e -> sacuvajPdf(stage));

        Button btnZatvori = new Button("Zatvori");
        btnZatvori.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.7); " +
                "-fx-padding: 7 16 7 16; -fx-background-radius: 6; -fx-cursor: hand; " +
                "-fx-border-color: rgba(255,255,255,0.3); -fx-border-radius: 6; -fx-border-width: 1;");
        btnZatvori.setOnAction(e -> stage.close());

        Button btnStampaj = new Button("Štampaj");
        btnStampaj.setStyle("-fx-background-color: #374151; -fx-text-fill: white; -fx-font-weight: bold; " +
                "-fx-padding: 7 16 7 16; -fx-background-radius: 6; -fx-cursor: hand;");
        btnStampaj.setOnAction(e -> stampaj());

        topbar.getChildren().addAll(lblNaslov, spacer, btnSacuvaj, btnStampaj, btnZatvori);

        // Prikaz stranice
        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(750);

        ScrollPane scroll = new ScrollPane(imageView);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #6b7280;");

        // Navigacija stranica
        Button btnPrethodna = new Button("◀ Prethodna");
        btnPrethodna.setStyle("-fx-background-color: #374151; -fx-text-fill: white; " +
                "-fx-padding: 6 14 6 14; -fx-background-radius: 6; -fx-cursor: hand;");
        btnPrethodna.setOnAction(e -> {
            if (trenutnaStrana > 0) {
                trenutnaStrana--;
                prikaziStranu();
            }
        });

        lblStrana = new Label();
        lblStrana.setStyle("-fx-text-fill: white; -fx-font-size: 12px;");

        Button btnSledeca = new Button("Sledeća ▶");
        btnSledeca.setStyle("-fx-background-color: #374151; -fx-text-fill: white; " +
                "-fx-padding: 6 14 6 14; -fx-background-radius: 6; -fx-cursor: hand;");
        btnSledeca.setOnAction(e -> {
            if (trenutnaStrana < stranice.size() - 1) {
                trenutnaStrana++;
                prikaziStranu();
            }
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

    private void stampaj() {
        java.io.File pdfFajl = new java.io.File(privremeniPdf);
        try {
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                if (desktop.isSupported(java.awt.Desktop.Action.PRINT)) {
                    desktop.print(pdfFajl);
                } else {
                    desktop.open(pdfFajl);
                }
            }
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Greška");
            alert.setContentText("Greška pri štampanju: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void sacuvajPdf(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Sačuvaj radni nalog");
        fileChooser.setInitialFileName("RN_" + nalog.getBrojNaloga().replace("-", "_") + ".pdf");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF fajlovi", "*.pdf")
        );
        fileChooser.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));

        File odabraniFajl = fileChooser.showSaveDialog(stage);
        if (odabraniFajl != null) {
            try {
                Files.copy(new File(privremeniPdf).toPath(),
                        odabraniFajl.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);

                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Uspešno");
                info.setHeaderText(null);
                info.setContentText("PDF je sačuvan:\n" + odabraniFajl.getAbsolutePath());
                info.showAndWait();
                stage.close();
            } catch (Exception e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Greška");
                alert.setContentText("Greška pri čuvanju: " + e.getMessage());
                alert.showAndWait();
            }
        }
    }
}