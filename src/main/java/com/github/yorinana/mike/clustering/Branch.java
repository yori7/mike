package com.github.yorinana.mike.clustering;

public final class Branch implements Node{
    int id;
    float[] value;
    Node left;
    Node right;


    public Branch(int id, Node left, Node right) {
        this.id = id;
        this.left = left;
        this.right = right;
        this.value = getBarycenter(getValues());
    }

    @Override
    public float[][] getValues() {
        float[][] leftValues = left.getValues();
        float[][] rightValues = right.getValues();
        float[][] res = new float[leftValues.length + rightValues.length][];
        System.arraycopy(leftValues, 0, res, 0, leftValues.length);
        System.arraycopy(rightValues, 0, res, leftValues.length, rightValues.length);
        return res;
    }

    @Override
    public int[] getIds() {
        int[] leftIds = left.getIds();
        int[] rightIds = right.getIds();
        int[] res = new int[leftIds.length + rightIds.length];
        System.arraycopy(leftIds, 0, res, 0, leftIds.length);
        System.arraycopy(rightIds, 0, res, leftIds.length, rightIds.length);
        return res;
    }

    @Override
    public int getId() {
        return this.id;
    }

    @Override
    public float[] getValue() {
        return this.value;
    }

    private float[] getBarycenter(float[][] values) {
        int numValues = values.length;
        int length = values[0].length;
        float[] barycenter = new float[length];
        for (float[] value : values) {
            for (int i = 0; i < length; i++) {
                barycenter[i] += value[i];
            }
        }
        for (int i = 0; i < length; i++) {
            barycenter[i] /= numValues;
        }
        return barycenter;
    }
}
