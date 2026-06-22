package autoservis.servis.dao;

import autoservis.servis.model.Predracun;
import autoservis.servis.model.PredracunStavka;
import autoservis.servis.util.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PredracunDao {

    public int dodaj(Predracun p) throws SQLException {
        String sql = """
                INSERT INTO predracuni (broj_predracuna, klijent_id, vozilo_id, radni_nalog_id,
                datum_kreiranja, datum_vazenja, status, nacin_placanja, rok_placanja,
                mesto_izdavanja, napomena, popust_procenat)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, p.getBrojPredracuna());
            stmt.setInt(2, p.getKlijentId());
            setNullableInt(stmt, 3, p.getVoziloId());
            setNullableInt(stmt, 4, p.getRadniNalogId());
            stmt.setString(5, p.getDatumKreiranja());
            stmt.setString(6, p.getDatumVazenja());
            stmt.setString(7, p.getStatus());
            stmt.setString(8, p.getNacinPlacanja());
            stmt.setString(9, p.getRokPlacanja());
            stmt.setString(10, p.getMestoIzdavanja());
            stmt.setString(11, p.getNapomena());
            stmt.setDouble(12, p.getPopustProcenat());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return -1;
    }

    public void izmeni(Predracun p) throws SQLException {
        String sql = """
                UPDATE predracuni SET klijent_id=?, vozilo_id=?, radni_nalog_id=?,
                datum_kreiranja=?, datum_vazenja=?, status=?, nacin_placanja=?, rok_placanja=?,
                mesto_izdavanja=?, napomena=?, popust_procenat=?
                WHERE id=?
                """;
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, p.getKlijentId());
            setNullableInt(stmt, 2, p.getVoziloId());
            setNullableInt(stmt, 3, p.getRadniNalogId());
            stmt.setString(4, p.getDatumKreiranja());
            stmt.setString(5, p.getDatumVazenja());
            stmt.setString(6, p.getStatus());
            stmt.setString(7, p.getNacinPlacanja());
            stmt.setString(8, p.getRokPlacanja());
            stmt.setString(9, p.getMestoIzdavanja());
            stmt.setString(10, p.getNapomena());
            stmt.setDouble(11, p.getPopustProcenat());
            stmt.setInt(12, p.getId());
            stmt.executeUpdate();
        }
    }

    public void promeniStatus(int id, String status) throws SQLException {
        String sql = "UPDATE predracuni SET status=? WHERE id=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, id);
            stmt.executeUpdate();
        }
    }

    public void postaviRadniNalogId(int predracunId, int nalogId) throws SQLException {
        String sql = "UPDATE predracuni SET radni_nalog_id=? WHERE id=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, nalogId);
            stmt.setInt(2, predracunId);
            stmt.executeUpdate();
        }
    }

    public void arhiviraj(int id) throws SQLException {
        String sql = "UPDATE predracuni SET arhiviran=1 WHERE id=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public void vratiIzArhive(int id) throws SQLException {
        String sql = "UPDATE predracuni SET arhiviran=0 WHERE id=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public String generisiBroj() throws SQLException {
        String godina = String.valueOf(java.time.Year.now().getValue()).substring(2);
        String sql = "SELECT broj_predracuna FROM predracuni WHERE broj_predracuna LIKE ?";
        int max = 0;
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, "PR-%-" + godina);
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
             ResultSet rs = st.executeQuery("SELECT pocetni_broj_predracuna FROM podesavanja WHERE id=1")) {
            if (rs.next()) pocetni = rs.getInt(1);
        } catch (Exception ignored) {}
        return "PR-" + Math.max(max + 1, pocetni) + "-" + godina;
    }

    public List<Predracun> vratiSve() throws SQLException {
        List<Predracun> lista = new ArrayList<>();
        String sql = "SELECT * FROM predracuni WHERE arhiviran=0 ORDER BY id DESC";
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) lista.add(mapiraj(rs));
        }
        return lista;
    }

    public Predracun vratiPoRadnomNalogu(int nalogId) throws SQLException {
        String sql = "SELECT * FROM predracuni WHERE radni_nalog_id=? LIMIT 1";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, nalogId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return mapiraj(rs);
            }
        }
        return null;
    }

    public List<Predracun> vratiArhivirane() throws SQLException {
        List<Predracun> lista = new ArrayList<>();
        String sql = "SELECT * FROM predracuni WHERE arhiviran=1 ORDER BY id DESC";
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) lista.add(mapiraj(rs));
        }
        return lista;
    }

    public Predracun vratiPoId(int id) throws SQLException {
        String sql = "SELECT * FROM predracuni WHERE id=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Predracun p = mapiraj(rs);
                    p.setStavke(vratiStavke(id));
                    return p;
                }
            }
        }
        return null;
    }

    public List<Predracun> pretrazi(String upit) throws SQLException {
        List<Predracun> lista = new ArrayList<>();
        String sql = """
                SELECT p.* FROM predracuni p
                LEFT JOIN klijenti k ON p.klijent_id = k.id
                WHERE p.arhiviran=0 AND (
                    p.broj_predracuna LIKE ? OR p.status LIKE ? OR p.nacin_placanja LIKE ? OR
                    k.ime LIKE ? OR k.prezime LIKE ? OR k.naziv_firme LIKE ? OR k.telefon LIKE ?
                )
                ORDER BY p.id DESC
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
            "SELECT COUNT(*) FROM predracuni p LEFT JOIN klijenti k ON p.klijent_id=k.id WHERE p.arhiviran=?");
        params.add(arhiviran ? 1 : 0);
        if (upit != null && !upit.isBlank()) {
            String p = "%" + upit + "%";
            sql.append(" AND (p.broj_predracuna LIKE ? OR p.status LIKE ? OR p.nacin_placanja LIKE ? OR k.ime LIKE ? OR k.prezime LIKE ? OR k.naziv_firme LIKE ? OR k.telefon LIKE ?)");
            for (int i = 0; i < 7; i++) params.add(p);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND p.status=?");
            params.add(status);
        }
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) stmt.setObject(i + 1, params.get(i));
            try (ResultSet rs = stmt.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        }
    }

    public List<Predracun> vratiStranicu(String upit, String status, boolean arhiviran, int offset, int limit) throws SQLException {
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder(
            "SELECT p.* FROM predracuni p LEFT JOIN klijenti k ON p.klijent_id=k.id WHERE p.arhiviran=?");
        params.add(arhiviran ? 1 : 0);
        if (upit != null && !upit.isBlank()) {
            String p = "%" + upit + "%";
            sql.append(" AND (p.broj_predracuna LIKE ? OR p.status LIKE ? OR p.nacin_placanja LIKE ? OR k.ime LIKE ? OR k.prezime LIKE ? OR k.naziv_firme LIKE ? OR k.telefon LIKE ?)");
            for (int i = 0; i < 7; i++) params.add(p);
        }
        if (status != null && !status.isBlank()) {
            sql.append(" AND p.status=?");
            params.add(status);
        }
        sql.append(" ORDER BY CASE p.status WHEN 'Kreiran' THEN 0 WHEN 'Poslat' THEN 1 WHEN 'Prihvaćen' THEN 2 WHEN 'Odbijen' THEN 3 WHEN 'Istekao' THEN 4 WHEN 'Fakturisan' THEN 5 ELSE 99 END, p.id DESC LIMIT ? OFFSET ?");
        params.add(limit);
        params.add(offset);
        List<Predracun> lista = new ArrayList<>();
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) stmt.setObject(i + 1, params.get(i));
            try (ResultSet rs = stmt.executeQuery()) { while (rs.next()) lista.add(mapiraj(rs)); }
        }
        return lista;
    }

    public void dodajStavku(int predracunId, PredracunStavka s) throws SQLException {
        String sql = """
                INSERT INTO predracun_stavke (predracun_id, tip, naziv, kolicina, jedinica_mere,
                cena_bez_pdv, pdv_stopa, popust_procenat, redni_broj)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, predracunId);
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

    public void obrisiStavke(int predracunId) throws SQLException {
        String sql = "DELETE FROM predracun_stavke WHERE predracun_id=?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, predracunId);
            stmt.executeUpdate();
        }
    }

    public List<PredracunStavka> vratiStavke(int predracunId) throws SQLException {
        List<PredracunStavka> lista = new ArrayList<>();
        String sql = "SELECT * FROM predracun_stavke WHERE predracun_id=? ORDER BY redni_broj";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, predracunId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(mapirajStavku(rs));
            }
        }
        return lista;
    }

    public void azurirajIstekle() throws SQLException {
        // Dates stored as "dd.MM.yyyy" — convert to "yyyy-MM-dd" for correct SQLite comparison
        String sql = """
                UPDATE predracuni SET status='Istekao'
                WHERE arhiviran=0 AND status NOT IN ('Fakturisan','Odbijen','Istekao')
                AND datum_vazenja IS NOT NULL AND datum_vazenja != ''
                AND substr(datum_vazenja,7,4)||'-'||substr(datum_vazenja,4,2)||'-'||substr(datum_vazenja,1,2) < date('now')
                """;
        try (java.sql.Statement stmt = DatabaseManager.getConnection().createStatement()) {
            stmt.executeUpdate(sql);
        }
    }

    public List<Predracun> vratiKojiIsticuZaDana(int dana) throws SQLException {
        List<Predracun> lista = new ArrayList<>();
        java.time.LocalDate datum = java.time.LocalDate.now().plusDays(dana);
        String datumStr = datum.format(java.time.format.DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        String sql = """
                SELECT * FROM predracuni WHERE arhiviran=0
                AND status NOT IN ('Fakturisan','Odbijen','Istekao')
                AND datum_vazenja=?
                """;
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, datumStr);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) lista.add(mapiraj(rs));
            }
        }
        return lista;
    }

    private Predracun mapiraj(ResultSet rs) throws SQLException {
        Predracun p = new Predracun();
        p.setId(rs.getInt("id"));
        p.setBrojPredracuna(rs.getString("broj_predracuna"));
        p.setKlijentId(rs.getInt("klijent_id"));
        int vid = rs.getInt("vozilo_id"); p.setVoziloId(rs.wasNull() ? null : vid);
        int nid = rs.getInt("radni_nalog_id"); p.setRadniNalogId(rs.wasNull() ? null : nid);
        p.setDatumKreiranja(rs.getString("datum_kreiranja"));
        p.setDatumVazenja(rs.getString("datum_vazenja"));
        p.setStatus(rs.getString("status"));
        p.setNacinPlacanja(rs.getString("nacin_placanja"));
        p.setRokPlacanja(rs.getString("rok_placanja"));
        p.setMestoIzdavanja(rs.getString("mesto_izdavanja"));
        p.setNapomena(rs.getString("napomena"));
        p.setPopustProcenat(rs.getDouble("popust_procenat"));
        p.setArhiviran(rs.getInt("arhiviran") == 1);
        return p;
    }

    private PredracunStavka mapirajStavku(ResultSet rs) throws SQLException {
        PredracunStavka s = new PredracunStavka();
        s.setId(rs.getInt("id"));
        s.setPredracunId(rs.getInt("predracun_id"));
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
