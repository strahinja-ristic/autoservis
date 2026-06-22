package autoservis.servis.dao;

import autoservis.servis.model.SablonUsluge;
import autoservis.servis.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SablonUslugeDao {

    public void dodaj(SablonUsluge sablon) throws SQLException {
        String sql = "INSERT INTO sabloni_usluga (naziv, cena) VALUES (?, ?)";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, sablon.getNaziv());
            stmt.setDouble(2, sablon.getCena());
            stmt.executeUpdate();
        }
    }

    public void izmeni(SablonUsluge sablon) throws SQLException {
        String sql = "UPDATE sabloni_usluga SET naziv=?, cena=? WHERE id=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, sablon.getNaziv());
            stmt.setDouble(2, sablon.getCena());
            stmt.setInt(3, sablon.getId());
            stmt.executeUpdate();
        }
    }

    public void arhiviraj(int id) throws SQLException {
        String sql = "UPDATE sabloni_usluga SET arhiviran=1 WHERE id=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public void vratiIzArhive(int id) throws SQLException {
        String sql = "UPDATE sabloni_usluga SET arhiviran=0 WHERE id=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public List<SablonUsluge> vratiArhivirane() throws SQLException {
        List<SablonUsluge> lista = new ArrayList<>();
        String sql = "SELECT * FROM sabloni_usluga WHERE arhiviran=1 ORDER BY naziv";
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) lista.add(mapiraj(rs));
        }
        return lista;
    }

    public List<SablonUsluge> vratiSve() throws SQLException {
        List<SablonUsluge> lista = new ArrayList<>();
        String sql = "SELECT * FROM sabloni_usluga WHERE arhiviran=0 ORDER BY naziv";
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapiraj(rs));
            }
        }
        return lista;
    }

    private SablonUsluge mapiraj(ResultSet rs) throws SQLException {
        SablonUsluge s = new SablonUsluge();
        s.setId(rs.getInt("id"));
        s.setNaziv(rs.getString("naziv"));
        s.setCena(rs.getDouble("cena"));
        s.setArhiviran(rs.getInt("arhiviran") == 1);
        return s;
    }
}