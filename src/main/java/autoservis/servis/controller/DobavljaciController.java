package autoservis.servis.controller;

import autoservis.servis.dao.DobavljacDao;
import autoservis.servis.model.Dobavljac;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.sql.SQLException;

public class DobavljaciController {

    private BorderPane view;
    private DobavljacDao dao;

    private TextField tfNaziv;
    private TextField tfAdresa;
    private TextField tfKontakt;
    private TextField tfPib;
    private Button btnSacuvaj;
    private Button btnOtkazi;

    private Dobavljac editovani = null;

    private final ObservableList<Dobavljac> lista = FXCollections.observableArrayList();

    public DobavljaciController() {
        dao = new DobavljacDao();
        view = new BorderPane();
        kreirajUI();
        ucitaj();
    }

    public BorderPane getView() { return view; }

    private void kreirajUI() {
        HBox topbar = new HBox(10);
        topbar.getStyleClass().add("topbar");
        topbar.setAlignment(Pos.CENTER_LEFT);
        Label breadcrumb = new Label("Magacin / ");
        breadcrumb.getStyleClass().add("topbar-title");
        Label bold = new Label("Dobavljači");
        bold.getStyleClass().add("topbar-title-bold");
        topbar.getChildren().addAll(breadcrumb, bold);

        // === LEFT: form ===
        VBox forma = new VBox(10);
        forma.getStyleClass().add("form-card");
        forma.setPadding(new Insets(16));
        forma.setMinWidth(320);
        forma.setMaxWidth(380);

        Label lblForma = new Label("Novi dobavljač");
        lblForma.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0a1628;");

        tfNaziv = new TextField();
        tfNaziv.setPromptText("Naziv firme / ime");
        tfNaziv.getStyleClass().add("form-field");
        tfNaziv.setMaxWidth(Double.MAX_VALUE);

        tfAdresa = new TextField();
        tfAdresa.setPromptText("Adresa (opciono)");
        tfAdresa.getStyleClass().add("form-field");
        tfAdresa.setMaxWidth(Double.MAX_VALUE);

        tfKontakt = new TextField();
        tfKontakt.setPromptText("Telefon / email (opciono)");
        tfKontakt.getStyleClass().add("form-field");
        tfKontakt.setMaxWidth(Double.MAX_VALUE);

        tfPib = new TextField();
        tfPib.setPromptText("PIB (opciono)");
        tfPib.getStyleClass().add("form-field");
        tfPib.setMaxWidth(Double.MAX_VALUE);

        btnSacuvaj = new Button("Dodaj dobavljača");
        btnSacuvaj.getStyleClass().add("btn-primary");
        btnSacuvaj.setMaxWidth(Double.MAX_VALUE);
        btnSacuvaj.setOnAction(e -> sacuvaj());

        btnOtkazi = new Button("Otkaži izmene");
        btnOtkazi.getStyleClass().add("btn-secondary");
        btnOtkazi.setMaxWidth(Double.MAX_VALUE);
        btnOtkazi.setVisible(false);
        btnOtkazi.setOnAction(e -> resetujFormu());

        forma.getChildren().addAll(
                lblForma,
                new Separator(),
                kreirajRed("Naziv *", tfNaziv),
                kreirajRed("Adresa", tfAdresa),
                kreirajRed("Kontakt", tfKontakt),
                kreirajRed("PIB", tfPib),
                btnSacuvaj,
                btnOtkazi
        );

        // === RIGHT: table ===
        Label lblTabela = new Label("Lista dobavljača");
        lblTabela.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0a1628;");

        TableView<Dobavljac> tabela = new TableView<>(lista);
        tabela.getStyleClass().add("table-view");
        tabela.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        VBox.setVgrow(tabela, Priority.ALWAYS);

        TableColumn<Dobavljac, String> colNaziv = new TableColumn<>("Naziv");
        colNaziv.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNaziv()));

        TableColumn<Dobavljac, String> colAdresa = new TableColumn<>("Adresa");
        colAdresa.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getAdresa() != null ? c.getValue().getAdresa() : ""));

        TableColumn<Dobavljac, String> colKontakt = new TableColumn<>("Kontakt");
        colKontakt.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getKontakt() != null ? c.getValue().getKontakt() : ""));
        colKontakt.setPrefWidth(130);
        colKontakt.setMaxWidth(160);

        TableColumn<Dobavljac, String> colPib = new TableColumn<>("PIB");
        colPib.setCellValueFactory(c -> new SimpleStringProperty(
                c.getValue().getPib() != null ? c.getValue().getPib() : ""));
        colPib.setPrefWidth(90);
        colPib.setMaxWidth(110);

        TableColumn<Dobavljac, Void> colIzmeni = new TableColumn<>("");
        colIzmeni.setPrefWidth(70);
        colIzmeni.setMaxWidth(75);
        colIzmeni.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Izmeni");
            {
                btn.getStyleClass().add("btn-secondary");
                btn.setOnAction(e -> popuniFormu(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        TableColumn<Dobavljac, Void> colArhiv = new TableColumn<>("");
        colArhiv.setPrefWidth(80);
        colArhiv.setMaxWidth(85);
        colArhiv.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("Arhiviraj");
            {
                btn.getStyleClass().add("btn-secondary");
                btn.setOnAction(e -> {
                    Dobavljac d = getTableView().getItems().get(getIndex());
                    Alert potvrda = new Alert(Alert.AlertType.CONFIRMATION);
                    potvrda.setTitle("Arhiviranje");
                    potvrda.setHeaderText(null);
                    potvrda.setContentText("Arhivirati dobavljaca \"" + d.getNaziv() + "\"?");
                    potvrda.showAndWait().ifPresent(r -> {
                        if (r == ButtonType.OK) {
                            try {
                                dao.arhiviraj(d.getId());
                                ucitaj();
                            } catch (SQLException ex) {
                                prikaziGresku("Greška pri arhiviranju: " + ex.getMessage());
                            }
                        }
                    });
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        tabela.getColumns().addAll(colNaziv, colAdresa, colKontakt, colPib, colIzmeni, colArhiv);

        VBox desni = new VBox(12, lblTabela, tabela);
        VBox.setVgrow(desni, Priority.ALWAYS);

        HBox content = new HBox(16, forma, desni);
        content.getStyleClass().add("content-area");
        HBox.setHgrow(desni, Priority.ALWAYS);

        view.setTop(topbar);
        view.setCenter(content);
    }

    private void sacuvaj() {
        String naziv = tfNaziv.getText().trim();
        if (naziv.isEmpty()) {
            prikaziGresku("Naziv je obavezan.");
            return;
        }
        try {
            if (editovani == null) {
                Dobavljac d = new Dobavljac();
                d.setNaziv(naziv);
                d.setAdresa(tfAdresa.getText().trim().isEmpty() ? null : tfAdresa.getText().trim());
                d.setKontakt(tfKontakt.getText().trim().isEmpty() ? null : tfKontakt.getText().trim());
                d.setPib(tfPib.getText().trim().isEmpty() ? null : tfPib.getText().trim());
                dao.dodaj(d);
            } else {
                editovani.setNaziv(naziv);
                editovani.setAdresa(tfAdresa.getText().trim().isEmpty() ? null : tfAdresa.getText().trim());
                editovani.setKontakt(tfKontakt.getText().trim().isEmpty() ? null : tfKontakt.getText().trim());
                editovani.setPib(tfPib.getText().trim().isEmpty() ? null : tfPib.getText().trim());
                dao.izmeni(editovani);
            }
            resetujFormu();
            ucitaj();
        } catch (SQLException e) {
            prikaziGresku("Greška pri čuvanju: " + e.getMessage());
        }
    }

    private void popuniFormu(Dobavljac d) {
        editovani = d;
        tfNaziv.setText(d.getNaziv() != null ? d.getNaziv() : "");
        tfAdresa.setText(d.getAdresa() != null ? d.getAdresa() : "");
        tfKontakt.setText(d.getKontakt() != null ? d.getKontakt() : "");
        tfPib.setText(d.getPib() != null ? d.getPib() : "");
        btnSacuvaj.setText("Sačuvaj izmene");
        btnOtkazi.setVisible(true);
    }

    private void resetujFormu() {
        editovani = null;
        tfNaziv.clear();
        tfAdresa.clear();
        tfKontakt.clear();
        tfPib.clear();
        btnSacuvaj.setText("Dodaj dobavljača");
        btnOtkazi.setVisible(false);
    }

    private void ucitaj() {
        try {
            lista.setAll(dao.vratiSve());
        } catch (SQLException e) {
            prikaziGresku("Greška pri učitavanju: " + e.getMessage());
        }
    }

    private VBox kreirajRed(String labelTekst, javafx.scene.Node polje) {
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
