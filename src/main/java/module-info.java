module net.universitecentrale.generateurquiz {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires mysql.connector.j;


    opens net.universitecentrale.generateurquiz to javafx.fxml;
    opens net.universitecentrale.generateurquiz.entity to javafx.base;
    exports net.universitecentrale.generateurquiz;
    exports net.universitecentrale.generateurquiz.controller;
    opens net.universitecentrale.generateurquiz.controller to javafx.fxml;
}