package autoservis.servis.model;

public class NalogArtikal {

    private int id;
    private int radniNalogId;
    private int artikalId;
    private String nazivArtikla;
    private double kolicina;
    private String jedinicaMere;

    public NalogArtikal() {}

    // Getteri i Setteri
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getRadniNalogId() { return radniNalogId; }
    public void setRadniNalogId(int radniNalogId) { this.radniNalogId = radniNalogId; }

    public int getArtikalId() { return artikalId; }
    public void setArtikalId(int artikalId) { this.artikalId = artikalId; }

    public String getNazivArtikla() { return nazivArtikla; }
    public void setNazivArtikla(String nazivArtikla) { this.nazivArtikla = nazivArtikla; }

    public double getKolicina() { return kolicina; }
    public void setKolicina(double kolicina) { this.kolicina = kolicina; }

    public String getJedinicaMere() { return jedinicaMere; }
    public void setJedinicaMere(String jedinicaMere) { this.jedinicaMere = jedinicaMere; }

    @Override
    public String toString() {
        return nazivArtikla + " - " + kolicina + " " + jedinicaMere;
    }
}