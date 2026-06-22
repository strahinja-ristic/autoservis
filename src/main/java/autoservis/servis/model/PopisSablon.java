package autoservis.servis.model;

public class PopisSablon {

    private int id;
    private String naziv;
    private int brojArtikala;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNaziv() { return naziv; }
    public void setNaziv(String naziv) { this.naziv = naziv; }

    public int getBrojArtikala() { return brojArtikala; }
    public void setBrojArtikala(int brojArtikala) { this.brojArtikala = brojArtikala; }

    @Override
    public String toString() { return naziv; }
}
