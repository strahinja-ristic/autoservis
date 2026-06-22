package autoservis.servis.model;

public class Podesavanja {

    private int id;
    private String nazivFirme;
    private String adresa;
    private String telefon;
    private String email;
    private String pib;
    private String maticniBroj;
    private String ziroRacun;
    private String logoPutanja;
    private boolean pdvObveznik;
    private double pdvStopa = 20.0;
    private int defaultRokPlacanja = 15;
    private String gmailAdresa;
    private String gmailAppPassword;
    private String emailTekstPredracun;
    private String emailTekstFaktura;
    private String emailNaslovPredracun;
    private String emailNaslovFaktura;
    private String emailZaglavlje;
    private String emailFooter;
    private String driverPutanja;
    private int pocetniBrojNaloga = 1;
    private int pocetniBrojFakture = 1;
    private int pocetniBrojPredracuna = 1;
    private String odgovornoLice;

    public Podesavanja() {}

    // Getteri i Setteri
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getLogoPutanja() { return logoPutanja; }
    public void setLogoPutanja(String logoPutanja) { this.logoPutanja = logoPutanja; }

    public String getNazivFirme() { return nazivFirme; }
    public void setNazivFirme(String nazivFirme) { this.nazivFirme = nazivFirme; }

    public String getAdresa() { return adresa; }
    public void setAdresa(String adresa) { this.adresa = adresa; }

    public String getTelefon() { return telefon; }
    public void setTelefon(String telefon) { this.telefon = telefon; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPib() { return pib; }
    public void setPib(String pib) { this.pib = pib; }

    public String getMaticniBroj() { return maticniBroj; }
    public void setMaticniBroj(String maticniBroj) { this.maticniBroj = maticniBroj; }

    public String getZiroRacun() { return ziroRacun; }
    public void setZiroRacun(String ziroRacun) { this.ziroRacun = ziroRacun; }

    public boolean isPdvObveznik() { return pdvObveznik; }
    public void setPdvObveznik(boolean pdvObveznik) { this.pdvObveznik = pdvObveznik; }

    public double getPdvStopa() { return pdvStopa; }
    public void setPdvStopa(double pdvStopa) { this.pdvStopa = pdvStopa; }

    public int getDefaultRokPlacanja() { return defaultRokPlacanja; }
    public void setDefaultRokPlacanja(int defaultRokPlacanja) { this.defaultRokPlacanja = defaultRokPlacanja; }

    public String getGmailAdresa() { return gmailAdresa; }
    public void setGmailAdresa(String gmailAdresa) { this.gmailAdresa = gmailAdresa; }

    public String getGmailAppPassword() { return gmailAppPassword; }
    public void setGmailAppPassword(String gmailAppPassword) { this.gmailAppPassword = gmailAppPassword; }

    public String getEmailTekstPredracun() { return emailTekstPredracun; }
    public void setEmailTekstPredracun(String emailTekstPredracun) { this.emailTekstPredracun = emailTekstPredracun; }

    public String getEmailTekstFaktura() { return emailTekstFaktura; }
    public void setEmailTekstFaktura(String emailTekstFaktura) { this.emailTekstFaktura = emailTekstFaktura; }

    public String getEmailNaslovPredracun() { return emailNaslovPredracun; }
    public void setEmailNaslovPredracun(String emailNaslovPredracun) { this.emailNaslovPredracun = emailNaslovPredracun; }

    public String getEmailNaslovFaktura() { return emailNaslovFaktura; }
    public void setEmailNaslovFaktura(String emailNaslovFaktura) { this.emailNaslovFaktura = emailNaslovFaktura; }

    public String getEmailZaglavlje() { return emailZaglavlje; }
    public void setEmailZaglavlje(String emailZaglavlje) { this.emailZaglavlje = emailZaglavlje; }

    public String getEmailFooter() { return emailFooter; }
    public void setEmailFooter(String emailFooter) { this.emailFooter = emailFooter; }

    public String getDriverPutanja() { return driverPutanja; }
    public void setDriverPutanja(String driverPutanja) { this.driverPutanja = driverPutanja; }

    public int getPocetniBrojNaloga() { return pocetniBrojNaloga; }
    public void setPocetniBrojNaloga(int pocetniBrojNaloga) { this.pocetniBrojNaloga = pocetniBrojNaloga; }

    public int getPocetniBrojFakture() { return pocetniBrojFakture; }
    public void setPocetniBrojFakture(int pocetniBrojFakture) { this.pocetniBrojFakture = pocetniBrojFakture; }

    public int getPocetniBrojPredracuna() { return pocetniBrojPredracuna; }
    public void setPocetniBrojPredracuna(int pocetniBrojPredracuna) { this.pocetniBrojPredracuna = pocetniBrojPredracuna; }

    public String getOdgovornoLice() { return odgovornoLice; }
    public void setOdgovornoLice(String odgovornoLice) { this.odgovornoLice = odgovornoLice; }

    @Override
    public String toString() {
        return nazivFirme;
    }
}