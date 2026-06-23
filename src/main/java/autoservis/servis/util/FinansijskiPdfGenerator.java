package autoservis.servis.util;

import autoservis.servis.model.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.LineSeparator;

import java.io.FileOutputStream;
import java.util.List;

public class FinansijskiPdfGenerator {

    private static final BaseColor CRNA        = new BaseColor(0, 0, 0);
    private static final BaseColor SIVA        = new BaseColor(85, 85, 85);
    private static final BaseColor SVETLO_SIVA = new BaseColor(249, 249, 249);
    private static final BaseColor BORDER_SIVA = new BaseColor(204, 204, 204);
    private static final BaseColor PLAVA       = new BaseColor(0, 87, 183);

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
    private static final Font FONT_DOK_TIP      = new Font(BF_BOLD, 18, Font.NORMAL, PLAVA);
    private static final Font FONT_DOK_LABEL    = new Font(BF,       8, Font.NORMAL, SIVA);
    private static final Font FONT_DOK_BROJ     = new Font(BF_BOLD, 13, Font.NORMAL, CRNA);
    private static final Font FONT_DOK_VAL      = new Font(BF_BOLD, 10, Font.NORMAL, CRNA);
    private static final Font FONT_SEC_NASLOV   = new Font(BF_BOLD,  9, Font.NORMAL, CRNA);
    private static final Font FONT_KV_NASLOV    = new Font(BF_BOLD,  9, Font.NORMAL, CRNA);
    private static final Font FONT_KV_LABEL     = new Font(BF,       9, Font.NORMAL, SIVA);
    private static final Font FONT_KV_VAL       = new Font(BF_BOLD,  9, Font.NORMAL, CRNA);
    private static final Font FONT_TH           = new Font(BF_BOLD,  9, Font.NORMAL, CRNA);
    private static final Font FONT_TD           = new Font(BF,        9, Font.NORMAL, CRNA);
    private static final Font FONT_NORMAL       = new Font(BF,        9, Font.NORMAL, CRNA);
    private static final Font FONT_BOLD         = new Font(BF_BOLD,   9, Font.NORMAL, CRNA);
    private static final Font FONT_RECAP_LABEL  = new Font(BF,        9, Font.NORMAL, SIVA);
    private static final Font FONT_RECAP_VAL    = new Font(BF_BOLD,  10, Font.NORMAL, CRNA);
    private static final Font FONT_UKUPNO       = new Font(BF_BOLD,  11, Font.NORMAL, PLAVA);
    private static final Font FONT_FOOTER       = new Font(BF,        8, Font.NORMAL, SIVA);
    private static final Font FONT_POTPIS_LABEL = new Font(BF,        9, Font.NORMAL, SIVA);
    private static final Font FONT_POTPIS_SUB   = new Font(BF,        8, Font.NORMAL, BORDER_SIVA);

    // ── PREDRACUN ────────────────────────────────────────────────

    public static String generisiPredracun(Predracun predracun, Klijent klijent, Vozilo vozilo,
                                            Podesavanja firma) throws Exception {
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String putanja = System.getProperty("user.home") + "/Desktop/PR_" +
                predracun.getBrojPredracuna().replace("-", "_") + "_" + timestamp + ".pdf";
        generisiPredracunNaPutanju(predracun, klijent, vozilo, firma, putanja);
        return putanja;
    }

    public static void generisiPredracunNaPutanju(Predracun predracun, Klijent klijent, Vozilo vozilo,
                                                   Podesavanja firma, String putanja) throws Exception {
        Document doc = new Document(PageSize.A4, 20, 20, 22, 22);
        PdfWriter.getInstance(doc, new FileOutputStream(putanja));
        doc.open();
        popuniPredracun(doc, predracun, klijent, vozilo, firma);
        doc.close();
    }

    // ── FAKTURA ──────────────────────────────────────────────────

    public static String generisiFakturu(Faktura faktura, Klijent klijent, Vozilo vozilo,
                                          Podesavanja firma) throws Exception {
        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String putanja = System.getProperty("user.home") + "/Desktop/F_" +
                faktura.getBrojFakture().replace("-", "_") + "_" + timestamp + ".pdf";
        generisiFakturuNaPutanju(faktura, klijent, vozilo, firma, putanja);
        return putanja;
    }

