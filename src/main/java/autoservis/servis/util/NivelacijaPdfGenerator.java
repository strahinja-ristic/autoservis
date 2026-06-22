package autoservis.servis.util;

import autoservis.servis.dao.PodesavanjaDao;
import autoservis.servis.model.Nivelacija;
import autoservis.servis.model.NivelacijaStavka;
import autoservis.servis.model.Podesavanja;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

public class NivelacijaPdfGenerator {

    private static final BaseColor CRNA        = new BaseColor(0, 0, 0);
    private static final BaseColor SIVA        = new BaseColor(85, 85, 85);
    private static final BaseColor SVETLO_SIVA = new BaseColor(249, 249, 249);
    private static final BaseColor BORDER_SIVA = new BaseColor(204, 204, 204);
    private static final BaseColor HEADER_BG   = new BaseColor(240, 244, 250);

    private static final BaseFont BF;
    private static final BaseFont BF_BOLD;

    static {
        try {
            BF      = BaseFont.createFont(BaseFont.HELVETICA,      "Cp1250", BaseFont.NOT_EMBEDDED);
            BF_BOLD = BaseFont.createFont(BaseFont.HELVETICA_BOLD, "Cp1250", BaseFont.NOT_EMBEDDED);
        } catch (Exception e) {
            throw new RuntimeException("Greška pri učitavanju fontova: " + e.getMessage(), e);
        }
    }

    private static final Font FONT_FIRMA   = new Font(BF_BOLD, 14, Font.NORMAL, CRNA);
    private static final Font FONT_INFO    = new Font(BF,       9, Font.NORMAL, SIVA);
    private static final Font FONT_NASLOV  = new Font(BF_BOLD, 16, Font.NORMAL, CRNA);
    private static final Font FONT_DATUM   = new Font(BF,      10, Font.NORMAL, SIVA);
    private static final Font FONT_TH      = new Font(BF_BOLD,  9, Font.NORMAL, CRNA);
    private static final Font FONT_TD      = new Font(BF,       9, Font.NORMAL, CRNA);
    private static final Font FONT_FOOTER  = new Font(BF,       9, Font.NORMAL, SIVA);
    private static final Font FONT_TOTAL   = new Font(BF_BOLD, 10, Font.NORMAL, CRNA);

    public static void generisi(Nivelacija niv, List<NivelacijaStavka> stavke) throws Exception {
        Podesavanja firma;
        try { firma = new PodesavanjaDao().vratiPodesavanja(); } catch (Exception e) { firma = new Podesavanja(); }

        FileChooser fc = new FileChooser();
        fc.setTitle("Sačuvaj nivelaciju cena");
        fc.setInitialFileName("Nivelacija_" + niv.getBroj().replace("/", "-") + ".pdf");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF fajlovi", "*.pdf"));
        File izabrani = fc.showSaveDialog(new Stage());
        if (izabrani == null) return;

        generisiNaPutanju(niv, stavke, firma, izabrani.getAbsolutePath());

        javafx.scene.control.Alert info = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        info.setTitle("Uspešno");
        info.setHeaderText(null);
        info.setContentText("PDF je sačuvan:\n" + izabrani.getAbsolutePath());
        info.showAndWait();
    }

    public static void generisiNaPutanju(Nivelacija niv, List<NivelacijaStavka> stavke,
                                          Podesavanja firma, String putanja) throws Exception {
        Document doc = new Document(PageSize.A4.rotate(), 28, 28, 25, 25);
        PdfWriter.getInstance(doc, new FileOutputStream(putanja));
        doc.open();

        // ── HEADER ──────────────────────────────────────────────
        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{60, 40});
        header.setSpacingAfter(6);

        PdfPCell cellFirma = new PdfPCell();
        cellFirma.setBorder(Rectangle.NO_BORDER);
        cellFirma.setPadding(0);
        String naziv = firma.getNazivFirme() != null ? firma.getNazivFirme() : "Moj Servis";
        cellFirma.addElement(new Paragraph(naziv, FONT_FIRMA));

        StringBuilder info = new StringBuilder();
        if (firma.getAdresa() != null && !firma.getAdresa().isBlank()) info.append(firma.getAdresa()).append("\n");
        if (firma.getTelefon() != null && !firma.getTelefon().isBlank()) info.append("Tel: ").append(firma.getTelefon()).append("\n");
        if (firma.getPib() != null && !firma.getPib().isBlank()) info.append("PIB: ").append(firma.getPib());
        if (!info.isEmpty()) {
            Paragraph pInfo = new Paragraph(info.toString(), FONT_INFO);
            pInfo.setLeading(11);
            pInfo.setSpacingBefore(3);
            cellFirma.addElement(pInfo);
        }

