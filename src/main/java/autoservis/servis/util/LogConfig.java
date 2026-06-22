package autoservis.servis.util;

import java.io.File;
import java.io.IOException;
import java.util.logging.*;

public class LogConfig {

    public static void initialize() {
        try {
            String appData = System.getenv("APPDATA");
            String appDir  = (appData != null ? appData : System.getProperty("user.home"))
                             + File.separator + "AutoServis";

            File logsDir = new File(appDir + File.separator + "logs");
            logsDir.mkdirs();

            String pattern = logsDir.getAbsolutePath() + File.separator + "autoservis%g.log";

            FileHandler fileHandler = new FileHandler(pattern, 2 * 1024 * 1024, 3, true);
            fileHandler.setFormatter(new SimpleFormatter() {
                private static final String FORMAT = "[%1$tF %1$tT] [%2$s] %3$s: %4$s%n";

                @Override
                public synchronized String format(LogRecord lr) {
                    return String.format(FORMAT,
                        new java.util.Date(lr.getMillis()),
                        lr.getLevel().getName(),
                        lr.getLoggerName().replaceAll("autoservis\\.servis\\.", ""),
                        formatMessage(lr)
                            + (lr.getThrown() != null ? " | " + lr.getThrown() : "")
                    );
                }
            });
            fileHandler.setLevel(Level.ALL);

            Logger rootLogger = Logger.getLogger("");
            rootLogger.setLevel(Level.INFO);
            for (Handler h : rootLogger.getHandlers().clone())
                rootLogger.removeHandler(h);
            rootLogger.addHandler(fileHandler);

        } catch (IOException e) {
            Logger.getLogger(LogConfig.class.getName())
                  .warning("Log fajl nije mogao biti kreiran: " + e.getMessage());
        }
    }
}
