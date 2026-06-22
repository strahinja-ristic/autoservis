package autoservis.servis.util;

import autoservis.servis.dao.PodesavanjaDao;
import autoservis.servis.dao.UlazDokumentDao;
import autoservis.servis.model.Podesavanja;
import autoservis.servis.model.UlazDokument;
import autoservis.servis.model.UlazMagacin;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
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

public class UlazRobePdfPreviewController {

    private final UlazDokument dokument;
    private String privremeniPdf;
    private final List<javafx.scene.image.Image> stranice = new ArrayList<>();
    private int trenutnaStrana = 0;
    private ImageView imageView;
    private Label lblStrana;

    public UlazRobePdfPreviewController(UlazDokument dokument) {
        this.dokument = dokument;
    }

    public void show() {
        Stage stage = new Stage();
        AppIkona.postavi(stage);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Prijem robe — " + dokument.getBroj());

        List<UlazMagacin> stavke;
        Podesavanja firma;
        try {
            stavke = new UlazDokumentDao().vratiStavke(dokument.getId());
            firma = new PodesavanjaDao().vratiPodesavanja();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Greška pri učitavanju podataka: " + e.getMessage()).showAndWait();
            return;
        }

        try {
            privremeniPdf = System.getProperty("java.io.tmpdir") + "/preview_ulaz_" + dokument.getId() + ".pdf";
            UlazRobePdfGenerator.generisiNaPutanju(dokument, stavke, firma, privremeniPdf);
            ucitajStranice();
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Greška pri generisanju PDF-a: " + e.getMessage()).showAndWait();
            return;
        }

        // Topbar
        HBox topbar = new HBox(10);
        topbar.setStyle("-fx-background-color: #0a1628; -fx-padding: 10 16 10 16;");
        topbar.setAlignment(Pos.CENTER_LEFT);

        Label lblNaslov = new Label("Prijem robe " + dokument.getBroj() + "  ·  " + dokument.getDatum());
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

        Button btnZatvori = new Button("Zatvori");
        btnZatvori.setStyle("-fx-background-color: transparent; -fx-text-fill: rgba(255,255,255,0.7); " +
                "-fx-padding: 7 16 7 16; -fx-background-radius: 6; -fx-cursor: hand; " +
                "-fx-border-color: rgba(255,255,255,0.3); -fx-border-radius: 6; -fx-border-width: 1;");
        btnZatvori.setOnAction(e -> stage.close());

        topbar.getChildren().addAll(lblNaslov, spacer, btnSacuvaj, btnStampaj, btnZatvori);

        // Prikaz
        imageView = new ImageView();
        imageView.setPreserveRatio(true);
        imageView.setFitWidth(750);

        ScrollPane scroll = new ScrollPane(imageView);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: #6b7280;");

        // Navigacija
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
        try {
            java.io.File fajl = new java.io.File(privremeniPdf);
            if (java.awt.Desktop.isDesktopSupported()) {
                java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
                if (desktop.isSupported(java.awt.Desktop.Action.PRINT)) desktop.print(fajl);
                else desktop.open(fajl);
            }
        } catch (Exception e) {
            new Alert(Alert.AlertType.ERROR, "Greška pri štampanju: " + e.getMessage()).showAndWait();
        }
    }

    private void sacuvajPdf(Stage stage) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Sačuvaj prijem robe");
        fc.setInitialFileName("PrijemRobe_" + dokument.getBroj().replace("/", "-") + ".pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF fajlovi", "*.pdf"));
        fc.setInitialDirectory(new File(System.getProperty("user.home") + "/Desktop"));

        File odabraniFajl = fc.showSaveDialog(stage);
        if (odabraniFajl != null) {
            try {
                Files.copy(new File(privremeniPdf).toPath(), odabraniFajl.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
                Alert info = new Alert(Alert.AlertType.INFORMATION);
                info.setTitle("Uspešno");
                info.setHeaderText(null);
                info.setContentText("Sačuvano:\n" + odabraniFajl.getAbsolutePath());
                info.showAndWait();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Greška pri čuvanju: " + e.getMessage()).showAndWait();
            }
        }
    }
}
