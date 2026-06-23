package autoservis.servis.model;

public class Vozilo {

    private int id;
    private int klijentId;
    private String marka;
    private String model;
    private Integer godiste;
    private String registracija;
    private String brojSasije;
    private int kilometraza;
    private boolean arhivirano;
    private String napomena;

    public Vozilo() {}

    // Getteri i Setteri
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getKlijentId() { return klijentId; }
    public void setKlijentId(int klijentId) { this.klijentId = klijentId; }

    public String getMarka() { return marka; }
    public void setMarka(String marka) { this.marka = marka; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public Integer getGodiste() { return godiste; }
    public void setGodiste(Integer godiste) { this.godiste = godiste; }

    public String getRegistracija() { return registracija; }
    public void setRegistracija(String registracija) { this.registracija = registracija; }

    public String getBrojSasije() { return brojSasije; }
    public void setBrojSasije(String brojSasije) { this.brojSasije = brojSasije; }

    public int getKilometraza() { return kilometraza; }
    public void setKilometraza(int kilometraza) { this.kilometraza = kilometraza; }

    public boolean isArhivirano() { return arhivirano; }
    public void setArhivirano(boolean arhivirano) { this.arhivirano = arhivirano; }

    public String getNapomena() { return napomena; }
    public void setNapomena(String napomena) { this.napomena = napomena; }

    @Override
    public String toString() {
        String m = marka != null ? marka : "";
        String mo = model != null ? model : "";
        String reg = registracija != null && !registracija.isBlank() ? " (" + registracija + ")" : "";
        return (m + " " + mo).trim() + reg;
    }
}