module com.example.tennis {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires lombok;
    requires java.naming;
    requires com.google.gson;
    requires org.hibernate.orm.core;
    requires java.persistence;
    requires java.sql;
    requires org.jsoup;
    requires java.net.http;
    requires org.json;

    opens com.example.tennis to javafx.fxml;
    exports com.example.tennis;
    exports com.example.tennis.entity;
    opens com.example.tennis.entity to com.google.gson, org.hibernate.orm.core;
    exports com.example.tennis.fx;
    opens com.example.tennis.fx to javafx.fxml;
}