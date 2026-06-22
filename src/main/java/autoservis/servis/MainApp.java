package autoservis.servis;

import autoservis.servis.controller.ActivationController;
import autoservis.servis.controller.GlavniController;
import autoservis.servis.dao.PodesavanjaDao;
import autoservis.servis.util.DatabaseManager;
import autoservis.servis.util.FeatureFlagService;
import autoservis.servis.util.LicenseService;
import autoservis.servis.util.LogConfig;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class MainApp extends Application {

    private static final Logger logger = Logger.getLogger(MainApp.class.getName());

    private static LicenseService.PingResult lastLicenseResult;
    private ScheduledExecutorService scheduler;

    public static LicenseService.PingResult getLastLicenseResult() { return lastLicenseResult; }

    @Override
    public void stop() throws Exception {
        if (scheduler != null) scheduler.shutdownNow();
        super.stop();
    }

    @Override
    public void start(Stage stage) throws Exception {
        LogConfig.initialize();
        DatabaseManager.initialize();

        LicenseService.LicenseStatus licenseStatus = LicenseService.getInstance().startupCheck();
        switch (licenseStatus) {
            case NEEDS_ACTIVATION -> { showActivationScreen(stage); return; }
            case BLOCKED          -> { showBlockedScreen(); return; }
            case OFFLINE_EXPIRED  -> { showOfflineExpiredScreen(); return; }
            default               -> showMainApp(stage);
        }
    }

    private void showMainApp(Stage stage) throws Exception {
        GlavniController glavni = new GlavniController(stage);
        Scene scene = new Scene(glavni.getView(), 1100, 680);
        scene.getStylesheets().add(getClass().getResource("/style/style.css").toExternalForm());

        stage.setTitle("Moj Servis");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.setMaximized(true);

        try {
            javafx.scene.image.Image icon = new javafx.scene.image.Image(
                    getClass().getResourceAsStream("/maintenance.png"));
            stage.getIcons().add(icon);
        } catch (Exception ignored) {}

        setupBackupSchedulers();

        String clientKey = DatabaseManager.getSetting("client_key");
        if (clientKey != null) {
            lastLicenseResult = LicenseService.getInstance().getLastPingResult();
            glavni.applyLicenseBanner();
        }

        LicenseService.getInstance().startPeriodicPing();
        stage.show();
    }

    private void showActivationScreen(Stage stage) {
        ActivationController activation = new ActivationController(() -> {
            try {
                showMainApp(stage);
            } catch (Exception e) {
                logger.severe("showMainApp after activation failed: " + e.getMessage());
            }
        });

        Scene scene = new Scene(activation.getView(), 480, 580);
        stage.setTitle("Aktivacija licence — Moj Servis");
        stage.setScene(scene);
        stage.setMinWidth(400);
        stage.setMinHeight(480);
        stage.setMaximized(false);

        try {
            javafx.scene.image.Image icon = new javafx.scene.image.Image(
                    getClass().getResourceAsStream("/maintenance.png"));
            stage.getIcons().add(icon);
        } catch (Exception ignored) {}

        stage.show();
    }

    private void showBlockedScreen() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Licenca blokirana");
        alert.setHeaderText("Pristup onemogućen");
        alert.setContentText("Vaša licenca je blokirana. Kontaktirajte podršku.");
        alert.showAndWait();
        Platform.exit();
    }

    private void showOfflineExpiredScreen() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Licenca istekla");
        alert.setHeaderText("Offline period istekao");
        alert.setContentText(
            "Licenca nije mogla biti potvrđena više od 7 dana. " +
            "Povežite se na internet i pokrenite aplikaciju ponovo.");
        alert.showAndWait();
        Platform.exit();
    }

    private void setupBackupSchedulers() {
        scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "backup-drajv");
            t.setDaemon(true);
            return t;
        });

        scheduler.scheduleAtFixedRate(() -> {
            if (!FeatureFlagService.getInstance().isEnabled("feature_drive")) return;
            try {
                String putanja = new PodesavanjaDao().vratiPodesavanja().getDriverPutanja();
                DatabaseManager.backupNaDrajv(putanja);
            } catch (Exception e) {
                logger.warning("Backup na drajv pao: " + e.getMessage());
            }
        }, 1, 1, TimeUnit.HOURS);

        scheduler.scheduleAtFixedRate(() -> {
            try {
                DatabaseManager.backupLokalni();
            } catch (Exception ignored) {}
        }, 0, 4, TimeUnit.HOURS);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
