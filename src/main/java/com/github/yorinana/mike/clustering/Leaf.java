package com.github.yorinana.mike.clustering;

public final class Leaf implements Node{
    int id;
    float[] value;

    public Leaf(int id, float[] val) {
        this.id = id;
        this.value = val;
    }

    @Override
    public float[] getValue() {
        return value;
    }

    @Override
    public float[][] getValues() {
        return new float[][]{value};
    }

    @Override
    public int[] getIds() {
        return new int[]{this.id};
    }

    @Override
    public int getId() {
        return this.id;
    }
}
