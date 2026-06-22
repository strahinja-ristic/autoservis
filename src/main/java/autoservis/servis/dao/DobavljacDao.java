package autoservis.servis.dao;

import autoservis.servis.model.Dobavljac;
import autoservis.servis.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DobavljacDao {

    public void dodaj(Dobavljac d) throws SQLException {
        String sql = "INSERT INTO dobavljaci (naziv, adresa, kontakt, pib) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, d.getNaziv());
            stmt.setString(2, d.getAdresa());
            stmt.setString(3, d.getKontakt());
            stmt.setString(4, d.getPib());
            stmt.executeUpdate();
        }
    }

    public void izmeni(Dobavljac d) throws SQLException {
        String sql = "UPDATE dobavljaci SET naziv=?, adresa=?, kontakt=?, pib=? WHERE id=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, d.getNaziv());
            stmt.setString(2, d.getAdresa());
            stmt.setString(3, d.getKontakt());
            stmt.setString(4, d.getPib());
            stmt.setInt(5, d.getId());
            stmt.executeUpdate();
        }
    }

    public void arhiviraj(int id) throws SQLException {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                "UPDATE dobavljaci SET arhiviran=1 WHERE id=?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public void vratiIzArhive(int id) throws SQLException {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                "UPDATE dobavljaci SET arhiviran=0 WHERE id=?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public List<Dobavljac> vratiSve() throws SQLException {
        return ucitaj("SELECT * FROM dobavljaci WHERE arhiviran=0 ORDER BY naziv");
    }

    public List<Dobavljac> vratiArhivirane() throws SQLException {
        return ucitaj("SELECT * FROM dobavljaci WHERE arhiviran=1 ORDER BY naziv");
    }

    public Dobavljac vratiPoId(int id) throws SQLException {
        String sql = "SELECT * FROM dobavljaci WHERE id=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapiraj(rs);
            }
        }
        return null;
    }

    private List<Dobavljac> ucitaj(String sql) throws SQLException {
        List<Dobavljac> lista = new ArrayList<>();
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) lista.add(mapiraj(rs));
        }
        return lista;
    }

    private Dobavljac mapiraj(ResultSet rs) throws SQLException {
        Dobavljac d = new Dobavljac();
        d.setId(rs.getInt("id"));
        d.setNaziv(rs.getString("naziv"));
        d.setAdresa(rs.getString("adresa"));
        d.setKontakt(rs.getString("kontakt"));
        d.setPib(rs.getString("pib"));
        d.setArhiviran(rs.getInt("arhiviran") == 1);
        return d;
    }
}
