package autoservis.servis.model;

import java.util.List;

public class Nivelacija {

    private int id;
    private String broj;
    private String datum;
    private String napomena;
    private int brojStavki;
    private List<NivelacijaStavka> stavke;

    public Nivelacija() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getBroj() { return broj; }
    public void setBroj(String broj) { this.broj = broj; }

    public String getDatum() { return datum; }
    public void setDatum(String datum) { this.datum = datum; }

    public String getNapomena() { return napomena; }
    public void setNapomena(String napomena) { this.napomena = napomena; }

    public int getBrojStavki() { return brojStavki; }
    public void setBrojStavki(int brojStavki) { this.brojStavki = brojStavki; }

    public List<NivelacijaStavka> getStavke() { return stavke; }
    public void setStavke(List<NivelacijaStavka> stavke) { this.stavke = stavke; }

    @Override
    public String toString() { return broj + " — " + datum; }
}
