package balltree;

import java.util.*;

import utils.ContactPair;
import utils.Data;

public class TernaryBallTree extends BallTree {

    public double repartirionRatio = -1;

    public TernaryBallTree(int minLeafNB, ArrayList<Data> db, double repartirionRatio) {
        super(minLeafNB, db);
        this.repartirionRatio = repartirionRatio;
    }

    public TernaryBallNode buildBallTree() {
        // assert this.minLeafNB >= 10;
        for (int i = 0; i < pointNB; i++) {
            this.indexes[i] = i;
        }
        double[] pivot = this.getPivot(this.indexes, 0, pointNB - 1);
        double radius = this.getRadius(this.indexes, 0, pointNB - 1, pivot);
        TernaryBallNode root = new TernaryBallNode(1, 0, pointNB - 1, pivot, radius);
        try {
            this.localBuildBallTree(root, 1);
        } catch (StackOverflowError e) {
            System.out.println("Stackoverflow: " + constructCount);
        }
        return root;
    }

    public void localBuildBallTree(TernaryBallNode node, int depth) {
        if (node.idxEnd - node.idxStart + 1 < super.minLeafNB) {
            return;
        } else {
            constructCount++;
            this.splitNode(node);
            this.localBuildBallTree(node.leftNode, depth + 1);
            this.localBuildBallTree(node.rightNode, depth + 1);
            if (node.extraNode != null) {
                this.localBuildBallTree(node.extraNode, depth + 1);
            }
        }
    }

    public void splitNode(TernaryBallNode node) {
        // 1.get furthest1: furthest far from node's pivot
        double maxDist = 0.0;
        double tempDist = 0.0;
        int idx = node.idxStart;
        for (int i = node.idxStart; i < node.idxEnd + 1; i++) {
            tempDist = getDistance(input.get(this.indexes[i]), node.pivot);
            if (tempDist > maxDist) {
                maxDist = tempDist;
                idx = i;
            }
        }
        double[] furthest1 = input.get(this.indexes[idx]);
        // 2.get furthest2: furthest far from furthest1
        // PriorityQueue<Point> candidate = new PriorityQueue<>(cmp_point);
        idx = node.idxStart;
        maxDist = 0;
        for (int i = node.idxStart; i < node.idxEnd + 1; i++) {
            tempDist = getDistance(input.get(this.indexes[i]), furthest1);
            if (tempDist > maxDist) {
                maxDist = tempDist;
                idx = i;
            }
        }
        double[] furthest2 = input.get(this.indexes[idx]);

        // 3.update indexes
        int split = node.idxEnd;
        idx = node.idxStart;
        double sum = node.idxEnd - node.idxStart + 1;

        while (idx <= split) {
            maxDist = getDistance(input.get(this.indexes[idx]), furthest1);
            tempDist = getDistance(input.get(this.indexes[idx]), furthest2);
            if (maxDist > tempDist) {
                int temp = this.indexes[idx];
                this.indexes[idx] = this.indexes[split];
                if (idx != split) {
                    this.indexes[split] = temp;
                    split = split - 1;
                } else {
                    idx = idx + 1;
                }
            } else {
                idx = idx + 1;
            }
        }

        // if it is highly imbalanced, adjust the child to be ternary
        double[] pivotL = null, pivotE = null, pivotR = null;
        double radiusL = 0, radiusE = 0, radiusR = 0;
        int leftWeight = split - node.idxStart;
        int rightWeight = node.idxEnd - split + 1;
        double diff = Math.abs(leftWeight - rightWeight);

        if (diff / sum >= repartirionRatio && leftWeight > minLeafNB && rightWeight > minLeafNB) {
            int split1, split2;
            // get a new partition node
            double[] newNode = null;
            double[] anchor = new double[2];
            anchor[0] = (furthest1[0] + furthest2[0]) / 2;
            anchor[1] = (furthest1[1] + furthest2[1]) / 2;
            double minDist = 10000;
            if (leftWeight > rightWeight) {
                // get a new node for partition
                for (int i = node.idxStart; i < split; i++) {
                    tempDist = getDistance(input.get(this.indexes[i]), anchor);
                    if (tempDist < minDist) {
                        minDist = tempDist;
                        newNode = input.get(this.indexes[i]);
                    }
                }
                split2 = split;
                // 3.update indexes, really split node
                idx = node.idxStart;
                while (idx <= split) {
                    maxDist = getDistance(input.get(this.indexes[idx]), newNode);
                    tempDist = getDistance(input.get(this.indexes[idx]), furthest1);
                    if (maxDist > tempDist) {
                        int temp = this.indexes[idx];
                        this.indexes[idx] = this.indexes[split];
                        if (idx != split) {
                            this.indexes[split] = temp;
                            split = split - 1;
                        } else {
                            idx = idx + 1;
                        }
                    } else {
                        idx = idx + 1;
                    }
                }
                split1 = split;
            } else {
                for (int i = split; i < node.idxEnd; i++) {
                    tempDist = getDistance(input.get(this.indexes[i]), anchor);
                    if (tempDist < minDist) {
                        minDist = tempDist;
                        newNode = input.get(this.indexes[i]);
                    }
                }
                split1 = split;
                // 3.update indexes, really split node
                idx = split;
                split = node.idxEnd;
                while (idx <= split) {
                    maxDist = getDistance(input.get(this.indexes[idx]), newNode);
                    tempDist = getDistance(input.get(this.indexes[idx]), furthest2);
                    if (maxDist > tempDist) {
                        int temp = this.indexes[idx];
                        this.indexes[idx] = this.indexes[split];
                        if (idx != split) {
                            this.indexes[split] = temp;
                            split = split - 1;
                        } else {
                            idx = idx + 1;
                        }
                    } else {
                        // rightDists[idx - node.idxStart] = tempDist;
                        idx = idx + 1;
                    }
                }
                split2 = split;
            }
            // System.out.println(node.idxStart+"/"+split1+"/"+split2+"/"+node.idxEnd);
            // update node info
            pivotL = getPivot(indexes, node.idxStart, split1 - 1);
            radiusL = this.getRadius(indexes, node.idxStart, split1 - 1,
                    pivotL);
            if (split1 != split2) {
                pivotE = getPivot(indexes, split1, split2 - 1);
                radiusE = this.getRadius(indexes, split1, split2 - 1,
                        pivotE);
                node.extraNode = new TernaryBallNode(node.id * 3 + 1, split1, split2 - 1, pivotE, radiusE);
            }
            pivotR = this.getPivot(indexes, split2, node.idxEnd);
            radiusR = this.getRadius(indexes, split2, node.idxEnd, pivotR);
            node.leftNode = new TernaryBallNode(node.id * 3, node.idxStart, split1 - 1, pivotL, radiusL);
            node.rightNode = new TernaryBallNode(node.id * 3 + 2, split2, node.idxEnd, pivotR, radiusR);
        } else {
            // 4.update node info leftNode:idxStart->split-1, rightNode:split->idxEnd
            pivotL = getPivot(indexes, node.idxStart, split - 1);
            radiusL = this.getRadius(indexes, node.idxStart, split - 1, pivotL);
            pivotR = this.getPivot(indexes, split, node.idxEnd);
            radiusR = this.getRadius(indexes, split, node.idxEnd, pivotR);
            node.leftNode = new TernaryBallNode(node.id * 3, node.idxStart, split - 1, pivotL, radiusL);
            node.rightNode = new TernaryBallNode(node.id * 3 + 1, split, node.idxEnd, pivotR, radiusR);
        }
    }

