package autoservis.servis.model;

public class PredracunStavka {

    private int id;
    private int predracunId;
    private String tip; // Usluga/Artikal
    private String naziv;
    private double kolicina;
    private String jedinicaMere;
    private double cenaBezPdv;
    private double pdvStopa;
    private double popustProcenat;
    private int redniBroj;

    public PredracunStavka() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPredracunId() { return predracunId; }
    public void setPredracunId(int predracunId) { this.predracunId = predracunId; }

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
