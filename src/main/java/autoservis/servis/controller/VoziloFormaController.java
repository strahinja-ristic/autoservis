package autoservis.servis.controller;

import autoservis.servis.dao.KlijentDao;
import autoservis.servis.dao.VoziloDao;
import autoservis.servis.model.Klijent;
import autoservis.servis.model.Vozilo;
import autoservis.servis.util.AppIkona;
import autoservis.servis.util.AutoCompleteField;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class VoziloFormaController {

    private Vozilo vozilo;
    private Runnable onSave;
    private VoziloDao voziloDao;
    private KlijentDao klijentDao;
    private Stage stage;

    private AutoCompleteField<Klijent> acKlijent;
    private TextField tfMarka;
    private TextField tfModel;
    private TextField tfGodiste;
    private TextField tfRegistracija;
    private TextField tfBrojSasije;
    private TextField tfKilometraza;
    private TextArea taNapomena;

    public VoziloFormaController(Vozilo vozilo, Runnable onSave) {
        this.vozilo = vozilo;
        this.onSave = onSave;
        this.voziloDao = new VoziloDao();
        this.klijentDao = new KlijentDao();
    }

    public void show() {
        stage = new Stage();
        AppIkona.postavi(stage);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(vozilo == null ? "Novo vozilo" : "Izmena vozila");
        stage.setMinWidth(460);

        VBox root = new VBox(12);
        root.getStyleClass().add("form-card");
        root.setPadding(new Insets(20));

        ucitajKlijente();

        tfMarka = kreirajPolje("npr. Volkswagen");
        tfModel = kreirajPolje("npr. Golf");
        tfGodiste = kreirajPolje("npr. 2018");
        tfRegistracija = kreirajPolje("npr. BG123AB");
        tfBrojSasije = kreirajPolje("Broj šasije");
        tfKilometraza = kreirajPolje("npr. 85000");
        taNapomena = new TextArea();
        taNapomena.setPromptText("Napomena (npr. Fabrički metan, LPG uređaj...)");
        taNapomena.setPrefRowCount(3);
        taNapomena.setWrapText(true);
        taNapomena.getStyleClass().add("form-field");

        tfRegistracija.setTextFormatter(new TextFormatter<>(change -> {
            change.setText(change.getText().toUpperCase());
            return change;
        }));
        tfBrojSasije.setTextFormatter(new TextFormatter<>(change -> {
            change.setText(change.getText().toUpperCase());
            return change;
        }));

        VBox forma = new VBox(10);
        forma.getChildren().addAll(
                kreirajRedNode("Klijent *", acKlijent.getTextField()),
                kreirajRed("Marka *", tfMarka),
                kreirajRed("Model *", tfModel),
                kreirajRed("Godište", tfGodiste),
                kreirajRed("Registracija", tfRegistracija),
                kreirajRed("Broj šasije", tfBrojSasije),
                kreirajRed("Kilometraža", tfKilometraza),
                kreirajRedNode("Uređaj", taNapomena)
        );

        Button btnSacuvaj = new Button("Sačuvaj");
        btnSacuvaj.getStyleClass().add("btn-primary");
        btnSacuvaj.setOnAction(e -> sacuvaj());

        Button btnOtkazi = new Button("Otkaži");
        btnOtkazi.getStyleClass().add("btn-secondary");
        btnOtkazi.setOnAction(e -> stage.close());

        HBox dugmad = new HBox(8, btnSacuvaj, btnOtkazi);
        dugmad.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(forma, new Separator(), dugmad);

        if (vozilo != null) {
            popuniFormu();
        }

        Scene scene = new Scene(root);
        stage.setWidth(480);
        scene.getStylesheets().add(getClass().getResource("/style/style.css").toExternalForm());
        stage.setScene(scene);
        stage.showAndWait();
    }

    private void ucitajKlijente() {
        try {
            List<Klijent> lista = klijentDao.vratiSve();
            acKlijent = new AutoCompleteField<>(lista, Klijent::getPunoIme);
            acKlijent.setPromptText("Pretraži klijenta...");
        } catch (SQLException e) {
            prikaziGresku("Greška pri učitavanju klijenata.");
        }
    }

    private void popuniFormu() {
        try {
            Klijent k = klijentDao.vratiPoId(vozilo.getKlijentId());
            if (k != null) acKlijent.postavi(k);
        } catch (SQLException e) {
            prikaziGresku("Greška pri učitavanju klijenta: " + e.getMessage());
        }
        tfMarka.setText(vozilo.getMarka());
        tfModel.setText(vozilo.getModel());
        tfGodiste.setText(vozilo.getGodiste() != null ? String.valueOf(vozilo.getGodiste()) : "");
        tfRegistracija.setText(vozilo.getRegistracija() != null ? vozilo.getRegistracija() : "");
        tfBrojSasije.setText(vozilo.getBrojSasije() != null ? vozilo.getBrojSasije() : "");
        tfKilometraza.setText(vozilo.getKilometraza() > 0 ? String.valueOf(vozilo.getKilometraza()) : "");
        taNapomena.setText(vozilo.getNapomena() != null ? vozilo.getNapomena() : "");
    }

    private void sacuvaj() {
        if (!validiraj()) return;

        Vozilo v = vozilo != null ? vozilo : new Vozilo();
        v.setKlijentId(acKlijent.getOdabraniElement().getId());
        v.setMarka(tfMarka.getText().trim());
        v.setModel(tfModel.getText().trim());
        v.setGodiste(tfGodiste.getText().isBlank() ? null : Integer.parseInt(tfGodiste.getText().trim()));
        v.setRegistracija(tfRegistracija.getText().trim());
        v.setBrojSasije(tfBrojSasije.getText().trim());
        v.setKilometraza(tfKilometraza.getText().isBlank() ? 0 : Integer.parseInt(tfKilometraza.getText().trim()));
        v.setNapomena(taNapomena.getText().isBlank() ? null : taNapomena.getText().trim());

        try {
            if (vozilo == null) {
                voziloDao.dodaj(v);
            } else {
                voziloDao.izmeni(v);
            }
            onSave.run();
            stage.close();
        } catch (Exception e) {
            prikaziGresku("Greška pri čuvanju: " + e.getMessage());
        }
    }

    private boolean validiraj() {
        if (acKlijent.getOdabraniElement() == null) {
            prikaziGresku("Morate odabrati klijenta.");
            return false;
        }
        if (tfMarka.getText().isBlank()) {
            prikaziGresku("Marka je obavezna.");
            return false;
        }
        if (tfModel.getText().isBlank()) {
            prikaziGresku("Model je obavezan.");
            return false;
        }
        if (!tfGodiste.getText().isBlank()) {
            try {
                Integer.parseInt(tfGodiste.getText().trim());
            } catch (NumberFormatException e) {
                prikaziGresku("Godište mora biti broj.");
                return false;
            }
        }
        if (!tfKilometraza.getText().isBlank()) {
            try {
                Integer.parseInt(tfKilometraza.getText().trim());
            } catch (NumberFormatException e) {
                prikaziGresku("Kilometraža mora biti broj.");
                return false;
            }
        }
        return true;
    }

    private VBox kreirajRed(String labelTekst, TextField polje) {
        VBox red = new VBox(4);
        Label lbl = new Label(labelTekst);
        lbl.getStyleClass().add("form-label");
        polje.getStyleClass().add("form-field");
        red.getChildren().addAll(lbl, polje);
        return red;
    }

    private VBox kreirajRedNode(String labelTekst, javafx.scene.Node polje) {
        VBox red = new VBox(4);
        Label lbl = new Label(labelTekst);
        lbl.getStyleClass().add("form-label");
        red.getChildren().addAll(lbl, polje);
        return red;
    }

    private TextField kreirajPolje(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setMaxWidth(Double.MAX_VALUE);
        return tf;
    }

    private void prikaziGresku(String poruka) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greška");
        alert.setContentText(poruka);
        alert.showAndWait();
    }
}