        PdfPCell cellDesno = new PdfPCell();
        cellDesno.setBorder(Rectangle.NO_BORDER);
        cellDesno.setPadding(0);
        cellDesno.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Paragraph pNaslov = new Paragraph("NIVELACIJA CENA", FONT_NASLOV);
        pNaslov.setAlignment(Element.ALIGN_RIGHT);
        cellDesno.addElement(pNaslov);
        Paragraph pBroj = new Paragraph("Broj: " + niv.getBroj(), FONT_DATUM);
        pBroj.setAlignment(Element.ALIGN_RIGHT);
        pBroj.setSpacingBefore(2);
        cellDesno.addElement(pBroj);
        Paragraph pDatum = new Paragraph("Datum: " + niv.getDatum(), FONT_DATUM);
        pDatum.setAlignment(Element.ALIGN_RIGHT);
        pDatum.setSpacingBefore(2);
        cellDesno.addElement(pDatum);

        header.addCell(cellFirma);
        header.addCell(cellDesno);
        doc.add(header);

        // Linija
        PdfPTable linija = new PdfPTable(1);
        linija.setWidthPercentage(100);
        linija.setSpacingAfter(8);
        PdfPCell lc = new PdfPCell();
        lc.setBorderWidthTop(2f); lc.setBorderWidthBottom(0);
        lc.setBorderWidthLeft(0); lc.setBorderWidthRight(0);
        lc.setBorderColorTop(CRNA); lc.setFixedHeight(1);
        linija.addCell(lc);
        doc.add(linija);

        if (niv.getNapomena() != null && !niv.getNapomena().isBlank()) {
            Paragraph pNap = new Paragraph("Napomena: " + niv.getNapomena(), FONT_INFO);
            pNap.setSpacingAfter(6);
            doc.add(pNap);
        }

        // ── TABELA ──────────────────────────────────────────────
        // #, Šifra, Naziv, JM, Na stanju, Stara cena, Vred. stara, Nova cena, Vred. nova, Razlika
        PdfPTable tabela = new PdfPTable(new float[]{4, 7, 26, 5, 9, 10, 10, 10, 10, 10});
        tabela.setWidthPercentage(100);
        tabela.setSpacingAfter(10);

        tabela.addCell(kreirajTH("#"));
        tabela.addCell(kreirajTH("Šifra"));
        tabela.addCell(kreirajTH("Naziv"));
        tabela.addCell(kreirajTH("JM"));
        tabela.addCell(kreirajTH("Na stanju"));
        tabela.addCell(kreirajTH("Stara cena"));
        tabela.addCell(kreirajTH("Vred. stara"));
        tabela.addCell(kreirajTH("Nova cena"));
        tabela.addCell(kreirajTH("Vred. nova"));
        tabela.addCell(kreirajTH("Razlika"));

        double ukupnoRazlika = 0;
        double ukupnoStara = 0;
        double ukupnoNova = 0;

