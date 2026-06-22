package autoservis.servis.util;

import autoservis.servis.model.Podesavanja;
import autoservis.servis.model.UlazDokument;
import autoservis.servis.model.UlazMagacin;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.FileOutputStream;
import java.util.List;

public class UlazRobePdfGenerator {

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

    public static void generisiNaPutanju(UlazDokument dok, List<UlazMagacin> stavke,
                                          Podesavanja firma, String putanja) throws Exception {
        Document doc = new Document(PageSize.A4, 28, 28, 25, 25);
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
            pInfo.setLeading(12);
            pInfo.setSpacingBefore(3);
            cellFirma.addElement(pInfo);
        }

        PdfPCell cellDesno = new PdfPCell();
        cellDesno.setBorder(Rectangle.NO_BORDER);
        cellDesno.setPadding(0);
        cellDesno.setHorizontalAlignment(Element.ALIGN_RIGHT);
        Paragraph pNaslov = new Paragraph("PRIJEM ROBE", FONT_NASLOV);
        pNaslov.setAlignment(Element.ALIGN_RIGHT);
        cellDesno.addElement(pNaslov);
        Paragraph pBroj = new Paragraph("Broj: " + dok.getBroj(), FONT_DATUM);
        pBroj.setAlignment(Element.ALIGN_RIGHT);
        pBroj.setSpacingBefore(3);
        cellDesno.addElement(pBroj);
        Paragraph pDatum = new Paragraph("Datum: " + dok.getDatum(), FONT_DATUM);
        pDatum.setAlignment(Element.ALIGN_RIGHT);
        pDatum.setSpacingBefore(2);
        cellDesno.addElement(pDatum);

        header.addCell(cellFirma);
        header.addCell(cellDesno);
        doc.add(header);

        // Linija
        PdfPTable linija = new PdfPTable(1);
        linija.setWidthPercentage(100);
        linija.setSpacingAfter(10);
        PdfPCell lc = new PdfPCell();
        lc.setBorderWidthTop(2f); lc.setBorderWidthBottom(0);
        lc.setBorderWidthLeft(0); lc.setBorderWidthRight(0);
        lc.setBorderColorTop(CRNA); lc.setFixedHeight(1);
        linija.addCell(lc);
        doc.add(linija);

        // Dobavljač i broj otpremnice
        StringBuilder metaLinija = new StringBuilder();
        if (dok.getDobavljacNaziv() != null && !dok.getDobavljacNaziv().isBlank())
            metaLinija.append("Dobavljač: ").append(dok.getDobavljacNaziv());
        if (dok.getBrojOtpremnice() != null && !dok.getBrojOtpremnice().isBlank()) {
            if (!metaLinija.isEmpty()) metaLinija.append("     ");
            metaLinija.append("Br. otpremnice: ").append(dok.getBrojOtpremnice());
        }
        if (!metaLinija.isEmpty()) {
            Paragraph pMeta = new Paragraph(metaLinija.toString(), FONT_INFO);
            pMeta.setSpacingAfter(4);
            doc.add(pMeta);
        }

        if (dok.getNapomena() != null && !dok.getNapomena().isBlank()) {
            Paragraph pNap = new Paragraph("Napomena: " + dok.getNapomena(), FONT_INFO);
            pNap.setSpacingAfter(8);
            doc.add(pNap);
        }

        // ── TABELA ──────────────────────────────────────────────
        PdfPTable tabela = new PdfPTable(new float[]{6, 54, 20, 20});
        tabela.setWidthPercentage(100);
        tabela.setSpacingAfter(12);

        tabela.addCell(kreirajTH("#"));
        tabela.addCell(kreirajTH("Artikal"));
        tabela.addCell(kreirajTH("Količina"));
        tabela.addCell(kreirajTH("Jed. mere"));

        double ukupno = 0;
        for (int i = 0; i < stavke.size(); i++) {
            UlazMagacin s = stavke.get(i);
            BaseColor boja = (i % 2 == 0) ? BaseColor.WHITE : SVETLO_SIVA;
            ukupno += s.getKolicina();

            tabela.addCell(kreirajTD(String.valueOf(i + 1), boja, Element.ALIGN_CENTER));
            tabela.addCell(kreirajTD(s.getNazivArtikla(), boja, Element.ALIGN_LEFT));
            tabela.addCell(kreirajTD(fmtKol(s.getKolicina()), boja, Element.ALIGN_RIGHT));
            tabela.addCell(kreirajTD("", boja, Element.ALIGN_LEFT));
        }

        doc.add(tabela);

        // ── FOOTER ───────────────────────────────────────────────
        PdfPTable footer = new PdfPTable(3);
        footer.setWidthPercentage(100);
        footer.setSpacingBefore(20);

        String odgovornoLice = firma.getOdgovornoLice() != null && !firma.getOdgovornoLice().isBlank()
                ? firma.getOdgovornoLice() : "_______________________";
        footer.addCell(kreirajFooterCelija("Primio: " + odgovornoLice));
        footer.addCell(kreirajFooterCelija("Datum i mesto: _______________________"));
        footer.addCell(kreirajFooterCelija("Potpis i pečat: _______________________"));
        doc.add(footer);

        doc.close();
    }

    private static PdfPCell kreirajTH(String tekst) {
        PdfPCell c = new PdfPCell(new Phrase(tekst, FONT_TH));
        c.setPadding(5); c.setBorderColor(CRNA); c.setBorderWidth(0.5f);
        c.setBackgroundColor(HEADER_BG);
        return c;
    }

    private static PdfPCell kreirajTD(String tekst, BaseColor boja, int align) {
        PdfPCell c = new PdfPCell(new Phrase(tekst, FONT_TD));
        c.setPadding(5); c.setBorderColor(BORDER_SIVA); c.setBorderWidth(0.5f);
        c.setBackgroundColor(boja); c.setHorizontalAlignment(align);
        c.setMinimumHeight(18);
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
}
