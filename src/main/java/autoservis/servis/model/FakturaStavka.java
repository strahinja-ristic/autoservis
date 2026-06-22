package autoservis.servis.model;

public class FakturaStavka {

    private int id;
    private int fakturaId;
    private String tip;
    private String naziv;
    private double kolicina;
    private String jedinicaMere;
    private double cenaBezPdv;
    private double pdvStopa;
    private double popustProcenat;
    private int redniBroj;

    public FakturaStavka() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getFakturaId() { return fakturaId; }
    public void setFakturaId(int fakturaId) { this.fakturaId = fakturaId; }

    public String getTip() { return tip; }
    public void setTip(String tip) { this.tip = tip; }

    public String getNaziv() { return naziv; }
    public void setNaziv(String naziv) { this.naziv = naziv; }

    public double getKolicina() { return kolicina; }
    public void setKolicina(double kolicina) { this.kolicina = kolicina; }

    public String getJedinicaMere() { return jedinicaMere; }
    public void setJedinicaMere(String jedinicaMere) { this.jedinicaMere = jedinicaMere; }

    public double getCenaBezPdv() { return cenaBezPdv; }
    public void setCenaBezPdv(double cenaBezPdv) { this.cenaBezPdv = cenaBezPdv; }

    public double getPdvStopa() { return pdvStopa; }
    public void setPdvStopa(double pdvStopa) { this.pdvStopa = pdvStopa; }

    public double getPopustProcenat() { return popustProcenat; }
    public void setPopustProcenat(double popustProcenat) { this.popustProcenat = popustProcenat; }

    public int getRedniBroj() { return redniBroj; }
    public void setRedniBroj(int redniBroj) { this.redniBroj = redniBroj; }

    public double iznosBezPdv() {
        return kolicina * cenaBezPdv * (1.0 - popustProcenat / 100.0);
    }

    public double iznosPdv() {
        return iznosBezPdv() * pdvStopa / 100.0;
    }

    public double iznosUkupno() {
        return iznosBezPdv() + iznosPdv();
    }
}
