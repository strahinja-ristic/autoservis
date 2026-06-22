package autoservis.servis.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class DatabaseManager {

    private static final Logger logger = Logger.getLogger(DatabaseManager.class.getName());

    private static final String APP_DIR;
    private static final String DB_URL;

    static {
        String appData = System.getenv("APPDATA");
        APP_DIR = (appData != null ? appData : System.getProperty("user.home"))
                  + java.io.File.separator + "AutoServis";
        new java.io.File(APP_DIR).mkdirs();
        DB_URL = "jdbc:sqlite:" + APP_DIR + java.io.File.separator + "autoservis.db";
    }

    private static Connection connection;

    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(DB_URL);
            connection.createStatement().execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    public static void initialize() {
        try (Statement stmt = getConnection().createStatement()) {

            // Migracija: preimenuj staru tabelu u ulaz_magacin ako postoji
            try { stmt.execute("ALTER TABLE ulaz_skladiste RENAME TO ulaz_magacin"); } catch (SQLException e) {
                if (!e.getMessage().contains("no such table")) logger.warning("Migracija ulaz_skladiste: " + e.getMessage());
            }

            // Podešavanja firme
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS podesavanja (
                            id INTEGER PRIMARY KEY,
                            naziv_firme TEXT,
                            adresa TEXT,
                            telefon TEXT,
                            email TEXT,
                            pib TEXT,
                            maticni_broj TEXT,
                            žiro_racun TEXT
                        )
                    """);

            // Klijenti
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS klijenti (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            tip TEXT NOT NULL,
                            ime TEXT,
                            prezime TEXT,
                            naziv_firme TEXT,
                            pib TEXT,
                            maticni_broj TEXT,
                            adresa TEXT,
                            telefon TEXT,
                            email TEXT,
                            arhiviran INTEGER DEFAULT 0
                        )
                    """);

            // Vozila
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS vozila (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            klijent_id INTEGER NOT NULL,
                            marka TEXT NOT NULL,
                            model TEXT NOT NULL,
                            godiste INTEGER,
                            registracija TEXT,
                            broj_sasije TEXT,
                            kilometraza INTEGER DEFAULT 0,
                            arhivirano INTEGER DEFAULT 0,
                            FOREIGN KEY (klijent_id) REFERENCES klijenti(id)
                        )
                    """);

            // Radni nalozi
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS radni_nalozi (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            broj_naloga TEXT NOT NULL UNIQUE,
                            vozilo_id INTEGER NOT NULL,
                            klijent_id INTEGER NOT NULL,
                            kilometraza_prijema INTEGER,
                            datum_prijema TEXT,
                            datum_zavrsetka TEXT,
                            opis_kvara TEXT,
                            status TEXT DEFAULT 'Primljeno',
                            ostecenja TEXT,
                            sledeci_servis_km INTEGER,
                            sledeci_servis_datum TEXT,
                            arhiviran INTEGER DEFAULT 0,
                            FOREIGN KEY (vozilo_id) REFERENCES vozila(id),
                            FOREIGN KEY (klijent_id) REFERENCES klijenti(id)
                        )
                    """);

            // Usluge na radnom nalogu
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS usluge (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            radni_nalog_id INTEGER NOT NULL,
                            opis TEXT NOT NULL,
                            FOREIGN KEY (radni_nalog_id) REFERENCES radni_nalozi(id)
                        )
                    """);

            // Artikli u magacinu
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS artikli (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            naziv TEXT NOT NULL,
                            jedinica_mere TEXT,
                            kolicina REAL DEFAULT 0,
                            nabavna_cena REAL DEFAULT 0,
                            prodajna_cena REAL DEFAULT 0,
                            minimalna_kolicina REAL DEFAULT 0,
                            arhiviran INTEGER DEFAULT 0
                        )
                    """);

            // Artikli na radnom nalogu
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS nalog_artikli (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            radni_nalog_id INTEGER NOT NULL,
                            artikal_id INTEGER NOT NULL,
                            kolicina REAL NOT NULL,
                            FOREIGN KEY (radni_nalog_id) REFERENCES radni_nalozi(id),
                            FOREIGN KEY (artikal_id) REFERENCES artikli(id)
                        )
                    """);

            // Dokumenti ulaza robe
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS ulaz_dokumenti (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            broj TEXT NOT NULL UNIQUE,
                            datum TEXT NOT NULL,
                            napomena TEXT
                        )
                    """);

            // Ulaz u magacin (istorija)
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS ulaz_magacin (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            artikal_id INTEGER NOT NULL,
                            kolicina REAL NOT NULL,
                            datum TEXT NOT NULL,
                            napomena TEXT,
                            dokument_id INTEGER,
                            FOREIGN KEY (artikal_id) REFERENCES artikli(id),
                            FOREIGN KEY (dokument_id) REFERENCES ulaz_dokumenti(id)
                        )
                    """);

            // Sabloni usluga
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS sabloni_usluga (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            naziv TEXT NOT NULL,
                            cena REAL DEFAULT 0,
                            arhiviran INTEGER DEFAULT 0
                        )
                    """);

            // Predracuni
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS predracuni (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            broj_predracuna TEXT NOT NULL UNIQUE,
                            klijent_id INTEGER NOT NULL,
                            vozilo_id INTEGER,
                            radni_nalog_id INTEGER,
                            datum_kreiranja TEXT NOT NULL,
                            datum_vazenja TEXT,
                            status TEXT DEFAULT 'Kreiran',
                            nacin_placanja TEXT DEFAULT 'Gotovina',
                            rok_placanja TEXT,
                            mesto_izdavanja TEXT,
                            napomena TEXT,
                            popust_procenat REAL DEFAULT 0,
                            arhiviran INTEGER DEFAULT 0,
                            FOREIGN KEY (klijent_id) REFERENCES klijenti(id),
                            FOREIGN KEY (vozilo_id) REFERENCES vozila(id),
                            FOREIGN KEY (radni_nalog_id) REFERENCES radni_nalozi(id)
                        )
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS predracun_stavke (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            predracun_id INTEGER NOT NULL,
                            tip TEXT NOT NULL,
                            naziv TEXT NOT NULL,
                            kolicina REAL NOT NULL DEFAULT 1,
                            jedinica_mere TEXT DEFAULT 'kom',
                            cena_bez_pdv REAL NOT NULL DEFAULT 0,
                            pdv_stopa REAL DEFAULT 0,
                            popust_procenat REAL DEFAULT 0,
                            redni_broj INTEGER DEFAULT 0,
                            FOREIGN KEY (predracun_id) REFERENCES predracuni(id)
                        )
                    """);

            // Fakture
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS fakture (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            broj_fakture TEXT NOT NULL UNIQUE,
                            klijent_id INTEGER NOT NULL,
                            vozilo_id INTEGER,
                            radni_nalog_id INTEGER,
                            predracun_id INTEGER,
                            datum_kreiranja TEXT NOT NULL,
                            datum_placanja TEXT,
                            status TEXT DEFAULT 'Kreirana',
                            nacin_placanja TEXT DEFAULT 'Gotovina',
                            rok_placanja TEXT,
                            mesto_izdavanja TEXT,
                            mesto_isporuke TEXT,
                            napomena TEXT,
                            popust_procenat REAL DEFAULT 0,
                            arhiviran INTEGER DEFAULT 0,
                            FOREIGN KEY (klijent_id) REFERENCES klijenti(id),
                            FOREIGN KEY (vozilo_id) REFERENCES vozila(id),
                            FOREIGN KEY (radni_nalog_id) REFERENCES radni_nalozi(id),
                            FOREIGN KEY (predracun_id) REFERENCES predracuni(id)
                        )
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS faktura_stavke (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            faktura_id INTEGER NOT NULL,
                            tip TEXT NOT NULL,
                            naziv TEXT NOT NULL,
                            kolicina REAL NOT NULL DEFAULT 1,
                            jedinica_mere TEXT DEFAULT 'kom',
                            cena_bez_pdv REAL NOT NULL DEFAULT 0,
                            pdv_stopa REAL DEFAULT 0,
                            popust_procenat REAL DEFAULT 0,
                            redni_broj INTEGER DEFAULT 0,
                            FOREIGN KEY (faktura_id) REFERENCES fakture(id)
                        )
                    """);

            try {
                stmt.execute("ALTER TABLE podesavanja ADD COLUMN logo_putanja TEXT");
            } catch (SQLException ignored) {}

            try {
                stmt.execute("ALTER TABLE klijenti ADD COLUMN napomena TEXT");
            } catch (SQLException ignored) {}

            try { stmt.execute("ALTER TABLE podesavanja ADD COLUMN pdv_obveznik INTEGER DEFAULT 0"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE podesavanja ADD COLUMN pdv_stopa REAL DEFAULT 20"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE podesavanja ADD COLUMN default_rok_placanja INTEGER DEFAULT 15"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE podesavanja ADD COLUMN gmail_adresa TEXT"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE podesavanja ADD COLUMN gmail_app_password TEXT"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE podesavanja ADD COLUMN email_tekst_predracun TEXT"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE podesavanja ADD COLUMN email_tekst_faktura TEXT"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE podesavanja ADD COLUMN email_naslov_predracun TEXT"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE podesavanja ADD COLUMN email_naslov_faktura TEXT"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE podesavanja ADD COLUMN email_zaglavlje TEXT"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE podesavanja ADD COLUMN email_footer TEXT"); } catch (SQLException ignored) {}

            try {
                stmt.execute("ALTER TABLE vozila ADD COLUMN napomena TEXT");
            } catch (SQLException ignored) {}

            try {
                stmt.execute("ALTER TABLE radni_nalozi ADD COLUMN zahtev_klijenta TEXT");
            } catch (SQLException ignored) {}

            try {
                stmt.execute("ALTER TABLE radni_nalozi ADD COLUMN napomena TEXT");
            } catch (SQLException ignored) {}

            try {
                stmt.execute("ALTER TABLE radni_nalozi ADD COLUMN faktura TEXT");
            } catch (SQLException ignored) {}

            try {
                stmt.execute("ALTER TABLE usluge ADD COLUMN cena REAL DEFAULT 0");
            } catch (SQLException ignored) {}

            try { stmt.execute("ALTER TABLE podesavanja ADD COLUMN valuta TEXT DEFAULT 'RSD'"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE ulaz_magacin ADD COLUMN dokument_id INTEGER REFERENCES ulaz_dokumenti(id)"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE fakture ADD COLUMN broj_racuna TEXT"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE fakture ADD COLUMN pfr_broj TEXT"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE podesavanja ADD COLUMN driver_putanja TEXT"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE artikli ADD COLUMN vrsta TEXT DEFAULT 'Artikal'"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE artikli ADD COLUMN sifra TEXT"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE podesavanja ADD COLUMN pocetni_broj_naloga INTEGER DEFAULT 1"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE podesavanja ADD COLUMN pocetni_broj_fakture INTEGER DEFAULT 1"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE podesavanja ADD COLUMN pocetni_broj_predracuna INTEGER DEFAULT 1"); } catch (SQLException ignored) {}
            try { stmt.execute("CREATE UNIQUE INDEX IF NOT EXISTS idx_artikli_sifra ON artikli(sifra) WHERE sifra IS NOT NULL"); } catch (SQLException ignored) {}

            // Dobavljaci
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS dobavljaci (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            naziv TEXT NOT NULL,
                            adresa TEXT,
                            kontakt TEXT,
                            pib TEXT,
                            arhiviran INTEGER DEFAULT 0
                        )
                    """);

            // Nivelacije cena
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS nivelacije (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            broj TEXT NOT NULL UNIQUE,
                            datum TEXT NOT NULL,
                            napomena TEXT
                        )
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS nivelacija_stavke (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            nivelacija_id INTEGER NOT NULL,
                            artikal_id INTEGER NOT NULL,
                            naziv_artikla TEXT NOT NULL,
                            sifra_artikla INTEGER,
                            jedinica_mere TEXT,
                            vrsta TEXT,
                            kolicina_stanju REAL,
                            stara_cena REAL NOT NULL DEFAULT 0,
                            nova_cena REAL NOT NULL DEFAULT 0,
                            FOREIGN KEY (nivelacija_id) REFERENCES nivelacije(id),
                            FOREIGN KEY (artikal_id) REFERENCES artikli(id)
                        )
                    """);

            try { stmt.execute("ALTER TABLE podesavanja ADD COLUMN odgovorno_lice TEXT"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE ulaz_dokumenti ADD COLUMN dobavljac_id INTEGER REFERENCES dobavljaci(id)"); } catch (SQLException ignored) {}
            try { stmt.execute("ALTER TABLE ulaz_dokumenti ADD COLUMN broj_otpremnice TEXT"); } catch (SQLException ignored) {}

            // Šabloni popisa
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS popis_sabloni (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            naziv TEXT NOT NULL
                        )
                    """);

            // Licenciranje — key/value store
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS settings (
                            key   TEXT PRIMARY KEY,
                            value TEXT
                        )
                    """);

            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS popis_sablon_stavke (
                            id INTEGER PRIMARY KEY AUTOINCREMENT,
                            sablon_id INTEGER NOT NULL REFERENCES popis_sabloni(id) ON DELETE CASCADE,
                            artikal_id INTEGER NOT NULL REFERENCES artikli(id),
                            UNIQUE(sablon_id, artikal_id)
                        )
                    """);

            logger.info("Baza podataka uspešno inicijalizovana.");

        } catch (SQLException e) {
            logger.severe("Greška pri inicijalizaciji baze: " + e.getMessage());
        }
    }

    public static String getSetting(String key) {
        try (java.sql.PreparedStatement ps = getConnection()
                .prepareStatement("SELECT value FROM settings WHERE key = ?")) {
            ps.setString(1, key);
            java.sql.ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString(1);
        } catch (SQLException e) {
            logger.warning("getSetting failed for key=" + key + ": " + e.getMessage());
        }
        return null;
    }

    public static void saveSetting(String key, String value) {
        try (java.sql.PreparedStatement ps = getConnection().prepareStatement(
                "INSERT INTO settings(key,value) VALUES(?,?) " +
                "ON CONFLICT(key) DO UPDATE SET value=excluded.value")) {
            ps.setString(1, key);
            ps.setString(2, value);
            ps.executeUpdate();
        } catch (SQLException e) {
            logger.warning("saveSetting failed for key=" + key + ": " + e.getMessage());
        }
    }

    private static void exec(String sql) throws SQLException {
        getConnection().createStatement().execute(sql);
    }

    public static void backupNaDrajv(String driverPutanja) {
        if (driverPutanja == null || driverPutanja.isBlank()) return;
        try {
            java.io.File destinacija = new java.io.File(driverPutanja);
            if (!destinacija.exists()) destinacija.mkdirs();
            java.io.File izvor = new java.io.File(APP_DIR + java.io.File.separator + "autoservis.db");
            if (!izvor.exists()) return;
            java.io.File cilj = new java.io.File(destinacija, "autoservis.db");
            java.nio.file.Files.copy(izvor.toPath(), cilj.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            logger.info("Backup na drajv: " + cilj.getAbsolutePath());
        } catch (Exception e) {
            logger.warning("Greška pri backup-u na drajv: " + e.getMessage());
        }
    }

    public static void backupLokalni() {
        try {
            java.io.File izvor = new java.io.File(APP_DIR + java.io.File.separator + "autoservis.db");
            if (!izvor.exists()) return;

            java.io.File backupFolder = new java.io.File(APP_DIR + java.io.File.separator + "backup");
            if (!backupFolder.exists()) backupFolder.mkdirs();

            String timestamp = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd_MM_yyyy_HHmm"));
            java.io.File cilj = new java.io.File(backupFolder, "backup_" + timestamp + ".db");
            java.nio.file.Files.copy(izvor.toPath(), cilj.toPath(),
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            logger.info("Lokalni backup napravljen: " + cilj.getAbsolutePath());

            // Obrisi backupe starije od 30 dana
            java.io.File[] sviBackupi = backupFolder.listFiles(
                    (dir, name) -> name.startsWith("backup_") && name.endsWith(".db"));
            if (sviBackupi != null) {
                java.time.LocalDate granica = java.time.LocalDate.now().minusDays(30);
                java.time.format.DateTimeFormatter fmt =
                        java.time.format.DateTimeFormatter.ofPattern("dd_MM_yyyy");
                for (java.io.File f : sviBackupi) {
                    try {
                        // Prvih 10 karaktera datumskog dela pokriva oba formata (sa i bez HHmm)
                        String imeFajla = f.getName().replace("backup_", "").replace(".db", "");
                        String datumDeo = imeFajla.length() >= 10 ? imeFajla.substring(0, 10) : imeFajla;
                        java.time.LocalDate datumBackupa = java.time.LocalDate.parse(datumDeo, fmt);
                        if (datumBackupa.isBefore(granica)) {
                            f.delete();
                            logger.info("Obrisan stari backup: " + f.getName());
                        }
                    } catch (Exception ignored) {}
                }
            }

        } catch (Exception e) {
            logger.warning("Greška pri lokalnom backup-u: " + e.getMessage());
        }
    }
}