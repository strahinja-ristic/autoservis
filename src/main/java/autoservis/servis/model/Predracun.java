package autoservis.servis.model;

import java.util.ArrayList;
import java.util.List;

public class Predracun {

    private int id;
    private String brojPredracuna;
    private int klijentId;
    private Integer voziloId;
    private Integer radniNalogId;
    private String datumKreiranja;
    private String datumVazenja;
    private String status; // Kreiran/Poslat/Prihvaćen/Odbijen/Istekao/Fakturisan
    private String nacinPlacanja; // Gotovina/Kartica/Prenos
    private String rokPlacanja;
    private String mestoIzdavanja;
    private String napomena;
    private double popustProcenat;
    private boolean arhiviran;

    private List<PredracunStavka> stavke = new ArrayList<>();

    private String klijentIme = "";
    private double iznosZaUplatu = 0;

    public Predracun() {}

    public String getKlijentIme() { return klijentIme; }
    public void setKlijentIme(String klijentIme) { this.klijentIme = klijentIme != null ? klijentIme : ""; }

    public double getIznosZaUplatu() { return iznosZaUplatu; }
    public void setIznosZaUplatu(double iznosZaUplatu) { this.iznosZaUplatu = iznosZaUplatu; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getBrojPredracuna() { return brojPredracuna; }
    public void setBrojPredracuna(String brojPredracuna) { this.brojPredracuna = brojPredracuna; }

    public int getKlijentId() { return klijentId; }
    public void setKlijentId(int klijentId) { this.klijentId = klijentId; }

    public Integer getVoziloId() { return voziloId; }
    public void setVoziloId(Integer voziloId) { this.voziloId = voziloId; }

    public Integer getRadniNalogId() { return radniNalogId; }
    public void setRadniNalogId(Integer radniNalogId) { this.radniNalogId = radniNalogId; }

    public String getDatumKreiranja() { return datumKreiranja; }
    public void setDatumKreiranja(String datumKreiranja) { this.datumKreiranja = datumKreiranja; }

    public String getDatumVazenja() { return datumVazenja; }
    public void setDatumVazenja(String datumVazenja) { this.datumVazenja = datumVazenja; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getNacinPlacanja() { return nacinPlacanja; }
    public void setNacinPlacanja(String nacinPlacanja) { this.nacinPlacanja = nacinPlacanja; }

    public String getRokPlacanja() { return rokPlacanja; }
    public void setRokPlacanja(String rokPlacanja) { this.rokPlacanja = rokPlacanja; }

    public String getMestoIzdavanja() { return mestoIzdavanja; }
    public void setMestoIzdavanja(String mestoIzdavanja) { this.mestoIzdavanja = mestoIzdavanja; }

    public String getNapomena() { return napomena; }
    public void setNapomena(String napomena) { this.napomena = napomena; }

    public double getPopustProcenat() { return popustProcenat; }
    public void setPopustProcenat(double popustProcenat) { this.popustProcenat = popustProcenat; }

    public boolean isArhiviran() { return arhiviran; }
    public void setArhiviran(boolean arhiviran) { this.arhiviran = arhiviran; }

    public List<PredracunStavka> getStavke() { return stavke; }
    public void setStavke(List<PredracunStavka> stavke) { this.stavke = stavke; }

    public double ukupnoBezPdv() {
        return stavke.stream().mapToDouble(PredracunStavka::iznosBezPdv).sum();
    }

    public double ukupniPdv() {
        return stavke.stream().mapToDouble(PredracunStavka::iznosPdv).sum();
    }

    public double globalniPopustIznos() {
        return (ukupnoBezPdv() + ukupniPdv()) * popustProcenat / 100.0;
    }

    public double zaUplatu() {
        return ukupnoBezPdv() + ukupniPdv() - globalniPopustIznos();
    }

    @Override
    public String toString() {
        return brojPredracuna != null ? brojPredracuna : "";
    }
}
