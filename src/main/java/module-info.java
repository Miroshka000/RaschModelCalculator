module RaschModelCalculator {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.poi.ooxml;
    requires org.apache.poi.poi;
    requires atlantafx.base;

    requires commons.math3;
    requires org.apache.commons.collections4;
    requires org.apache.commons.codec;
    requires org.apache.commons.compress;
    requires org.apache.xmlbeans;
    requires org.apache.logging.log4j;
    requires java.desktop;

    opens miroshka.rasch.controller to javafx.fxml;
    opens miroshka.rasch.model to javafx.fxml, javafx.base;

    exports miroshka.rasch.logic;
    exports miroshka.rasch.model;
    exports miroshka.rasch.view;
    exports miroshka.rasch.utils;
    exports miroshka.rasch;
} 