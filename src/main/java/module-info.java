module com.github.yorinana.mike {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.swing;


    opens com.github.yorinana.mike to javafx.fxml;
    exports com.github.yorinana.mike;
    exports com.github.yorinana.mike.filters;
    opens com.github.yorinana.mike.filters to javafx.fxml;
    exports com.github.yorinana.mike.clustering;
    opens com.github.yorinana.mike.clustering to javafx.fxml;
}