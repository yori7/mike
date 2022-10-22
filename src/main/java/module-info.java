module com.github.yorinana.mike {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;


    opens com.github.yorinana.mike to javafx.fxml;
    exports com.github.yorinana.mike;
}