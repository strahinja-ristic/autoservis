package autoservis.servis.util;

import autoservis.servis.model.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;

import java.io.FileOutputStream;
import java.util.List;

public class PdfGenerator {

    private static final BaseColor CRNA        = new BaseColor(0, 0, 0);
    private static final BaseColor SIVA        = new BaseColor(85, 85, 85);
    private static final BaseColor SVETLO_SIVA = new BaseColor(249, 249, 249);
    private static final BaseColor BORDER_SIVA = new BaseColor(204, 204, 204);

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

    private static final Font FONT_NAZIV_FIRME  = new Font(BF_BOLD, 14, Font.NORMAL, CRNA);
    private static final Font FONT_INFO_FIRME   = new Font(BF,       9, Font.NORMAL, SIVA);
    private static final Font FONT_NALOG_LABEL  = new Font(BF,       8, Font.NORMAL, SIVA);
    private static final Font FONT_NALOG_BROJ   = new Font(BF_BOLD, 14, Font.NORMAL, CRNA);
    private static final Font FONT_NALOG_VAL    = new Font(BF_BOLD, 10, Font.NORMAL, CRNA);
    private static final Font FONT_SEC_NASLOV   = new Font(BF_BOLD,  9, Font.NORMAL, CRNA);
    private static final Font FONT_KV_NASLOV    = new Font(BF_BOLD,  9, Font.NORMAL, CRNA);
    private static final Font FONT_KV_LABEL     = new Font(BF,       9, Font.NORMAL, SIVA);
    private static final Font FONT_KV_VAL       = new Font(BF_BOLD,  9, Font.NORMAL, CRNA);
    private static final Font FONT_NORMAL       = new Font(BF,       9, Font.NORMAL, CRNA);
    private static final Font FONT_BOLD         = new Font(BF_BOLD,  9, Font.NORMAL, CRNA);
    private static final Font FONT_TH           = new Font(BF_BOLD,  9, Font.NORMAL, CRNA);
    private static final Font FONT_TD           = new Font(BF,       9, Font.NORMAL, CRNA);
    private static final Font FONT_FOOTER       = new Font(BF,       8, Font.NORMAL, SIVA);
    private static final Font FONT_FOOTER_NASLOV= new Font(BF_BOLD,  8, Font.NORMAL, CRNA);
    private static final Font FONT_POTPIS_LABEL = new Font(BF,       9, Font.NORMAL, SIVA);
    private static final Font FONT_POTPIS_SUB   = new Font(BF,       8, Font.NORMAL, BORDER_SIVA);

    public static void generisiNaPutanju(
            RadniNalog nalog, Klijent klijent, Vozilo vozilo,
            Podesavanja firma, List<String> usluge,
            List<NalogArtikal> artikli, String putanja) throws Exception {

        Document doc = new Document(PageSize.A4, 28, 28, 25, 25);
        PdfWriter.getInstance(doc, new FileOutputStream(putanja));
        doc.open();
        popuniDokument(doc, nalog, klijent, vozilo, firma, usluge, artikli);
        doc.close();
    }

    public static String generisiRadniNalog(
            RadniNalog nalog, Klijent klijent, Vozilo vozilo,
            Podesavanja firma, List<String> usluge,
            List<NalogArtikal> artikli) throws Exception {

        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String putanja = System.getProperty("user.home") + "/Desktop/RN_" +
                nalog.getBrojNaloga().replace("-", "_") + "_" + timestamp + ".pdf";
        generisiNaPutanju(nalog, klijent, vozilo, firma, usluge, artikli, putanja);
        return putanja;
    }

    private static void popuniDokument(Document doc, RadniNalog nalog, Klijent klijent,
                                       Vozilo vozilo, Podesavanja firma,
                                       List<String> usluge, List<NalogArtikal> artikli) throws Exception {

        // ── HEADER ───────────────────────────────────────────────
        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{70, 30});
        header.setSpacingAfter(6);

        PdfPCell cellFirma = new PdfPCell();
        cellFirma.setBorder(Rectangle.NO_BORDER);
        cellFirma.setPadding(0);
        cellFirma.setPaddingBottom(5);

        String nazivFirme = firma.getNazivFirme() != null ? firma.getNazivFirme() : "Moj Servis";
        cellFirma.addElement(new Paragraph(nazivFirme, FONT_NAZIV_FIRME));

