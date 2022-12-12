package com.github.yorinana.mike;

import java.awt.image.BufferedImage;

public class Filters {
    public static BufferedImage flatField(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage newImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        int size = (int)Math.sqrt(h * w * 0.001);
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
        return newImg;
    }

    public static BufferedImage blur(BufferedImage img, int size) {
        int w = img.getWidth();
        int h = img.getHeight();
        BufferedImage newImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        int sumR = 0, sumG = 0, sumB = 0;

        boolean judgeX0, judgeX1, judgeY0, judgeY1;
        int x0, x1, y0, y1;
        int kernelW, kernelH;
        int num;

        int[] rgb;
        int newR, newG, newB;
        int newRGB;

        int[] sumColsR = new int[w], sumColsG = new int[w], sumColsB = new int[w];
        // -------
        for (int y = 0; y < h; y++) {
            judgeY0 = (y - size) > 0;
            y0 = judgeY0 ? y - size : 0;
            judgeY1 = (y + size) < h;
            y1 = judgeY1 ? y + size : h - 1;
            kernelH = y1 - y0 + 1;
            for (int x = 0; x < w; x++) {
                judgeX0 = (x - size) > 0;
                x0 = judgeX0 ? x - size : 0;
                judgeX1 = (x + size) < w;
                x1 = judgeX1 ? x + size : w - 1;
                kernelW = x1 - x0 + 1;
                num = kernelW * kernelH;
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
                        sumR += sumColsR[xc];
                        sumG += sumColsG[xc];
                        sumB += sumColsB[xc];
                    }
                } else { // sum-sumCols[x0-1]+sumCols[x1](stride sum to x direction)
                    if (judgeX0) { // subtract sumCols[x0-1]
                        sumR -= sumColsR[x0-1];
                        sumG -= sumColsG[x0-1];
                        sumB -= sumColsB[x0-1];
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
                        sumR += sumColsR[x1];
                        sumG += sumColsG[x1];
                        sumB += sumColsB[x1];
                    }
                }
                // set the RGB to new image
                newR = ((sumR / num) << 16);
                newG = ((sumG / num) << 8);
                newB = (sumB / num);
                newRGB = newR | newG | newB;
                newImg.setRGB(x, y, newRGB);
            }
            sumR = 0;
            sumG = 0;
            sumB = 0;
        }
        return newImg;
    }

    protected static int[] average(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        int numPixel = w * h;
        int[] res = new int[3];
        int[] rgb;
        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                rgb = getRGB(img, x, y);
                for (int c = 0; c < 3; c++) {
                    res[c] += rgb[c];
                }
            }
        }
        for (int c = 0; c < 3; c++) {
            res[c] /= numPixel;
        }
        return res;
    }

    public static int[] getRGB(BufferedImage img, int x, int y) {
        int c = img.getRGB(x, y);
        return getRGB(c);
    }

    public static int[] getRGB(int c) {
        int r = c >> 16 & 0xff;
        int g = c >> 8 & 0xff;
        int b = c & 0xff;
        return new int[]{r, g, b};
    }
}