        for (int i = 0; i < stavke.size(); i++) {
            NivelacijaStavka s = stavke.get(i);
            BaseColor boja = (i % 2 == 0) ? BaseColor.WHITE : SVETLO_SIVA;

            String kolStr = (s.isUsluga() || s.getKolicinaStanju() == null) ? "—"
                    : fmtKol(s.getKolicinaStanju()) + " " + (s.getJedinicaMere() != null ? s.getJedinicaMere() : "");
            String sifraStr = s.getSifraArtikla() != null ? String.valueOf(s.getSifraArtikla()) : "";
            Double vrStara = s.getVrednostPoStaroj();
            Double vrNova  = s.getVrednostPoNovoj();
            Double razlika = s.getRazlika();

            if (vrStara != null) ukupnoStara   += vrStara;
            if (vrNova  != null) ukupnoNova    += vrNova;
            if (razlika != null) ukupnoRazlika += razlika;

            tabela.addCell(kreirajTD(String.valueOf(i + 1), boja, Element.ALIGN_CENTER));
            tabela.addCell(kreirajTD(sifraStr, boja, Element.ALIGN_LEFT));
            tabela.addCell(kreirajTD(s.getNazivArtikla(), boja, Element.ALIGN_LEFT));
            tabela.addCell(kreirajTD(s.getJedinicaMere() != null ? s.getJedinicaMere() : "", boja, Element.ALIGN_CENTER));
            tabela.addCell(kreirajTD(s.isUsluga() || s.getKolicinaStanju() == null ? "—" : fmtKol(s.getKolicinaStanju()), boja, Element.ALIGN_RIGHT));
            tabela.addCell(kreirajTD(fmtCena(s.getStaraCena()), boja, Element.ALIGN_RIGHT));
            tabela.addCell(kreirajTD(vrStara == null ? "—" : fmtCena(vrStara), boja, Element.ALIGN_RIGHT));
            tabela.addCell(kreirajTD(fmtCena(s.getNovaCena()), boja, Element.ALIGN_RIGHT));
            tabela.addCell(kreirajTD(vrNova == null ? "—" : fmtCena(vrNova), boja, Element.ALIGN_RIGHT));
            tabela.addCell(kreirajTD(razlika == null ? "—" : fmtCena(razlika), boja, Element.ALIGN_RIGHT));
        }

        doc.add(tabela);

        // ── SUMARNI RED ──────────────────────────────────────────
        PdfPTable sumaTable = new PdfPTable(new float[]{55, 15, 15, 15});
        sumaTable.setWidthPercentage(100);
        sumaTable.setSpacingAfter(14);

        PdfPCell prazno = new PdfPCell(new Phrase("Ukupno:", FONT_TOTAL));
        prazno.setBorder(Rectangle.NO_BORDER); prazno.setPadding(4);
        prazno.setHorizontalAlignment(Element.ALIGN_RIGHT);
        sumaTable.addCell(prazno);

        sumaTable.addCell(kreirajSuma(fmtCena(ukupnoStara)));
        sumaTable.addCell(kreirajSuma(fmtCena(ukupnoNova)));
        sumaTable.addCell(kreirajSuma(fmtCena(ukupnoRazlika)));
        doc.add(sumaTable);

        // ── FOOTER ───────────────────────────────────────────────
        PdfPTable footer = new PdfPTable(3);
        footer.setWidthPercentage(100);
        footer.setSpacingBefore(20);

        String odgovornoLice = firma.getOdgovornoLice() != null && !firma.getOdgovornoLice().isBlank()
                ? firma.getOdgovornoLice() : "_______________________";
        footer.addCell(kreirajFooterCelija("Odgovorno lice: " + odgovornoLice));
        footer.addCell(kreirajFooterCelija("Datum i mesto: _______________________"));
        footer.addCell(kreirajFooterCelija("Potpis i pečat: _______________________"));
        doc.add(footer);

        doc.close();
    }

    private static PdfPCell kreirajTH(String tekst) {
        PdfPCell c = new PdfPCell(new Phrase(tekst, FONT_TH));
        c.setPadding(4); c.setBorderColor(CRNA); c.setBorderWidth(0.5f);
        c.setBackgroundColor(HEADER_BG);
        return c;
    }

    private static PdfPCell kreirajTD(String tekst, BaseColor boja, int align) {
        PdfPCell c = new PdfPCell(new Phrase(tekst, FONT_TD));
        c.setPadding(4); c.setBorderColor(BORDER_SIVA); c.setBorderWidth(0.5f);
        c.setBackgroundColor(boja); c.setHorizontalAlignment(align);
        c.setMinimumHeight(18);
        return c;
    }

    private static PdfPCell kreirajSuma(String tekst) {
        PdfPCell c = new PdfPCell(new Phrase(tekst, FONT_TOTAL));
        c.setPadding(4); c.setBorderColor(CRNA); c.setBorderWidth(0.8f);
        c.setHorizontalAlignment(Element.ALIGN_RIGHT);
        return c;
    }

    private static PdfPCell kreirajFooterCelija(String tekst) {
        PdfPCell c = new PdfPCell(new Phrase(tekst, FONT_FOOTER));
        c.setBorder(Rectangle.NO_BORDER); c.setPadding(4);
        return c;
    }


    private static String fmtKol(double d) {
        return d == Math.floor(d) ? String.valueOf((int) d) : String.format("%.2f", d);
    }

    private static String fmtCena(double d) {
        return String.format("%.2f", d);
    }
}
