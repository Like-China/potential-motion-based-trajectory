package evaluation;

import java.util.*;

import mtree.MTreeClass;
import utils.*;

public class MJAlg {
    // index construction time / filtering time
    public long cTime = 0;
    public long nnJoinTime = 0;
    public long rangeJoinTime = 0;
    // pruning count
    public double candidateCount = 0;
    public double pruneRatio = 0;

    // POI tree

    public MJAlg() {
    }

    public ArrayList<TimeIntervalMR> tbSearch(TimeIntervalMR mra, MTreeClass mtree, double simThreshold) {
        ArrayList<TimeIntervalMR> res = new ArrayList<>();
        // pre-checking to get candidates
        MTreeClass.Query query = mtree.getNearestByRange(mra, mra.a);
        ArrayList<TimeIntervalMR> candidates = query.rangeQuery();
        candidateCount += candidates.size();
        for (TimeIntervalMR c : candidates) {
            double sim = mra.simTo(c);
            if (sim >= simThreshold) {
                res.add(c);
            }
        }
        return res;
    }

    // Threshold-based Join
    public int tbJoin(TimeIntervalMR[] MR_A, TimeIntervalMR[] MR_B, double simThreshold) {
        long t1 = System.currentTimeMillis();

        MTreeClass mtree = new MTreeClass();
        for (TimeIntervalMR mrb : MR_B) {
            mtree.add(mrb);
        }
        cTime = System.currentTimeMillis() - t1;

        int matchNB = 0;
        int i = 0;
        for (TimeIntervalMR mra : MR_A) {
            i++;
            ArrayList<TimeIntervalMR> res = tbSearch(mra, mtree, simThreshold);
            matchNB += res.size();
        }
        long t2 = System.currentTimeMillis();
        rangeJoinTime = t2 - t1;
        pruneRatio = (double) (MR_A.length * MR_B.length - candidateCount) / (MR_A.length * MR_B.length);
        // System.out.println("\n-- Range Pruning count: " + rangePruningCount + "
        // Pruning Ratio: "
        // + (rangePruningCount / ()));
        return matchNB;
    }

    public PriorityQueue<NN> kJoin(TimeIntervalMR[] MR_A, TimeIntervalMR[] MR_B, int k, boolean isSelfJoin) {
        long t1 = System.currentTimeMillis();

        MTreeClass mtree = new MTreeClass();
        for (TimeIntervalMR mrb : MR_B) {
            mtree.add(mrb);
        }
        cTime = System.currentTimeMillis() - t1;

        PriorityQueue<NN> nnCandidate = new PriorityQueue<>(Comp.NNComparator1);

        // 1. get nn candidates, sort NN according to their upper bound 1
        int i = 0;
        for (TimeIntervalMR mra : MR_A) {
            i++;
            MTreeClass.Query query = mtree.getNearestByRange(mra, mra.a);
            ArrayList<TimeIntervalMR> candidates = query.rangeQuery();
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
                // break;
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