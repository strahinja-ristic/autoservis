package autoservis.servis.controller;

import autoservis.servis.util.LicenseService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

public class ActivationController {

    private final BorderPane view;
    private final Runnable onSuccess;

    public ActivationController(Runnable onSuccess) {
        this.onSuccess = onSuccess;
        this.view = new BorderPane();
        kreirajUI();
    }

    public BorderPane getView() { return view; }

    private void kreirajUI() {
        view.setStyle("-fx-background-color: #1a1640;");

        // ---- Gornji deo: branding ----
        VBox branding = new VBox(12);
        branding.setAlignment(Pos.CENTER);
        branding.setPadding(new Insets(60, 40, 20, 40));

        Label icon = new Label("🔧");
        icon.setStyle("-fx-font-size: 48px;");

        Label title = new Label("Moj Servis");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label subtitle = new Label("Upravljanje auto servisom");
        subtitle.setStyle("-fx-font-size: 13px; -fx-text-fill: #9b9dc8;");

        branding.getChildren().addAll(icon, title, subtitle);

        // ---- Donji deo: bijeli card ----
        VBox card = new VBox(14);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(32, 36, 36, 36));
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 16 16 0 0;"
        );

        Label cardTitle = new Label("Aktivacija licence");
        cardTitle.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: #0a1628;");

        Label cardSub = new Label("Unesite licencni ključ koji ste dobili pri kupovini.");
        cardSub.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
        cardSub.setWrapText(true);

        TextField keyField = new TextField();
        keyField.setPromptText("XXXX-XXXX-XXXX-XXXX");
        keyField.setStyle(
            "-fx-font-size: 14px; -fx-padding: 10 14 10 14;" +
            "-fx-border-color: #d1d5db; -fx-border-radius: 8; -fx-background-radius: 8;" +
            "-fx-background-color: #f9fafb;"
        );
        keyField.setMaxWidth(Double.MAX_VALUE);

        Label statusLabel = new Label("");
        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #dc2626;");
        statusLabel.setWrapText(true);

        Button btnActivate = new Button("Aktiviraj");
        btnActivate.setStyle(
            "-fx-background-color: #534AB7; -fx-text-fill: white;" +
            "-fx-font-size: 13px; -fx-font-weight: bold;" +
            "-fx-padding: 10 28 10 28; -fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );
        btnActivate.setMaxWidth(Double.MAX_VALUE);

        btnActivate.setOnAction(e -> {
            String key = keyField.getText().trim();
            if (key.isBlank()) {
                statusLabel.setText("Unesite licencni ključ.");
                return;
            }
            btnActivate.setDisable(true);
            keyField.setDisable(true);
            statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #6b7280;");
            statusLabel.setText("Provjera...");

            Thread t = new Thread(() -> {
                LicenseService.PingResult result = LicenseService.getInstance().activate(key);
                Platform.runLater(() -> {
                    btnActivate.setDisable(false);
                    keyField.setDisable(false);
                    if (result == null) {
                        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #dc2626;");
                        statusLabel.setText("Greška: nema konekcije sa serverom.");
                    } else if (result.status == LicenseService.LicenseStatus.BLOCKED) {
                        statusLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #dc2626;");
                        statusLabel.setText("Licenca je blokirana. Kontaktirajte podršku.");
                    } else {
                        onSuccess.run();
                    }
                });
            });
            t.setDaemon(true);
            t.start();
        });

        keyField.setOnAction(e -> btnActivate.fire());

        card.getChildren().addAll(cardTitle, cardSub, keyField, btnActivate, statusLabel);

        view.setCenter(branding);
        view.setBottom(card);
    }
}
