package evaluation;

import java.util.*;
import balltree.TernaryBallNode;
import balltree.TernaryBallTree;
import utils.NN;
import utils.TimeIntervalMR;
import utils.Trajectory;

public class BJAlg {
    // index construction time / filtering time
    public long cTime = 0;
    public long fTime = 0;
    // the number of node accesses
    public int searchCount = 0;
    public int intersectCount = 0;
    // the repartition threshold
    public double repartirionRatio = 0;
    public int minLeafNB = 0;
    public int tsNB = 0;

    public static Comparator<NN> NNComparator = new Comparator<NN>() {
        @Override
        public int compare(NN p1, NN p2) {
            return p1.sim - p2.sim > 0 ? 1 : -1;
        }
    };

    public BJAlg(ArrayList<Trajectory> queries, ArrayList<Trajectory> db, int tsNB, double repartirionRatio,
            int minLeafNB) {
        this.tsNB = tsNB;
        this.repartirionRatio = repartirionRatio;
        this.minLeafNB = minLeafNB;
        // write intersection info into each trajectory
        constructMulti(queries, db);
    }

    // construct indexes at all timestamp
    public void constructMulti(ArrayList<Trajectory> queries, ArrayList<Trajectory> db) {
        long t1 = System.currentTimeMillis();
        for (int ts = 0; ts < tsNB - 1; ts++) {
            ArrayList<TimeIntervalMR> motionRanges = new ArrayList<>();
            for (Trajectory dbTrj : db) {
                motionRanges.add(dbTrj.EllipseSeq.get(ts));
            }
            TernaryBallTree bt = new TernaryBallTree(minLeafNB, motionRanges, repartirionRatio);
            TernaryBallNode root = bt.buildBallTree();
            for (Trajectory qTrj : queries) {
                TimeIntervalMR q = qTrj.EllipseSeq.get(ts);
                ArrayList<TimeIntervalMR> candidates = bt.searchRange(root, q);
                qTrj.ts2candidate.put(ts, candidates);
            }
        }
        long t2 = System.currentTimeMillis();
        cTime += (t2 - t1);
    }

    public ArrayList<Trajectory> rangeSearch(Trajectory qTrj, ArrayList<Trajectory> db, double simThreshold) {
        ArrayList<Trajectory> res = new ArrayList<>();
        for (Trajectory dbTrj : db) {
            if (qTrj.upperBoundTo(dbTrj) < simThreshold) {
                continue;
            }
            double sim = qTrj.simTo(dbTrj);
            if (sim >= simThreshold) {
                res.add(dbTrj);
            }
        }
        return res;
    }

    public int rangeJoin(ArrayList<Trajectory> queries, ArrayList<Trajectory> db, double simThreshold) {
        long t1 = System.currentTimeMillis();
        int matchNB = 0;
        int i = 0;
        for (Trajectory qTrj : queries) {
            i++;
            if (i % 100 == 0) {
                System.out.println(i + "/" + queries.size());
            }
            ArrayList<Trajectory> res = rangeSearch(qTrj, db, simThreshold);
            matchNB += res.size();
        }
        long t2 = System.currentTimeMillis();
        fTime = t2 - t1;
        return matchNB;
    }

    public PriorityQueue<NN> nnSearch(ArrayList<Trajectory> db, Trajectory qTrj, int k) {
        HashMap<Integer, ArrayList<TimeIntervalMR>> ts2candidate = qTrj.ts2candidate;
        HashMap<Integer, Integer> interCount = new HashMap<>();

        PriorityQueue<NN> candidateQueue = new PriorityQueue<>(NNComparator);
        PriorityQueue<NN> nnQueue = new PriorityQueue<>(NNComparator);

        // exact calculation to refine

        // for (int ts = 0; ts < tsNB - 1; ts++) {
        // ArrayList<TimeIntervalMR> mrs = ts2candidate.get(ts);
        // for (TimeIntervalMR mr : mrs) {
        // int objID = mr.objectID;
        // if (interCount.containsKey(objID)) {
        // interCount.put(objID, interCount.get(objID) + 1);
        // } else {
        // interCount.put(objID, 1);
        // }
        // }
        // }

        // sort trajectories with intersection count

        // List<Map.Entry<Integer, Integer>> list = new ArrayList<Map.Entry<Integer,
        // Integer>>(interCount.entrySet());
        // Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>() {
        // @Override
        // public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer,
        // Integer> o2) {
        // return o2.getValue().compareTo(o1.getValue());
        // }
        // });

        // 1. calculate the similarity to another trajectories that have more
        // intersection count

        // for (Map.Entry<Integer, Integer> mapping : list) {
        // Trajectory nnCandidate = db.get(mapping.getKey() - Settings.objectNB / 2);
        // double sim = qTrj.simTo(nnCandidate);
        // if (nnQueue.size() < k) {
        // nnQueue.add(new NN(nnCandidate, sim));
        // continue;
        // }
        // if (nnQueue.size() >= k && sim > nnQueue.peek().sim) {
        // nnQueue.poll();
        // nnQueue.add(new NN(nnCandidate, sim));
        // }
        // }

        // System.out.println("minNNsim: " + nnQueue.peek().sim);

        double max = 0;
        for (Trajectory dbTrj : db) {
            double upper = qTrj.upperBoundTo(dbTrj);
            candidateQueue.add(new NN(dbTrj, upper));
            max = max > upper ? max : upper;
        }
        // System.out.println("max upper: " + max);

        for (int i = 0; i < k; i++) {
            NN candidate = candidateQueue.poll();
            Trajectory nnCandidate = candidate.trj;
            double sim = qTrj.simTo(nnCandidate);
            nnQueue.add(new NN(nnCandidate, sim));
        }
        int calcNB = 0;
        while (!candidateQueue.isEmpty()) {
            NN candidate = candidateQueue.poll();
            Trajectory nnCandidate = candidate.trj;
            double curMinNNSim = nnQueue.peek().sim;
            calcNB++;
            if (curMinNNSim > candidate.sim) {
                // System.out.println(calcNB + "/" + curMinNNSim + "/" + candidate.sim);
                break;
            }
            double sim = qTrj.simTo(nnCandidate);
            if (sim > curMinNNSim) {
                nnQueue.poll();
                nnQueue.add(new NN(nnCandidate, sim));
            }
        }
        return nnQueue;
    }

    public void nnJoin(ArrayList<Trajectory> queries, ArrayList<Trajectory> db, int k) {
        long t1 = System.currentTimeMillis();
        for (Trajectory trj1 : queries) {
            PriorityQueue<NN> nn = nnSearch(db, trj1, k);
            // System.out.println(trj1.objectID);
            // while (!nn.isEmpty()) {
            // System.out.println(nn.poll());
            // }
            // System.out.println();

        }
        long t2 = System.currentTimeMillis();
        fTime += (t2 - t1);

    }

}