    public static void generisiFakturuNaPutanju(Faktura faktura, Klijent klijent, Vozilo vozilo,
                                                  Podesavanja firma, String putanja) throws Exception {
        generisiFakturuNaPutanju(faktura, klijent, vozilo, firma, java.util.Collections.emptyList(), putanja);
    }

    public static void generisiFakturuNaPutanju(Faktura faktura, Klijent klijent, Vozilo vozilo,
                                                  Podesavanja firma, java.util.List<String> usluge,
                                                  String putanja) throws Exception {
        Document doc = new Document(PageSize.A4, 20, 20, 22, 22);
        PdfWriter.getInstance(doc, new FileOutputStream(putanja));
        doc.open();
        popuniFakturu(doc, faktura, klijent, vozilo, firma, usluge);
        doc.close();
    }

    // ── SADRZAJ PREDRACUNA ────────────────────────────────────────

    private static void popuniPredracun(Document doc, Predracun p, Klijent k, Vozilo v,
                                         Podesavanja firma) throws Exception {
        dodajHeader(doc, firma, "PREDRAČUN", p.getBrojPredracuna());

        // Sekcija: meta podaci
        PdfPTable metaTable = new PdfPTable(7);
        metaTable.setWidthPercentage(100);
        metaTable.setWidths(new float[]{20, 2, 20, 2, 20, 2, 34});
        metaTable.setSpacingAfter(7);

        metaTable.addCell(kreirajMetaItem("DATUM KREIRANJA", p.getDatumKreiranja()));
        metaTable.addCell(kreirajDivider());
        metaTable.addCell(kreirajMetaItem("ROK VAŽENJA", nvl(p.getDatumVazenja())));
        metaTable.addCell(kreirajDivider());
        metaTable.addCell(kreirajMetaItem("NAČIN PLAĆANJA", nvl(p.getNacinPlacanja())));
        metaTable.addCell(kreirajDivider());
        metaTable.addCell(kreirajMetaItem("STATUS", nvl(p.getStatus())));
        doc.add(metaTable);

        dodajKvBlok(doc, k, v, p.getMestoIzdavanja());
        dodajStavkeTabela(doc, konvertujStavke(p.getStavke()), firma.isPdvObveznik());
        dodajRekapitulacija(doc, p.ukupnoBezPdv(), p.ukupniPdv(),
                p.getPopustProcenat(), p.globalniPopustIznos(), p.zaUplatu(), firma.isPdvObveznik());

        dodajInfoZaPlacanje(doc, firma, nvl(p.getNacinPlacanja()), nvl(p.getRokPlacanja()), p.getBrojPredracuna());

        if (p.getNapomena() != null && !p.getNapomena().isBlank()) {
            doc.add(kreirajSecNaslov("NAPOMENA"));
            PdfPTable t = new PdfPTable(1);
            t.setWidthPercentage(100);
            t.setSpacingAfter(5);
            PdfPCell c = new PdfPCell(new Phrase(p.getNapomena(), FONT_NORMAL));
            c.setPadding(5); c.setBorderColor(BORDER_SIVA); c.setBorderWidth(0.5f);
            t.addCell(c);
            doc.add(t);
        }

        dodajPotpisi(doc);
        dodajFooter(doc);
    }

    // ── SADRZAJ FAKTURE ───────────────────────────────────────────