        StringBuilder infoFirme = new StringBuilder();
        if (firma.getAdresa() != null && !firma.getAdresa().isBlank())
            infoFirme.append(firma.getAdresa()).append("\n");
        if (firma.getTelefon() != null && !firma.getTelefon().isBlank())
            infoFirme.append("Tel: ").append(firma.getTelefon()).append("\n");
        if (firma.getEmail() != null && !firma.getEmail().isBlank())
            infoFirme.append(firma.getEmail()).append("\n");
        if (firma.getPib() != null && !firma.getPib().isBlank())
            infoFirme.append("PIB: ").append(firma.getPib());
        if (firma.getMaticniBroj() != null && !firma.getMaticniBroj().isBlank())
            infoFirme.append(" | MB: ").append(firma.getMaticniBroj());
        if (firma.getZiroRacun() != null && !firma.getZiroRacun().isBlank())
            infoFirme.append("\nŽiro: ").append(firma.getZiroRacun());

        Paragraph pInfo = new Paragraph(infoFirme.toString(), FONT_INFO_FIRME);
        pInfo.setLeading(11);
        pInfo.setSpacingBefore(3);
        cellFirma.addElement(pInfo);

        PdfPCell cellLogo = new PdfPCell();
        cellLogo.setBorder(Rectangle.NO_BORDER);
        cellLogo.setPadding(0);
        cellLogo.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellLogo.setVerticalAlignment(Element.ALIGN_TOP);

        PdfPTable logoTable = new PdfPTable(1);
        logoTable.setWidthPercentage(100);
        PdfPCell logoCell = new PdfPCell();
        logoCell.setFixedHeight(55);
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        String logoPutanja = firma.getLogoPutanja();
        if (logoPutanja != null && !logoPutanja.isBlank()) {
            try {
                Image logo = Image.getInstance(logoPutanja);
                logo.scaleToFit(100, 48);
                logoCell.addElement(logo);
            } catch (Exception ex) {
                logoCell.addElement(new Paragraph("LOGO", FONT_INFO_FIRME));
            }
        } else {
            logoCell.addElement(new Paragraph("LOGO", FONT_INFO_FIRME));
        }

        logoTable.addCell(logoCell);
        cellLogo.addElement(logoTable);

        header.addCell(cellFirma);
        header.addCell(cellLogo);
        doc.add(header);

        // Linija ispod headera
        PdfPTable linija = new PdfPTable(1);
        linija.setWidthPercentage(100);
        linija.setSpacingAfter(6);
        PdfPCell linijaCell = new PdfPCell();
        linijaCell.setBorderWidthTop(2f);
        linijaCell.setBorderWidthBottom(0);
        linijaCell.setBorderWidthLeft(0);
        linijaCell.setBorderWidthRight(0);
        linijaCell.setBorderColorTop(CRNA);
        linijaCell.setFixedHeight(1);
        linija.addCell(linijaCell);
        doc.add(linija);

        // ── SEKCIJA: BROJ NALOGA, DATUMI, STATUS ─────────────────
        PdfPTable nalogSekcija = new PdfPTable(7);
        nalogSekcija.setWidthPercentage(100);
        nalogSekcija.setWidths(new float[]{25, 2, 25, 2, 25, 2, 19});
        nalogSekcija.setSpacingAfter(7);

        nalogSekcija.addCell(kreirajNalogItem("RADNI NALOG", nalog.getBrojNaloga(), true));
        nalogSekcija.addCell(kreirajDivider());
        nalogSekcija.addCell(kreirajNalogItem("DATUM PRIJEMA",
                nalog.getDatumPrijema() != null ? nalog.getDatumPrijema() : "—", false));
        nalogSekcija.addCell(kreirajDivider());
        nalogSekcija.addCell(kreirajNalogItem("DATUM ZAVRŠETKA",
                nalog.getDatumZavrsetka() != null && !nalog.getDatumZavrsetka().isBlank()
                        ? nalog.getDatumZavrsetka() : "—", false));
        nalogSekcija.addCell(kreirajDivider());
        nalogSekcija.addCell(kreirajNalogItem("STATUS",
                nalog.getStatus() != null ? nalog.getStatus() : "—", false));

        doc.add(nalogSekcija);

