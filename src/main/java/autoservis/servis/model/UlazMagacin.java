package autoservis.servis.model;

public class UlazMagacin {

    private int id;
    private int artikalId;
    private String nazivArtikla;
    private String jedinicaMere;
    private double kolicina;
    private String datum;
    private String napomena;
    private int dokumentId;

    public UlazMagacin() {}

    // Getteri i Setteri
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getArtikalId() { return artikalId; }
    public void setArtikalId(int artikalId) { this.artikalId = artikalId; }

    public String getNazivArtikla() { return nazivArtikla; }
    public void setNazivArtikla(String nazivArtikla) { this.nazivArtikla = nazivArtikla; }

    public String getJedinicaMere() { return jedinicaMere; }
    public void setJedinicaMere(String jedinicaMere) { this.jedinicaMere = jedinicaMere; }

    public double getKolicina() { return kolicina; }
    public void setKolicina(double kolicina) { this.kolicina = kolicina; }

    public String getDatum() { return datum; }
    public void setDatum(String datum) { this.datum = datum; }

    public String getNapomena() { return napomena; }
    public void setNapomena(String napomena) { this.napomena = napomena; }

    public int getDokumentId() { return dokumentId; }
    public void setDokumentId(int dokumentId) { this.dokumentId = dokumentId; }

    @Override
    public String toString() {
        return datum + " - " + nazivArtikla + " (" + kolicina + ")";
    }
}