    private static void popuniFakturu(Document doc, Faktura f, Klijent k, Vozilo v,
                                       Podesavanja firma, java.util.List<String> usluge) throws Exception {
        dodajHeader(doc, firma, "PRILOG UZ RAČUN", f.getBrojFakture());

        Paragraph prilog = new Paragraph("PRILOG UZ FISKALNI RAČUN", new Font(BF_BOLD, 11, Font.NORMAL, PLAVA));
        prilog.setAlignment(Element.ALIGN_CENTER);
        prilog.setSpacingBefore(2);
        prilog.setSpacingAfter(4);
        doc.add(prilog);

        // Fiskalni podaci — uvek se prikazuju na svakoj fakturi
        PdfPTable fiskalnaTable = new PdfPTable(3);
        fiskalnaTable.setWidthPercentage(100);
        fiskalnaTable.setWidths(new float[]{48, 2, 50});
        fiskalnaTable.setSpacingAfter(6);
        fiskalnaTable.addCell(kreirajMetaItem("BROJ RAČUNA", nvl(f.getBrojRacuna())));
        fiskalnaTable.addCell(kreirajDivider());
        fiskalnaTable.addCell(kreirajMetaItem("PFR BROJ", nvl(f.getPfrBroj())));
        doc.add(fiskalnaTable);

        PdfPTable metaTable = new PdfPTable(9);
        metaTable.setWidthPercentage(100);
        metaTable.setWidths(new float[]{18, 2, 18, 2, 18, 2, 18, 2, 20});
        metaTable.setSpacingAfter(7);

        metaTable.addCell(kreirajMetaItem("DATUM KREIRANJA", f.getDatumKreiranja()));
        metaTable.addCell(kreirajDivider());
        metaTable.addCell(kreirajMetaItem("ROK PLAĆANJA", nvl(f.getRokPlacanja())));
        metaTable.addCell(kreirajDivider());
        metaTable.addCell(kreirajMetaItem("NAČIN PLAĆANJA", nvl(f.getNacinPlacanja())));
        metaTable.addCell(kreirajDivider());
        metaTable.addCell(kreirajMetaItem("DATUM PLAĆANJA", nvl(f.getDatumPlacanja())));
        metaTable.addCell(kreirajDivider());
        metaTable.addCell(kreirajMetaItem("STATUS", nvl(f.getStatus())));
        doc.add(metaTable);

        dodajKvBlok(doc, k, v, f.getMestoIzdavanja());

        if (f.getMestoIsporuke() != null && !f.getMestoIsporuke().isBlank()) {
            PdfPTable mi = new PdfPTable(1);
            mi.setWidthPercentage(100);
            mi.setSpacingAfter(7);
            PdfPCell c = new PdfPCell();
            c.setBorderColor(CRNA); c.setBorderWidth(0.5f); c.setPadding(7);
            dodajKvNaslov(c, "MESTO ISPORUKE");
            dodajKvRed(c, "Adresa:", f.getMestoIsporuke());
            mi.addCell(c);
            doc.add(mi);
        }

        dodajStavkeTabela(doc, konvertujFakturaStavke(f.getStavke()), firma.isPdvObveznik());
        dodajRekapitulacija(doc, f.ukupnoBezPdv(), f.ukupniPdv(),
                f.getPopustProcenat(), f.globalniPopustIznos(), f.zaUplatu(), firma.isPdvObveznik());

        if (!"Gotovina".equals(f.getNacinPlacanja())) {
            dodajInfoZaPlacanje(doc, firma, nvl(f.getNacinPlacanja()), nvl(f.getRokPlacanja()), f.getBrojFakture());
        }

        if (f.getNapomena() != null && !f.getNapomena().isBlank()) {
            doc.add(kreirajSecNaslov("NAPOMENA"));
            PdfPTable t = new PdfPTable(1);
            t.setWidthPercentage(100);
            t.setSpacingAfter(5);
            PdfPCell c = new PdfPCell(new Phrase(f.getNapomena(), FONT_NORMAL));
            c.setPadding(5); c.setBorderColor(BORDER_SIVA); c.setBorderWidth(0.5f);
            t.addCell(c);
            doc.add(t);
        }

        dodajPotpisi(doc);
        dodajFooter(doc);

        if (usluge != null && !usluge.isEmpty()) {
            doc.newPage();
            dodajHeader(doc, firma, "IZVRŠENE USLUGE", f.getBrojFakture());

            PdfPTable uslTable = new PdfPTable(new float[]{8, 92});
            uslTable.setWidthPercentage(100);
            uslTable.setSpacingAfter(7);
            uslTable.addCell(kreirajTH("#"));
            uslTable.addCell(kreirajTH("Opis usluge"));
            for (int i = 0; i < usluge.size(); i++) {
                BaseColor boja = (i % 2 == 0) ? BaseColor.WHITE : SVETLO_SIVA;
                PdfPCell cBr = new PdfPCell(new Phrase((i + 1) + ".", FONT_TD));
                cBr.setPadding(4); cBr.setBorderColor(BORDER_SIVA); cBr.setBorderWidth(0.5f);
                cBr.setBackgroundColor(boja); cBr.setHorizontalAlignment(Element.ALIGN_RIGHT);
                PdfPCell cOp = new PdfPCell(new Phrase(usluge.get(i), FONT_TD));
                cOp.setPadding(4); cOp.setBorderColor(BORDER_SIVA); cOp.setBorderWidth(0.5f);
                cOp.setBackgroundColor(boja);
                uslTable.addCell(cBr);
                uslTable.addCell(cOp);
            }
            doc.add(uslTable);
        }
    }

