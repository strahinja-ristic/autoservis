package autoservis.servis.dao;

import autoservis.servis.model.Artikal;
import autoservis.servis.model.UlazMagacin;
import autoservis.servis.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ArtikalDao {

    // Dodaj novi artikal
    public void dodaj(Artikal artikal) throws SQLException {
        String sql = """
                INSERT INTO artikli (naziv, sifra, jedinica_mere, kolicina, nabavna_cena, prodajna_cena, minimalna_kolicina, vrsta)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, artikal.getNaziv());
            if (artikal.getSifra() != null) stmt.setInt(2, artikal.getSifra()); else stmt.setNull(2, Types.INTEGER);
            stmt.setString(3, artikal.getJedinicaMere());
            if (artikal.getKolicina() != null) stmt.setDouble(4, artikal.getKolicina());
            else stmt.setNull(4, Types.REAL);
            if (artikal.getNabavnaCena() != null) stmt.setDouble(5, artikal.getNabavnaCena());
            else stmt.setNull(5, Types.REAL);
            stmt.setDouble(6, artikal.getProdajnaCena());
            if (artikal.getMinimalnaKolicina() != null) stmt.setDouble(7, artikal.getMinimalnaKolicina());
            else stmt.setNull(7, Types.REAL);
            stmt.setString(8, artikal.getVrsta());
            stmt.executeUpdate();
        }
    }

    // Izmeni postojeci artikal
    public void izmeni(Artikal artikal) throws SQLException {
        String sql = """
                UPDATE artikli SET naziv=?, sifra=?, jedinica_mere=?, kolicina=?, nabavna_cena=?, prodajna_cena=?, minimalna_kolicina=?, vrsta=?
                WHERE id=?
                """;
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, artikal.getNaziv());
            if (artikal.getSifra() != null) stmt.setInt(2, artikal.getSifra()); else stmt.setNull(2, Types.INTEGER);
            stmt.setString(3, artikal.getJedinicaMere());
            if (artikal.getKolicina() != null) stmt.setDouble(4, artikal.getKolicina()); else stmt.setNull(4, Types.REAL);
            if (artikal.getNabavnaCena() != null) stmt.setDouble(5, artikal.getNabavnaCena()); else stmt.setNull(5, Types.REAL);
            stmt.setDouble(6, artikal.getProdajnaCena());
            if (artikal.getMinimalnaKolicina() != null) stmt.setDouble(7, artikal.getMinimalnaKolicina()); else stmt.setNull(7, Types.REAL);
            stmt.setString(8, artikal.getVrsta());
            stmt.setInt(9, artikal.getId());
            stmt.executeUpdate();
        }
    }

    // Arhiviraj artikal
    public void arhiviraj(int id) throws SQLException {
        String sql = "UPDATE artikli SET arhiviran=1 WHERE id=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public void vratiIzArhive(int id) throws SQLException {
        String sql = "UPDATE artikli SET arhiviran=0 WHERE id=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public List<Artikal> vratiArhivirane() throws SQLException {
        List<Artikal> lista = new ArrayList<>();
        String sql = "SELECT * FROM artikli WHERE arhiviran=1 ORDER BY naziv";
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) lista.add(mapiraj(rs));
        }
        return lista;
    }

    // Smanji kolicinu (izlaz - pri zavrsetku naloga)
    public void smanjiKolicinu(int artikalId, double kolicina) throws SQLException {
        String sql = "UPDATE artikli SET kolicina = kolicina - ? WHERE id=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setDouble(1, kolicina);
            stmt.setInt(2, artikalId);
            stmt.executeUpdate();
        }
    }

    // Povecaj kolicinu (ulaz u skladiste)
    public void povecajKolicinu(int artikalId, double kolicina) throws SQLException {
        String sql = "UPDATE artikli SET kolicina = kolicina + ? WHERE id=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setDouble(1, kolicina);
            stmt.setInt(2, artikalId);
            stmt.executeUpdate();
        }
    }

    public boolean postojiSifra(Integer sifra) throws SQLException {
        if (sifra == null) return false;
        String sql = "SELECT COUNT(*) FROM artikli WHERE sifra=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, sifra);
            try (ResultSet rs = stmt.executeQuery()) { return rs.next() && rs.getInt(1) > 0; }
        }
    }

    public boolean postojiSifraZaDrugi(Integer sifra, int excludeId) throws SQLException {
        if (sifra == null) return false;
        String sql = "SELECT COUNT(*) FROM artikli WHERE sifra=? AND id != ?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, sifra);
            stmt.setInt(2, excludeId);
            try (ResultSet rs = stmt.executeQuery()) { return rs.next() && rs.getInt(1) > 0; }
        }
    }

    // Vrati sve aktivne artikle
    public List<Artikal> vratiSve() throws SQLException {
        List<Artikal> lista = new ArrayList<>();
        String sql = "SELECT * FROM artikli WHERE arhiviran=0 ORDER BY naziv";
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapiraj(rs));
            }
        }
        return lista;
    }

    // Vrati artikle ispod minimalnog stanja
    public List<Artikal> vratiIspodMinimuma() throws SQLException {
        List<Artikal> lista = new ArrayList<>();
        String sql = "SELECT * FROM artikli WHERE arhiviran=0 AND vrsta='Artikal' AND kolicina < minimalna_kolicina ORDER BY naziv";
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapiraj(rs));
            }
        }
        return lista;
    }

    // Vrati artikal po ID-u
    public Artikal vratiPoId(int id) throws SQLException {
        String sql = "SELECT * FROM artikli WHERE id=?";
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

    // Vrati artikal po tacnom nazivu
    public Artikal vratiPoNazivu(String naziv) throws SQLException {
        String sql = "SELECT * FROM artikli WHERE arhiviran=0 AND naziv=? LIMIT 1";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, naziv);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapiraj(rs);
            }
        }
        return null;
    }

    // Pretraga artikala (po nazivu ili sifri)
    public List<Artikal> pretrazi(String upit) throws SQLException {
        List<Artikal> lista = new ArrayList<>();
        String sql = "SELECT * FROM artikli WHERE arhiviran=0 AND (naziv LIKE ? OR sifra LIKE ?) ORDER BY naziv";
        String pattern = "%" + upit + "%";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, pattern);
            stmt.setString(2, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(mapiraj(rs));
                }
            }
        }
        return lista;
    }

    // Dodaj ulaz u skladiste (istorija)
    public void dodajUlaz(UlazMagacin ulaz) throws SQLException {
        String sql = """
                INSERT INTO ulaz_magacin (artikal_id, kolicina, datum, napomena, dokument_id)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, ulaz.getArtikalId());
            stmt.setDouble(2, ulaz.getKolicina());
            stmt.setString(3, ulaz.getDatum());
            stmt.setString(4, ulaz.getNapomena());
            if (ulaz.getDokumentId() > 0) stmt.setInt(5, ulaz.getDokumentId());
            else stmt.setNull(5, Types.INTEGER);
            stmt.executeUpdate();
        }
        povecajKolicinu(ulaz.getArtikalId(), ulaz.getKolicina());
    }

    // Vrati istoriju ulaza za artikal
    public List<UlazMagacin> vratiIstorijaUlaza(int artikalId) throws SQLException {
        List<UlazMagacin> lista = new ArrayList<>();
        String sql = """
                SELECT u.*, a.naziv as naziv_artikla
                FROM ulaz_magacin u
                JOIN artikli a ON u.artikal_id = a.id
                WHERE u.artikal_id=?
                ORDER BY u.datum DESC
                """;
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, artikalId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UlazMagacin u = new UlazMagacin();
                    u.setId(rs.getInt("id"));
                    u.setArtikalId(rs.getInt("artikal_id"));
                    u.setNazivArtikla(rs.getString("naziv_artikla"));
                    u.setKolicina(rs.getDouble("kolicina"));
                    u.setDatum(rs.getString("datum"));
                    u.setNapomena(rs.getString("napomena"));
                    lista.add(u);
                }
            }
        }
        return lista;
    }

    // Pomocna metoda - mapiranje ResultSet u Artikal objekat
    private Artikal mapiraj(ResultSet rs) throws SQLException {
        Artikal a = new Artikal();
        a.setId(rs.getInt("id"));
        a.setNaziv(rs.getString("naziv"));
        int sv = rs.getInt("sifra"); a.setSifra(rs.wasNull() ? null : sv);
        a.setJedinicaMere(rs.getString("jedinica_mere"));
        double kol = rs.getDouble("kolicina"); a.setKolicina(rs.wasNull() ? null : kol);
        double nab = rs.getDouble("nabavna_cena"); a.setNabavnaCena(rs.wasNull() ? null : nab);
        a.setProdajnaCena(rs.getDouble("prodajna_cena"));
        double min = rs.getDouble("minimalna_kolicina"); a.setMinimalnaKolicina(rs.wasNull() ? null : min);
        a.setArhiviran(rs.getInt("arhiviran") == 1);
        try { a.setVrsta(rs.getString("vrsta") != null ? rs.getString("vrsta") : "Artikal"); } catch (SQLException ignored) {}
        return a;
    }
}