package autoservis.servis.model;

import java.util.ArrayList;
import java.util.List;

public class RadniNalog {

    private int id;
    private String brojNaloga;
    private int voziloId;
    private int klijentId;
    private int kilometrazaPrijema;
    private String datumPrijema;
    private String datumZavrsetka;
    private String opisKvara;
    private String zahtevKlijenta;
    private String status; // "Primljeno", "U radu", "Završeno"
    private String ostecenja;
    private Integer sledeciServisKm;
    private String sledeciServisDatum;
    private String napomena;
    private String faktura;
    private boolean arhiviran;

    private List<NalogUsluga> usluge = new ArrayList<>();
    private List<NalogArtikal> artikli = new ArrayList<>();

    private String klijentIme = "";
    private String voziloStr = "";

    public RadniNalog() {}

    // Getteri i Setteri
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getBrojNaloga() { return brojNaloga; }
    public void setBrojNaloga(String brojNaloga) { this.brojNaloga = brojNaloga; }

    public int getVoziloId() { return voziloId; }
    public void setVoziloId(int voziloId) { this.voziloId = voziloId; }

    public int getKlijentId() { return klijentId; }
    public void setKlijentId(int klijentId) { this.klijentId = klijentId; }

    public int getKilometrazaPrijema() { return kilometrazaPrijema; }
    public void setKilometrazaPrijema(int kilometrazaPrijema) { this.kilometrazaPrijema = kilometrazaPrijema; }

    public String getDatumPrijema() { return datumPrijema; }
    public void setDatumPrijema(String datumPrijema) { this.datumPrijema = datumPrijema; }

    public String getDatumZavrsetka() { return datumZavrsetka; }
    public void setDatumZavrsetka(String datumZavrsetka) { this.datumZavrsetka = datumZavrsetka; }

    public String getOpisKvara() { return opisKvara; }
    public void setOpisKvara(String opisKvara) { this.opisKvara = opisKvara; }

    public String getZahtevKlijenta() { return zahtevKlijenta; }
    public void setZahtevKlijenta(String zahtevKlijenta) { this.zahtevKlijenta = zahtevKlijenta; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getOstecenja() { return ostecenja; }
    public void setOstecenja(String ostecenja) { this.ostecenja = ostecenja; }

    public Integer getSledeciServisKm() { return sledeciServisKm; }
    public void setSledeciServisKm(Integer sledeciServisKm) { this.sledeciServisKm = sledeciServisKm; }

    public String getSledeciServisDatum() { return sledeciServisDatum; }
    public void setSledeciServisDatum(String sledeciServisDatum) { this.sledeciServisDatum = sledeciServisDatum; }

    public String getNapomena() { return napomena; }
    public void setNapomena(String napomena) { this.napomena = napomena; }

    public String getFaktura() { return faktura; }
    public void setFaktura(String faktura) { this.faktura = faktura; }

    public boolean isArhiviran() { return arhiviran; }
    public void setArhiviran(boolean arhiviran) { this.arhiviran = arhiviran; }

    public List<NalogUsluga> getUsluge() { return usluge; }
    public void setUsluge(List<NalogUsluga> usluge) { this.usluge = usluge; }

    public List<NalogArtikal> getArtikli() { return artikli; }
    public void setArtikli(List<NalogArtikal> artikli) { this.artikli = artikli; }

    public String getKlijentIme() { return klijentIme; }
    public void setKlijentIme(String klijentIme) { this.klijentIme = klijentIme != null ? klijentIme : ""; }

    public String getVoziloStr() { return voziloStr; }
    public void setVoziloStr(String voziloStr) { this.voziloStr = voziloStr != null ? voziloStr : ""; }

    @Override
    public String toString() {
        return brojNaloga + " - " + status;
    }
}