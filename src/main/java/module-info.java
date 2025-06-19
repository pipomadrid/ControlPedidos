module ControlPedidos {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.base;
    requires java.desktop;
    requires java.prefs;
    requires org.apache.poi.ooxml;
    requires org.apache.poi.poi;
    requires org.apache.commons.collections4;
    requires org.apache.xmlbeans;
    requires org.apache.commons.compress;


    opens com.pedrojosesaez.controlpedidos to javafx.fxml;
    exports com.pedrojosesaez.controlpedidos;
}