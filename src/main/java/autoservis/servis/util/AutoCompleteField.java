package autoservis.servis.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;

import java.util.Comparator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class AutoCompleteField<T> {

    private final TextField textField;
    private final Popup popup;
    private final ListView<T> listView;
    private final ObservableList<T> sviElementi;
    private final Function<T, String> prikazivac;
    private Function<T, String> filterFunkcija;
    private BiFunction<T, String, Integer> priorityFunkcija;
    private Consumer<T> onOdabrano;
    private T odabraniElement;

    public AutoCompleteField(List<T> elementi, Function<T, String> prikazivac) {
        this.sviElementi = FXCollections.observableArrayList(elementi);
        this.prikazivac = prikazivac;

        textField = new TextField();
        textField.getStyleClass().add("form-field");
        textField.setMaxWidth(Double.MAX_VALUE);

        listView = new ListView<>();
        listView.setItems(FXCollections.observableArrayList(elementi));
        listView.setPrefHeight(200);
        listView.setMaxHeight(200);
        listView.setPrefWidth(350);

        // Kako se prikazuju stavke u listi
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(T item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(prikazivac.apply(item));
                }
            }
        });

        VBox popupSadrzaj = new VBox(listView);
        popupSadrzaj.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: #d0d9e6;" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 3);"
        );
        popupSadrzaj.setPadding(new Insets(4));

        popup = new Popup();
        popup.getContent().add(popupSadrzaj);
        popup.setAutoHide(true);

        // Listener za pretragu
        textField.textProperty().addListener((obs, stari, novi) -> {
            if (odabraniElement != null &&
                    prikazivac.apply(odabraniElement).equals(novi)) {
                return;
            }
            odabraniElement = null;

            if (novi == null || novi.isBlank()) {
                listView.setItems(FXCollections.observableArrayList(sviElementi));
            } else {
                String filter = novi.toLowerCase();
                Function<T, String> fn = filterFunkcija != null ? filterFunkcija : prikazivac;
                ObservableList<T> filtrirani = FXCollections.observableArrayList();
                for (T item : sviElementi) {
                    if (fn.apply(item).toLowerCase().contains(filter)) {
                        filtrirani.add(item);
                    }
                }
                if (priorityFunkcija != null) {
                    filtrirani.sort(Comparator.comparingInt(item -> priorityFunkcija.apply(item, filter)));
                }
                listView.setItems(filtrirani);
            }

            if (!listView.getItems().isEmpty()) {
                prikaziPopup();
            } else {
                popup.hide();
            }
        });

        // Odabir iz liste
        listView.setOnMouseClicked(e -> {
            T selected = listView.getSelectionModel().getSelectedItem();
            if (selected != null) {
                odaberi(selected);
            }
        });

        // Navigacija tastaturom
        textField.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case DOWN -> {
                    if (!popup.isShowing()) prikaziPopup();
                    listView.requestFocus();
                    listView.getSelectionModel().selectFirst();
                }
                case ESCAPE -> popup.hide();
                case ENTER -> {
                    if (!listView.getItems().isEmpty()) {
                        T selected = listView.getSelectionModel().getSelectedItem();
                        if (selected == null) selected = listView.getItems().get(0);
                        odaberi(selected);
                    }
                }
                default -> {}
            }
        });

        listView.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ENTER -> {
                    T selected = listView.getSelectionModel().getSelectedItem();
                    if (selected != null) odaberi(selected);
                }
                case ESCAPE -> {
                    popup.hide();
                    textField.requestFocus();
                }
                default -> {}
            }
        });

        // Sakrij popup kad izgubi fokus
        textField.focusedProperty().addListener((obs, bio, jeste) -> {
            if (!jeste) {
                javafx.application.Platform.runLater(() -> {
                    if (!listView.isFocused()) {
                        popup.hide();
                    }
                });
            }
        });
    }

    private void prikaziPopup() {
        if (textField.getScene() == null) return;
        javafx.geometry.Bounds bounds = textField.localToScreen(textField.getBoundsInLocal());
        if (bounds == null) return;
        listView.setPrefWidth(bounds.getWidth());
        popup.show(textField, bounds.getMinX(), bounds.getMaxY() + 2);
    }

    private void odaberi(T element) {
        odabraniElement = element;
        textField.setText(prikazivac.apply(element));
        popup.hide();
        textField.requestFocus();
        if (onOdabrano != null) {
            onOdabrano.accept(element);
        }
    }

    public TextField getTextField() {
        return textField;
    }

    public T getOdabraniElement() {
        return odabraniElement;
    }

    public void setOnOdabrano(Consumer<T> onOdabrano) {
        this.onOdabrano = onOdabrano;
    }

    public void setFilterFunkcija(Function<T, String> filterFunkcija) {
        this.filterFunkcija = filterFunkcija;
    }

    public void setPriorityFunkcija(BiFunction<T, String, Integer> priorityFunkcija) {
        this.priorityFunkcija = priorityFunkcija;
    }

    public void setPromptText(String text) {
        textField.setPromptText(text);
    }

    public void setElementi(List<T> elementi) {
        sviElementi.setAll(elementi);
        listView.setItems(FXCollections.observableArrayList(elementi));
        odabraniElement = null;
        textField.clear();
    }

    public void postavi(T element) {
        if (element != null) {
            odabraniElement = element;
            textField.setText(prikazivac.apply(element));
        }
    }
}