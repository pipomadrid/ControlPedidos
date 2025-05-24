module ControlPedidos {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.desktop;
    requires org.apache.poi.poi;
    requires org.apache.poi.ooxml;
    requires java.prefs;

    opens com.pedrojosesaez.controlpedidos to javafx.fxml;
    exports com.pedrojosesaez.controlpedidos;
}