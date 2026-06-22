package autoservis.servis.controller;

import autoservis.servis.dao.KlijentDao;
import autoservis.servis.util.AppIkona;
import autoservis.servis.model.Klijent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class KlijentFormaController {

    private Klijent klijent;
    private Runnable onSave;
    private KlijentDao klijentDao;
    private Stage stage;

    // Forma polja
    private ToggleGroup tipGroup;
    private RadioButton rbFizicko;
    private RadioButton rbPravno;
    private TextField tfIme;
    private TextField tfPrezime;
    private TextField tfNazivFirme;
    private TextField tfPib;
    private TextField tfMaticniBroj;
    private TextField tfAdresa;
    private TextField tfTelefon;
    private TextField tfEmail;
    private TextArea taNapomena;

    // Paneli koji se menjaju zavisno od tipa
    private VBox panelFizicko;
    private VBox panelPravno;

    public KlijentFormaController(Klijent klijent, Runnable onSave) {
        this.klijent = klijent;
        this.onSave = onSave;
        this.klijentDao = new KlijentDao();
    }

    public void show() {
        stage = new Stage();
        AppIkona.postavi(stage);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(klijent == null ? "Novi klijent" : "Izmena klijenta");
        stage.setMinWidth(450);

        VBox root = new VBox(16);
        root.getStyleClass().add("form-card");
        root.setPadding(new Insets(20));

        // Tip klijenta
        Label lblTip = new Label("Tip klijenta");
        lblTip.getStyleClass().add("form-label");

        tipGroup = new ToggleGroup();
        rbFizicko = new RadioButton("Fizičko lice");
        rbPravno = new RadioButton("Pravno lice");
        rbFizicko.setToggleGroup(tipGroup);
        rbPravno.setToggleGroup(tipGroup);
        rbFizicko.setSelected(true);

        HBox tipBox = new HBox(16, rbFizicko, rbPravno);
        tipBox.setAlignment(Pos.CENTER_LEFT);

        // Panel za fizicko lice
        panelFizicko = new VBox(10);
        tfIme = kreirajPolje("Ime");
        tfPrezime = kreirajPolje("Prezime");
        panelFizicko.getChildren().addAll(
                kreirajRed("Ime", tfIme),
                kreirajRed("Prezime", tfPrezime)
        );

        // Panel za pravno lice
        panelPravno = new VBox(10);
        panelPravno.setVisible(false);
        panelPravno.setManaged(false);
        tfNazivFirme = kreirajPolje("Naziv firme");
        tfPib = kreirajPolje("PIB");
        tfMaticniBroj = kreirajPolje("Matični broj");
        panelPravno.getChildren().addAll(
                kreirajRed("Naziv firme", tfNazivFirme),
                kreirajRed("PIB", tfPib),
                kreirajRed("Matični broj", tfMaticniBroj)
        );

        // Zajednicki podaci
        tfAdresa = kreirajPolje("Adresa");
        tfTelefon = kreirajPolje("Telefon");
        tfEmail = kreirajPolje("Email");

        taNapomena = new TextArea();
        taNapomena.setPromptText("npr. plaća gotovinom, VIP klijent...");
        taNapomena.setPrefRowCount(2);
        taNapomena.setWrapText(true);

        VBox zajednicki = new VBox(10);
        zajednicki.getChildren().addAll(
                kreirajRed("Adresa", tfAdresa),
                kreirajRed("Telefon", tfTelefon),
                kreirajRed("Email", tfEmail),
                kreirajNapomenaRed("Napomena", taNapomena)
        );

        // Promena tipa
        tipGroup.selectedToggleProperty().addListener((obs, old, novo) -> {
            boolean isPravno = novo == rbPravno;
            panelFizicko.setVisible(!isPravno);
            panelFizicko.setManaged(!isPravno);
            panelPravno.setVisible(isPravno);
            panelPravno.setManaged(isPravno);
            stage.sizeToScene();
        });

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
                lblTip, tipBox,
                panelFizicko, panelPravno,
                zajednicki,
                new Separator(),
                dugmad
        );

        // Popuni formu ako je izmena
        if (klijent != null) {
            popuniFormu();
        }

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/style/style.css").toExternalForm());
        stage.setWidth(480);
        stage.setScene(scene);
        stage.showAndWait();
    }

    private VBox kreirajNapomenaRed(String labelTekst, TextArea polje) {
        VBox red = new VBox(4);
        Label lbl = new Label(labelTekst);
        lbl.getStyleClass().add("form-label");
        polje.getStyleClass().add("form-textarea");
        red.getChildren().addAll(lbl, polje);
        return red;
    }

    private void popuniFormu() {
        if ("Pravno".equals(klijent.getTip())) {
            rbPravno.setSelected(true);
            tfNazivFirme.setText(klijent.getNazivFirme());
            tfPib.setText(klijent.getPib());
            tfMaticniBroj.setText(klijent.getMaticniBroj());

        } else {
            rbFizicko.setSelected(true);
            tfIme.setText(klijent.getIme());
            tfPrezime.setText(klijent.getPrezime());
        }
        tfAdresa.setText(klijent.getAdresa() != null ? klijent.getAdresa() : "");
        tfTelefon.setText(klijent.getTelefon() != null ? klijent.getTelefon() : "");
        tfEmail.setText(klijent.getEmail() != null ? klijent.getEmail() : "");
        taNapomena.setText(klijent.getNapomena() != null ? klijent.getNapomena() : "");

    }

    private void sacuvaj() {
        if (!validiraj()) return;

        Klijent k = klijent != null ? klijent : new Klijent();

        if (rbPravno.isSelected()) {
            k.setTip("Pravno");
            k.setNazivFirme(tfNazivFirme.getText().trim());
            k.setPib(tfPib.getText().trim());
            k.setMaticniBroj(tfMaticniBroj.getText().trim());
            k.setIme("");
            k.setPrezime("");
        } else {
            k.setTip("Fizičko");
            k.setIme(tfIme.getText().trim());
            k.setPrezime(tfPrezime.getText().trim());
            k.setNazivFirme("");
            k.setPib("");
            k.setMaticniBroj("");
        }

        k.setAdresa(tfAdresa.getText().trim());
        k.setTelefon(tfTelefon.getText().trim());
        k.setEmail(tfEmail.getText().trim());
        k.setNapomena(taNapomena.getText().trim());

        try {
            if (klijent == null) {
                klijentDao.dodaj(k);
            } else {
                klijentDao.izmeni(k);
            }
            onSave.run();
            stage.close();
        } catch (Exception e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Greška");
            alert.setContentText("Greška pri čuvanju: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private boolean validiraj() {
        if (rbFizicko.isSelected()) {
            if (tfIme.getText().isBlank() || tfPrezime.getText().isBlank()) {
                prikaziGresku("Ime i prezime su obavezni.");
                return false;
            }
        } else {
            if (tfNazivFirme.getText().isBlank()) {
                prikaziGresku("Naziv firme je obavezan.");
                return false;
            }
        }
        return true;
    }

    private TextField kreirajPolje(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.getStyleClass().add("form-field");
        tf.setMaxWidth(Double.MAX_VALUE);
        return tf;
    }

    private VBox kreirajRed(String labelTekst, TextField polje) {
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