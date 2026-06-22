package autoservis.servis.dao;

import autoservis.servis.model.Podesavanja;
import autoservis.servis.util.DatabaseManager;

import java.sql.*;

public class PodesavanjaDao {

    public Podesavanja vratiPodesavanja() throws SQLException {
        String sql = "SELECT * FROM podesavanja WHERE id=1";
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) return mapiraj(rs);
        }
        return new Podesavanja();
    }

    public void sacuvaj(Podesavanja p) throws SQLException {
        String sql = """
                INSERT INTO podesavanja (id, naziv_firme, adresa, telefon, email, pib, maticni_broj,
                žiro_racun, logo_putanja, pdv_obveznik, pdv_stopa, default_rok_placanja,
                gmail_adresa, gmail_app_password, email_tekst_predracun, email_tekst_faktura,
                email_naslov_predracun, email_naslov_faktura, email_zaglavlje, email_footer, driver_putanja,
                pocetni_broj_naloga, pocetni_broj_fakture, pocetni_broj_predracuna, odgovorno_lice)
                VALUES (1, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT(id) DO UPDATE SET
                    naziv_firme=excluded.naziv_firme,
                    adresa=excluded.adresa,
                    telefon=excluded.telefon,
                    email=excluded.email,
                    pib=excluded.pib,
                    maticni_broj=excluded.maticni_broj,
                    žiro_racun=excluded.žiro_racun,
                    logo_putanja=excluded.logo_putanja,
                    pdv_obveznik=excluded.pdv_obveznik,
                    pdv_stopa=excluded.pdv_stopa,
                    default_rok_placanja=excluded.default_rok_placanja,
                    gmail_adresa=excluded.gmail_adresa,
                    gmail_app_password=excluded.gmail_app_password,
                    email_tekst_predracun=excluded.email_tekst_predracun,
                    email_tekst_faktura=excluded.email_tekst_faktura,
                    email_naslov_predracun=excluded.email_naslov_predracun,
                    email_naslov_faktura=excluded.email_naslov_faktura,
                    email_zaglavlje=excluded.email_zaglavlje,
                    email_footer=excluded.email_footer,
                    driver_putanja=excluded.driver_putanja,
                    pocetni_broj_naloga=excluded.pocetni_broj_naloga,
                    pocetni_broj_fakture=excluded.pocetni_broj_fakture,
                    pocetni_broj_predracuna=excluded.pocetni_broj_predracuna,
                    odgovorno_lice=excluded.odgovorno_lice
                """;
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, p.getNazivFirme());
            stmt.setString(2, p.getAdresa());
            stmt.setString(3, p.getTelefon());
            stmt.setString(4, p.getEmail());
            stmt.setString(5, p.getPib());
            stmt.setString(6, p.getMaticniBroj());
            stmt.setString(7, p.getZiroRacun());
            stmt.setString(8, p.getLogoPutanja());
            stmt.setInt(9, p.isPdvObveznik() ? 1 : 0);
            stmt.setDouble(10, p.getPdvStopa());
            stmt.setInt(11, p.getDefaultRokPlacanja());
            stmt.setString(12, p.getGmailAdresa());
            stmt.setString(13, p.getGmailAppPassword());
            stmt.setString(14, p.getEmailTekstPredracun());
            stmt.setString(15, p.getEmailTekstFaktura());
            stmt.setString(16, p.getEmailNaslovPredracun());
            stmt.setString(17, p.getEmailNaslovFaktura());
            stmt.setString(18, p.getEmailZaglavlje());
            stmt.setString(19, p.getEmailFooter());
            stmt.setString(20, p.getDriverPutanja());
            stmt.setInt(21, p.getPocetniBrojNaloga());
            stmt.setInt(22, p.getPocetniBrojFakture());
            stmt.setInt(23, p.getPocetniBrojPredracuna());
            stmt.setString(24, p.getOdgovornoLice());
            stmt.executeUpdate();
        }
    }

    private Podesavanja mapiraj(ResultSet rs) throws SQLException {
        Podesavanja p = new Podesavanja();
        p.setId(rs.getInt("id"));
        p.setNazivFirme(rs.getString("naziv_firme"));
        p.setAdresa(rs.getString("adresa"));
        p.setTelefon(rs.getString("telefon"));
        p.setEmail(rs.getString("email"));
        p.setPib(rs.getString("pib"));
        p.setMaticniBroj(rs.getString("maticni_broj"));
        p.setZiroRacun(rs.getString("žiro_racun"));
        p.setLogoPutanja(rs.getString("logo_putanja"));
        try { p.setPdvObveznik(rs.getInt("pdv_obveznik") == 1); } catch (SQLException ignored) {}
        try { p.setPdvStopa(rs.getDouble("pdv_stopa")); } catch (SQLException ignored) {}
        try { p.setDefaultRokPlacanja(rs.getInt("default_rok_placanja")); } catch (SQLException ignored) {}
        try { p.setGmailAdresa(rs.getString("gmail_adresa")); } catch (SQLException ignored) {}
        try { p.setGmailAppPassword(rs.getString("gmail_app_password")); } catch (SQLException ignored) {}
        try { p.setEmailTekstPredracun(rs.getString("email_tekst_predracun")); } catch (SQLException ignored) {}
        try { p.setEmailTekstFaktura(rs.getString("email_tekst_faktura")); } catch (SQLException ignored) {}
        try { p.setEmailNaslovPredracun(rs.getString("email_naslov_predracun")); } catch (SQLException ignored) {}
        try { p.setEmailNaslovFaktura(rs.getString("email_naslov_faktura")); } catch (SQLException ignored) {}
        try { p.setEmailZaglavlje(rs.getString("email_zaglavlje")); } catch (SQLException ignored) {}
        try { p.setEmailFooter(rs.getString("email_footer")); } catch (SQLException ignored) {}
        try { p.setDriverPutanja(rs.getString("driver_putanja")); } catch (SQLException ignored) {}
        try { p.setPocetniBrojNaloga(rs.getInt("pocetni_broj_naloga")); } catch (SQLException ignored) {}
        try { p.setPocetniBrojFakture(rs.getInt("pocetni_broj_fakture")); } catch (SQLException ignored) {}
        try { p.setPocetniBrojPredracuna(rs.getInt("pocetni_broj_predracuna")); } catch (SQLException ignored) {}
        try { p.setOdgovornoLice(rs.getString("odgovorno_lice")); } catch (SQLException ignored) {}
        return p;
    }
}
