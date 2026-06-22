package autoservis.servis.controller;

import autoservis.servis.dao.PodesavanjaDao;
import autoservis.servis.model.Podesavanja;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.sql.SQLException;

public class PodesavanjaController {

    private BorderPane view;
    private PodesavanjaDao podesavanjaDao;

    private TextField tfNazivFirme;
    private TextField tfAdresa;
    private TextField tfTelefon;
    private TextField tfEmail;
    private TextField tfPib;
    private TextField tfMaticniBroj;
    private TextField tfZiroRacun;
    private TextField tfLogoPutanja;
    private TextField tfOdgovornoLice;

    private CheckBox cbPdvObveznik;
    private TextField tfPdvStopa;
    private VBox pdvStopaRow;
    private TextField tfDefaultRokPlacanja;

    private TextField tfPocetniBrojNaloga;
    private TextField tfPocetniBrojFakture;
    private TextField tfPocetniBrojPredracuna;

    private TextField tfDriverPutanja;
    private TextField tfGmailAdresa;
    private PasswordField pfGmailAppPassword;
    private TextField tfEmailNaslovPredracun;
    private TextField tfEmailNaslovFaktura;
    private TextArea taEmailZaglavlje;
    private TextArea taEmailTekstPredracun;
    private TextArea taEmailTekstFaktura;
    private TextArea taEmailFooter;

    public PodesavanjaController() {
        this.podesavanjaDao = new PodesavanjaDao();
        this.view = new BorderPane();
        kreirajUI();
        ucitajPodesavanja();
    }

    public BorderPane getView() {
        return view;
    }

