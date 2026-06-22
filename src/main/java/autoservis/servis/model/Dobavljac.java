package autoservis.servis.model;

public class Dobavljac {

    private int id;
    private String naziv;
    private String adresa;
    private String kontakt;
    private String pib;
    private boolean arhiviran;

    public Dobavljac() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNaziv() { return naziv; }
    public void setNaziv(String naziv) { this.naziv = naziv; }

    public String getAdresa() { return adresa; }
    public void setAdresa(String adresa) { this.adresa = adresa; }

    public String getKontakt() { return kontakt; }
    public void setKontakt(String kontakt) { this.kontakt = kontakt; }

    public String getPib() { return pib; }
    public void setPib(String pib) { this.pib = pib; }

    public boolean isArhiviran() { return arhiviran; }
    public void setArhiviran(boolean arhiviran) { this.arhiviran = arhiviran; }

    @Override
    public String toString() { return naziv != null ? naziv : ""; }
}
