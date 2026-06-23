package autoservis.servis.model;

import java.util.ArrayList;
import java.util.List;

public class Faktura {

    private int id;
    private String brojFakture;
    private int klijentId;
    private Integer voziloId;
    private Integer radniNalogId;
    private Integer predracunId;
    private String datumKreiranja;
    private String datumPlacanja;
    private String status; // Kreirana/Poslata/Plaćena/Stornirana
    private String nacinPlacanja;
    private String rokPlacanja;
    private String mestoIzdavanja;
    private String mestoIsporuke;
    private String napomena;
    private double popustProcenat;
    private boolean arhiviran;
    private String brojRacuna;
    private String pfrBroj;

    private List<FakturaStavka> stavke = new ArrayList<>();

    private String klijentIme = "";
    private double iznosZaUplatu = 0;

    public Faktura() {}

    public String getKlijentIme() { return klijentIme; }
    public void setKlijentIme(String klijentIme) { this.klijentIme = klijentIme != null ? klijentIme : ""; }

    public double getIznosZaUplatu() { return iznosZaUplatu; }
    public void setIznosZaUplatu(double iznosZaUplatu) { this.iznosZaUplatu = iznosZaUplatu; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getBrojFakture() { return brojFakture; }
    public void setBrojFakture(String brojFakture) { this.brojFakture = brojFakture; }

    public int getKlijentId() { return klijentId; }
    public void setKlijentId(int klijentId) { this.klijentId = klijentId; }

    public Integer getVoziloId() { return voziloId; }
    public void setVoziloId(Integer voziloId) { this.voziloId = voziloId; }

    public Integer getRadniNalogId() { return radniNalogId; }
    public void setRadniNalogId(Integer radniNalogId) { this.radniNalogId = radniNalogId; }

    public Integer getPredracunId() { return predracunId; }
    public void setPredracunId(Integer predracunId) { this.predracunId = predracunId; }

    public String getDatumKreiranja() { return datumKreiranja; }
    public void setDatumKreiranja(String datumKreiranja) { this.datumKreiranja = datumKreiranja; }

    public String getDatumPlacanja() { return datumPlacanja; }
    public void setDatumPlacanja(String datumPlacanja) { this.datumPlacanja = datumPlacanja; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNacinPlacanja() { return nacinPlacanja; }
    public void setNacinPlacanja(String nacinPlacanja) { this.nacinPlacanja = nacinPlacanja; }

    public String getRokPlacanja() { return rokPlacanja; }
    public void setRokPlacanja(String rokPlacanja) { this.rokPlacanja = rokPlacanja; }

    public String getMestoIzdavanja() { return mestoIzdavanja; }
    public void setMestoIzdavanja(String mestoIzdavanja) { this.mestoIzdavanja = mestoIzdavanja; }

    public String getMestoIsporuke() { return mestoIsporuke; }
    public void setMestoIsporuke(String mestoIsporuke) { this.mestoIsporuke = mestoIsporuke; }

    public String getNapomena() { return napomena; }
    public void setNapomena(String napomena) { this.napomena = napomena; }

    public double getPopustProcenat() { return popustProcenat; }
    public void setPopustProcenat(double popustProcenat) { this.popustProcenat = popustProcenat; }

    public boolean isArhiviran() { return arhiviran; }
    public void setArhiviran(boolean arhiviran) { this.arhiviran = arhiviran; }

    public String getBrojRacuna() { return brojRacuna; }
    public void setBrojRacuna(String brojRacuna) { this.brojRacuna = brojRacuna; }

    public String getPfrBroj() { return pfrBroj; }
    public void setPfrBroj(String pfrBroj) { this.pfrBroj = pfrBroj; }

    public List<FakturaStavka> getStavke() { return stavke; }
    public void setStavke(List<FakturaStavka> stavke) { this.stavke = stavke; }

    public double ukupnoBezPdv() {
        return stavke.stream().mapToDouble(FakturaStavka::iznosBezPdv).sum();
    }

    public double ukupniPdv() {
        return stavke.stream().mapToDouble(FakturaStavka::iznosPdv).sum();
    }

    public double globalniPopustIznos() {
        return (ukupnoBezPdv() + ukupniPdv()) * popustProcenat / 100.0;
    }

    public double zaUplatu() {
        return ukupnoBezPdv() + ukupniPdv() - globalniPopustIznos();
    }

    @Override
    public String toString() {
        return brojFakture != null ? brojFakture : "";
    }
}
