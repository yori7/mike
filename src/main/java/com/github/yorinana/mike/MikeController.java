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
import java.util.Arrays;
import java.util.Random;

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

        // test
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
                    BufferedImage.TYPE_INT_ARGB
            );
            SwingFXUtils.fromFXImage(image, bufImage);
            binalize(bufImage);
            imageView2.setImage(image2);
        }
    }

    protected String getExt(String fileName) {
        int idx = fileName.lastIndexOf(".");
        String ext = fileName.substring(idx + 1);
        return ext.toLowerCase();
    }

    protected void binalize(BufferedImage img) {
        BufferedImage flatImg = flatField(img);
        BufferedImage[] sepImg = kmeans(flatImg, 3);
        image2 = SwingFXUtils.toFXImage(sepImg[0], null);
    }

    protected BufferedImage flatField(BufferedImage img) {
        BufferedImage newImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
        int size = (int)Math.sqrt(img.getHeight() * img.getWidth() * 0.05);
        BufferedImage gausianImg = gausian(img, size);
        int[] ave = average(img);
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                int[] rgb = getRGB(img, x, y);
                int[] gausianRGB = getRGB(gausianImg, x, y);
                for (int i = 0; i < 3; i++) {
                    rgb[i] = rgb[i] / gausianRGB[i] * ave[i];
                }
                int newR = (rgb[0] << 16) & 0xff0000;
                int newG = (rgb[1] << 8) & 0x00ff00;
                int newB = rgb[2] & 0x0000ff;
                int newRGB = newR & newG & newB;
                newImg.setRGB(x, y, newRGB);
            }
        }
        return newImg;
    }

    protected BufferedImage gausian(BufferedImage img, int size) {
        BufferedImage newImg = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
        int num = (size * 2 + 1) * (size * 2 + 1);
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                int[] rgb = new int[]{0, 0, 0};
                int numxy = num;
                for (int xAround = -size; xAround <= size; xAround++) {
                    for (int yAround = -size; yAround <= size; yAround++) {
                        int[] rgbAround;
                        if ((xAround < 0) | (yAround < 0)) {
                            numxy -= 1;
                        } else {
                            rgbAround = getRGB(img, xAround, yAround);
                            for (int i = 0; i < 3; i++) {
                                rgb[i] += rgbAround[i];
                            }
                        }
                    }
                }
                int newR = ((rgb[0] / numxy) << 16) & 0xff0000;
                int newG = ((rgb[1] / numxy) << 8) & 0x00ff00;
                int newB = (rgb[2] / numxy) & 0x0000ff;
                int newRGB = newR & newG & newB;
                newImg.setRGB(x, y, newRGB);
            }
        }
        return newImg;
    }

    protected int[] average(BufferedImage img) {
        int[] rgb = new int[3];
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                int[] rgbxy = getRGB(img, x, y);
                for (int i = 0; i < 3; i++) {
                    rgb[i] += rgbxy[i];
                }
            }
        }
        int numPixel = img.getWidth() * img.getHeight();
        for (int i = 0; i < 3; i++) {
            rgb[i] /= numPixel;
        }
        return rgb;
    }

    protected int[] getRGB(BufferedImage img, int x, int y) {
        int pixel = img.getRGB(x, y);
        int r = pixel >> 16 & 0xff;
        int g = pixel >> 8 & 0xff;
        int b = pixel & 0xff;
        return new int[]{r, g, b};
    }

    protected BufferedImage[] kmeans(BufferedImage img, int numClusters) {
        final int w = img.getWidth();
        final int h = img.getHeight();
        final int numPx = w * h;
        // Initialize
        float[] weight = new float[numPx];
        float[][] barycenters = new float[3][numClusters];
        Arrays.fill(weight, 1);
        for (int i = 0;; i++) {
            int pos = weightedRandom(weight);
            int[] tmpRGB = getRGB(img, pos % h, pos / h);
            for (int c = 0; c < 3; c++) {
                barycenters[i][c] = (float)tmpRGB[c];
            }
            if (i == numClusters) {
                break;
            }
            Arrays.fill(weight, 0);
            for (int j = 0; j < i; j++) {
                float[] barycenter = barycenters[j];
                for (int x = 0; x < w; x++) {
                    for (int y = 0; y < h; y++) {
                        int[] rgb = getRGB(img, x, y);
                        float distance = getDistance(rgb, barycenter);
                        weight[x+y*w] += distance;
                    }
                }
            }
        }
        
        // Learning
        final int maxIter = 30;
        final int maxD = 256;
        int[] labels = new int[numPx];
        for (int iter = 0;; iter++) {
            // Update labels
            for (int x = 0; x < w; x++) {
                for (int y = 0; y < h; y++) {
                    float distance = maxD;
                    int[] rgb = getRGB(img, x, y);
                    for (int i = 0; i < numClusters; i++) {
                        float[] barycenter = barycenters[i];
                        float d = getDistance(rgb, barycenter);
                        if (d < distance) {
                            distance = d;
                            labels[x + y * w] = i;
                        }
                    }
                }
            }
            if (iter == maxIter) {
                break;
            }

            // Update barycenters
            Arrays.fill(barycenters, new float[]{0, 0, 0});
            int[] numEachLabels = new int[numClusters];
            for (int i = 0; i < numPx; i++) {
                int[] rgb = getRGB(img, i % h, i / h);
                int label = labels[i];
                numEachLabels[label]++;
                for (int c = 0; c < 3; c++) {
                    barycenters[label][c] += rgb[c];
                }
            }
            for (int i = 0; i < numClusters; i++) {
                for (int c = 0; c < 3; c++) {
                    barycenters[i][c] /= numEachLabels[i];
                }
            }
        }
        BufferedImage[] res = new BufferedImage[numClusters];
        for (int i = 0; i < h; i++) {
            for (int j = 0; j < numClusters; j++) {
                if (j == labels[i]) {
                    res[j].setRGB(i%h, i/h, 0x000000);
                } else {
                    res[j].setRGB(i%h, i/h, 0xffffff);
                }
            }
        }
        return res;
    }
    
    protected int weightedRandom(float[] weight) {
        float sumWeight = 0;
        for (float v : weight) {
            sumWeight += v;
        }
        Random rand = new Random();
        float val = rand.nextFloat(sumWeight);
        sumWeight = 0;
        for (int i = 0; i < weight.length; i++) {
            sumWeight += weight[i];
            if (val < sumWeight) {
                return i;
            }
        }
        return weight.length - 1;
    }
    
    protected float getDistance(int[] a, int[] b) {
        float[] fa = new float[3];
        float[] fb = new float[3];
        for (int c = 0; c < 3; c++) {
            fa[c] = (float)a[c];
            fb[c] = (float)b[c];
        }
        return getDistance(fa, fb);
    }

    protected float getDistance(float[] a, int[] b) {
        float[] fb = new float[3];
        for (int c = 0; c < 3; c++) {
            fb[c] = (float)b[c];
        }
        return getDistance(a, fb);
    }

    protected float getDistance(int[] a, float[] b) {
        float[] fa = new float[3];
        for (int c = 0; c < 3; c++) {
            fa[c] = (float)a[c];
        }
        return getDistance(fa, b);
    }

    protected float getDistance(float[] a, float[] b) {
        float d2 = 0;
        for (int c = 0; c < 3; c++) {
            d2 += Math.pow(a[c] - b[c], 2);
        }
        return (float)Math.sqrt(d2);
    }
}