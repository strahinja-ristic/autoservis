package autoservis.servis.dao;

import autoservis.servis.model.Artikal;
import autoservis.servis.model.PopisSablon;
import autoservis.servis.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PopisSablonDao {

    public int dodaj(String naziv) throws SQLException {
        String sql = "INSERT INTO popis_sabloni (naziv) VALUES (?)";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, naziv);
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    public void preimenuj(int id, String naziv) throws SQLException {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                "UPDATE popis_sabloni SET naziv=? WHERE id=?")) {
            stmt.setString(1, naziv);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }

    public void obrisi(int id) throws SQLException {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                "DELETE FROM popis_sabloni WHERE id=?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public List<PopisSablon> vratiSve() throws SQLException {
        List<PopisSablon> lista = new ArrayList<>();
        String sql = """
                SELECT s.id, s.naziv, COUNT(st.id) AS broj_artikala
                FROM popis_sabloni s
                LEFT JOIN popis_sablon_stavke st ON st.sablon_id = s.id
                GROUP BY s.id
                ORDER BY s.naziv
                """;
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                PopisSablon s = new PopisSablon();
                s.setId(rs.getInt("id"));
                s.setNaziv(rs.getString("naziv"));
                s.setBrojArtikala(rs.getInt("broj_artikala"));
                lista.add(s);
            }
        }
        return lista;
    }

    public List<Artikal> vratiArtikle(int sablonId) throws SQLException {
        List<Artikal> lista = new ArrayList<>();
        String sql = """
                SELECT a.* FROM artikli a
                JOIN popis_sablon_stavke st ON st.artikal_id = a.id
                WHERE st.sablon_id = ?
                ORDER BY a.naziv
                """;
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, sablonId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(mapirajArtikal(rs));
            }
        }
        return lista;
    }

    public void dodajArtikal(int sablonId, int artikalId) throws SQLException {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                "INSERT OR IGNORE INTO popis_sablon_stavke (sablon_id, artikal_id) VALUES (?, ?)")) {
            stmt.setInt(1, sablonId);
            stmt.setInt(2, artikalId);
            stmt.executeUpdate();
        }
    }

    public void ukloniArtikal(int sablonId, int artikalId) throws SQLException {
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(
                "DELETE FROM popis_sablon_stavke WHERE sablon_id=? AND artikal_id=?")) {
            stmt.setInt(1, sablonId);
            stmt.setInt(2, artikalId);
            stmt.executeUpdate();
        }
    }

    private Artikal mapirajArtikal(ResultSet rs) throws SQLException {
        Artikal a = new Artikal();
        a.setId(rs.getInt("id"));
        a.setNaziv(rs.getString("naziv"));
        a.setJedinicaMere(rs.getString("jedinica_mere"));
        try { a.setKolicina(rs.getDouble("kolicina")); } catch (SQLException ignored) {}
        try { a.setNabavnaCena(rs.getDouble("nabavna_cena")); } catch (SQLException ignored) {}
        try { a.setProdajnaCena(rs.getDouble("prodajna_cena")); } catch (SQLException ignored) {}
        try { a.setMinimalnaKolicina(rs.getDouble("minimalna_kolicina")); } catch (SQLException ignored) {}
        try { a.setVrsta(rs.getString("vrsta")); } catch (SQLException ignored) {}
        try { String s = rs.getString("sifra"); a.setSifra(s != null ? Integer.parseInt(s) : null); } catch (Exception ignored) {}
        return a;
    }
}