    private void kreirajUI() {
        HBox topbar = new HBox(10);
        topbar.getStyleClass().add("topbar");
        topbar.setAlignment(Pos.CENTER_LEFT);
        Label breadcrumb = new Label("Početna / ");
        breadcrumb.getStyleClass().add("topbar-title");
        Label bold = new Label("Podešavanja");
        bold.getStyleClass().add("topbar-title-bold");
        topbar.getChildren().addAll(breadcrumb, bold);

        VBox content = new VBox(20);
        content.getStyleClass().add("content-area");

        // Sekcija 1: Podaci o firmi
        VBox forma1 = new VBox(12);
        forma1.getStyleClass().add("form-card");
        forma1.setPadding(new Insets(20));
        forma1.setMaxWidth(620);

        Label lblNaslov1 = new Label("Podaci o firmi");
        lblNaslov1.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #0a1628;");
        Label lblOpis1 = new Label("Koriste se u zaglavlju radnih naloga, predračuna i priloga uz račun.");
        lblOpis1.setStyle("-fx-font-size: 12px; -fx-text-fill: #8a9ab5;");

        tfNazivFirme = kreirajPolje("Naziv firme");
        tfAdresa = kreirajPolje("Adresa");
        tfTelefon = kreirajPolje("Telefon");
        tfEmail = kreirajPolje("Email");
        tfPib = kreirajPolje("PIB");
        tfMaticniBroj = kreirajPolje("Matični broj");
        tfZiroRacun = kreirajPolje("Žiro račun");
        tfOdgovornoLice = kreirajPolje("Ime i prezime odgovornog lica");

        tfLogoPutanja = new TextField();
        tfLogoPutanja.setPromptText("Putanja do logo fajla...");
        tfLogoPutanja.getStyleClass().add("form-field");
        tfLogoPutanja.setMaxWidth(Double.MAX_VALUE);

        Button btnOdaberiLogo = new Button("Odaberi...");
        btnOdaberiLogo.getStyleClass().add("btn-secondary");
        btnOdaberiLogo.setOnAction(e -> {
            FileChooser fc = new FileChooser();
            fc.setTitle("Odaberi logo");
            fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Slike", "*.png", "*.jpg", "*.jpeg"));
            java.io.File fajl = fc.showOpenDialog(null);
            if (fajl != null) tfLogoPutanja.setText(fajl.getAbsolutePath());
        });

        HBox logoBox = new HBox(8, tfLogoPutanja, btnOdaberiLogo);
        HBox.setHgrow(tfLogoPutanja, Priority.ALWAYS);

        GridPane grid1 = new GridPane();
        grid1.setHgap(12);
        grid1.setVgap(10);
        grid1.getColumnConstraints().addAll(kolona(50), kolona(50));
        grid1.add(kreirajRedTF("Naziv firme *", tfNazivFirme), 0, 0, 2, 1);
        grid1.add(kreirajRedTF("Adresa", tfAdresa), 0, 1, 2, 1);
        grid1.add(kreirajRedTF("Telefon", tfTelefon), 0, 2);
        grid1.add(kreirajRedTF("Email", tfEmail), 1, 2);
        grid1.add(kreirajRedTF("PIB", tfPib), 0, 3);
        grid1.add(kreirajRedTF("Matični broj", tfMaticniBroj), 1, 3);
        grid1.add(kreirajRedTF("Žiro račun", tfZiroRacun), 0, 4, 2, 1);
        grid1.add(kreirajRedTF("Odgovorno lice", tfOdgovornoLice), 0, 5, 2, 1);
        grid1.add(kreirajRedHBox("Logo firme", logoBox), 0, 6, 2, 1);

        forma1.getChildren().addAll(lblNaslov1, lblOpis1, new Separator(), grid1);

        // Sekcija 2: PDV
        VBox forma2 = new VBox(12);
        forma2.getStyleClass().add("form-card");
        forma2.setPadding(new Insets(20));
        forma2.setMaxWidth(620);

        Label lblNaslov2 = new Label("PDV i finansijsko podešavanje");
        lblNaslov2.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #0a1628;");

        cbPdvObveznik = new CheckBox("PDV obveznik");
        cbPdvObveznik.setStyle("-fx-font-size: 13px;");

        tfPdvStopa = kreirajPolje("npr. 20");
        pdvStopaRow = kreirajRedTF("PDV stopa (%)", tfPdvStopa);
        pdvStopaRow.setVisible(false);
        pdvStopaRow.setManaged(false);

        cbPdvObveznik.selectedProperty().addListener((obs, old, novo) -> {
            pdvStopaRow.setVisible(novo);
            pdvStopaRow.setManaged(novo);
        });

        tfDefaultRokPlacanja = kreirajPolje("npr. 15");

        GridPane grid2 = new GridPane();
        grid2.setHgap(12);
        grid2.setVgap(10);
        grid2.getColumnConstraints().addAll(kolona(50), kolona(50));
        grid2.add(kreirajRedTF("Podrazumevani rok plaćanja (dana)", tfDefaultRokPlacanja), 0, 0);
        grid2.add(pdvStopaRow, 1, 0);

        forma2.getChildren().addAll(lblNaslov2, new Separator(), cbPdvObveznik, grid2);

        // Sekcija: Numeracija dokumenata
        VBox formaNumeracija = new VBox(12);
        formaNumeracija.getStyleClass().add("form-card");
        formaNumeracija.setPadding(new Insets(20));
        formaNumeracija.setMaxWidth(620);

        Label lblNaslovNum = new Label("Numeracija dokumenata");
        lblNaslovNum.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #0a1628;");
        Label lblOpisNum = new Label("Početni broj koji će se koristiti kada nema prethodnih dokumenata u tekućoj godini.");
        lblOpisNum.setStyle("-fx-font-size: 12px; -fx-text-fill: #8a9ab5;");
        lblOpisNum.setWrapText(true);
        lblOpisNum.setMaxWidth(580);

        tfPocetniBrojNaloga = kreirajPolje("npr. 1");
        tfPocetniBrojFakture = kreirajPolje("npr. 1");
        tfPocetniBrojPredracuna = kreirajPolje("npr. 1");

        GridPane gridNum = new GridPane();
        gridNum.setHgap(12);
        gridNum.setVgap(10);
        gridNum.getColumnConstraints().addAll(kolona(33), kolona(33), kolona(34));
        gridNum.add(kreirajRedTF("Početni br. radnog naloga", tfPocetniBrojNaloga), 0, 0);
        gridNum.add(kreirajRedTF("Početni br. priloga uz račun", tfPocetniBrojFakture), 1, 0);
        gridNum.add(kreirajRedTF("Početni br. predračuna", tfPocetniBrojPredracuna), 2, 0);

        formaNumeracija.getChildren().addAll(lblNaslovNum, lblOpisNum, new Separator(), gridNum);

        // Sekcija 3: Backup na drajv
        VBox forma3backup = new VBox(12);
        forma3backup.getStyleClass().add("form-card");
        forma3backup.setPadding(new Insets(20));
        forma3backup.setMaxWidth(620);

        Label lblNaslov3b = new Label("Automatski backup na drajv");
        lblNaslov3b.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #0a1628;");
        Label lblOpis3b = new Label("Baza se automatski kopira na odabranu lokaciju svakih sat vremena. Možete koristiti USB, mrežni disk ili folder koji Google Drive/Dropbox sinhronizuje.");
        lblOpis3b.setStyle("-fx-font-size: 12px; -fx-text-fill: #8a9ab5;");
        lblOpis3b.setWrapText(true);
        lblOpis3b.setMaxWidth(580);

        tfDriverPutanja = new TextField();
        tfDriverPutanja.setPromptText("Putanja do foldera za backup...");
        tfDriverPutanja.getStyleClass().add("form-field");
        tfDriverPutanja.setMaxWidth(Double.MAX_VALUE);

        Button btnOdaberiDriver = new Button("Odaberi folder...");
        btnOdaberiDriver.getStyleClass().add("btn-secondary");
        btnOdaberiDriver.setOnAction(e -> {
            DirectoryChooser dc = new DirectoryChooser();
            dc.setTitle("Odaberi folder za backup baze");
            java.io.File folder = dc.showDialog(null);
            if (folder != null) tfDriverPutanja.setText(folder.getAbsolutePath());
        });

        HBox driverBox = new HBox(8, tfDriverPutanja, btnOdaberiDriver);
        HBox.setHgrow(tfDriverPutanja, Priority.ALWAYS);

        forma3backup.getChildren().addAll(lblNaslov3b, lblOpis3b, new Separator(),
                kreirajRedHBox("Folder za backup (svakih sat vremena)", driverBox));

        // Sekcija 4: Email
        VBox forma3 = new VBox(12);
        forma3.getStyleClass().add("form-card");
        forma3.setPadding(new Insets(20));
        forma3.setMaxWidth(620);

        Label lblNaslov3 = new Label("Email podešavanja (Gmail)");
        lblNaslov3.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #0a1628;");
        Label lblOpis3 = new Label("Koristite Gmail App Password, ne pravu lozinku. Uključite 2FA i generišite App Password u Google podešavanjima.");
        lblOpis3.setStyle("-fx-font-size: 12px; -fx-text-fill: #8a9ab5;");
        lblOpis3.setWrapText(true);
        lblOpis3.setMaxWidth(580);

        tfGmailAdresa = kreirajPolje("email@gmail.com");
        pfGmailAppPassword = new PasswordField();
        pfGmailAppPassword.setPromptText("App Password (16 znakova)");
        pfGmailAppPassword.getStyleClass().add("form-field");
        pfGmailAppPassword.setMaxWidth(Double.MAX_VALUE);

        tfEmailNaslovPredracun = kreirajPolje("Naslov email-a uz predračun...");
        tfEmailNaslovFaktura = kreirajPolje("Naslov email-a uz prilog uz račun...");

        taEmailZaglavlje = new TextArea();
        taEmailZaglavlje.setPromptText("Zaglavlje (prikazuje se pre tela poruke, za oba tipa)...");
        taEmailZaglavlje.getStyleClass().add("form-textarea");
        taEmailZaglavlje.setPrefRowCount(2);
        taEmailZaglavlje.setWrapText(true);

        taEmailTekstPredracun = new TextArea();
        taEmailTekstPredracun.setPromptText("Telo email poruke koja se šalje uz predračun...");
        taEmailTekstPredracun.getStyleClass().add("form-textarea");
        taEmailTekstPredracun.setPrefRowCount(3);
        taEmailTekstPredracun.setWrapText(true);

        taEmailTekstFaktura = new TextArea();
        taEmailTekstFaktura.setPromptText("Telo email poruke koja se šalje uz prilog uz račun...");
        taEmailTekstFaktura.getStyleClass().add("form-textarea");
        taEmailTekstFaktura.setPrefRowCount(3);
        taEmailTekstFaktura.setWrapText(true);

        taEmailFooter = new TextArea();
        taEmailFooter.setPromptText("Footer (prikazuje se posle tela poruke, za oba tipa)...");
        taEmailFooter.getStyleClass().add("form-textarea");
        taEmailFooter.setPrefRowCount(2);
        taEmailFooter.setWrapText(true);

        GridPane grid3 = new GridPane();
        grid3.setHgap(12);
        grid3.setVgap(10);
        grid3.getColumnConstraints().addAll(kolona(50), kolona(50));
        grid3.add(kreirajRedTF("Gmail adresa", tfGmailAdresa), 0, 0);
        grid3.add(kreirajRedPF("Gmail App Password", pfGmailAppPassword), 1, 0);
        grid3.add(kreirajRedTA("Zaglavlje (zajedničko)", taEmailZaglavlje), 0, 1, 2, 1);
        grid3.add(kreirajRedTF("Naslov email-a uz predračun", tfEmailNaslovPredracun), 0, 2);
        grid3.add(kreirajRedTF("Naslov email-a uz prilog uz račun", tfEmailNaslovFaktura), 1, 2);
        grid3.add(kreirajRedTA("Telo email-a uz predračun", taEmailTekstPredracun), 0, 3, 2, 1);
        grid3.add(kreirajRedTA("Telo email-a uz prilog uz račun", taEmailTekstFaktura), 0, 4, 2, 1);
        grid3.add(kreirajRedTA("Footer (zajednički)", taEmailFooter), 0, 5, 2, 1);

        forma3.getChildren().addAll(lblNaslov3, lblOpis3, new Separator(), grid3);

        Button btnSacuvaj = new Button("Sačuvaj podešavanja");
        btnSacuvaj.getStyleClass().add("btn-primary");
        btnSacuvaj.setOnAction(e -> sacuvaj());
        HBox dugmad = new HBox(btnSacuvaj);
        dugmad.setAlignment(Pos.CENTER_RIGHT);
        dugmad.setMaxWidth(620);

        content.getChildren().addAll(forma1, forma2, formaNumeracija, forma3backup, forma3, dugmad);

        view.setTop(topbar);
        view.setCenter(new ScrollPane(content) {{
            setFitToWidth(true);
            setStyle("-fx-background-color: transparent;");
        }});
    }

