package autoservis.servis.dao;

import autoservis.servis.model.Faktura;
import autoservis.servis.model.FakturaStavka;
import autoservis.servis.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FakturaDao {

    public int dodaj(Faktura f) throws SQLException {
        String sql = """
                INSERT INTO fakture (broj_fakture, klijent_id, vozilo_id, radni_nalog_id, predracun_id,
                datum_kreiranja, datum_placanja, status, nacin_placanja, rok_placanja,
                mesto_izdavanja, mesto_isporuke, napomena, popust_procenat, broj_racuna, pfr_broj)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, f.getBrojFakture());
            stmt.setInt(2, f.getKlijentId());
            setNullableInt(stmt, 3, f.getVoziloId());
            setNullableInt(stmt, 4, f.getRadniNalogId());
            setNullableInt(stmt, 5, f.getPredracunId());
            stmt.setString(6, f.getDatumKreiranja());
            stmt.setString(7, f.getDatumPlacanja());
            stmt.setString(8, f.getStatus());
            stmt.setString(9, f.getNacinPlacanja());
            stmt.setString(10, f.getRokPlacanja());
            stmt.setString(11, f.getMestoIzdavanja());
            stmt.setString(12, f.getMestoIsporuke());
            stmt.setString(13, f.getNapomena());
            stmt.setDouble(14, f.getPopustProcenat());
            stmt.setString(15, f.getBrojRacuna());
            stmt.setString(16, f.getPfrBroj());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    public void izmeni(Faktura f) throws SQLException {
        String sql = """
                UPDATE fakture SET klijent_id=?, vozilo_id=?, radni_nalog_id=?, predracun_id=?,
                datum_kreiranja=?, datum_placanja=?, status=?, nacin_placanja=?, rok_placanja=?,
                mesto_izdavanja=?, mesto_isporuke=?, napomena=?, popust_procenat=?, broj_racuna=?, pfr_broj=?
                WHERE id=?
                """;
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, f.getKlijentId());
            setNullableInt(stmt, 2, f.getVoziloId());
            setNullableInt(stmt, 3, f.getRadniNalogId());
            setNullableInt(stmt, 4, f.getPredracunId());
            stmt.setString(5, f.getDatumKreiranja());
            stmt.setString(6, f.getDatumPlacanja());
            stmt.setString(7, f.getStatus());
            stmt.setString(8, f.getNacinPlacanja());
            stmt.setString(9, f.getRokPlacanja());
            stmt.setString(10, f.getMestoIzdavanja());
            stmt.setString(11, f.getMestoIsporuke());
            stmt.setString(12, f.getNapomena());
            stmt.setDouble(13, f.getPopustProcenat());
            stmt.setString(14, f.getBrojRacuna());
            stmt.setString(15, f.getPfrBroj());
            stmt.setInt(16, f.getId());
            stmt.executeUpdate();
        }
    }

    public void promeniStatus(int id, String status) throws SQLException {
        String sql = "UPDATE fakture SET status=? WHERE id=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }

    public void arhiviraj(int id) throws SQLException {
        String sql = "UPDATE fakture SET arhiviran=1 WHERE id=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public void vratiIzArhive(int id) throws SQLException {
        String sql = "UPDATE fakture SET arhiviran=0 WHERE id=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public String generisiBroj() throws SQLException {
        String godina = String.valueOf(java.time.Year.now().getValue()).substring(2);
        String sql = "SELECT broj_fakture FROM fakture WHERE broj_fakture LIKE ?";
        int max = 0;
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, "F-%-" + godina);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    try {
                        int n = Integer.parseInt(rs.getString(1).split("-")[1]);
                        if (n > max) max = n;
                    } catch (Exception ignored) {}
                }
            }
        }
        int pocetni = 1;
        try (java.sql.Statement st = DatabaseManager.getConnection().createStatement();
             ResultSet rs = st.executeQuery("SELECT pocetni_broj_fakture FROM podesavanja WHERE id=1")) {
            if (rs.next()) pocetni = rs.getInt(1);
        } catch (Exception ignored) {}
        return "F-" + Math.max(max + 1, pocetni) + "-" + godina;
    }

    public List<Faktura> vratiSve() throws SQLException {
        List<Faktura> lista = new ArrayList<>();
        String sql = "SELECT * FROM fakture WHERE arhiviran=0 ORDER BY id DESC";
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) lista.add(mapiraj(rs));
        }
        return lista;
    }

    public List<Faktura> vratiArhivirane() throws SQLException {
        List<Faktura> lista = new ArrayList<>();
        String sql = "SELECT * FROM fakture WHERE arhiviran=1 ORDER BY id DESC";
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) lista.add(mapiraj(rs));
        }
        return lista;
    }

    public Faktura vratiPoId(int id) throws SQLException {
        String sql = "SELECT * FROM fakture WHERE id=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Faktura f = mapiraj(rs);
                    f.setStavke(vratiStavke(id));
                    return f;
                }
            }
        }
        return null;
    }

    public List<Faktura> pretrazi(String upit) throws SQLException {
        List<Faktura> lista = new ArrayList<>();
        String sql = """
                SELECT f.* FROM fakture f
                LEFT JOIN klijenti k ON f.klijent_id = k.id
                WHERE f.arhiviran=0 AND (
                    f.broj_fakture LIKE ? OR f.status LIKE ? OR f.nacin_placanja LIKE ? OR
                    k.ime LIKE ? OR k.prezime LIKE ? OR k.naziv_firme LIKE ? OR k.telefon LIKE ?
                )
                ORDER BY f.id DESC
                """;
        String pattern = "%" + upit + "%";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            for (int i = 1; i <= 7; i++) stmt.setString(i, pattern);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(mapiraj(rs));
            }
        }
        return lista;
    }

    public int broji(String upit, String status, boolean arhiviran) throws SQLException {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) FROM fakture f LEFT JOIN klijenti k ON f.klijent_id=k.id WHERE f.arhiviran=?");
        params.add(arhiviran ? 1 : 0);
        if (upit != null && !upit.isBlank()) {
            String p = "%" + upit + "%";
            sql.append(" AND (f.broj_fakture LIKE ? OR f.status LIKE ? OR f.nacin_placanja LIKE ? OR k.ime LIKE ? OR k.prezime LIKE ? OR k.naziv_firme LIKE ? OR k.telefon LIKE ?)");
            for (int i = 0; i < 7; i++) params.add(p);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND f.status=?");
            params.add(status);
        }
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) stmt.setObject(i + 1, params.get(i));
            try (ResultSet rs = stmt.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        }
    }

    public List<Faktura> vratiStranicu(String upit, String status, boolean arhiviran, int offset, int limit) throws SQLException {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT f.* FROM fakture f LEFT JOIN klijenti k ON f.klijent_id=k.id WHERE f.arhiviran=?");
        params.add(arhiviran ? 1 : 0);
        if (upit != null && !upit.isBlank()) {
            String p = "%" + upit + "%";
            sql.append(" AND (f.broj_fakture LIKE ? OR f.status LIKE ? OR f.nacin_placanja LIKE ? OR k.ime LIKE ? OR k.prezime LIKE ? OR k.naziv_firme LIKE ? OR k.telefon LIKE ?)");
            for (int i = 0; i < 7; i++) params.add(p);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND f.status=?");
            params.add(status);
        }
        sql.append(" ORDER BY CASE f.status WHEN 'Kreirana' THEN 0 WHEN 'Poslata' THEN 1 WHEN 'Plaćena' THEN 2 WHEN 'Stornirana' THEN 3 ELSE 99 END, f.id DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        List<Faktura> lista = new ArrayList<>();
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) stmt.setObject(i + 1, params.get(i));
            try (ResultSet rs = stmt.executeQuery()) { while (rs.next()) lista.add(mapiraj(rs)); }
        }
        return lista;
    }

    public void dodajStavku(int fakturaId, FakturaStavka s) throws SQLException {
        String sql = """
                INSERT INTO faktura_stavke (faktura_id, tip, naziv, kolicina, jedinica_mere,
                cena_bez_pdv, pdv_stopa, popust_procenat, redni_broj)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, fakturaId);
            stmt.setString(2, s.getTip());
            stmt.setString(3, s.getNaziv());
            stmt.setDouble(4, s.getKolicina());
            stmt.setString(5, s.getJedinicaMere());
            stmt.setDouble(6, s.getCenaBezPdv());
            stmt.setDouble(7, s.getPdvStopa());
            stmt.setDouble(8, s.getPopustProcenat());
            stmt.setInt(9, s.getRedniBroj());
            stmt.executeUpdate();
        }
    }

    public void obrisiStavke(int fakturaId) throws SQLException {
        String sql = "DELETE FROM faktura_stavke WHERE faktura_id=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, fakturaId);
            stmt.executeUpdate();
        }
    }

    public List<FakturaStavka> vratiStavke(int fakturaId) throws SQLException {
        List<FakturaStavka> lista = new ArrayList<>();
        String sql = "SELECT * FROM faktura_stavke WHERE faktura_id=? ORDER BY redni_broj";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, fakturaId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(mapirajStavku(rs));
            }
        }
        return lista;
    }

    public int broji(String statusFilter) throws SQLException {
        String sql = statusFilter == null
                ? "SELECT COUNT(*) FROM fakture WHERE arhiviran=0"
                : "SELECT COUNT(*) FROM fakture WHERE arhiviran=0 AND status=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            if (statusFilter != null) stmt.setString(1, statusFilter);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    public String postojiZaPredracun(int predracunId) throws SQLException {
        String sql = """
                SELECT broj_fakture FROM fakture
                WHERE arhiviran=0 AND (
                    predracun_id=?
                    OR radni_nalog_id=(SELECT radni_nalog_id FROM predracuni WHERE id=? AND radni_nalog_id IS NOT NULL)
                )
                LIMIT 1
                """;
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, predracunId);
            stmt.setInt(2, predracunId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString("broj_fakture");
            }
        }
        return null;
    }

    public String postojiZaNalog(int nalogId) throws SQLException {
        String sql = "SELECT broj_fakture FROM fakture WHERE radni_nalog_id=? AND arhiviran=0 LIMIT 1";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, nalogId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getString("broj_fakture");
            }
        }
        return null;
    }

    private Faktura mapiraj(ResultSet rs) throws SQLException {
        Faktura f = new Faktura();
        f.setId(rs.getInt("id"));
        f.setBrojFakture(rs.getString("broj_fakture"));
        f.setKlijentId(rs.getInt("klijent_id"));
        int vid = rs.getInt("vozilo_id"); f.setVoziloId(rs.wasNull() ? null : vid);
        int nid = rs.getInt("radni_nalog_id"); f.setRadniNalogId(rs.wasNull() ? null : nid);
        int pid = rs.getInt("predracun_id"); f.setPredracunId(rs.wasNull() ? null : pid);
        f.setDatumKreiranja(rs.getString("datum_kreiranja"));
        f.setDatumPlacanja(rs.getString("datum_placanja"));
        f.setStatus(rs.getString("status"));
        f.setNacinPlacanja(rs.getString("nacin_placanja"));
        f.setRokPlacanja(rs.getString("rok_placanja"));
        f.setMestoIzdavanja(rs.getString("mesto_izdavanja"));
        f.setMestoIsporuke(rs.getString("mesto_isporuke"));
        f.setNapomena(rs.getString("napomena"));
        f.setPopustProcenat(rs.getDouble("popust_procenat"));
        f.setArhiviran(rs.getInt("arhiviran") == 1);
        f.setBrojRacuna(rs.getString("broj_racuna"));
        f.setPfrBroj(rs.getString("pfr_broj"));
        return f;
    }

    private FakturaStavka mapirajStavku(ResultSet rs) throws SQLException {
        FakturaStavka s = new FakturaStavka();
        s.setId(rs.getInt("id"));
        s.setFakturaId(rs.getInt("faktura_id"));
        s.setTip(rs.getString("tip"));
        s.setNaziv(rs.getString("naziv"));
        s.setKolicina(rs.getDouble("kolicina"));
        s.setJedinicaMere(rs.getString("jedinica_mere"));
        s.setCenaBezPdv(rs.getDouble("cena_bez_pdv"));
        s.setPdvStopa(rs.getDouble("pdv_stopa"));
        s.setPopustProcenat(rs.getDouble("popust_procenat"));
        s.setRedniBroj(rs.getInt("redni_broj"));
        return s;
    }

    private void setNullableInt(PreparedStatement stmt, int idx, Integer val) throws SQLException {
        if (val != null) stmt.setInt(idx, val);
        else stmt.setNull(idx, Types.INTEGER);
    }
}
