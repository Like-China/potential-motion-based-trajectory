package evaluation;

import java.util.*;

import balltree.TernaryBallNode;
import balltree.TernaryBallTree;
import poi.QuadTree;
import utils.NN;
import utils.TimeIntervalMR;

public class BJAlg {
    // index construction time / filtering time
    public long cTime = 0;
    public long nnJoinTime = 0;
    public long rangeJoinTime = 0;
    // the number of node accesses
    public int searchCount = 0;
    public int intersectCount = 0;
    // the repartition threshold
    public double repartirionRatio = 0;
    public int minLeafNB = 0;
    // pruning count
    public int candidateCount = 0;
    public double upper1PruningCount = 0;
    public double upper2PruningCount = 0;
    public double nnPruningCount = 0;
    // POI tree
    public QuadTree qTree = null;

    public static Comparator<NN> NNComparator = new Comparator<NN>() {
        @Override
        public int compare(NN p1, NN p2) {
            return p1.sim - p2.sim > 0 ? 1 : -1;
        }
    };

    public BJAlg(TimeIntervalMR[] MR_A, TimeIntervalMR[] MR_B, double repartirionRatio,
            int minLeafNB, QuadTree qTree) {
        this.repartirionRatio = repartirionRatio;
        this.minLeafNB = minLeafNB;
        this.qTree = qTree;
    }

    public ArrayList<TimeIntervalMR> tbSearch(TimeIntervalMR mra, TernaryBallTree bt, TernaryBallNode root,
            double simThreshold) {
        ArrayList<TimeIntervalMR> res = new ArrayList<>();
        // pre-checking to get candidates
        ArrayList<TimeIntervalMR> candidates = bt.searchRange(root, mra);
        candidateCount += candidates.size();
        if (mra.POIs.size() == 0) {
            return res;
        }
        for (TimeIntervalMR c : candidates) {
            // double simUpper = mra.upperBound1To(c);
            // if (simUpper < simThreshold) {
            // upper1PruningCount += 1;
            // continue;
            // }
            double sim = mra.simTo(c, this.qTree);
            if (sim >= simThreshold) {
                // System.out.println(simUpper + "/" + sim + "/" + simThreshold);
                res.add(c);
            }
        }
        return res;
    }

    // Threshold-based Join
    public int tbJoin(TimeIntervalMR[] MR_A, TimeIntervalMR[] MR_B, double simThreshold) {
        long t1 = System.currentTimeMillis();

        TernaryBallTree bt = new TernaryBallTree(minLeafNB, MR_B, repartirionRatio);
        TernaryBallNode root = bt.buildBallTree();
        cTime = System.currentTimeMillis() - t1;

        int matchNB = 0;
        int i = 0;
        for (TimeIntervalMR mra : MR_A) {
            i++;
            if (i % (MR_A.length / 10) == 0) {
                System.out.print(i * 100 / MR_A.length + "%-> ");
            }
            ArrayList<TimeIntervalMR> res = tbSearch(mra, bt, root, simThreshold);
            matchNB += res.size();
        }
        long t2 = System.currentTimeMillis();
        rangeJoinTime = t2 - t1;
        System.out.println("\n-- Upper 1 Pruning Count: " + upper1PruningCount + " Upper 1  Pruning Ratio: "
                + (upper1PruningCount / candidateCount));
        return matchNB;
    }

    // public PriorityQueue<NN> nnSearch(ArrayList<Trajectory> db, Trajectory qTrj,
    // int k) {
    // PriorityQueue<NN> nnQueue = new PriorityQueue<>(NNComparator);

    // HashMap<Integer, Integer> interCount = new HashMap<>();
    // ArrayList<Trajectory> checked = new ArrayList<>();
    // if (Settings.useOrder) {
    // // sort trajectories with intersection count, then calculate the similarity
    // to
    // // another trajectories that have more intersection count
    // HashMap<Integer, ArrayList<TimeIntervalMR>> ts2candidate = qTrj.ts2candidate;
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
    // }

    // if (interCount.size() >= k) {
    // List<Map.Entry<Integer, Integer>> list = new ArrayList<Map.Entry<Integer,
    // Integer>>(interCount.entrySet());
    // int listSize = list.size();
    // if (listSize > k) {
    // Collections.sort(list, new Comparator<Map.Entry<Integer, Integer>>() {
    // @Override
    // public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer,
    // Integer> o2) {
    // return o2.getValue().compareTo(o1.getValue());
    // }
    // });
    // list = list.subList(0, k);
    // }
    // for (Map.Entry<Integer, Integer> mapping : list) {
    // Trajectory nnCandidate = db.get(mapping.getKey());
    // checked.add(nnCandidate);
    // double sampleSim = qTrj.sampleLocsimTo(nnCandidate);
    // double sim = qTrj.simTo(nnCandidate, sampleSim);
    // nnQueue.add(new NN(nnCandidate, sim));
    // }
    // } else {
    // for (int i = 0; i < k; i++) {
    // Trajectory nnCandidate = db.get(i);
    // double sampleSim = qTrj.sampleLocsimTo(nnCandidate);
    // double sim = qTrj.simTo(nnCandidate, sampleSim);
    // nnQueue.add(new NN(nnCandidate, sim));
    // checked.add(nnCandidate);
    // }
    // }

    // for (int i = 0; i < db.size(); i++) {
    // Trajectory nnCandidate = db.get(i);
    // if (checked.contains(nnCandidate))
    // continue;
    // double curMinNNSim = nnQueue.peek().sim;
    // double upper = qTrj.upperBoundTo(nnCandidate);
    // if (curMinNNSim > upper) {
    // nnPruningCount++;
    // // System.out.println(curMinNNSim + "/" + upper);
    // continue;
    // }
    // double sampleSim = qTrj.sampleLocsimTo(nnCandidate);
    // double sim = qTrj.simTo(nnCandidate, sampleSim);
    // if (sim > curMinNNSim) {
    // nnQueue.poll();
    // nnQueue.add(new NN(nnCandidate, sim));
    // }
    // }

    // return nnQueue;
    // }

    // public ArrayList<PriorityQueue<NN>> nnJoin(ArrayList<Trajectory> queries,
    // ArrayList<Trajectory> db, int k) {
    // long t1 = System.currentTimeMillis();
    // ArrayList<PriorityQueue<NN>> nns = new ArrayList<>();
    // int i = 0;
    // int qSize = queries.size();
    // int dbSize = db.size();
    // for (Trajectory trj1 : queries) {
    // i++;
    // if (i % (qSize / 10) == 0) {
    // System.out.print(i * 100 / qSize + "%-> ");
    // }
    // PriorityQueue<NN> nn = nnSearch(db, trj1, k);
    // nns.add(nn);
    // }
    // long t2 = System.currentTimeMillis();
    // nnJoinTime += (t2 - t1);
    // System.out.println("\n-- NN Pruning count: " + nnPruningCount + " Pruning
    // Ratio: "
    // + (nnPruningCount / (dbSize * qSize)));
    // return nns;
    // }

}
