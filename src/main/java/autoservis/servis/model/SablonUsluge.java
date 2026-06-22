package autoservis.servis.model;

public class SablonUsluge {

    private int id;
    private String naziv;
    private double cena;
    private boolean arhiviran;

    public SablonUsluge() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNaziv() { return naziv; }
    public void setNaziv(String naziv) { this.naziv = naziv; }

    public double getCena() { return cena; }
    public void setCena(double cena) { this.cena = cena; }

    public boolean isArhiviran() { return arhiviran; }
    public void setArhiviran(boolean arhiviran) { this.arhiviran = arhiviran; }

    @Override
    public String toString() {
        return naziv;
    }
}