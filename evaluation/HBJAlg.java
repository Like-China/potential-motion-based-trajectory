package evaluation;

import java.util.*;

import balltree.TernaryBallNode;
import balltree.TernaryBallTree;
import utils.NN;
import utils.TimeIntervalMR;

public class HBJAlg {
    // index construction time / filtering time
    public long cTime = 0;
    public long nnJoinTime = 0;
    public long rangeJoinTime = 0;
    // the number of node accesses
    public double searchCount = 0;
    public int nodeAccess = 0;
    // the repartition threshold
    public double repartirionRatio = 0;
    public int minLeafNB = 0;
    // pruning count
    public double pruneRatio = 0;
    public double upper1PruningCount = 0;
    public double upper2PruningCount = 0;
    public double nnPruningCount = 0;
    public int count = 0;

    public HBJAlg(TimeIntervalMR[] MR_A, TimeIntervalMR[] MR_B, double repartirionRatio,
            int minLeafNB) {
        this.repartirionRatio = repartirionRatio;
        this.minLeafNB = minLeafNB;
    }

    public ArrayList<TimeIntervalMR> tbSearch(TimeIntervalMR mra, TernaryBallTree bt, TernaryBallNode root,
            double simThreshold, boolean useUB) {
        ArrayList<TimeIntervalMR> res = new ArrayList<>();
        // pre-checking to get candidates
        ArrayList<TimeIntervalMR> candidates = bt.searchRange(root, mra);
        if (mra.POIs.size() == 0) {
            return res;
        }
        for (TimeIntervalMR c : candidates) {
            if (useUB) {
                double simUpper = mra.upperBound1To(c);

                if (simUpper < simThreshold) {
                    upper1PruningCount += 1;
                    continue;
                }

                if (simUpper < simThreshold) {
                    upper2PruningCount += 1;
                    simUpper = mra.upperBound2To(c);
                    System.out.println(mra);
                    System.out.println(c);
                    System.out.println(simUpper);
                    System.out.println();
                    continue;
                }
            }

            searchCount += 1;
            double sim = mra.simTo(c);
            if (sim >= simThreshold) {
                // System.out.println(simUpper + "/" + sim + "/" + simThreshold);
                res.add(c);
            }
        }
        return res;
    }

    // Threshold-based Join
    public int tbJoin(TimeIntervalMR[] MR_A, TimeIntervalMR[] MR_B, double simThreshold, boolean useUB) {
        long t1 = System.currentTimeMillis();

        TernaryBallTree bt = new TernaryBallTree(minLeafNB, MR_B, repartirionRatio);
        TernaryBallNode root = bt.buildBallTree();
        cTime = System.currentTimeMillis() - t1;

        int matchNB = 0;
        int i = 0;
        for (TimeIntervalMR mra : MR_A) {
            i++;
            ArrayList<TimeIntervalMR> res = tbSearch(mra, bt, root, simThreshold, useUB);
            matchNB += res.size();
        }
        long t2 = System.currentTimeMillis();
        rangeJoinTime = t2 - t1;
        // System.out.println("\n-- Upper 1 Pruning Count: " + upper1PruningCount + "
        // Search Count: " + searchCount);
        // String info = String.format("Construction Node Access: %d Search Node
        // Access:%d",
        // bt.constructCount, bt.searchCount / MR_A.length);
        // System.out.println(info);
        pruneRatio = (double) (MR_A.length * MR_B.length - searchCount) / (MR_A.length * MR_B.length);
        nodeAccess = bt.searchCount;
        return matchNB;
    }

    public PriorityQueue<NN> kJoin(TimeIntervalMR[] MR_A, TimeIntervalMR[] MR_B, int k, boolean isSelfJoin) {
        long t1 = System.currentTimeMillis();

        TernaryBallTree bt = new TernaryBallTree(minLeafNB, MR_B, repartirionRatio);
        TernaryBallNode root = bt.buildBallTree();
        cTime = System.currentTimeMillis() - t1;

        PriorityQueue<NN> nnCandidate = new PriorityQueue<>(Comp.NNComparator1);

        // 1. get nn candidates, sort NN according to their upper bound 1
        int i = 0;
        for (TimeIntervalMR mra : MR_A) {
            i++;
            ArrayList<TimeIntervalMR> candidates = bt.searchRange(root, mra);
            for (TimeIntervalMR mrb : candidates) {
                if (isSelfJoin && mra.objectID >= mrb.objectID)
                    continue;
                double simUpper = mra.upperBound1To(mrb);
                if (simUpper > 0) {
                    NN n = new NN(mra, mrb);
                    n.simUpper1 = simUpper;
                    nnCandidate.add(new NN(mra, mrb));
                }
            }
        }
        // System.out.println("\nNN Candidate Size After Pre-checking and UB 1: " +
        // nnCandidate.size());
        // 2. refinement NN to get final res
        PriorityQueue<NN> res = new PriorityQueue<>(Comp.NNComparator2);
        for (NN nCandidate : nnCandidate) {
            TimeIntervalMR mra = nCandidate.mra;
            TimeIntervalMR mrb = nCandidate.mrb;
            double sim = mra.simTo(mrb);
            nCandidate.sim = sim;

            if (res.size() < k) {
                res.add(nCandidate);
            } else {
                double minKsim = res.peek().sim;
                // if (nCandidate.simUpper1 < minKsim) {
                // System.out.println();
                // }
                if (sim > minKsim) {
                    res.poll();
                    res.add(nCandidate);
                }
            }
        }
        long t2 = System.currentTimeMillis();
        nnJoinTime += (t2 - t1);
        // System.out.println("NN Pruning count: " + nnPruningCount + " Pruning Ratio: "
        // + (nnPruningCount / (MR_A.length * MR_B.length)));
        return res;
    }

}
