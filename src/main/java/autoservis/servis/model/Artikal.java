package autoservis.servis.model;

public class Artikal {

    private int id;
    private String naziv;
    private Integer sifra;
    private String jedinicaMere;
    private Double kolicina;
    private Double nabavnaCena;
    private double prodajnaCena;
    private Double minimalnaKolicina;
    private boolean arhiviran;
    private String vrsta = "Artikal";

    public Artikal() {}

    // Getteri i Setteri
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNaziv() { return naziv; }
    public void setNaziv(String naziv) { this.naziv = naziv; }

    public Integer getSifra() { return sifra; }
    public void setSifra(Integer sifra) { this.sifra = sifra; }

    public String getJedinicaMere() { return jedinicaMere; }
    public void setJedinicaMere(String jedinicaMere) { this.jedinicaMere = jedinicaMere; }

    public Double getKolicina() { return kolicina; }
    public void setKolicina(Double kolicina) { this.kolicina = kolicina; }

    public Double getNabavnaCena() { return nabavnaCena; }
    public void setNabavnaCena(Double nabavnaCena) { this.nabavnaCena = nabavnaCena; }

    public double getProdajnaCena() { return prodajnaCena; }
    public void setProdajnaCena(double prodajnaCena) { this.prodajnaCena = prodajnaCena; }

    public Double getMinimalnaKolicina() { return minimalnaKolicina; }
    public void setMinimalnaKolicina(Double minimalnaKolicina) { this.minimalnaKolicina = minimalnaKolicina; }

    public boolean isUsluga() { return "Usluga".equals(vrsta); }

    public boolean isArhiviran() { return arhiviran; }
    public void setArhiviran(boolean arhiviran) { this.arhiviran = arhiviran; }

    public String getVrsta() { return vrsta; }
    public void setVrsta(String vrsta) { this.vrsta = vrsta; }

    public boolean isIspodMinimuma() {
        return kolicina != null && minimalnaKolicina != null && kolicina < minimalnaKolicina;
    }

    @Override
    public String toString() {
        if (isUsluga()) return naziv;
        return naziv + " (" + kolicina + " " + jedinicaMere + ")";
    }
}