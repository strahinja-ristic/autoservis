package autoservis.servis.model;

public class NivelacijaStavka {

    private int id;
    private int nivelacijaId;
    private int artikalId;
    private String nazivArtikla;
    private Integer sifraArtikla;
    private String jedinicaMere;
    private String vrsta;
    private Double kolicinaStanju;
    private double staraCena;
    private double novaCena;

    public NivelacijaStavka() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getNivelacijaId() { return nivelacijaId; }
    public void setNivelacijaId(int nivelacijaId) { this.nivelacijaId = nivelacijaId; }

    public int getArtikalId() { return artikalId; }
    public void setArtikalId(int artikalId) { this.artikalId = artikalId; }

    public String getNazivArtikla() { return nazivArtikla; }
    public void setNazivArtikla(String nazivArtikla) { this.nazivArtikla = nazivArtikla; }

    public Integer getSifraArtikla() { return sifraArtikla; }
    public void setSifraArtikla(Integer sifraArtikla) { this.sifraArtikla = sifraArtikla; }

    public String getJedinicaMere() { return jedinicaMere; }
    public void setJedinicaMere(String jedinicaMere) { this.jedinicaMere = jedinicaMere; }

    public String getVrsta() { return vrsta; }
    public void setVrsta(String vrsta) { this.vrsta = vrsta; }

    public Double getKolicinaStanju() { return kolicinaStanju; }
    public void setKolicinaStanju(Double kolicinaStanju) { this.kolicinaStanju = kolicinaStanju; }

    public double getStaraCena() { return staraCena; }
    public void setStaraCena(double staraCena) { this.staraCena = staraCena; }

    public double getNovaCena() { return novaCena; }
    public void setNovaCena(double novaCena) { this.novaCena = novaCena; }

    public boolean isUsluga() { return "Usluga".equals(vrsta); }

    public Double getVrednostPoStaroj() {
        if (isUsluga() || kolicinaStanju == null) return null;
        return kolicinaStanju * staraCena;
    }

    public Double getVrednostPoNovoj() {
        if (isUsluga() || kolicinaStanju == null) return null;
        return kolicinaStanju * novaCena;
    }

    public Double getRazlika() {
        Double stara = getVrednostPoStaroj();
        Double nova = getVrednostPoNovoj();
        if (stara == null || nova == null) return null;
        return nova - stara;
    }
}
