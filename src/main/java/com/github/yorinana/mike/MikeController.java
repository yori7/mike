package com.github.yorinana.mike;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MikeController {
    @FXML
    private Label welcomeText;

    @FXML
    protected void onHelloButtonClick() {
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}