package evaluation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

import poi.QuadTree;
import utils.NN;
import utils.TimeIntervalMR;
import utils.Trajectory;

public class BFAlg {
    public long nnJoinTime = 0;
    public long rangeJoinTime = 0;
    // POI tree
    public QuadTree qTree = null;
    // pre-checking count
    public double tbPruningCount = 0;
    public double kPruningCount = 0;

    public BFAlg(QuadTree qTree) {
        this.qTree = qTree;
    }

    public static Comparator<NN> NNComparator = new Comparator<NN>() {
        @Override
        public int compare(NN p1, NN p2) {
            return p1.sim - p2.sim > 0 ? 1 : -1;
        }
    };

    public ArrayList<TimeIntervalMR> tbSearch(TimeIntervalMR mra, TimeIntervalMR[] MR_B, double simThreshold) {
        ArrayList<TimeIntervalMR> res = new ArrayList<>();
        ArrayList<TimeIntervalMR> candidates = new ArrayList<>();
        // basic pre-checking using direct distance computation
        for (TimeIntervalMR mrb : MR_B) {
            double[] A = mra.center;
            double[] B = mrb.center;
            double dist = Math.sqrt(Math.pow(A[0] - B[0], 2) + Math.pow(A[1] - B[1], 2));
            if (dist <= mra.a + mrb.a) {
                candidates.add(mrb);
            }
        }
        tbPruningCount += (MR_B.length - candidates.size());
        // basic refinement
        for (TimeIntervalMR c : candidates) {
            double sim = mra.simTo(c, this.qTree);
            if (sim >= simThreshold) {
                res.add(c);
            }
        }
        return res;
    }

    // Threshold-based Join
    public int tbJoin(TimeIntervalMR[] MR_A,
            TimeIntervalMR[] MR_B, double simThreshold) {
        long t1 = System.currentTimeMillis();
        int matchNB = 0;
        int i = 0;
        for (TimeIntervalMR mra : MR_A) {
            i++;
            if (i % (MR_A.length / 10) == 0) {
                System.out.print(i * 100 / MR_A.length + "%-> ");
            }
            ArrayList<TimeIntervalMR> res = tbSearch(mra, MR_B, simThreshold);
            matchNB += res.size();
        }
        System.out.println("Precheck Pruning Ratio: " + (tbPruningCount / (MR_A.length * MR_B.length)));
        long t2 = System.currentTimeMillis();
        rangeJoinTime = t2 - t1;
        return matchNB;
    }

    public PriorityQueue<NN> nnSearch(ArrayList<Trajectory> db, Trajectory qTrj, int k) {
        PriorityQueue<NN> nnQueue = new PriorityQueue<>(NNComparator);
        // for (Trajectory dbTrj : db) {
        // double sampleSim = qTrj.sampleLocsimTo(dbTrj);
        // double sim = qTrj.simTo(dbTrj, sampleSim);
        // if (nnQueue.size() < k) {
        // nnQueue.add(new NN(dbTrj, sim));
        // continue;
        // }
        // if (sim > nnQueue.peek().sim) {
        // nnQueue.poll();
        // nnQueue.add(new NN(dbTrj, sim));
        // }
        // }
        return nnQueue;
    }

    public ArrayList<PriorityQueue<NN>> nnJoin(ArrayList<Trajectory> queries, ArrayList<Trajectory> db, int k) {
        long t1 = System.currentTimeMillis();
        ArrayList<PriorityQueue<NN>> nns = new ArrayList<>();
        int i = 0;
        for (Trajectory qTrj : queries) {
            i++;
            if (i % (queries.size() / 10) == 0) {
                System.out.print(i * 100 / queries.size() + "%-> ");
            }
            PriorityQueue<NN> nn = nnSearch(db, qTrj, k);
            nns.add(nn);
        }
        long t2 = System.currentTimeMillis();
        nnJoinTime += (t2 - t1);
        return nns;
    }

}