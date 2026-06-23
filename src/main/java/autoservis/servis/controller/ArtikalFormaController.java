package autoservis.servis.controller;

import autoservis.servis.dao.ArtikalDao;
import autoservis.servis.util.AppIkona;
import autoservis.servis.model.Artikal;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ArtikalFormaController {

    private Artikal artikal;
    private Runnable onSave;
    private ArtikalDao artikalDao;
    private Stage stage;

    private static final javafx.collections.ObservableList<String> JEDINICE =
        javafx.collections.FXCollections.observableArrayList(
            "kom", "l", "ml", "kg", "g", "h", "m", "m²", "set", "par", "paket"
        );

    private static final javafx.collections.ObservableList<String> VRSTE =
        javafx.collections.FXCollections.observableArrayList("Artikal", "Usluga");

    private ComboBox<String> cbVrsta;
    private TextField tfNaziv;
    private TextField tfSifra;
    private ComboBox<String> cbJedinicaMere;
    private TextField tfKolicina;
    private TextField tfNabavnaCena;
    private TextField tfProdajnaCena;
    private TextField tfMinimalnaKolicina;

    private VBox redKolicina;
    private VBox redNabavna;
    private VBox redMinimalna;

    public ArtikalFormaController(Artikal artikal, Runnable onSave) {
        this.artikal = artikal;
        this.onSave = onSave;
        this.artikalDao = new ArtikalDao();
    }

    public void show() {
        stage = new Stage();
        AppIkona.postavi(stage);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle(artikal == null ? "Novi artikal" : "Izmena artikla");
        stage.setMinWidth(440);

        VBox root = new VBox(12);
        root.getStyleClass().add("form-card");
        root.setPadding(new Insets(20));

        cbVrsta = new ComboBox<>(VRSTE);
        cbVrsta.setValue("Artikal");
        cbVrsta.getStyleClass().add("search-field");
        cbVrsta.setMaxWidth(Double.MAX_VALUE);

        tfNaziv = kreirajPolje("npr. Motorno ulje 5W40");
        tfSifra = kreirajPolje("npr. MOT-5W40");
        cbJedinicaMere = new ComboBox<>(JEDINICE);
        cbJedinicaMere.setValue("kom");
        cbJedinicaMere.getStyleClass().add("search-field");
        cbJedinicaMere.setMaxWidth(Double.MAX_VALUE);
        tfKolicina = kreirajPolje("Trenutna količina na stanju");
        tfNabavnaCena = kreirajPolje("0.00");
        tfProdajnaCena = kreirajPolje("0.00");
        tfMinimalnaKolicina = kreirajPolje("Minimalna količina za upozorenje");

        // Cene u jednom redu
        HBox ceneBox = new HBox(12);
        redNabavna = new VBox(4);
        Label lblNabavna = new Label("Nabavna cena");
        lblNabavna.getStyleClass().add("form-label");
        redNabavna.getChildren().addAll(lblNabavna, tfNabavnaCena);

        VBox prodajnaBox = new VBox(4);
        Label lblProdajna = new Label("Prodajna cena");
        lblProdajna.getStyleClass().add("form-label");
        prodajnaBox.getChildren().addAll(lblProdajna, tfProdajnaCena);

        HBox.setHgrow(redNabavna, Priority.ALWAYS);
        HBox.setHgrow(prodajnaBox, Priority.ALWAYS);
        ceneBox.getChildren().addAll(redNabavna, prodajnaBox);

        redKolicina = kreirajRed("Količina na stanju", tfKolicina);
        redMinimalna = kreirajRed("Minimalna količina", tfMinimalnaKolicina);

        cbVrsta.valueProperty().addListener((obs, old, novo) -> azurirajVidljivostPolja(novo));

        VBox forma = new VBox(10);
        forma.getChildren().addAll(
                kreirajRedCB("Vrsta *", cbVrsta),
                kreirajRed("Naziv *", tfNaziv),
                kreirajRed("Šifra *", tfSifra),
                kreirajRedCB("Jedinica mere *", cbJedinicaMere),
                redKolicina,
                ceneBox,
                redMinimalna
        );

        // Dugmad
        Button btnSacuvaj = new Button("Sačuvaj");
        btnSacuvaj.getStyleClass().add("btn-primary");
        btnSacuvaj.setOnAction(e -> sacuvaj());

        Button btnOtkazi = new Button("Otkaži");
        btnOtkazi.getStyleClass().add("btn-secondary");
        btnOtkazi.setOnAction(e -> stage.close());

        HBox dugmad = new HBox(8, btnSacuvaj, btnOtkazi);
        dugmad.setAlignment(Pos.CENTER_RIGHT);

        root.getChildren().addAll(forma, new Separator(), dugmad);

        if (artikal != null) {
            popuniFormu();
        }

        Scene scene = new Scene(root);
        stage.setWidth(460);
        scene.getStylesheets().add(getClass().getResource("/style/style.css").toExternalForm());
        stage.setScene(scene);
        stage.showAndWait();
    }

    private void popuniFormu() {
        if (artikal.getVrsta() != null) cbVrsta.setValue(artikal.getVrsta());
        tfNaziv.setText(artikal.getNaziv() != null ? artikal.getNaziv() : "");
        if (artikal.getJedinicaMere() != null) cbJedinicaMere.setValue(artikal.getJedinicaMere());
        if (artikal.getSifra() != null) tfSifra.setText(String.valueOf(artikal.getSifra()));
        if (artikal.getKolicina() != null) tfKolicina.setText(String.valueOf(artikal.getKolicina()));
        if (artikal.getNabavnaCena() != null) tfNabavnaCena.setText(String.valueOf(artikal.getNabavnaCena()));
        tfProdajnaCena.setText(String.valueOf(artikal.getProdajnaCena()));
        if (artikal.getMinimalnaKolicina() != null) tfMinimalnaKolicina.setText(String.valueOf(artikal.getMinimalnaKolicina()));
        azurirajVidljivostPolja(artikal.getVrsta());
    }

    private void azurirajVidljivostPolja(String vrsta) {
        boolean jeArtikal = !"Usluga".equals(vrsta);
        redKolicina.setVisible(jeArtikal);
        redKolicina.setManaged(jeArtikal);
        redNabavna.setVisible(jeArtikal);
        redNabavna.setManaged(jeArtikal);
        redMinimalna.setVisible(jeArtikal);
        redMinimalna.setManaged(jeArtikal);
    }

    private void sacuvaj() {
        if (!validiraj()) return;

        Artikal a = artikal != null ? artikal : new Artikal();
        a.setVrsta(cbVrsta.getValue());
        a.setNaziv(tfNaziv.getText().trim());
        try {
            a.setSifra(tfSifra.getText().isBlank() ? null : Integer.parseInt(tfSifra.getText().trim()));
        } catch (NumberFormatException e) {
            prikaziGresku("Šifra mora biti broj.");
            return;
        }
        a.setJedinicaMere(cbJedinicaMere.getValue());

        boolean jeUsluga = "Usluga".equals(a.getVrsta());
        if (jeUsluga) {
            a.setKolicina(null);
            a.setNabavnaCena(null);
            a.setMinimalnaKolicina(null);
        } else {
            try {
                a.setKolicina(Double.parseDouble(tfKolicina.getText().trim()));
            } catch (NumberFormatException e) {
                a.setKolicina(0.0);
            }
            try {
                a.setNabavnaCena(Double.parseDouble(tfNabavnaCena.getText().trim()));
            } catch (NumberFormatException e) {
                a.setNabavnaCena(0.0);
            }
            try {
                a.setMinimalnaKolicina(Double.parseDouble(tfMinimalnaKolicina.getText().trim()));
            } catch (NumberFormatException e) {
                a.setMinimalnaKolicina(0.0);
            }
        }
        try {
            a.setProdajnaCena(Double.parseDouble(tfProdajnaCena.getText().trim()));
        } catch (NumberFormatException e) {
            a.setProdajnaCena(0);
        }

        try {
            if (artikal == null) {
                artikalDao.dodaj(a);
            } else {
                artikalDao.izmeni(a);
            }
            onSave.run();
            stage.close();
        } catch (Exception e) {
            prikaziGresku("Greška pri čuvanju: " + e.getMessage());
        }
    }

    private boolean validiraj() {
        if (tfNaziv.getText().isBlank()) {
            prikaziGresku("Naziv artikla je obavezan.");
            return false;
        }
        if (cbJedinicaMere.getValue() == null) {
            prikaziGresku("Jedinica mere je obavezna.");
            return false;
        }
        if (tfSifra.getText().isBlank()) {
            prikaziGresku("Šifra artikla je obavezna.");
            return false;
        }
        try {
            Integer sifra = Integer.parseInt(tfSifra.getText().trim());
            boolean zauzeta = artikal == null
                    ? artikalDao.postojiSifra(sifra)
                    : artikalDao.postojiSifraZaDrugi(sifra, artikal.getId());
            if (zauzeta) {
                prikaziGresku("Šifra " + sifra + " već postoji. Šifra mora biti jedinstvena.");
                return false;
            }
        } catch (NumberFormatException e) {
            prikaziGresku("Šifra mora biti broj.");
            return false;
        } catch (Exception e) {
            prikaziGresku("Greška pri proveri šifre: " + e.getMessage());
            return false;
        }
        if (!tfKolicina.getText().isBlank()) {
            try {
                if (Double.parseDouble(tfKolicina.getText().trim()) < 0) {
                    prikaziGresku("Količina ne može biti negativna.");
                    return false;
                }
            } catch (NumberFormatException e) {
                prikaziGresku("Količina mora biti broj.");
                return false;
            }
        }
        if (!tfNabavnaCena.getText().isBlank()) {
            try {
                if (Double.parseDouble(tfNabavnaCena.getText().trim()) < 0) {
                    prikaziGresku("Nabavna cena ne može biti negativna.");
                    return false;
                }
            } catch (NumberFormatException e) {
                prikaziGresku("Nabavna cena mora biti broj.");
                return false;
            }
        }
        if (!tfProdajnaCena.getText().isBlank()) {
            try {
                if (Double.parseDouble(tfProdajnaCena.getText().trim()) < 0) {
                    prikaziGresku("Prodajna cena ne može biti negativna.");
                    return false;
                }
            } catch (NumberFormatException e) {
                prikaziGresku("Prodajna cena mora biti broj.");
                return false;
            }
        }
        if (!tfMinimalnaKolicina.getText().isBlank()) {
            try {
                if (Double.parseDouble(tfMinimalnaKolicina.getText().trim()) < 0) {
                    prikaziGresku("Minimalna količina ne može biti negativna.");
                    return false;
                }
            } catch (NumberFormatException e) {
                prikaziGresku("Minimalna količina mora biti broj.");
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

    private VBox kreirajRedCB(String labelTekst, ComboBox<String> polje) {
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