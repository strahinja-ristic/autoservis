package autoservis.servis.model;

public class UlazDokument {

    private int id;
    private String broj;
    private String datum;
    private String napomena;
    private int brojStavki;
    private Integer dobavljacId;
    private String dobavljacNaziv;
    private String brojOtpremnice;

    public UlazDokument() {}

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

    public Integer getDobavljacId() { return dobavljacId; }
    public void setDobavljacId(Integer dobavljacId) { this.dobavljacId = dobavljacId; }

    public String getDobavljacNaziv() { return dobavljacNaziv; }
    public void setDobavljacNaziv(String dobavljacNaziv) { this.dobavljacNaziv = dobavljacNaziv; }

    public String getBrojOtpremnice() { return brojOtpremnice; }
    public void setBrojOtpremnice(String brojOtpremnice) { this.brojOtpremnice = brojOtpremnice; }

    @Override
    public String toString() { return broj + " — " + datum; }
}