    public ArrayList<ContactPair> searchRange(TernaryBallNode node, Data qdata) {
        ArrayList<ContactPair> res = new ArrayList<>();
        this.rangeSearch(node, qdata, res);
        return res;
    }

    public void rangeSearch(TernaryBallNode node, Data qdata, ArrayList<ContactPair> res) {
        searchCount++;
        double[] target = qdata.values;
        double range = qdata.radius;
        double nodeDist = this.getDistance(target, node.pivot) - node.radius - range;
        if (nodeDist > 0) {
            return;
        }
        if (node.leftNode != null && node.rightNode != null) {
            double leftPivotDist = this.getDistance(target, node.leftNode.pivot);
            double rightPivotDist = this.getDistance(target, node.rightNode.pivot);
            double leftBallDist = leftPivotDist - node.leftNode.radius - range;
            double rightBallDist = rightPivotDist - node.rightNode.radius - range;
            if (node.extraNode != null) {
                double extraPivotDist = this.getDistance(target, node.extraNode.pivot);
                double extraBallDist = extraPivotDist - node.extraNode.radius - range;
                if (extraBallDist <= 0) {
                    rangeSearch(node.extraNode, qdata, res);
                }
            }
            if (leftBallDist <= 0) {
                rangeSearch(node.leftNode, qdata, res);
            }
            if (rightBallDist <= 0) {
                rangeSearch(node.rightNode, qdata, res);
            }
        } else if (node.leftNode != null || node.rightNode != null) {
            System.out.println("This node only one leaf, Unreasonable!");
        } else if (node.leftNode == null && node.rightNode == null) {
            for (int i = node.idxStart; i < node.idxEnd + 1; i++) {
                double dist = this.getDistance(target, input.get(indexes[i]));
                if (dist <= range + dbRadius.get(indexes[i])) {
                    res.add(new ContactPair(qdata, db.get(indexes[i]), dist));
                }
            }
        } else {
            System.out.println("Search unsuccessfully:" + node.id + " " + node.radius);
        }
    }

}