        // ── KLIJENT I VOZILO ──────────────────────────────────────
        PdfPTable kvTable = new PdfPTable(2);
        kvTable.setWidthPercentage(100);
        kvTable.setWidths(new float[]{50, 50});
        kvTable.setSpacingAfter(7);

        PdfPCell cellKlijent = new PdfPCell();
        cellKlijent.setBorderColor(CRNA);
        cellKlijent.setBorderWidth(0.5f);
        cellKlijent.setPadding(7);

        dodajKvNaslov(cellKlijent, "PODACI O KLIJENTU");

        if ("Pravno".equals(klijent.getTip())) {
            dodajKvRed(cellKlijent, "Naziv firme:", klijent.getNazivFirme());
            dodajKvRed(cellKlijent, "PIB:", klijent.getPib());
            dodajKvRed(cellKlijent, "Matični br.:", klijent.getMaticniBroj());
        } else {
            dodajKvRed(cellKlijent, "Ime i prezime:", klijent.getPunoIme());
        }
        dodajKvRed(cellKlijent, "Telefon:", klijent.getTelefon());
        dodajKvRed(cellKlijent, "Adresa:", klijent.getAdresa());

        PdfPCell cellVozilo = new PdfPCell();
        cellVozilo.setBorderColor(CRNA);
        cellVozilo.setBorderWidth(0.5f);
        cellVozilo.setBorderWidthLeft(0);
        cellVozilo.setPadding(7);

        dodajKvNaslov(cellVozilo, "PODACI O VOZILU");
        dodajKvRed(cellVozilo, "Marka / model:", vozilo.getMarka() + " " + vozilo.getModel());
        dodajKvRed(cellVozilo, "Registracija:", vozilo.getRegistracija());
        if (vozilo.getGodiste() != null) dodajKvRed(cellVozilo, "Godište:", String.valueOf(vozilo.getGodiste()));
        dodajKvRed(cellVozilo, "Br. šasije:", vozilo.getBrojSasije());
        dodajKvRed(cellVozilo, "Kilometraža:", nalog.getKilometrazaPrijema() + " km");

        kvTable.addCell(cellKlijent);
        kvTable.addCell(cellVozilo);
        doc.add(kvTable);

        // ── OPIS KVARA ────────────────────────────────────────────
        if (nalog.getOpisKvara() != null && !nalog.getOpisKvara().isBlank()) {
            doc.add(kreirajSecNaslov("OPIS KVARA"));
            PdfPTable t = new PdfPTable(1);
            t.setWidthPercentage(100);
            t.setSpacingAfter(5);
            PdfPCell c = new PdfPCell(new Phrase(nalog.getOpisKvara(), FONT_NORMAL));
            c.setPadding(5);
            c.setBorderColor(BORDER_SIVA);
            c.setBorderWidth(0.5f);
            c.setMinimumHeight(20);
            t.addCell(c);
            doc.add(t);
        }

        // ── ZAHTEV KLIJENTA ───────────────────────────────────────
        if (nalog.getZahtevKlijenta() != null && !nalog.getZahtevKlijenta().isBlank()) {
            doc.add(kreirajSecNaslov("ZAHTEV KLIJENTA"));
            PdfPTable t = new PdfPTable(1);
            t.setWidthPercentage(100);
            t.setSpacingAfter(5);
            PdfPCell c = new PdfPCell(new Phrase(nalog.getZahtevKlijenta(), FONT_NORMAL));
            c.setPadding(5);
            c.setBorderColor(BORDER_SIVA);
            c.setBorderWidth(0.5f);
            c.setMinimumHeight(20);
            t.addCell(c);
            doc.add(t);
        }

        // ── OSTECENJA ─────────────────────────────────────────────
        if (nalog.getOstecenja() != null && !nalog.getOstecenja().isBlank()) {
            doc.add(kreirajSecNaslov("VIDLJIVA OŠTEĆENJA PRI PRIJEMU"));
            PdfPTable t = new PdfPTable(1);
            t.setWidthPercentage(100);
            t.setSpacingAfter(5);
            PdfPCell c = new PdfPCell(new Phrase(nalog.getOstecenja(), FONT_NORMAL));
            c.setPadding(5);
            c.setBorderColor(BORDER_SIVA);
            c.setBorderWidth(0.5f);
            c.setMinimumHeight(18);
            t.addCell(c);
            doc.add(t);
        }

