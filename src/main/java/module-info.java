module ControlPedidos {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.desktop;
    requires java.prefs;
    requires org.apache.poi.ooxml;

    opens com.pedrojosesaez.controlpedidos to javafx.fxml,org.apache.poi.ooxml,org.apache.poi.ooxml.schemas;
    exports com.pedrojosesaez.controlpedidos;
}