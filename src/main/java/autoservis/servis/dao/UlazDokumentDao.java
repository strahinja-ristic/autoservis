package autoservis.servis.dao;

import autoservis.servis.model.UlazDokument;
import autoservis.servis.model.UlazMagacin;
import autoservis.servis.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UlazDokumentDao {

    public int dodaj(UlazDokument d) throws SQLException {
        String sql = "INSERT INTO ulaz_dokumenti (broj, datum, napomena, dobavljac_id, broj_otpremnice) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, d.getBroj());
            stmt.setString(2, d.getDatum());
            stmt.setString(3, d.getNapomena() != null && !d.getNapomena().isBlank() ? d.getNapomena() : null);
            if (d.getDobavljacId() != null) stmt.setInt(4, d.getDobavljacId());
            else stmt.setNull(4, java.sql.Types.INTEGER);
            stmt.setString(5, d.getBrojOtpremnice());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    public String generisiBroj() throws SQLException {
        String godina = String.valueOf(java.time.Year.now().getValue()).substring(2);
        String sql = "SELECT broj FROM ulaz_dokumenti WHERE broj LIKE ?";
        int max = 0;
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, "UD-%-" + godina);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    try {
                        int n = Integer.parseInt(rs.getString(1).split("-")[1]);
                        if (n > max) max = n;
                    } catch (Exception ignored) {}
                }
            }
        }
        return "UD-" + (max + 1) + "-" + godina;
    }

    public List<UlazDokument> vratiSve() throws SQLException {
        List<UlazDokument> lista = new ArrayList<>();
        String sql = """
                SELECT d.id, d.broj, d.datum, d.napomena, d.dobavljac_id, d.broj_otpremnice,
                       dob.naziv AS dobavljac_naziv, COUNT(s.id) AS broj_stavki
                FROM ulaz_dokumenti d
                LEFT JOIN ulaz_magacin s ON s.dokument_id = d.id
                LEFT JOIN dobavljaci dob ON d.dobavljac_id = dob.id
                GROUP BY d.id
                ORDER BY d.id DESC
                """;
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                UlazDokument d = new UlazDokument();
                d.setId(rs.getInt("id"));
                d.setBroj(rs.getString("broj"));
                d.setDatum(rs.getString("datum"));
                d.setNapomena(rs.getString("napomena") != null ? rs.getString("napomena") : "");
                d.setBrojStavki(rs.getInt("broj_stavki"));
                int dobId = rs.getInt("dobavljac_id");
                d.setDobavljacId(rs.wasNull() ? null : dobId);
                d.setDobavljacNaziv(rs.getString("dobavljac_naziv"));
                d.setBrojOtpremnice(rs.getString("broj_otpremnice"));
                lista.add(d);
            }
        }
        return lista;
    }

    public List<UlazMagacin> vratiStavke(int dokId) throws SQLException {
        List<UlazMagacin> lista = new ArrayList<>();
        String sql = """
                SELECT s.*, a.naziv AS naziv_artikla, a.jedinica_mere
                FROM ulaz_magacin s
                JOIN artikli a ON s.artikal_id = a.id
                WHERE s.dokument_id = ?
                ORDER BY s.id
                """;
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, dokId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UlazMagacin s = new UlazMagacin();
                    s.setId(rs.getInt("id"));
                    s.setArtikalId(rs.getInt("artikal_id"));
                    s.setNazivArtikla(rs.getString("naziv_artikla"));
                    s.setJedinicaMere(rs.getString("jedinica_mere"));
                    s.setKolicina(rs.getDouble("kolicina"));
                    s.setDatum(rs.getString("datum"));
                    s.setNapomena(rs.getString("napomena"));
                    lista.add(s);
                }
            }
        }
        return lista;
    }
}