        // ── USLUGE ────────────────────────────────────────────────
        doc.add(kreirajSecNaslov("IZVRŠENE USLUGE"));
        PdfPTable uslTable = new PdfPTable(new float[]{8, 92});
        uslTable.setWidthPercentage(100);
        uslTable.setSpacingAfter(5);

        uslTable.addCell(kreirajTH("#"));
        uslTable.addCell(kreirajTH("Opis usluge"));

        if (usluge == null || usluge.isEmpty()) {
            PdfPCell prazna = new PdfPCell(new Phrase("—", FONT_NORMAL));
            prazna.setColspan(2);
            prazna.setPadding(4);
            prazna.setBorderColor(BORDER_SIVA);
            prazna.setBorderWidth(0.5f);
            uslTable.addCell(prazna);
        } else {
            for (int i = 0; i < usluge.size(); i++) {
                BaseColor boja = (i % 2 == 0) ? BaseColor.WHITE : SVETLO_SIVA;
                PdfPCell brCell = new PdfPCell(new Phrase((i + 1) + ".", FONT_TD));
                brCell.setPadding(4);
                brCell.setBorderColor(BORDER_SIVA);
                brCell.setBorderWidth(0.5f);
                brCell.setBackgroundColor(boja);
                brCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

                PdfPCell uslCell = new PdfPCell(new Phrase(usluge.get(i), FONT_TD));
                uslCell.setPadding(4);
                uslCell.setBorderColor(BORDER_SIVA);
                uslCell.setBorderWidth(0.5f);
                uslCell.setBackgroundColor(boja);

                uslTable.addCell(brCell);
                uslTable.addCell(uslCell);
            }
        }
        doc.add(uslTable);

        // ── ARTIKLI ───────────────────────────────────────────────
        doc.add(kreirajSecNaslov("UGRAĐENI / UTROŠENI ARTIKLI"));
        PdfPTable artTable = new PdfPTable(new float[]{60, 20, 20});
        artTable.setWidthPercentage(100);
        artTable.setSpacingAfter(5);

        artTable.addCell(kreirajTH("Naziv artikla"));
        artTable.addCell(kreirajTH("Količina"));
        artTable.addCell(kreirajTH("Jed. mere"));

        if (artikli == null || artikli.isEmpty()) {
            PdfPCell prazna = new PdfPCell(new Phrase("—", FONT_NORMAL));
            prazna.setColspan(3);
            prazna.setPadding(4);
            prazna.setBorderColor(BORDER_SIVA);
            prazna.setBorderWidth(0.5f);
            artTable.addCell(prazna);
        } else {
            for (int i = 0; i < artikli.size(); i++) {
                NalogArtikal na = artikli.get(i);
                BaseColor boja = (i % 2 == 0) ? BaseColor.WHITE : SVETLO_SIVA;

                PdfPCell cNaziv = new PdfPCell(new Phrase(na.getNazivArtikla(), FONT_TD));
                cNaziv.setPadding(4);
                cNaziv.setBorderColor(BORDER_SIVA);
                cNaziv.setBorderWidth(0.5f);
                cNaziv.setBackgroundColor(boja);

                PdfPCell cKol = new PdfPCell(new Phrase(String.valueOf(na.getKolicina()), FONT_TD));
                cKol.setPadding(4);
                cKol.setBorderColor(BORDER_SIVA);
                cKol.setBorderWidth(0.5f);
                cKol.setBackgroundColor(boja);
                cKol.setHorizontalAlignment(Element.ALIGN_CENTER);

                PdfPCell cJM = new PdfPCell(new Phrase(na.getJedinicaMere(), FONT_TD));
                cJM.setPadding(4);
                cJM.setBorderColor(BORDER_SIVA);
                cJM.setBorderWidth(0.5f);
                cJM.setBackgroundColor(boja);
                cJM.setHorizontalAlignment(Element.ALIGN_CENTER);

                artTable.addCell(cNaziv);
                artTable.addCell(cKol);
                artTable.addCell(cJM);
            }
        }
        doc.add(artTable);

