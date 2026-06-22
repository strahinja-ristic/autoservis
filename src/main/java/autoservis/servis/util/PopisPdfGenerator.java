package autoservis.servis.util;

import autoservis.servis.model.Artikal;
import autoservis.servis.model.Podesavanja;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;

import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

public class PopisPdfGenerator {

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
    private static final Font FONT_FOOTER  = new Font(BF,       8, Font.NORMAL, SIVA);

    public static String generisi(List<Artikal> artikli, Podesavanja firma) throws Exception {
        String timestamp = LocalDate.now().format(DateTimeFormatter.ofPattern("dd_MM_yyyy"));
        String putanja = System.getProperty("user.home") + "/Desktop/Popis_" + timestamp + ".pdf";
        generisiNaPutanju(artikli, firma, putanja);
        return putanja;
    }

    public static void generisiNaPutanju(List<Artikal> artikli, Podesavanja firma, String putanja) throws Exception {
        // Sortiraj: najpre po sifri (null ide na kraj), pa po nazivu
        List<Artikal> sortirani = artikli.stream()
                .filter(a -> "Artikal".equals(a.getVrsta()))
                .sorted(Comparator
                        .comparing((Artikal a) -> a.getSifra() == null, Comparator.naturalOrder())
                        .thenComparing(a -> a.getSifra() != null ? a.getSifra() : Integer.MAX_VALUE)
                        .thenComparing(Artikal::getNaziv))
                .toList();

        Document doc = new Document(PageSize.A4, 28, 28, 28, 28);
        PdfWriter.getInstance(doc, new FileOutputStream(putanja));
        doc.open();

        // ── HEADER ──────────────────────────────────────────────
        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{65, 35});
        header.setSpacingAfter(6);

        PdfPCell cellFirma = new PdfPCell();
        cellFirma.setBorder(Rectangle.NO_BORDER);
        cellFirma.setPadding(0);
        String naziv = firma.getNazivFirme() != null ? firma.getNazivFirme() : "Moj Servis";
        cellFirma.addElement(new Paragraph(naziv, FONT_FIRMA));

        StringBuilder info = new StringBuilder();
        if (firma.getAdresa() != null && !firma.getAdresa().isBlank())
            info.append(firma.getAdresa()).append("\n");
        if (firma.getTelefon() != null && !firma.getTelefon().isBlank())
            info.append("Tel: ").append(firma.getTelefon()).append("\n");
        if (firma.getPib() != null && !firma.getPib().isBlank())
            info.append("PIB: ").append(firma.getPib());
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
        Paragraph pNaslov = new Paragraph("POPIS SKLADIŠTA", FONT_NASLOV);
        pNaslov.setAlignment(Element.ALIGN_RIGHT);
        cellDesno.addElement(pNaslov);
        String datum = LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
        Paragraph pDatum = new Paragraph("Datum: " + datum, FONT_DATUM);
        pDatum.setAlignment(Element.ALIGN_RIGHT);
        pDatum.setSpacingBefore(3);
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

        // ── TABELA ───────────────────────────────────────────────
        PdfPTable tabela = new PdfPTable(new float[]{12, 15, 43, 15, 15});
        tabela.setWidthPercentage(100);
        tabela.setSpacingAfter(14);

        tabela.addCell(kreirajTH("#"));
        tabela.addCell(kreirajTH("Šifra"));
        tabela.addCell(kreirajTH("Naziv artikla"));
        tabela.addCell(kreirajTH("Količina"));
        tabela.addCell(kreirajTH("Zbrojeno"));

        for (int i = 0; i < sortirani.size(); i++) {
            Artikal a = sortirani.get(i);
            BaseColor boja = (i % 2 == 0) ? BaseColor.WHITE : SVETLO_SIVA;

            tabela.addCell(kreirajTD(String.valueOf(i + 1), boja, Element.ALIGN_CENTER));
            tabela.addCell(kreirajTD(a.getSifra() != null ? String.valueOf(a.getSifra()) : "", boja, Element.ALIGN_LEFT));
            tabela.addCell(kreirajTD(a.getNaziv(), boja, Element.ALIGN_LEFT));

            String kolStr = a.getKolicina() != null
                    ? fmtKol(a.getKolicina()) + " " + (a.getJedinicaMere() != null ? a.getJedinicaMere() : "")
                    : "—";
            tabela.addCell(kreirajTD(kolStr, boja, Element.ALIGN_RIGHT));
            tabela.addCell(kreirajTD("", boja, Element.ALIGN_LEFT));
        }
        doc.add(tabela);

        // ── FOOTER ───────────────────────────────────────────────
        PdfPTable footer = new PdfPTable(1);
        footer.setWidthPercentage(100);
        PdfPCell fc = new PdfPCell();
        fc.setBorderColor(BORDER_SIVA); fc.setBorderWidth(0.5f); fc.setPadding(5);
        String odgovornoLice = firma.getOdgovornoLice() != null && !firma.getOdgovornoLice().isBlank()
                ? firma.getOdgovornoLice() : "_______________________";
        Paragraph fp = new Paragraph(
                "Ukupno artikala: " + sortirani.size() + "   |   " +
                "Popis obavio: " + odgovornoLice + "   |   " +
                "Potpis: _______________________",
                FONT_FOOTER);
        fp.setLeading(10);
        fc.addElement(fp);
        footer.addCell(fc);
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

    private static String fmtKol(double d) {
        return d == Math.floor(d) ? String.valueOf((int) d) : String.format("%.2f", d);
    }
}
