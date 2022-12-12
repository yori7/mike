package com.github.yorinana.mike;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Random;

public final class Kmeans {
    private final int k;
    private final int maxIter;
    private float[][] barycenters;
    private static final int dim = 3;

    Kmeans(int k, int maxIter) {
        this.k = k;
        this.maxIter = maxIter;
        this.barycenters = new float[k][];
    }

    public void fit(float[][] points) {
        int length = points.length;
        init(points, length);
        learn(points, length);
    }

    public void fit(int[][] points) {
        float[][] floatPoints = iArr2FArr(points);
        fit(floatPoints);
    }

    private void init(float[][] points, int length) {
        float[] weight = new float[length];
        float[][] barycenters = new float[this.k][dim];

        Arrays.fill(weight, 1);
        for (int cluster = 0; ; cluster++) { // decide initial barycenters
            int pos = weightedRandom(weight);
            float[] tmpRGB = points[pos];
            System.arraycopy(tmpRGB, 0, barycenters[cluster], 0, dim);

            if (cluster == this.k-1) {
                break;
            }

            // Update weights
            Arrays.fill(weight, 442);
            for (int i = 0; i < cluster+1; i++) {
                float[] barycenter = barycenters[i];
                for (pos = 0; pos < length; pos++) {
                    float[] rgb = points[pos];
                    float distance = getDistance(rgb, barycenter);
                    weight[pos] = Math.min(weight[pos], distance);
                }
            }
        }

        this.barycenters = barycenters;
    }

    private int weightedRandom(float[] weight) {
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

    private float[][] iArr2FArr(int[][] intArr) {
        int len1 = intArr.length;
        int len2 = intArr[0].length;
        float[][] floatArr = new float[len1][len2];

        for (int i = 0; i < len1; i++) {
            for (int j = 0; j < len2; j++) {
                floatArr[i][j] = (float)intArr[i][j];
            }
        }

        return floatArr;
    }

    private float getDistance(float[] p1, float[] p2) {
        float res = 0;
        for (int i = 0; i < dim; i++) {
            res += Math.pow(p1[i]-p2[i], 2);
        }
        return res;
    }

    private void learn(float[][] points, int length) {
        // float[][] barycenters = this.barycenters;

        int[] labels;
        for (int iter = 0;; iter++) {
            // Update labels
            labels = predict(points, length);

            if (iter == maxIter) {
                break;
            }

            // Update barycenters
            for (int cluster = 0; cluster < this.k; cluster++) {
                Arrays.fill(barycenters[cluster], 0);
            }
            int[] numEachLabels = new int[this.k];
            for (int pos = 0; pos < length; pos++) {
                float[] rgb = points[pos];
                int label = labels[pos];
                numEachLabels[label]++;
                for (int c = 0; c < dim; c++) {
                    barycenters[label][c] += rgb[c];
                }
            }
            for (int cluster = 0; cluster < this.k; cluster++) {
                for (int c = 0; c < 3; c++) {
                    if (numEachLabels[cluster] != 0) {
                        barycenters[cluster][c] /= numEachLabels[cluster];
                    }
                }
            }
        }
    }

    public int[] predict(float[][] points, int length) {
        int[] labels = new int[length];
        float distance;
        for (int pos = 0; pos < length; pos++) {
            distance = 442; // 442 = distance between (0, 0, 0) and (255, 255, 255)
            float[] rgb = points[pos];
            for (int cluster = 0; cluster < this.k; cluster++) {
                float[] barycenter = barycenters[cluster];
                float d = getDistance(rgb, barycenter);
                if (d < distance) {
                    distance = d;
                    labels[pos] = cluster;
                }
            }
        }
        return labels;
    }

    public int[] predict(float[][] points) {
        int length = points.length;
        return predict(points, length);
    }

    public int[] predict(int[][] points) {
        float[][] floatPoints = iArr2FArr(points);
        return predict(floatPoints);
    }

    public static BufferedImage[] labels2BufImage(int[] labels, int w, int h, int k) {
        int numPx = w * h;
        BufferedImage[] res = new BufferedImage[k];
        for (int cluster = 0; cluster < k; cluster++) {
            res[cluster] = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        }
        for (int px = 0; px < numPx; px++) {
            for (int cluster = 0; cluster < k; cluster++) {
                if (cluster == labels[px]) {
                    res[cluster].setRGB(px%w, px/w, 0x000000);
                } else {
                    res[cluster].setRGB(px%w, px/w, 0xffffff);
                }
            }
        }

        return res;
    }
}