        // ── SLEDECI SERVIS ────────────────────────────────────────
        if ((nalog.getSledeciServisKm() != null && nalog.getSledeciServisKm() > 0) ||
                (nalog.getSledeciServisDatum() != null && !nalog.getSledeciServisDatum().isBlank())) {

            doc.add(kreirajSecNaslov("NAREDNI SERVIS"));
            PdfPTable t = new PdfPTable(1);
            t.setWidthPercentage(100);
            t.setSpacingAfter(5);
            PdfPCell c = new PdfPCell();
            c.setPadding(5);
            c.setBorderColor(BORDER_SIVA);
            c.setBorderWidth(0.5f);

            StringBuilder servisTekst = new StringBuilder();
            if (nalog.getSledeciServisKm() != null && nalog.getSledeciServisKm() > 0)
                servisTekst.append("Kilometraža: ").append(nalog.getSledeciServisKm()).append(" km");
            if (nalog.getSledeciServisDatum() != null && !nalog.getSledeciServisDatum().isBlank()) {
                if (servisTekst.length() > 0) servisTekst.append("     |     ");
                servisTekst.append("Datum: ").append(nalog.getSledeciServisDatum());
            }
            c.addElement(new Phrase(servisTekst.toString(), FONT_BOLD));
            t.addCell(c);
            doc.add(t);
        }

        // ── NAPOMENA ──────────────────────────────────────────────
        if (nalog.getNapomena() != null && !nalog.getNapomena().isBlank()) {
            doc.add(kreirajSecNaslov("NAPOMENA"));
            PdfPTable t = new PdfPTable(1);
            t.setWidthPercentage(100);
            t.setSpacingAfter(5);
            PdfPCell c = new PdfPCell(new Phrase(nalog.getNapomena(), FONT_NORMAL));
            c.setPadding(5);
            c.setBorderColor(BORDER_SIVA);
            c.setBorderWidth(0.5f);
            c.setMinimumHeight(18);
            t.addCell(c);
            doc.add(t);
        }

        // ── POTPISI ───────────────────────────────────────────────
        PdfPTable potpisTable = new PdfPTable(3);
        potpisTable.setWidthPercentage(100);
        potpisTable.setWidths(new float[]{44, 12, 44});
        potpisTable.setSpacingAfter(7);

        PdfPCell cellServ = new PdfPCell();
        cellServ.setBorder(Rectangle.NO_BORDER);
        cellServ.setPadding(4);
        cellServ.setFixedHeight(32);
        cellServ.addElement(new Paragraph("Potpis servisera:", FONT_POTPIS_LABEL));

        PdfPCell spacer1 = new PdfPCell();
        spacer1.setBorder(Rectangle.NO_BORDER);

        PdfPCell cellPrim = new PdfPCell();
        cellPrim.setBorder(Rectangle.NO_BORDER);
        cellPrim.setPadding(4);
        cellPrim.setFixedHeight(32);
        Paragraph pPrimLabel = new Paragraph("Potpis primaoca:", FONT_POTPIS_LABEL);
        pPrimLabel.setAlignment(Element.ALIGN_RIGHT);
        cellPrim.addElement(pPrimLabel);

        PdfPCell linServ = new PdfPCell();
        linServ.setBorderWidthTop(1f);
        linServ.setBorderColorTop(CRNA);
        linServ.setBorderWidthBottom(0);
        linServ.setBorderWidthLeft(0);
        linServ.setBorderWidthRight(0);
        linServ.setPaddingTop(3);
        Paragraph pSubServ = new Paragraph("ime i prezime / potpis", FONT_POTPIS_SUB);
        pSubServ.setAlignment(Element.ALIGN_CENTER);
        linServ.addElement(pSubServ);

        PdfPCell spacer2 = new PdfPCell();
        spacer2.setBorder(Rectangle.NO_BORDER);

        PdfPCell linPrim = new PdfPCell();
        linPrim.setBorderWidthTop(1f);
        linPrim.setBorderColorTop(CRNA);
        linPrim.setBorderWidthBottom(0);
        linPrim.setBorderWidthLeft(0);
        linPrim.setBorderWidthRight(0);
        linPrim.setPaddingTop(3);
        Paragraph pSubPrim = new Paragraph("ime i prezime / potpis", FONT_POTPIS_SUB);
        pSubPrim.setAlignment(Element.ALIGN_CENTER);
        linPrim.addElement(pSubPrim);

        potpisTable.addCell(cellServ);
        potpisTable.addCell(spacer1);
        potpisTable.addCell(cellPrim);
        potpisTable.addCell(linServ);
        potpisTable.addCell(spacer2);
        potpisTable.addCell(linPrim);
        doc.add(potpisTable);

