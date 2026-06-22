package autoservis.servis.util;

import javafx.scene.image.Image;
import javafx.stage.Stage;

public class AppIkona {

    private static final Image IKONA;

    static {
        Image img = null;
        try {
            img = new Image(AppIkona.class.getResourceAsStream("/maintenance.png"));
        } catch (Exception ignored) {}
        IKONA = img;
    }

    public static void postavi(Stage stage) {
        if (IKONA != null) stage.getIcons().add(IKONA);
    }
}
