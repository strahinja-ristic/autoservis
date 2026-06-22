package autoservis.servis.dao;

import autoservis.servis.model.Artikal;
import autoservis.servis.model.NalogArtikal;
import autoservis.servis.model.NalogUsluga;
import autoservis.servis.model.RadniNalog;
import autoservis.servis.util.DatabaseManager;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RadniNalogDao {

    // Dodaj novi radni nalog
    public int dodaj(RadniNalog nalog) throws SQLException {
        String sql = """
                INSERT INTO radni_nalozi (broj_naloga, vozilo_id, klijent_id, kilometraza_prijema,
                datum_prijema, datum_zavrsetka, opis_kvara, zahtev_klijenta, status, ostecenja,
                sledeci_servis_km, sledeci_servis_datum, napomena, faktura)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, nalog.getBrojNaloga());
            stmt.setInt(2, nalog.getVoziloId());
            stmt.setInt(3, nalog.getKlijentId());
            stmt.setInt(4, nalog.getKilometrazaPrijema());
            stmt.setString(5, nalog.getDatumPrijema());
            stmt.setString(6, nalog.getDatumZavrsetka());
            stmt.setString(7, nalog.getOpisKvara());
            stmt.setString(8, nalog.getZahtevKlijenta());
            stmt.setString(9, nalog.getStatus());
            stmt.setString(10, nalog.getOstecenja());
            if (nalog.getSledeciServisKm() != null) {
                stmt.setInt(11, nalog.getSledeciServisKm());
            } else {
                stmt.setNull(11, Types.INTEGER);
            }
            stmt.setString(12, nalog.getSledeciServisDatum());
            stmt.setString(13, nalog.getNapomena());
            stmt.setString(14, nalog.getFaktura());
            stmt.executeUpdate();

            // Vrati generisani ID
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        }
        return -1;
    }

    // Izmeni radni nalog
    public void izmeni(RadniNalog nalog) throws SQLException {
        String sql = """
                UPDATE radni_nalozi SET kilometraza_prijema=?, datum_prijema=?, datum_zavrsetka=?,
                opis_kvara=?, zahtev_klijenta=?, status=?, ostecenja=?, sledeci_servis_km=?,
                sledeci_servis_datum=?, napomena=?, faktura=?
                WHERE id=?
                """;
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, nalog.getKilometrazaPrijema());
            stmt.setString(2, nalog.getDatumPrijema());
            stmt.setString(3, nalog.getDatumZavrsetka());
            stmt.setString(4, nalog.getOpisKvara());
            stmt.setString(5, nalog.getZahtevKlijenta());
            stmt.setString(6, nalog.getStatus());
            stmt.setString(7, nalog.getOstecenja());
            if (nalog.getSledeciServisKm() != null) {
                stmt.setInt(8, nalog.getSledeciServisKm());
            } else {
                stmt.setNull(8, Types.INTEGER);
            }
            stmt.setString(9, nalog.getSledeciServisDatum());
            stmt.setString(10, nalog.getNapomena());
            stmt.setString(11, nalog.getFaktura());
            stmt.setInt(12, nalog.getId());
            stmt.executeUpdate();
        }
    }

    // Promeni status naloga
    public void promeniStatus(int nalogId, String status) throws SQLException {
        String sql = "UPDATE radni_nalozi SET status=? WHERE id=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, nalogId);
            stmt.executeUpdate();
        }
    }

    // Arhiviraj nalog
    public void arhiviraj(int id) throws SQLException {
        String sql = "UPDATE radni_nalozi SET arhiviran=1 WHERE id=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // Vrati nalog iz arhive
    public void vratiIzArhive(int id) throws SQLException {
        String sql = "UPDATE radni_nalozi SET arhiviran=0 WHERE id=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    // Vrati sledeci broj naloga (format: n-gg)
    public String generisiBrojNaloga() throws SQLException {
        String godina = String.valueOf(java.time.Year.now().getValue()).substring(2);
        String sql = "SELECT broj_naloga FROM radni_nalozi WHERE broj_naloga LIKE ?";
        int max = 0;
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, "%-" + godina);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    try {
                        int n = Integer.parseInt(rs.getString(1).split("-")[0]);
                        if (n > max) max = n;
                    } catch (Exception ignored) {}
                }
            }
        }
        int pocetni = 1;
        try (java.sql.Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT pocetni_broj_naloga FROM podesavanja WHERE id=1")) {
            if (rs.next()) pocetni = rs.getInt(1);
        } catch (Exception ignored) {}
        return Math.max(max + 1, pocetni) + "-" + godina;
    }

    // Vrati sve aktivne naloge
    public List<RadniNalog> vratiSve() throws SQLException {
        List<RadniNalog> lista = new ArrayList<>();
        String sql = """
                SELECT * FROM radni_nalozi WHERE arhiviran=0
                ORDER BY CASE status
                    WHEN 'Primljeno' THEN 1
                    WHEN 'U radu' THEN 2
                    WHEN 'Završeno' THEN 3
                    ELSE 4
                END, id DESC
                """;
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapiraj(rs));
            }
        }
        return lista;
    }

    // Vrati naloge po vozilu
    public List<RadniNalog> vratiPoVozilu(int voziloId) throws SQLException {
        List<RadniNalog> lista = new ArrayList<>();
        String sql = "SELECT * FROM radni_nalozi WHERE vozilo_id=? AND arhiviran=0 ORDER BY id DESC";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, voziloId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapiraj(rs));
                }
            }
        }
        return lista;
    }

    // Vrati nalog po ID-u sa uslugama i artiklima
    public RadniNalog vratiPoId(int id) throws SQLException {
        String sql = "SELECT * FROM radni_nalozi WHERE id=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    RadniNalog nalog = mapiraj(rs);
                    nalog.setUsluge(vratiUsluge(id));
                    nalog.setArtikli(vratiArtikle(id));
                    return nalog;
                }
            }
        }
        return null;
    }

    // Dodaj uslugu na nalog
    public void dodajUslugu(int nalogId, String opis, double cena) throws SQLException {
        String sql = "INSERT INTO usluge (radni_nalog_id, opis, cena) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, nalogId);
            stmt.setString(2, opis);
            stmt.setDouble(3, cena);
            stmt.executeUpdate();
        }
    }

    // Obrisi sve usluge naloga (za izmenu)
    public void obrisiUsluge(int nalogId) throws SQLException {
        String sql = "DELETE FROM usluge WHERE radni_nalog_id=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, nalogId);
            stmt.executeUpdate();
        }
    }

    // Dodaj artikal na nalog
    public void dodajArtikal(int nalogId, int artikalId, double kolicina) throws SQLException {
        String sql = "INSERT INTO nalog_artikli (radni_nalog_id, artikal_id, kolicina) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, nalogId);
            stmt.setInt(2, artikalId);
            stmt.setDouble(3, kolicina);
            stmt.executeUpdate();
        }
    }

    // Obrisi sve artikle naloga (za izmenu)
    public void obrisiArtikle(int nalogId) throws SQLException {
        String sql = "DELETE FROM nalog_artikli WHERE radni_nalog_id=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, nalogId);
            stmt.executeUpdate();
        }
    }

    // Pretraga naloga
    public List<RadniNalog> pretrazi(String upit) throws SQLException {
        List<RadniNalog> lista = new ArrayList<>();
        String sql = """
            SELECT rn.* FROM radni_nalozi rn
            LEFT JOIN klijenti k ON rn.klijent_id = k.id
            LEFT JOIN vozila v ON rn.vozilo_id = v.id
            WHERE rn.arhiviran=0 AND (
                rn.broj_naloga LIKE ? OR rn.opis_kvara LIKE ? OR
                rn.status LIKE ? OR rn.ostecenja LIKE ? OR
                k.ime LIKE ? OR k.prezime LIKE ? OR k.naziv_firme LIKE ? OR
                k.telefon LIKE ? OR v.registracija LIKE ? OR
                v.marka LIKE ? OR v.model LIKE ?
            )
            ORDER BY CASE rn.status
                WHEN 'Primljeno' THEN 1
                WHEN 'U radu' THEN 2
                WHEN 'Završeno' THEN 3
                ELSE 4
            END, rn.id DESC
            """;
        String pattern = "%" + upit + "%";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            for (int i = 1; i <= 11; i++) stmt.setString(i, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(mapiraj(rs));
            }
        }
        return lista;
    }

    public int broji(String upit, String status, boolean arhiviran) throws SQLException {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) FROM radni_nalozi rn" +
            " LEFT JOIN klijenti k ON rn.klijent_id=k.id" +
            " LEFT JOIN vozila v ON rn.vozilo_id=v.id" +
            " WHERE rn.arhiviran=?");
        params.add(arhiviran ? 1 : 0);
        if (upit != null && !upit.isBlank()) {
            String p = "%" + upit + "%";
            sql.append(" AND (rn.broj_naloga LIKE ? OR rn.opis_kvara LIKE ? OR rn.status LIKE ? OR rn.ostecenja LIKE ? OR k.ime LIKE ? OR k.prezime LIKE ? OR k.naziv_firme LIKE ? OR k.telefon LIKE ? OR v.registracija LIKE ? OR v.marka LIKE ? OR v.model LIKE ?)");
            for (int i = 0; i < 11; i++) params.add(p);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND rn.status=?");
            params.add(status);
        }
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) stmt.setObject(i + 1, params.get(i));
            try (ResultSet rs = stmt.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        }
    }

    public List<RadniNalog> vratiStranicu(String upit, String status, boolean arhiviran, int offset, int limit) throws SQLException {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT rn.* FROM radni_nalozi rn" +
            " LEFT JOIN klijenti k ON rn.klijent_id=k.id" +
            " LEFT JOIN vozila v ON rn.vozilo_id=v.id" +
            " WHERE rn.arhiviran=?");
        params.add(arhiviran ? 1 : 0);
        if (upit != null && !upit.isBlank()) {
            String p = "%" + upit + "%";
            sql.append(" AND (rn.broj_naloga LIKE ? OR rn.opis_kvara LIKE ? OR rn.status LIKE ? OR rn.ostecenja LIKE ? OR k.ime LIKE ? OR k.prezime LIKE ? OR k.naziv_firme LIKE ? OR k.telefon LIKE ? OR v.registracija LIKE ? OR v.marka LIKE ? OR v.model LIKE ?)");
            for (int i = 0; i < 11; i++) params.add(p);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND rn.status=?");
            params.add(status);
        }
        sql.append(" ORDER BY CASE rn.status WHEN 'Primljeno' THEN 1 WHEN 'U radu' THEN 2 WHEN 'Završeno' THEN 3 ELSE 4 END, rn.id DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        List<RadniNalog> lista = new ArrayList<>();
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) stmt.setObject(i + 1, params.get(i));
            try (ResultSet rs = stmt.executeQuery()) { while (rs.next()) lista.add(mapiraj(rs)); }
        }
        return lista;
    }

    // Pomocna - vrati usluge naloga
    private List<NalogUsluga> vratiUsluge(int nalogId) throws SQLException {
        List<NalogUsluga> lista = new ArrayList<>();
        String sql = "SELECT opis, cena FROM usluge WHERE radni_nalog_id=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, nalogId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new NalogUsluga(rs.getString("opis"), rs.getDouble("cena")));
                }
            }
        }
        return lista;
    }

    public List<RadniNalog> vratiArhivirane() throws SQLException {
        List<RadniNalog> lista = new ArrayList<>();
        String sql = "SELECT * FROM radni_nalozi WHERE arhiviran=1 ORDER BY id DESC";
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapiraj(rs));
            }
        }
        return lista;
    }

    // Pomocna - vrati artikle naloga
    private List<NalogArtikal> vratiArtikle(int nalogId) throws SQLException {
        List<NalogArtikal> lista = new ArrayList<>();
        String sql = """
                SELECT na.*, a.naziv as naziv_artikla, a.jedinica_mere
                FROM nalog_artikli na
                JOIN artikli a ON na.artikal_id = a.id
                WHERE na.radni_nalog_id=?
                """;
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, nalogId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    NalogArtikal na = new NalogArtikal();
                    na.setId(rs.getInt("id"));
                    na.setRadniNalogId(nalogId);
                    na.setArtikalId(rs.getInt("artikal_id"));
                    na.setNazivArtikla(rs.getString("naziv_artikla"));
                    na.setKolicina(rs.getDouble("kolicina"));
                    na.setJedinicaMere(rs.getString("jedinica_mere"));
                    lista.add(na);
                }
            }
        }
        return lista;
    }

    // Pomocna metoda - mapiranje ResultSet u RadniNalog objekat
    private RadniNalog mapiraj(ResultSet rs) throws SQLException {
        RadniNalog rn = new RadniNalog();
        rn.setId(rs.getInt("id"));
        rn.setBrojNaloga(rs.getString("broj_naloga"));
        rn.setVoziloId(rs.getInt("vozilo_id"));
        rn.setKlijentId(rs.getInt("klijent_id"));
        rn.setKilometrazaPrijema(rs.getInt("kilometraza_prijema"));
        rn.setDatumPrijema(rs.getString("datum_prijema"));
        rn.setDatumZavrsetka(rs.getString("datum_zavrsetka"));
        rn.setOpisKvara(rs.getString("opis_kvara"));
        rn.setZahtevKlijenta(rs.getString("zahtev_klijenta"));
        rn.setStatus(rs.getString("status"));
        rn.setOstecenja(rs.getString("ostecenja"));
        int sledeciKm = rs.getInt("sledeci_servis_km");
        rn.setSledeciServisKm(rs.wasNull() ? null : sledeciKm);
        rn.setSledeciServisDatum(rs.getString("sledeci_servis_datum"));
        rn.setNapomena(rs.getString("napomena"));
        rn.setFaktura(rs.getString("faktura"));
        rn.setArhiviran(rs.getInt("arhiviran") == 1);
        return rn;
    }
    // Zavrsi nalog i skini artikle sa stanja
    public void zavrsiBNalog(int nalogId) throws SQLException {
        ArtikalDao artikalDao = new ArtikalDao();
        List<NalogArtikal> artikli = vratiArtikle(nalogId);

        List<String> nedovoljno = new ArrayList<>();
        for (NalogArtikal na : artikli) {
            Artikal a = artikalDao.vratiPoId(na.getArtikalId());
            if (a == null || a.isUsluga()) continue;
            if (a.getKolicina() != null && a.getKolicina() < na.getKolicina()) {
                nedovoljno.add("• " + a.getNaziv() + " — stanje: " + a.getKolicina()
                        + " " + a.getJedinicaMere() + ", potrebno: " + na.getKolicina());
            }
        }

        if (!nedovoljno.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Nedovoljno na stanju");
            alert.setHeaderText("Sledeći artikli nemaju dovoljno zaliha:");
            alert.setContentText(String.join("\n", nedovoljno)
                    + "\n\nAko nastavite, stanje će biti negativno. Da li želite da nastavite?");
            alert.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);
            var rezultat = alert.showAndWait();
            if (rezultat.isEmpty() || rezultat.get() != ButtonType.YES) return;
        }

        for (NalogArtikal na : artikli) {
            Artikal a = artikalDao.vratiPoId(na.getArtikalId());
            if (a == null || a.isUsluga()) continue;
            artikalDao.smanjiKolicinu(na.getArtikalId(), na.getKolicina());
        }
        promeniStatus(nalogId, "Završeno");
    }
}