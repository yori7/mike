package com.github.yorinana.mike;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.embed.swing.SwingFXUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class MikeController {
    @FXML private Label statusText;
    @FXML private VBox canvas;
    @FXML private ImageView imageView;
    @FXML private VBox canvas2;
    @FXML private ImageView imageView2;
    private Image image;


    @FXML
    protected void onOpenButtonClick() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Open File");
        fc.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
        File file = fc.showOpenDialog(null);

        image = new Image("file:///" + file.getPath());
        imageView.setImage(image);
        imageView.fitWidthProperty().bind(canvas.widthProperty());
        imageView.fitHeightProperty().bind(canvas.heightProperty());
        imageView.setPreserveRatio(true);

        // test
        imageView2.setImage(image);
        imageView2.fitWidthProperty().bind(canvas2.widthProperty());
        imageView2.fitHeightProperty().bind(canvas2.heightProperty());
        imageView2.setPreserveRatio(true);

        statusText.setText(file.getName());
    }

    @FXML
    protected void onSaveButtonClick() {
        FileChooser fs = new FileChooser();
        fs.setTitle("Open File");
        fs.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
        File file = fs.showSaveDialog(null);
        if (file != null & image != null) {
            try {
                BufferedImage img = new BufferedImage(
                        (int) image.getWidth(),
                        (int) image.getHeight(),
                        BufferedImage.TYPE_INT_ARGB
                );
                SwingFXUtils.fromFXImage(image, img);
                ImageIO.write(img, getExt(file.getName()), file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected String getExt(String fileName) {
        int idx = fileName.lastIndexOf(".");
        String ext = fileName.substring(idx + 1);
        return ext.toLowerCase();
    }
}