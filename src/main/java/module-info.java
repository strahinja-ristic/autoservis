module autoservis.servis {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;
    requires java.sql;
    requires java.logging;
    requires org.xerial.sqlitejdbc;
    requires itextpdf;
    requires org.apache.pdfbox;
    requires javafx.swing;
    requires java.mail;
    requires java.net.http;

    opens autoservis.servis.controller to javafx.fxml;
    exports autoservis.servis;
    opens autoservis.servis.util to javafx.fxml;
}