package com.github.yorinana.mike;

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static com.github.yorinana.mike.Filters.flatField;
import static com.github.yorinana.mike.Filters.getRGB;
import static com.github.yorinana.mike.Kmeans.labels2BufImage;

public class MikeController {
    @FXML private Label statusText;
    @FXML private VBox canvas;
    @FXML private ImageView imageView;
    @FXML private VBox canvas2;
    @FXML private ImageView imageView2;
    private Image image;
    private Image image2;


    @FXML
    protected void onOpenButtonClick() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Open File");
        fc.setInitialDirectory(
                new File(System.getProperty("user.home"))
        );
        File file = fc.showOpenDialog(null);
        try {
            BufferedImage bufImage = ImageIO.read(new File(file.getPath()));
            image = SwingFXUtils.toFXImage(bufImage, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        imageView.setImage(image);
        imageView.fitWidthProperty().bind(canvas.widthProperty());
        imageView.fitHeightProperty().bind(canvas.heightProperty());
        imageView.setPreserveRatio(true);

        image2 = image;
        imageView2.setImage(image2);
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
        if (file != null & image2 != null) {
            try {
                BufferedImage img = new BufferedImage(
                        (int) image2.getWidth(),
                        (int) image2.getHeight(),
                        BufferedImage.TYPE_INT_ARGB
                );
                SwingFXUtils.fromFXImage(image2, img);
                ImageIO.write(img, getExt(file.getName()), file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    protected void onApplyButtonClick() {
        if (image != null) {
            BufferedImage bufImage = new BufferedImage(
                    (int) image.getWidth(),
                    (int) image.getHeight(),
                    BufferedImage.TYPE_INT_RGB
            );
            SwingFXUtils.fromFXImage(image, bufImage);
            applyAction(bufImage);
            imageView2.setImage(image2);
        }
    }

    protected String getExt(String fileName) {
        int idx = fileName.lastIndexOf(".");
        String ext = fileName.substring(idx + 1);
        return ext.toLowerCase();
    }

    protected void applyAction(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        int k = 5;
        int maxIter = 30;

        BufferedImage flatImg = flatField(img);

        Kmeans model = new Kmeans(k, maxIter);
        int[] flatImgData = flatImg.getRGB(0, 0, flatImg.getWidth(), flatImg.getHeight(), null, 0, flatImg.getWidth());
        int[][] flatImgRGB = new int[flatImgData.length][3];
        for (int i = 0; i < flatImgData.length; i++) {
            flatImgRGB[i] = getRGB(flatImgData[i]);
        }
        model.fit(flatImgRGB);
        int[] labels = model.predict(flatImgRGB);
        BufferedImage[] sepImg = labels2BufImage(labels, w, h, k);

        image2 = SwingFXUtils.toFXImage(sepImg[0], null);
    }
}