    // ── ZAJEDNICKI BLOKOVI ────────────────────────────────────────

    private static void dodajHeader(Document doc, Podesavanja firma,
                                     String tipDokumenta, String brojDokumenta) throws Exception {
        PdfPTable header = new PdfPTable(3);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{44, 28, 28});
        header.setSpacingAfter(6);

        // Firma levo
        PdfPCell cellFirma = new PdfPCell();
        cellFirma.setBorder(Rectangle.NO_BORDER);
        cellFirma.setPadding(0);
        cellFirma.setPaddingBottom(5);
        String naziv = firma.getNazivFirme() != null ? firma.getNazivFirme() : "Moj Servis";
        cellFirma.addElement(new Paragraph(naziv, FONT_NAZIV_FIRME));

        StringBuilder info = new StringBuilder();
        if (firma.getAdresa() != null && !firma.getAdresa().isBlank()) info.append(firma.getAdresa()).append("\n");
        if (firma.getTelefon() != null && !firma.getTelefon().isBlank()) info.append("Tel: ").append(firma.getTelefon()).append("\n");
        if (firma.getEmail() != null && !firma.getEmail().isBlank()) info.append(firma.getEmail()).append("\n");
        if (firma.getPib() != null && !firma.getPib().isBlank()) info.append("PIB: ").append(firma.getPib());
        if (firma.getMaticniBroj() != null && !firma.getMaticniBroj().isBlank()) info.append("  MB: ").append(firma.getMaticniBroj());
        Paragraph pInfo = new Paragraph(info.toString(), FONT_INFO_FIRME);
        pInfo.setLeading(11);
        pInfo.setSpacingBefore(3);
        cellFirma.addElement(pInfo);

        // Tip dokumenta u sredini
        PdfPCell cellTip = new PdfPCell();
        cellTip.setBorder(Rectangle.NO_BORDER);
        cellTip.setHorizontalAlignment(Element.ALIGN_CENTER);
        cellTip.setVerticalAlignment(Element.ALIGN_MIDDLE);
        Paragraph pTip = new Paragraph(tipDokumenta, FONT_DOK_TIP);
        pTip.setAlignment(Element.ALIGN_CENTER);
        cellTip.addElement(pTip);
        Paragraph pBroj = new Paragraph(brojDokumenta, FONT_DOK_BROJ);
        pBroj.setAlignment(Element.ALIGN_CENTER);
        cellTip.addElement(pBroj);

        // Logo desno
        PdfPCell cellLogo = new PdfPCell();
        cellLogo.setBorder(Rectangle.NO_BORDER);
        cellLogo.setPadding(0);
        cellLogo.setHorizontalAlignment(Element.ALIGN_RIGHT);
        cellLogo.setVerticalAlignment(Element.ALIGN_MIDDLE);
        String logoPutanja = firma.getLogoPutanja();
        if (logoPutanja != null && !logoPutanja.isBlank()) {
            try {
                Image logo = Image.getInstance(logoPutanja);
                logo.scaleToFit(90, 48);
                PdfPTable lt = new PdfPTable(1);
                lt.setWidthPercentage(100);
                PdfPCell lc = new PdfPCell();
                lc.setFixedHeight(55);
                lc.setBorder(Rectangle.NO_BORDER);
                lc.setHorizontalAlignment(Element.ALIGN_CENTER);
                lc.setVerticalAlignment(Element.ALIGN_MIDDLE);
                lc.addElement(logo);
                lt.addCell(lc);
                cellLogo.addElement(lt);
            } catch (Exception ignored) {}
        }

        header.addCell(cellFirma);
        header.addCell(cellTip);
        header.addCell(cellLogo);
        doc.add(header);

