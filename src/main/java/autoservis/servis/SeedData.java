package autoservis.servis;

import autoservis.servis.util.DatabaseManager;

import java.sql.*;

public class SeedData {

    public static void main(String[] args) throws Exception {
        DatabaseManager.initialize();
        Connection c = DatabaseManager.getConnection();
        c.createStatement().execute("PRAGMA foreign_keys = OFF");

        int n = 0;

        // ── PODESAVANJA ──────────────────────────────────────────────
        n += exec(c, "INSERT OR REPLACE INTO podesavanja " +
            "(id, naziv_firme, adresa, telefon, email, pib, maticni_broj, \"žiro_racun\"," +
            " pdv_obveznik, pdv_stopa, default_rok_placanja," +
            " gmail_adresa, gmail_app_password, email_tekst_predracun, email_tekst_faktura) " +
            "VALUES (1, 'AutoServis Petrović', 'Knez Mihailova 14, Beograd', '011 123 4567'," +
            " 'servis@petrovicauto.rs', '101234567', '12345678', '160-123456789-01'," +
            " 1, 20.0, 15, '', ''," +
            " 'U prilogu se nalazi predračun. Javite nam se ako imate pitanja.'," +
            " 'U prilogu se nalazi faktura. Molimo izvršite uplatu u roku.')");

        // ── KLIJENTI ─────────────────────────────────────────────────
        n += exec(c, "INSERT OR IGNORE INTO klijenti (id,tip,ime,prezime,adresa,telefon,email) VALUES (1,'Fizičko','Marko','Jovanović','Cara Dušana 5, Beograd','064 111 2233','marko.j@gmail.com')");
        n += exec(c, "INSERT OR IGNORE INTO klijenti (id,tip,ime,prezime,adresa,telefon,email) VALUES (2,'Fizičko','Ana','Nikolić','Vojvode Stepe 88, Beograd','063 444 5566','ana.nikolic@gmail.com')");
        n += exec(c, "INSERT OR IGNORE INTO klijenti (id,tip,ime,prezime,adresa,telefon,email) VALUES (3,'Fizičko','Stefan','Milovanović','Bulevar Oslobođenja 22, Novi Sad','065 777 8899','stefan.m@gmail.com')");
        n += exec(c, "INSERT OR IGNORE INTO klijenti (id,tip,naziv_firme,pib,maticni_broj,adresa,telefon,email,napomena) VALUES (4,'Pravno','Logistika DOO','987654321','55667788','Industrijska 3, Zemun','011 987 6543','nabavka@logistika.rs','Plaća virmanom')");
        n += exec(c, "INSERT OR IGNORE INTO klijenti (id,tip,ime,prezime,adresa,telefon,email) VALUES (5,'Fizičko','Jelena','Stojanović','Rige od Fere 7, Beograd','062 333 4455','jelena.s@gmail.com')");

        // ── VOZILA ───────────────────────────────────────────────────
        // Šema: id, klijent_id, marka, model, godiste, registracija, broj_sasije, kilometraza
        n += exec(c, "INSERT OR IGNORE INTO vozila (id,klijent_id,marka,model,godiste,registracija,broj_sasije,kilometraza) VALUES (1,1,'Volkswagen','Golf 7',2018,'BG 123-AB','WVWZZZ1KZAW123456',87500)");
        n += exec(c, "INSERT OR IGNORE INTO vozila (id,klijent_id,marka,model,godiste,registracija,broj_sasije,kilometraza) VALUES (2,1,'BMW','E46 320d',2003,'BG 456-CD','12345678901234567',210000)");
        n += exec(c, "INSERT OR IGNORE INTO vozila (id,klijent_id,marka,model,godiste,registracija,broj_sasije,kilometraza) VALUES (3,2,'Renault','Clio 4',2015,'NS 789-EF','VF1234567890ABCDE',45200)");
        n += exec(c, "INSERT OR IGNORE INTO vozila (id,klijent_id,marka,model,godiste,registracija,broj_sasije,kilometraza) VALUES (4,3,'Škoda','Octavia 3',2019,'NS 321-GH','TMBAXXXXXXX567890',62000)");
        n += exec(c, "INSERT OR IGNORE INTO vozila (id,klijent_id,marka,model,godiste,registracija,broj_sasije,kilometraza) VALUES (5,4,'Mercedes','Sprinter 316',2020,'BG 654-IJ','WDB90363710XXXYYY',95000)");
        n += exec(c, "INSERT OR IGNORE INTO vozila (id,klijent_id,marka,model,godiste,registracija,broj_sasije,kilometraza) VALUES (6,5,'Toyota','Yaris',2021,'BG 999-KL','JTDKB20U00ZXXXXXX',18500)");

        // ── ARTIKLI ──────────────────────────────────────────────────
        // Šema: id, naziv, jedinica_mere, kolicina, nabavna_cena, prodajna_cena, minimalna_kolicina
        n += exec(c, "INSERT OR IGNORE INTO artikli (id,naziv,jedinica_mere,kolicina,nabavna_cena,prodajna_cena,minimalna_kolicina) VALUES (1,'Motorno ulje 5W-40 1L','kom',50,500,800,10)");
        n += exec(c, "INSERT OR IGNORE INTO artikli (id,naziv,jedinica_mere,kolicina,nabavna_cena,prodajna_cena,minimalna_kolicina) VALUES (2,'Filter ulja Golf 7 1.6 TDI','kom',20,380,650,5)");
        n += exec(c, "INSERT OR IGNORE INTO artikli (id,naziv,jedinica_mere,kolicina,nabavna_cena,prodajna_cena,minimalna_kolicina) VALUES (3,'Filter vazduha Golf 7','kom',15,550,900,5)");
        n += exec(c, "INSERT OR IGNORE INTO artikli (id,naziv,jedinica_mere,kolicina,nabavna_cena,prodajna_cena,minimalna_kolicina) VALUES (4,'Kočiona pločica prednja BMW E46','kom',8,1400,2200,4)");
        n += exec(c, "INSERT OR IGNORE INTO artikli (id,naziv,jedinica_mere,kolicina,nabavna_cena,prodajna_cena,minimalna_kolicina) VALUES (5,'Svećice NGK BKR6E (4kom)','kom',12,750,1200,4)");
        n += exec(c, "INSERT OR IGNORE INTO artikli (id,naziv,jedinica_mere,kolicina,nabavna_cena,prodajna_cena,minimalna_kolicina) VALUES (6,'Antifriz 5L','lit',10,550,900,5)");
        n += exec(c, "INSERT OR IGNORE INTO artikli (id,naziv,jedinica_mere,kolicina,nabavna_cena,prodajna_cena,minimalna_kolicina) VALUES (7,'Klinasti kaiš univerzalni','kom',6,900,1500,3)");

        // ── RADNI NALOZI ─────────────────────────────────────────────
        // Šema: id,broj_naloga,klijent_id,vozilo_id,datum_prijema,datum_zavrsetka,
        //       kilometraza_prijema,sledeci_servis_km,sledeci_servis_datum,
        //       status,opis_kvara,zahtev_klijenta,napomena,faktura,ostecenja
        n += exec(c, "INSERT OR IGNORE INTO radni_nalozi (id,broj_naloga,klijent_id,vozilo_id,datum_prijema,datum_zavrsetka,kilometraza_prijema,sledeci_servis_km,status,opis_kvara,zahtev_klijenta,napomena,faktura,ostecenja) VALUES (1,'1-26',1,1,'10.01.2026','10.01.2026',87500,97500,'Završeno','Redovan servis — menjanje ulja i filtera','Klijent zahteva punjenje tečnosti za pranje vetrobrana','Sve urađeno po planu. Filtri bili jako prljavi.','F-1-26','Manja ogrebotina na zadnjem braniku')");
        n += exec(c, "INSERT OR IGNORE INTO radni_nalozi (id,broj_naloga,klijent_id,vozilo_id,datum_prijema,datum_zavrsetka,kilometraza_prijema,status,opis_kvara,zahtev_klijenta,napomena) VALUES (2,'2-26',2,3,'15.01.2026','16.01.2026',45200,'Završeno','Pali se lampica motora, sumnja na senzor','Lampica gori 2 nedelje','Zamenjen lambda senzor. Lampica ugašena.')");
        n += exec(c, "INSERT OR IGNORE INTO radni_nalozi (id,broj_naloga,klijent_id,vozilo_id,datum_prijema,datum_zavrsetka,kilometraza_prijema,sledeci_servis_km,status,opis_kvara,zahtev_klijenta,napomena) VALUES (3,'3-26',3,4,'20.02.2026','21.02.2026',62000,72000,'Završeno','Škripanje pri kočenju, proveriti pločice','Škripanje se čuje samo ujutru','Zamenjene prednje kočione pločice i diskovi.')");
        n += exec(c, "INSERT OR IGNORE INTO radni_nalozi (id,broj_naloga,klijent_id,vozilo_id,datum_prijema,kilometraza_prijema,status,opis_kvara,zahtev_klijenta,ostecenja) VALUES (4,'4-26',1,2,'05.03.2026',210000,'U radu','Ulje curi ispod motora, proveriti zaptivke','Curenje pre mesec dana, sve jače','Ogrebotine na prednjem braniku levo')");
        n += exec(c, "INSERT OR IGNORE INTO radni_nalozi (id,broj_naloga,klijent_id,vozilo_id,datum_prijema,kilometraza_prijema,sledeci_servis_km,sledeci_servis_datum,status,opis_kvara,zahtev_klijenta) VALUES (5,'5-26',4,5,'10.03.2026',95000,105000,'10.06.2026','Primljeno','Redovan servis po ugovoru — svaka 10000km','Proveriti i kočioni sistem')");
        n += exec(c, "INSERT OR IGNORE INTO radni_nalozi (id,broj_naloga,klijent_id,vozilo_id,datum_prijema,kilometraza_prijema,status,opis_kvara,zahtev_klijenta) VALUES (6,'6-26',5,6,'01.04.2026',18500,'U radu','Baterija ne drži punjenje, teško pali','Auto stoji u garaži i slabo se vozi')");

        // ── USLUGE NA NALOZIMA ───────────────────────────────────────
        // Šema: id, radni_nalog_id, opis
        n += exec(c, "INSERT OR IGNORE INTO usluge (radni_nalog_id,opis) VALUES (1,'Menjanje motornog ulja')");
        n += exec(c, "INSERT OR IGNORE INTO usluge (radni_nalog_id,opis) VALUES (1,'Zamena filtera ulja')");
        n += exec(c, "INSERT OR IGNORE INTO usluge (radni_nalog_id,opis) VALUES (1,'Zamena filtera vazduha')");
        n += exec(c, "INSERT OR IGNORE INTO usluge (radni_nalog_id,opis) VALUES (1,'Punjenje tečnosti za pranje vetrobrana')");
        n += exec(c, "INSERT OR IGNORE INTO usluge (radni_nalog_id,opis) VALUES (2,'Dijagnostika OBD')");
        n += exec(c, "INSERT OR IGNORE INTO usluge (radni_nalog_id,opis) VALUES (2,'Zamena lambda senzora')");
        n += exec(c, "INSERT OR IGNORE INTO usluge (radni_nalog_id,opis) VALUES (3,'Zamena prednjih kočionih pločica')");
        n += exec(c, "INSERT OR IGNORE INTO usluge (radni_nalog_id,opis) VALUES (3,'Zamena prednjih kočionih diskova')");
        n += exec(c, "INSERT OR IGNORE INTO usluge (radni_nalog_id,opis) VALUES (4,'Dijagnostika curenja ulja')");
        n += exec(c, "INSERT OR IGNORE INTO usluge (radni_nalog_id,opis) VALUES (5,'Zamena motornog ulja i filtera')");
        n += exec(c, "INSERT OR IGNORE INTO usluge (radni_nalog_id,opis) VALUES (6,'Provera i zamena baterije')");

        // ── ARTIKLI NA NALOZIMA ──────────────────────────────────────
        // Šema: id, radni_nalog_id, artikal_id, kolicina
        n += exec(c, "INSERT OR IGNORE INTO nalog_artikli (radni_nalog_id,artikal_id,kolicina) VALUES (1,1,4)");
        n += exec(c, "INSERT OR IGNORE INTO nalog_artikli (radni_nalog_id,artikal_id,kolicina) VALUES (1,2,1)");
        n += exec(c, "INSERT OR IGNORE INTO nalog_artikli (radni_nalog_id,artikal_id,kolicina) VALUES (1,3,1)");
        n += exec(c, "INSERT OR IGNORE INTO nalog_artikli (radni_nalog_id,artikal_id,kolicina) VALUES (3,4,4)");
        n += exec(c, "INSERT OR IGNORE INTO nalog_artikli (radni_nalog_id,artikal_id,kolicina) VALUES (5,1,5)");
        n += exec(c, "INSERT OR IGNORE INTO nalog_artikli (radni_nalog_id,artikal_id,kolicina) VALUES (5,2,1)");

        // ── ULAZ ROBE ────────────────────────────────────────────────
        // Šema: id, artikal_id, kolicina, datum, napomena
        n += exec(c, "INSERT OR IGNORE INTO ulaz_magacin (id,artikal_id,kolicina,datum,napomena) VALUES (1,1,20,'2026-01-02','Početna nabavka')");
        n += exec(c, "INSERT OR IGNORE INTO ulaz_magacin (id,artikal_id,kolicina,datum,napomena) VALUES (2,2,10,'2026-01-02','Početna nabavka')");
        n += exec(c, "INSERT OR IGNORE INTO ulaz_magacin (id,artikal_id,kolicina,datum,napomena) VALUES (3,3,10,'2026-01-02','Početna nabavka')");
        n += exec(c, "INSERT OR IGNORE INTO ulaz_magacin (id,artikal_id,kolicina,datum,napomena) VALUES (4,4,8,'2026-01-02','Početna nabavka')");
        n += exec(c, "INSERT OR IGNORE INTO ulaz_magacin (id,artikal_id,kolicina,datum,napomena) VALUES (5,1,30,'2026-02-15','Dopuna zaliha')");
        n += exec(c, "INSERT OR IGNORE INTO ulaz_magacin (id,artikal_id,kolicina,datum,napomena) VALUES (6,5,12,'2026-02-15','Dopuna zaliha')");
        n += exec(c, "INSERT OR IGNORE INTO ulaz_magacin (id,artikal_id,kolicina,datum,napomena) VALUES (7,6,10,'2026-03-01','Proleće — rashladni sistemi')");

        // ── PREDRACUNI ───────────────────────────────────────────────
        n += exec(c, "INSERT OR IGNORE INTO predracuni (id,broj_predracuna,klijent_id,vozilo_id,radni_nalog_id,datum_kreiranja,datum_vazenja,status,nacin_placanja,rok_placanja,mesto_izdavanja,popust_procenat) VALUES (1,'PR-1-26',1,1,1,'10.01.2026','25.01.2026','Fakturisan','Gotovina','7 dana','Beograd',0)");
        n += exec(c, "INSERT OR IGNORE INTO predracuni (id,broj_predracuna,klijent_id,vozilo_id,radni_nalog_id,datum_kreiranja,datum_vazenja,status,nacin_placanja,rok_placanja,mesto_izdavanja,napomena,popust_procenat) VALUES (2,'PR-2-26',3,4,3,'20.02.2026','06.03.2026','Fakturisan','Prenos','15 dana','Beograd','Posebna cena za redovnog klijenta',5)");
        n += exec(c, "INSERT OR IGNORE INTO predracuni (id,broj_predracuna,klijent_id,vozilo_id,radni_nalog_id,datum_kreiranja,datum_vazenja,status,nacin_placanja,rok_placanja,mesto_izdavanja,napomena,popust_procenat) VALUES (3,'PR-3-26',4,5,5,'10.03.2026','25.03.2026','Prihvaćen','Prenos','30 dana','Beograd','Korporativni klijent',10)");
        n += exec(c, "INSERT OR IGNORE INTO predracuni (id,broj_predracuna,klijent_id,vozilo_id,radni_nalog_id,datum_kreiranja,datum_vazenja,status,nacin_placanja,rok_placanja,mesto_izdavanja,popust_procenat) VALUES (4,'PR-4-26',5,6,6,'01.04.2026','16.04.2026','Poslat','Gotovina','7 dana','Beograd',0)");
        n += exec(c, "INSERT OR IGNORE INTO predracuni (id,broj_predracuna,klijent_id,vozilo_id,radni_nalog_id,datum_kreiranja,datum_vazenja,status,nacin_placanja,rok_placanja,mesto_izdavanja,popust_procenat) VALUES (5,'PR-5-26',2,3,2,'15.01.2026','30.01.2026','Kreiran','Kartica','0 dana','Beograd',0)");

        // ── STAVKE PREDRACUNA ────────────────────────────────────────
        n += exec(c, "INSERT OR IGNORE INTO predracun_stavke (predracun_id,tip,naziv,kolicina,jedinica_mere,cena_bez_pdv,pdv_stopa,popust_procenat,redni_broj) VALUES (1,'Usluga','Menjanje motornog ulja',1,'usl',1500,20,0,1)");
        n += exec(c, "INSERT OR IGNORE INTO predracun_stavke (predracun_id,tip,naziv,kolicina,jedinica_mere,cena_bez_pdv,pdv_stopa,popust_procenat,redni_broj) VALUES (1,'Artikal','Motorno ulje 5W-40 (4L)',4,'kom',800,20,0,2)");
        n += exec(c, "INSERT OR IGNORE INTO predracun_stavke (predracun_id,tip,naziv,kolicina,jedinica_mere,cena_bez_pdv,pdv_stopa,popust_procenat,redni_broj) VALUES (1,'Artikal','Filter ulja',1,'kom',650,20,0,3)");
        n += exec(c, "INSERT OR IGNORE INTO predracun_stavke (predracun_id,tip,naziv,kolicina,jedinica_mere,cena_bez_pdv,pdv_stopa,popust_procenat,redni_broj) VALUES (1,'Artikal','Filter vazduha',1,'kom',900,20,0,4)");
        n += exec(c, "INSERT OR IGNORE INTO predracun_stavke (predracun_id,tip,naziv,kolicina,jedinica_mere,cena_bez_pdv,pdv_stopa,popust_procenat,redni_broj) VALUES (2,'Usluga','Zamena prednjih kočionih pločica',1,'usl',2500,20,0,1)");
        n += exec(c, "INSERT OR IGNORE INTO predracun_stavke (predracun_id,tip,naziv,kolicina,jedinica_mere,cena_bez_pdv,pdv_stopa,popust_procenat,redni_broj) VALUES (2,'Usluga','Zamena prednjih kočionih diskova',1,'usl',1500,20,0,2)");
        n += exec(c, "INSERT OR IGNORE INTO predracun_stavke (predracun_id,tip,naziv,kolicina,jedinica_mere,cena_bez_pdv,pdv_stopa,popust_procenat,redni_broj) VALUES (2,'Artikal','Kočione pločice Brembo',4,'kom',2200,20,0,3)");
        n += exec(c, "INSERT OR IGNORE INTO predracun_stavke (predracun_id,tip,naziv,kolicina,jedinica_mere,cena_bez_pdv,pdv_stopa,popust_procenat,redni_broj) VALUES (3,'Usluga','Redovan servis',1,'usl',3000,20,0,1)");
        n += exec(c, "INSERT OR IGNORE INTO predracun_stavke (predracun_id,tip,naziv,kolicina,jedinica_mere,cena_bez_pdv,pdv_stopa,popust_procenat,redni_broj) VALUES (3,'Artikal','Motorno ulje 5W-40 (5L)',5,'kom',800,20,0,2)");
        n += exec(c, "INSERT OR IGNORE INTO predracun_stavke (predracun_id,tip,naziv,kolicina,jedinica_mere,cena_bez_pdv,pdv_stopa,popust_procenat,redni_broj) VALUES (3,'Artikal','Filter ulja Sprinter',1,'kom',850,20,0,3)");
        n += exec(c, "INSERT OR IGNORE INTO predracun_stavke (predracun_id,tip,naziv,kolicina,jedinica_mere,cena_bez_pdv,pdv_stopa,popust_procenat,redni_broj) VALUES (3,'Usluga','Provera kočionog sistema',1,'usl',1500,20,0,4)");
        n += exec(c, "INSERT OR IGNORE INTO predracun_stavke (predracun_id,tip,naziv,kolicina,jedinica_mere,cena_bez_pdv,pdv_stopa,popust_procenat,redni_broj) VALUES (4,'Usluga','Provera i zamena baterije',1,'usl',500,20,0,1)");
        n += exec(c, "INSERT OR IGNORE INTO predracun_stavke (predracun_id,tip,naziv,kolicina,jedinica_mere,cena_bez_pdv,pdv_stopa,popust_procenat,redni_broj) VALUES (4,'Artikal','Akumulator 60Ah Bosch',1,'kom',8500,20,0,2)");
        n += exec(c, "INSERT OR IGNORE INTO predracun_stavke (predracun_id,tip,naziv,kolicina,jedinica_mere,cena_bez_pdv,pdv_stopa,popust_procenat,redni_broj) VALUES (5,'Usluga','Dijagnostika OBD',1,'usl',800,20,0,1)");
        n += exec(c, "INSERT OR IGNORE INTO predracun_stavke (predracun_id,tip,naziv,kolicina,jedinica_mere,cena_bez_pdv,pdv_stopa,popust_procenat,redni_broj) VALUES (5,'Usluga','Zamena lambda senzora',1,'usl',2000,20,0,2)");
        n += exec(c, "INSERT OR IGNORE INTO predracun_stavke (predracun_id,tip,naziv,kolicina,jedinica_mere,cena_bez_pdv,pdv_stopa,popust_procenat,redni_broj) VALUES (5,'Artikal','Lambda senzor Renault Clio',1,'kom',3500,20,0,3)");

        // ── FAKTURE ──────────────────────────────────────────────────
        n += exec(c, "INSERT OR IGNORE INTO fakture (id,broj_fakture,klijent_id,vozilo_id,radni_nalog_id,predracun_id,datum_kreiranja,datum_placanja,status,nacin_placanja,rok_placanja,mesto_izdavanja,mesto_isporuke,popust_procenat) VALUES (1,'F-1-26',1,1,1,1,'10.01.2026','17.01.2026','Plaćena','Gotovina','7 dana','Beograd','Beograd',0)");
        n += exec(c, "INSERT OR IGNORE INTO fakture (id,broj_fakture,klijent_id,vozilo_id,radni_nalog_id,predracun_id,datum_kreiranja,datum_placanja,status,nacin_placanja,rok_placanja,mesto_izdavanja,mesto_isporuke,napomena,popust_procenat) VALUES (2,'F-2-26',3,4,3,2,'21.02.2026','08.03.2026','Poslata','Prenos','15 dana','Beograd','Novi Sad','Posebna cena',5)");
        n += exec(c, "INSERT OR IGNORE INTO fakture (id,broj_fakture,klijent_id,vozilo_id,datum_kreiranja,datum_placanja,status,nacin_placanja,rok_placanja,mesto_izdavanja,mesto_isporuke,napomena,popust_procenat) VALUES (3,'F-3-26',4,5,'15.03.2026','14.04.2026','Kreirana','Prenos','30 dana','Beograd','Zemun','Korporativni klijent',10)");

        // ── STAVKE FAKTURA ───────────────────────────────────────────
        n += exec(c, "INSERT OR IGNORE INTO faktura_stavke (faktura_id,tip,naziv,kolicina,jedinica_mere,cena_bez_pdv,pdv_stopa,popust_procenat,redni_broj) VALUES (1,'Usluga','Menjanje motornog ulja',1,'usl',1500,20,0,1)");
        n += exec(c, "INSERT OR IGNORE INTO faktura_stavke (faktura_id,tip,naziv,kolicina,jedinica_mere,cena_bez_pdv,pdv_stopa,popust_procenat,redni_broj) VALUES (1,'Artikal','Motorno ulje 5W-40 (4L)',4,'kom',800,20,0,2)");
        n += exec(c, "INSERT OR IGNORE INTO faktura_stavke (faktura_id,tip,naziv,kolicina,jedinica_mere,cena_bez_pdv,pdv_stopa,popust_procenat,redni_broj) VALUES (1,'Artikal','Filter ulja',1,'kom',650,20,0,3)");
        n += exec(c, "INSERT OR IGNORE INTO faktura_stavke (faktura_id,tip,naziv,kolicina,jedinica_mere,cena_bez_pdv,pdv_stopa,popust_procenat,redni_broj) VALUES (1,'Artikal','Filter vazduha',1,'kom',900,20,0,4)");
        n += exec(c, "INSERT OR IGNORE INTO faktura_stavke (faktura_id,tip,naziv,kolicina,jedinica_mere,cena_bez_pdv,pdv_stopa,popust_procenat,redni_broj) VALUES (2,'Usluga','Zamena prednjih kočionih pločica',1,'usl',2500,20,0,1)");
        n += exec(c, "INSERT OR IGNORE INTO faktura_stavke (faktura_id,tip,naziv,kolicina,jedinica_mere,cena_bez_pdv,pdv_stopa,popust_procenat,redni_broj) VALUES (2,'Usluga','Zamena prednjih kočionih diskova',1,'usl',1500,20,0,2)");
        n += exec(c, "INSERT OR IGNORE INTO faktura_stavke (faktura_id,tip,naziv,kolicina,jedinica_mere,cena_bez_pdv,pdv_stopa,popust_procenat,redni_broj) VALUES (2,'Artikal','Kočione pločice Brembo',4,'kom',2200,20,0,3)");
        n += exec(c, "INSERT OR IGNORE INTO faktura_stavke (faktura_id,tip,naziv,kolicina,jedinica_mere,cena_bez_pdv,pdv_stopa,popust_procenat,redni_broj) VALUES (3,'Usluga','Redovan servis po ugovoru',1,'usl',3000,20,0,1)");
        n += exec(c, "INSERT OR IGNORE INTO faktura_stavke (faktura_id,tip,naziv,kolicina,jedinica_mere,cena_bez_pdv,pdv_stopa,popust_procenat,redni_broj) VALUES (3,'Artikal','Motorno ulje 5W-40 (5L)',5,'kom',800,20,0,2)");
        n += exec(c, "INSERT OR IGNORE INTO faktura_stavke (faktura_id,tip,naziv,kolicina,jedinica_mere,cena_bez_pdv,pdv_stopa,popust_procenat,redni_broj) VALUES (3,'Artikal','Filter ulja Sprinter',1,'kom',850,20,0,3)");
        n += exec(c, "INSERT OR IGNORE INTO faktura_stavke (faktura_id,tip,naziv,kolicina,jedinica_mere,cena_bez_pdv,pdv_stopa,popust_procenat,redni_broj) VALUES (3,'Usluga','Provera kočionog sistema',1,'usl',1500,20,0,4)");

        System.out.println("Ubaceno " + n + " redova.");
    }

    static int exec(Connection c, String sql) {
        try {
            c.createStatement().execute(sql);
            return 1;
        } catch (SQLException e) {
            System.out.println("GRESKA: " + e.getMessage());
            return 0;
        }
    }
}
