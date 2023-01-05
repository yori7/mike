package com.github.yorinana.mike.clustering;

import java.util.*;

public final class Ward implements Hierarchical {
    int length = 0;
    int dimension = 0;
    Node tree;
    Map<int[],Float> distances;

    @Override
    public void fit(int[][] data) {
        float[][] floatData = data2Float(data);
        fit(floatData);
    }

    @Override
    public void fit(float[][] data) {
        this.length = data.length;
        this.dimension = data[0].length;
        Map<Integer, Node> nodes = new HashMap<>();
        Map<int[],Float> hashedDistance = new HashMap<>();
        for (int i = 0; i < length; i++) {
            nodes.put(i, new Leaf(i, data[i]));
        }
        int count = this.length;
        while (nodes.size() != 1) {
            int[] nearestID = new int[2];
            float nearestDistance = 500000;
            for (int id1 : nodes.keySet()) {
                for (int id2 : nodes.keySet()) {
                    if (id1 == id2) {
                        continue;
                    }
                    int[] key = new int[]{id1, id2};
                    if (!hashedDistance.containsKey(key)) {
                        hashedDistance.put(
                                key,
                                getDistance(nodes.get(id1).getValue(), nodes.get(id2).getValue())
                        );
                    }
                    float distance = hashedDistance.get(key);
                    if (distance < nearestDistance) {
                        nearestID = key;
                        nearestDistance = distance;
                    }
                }
            }
            nodes.put(count, new Branch(count++, nodes.remove(nearestID[0]), nodes.remove(nearestID[1])));
        }
        this.tree = nodes.get(0);
        this.distances = hashedDistance;
    }

    @Override
    public int[] predict(int k) {
        int[] labels = new int[length];
        if (k <= length) {
            for (int i = 0; i < length; i++) {
                labels[i] = i;
            }
            return labels;
        }

        List<Node> nodes = new ArrayList<>();
        nodes.add(this.tree);
        // body
        while (nodes.size() < k) {
            Node maxNode = nodes.get(0);
            int maxID = maxNode.getId();
            for (int i = 1; i < nodes.size(); i++) {
                Node node = nodes.get(i);
                int id = node.getId();
                if (maxID < id) {
                    maxID = id;
                    maxNode = node;
                }
            }
            nodes.remove(maxID);
            nodes.add(((Branch) maxNode).left);
            nodes.add(((Branch) maxNode).right);
        }
        int count = 0;
        for (Node node: nodes) {
            for (int id: node.getIds()) {
                labels[id] = count;
            }
            count++;
        }

        return labels;
    }

    private float getDistance(float[] p1, float[] p2) {
        int length = p1.length;
        float res = 0;
        for (int i = 0; i < length; i++) {
            res += Math.pow(p1[i]-p2[i], 2);
        }
        return (float) Math.sqrt(res);
    }


}
