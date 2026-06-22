package autoservis.servis.dao;

import autoservis.servis.model.Vozilo;
import autoservis.servis.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VoziloDao {

    // Dodaj novo vozilo
    public void dodaj(Vozilo vozilo) throws SQLException {
        String sql = """
                INSERT INTO vozila (klijent_id, marka, model, godiste, registracija, broj_sasije, kilometraza, napomena)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, vozilo.getKlijentId());
            stmt.setString(2, vozilo.getMarka());
            stmt.setString(3, vozilo.getModel());
            if (vozilo.getGodiste() != null) stmt.setInt(4, vozilo.getGodiste());
            else stmt.setNull(4, Types.INTEGER);
            stmt.setString(5, vozilo.getRegistracija());
            stmt.setString(6, vozilo.getBrojSasije());
            stmt.setInt(7, vozilo.getKilometraza());
            stmt.setString(8, vozilo.getNapomena());
            stmt.executeUpdate();
        }
    }

    // Izmeni postojece vozilo
    public void izmeni(Vozilo vozilo) throws SQLException {
        String sql = """
                UPDATE vozila SET klijent_id=?, marka=?, model=?, godiste=?, registracija=?, broj_sasije=?, kilometraza=?, napomena=?
                WHERE id=?
                """;
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, vozilo.getKlijentId());
            stmt.setString(2, vozilo.getMarka());
            stmt.setString(3, vozilo.getModel());
            if (vozilo.getGodiste() != null) stmt.setInt(4, vozilo.getGodiste());
            else stmt.setNull(4, Types.INTEGER);
            stmt.setString(5, vozilo.getRegistracija());
            stmt.setString(6, vozilo.getBrojSasije());
            stmt.setInt(7, vozilo.getKilometraza());
            stmt.setString(8, vozilo.getNapomena());
            stmt.setInt(9, vozilo.getId());
            stmt.executeUpdate();
        }
    }

    // Azuriraj samo kilometrazu
    public void azurirajKilometrazу(int voziloId, int novaKilometraza) throws SQLException {
        String sql = "UPDATE vozila SET kilometraza=? WHERE id=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, novaKilometraza);
            stmt.setInt(2, voziloId);
            stmt.executeUpdate();
        }
    }

    // Arhiviraj vozilo
    public void arhiviraj(int id) throws SQLException {
        String sql = "UPDATE vozila SET arhivirano=1 WHERE id=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public void vratiIzArhive(int id) throws SQLException {
        String sql = "UPDATE vozila SET arhivirano=0 WHERE id=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public int brojiAktivna() throws SQLException {
        String sql = "SELECT COUNT(*) FROM vozila WHERE arhivirano=0";
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public List<Vozilo> vratiArhivirane() throws SQLException {
        List<Vozilo> lista = new ArrayList<>();
        String sql = "SELECT * FROM vozila WHERE arhivirano=1 ORDER BY marka, model";
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) lista.add(mapiraj(rs));
        }
        return lista;
    }

    // Vrati sva aktivna vozila jednog klijenta
    public List<Vozilo> vratiPoKlijentu(int klijentId) throws SQLException {
        List<Vozilo> lista = new ArrayList<>();
        String sql = "SELECT * FROM vozila WHERE klijent_id=? AND arhivirano=0 ORDER BY marka, model";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, klijentId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapiraj(rs));
                }
            }
        }
        return lista;
    }

    // Vrati vozilo po ID-u
    public Vozilo vratiPoId(int id) throws SQLException {
        String sql = "SELECT * FROM vozila WHERE id=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapiraj(rs);
                }
            }
        }
        return null;
    }

    // Pretraga vozila
    public List<Vozilo> pretrazi(String upit) throws SQLException {
        List<Vozilo> lista = new ArrayList<>();
        String sql = """
            SELECT v.* FROM vozila v
            LEFT JOIN klijenti k ON v.klijent_id = k.id
            WHERE v.arhivirano=0 AND (
                v.registracija LIKE ? OR v.marka LIKE ? OR v.model LIKE ? OR
                v.broj_sasije LIKE ? OR v.godiste LIKE ? OR v.kilometraza LIKE ? OR
                k.ime LIKE ? OR k.prezime LIKE ? OR k.naziv_firme LIKE ?
            )
            ORDER BY v.marka, v.model
            """;
        String pattern = "%" + upit + "%";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            for (int i = 1; i <= 9; i++) stmt.setString(i, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(mapiraj(rs));
            }
        }
        return lista;
    }

    // Pomocna metoda - mapiranje ResultSet u Vozilo objekat
    private Vozilo mapiraj(ResultSet rs) throws SQLException {
        Vozilo v = new Vozilo();
        v.setId(rs.getInt("id"));
        v.setKlijentId(rs.getInt("klijent_id"));
        v.setMarka(rs.getString("marka"));
        v.setModel(rs.getString("model"));
        int g = rs.getInt("godiste"); v.setGodiste(rs.wasNull() ? null : g);
        v.setRegistracija(rs.getString("registracija"));
        v.setBrojSasije(rs.getString("broj_sasije"));
        v.setKilometraza(rs.getInt("kilometraza"));
        v.setArhivirano(rs.getInt("arhivirano") == 1);
        v.setNapomena(rs.getString("napomena"));
        return v;
    }
}