    private void ucitajPodesavanja() {
        try {
            Podesavanja p = podesavanjaDao.vratiPodesavanja();
            tfNazivFirme.setText(p.getNazivFirme() != null ? p.getNazivFirme() : "");
            tfAdresa.setText(p.getAdresa() != null ? p.getAdresa() : "");
            tfTelefon.setText(p.getTelefon() != null ? p.getTelefon() : "");
            tfEmail.setText(p.getEmail() != null ? p.getEmail() : "");
            tfPib.setText(p.getPib() != null ? p.getPib() : "");
            tfMaticniBroj.setText(p.getMaticniBroj() != null ? p.getMaticniBroj() : "");
            tfZiroRacun.setText(p.getZiroRacun() != null ? p.getZiroRacun() : "");
            tfLogoPutanja.setText(p.getLogoPutanja() != null ? p.getLogoPutanja() : "");
            tfOdgovornoLice.setText(p.getOdgovornoLice() != null ? p.getOdgovornoLice() : "");
            cbPdvObveznik.setSelected(p.isPdvObveznik());
            tfPdvStopa.setText(String.valueOf((int) p.getPdvStopa()));
            tfDefaultRokPlacanja.setText(String.valueOf(p.getDefaultRokPlacanja()));
            tfGmailAdresa.setText(p.getGmailAdresa() != null ? p.getGmailAdresa() : "");
            pfGmailAppPassword.setText(p.getGmailAppPassword() != null ? p.getGmailAppPassword() : "");
            taEmailTekstPredracun.setText(p.getEmailTekstPredracun() != null ? p.getEmailTekstPredracun() : "");
            taEmailTekstFaktura.setText(p.getEmailTekstFaktura() != null ? p.getEmailTekstFaktura() : "");
            tfEmailNaslovPredracun.setText(p.getEmailNaslovPredracun() != null ? p.getEmailNaslovPredracun() : "");
            tfEmailNaslovFaktura.setText(p.getEmailNaslovFaktura() != null ? p.getEmailNaslovFaktura() : "");
            taEmailZaglavlje.setText(p.getEmailZaglavlje() != null ? p.getEmailZaglavlje() : "");
            taEmailFooter.setText(p.getEmailFooter() != null ? p.getEmailFooter() : "");
            tfDriverPutanja.setText(p.getDriverPutanja() != null ? p.getDriverPutanja() : "");
            tfPocetniBrojNaloga.setText(String.valueOf(p.getPocetniBrojNaloga()));
            tfPocetniBrojFakture.setText(String.valueOf(p.getPocetniBrojFakture()));
            tfPocetniBrojPredracuna.setText(String.valueOf(p.getPocetniBrojPredracuna()));
        } catch (SQLException e) {
            prikaziGresku("Greška pri učitavanju podešavanja: " + e.getMessage());
        }
    }

