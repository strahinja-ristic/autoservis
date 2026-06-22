package autoservis.servis.dao;

import autoservis.servis.model.Nivelacija;
import autoservis.servis.model.NivelacijaStavka;
import autoservis.servis.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NivelacijaDao {

    public String generisiBroj() throws SQLException {
        String sql = "SELECT COUNT(*) FROM nivelacije";
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            int broj = rs.next() ? rs.getInt(1) + 1 : 1;
            return String.format("NIV-%04d", broj);
        }
    }

    public void dodaj(Nivelacija nivelacija) throws SQLException {
        Connection conn = DatabaseManager.getConnection();
        boolean autoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try {
            String sql = "INSERT INTO nivelacije (broj, datum, napomena) VALUES (?, ?, ?)";
            int id;
            try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, nivelacija.getBroj());
                stmt.setString(2, nivelacija.getDatum());
                stmt.setString(3, nivelacija.getNapomena());
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    id = rs.next() ? rs.getInt(1) : 0;
                }
            }
            nivelacija.setId(id);

            ArtikalDao artikalDao = new ArtikalDao();
            for (NivelacijaStavka stavka : nivelacija.getStavke()) {
                stavka.setNivelacijaId(id);
                dodajStavku(stavka, conn);
                // Azuriraj prodajnu cenu artikla
                String update = "UPDATE artikli SET prodajna_cena=? WHERE id=?";
                try (PreparedStatement ps = conn.prepareStatement(update)) {
                    ps.setDouble(1, stavka.getNovaCena());
                    ps.setInt(2, stavka.getArtikalId());
                    ps.executeUpdate();
                }
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(autoCommit);
        }
    }

    private void dodajStavku(NivelacijaStavka stavka, Connection conn) throws SQLException {
        String sql = """
                INSERT INTO nivelacija_stavke
                    (nivelacija_id, artikal_id, naziv_artikla, sifra_artikla, jedinica_mere, vrsta, kolicina_stanju, stara_cena, nova_cena)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, stavka.getNivelacijaId());
            stmt.setInt(2, stavka.getArtikalId());
            stmt.setString(3, stavka.getNazivArtikla());
            if (stavka.getSifraArtikla() != null) stmt.setInt(4, stavka.getSifraArtikla());
            else stmt.setNull(4, Types.INTEGER);
            stmt.setString(5, stavka.getJedinicaMere());
            stmt.setString(6, stavka.getVrsta());
            if (stavka.getKolicinaStanju() != null) stmt.setDouble(7, stavka.getKolicinaStanju());
            else stmt.setNull(7, Types.REAL);
            stmt.setDouble(8, stavka.getStaraCena());
            stmt.setDouble(9, stavka.getNovaCena());
            stmt.executeUpdate();
        }
    }

    public List<Nivelacija> vratiSve() throws SQLException {
        List<Nivelacija> lista = new ArrayList<>();
        String sql = """
                SELECT n.*, COUNT(s.id) AS broj_stavki
                FROM nivelacije n
                LEFT JOIN nivelacija_stavke s ON s.nivelacija_id = n.id
                GROUP BY n.id
                ORDER BY n.datum DESC, n.id DESC
                """;
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                Nivelacija n = new Nivelacija();
                n.setId(rs.getInt("id"));
                n.setBroj(rs.getString("broj"));
                n.setDatum(rs.getString("datum"));
                n.setNapomena(rs.getString("napomena"));
                n.setBrojStavki(rs.getInt("broj_stavki"));
                lista.add(n);
            }
        }
        return lista;
    }

    public List<NivelacijaStavka> vratiStavke(int nivelacijaId) throws SQLException {
        List<NivelacijaStavka> lista = new ArrayList<>();
        String sql = "SELECT * FROM nivelacija_stavke WHERE nivelacija_id=? ORDER BY id";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, nivelacijaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    NivelacijaStavka s = new NivelacijaStavka();
                    s.setId(rs.getInt("id"));
                    s.setNivelacijaId(rs.getInt("nivelacija_id"));
                    s.setArtikalId(rs.getInt("artikal_id"));
                    s.setNazivArtikla(rs.getString("naziv_artikla"));
                    int sifra = rs.getInt("sifra_artikla");
                    s.setSifraArtikla(rs.wasNull() ? null : sifra);
                    s.setJedinicaMere(rs.getString("jedinica_mere"));
                    s.setVrsta(rs.getString("vrsta"));
                    double kol = rs.getDouble("kolicina_stanju");
                    s.setKolicinaStanju(rs.wasNull() ? null : kol);
                    s.setStaraCena(rs.getDouble("stara_cena"));
                    s.setNovaCena(rs.getDouble("nova_cena"));
                    lista.add(s);
                }
            }
        }
        return lista;
    }
}
