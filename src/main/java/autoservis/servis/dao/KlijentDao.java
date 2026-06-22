package autoservis.servis.dao;

import autoservis.servis.model.Klijent;
import autoservis.servis.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class KlijentDao {

    // Dodaj novog klijenta
    public void dodaj(Klijent klijent) throws SQLException {
        String sql = """
                INSERT INTO klijenti (tip, ime, prezime, naziv_firme, pib, maticni_broj, adresa, telefon, email, napomena)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, klijent.getTip());
            stmt.setString(2, klijent.getIme());
            stmt.setString(3, klijent.getPrezime());
            stmt.setString(4, klijent.getNazivFirme());
            stmt.setString(5, klijent.getPib());
            stmt.setString(6, klijent.getMaticniBroj());
            stmt.setString(7, klijent.getAdresa());
            stmt.setString(8, klijent.getTelefon());
            stmt.setString(9, klijent.getEmail());
            stmt.setString(10, klijent.getNapomena());
            stmt.executeUpdate();
        }
    }

    // Izmeni postojeceg klijenta
    public void izmeni(Klijent klijent) throws SQLException {
        String sql = """
                UPDATE klijenti SET tip=?, ime=?, prezime=?, naziv_firme=?, pib=?, maticni_broj=?, adresa=?, telefon=?, email=?, napomena=?
                WHERE id=?
                """;
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, klijent.getTip());
            stmt.setString(2, klijent.getIme());
            stmt.setString(3, klijent.getPrezime());
            stmt.setString(4, klijent.getNazivFirme());
            stmt.setString(5, klijent.getPib());
            stmt.setString(6, klijent.getMaticniBroj());
            stmt.setString(7, klijent.getAdresa());
            stmt.setString(8, klijent.getTelefon());
            stmt.setString(9, klijent.getEmail());
            stmt.setString(10, klijent.getNapomena());
            stmt.setInt(11, klijent.getId());
            stmt.executeUpdate();
        }
    }

    // Arhiviraj klijenta (umesto brisanja)
    public void arhiviraj(int id) throws SQLException {
        String sql = "UPDATE klijenti SET arhiviran=1 WHERE id=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public void vratiIzArhive(int id) throws SQLException {
        String sql = "UPDATE klijenti SET arhiviran=0 WHERE id=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public List<Klijent> vratiArhivirane() throws SQLException {
        List<Klijent> lista = new ArrayList<>();
        String sql = "SELECT * FROM klijenti WHERE arhiviran=1 ORDER BY ime, naziv_firme";
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) lista.add(mapiraj(rs));
        }
        return lista;
    }

    // Vrati sve aktivne klijente
    public List<Klijent> vratiSve() throws SQLException {
        List<Klijent> lista = new ArrayList<>();
        String sql = "SELECT * FROM klijenti WHERE arhiviran=0 ORDER BY ime, naziv_firme";
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapiraj(rs));
            }
        }
        return lista;
    }

    // Vrati klijenta po ID-u
    public Klijent vratiPoId(int id) throws SQLException {
        String sql = "SELECT * FROM klijenti WHERE id=?";
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

    // Pretraga klijenata
    public List<Klijent> pretrazi(String upit) throws SQLException {
        List<Klijent> lista = new ArrayList<>();
        String sql = """
            SELECT * FROM klijenti
            WHERE arhiviran=0 AND (
                ime LIKE ? OR prezime LIKE ? OR naziv_firme LIKE ? OR
                telefon LIKE ? OR email LIKE ? OR adresa LIKE ? OR
                pib LIKE ? OR maticni_broj LIKE ? OR napomena LIKE ?
            )
            ORDER BY ime, naziv_firme
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

    // Pomocna metoda - mapiranje ResultSet u Klijent objekat
    private Klijent mapiraj(ResultSet rs) throws SQLException {
        Klijent k = new Klijent();
        k.setId(rs.getInt("id"));
        k.setTip(rs.getString("tip"));
        k.setIme(rs.getString("ime"));
        k.setPrezime(rs.getString("prezime"));
        k.setNazivFirme(rs.getString("naziv_firme"));
        k.setPib(rs.getString("pib"));
        k.setMaticniBroj(rs.getString("maticni_broj"));
        k.setAdresa(rs.getString("adresa"));
        k.setTelefon(rs.getString("telefon"));
        k.setEmail(rs.getString("email"));
        k.setArhiviran(rs.getInt("arhiviran") == 1);
        k.setNapomena(rs.getString("napomena"));
        return k;
    }
}