        // Linija
        PdfPTable linija = new PdfPTable(1);
        linija.setWidthPercentage(100);
        linija.setSpacingAfter(6);
        PdfPCell lc = new PdfPCell();
        lc.setBorderWidthTop(2f); lc.setBorderWidthBottom(0); lc.setBorderWidthLeft(0); lc.setBorderWidthRight(0);
        lc.setBorderColorTop(PLAVA); lc.setFixedHeight(1);
        linija.addCell(lc);
        doc.add(linija);
    }

    private static void dodajKvBlok(Document doc, Klijent k, Vozilo v, String mestoIzdavanja) throws Exception {
        PdfPTable kvTable = new PdfPTable(2);
        kvTable.setWidthPercentage(100);
        kvTable.setWidths(new float[]{50, 50});
        kvTable.setSpacingAfter(7);

        PdfPCell cellV = new PdfPCell();
        cellV.setBorderColor(CRNA); cellV.setBorderWidth(0.5f); cellV.setPadding(7);
        dodajKvNaslov(cellV, "PODACI O VOZILU");
        if (v != null) {
            dodajKvRed(cellV, "Marka / model:", v.getMarka() + " " + v.getModel());
            dodajKvRed(cellV, "Registracija:", v.getRegistracija());
            if (v.getGodiste() != null) dodajKvRed(cellV, "Godište:", String.valueOf(v.getGodiste()));
            dodajKvRed(cellV, "Br. šasije:", v.getBrojSasije());
        }
        if (mestoIzdavanja != null && !mestoIzdavanja.isBlank())
            dodajKvRed(cellV, "Mesto izdavanja:", mestoIzdavanja);

        PdfPCell cellK = new PdfPCell();
        cellK.setBorderColor(CRNA); cellK.setBorderWidth(0.5f); cellK.setBorderWidthLeft(0); cellK.setPadding(7);
        dodajKvNaslov(cellK, "PODACI O KLIJENTU");
        if ("Pravno".equals(k.getTip())) {
            dodajKvRed(cellK, "Naziv firme:", k.getNazivFirme());
            dodajKvRed(cellK, "PIB:", k.getPib());
        } else {
            dodajKvRed(cellK, "Ime i prezime:", k.getPunoIme());
        }
        dodajKvRed(cellK, "Telefon:", k.getTelefon());
        dodajKvRed(cellK, "Email:", k.getEmail());
        dodajKvRed(cellK, "Adresa:", k.getAdresa());

        kvTable.addCell(cellV);
        kvTable.addCell(cellK);
        doc.add(kvTable);
    }

    private static void dodajStavkeTabela(Document doc, List<StavkaRed> stavke, boolean pdvObveznik) throws Exception {
        doc.add(kreirajSecNaslov("STAVKE"));

        float[] sirine = pdvObveznik
                ? new float[]{4, 28, 7, 7, 15, 7, 14, 18}
                : new float[]{4, 36, 8, 8, 20, 24};

        PdfPTable t = new PdfPTable(sirine);
        t.setWidthPercentage(100);
        t.setSpacingAfter(5);

        t.addCell(kreirajTH("#"));
        t.addCell(kreirajTH("Naziv"));
        t.addCell(kreirajTH("Kol."));
        t.addCell(kreirajTH("JM"));
        t.addCell(kreirajTH(pdvObveznik ? "Cena" : "Cena (RSD)"));
        if (pdvObveznik) {
            t.addCell(kreirajTH("PDV%"));
            t.addCell(kreirajTH("PDV (RSD)"));
        }
        t.addCell(kreirajTH("Ukupno (RSD)"));

        if (stavke.isEmpty()) {
            int colspan = pdvObveznik ? 8 : 6;
            PdfPCell prazna = new PdfPCell(new Phrase("—", FONT_TD));
            prazna.setColspan(colspan);
            prazna.setPadding(4);
            prazna.setBorderColor(BORDER_SIVA);
            prazna.setBorderWidth(0.5f);
            t.addCell(prazna);
        } else {
            for (int i = 0; i < stavke.size(); i++) {
                StavkaRed s = stavke.get(i);
                BaseColor boja = (i % 2 == 0) ? BaseColor.WHITE : SVETLO_SIVA;
                t.addCell(kreirajTD(String.valueOf(i + 1), boja, Element.ALIGN_CENTER));
                t.addCell(kreirajTD(s.naziv, boja, Element.ALIGN_LEFT));
                t.addCell(kreirajTD(fmt(s.kolicina), boja, Element.ALIGN_CENTER));
                t.addCell(kreirajTD(s.jm, boja, Element.ALIGN_CENTER));
                t.addCell(kreirajTD(fmtBroj(s.cenaBezPdv), boja, Element.ALIGN_RIGHT));
                if (pdvObveznik) {
                    t.addCell(kreirajTD(fmt(s.pdvStopa) + "%", boja, Element.ALIGN_CENTER));
                    t.addCell(kreirajTD(fmtBroj(s.iznosPdv), boja, Element.ALIGN_RIGHT));
                }
                t.addCell(kreirajTD(fmtBroj(s.iznosUkupno), boja, Element.ALIGN_RIGHT));
            }
        }
        doc.add(t);
    }

    private static void dodajRekapitulacija(Document doc, double bezPdv, double pdv,
                                             double popustProc, double popustIznos,
                                             double zaUplatu, boolean pdvObveznik) throws Exception {
        doc.add(kreirajSecNaslov("REKAPITULACIJA"));

        PdfPTable recap = new PdfPTable(2);
        recap.setWidthPercentage(50);
        recap.setHorizontalAlignment(Element.ALIGN_RIGHT);
        recap.setWidths(new float[]{60, 40});
        recap.setSpacingAfter(5);

        if (pdvObveznik) {
            dodajRecapRed(recap, "Ukupno bez PDV:", fmtCena(bezPdv));
            dodajRecapRed(recap, "Ukupan PDV:", fmtCena(pdv));
        } else {
            dodajRecapRed(recap, "Ukupno:", fmtCena(bezPdv));
        }
        if (popustProc > 0) {
            dodajRecapRed(recap, "Popust (" + fmt(popustProc) + "%):", "- " + fmtCena(popustIznos));
        }

        // Ukupno za uplatu — bold plavo
        PdfPCell lbl = new PdfPCell(new Phrase("UKUPNO ZA UPLATU:", FONT_UKUPNO));
        lbl.setBorderWidthTop(1f); lbl.setBorderColorTop(PLAVA);
        lbl.setBorderWidthBottom(0); lbl.setBorderWidthLeft(0); lbl.setBorderWidthRight(0);
        lbl.setPadding(5);
        PdfPCell val = new PdfPCell(new Phrase(fmtCena(zaUplatu), FONT_UKUPNO));
        val.setBorderWidthTop(1f); val.setBorderColorTop(PLAVA);
        val.setBorderWidthBottom(0); val.setBorderWidthLeft(0); val.setBorderWidthRight(0);
        val.setPadding(5); val.setHorizontalAlignment(Element.ALIGN_RIGHT);
        recap.addCell(lbl);
        recap.addCell(val);

        doc.add(recap);
    }

    private static void dodajInfoZaPlacanje(Document doc, Podesavanja firma,
                                              String nacinPlacanja, String rokPlacanja, String pozivNaBroj) throws Exception {
        doc.add(kreirajSecNaslov("INFORMACIJE ZA PLAĆANJE"));

        PdfPTable t = new PdfPTable(2);
        t.setWidthPercentage(100);
        t.setWidths(new float[]{55, 45});
        t.setSpacingAfter(7);

        // Leva celija — podaci za uplatu
        PdfPCell cellPlacanje = new PdfPCell();
        cellPlacanje.setBorderColor(PLAVA); cellPlacanje.setBorderWidth(0.5f); cellPlacanje.setPadding(7);
        dodajKvNaslov(cellPlacanje, "PODACI ZA UPLATU");
        if (firma.getZiroRacun() != null && !firma.getZiroRacun().isBlank())
            dodajKvRed(cellPlacanje, "Račun za uplatu:", firma.getZiroRacun());
        dodajKvRed(cellPlacanje, "Poziv na broj:", pozivNaBroj);
        String valutaTekst = rokPlacanja;
        try {
            int dana = Integer.parseInt(rokPlacanja.trim());
            valutaTekst = dana + " dana od dana izdavanja računa";
        } catch (NumberFormatException ignored) {}
        dodajKvRed(cellPlacanje, "Valuta:", valutaTekst);
        dodajKvRed(cellPlacanje, "Način plaćanja:", nacinPlacanja);

        // Desna celija — pravne napomene
        PdfPCell cellPravno = new PdfPCell();
        cellPravno.setBorderColor(PLAVA); cellPravno.setBorderWidth(0.5f);
        cellPravno.setBorderWidthLeft(0); cellPravno.setPadding(7);
        dodajKvNaslov(cellPravno, "PRAVNE NAPOMENE");

        String pdvStatus = firma.isPdvObveznik() ? "Da — PDV obveznik" : "Ne — nije u sistemu PDV-a";
        dodajKvRed(cellPravno, "Firma u sistemu PDV-a:", pdvStatus);

        String adresa = firma.getAdresa();
        if (adresa != null && !adresa.isBlank()) {
            String grad = adresa.contains(",")
                    ? adresa.substring(adresa.lastIndexOf(',') + 1).trim()
                    : adresa.trim();
            if (!grad.isBlank())
                dodajKvRed(cellPravno, "Nadležni sud:", "Sud u " + grad);
        }

        t.addCell(cellPlacanje);
        t.addCell(cellPravno);
        doc.add(t);
    }

    private static void dodajRecapRed(PdfPTable t, String label, String value) {
        PdfPCell lbl = new PdfPCell(new Phrase(label, FONT_RECAP_LABEL));
        lbl.setBorder(Rectangle.NO_BORDER); lbl.setPadding(3);
        PdfPCell val = new PdfPCell(new Phrase(value, FONT_RECAP_VAL));
        val.setBorder(Rectangle.NO_BORDER); val.setPadding(3);
        val.setHorizontalAlignment(Element.ALIGN_RIGHT);
        t.addCell(lbl); t.addCell(val);
    }

    private static void dodajPotpisi(Document doc) throws Exception {
        doc.add(Chunk.NEWLINE);
        PdfPTable potpisTable = new PdfPTable(3);
        potpisTable.setWidthPercentage(100);
        potpisTable.setWidths(new float[]{44, 12, 44});
        potpisTable.setSpacingAfter(7);

        PdfPCell cs = new PdfPCell();
        cs.setBorder(Rectangle.NO_BORDER); cs.setPadding(4); cs.setFixedHeight(30);
        cs.addElement(new Paragraph("Izdao / Pečat:", FONT_POTPIS_LABEL));

        PdfPCell sp1 = new PdfPCell(); sp1.setBorder(Rectangle.NO_BORDER);

        PdfPCell cp = new PdfPCell();
        cp.setBorder(Rectangle.NO_BORDER); cp.setPadding(4); cp.setFixedHeight(30);
        Paragraph pp = new Paragraph("Primio / Potpis:", FONT_POTPIS_LABEL);
        pp.setAlignment(Element.ALIGN_RIGHT);
        cp.addElement(pp);

        PdfPCell ls = new PdfPCell();
        ls.setBorderWidthTop(1f); ls.setBorderColorTop(CRNA);
        ls.setBorderWidthBottom(0); ls.setBorderWidthLeft(0); ls.setBorderWidthRight(0);
        ls.setPaddingTop(3);
        Paragraph pss = new Paragraph("ime i prezime / potpis", FONT_POTPIS_SUB);
        pss.setAlignment(Element.ALIGN_CENTER);
        ls.addElement(pss);

        PdfPCell sp2 = new PdfPCell(); sp2.setBorder(Rectangle.NO_BORDER);

        PdfPCell lp = new PdfPCell();
        lp.setBorderWidthTop(1f); lp.setBorderColorTop(CRNA);
        lp.setBorderWidthBottom(0); lp.setBorderWidthLeft(0); lp.setBorderWidthRight(0);
        lp.setPaddingTop(3);
        Paragraph psp = new Paragraph("ime i prezime / potpis", FONT_POTPIS_SUB);
        psp.setAlignment(Element.ALIGN_CENTER);
        lp.addElement(psp);

        potpisTable.addCell(cs); potpisTable.addCell(sp1); potpisTable.addCell(cp);
        potpisTable.addCell(ls); potpisTable.addCell(sp2); potpisTable.addCell(lp);
        doc.add(potpisTable);
    }

    private static void dodajFooter(Document doc) throws Exception {
        PdfPTable footer = new PdfPTable(1);
        footer.setWidthPercentage(100);
        PdfPCell fc = new PdfPCell();
        fc.setBorderColor(BORDER_SIVA); fc.setBorderWidth(0.5f); fc.setPadding(5);
        Paragraph fn = new Paragraph("Napomena", new Font(BF_BOLD, 7, Font.NORMAL, CRNA));
        fn.setSpacingAfter(2); fc.addElement(fn);
        Paragraph fp = new Paragraph(
                "Ovaj dokument je kreiran elektronski i služi kao osnova za plaćanje. " +
                "Plaćanje se vrši na žiro račun naznačen u dokumentu ili gotovinom. " +
                "U slučaju prigovora kontaktirati nas u roku od 8 dana od prijema dokumenta.",
                FONT_FOOTER);
        fp.setLeading(10); fc.addElement(fp);
        footer.addCell(fc);
        doc.add(footer);
    }

    // ── POMOCNE METODE ─────────────────────────────────────────────

    private static PdfPCell kreirajMetaItem(String label, String value) {
        PdfPCell cell = new PdfPCell();
        cell.setBorderColor(CRNA); cell.setBorderWidth(0.5f); cell.setPadding(5);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        Paragraph pl = new Paragraph(label, FONT_DOK_LABEL);
        pl.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(pl);
        Paragraph pv = new Paragraph(value, FONT_DOK_VAL);
        pv.setAlignment(Element.ALIGN_CENTER);
        cell.addElement(pv);
        return cell;
    }

    private static PdfPCell kreirajDivider() {
        PdfPCell c = new PdfPCell();
        c.setBorderColor(BORDER_SIVA); c.setBorderWidth(0.5f); c.setFixedHeight(32);
        return c;
    }

    private static Paragraph kreirajSecNaslov(String tekst) {
        Paragraph p = new Paragraph(tekst, FONT_SEC_NASLOV);
        p.setSpacingBefore(2); p.setSpacingAfter(2);
        LineSeparator ls = new LineSeparator(1.5f, 100, PLAVA, Element.ALIGN_LEFT, -2);
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
        lbl.setBorder(Rectangle.NO_BORDER); lbl.setPaddingBottom(2);
        PdfPCell val = new PdfPCell(new Phrase(value, FONT_KV_VAL));
        val.setBorder(Rectangle.NO_BORDER); val.setPaddingBottom(2);
        red.addCell(lbl); red.addCell(val);
        cell.addElement(red);
    }

    private static PdfPCell kreirajTH(String tekst) {
        PdfPCell c = new PdfPCell(new Phrase(tekst, FONT_TH));
        c.setPadding(4); c.setBorderColor(CRNA); c.setBorderWidth(0.5f);
        c.setBackgroundColor(new BaseColor(240, 244, 250));
        return c;
    }

    private static PdfPCell kreirajTD(String tekst, BaseColor boja, int align) {
        PdfPCell c = new PdfPCell(new Phrase(tekst, FONT_TD));
        c.setPadding(4); c.setBorderColor(BORDER_SIVA); c.setBorderWidth(0.5f);
        c.setBackgroundColor(boja); c.setHorizontalAlignment(align);
        return c;
    }

    private static String nvl(String s) { return s != null && !s.isBlank() ? s : "—"; }
    private static String fmt(double d) {
        return d == Math.floor(d) ? String.valueOf((int) d) : String.format("%.2f", d);
    }
    private static String fmtCena(double d) { return String.format("%,.2f RSD", d); }
    private static String fmtBroj(double d) { return String.format("%,.2f", d); }

    // Adapter klasa za zajednicku tabelu stavki
    private record StavkaRed(String naziv, double kolicina, String jm,
                              double cenaBezPdv, double pdvStopa, double iznosPdv, double iznosUkupno) {}

    private static List<StavkaRed> konvertujStavke(List<PredracunStavka> stavke) {
        return stavke.stream().map(s -> new StavkaRed(
                s.getNaziv(), s.getKolicina(), s.getJedinicaMere(),
                s.getCenaBezPdv(), s.getPdvStopa(), s.iznosPdv(), s.iznosUkupno()
        )).toList();
    }

    private static List<StavkaRed> konvertujFakturaStavke(List<FakturaStavka> stavke) {
        return stavke.stream().map(s -> new StavkaRed(
                s.getNaziv(), s.getKolicina(), s.getJedinicaMere(),
                s.getCenaBezPdv(), s.getPdvStopa(), s.iznosPdv(), s.iznosUkupno()
        )).toList();
    }
}
