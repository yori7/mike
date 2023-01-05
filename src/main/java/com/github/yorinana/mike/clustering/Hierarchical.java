package com.github.yorinana.mike.clustering;

public interface Hierarchical {
    void fit(int[][] data);
    void fit(float[][] data);

    int[] predict(int k);

    default float[][] data2Float(int[][] data) {
        int numData = data.length;
        int dimension = data[0].length;
        float[][] floatData = new float[numData][dimension];
        for (int i = 0; i < numData; i++) {
            for (int j = 0; j < dimension; j++) {
                floatData[i][j] = (float) data[i][j];
            }
        }
        return floatData;
    }
}
