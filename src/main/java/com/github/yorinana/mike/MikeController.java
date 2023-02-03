package com.github.yorinana.mike;

import com.github.yorinana.mike.clustering.Kmeans;
// import com.github.yorinana.mike.clustering.Ward;
import javafx.concurrent.Task;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.github.yorinana.mike.filters.Filters.flatField;
import static com.github.yorinana.mike.filters.Filters.getRGB;
import static com.github.yorinana.mike.clustering.Kmeans.labels2BufImage;

public class MikeController {
    @FXML public HBox imageViewer;
    @FXML private Label fileNameText;
    @FXML private VBox canvas;
    @FXML private ImageView imageView;
    @FXML private VBox canvas2;
    @FXML private ImageView imageView2;
    @FXML private Button openButton;
    @FXML private Button applyButton;
    @FXML private Button saveButton;
    @FXML private Label statusText;
    @FXML private CheckBox flatFieldCheck;
    @FXML private CheckBox thresholdingCheck;
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

        fileNameText.setText(file.getName());
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
            final ExecutorService executor = Executors.newSingleThreadExecutor();
            Task<Void> bgApplyTask = new Task<>() {
                @Override
                protected Void call() {
                    SwingFXUtils.fromFXImage(image, bufImage);
                    applyAction(bufImage);
                    return null;
                }

                @Override
                protected void running() {
                    super.running();
                    updateMessage("Processing");
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    updateMessage("Finish");
                    try {
                        TimeUnit.MILLISECONDS.sleep(12000);
                        updateMessage("Ready");
                    } catch (InterruptedException e) {
                        updateMessage("Ready");
                        throw new RuntimeException(e);
                    }

                }

                @Override
                protected void failed() {
                    super.failed();
                    updateMessage("Failed");
                    try {
                        TimeUnit.MILLISECONDS.sleep(12000);
                        updateMessage("Ready");
                    } catch (InterruptedException e) {
                        updateMessage("Ready");
                        throw new RuntimeException(e);
                    }
                }

                @Override
                protected void cancelled() {
                    super.cancelled();
                    updateMessage("Interrupted");
                    try {
                        TimeUnit.MILLISECONDS.sleep(12000);
                        updateMessage("Ready");
                    } catch (InterruptedException e) {
                        updateMessage("Ready");
                        throw new RuntimeException(e);
                    }
                }
            };
            openButton.disableProperty().bind(bgApplyTask.runningProperty());
            applyButton.disableProperty().bind(bgApplyTask.runningProperty());
            saveButton.disableProperty().bind(bgApplyTask.runningProperty());
            statusText.textProperty().bind(bgApplyTask.messageProperty());
            bgApplyTask.setOnSucceeded(workerStateEvent -> imageView2.setImage(image2));
            executor.submit(bgApplyTask);
            // imageView2.setImage(image2);
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
        int k = 3;
        int maxIter = 30;
        
        if (flatFieldCheck.isSelected()) {
            img = flatField(img);
        }
        
        if (thresholdingCheck.isSelected()) {
            Kmeans model = new Kmeans(k, maxIter);
            // Ward model = new Ward();
            int[] flatImgData = img.getRGB(0, 0, img.getWidth(), img.getHeight(), null, 0, img.getWidth());
            int[][] flatImgRGB = new int[flatImgData.length][3];
            for (int i = 0; i < flatImgData.length; i++) {
                flatImgRGB[i] = getRGB(flatImgData[i]);
            }
            model.fit(flatImgRGB);
            int[] labels = model.predict(flatImgRGB);
            BufferedImage[] sepImg = labels2BufImage(labels, w, h, k);
            image2 = SwingFXUtils.toFXImage(sepImg[0], null);
        } else {
            image2 = SwingFXUtils.toFXImage(img, null);
        }
    }
}