    private void sacuvaj() {
        if (tfNazivFirme.getText().isBlank()) {
            prikaziGresku("Naziv firme je obavezan.");
            return;
        }

        double pdvStopa = 20.0;
        int rokPlacanja = 15;
        int pocetniBrojNaloga = 1, pocetniBrojFakture = 1, pocetniBrojPredracuna = 1;
        try {
            if (!tfPdvStopa.getText().isBlank())
                pdvStopa = Double.parseDouble(tfPdvStopa.getText().replace(',', '.'));
        } catch (NumberFormatException e) {
            prikaziGresku("PDV stopa mora biti broj."); return;
        }
        try {
            if (!tfDefaultRokPlacanja.getText().isBlank())
                rokPlacanja = Integer.parseInt(tfDefaultRokPlacanja.getText().trim());
        } catch (NumberFormatException e) {
            prikaziGresku("Rok plaćanja mora biti ceo broj."); return;
        }
        try {
            if (!tfPocetniBrojNaloga.getText().isBlank())
                pocetniBrojNaloga = Integer.parseInt(tfPocetniBrojNaloga.getText().trim());
        } catch (NumberFormatException e) {
            prikaziGresku("Početni broj radnog naloga mora biti ceo broj."); return;
        }
        try {
            if (!tfPocetniBrojFakture.getText().isBlank())
                pocetniBrojFakture = Integer.parseInt(tfPocetniBrojFakture.getText().trim());
        } catch (NumberFormatException e) {
            prikaziGresku("Početni broj fakture mora biti ceo broj."); return;
        }
        try {
            if (!tfPocetniBrojPredracuna.getText().isBlank())
                pocetniBrojPredracuna = Integer.parseInt(tfPocetniBrojPredracuna.getText().trim());
        } catch (NumberFormatException e) {
            prikaziGresku("Početni broj predračuna mora biti ceo broj."); return;
        }

        Podesavanja p = new Podesavanja();
        p.setNazivFirme(tfNazivFirme.getText().trim());
        p.setAdresa(tfAdresa.getText().trim());
        p.setTelefon(tfTelefon.getText().trim());
        p.setEmail(tfEmail.getText().trim());
        p.setPib(tfPib.getText().trim());
        p.setMaticniBroj(tfMaticniBroj.getText().trim());
        p.setZiroRacun(tfZiroRacun.getText().trim());
        p.setLogoPutanja(tfLogoPutanja.getText().trim());
        p.setOdgovornoLice(tfOdgovornoLice.getText().trim().isEmpty() ? null : tfOdgovornoLice.getText().trim());
        p.setPdvObveznik(cbPdvObveznik.isSelected());
        p.setPdvStopa(pdvStopa);
        p.setDefaultRokPlacanja(rokPlacanja);
        p.setGmailAdresa(tfGmailAdresa.getText().trim());
        p.setGmailAppPassword(pfGmailAppPassword.getText());
        p.setEmailTekstPredracun(taEmailTekstPredracun.getText());
        p.setEmailTekstFaktura(taEmailTekstFaktura.getText());
        p.setEmailNaslovPredracun(tfEmailNaslovPredracun.getText().trim());
        p.setEmailNaslovFaktura(tfEmailNaslovFaktura.getText().trim());
        p.setEmailZaglavlje(taEmailZaglavlje.getText());
        p.setEmailFooter(taEmailFooter.getText());
        p.setDriverPutanja(tfDriverPutanja.getText().trim());
        p.setPocetniBrojNaloga(pocetniBrojNaloga);
        p.setPocetniBrojFakture(pocetniBrojFakture);
        p.setPocetniBrojPredracuna(pocetniBrojPredracuna);

        try {
            podesavanjaDao.sacuvaj(p);
            Alert info = new Alert(Alert.AlertType.INFORMATION);
            info.setTitle("Uspešno");
            info.setHeaderText(null);
            info.setContentText("Podešavanja su uspešno sačuvana!");
            info.showAndWait();
        } catch (SQLException e) {
            prikaziGresku("Greška pri čuvanju: " + e.getMessage());
        }
    }

