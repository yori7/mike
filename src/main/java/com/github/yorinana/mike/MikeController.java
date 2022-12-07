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
        BufferedImage flatImg = flatField(img);
        // BufferedImage[] sepImg = kmeans(flatImg, 3);
        image2 = SwingFXUtils.toFXImage(flatImg, null);
    }

    final protected BufferedImage flatField(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage newImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        int size = (int)Math.sqrt(h * w * 0.04);
        BufferedImage blurImg = blur(img, size);

        float[] ave = new float[3];
        int[] tmpAve = average(img);
        for (int c = 0; c < 3; c++) {
            ave[c] = (float)tmpAve[c];
        }

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int[] rgb = getRGB(img, x, y);
                int[] blurRGB = getRGB(blurImg, x, y);
                for (int c = 0; c < 3; c++) {
                    rgb[c] = (int) (((float)(rgb[c]+1) / (float)(blurRGB[c]+1)) * ave[c]);
                }
                int newR = (rgb[0] << 16) & 0xff0000;
                int newG = (rgb[1] << 8) & 0x00ff00;
                int newB = (rgb[2]) & 0x0000ff;
                int newRGB = newR | newG | newB;
                newImg.setRGB(x, y, newRGB);
            }
        }
        return blurImg;
    }

    final protected BufferedImage blur(BufferedImage img, int size) {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage newImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        //int r, g, b;
        int sumr = 0, sumg = 0, sumb = 0;

        boolean judgeX0, judgeX1, judgeY0, judgeY1;
        int x0, x1, y0, y1;
        int curnelW, curnelH;
        int num;

        //int[] rgbAround; //
        int[] rgb;
        int newr, newg, newb;
        int newRGB;

        int[] sumColsR = new int[w], sumColsG = new int[w], sumColsB = new int[w];
        // -------
        for (int y = 0; y < h; y++) {
            judgeY0 = (y - size) > 0;
            y0 = judgeY0 ? y - size : 0;
            judgeY1 = (y + size) < h;
            y1 = judgeY1 ? y + size : h - 1;
            curnelH = y1 - y0 + 1;
            for (int x = 0; x < w; x++) {
                judgeX0 = (x - size) > 0;
                x0 = judgeX0 ? x - size : 0;
                judgeX1 = (x + size) < w;
                x1 = judgeX1 ? x + size : w - 1;
                curnelW = x1 - x0 + 1;
                num = curnelW * curnelH;
                if (x == 0) { // collect sumCols
                    for (int xc = x0; xc <= x1; xc++) {
                        if (y == 0) { // access each pixel
                            for (int yc = y0; yc <= y1; yc++) {
                                rgb = getRGB(img, xc, yc);
                                sumColsR[xc] += rgb[0];
                                sumColsG[xc] += rgb[1];
                                sumColsB[xc] += rgb[2];
                            }
                        } else { // stride sumCols[xc] to y direction
                            if (judgeY0) {
                                rgb = getRGB(img, xc, y0-1);
                                sumColsR[xc] -= rgb[0];
                                sumColsG[xc] -= rgb[1];
                                sumColsB[xc] -= rgb[2];
                            }
                            if (judgeY1) {
                                rgb = getRGB(img, xc, y1);
                                sumColsR[xc] += rgb[0];
                                sumColsG[xc] += rgb[1];
                                sumColsB[xc] += rgb[2];
                            }
                        }
                        sumr += sumColsR[xc];
                        sumg += sumColsG[xc];
                        sumb += sumColsB[xc];
                     }
                } else { // sum-sumCols[x0-1]+sumCols[x1](stride sum to x direction)
                    if (judgeX0) { // subtract sumCols[x0-1]
                        sumr -= sumColsR[x0-1];
                        sumg -= sumColsG[x0-1];
                        sumb -= sumColsB[x0-1];
                    }
                    if (judgeX1) { // add sumCols[x1]
                        if (y == 0) { // access each pixel
                            for (int yc = y0; yc <= y1; yc++) {
                                rgb = getRGB(img, x1, yc);
                                sumColsR[x1] += rgb[0];
                                sumColsG[x1] += rgb[1];
                                sumColsB[x1] += rgb[2];
                            }
                        } else { // stride sumCols[x1] to y direction
                            if (judgeY0) {
                                rgb = getRGB(img, x1, y0-1);
                                sumColsR[x1] -= rgb[0];
                                sumColsG[x1] -= rgb[1];
                                sumColsB[x1] -= rgb[2];
                            }
                            if (judgeY1) {
                                rgb = getRGB(img, x1, y1);
                                sumColsR[x1] += rgb[0];
                                sumColsG[x1] += rgb[1];
                                sumColsB[x1] += rgb[2];
                            }
                        }
                        sumr += sumColsR[x1];
                        sumg += sumColsG[x1];
                        sumb += sumColsB[x1];
                    }
                }
                // set the RGB to new image
                newr = ((sumr / num) << 16);
                newg = ((sumg / num) << 8);
                newb = (sumb / num);
                newRGB = newr | newg | newb;
                newImg.setRGB(x, y, newRGB);
            }
            sumr = 0;
            sumg = 0;
            sumb = 0;
        }
        // -------
        /*
        for (int x = 0; x < w; x++) {
            int y = 0;
            r = 0;
            g = 0;
            b = 0;
            x0 = (x - size) > 0 ? x - size : 0;
            x1 = (x + size) < w ? x + size : w - 1;
            curnelW = x1 - x0 + 1;

            y0 = (y - size) > 0 ? y - size : 0;
            y1 = (y + size) < h ? y + size : h - 1;
            curnelH = y1 - y0 + 1;

            num = curnelW * curnelH;
            for (int yAround = y0; yAround <= y1; yAround++) {
                for (int xAround = x0; xAround <= x1; xAround++) {
                    rgbAround = getRGB(img, xAround, yAround);
                    sumColsR[yAround] += rgbAround[0];
                    sumColsG[yAround] += rgbAround[1];
                    sumColsB[yAround] += rgbAround[2];
                }
                r += sumColsR[yAround];
                g += sumColsG[yAround];
                b += sumColsB[yAround];
            }

            int newR = ((r / num) << 16);
            int newG = ((g / num) << 8);
            int newB = (b / num);
            int newRGB = newR | newG | newB;
            newImg.setRGB(x, y, newRGB);

            int beforeCol, nextCol;
            for (y = 1; y < h; y++) {
                beforeCol = y - size - 1;
                if ((beforeCol) >= 0) {
                    r -= sumColsR[beforeCol];
                    g -= sumColsG[beforeCol];
                    b -= sumColsB[beforeCol];
                    num -= curnelW;
                }
                nextCol = y + size;
                if (nextCol < h) {
                    for (int xAround = x0; xAround <= x1; xAround++) {
                        rgbAround = getRGB(img, xAround, nextCol);
                        sumColsR[nextCol] += rgbAround[0];
                        sumColsG[nextCol] += rgbAround[1];
                        sumColsB[nextCol] += rgbAround[2];
                    }
                    r += sumColsR[nextCol];
                    g += sumColsG[nextCol];
                    b += sumColsB[nextCol];
                    num += curnelW;
                }
                newR = ((r / num) << 16);
                newG = ((g / num) << 8);
                newB = (b / num);
                newRGB = newR | newG | newB;
                newImg.setRGB(x, y, newRGB);
            }
            Arrays.fill(sumColsR, 0);
            Arrays.fill(sumColsG, 0);
            Arrays.fill(sumColsB, 0);
        }
         */
        return newImg;
    }

    final protected int[] average(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        int numPixel = w * h;
        int[] rgb = new int[3];
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int[] rgbxy = getRGB(img, x, y);
                for (int c = 0; c < 3; c++) {
                    rgb[c] += rgbxy[c];
                }
            }
        }
        for (int c = 0; c < 3; c++) {
            rgb[c] /= numPixel;
        }
        return rgb;
    }

    final protected int[] getRGB(BufferedImage img, int x, int y) {
        int pixel = img.getRGB(x, y);
        int r = pixel >> 16 & 0xff;
        int g = pixel >> 8 & 0xff;
        int b = pixel & 0xff;
        return new int[]{r, g, b};
    }

    final protected BufferedImage[] kmeans(BufferedImage img, int numClusters) {
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
        final int maxIter = 10;
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
                    if (numEachLabels[i] != 0) {
                        barycenters[i][c] /= numEachLabels[i];
                    }
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
    
    final protected int weightedRandom(float[] weight) {
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
    
    final protected float getDistance(int[] a, int[] b) {
        float[] fa = new float[3];
        float[] fb = new float[3];
        for (int c = 0; c < 3; c++) {
            fa[c] = (float)a[c];
            fb[c] = (float)b[c];
        }
        return getDistance(fa, fb);
    }

    final protected float getDistance(float[] a, int[] b) {
        float[] fb = new float[3];
        for (int c = 0; c < 3; c++) {
            fb[c] = (float)b[c];
        }
        return getDistance(a, fb);
    }

    final protected float getDistance(int[] a, float[] b) {
        float[] fa = new float[3];
        for (int c = 0; c < 3; c++) {
            fa[c] = (float)a[c];
        }
        return getDistance(fa, b);
    }

    final protected float getDistance(float[] a, float[] b) {
        float d2 = 0;
        for (int c = 0; c < 3; c++) {
            d2 += Math.pow(a[c] - b[c], 2);
        }
        return (float)Math.sqrt(d2);
    }
}