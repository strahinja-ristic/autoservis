package autoservis.servis.model;

public class Klijent {

    private int id;
    private String tip; // "Fizičko" ili "Pravno"
    private String ime;
    private String prezime;
    private String nazivFirme;
    private String pib;
    private String maticniBroj;
    private String adresa;
    private String telefon;
    private String email;
    private boolean arhiviran;
    private String napomena;

    public Klijent() {}

    // Getteri i Setteri
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTip() { return tip; }
    public void setTip(String tip) { this.tip = tip; }

    public String getIme() { return ime; }
    public void setIme(String ime) { this.ime = ime; }

    public String getPrezime() { return prezime; }
    public void setPrezime(String prezime) { this.prezime = prezime; }

    public String getNazivFirme() { return nazivFirme; }
    public void setNazivFirme(String nazivFirme) { this.nazivFirme = nazivFirme; }

    public String getPib() { return pib; }
    public void setPib(String pib) { this.pib = pib; }

    public String getMaticniBroj() { return maticniBroj; }
    public void setMaticniBroj(String maticniBroj) { this.maticniBroj = maticniBroj; }

    public String getNapomena() { return napomena; }
    public void setNapomena(String napomena) { this.napomena = napomena; }

    public String getAdresa() { return adresa; }
    public void setAdresa(String adresa) { this.adresa = adresa; }

    public String getTelefon() { return telefon; }
    public void setTelefon(String telefon) { this.telefon = telefon; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public boolean isArhiviran() { return arhiviran; }
    public void setArhiviran(boolean arhiviran) { this.arhiviran = arhiviran; }

    // Pomocna metoda - vraca puno ime za prikaz u listi
    public String getPunoIme() {
        if ("Pravno".equals(tip)) {
            return nazivFirme;
        }
        return ime + " " + prezime;
    }

    @Override
    public String toString() {
        return getPunoIme();
    }
}