    private TextField kreirajPolje(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.getStyleClass().add("form-field");
        tf.setMaxWidth(Double.MAX_VALUE);
        return tf;
    }

    private VBox kreirajRedTF(String labelTekst, TextField polje) {
        VBox red = new VBox(4);
        Label lbl = new Label(labelTekst);
        lbl.getStyleClass().add("form-label");
        red.getChildren().addAll(lbl, polje);
        return red;
    }

    private VBox kreirajRedPF(String labelTekst, PasswordField polje) {
        VBox red = new VBox(4);
        Label lbl = new Label(labelTekst);
        lbl.getStyleClass().add("form-label");
        red.getChildren().addAll(lbl, polje);
        return red;
    }

    private VBox kreirajRedTA(String labelTekst, TextArea polje) {
        VBox red = new VBox(4);
        Label lbl = new Label(labelTekst);
        lbl.getStyleClass().add("form-label");
        red.getChildren().addAll(lbl, polje);
        return red;
    }

    private VBox kreirajRedHBox(String labelTekst, HBox polje) {
        VBox red = new VBox(4);
        Label lbl = new Label(labelTekst);
        lbl.getStyleClass().add("form-label");
        red.getChildren().addAll(lbl, polje);
        return red;
    }

    private ColumnConstraints kolona(double procenat) {
        ColumnConstraints cc = new ColumnConstraints();
        cc.setPercentWidth(procenat);
        return cc;
    }

    private void prikaziGresku(String poruka) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Greška");
        alert.setContentText(poruka);
        alert.showAndWait();
    }
}
