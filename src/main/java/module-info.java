module com.emlak.gui {
    requires javafx.controls;
    requires javafx.fxml;

    opens com.emlak.gui to javafx.graphics, javafx.fxml;
    exports com.emlak.gui;
    opens com.emlak.models to javafx.base;
}