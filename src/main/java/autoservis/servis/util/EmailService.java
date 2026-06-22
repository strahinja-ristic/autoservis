package autoservis.servis.util;

import autoservis.servis.model.Podesavanja;

import javax.mail.*;
import javax.mail.internet.*;
import java.io.File;
import java.util.Properties;

public class EmailService {

    public static void posaljiPdf(Podesavanja firma, String primaocEmail,
                                   String subject, String tekst, String pdfPutanja) throws Exception {

        if (!FeatureFlagService.getInstance().isEnabled("feature_email"))
            throw new Exception("Slanje emailom nije dostupno u vašoj licenci.");

        if (firma.getGmailAdresa() == null || firma.getGmailAdresa().isBlank())
            throw new Exception("Gmail adresa nije podešena u podešavanjima.");
        if (firma.getGmailAppPassword() == null || firma.getGmailAppPassword().isBlank())
            throw new Exception("Gmail App Password nije podešen u podešavanjima.");
        if (primaocEmail == null || primaocEmail.isBlank())
            throw new Exception("Klijent nema upisanu email adresu.");

        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");

        String korisnik = firma.getGmailAdresa();
        String lozinka = firma.getGmailAppPassword();

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(korisnik, lozinka);
            }
        });

        Message poruka = new MimeMessage(session);
        poruka.setFrom(new InternetAddress(korisnik));
        poruka.setRecipients(Message.RecipientType.TO, InternetAddress.parse(primaocEmail));
        poruka.setSubject(subject);

        MimeBodyPart tekstPart = new MimeBodyPart();
        tekstPart.setText(tekst != null ? tekst : "");

        MimeBodyPart prilogPart = new MimeBodyPart();
        prilogPart.attachFile(new File(pdfPutanja));

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(tekstPart);
        multipart.addBodyPart(prilogPart);

        poruka.setContent(multipart);
        Transport.send(poruka);
    }
}