        // ── FOOTER ────────────────────────────────────────────────
        PdfPTable footerTable = new PdfPTable(1);
        footerTable.setWidthPercentage(100);

        PdfPCell footerCell = new PdfPCell();
        footerCell.setBorderColor(BORDER_SIVA);
        footerCell.setBorderWidth(0.5f);
        footerCell.setPadding(5);

        Paragraph footerNaslov = new Paragraph("Napomena", FONT_FOOTER_NASLOV);
        footerNaslov.setSpacingAfter(2);
        footerCell.addElement(footerNaslov);

        String footerTekst = "Naručilac je saglasan: 1) Da se izvrše navedeni potrebni radovi " +
                "2) Da se izvrše i obave i oni nepredvidivi radovi koji su neophodni za izvršenje naručenih radova " +
                "3) Da se izvršeni radovi i ugrađeni delovi naplate po važećim cenama servisa " +
                "4) Da rok završetka radova može da bude produžen u slučaju nedostatka rezervnih delova, dodatnih problema ili više sile " +
                "5) Da po preuzimanju vozila preuzme stare delove, u protivnom isti će biti uništeni " +
                "6) Da isplati vrednost popravke pre preuzimanja vozila " +
                "7) U slučaju spora nadležan je sud u sedištu auto servisa";

        Paragraph footerP = new Paragraph(footerTekst, FONT_FOOTER);
        footerP.setLeading(10);
        footerCell.addElement(footerP);

        footerTable.addCell(footerCell);
        doc.add(footerTable);
    }

    // ── POMOCNE METODE ────────────────────────────────────────────

    private static PdfPCell kreirajNalogItem(String label, String value, boolean veliki) {
        PdfPCell cell = new PdfPCell();
        cell.setBorderColor(CRNA);
        cell.setBorderWidth(0.5f);
        cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Paragraph pLabel = new Paragraph(label, FONT_NALOG_LABEL);
        pLabel.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(pLabel);

        Font font = veliki ? FONT_NALOG_BROJ : FONT_NALOG_VAL;
        Paragraph pVal = new Paragraph(value, font);
        pVal.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(pVal);

        return cell;
    }

    private static PdfPCell kreirajDivider() {
        PdfPCell cell = new PdfPCell();
        cell.setBorderColor(BORDER_SIVA);
        cell.setBorderWidth(0.5f);
        cell.setFixedHeight(36);
        return cell;
    }

    private static Paragraph kreirajSecNaslov(String tekst) {
        Paragraph p = new Paragraph(tekst, FONT_SEC_NASLOV);
        p.setSpacingBefore(2);
        p.setSpacingAfter(2);
        LineSeparator ls = new LineSeparator(1.5f, 100, CRNA, Element.ALIGN_LEFT, -2);
        p.add(new Chunk(ls));
        return p;
    }

    private static void dodajKvNaslov(PdfPCell cell, String tekst) {
        Paragraph p = new Paragraph(tekst, FONT_KV_NASLOV);
        p.setSpacingAfter(3);
        LineSeparator ls = new LineSeparator(0.5f, 100, BORDER_SIVA, Element.ALIGN_LEFT, -2);
        p.add(new Chunk(ls));
        cell.addElement(p);
    }

    private static void dodajKvRed(PdfPCell cell, String label, String value) {
        if (value == null || value.isBlank()) return;
        PdfPTable red = new PdfPTable(new float[]{38, 62});
        red.setWidthPercentage(100);

        PdfPCell lbl = new PdfPCell(new Phrase(label, FONT_KV_LABEL));
        lbl.setBorder(Rectangle.NO_BORDER);
        lbl.setPaddingBottom(2);

        PdfPCell val = new PdfPCell(new Phrase(value, FONT_KV_VAL));
        val.setBorder(Rectangle.NO_BORDER);
        val.setPaddingBottom(2);

        red.addCell(lbl);
        red.addCell(val);
        cell.addElement(red);
    }

    private static PdfPCell kreirajTH(String tekst) {
        PdfPCell cell = new PdfPCell(new Phrase(tekst, FONT_TH));
        cell.setPadding(4);
        cell.setBorderColor(CRNA);
        cell.setBorderWidth(0.5f);
        return cell;
    }
}
