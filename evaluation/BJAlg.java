package evaluation;

import java.util.*;
import balltree.TernaryBallNode;
import balltree.TernaryBallTree;
import utils.TimeIntervalMR;
import utils.Trajectory;

public class BJAlg {
    // index construction time / filtering time
    public long cTime = 0;
    public long fTime = 0;
    // the number of node accesses
    public int searchCount = 0;
    // the repartition threshold
    public double repartirionRatio = 0;
    public int minLeafNB = 0;
    public int tsNB = 0;
    // tree structure
    HashMap<Integer, TernaryBallTree> ballTreeAtEachTimestamp = new HashMap<>();
    HashMap<Integer, TernaryBallNode> ballRootAtEachTimestamp = new HashMap<>();

    public BJAlg(int tsNB, double repartirionRatio, int minLeafNB) {
        this.tsNB = tsNB;
        this.repartirionRatio = repartirionRatio;
        this.minLeafNB = minLeafNB;
    }

    // construct indexes at all timestamp
    public void constructMulti(ArrayList<Trajectory> db) {
        long t1 = System.currentTimeMillis();
        for (int ts = 0; ts < tsNB; ts++) {
            ArrayList<TimeIntervalMR> motionRanges = new ArrayList<>();
            for (Trajectory dbTrj : db) {
                motionRanges.add(dbTrj.EllipseSeq.get(ts));
            }
            TernaryBallTree bt = new TernaryBallTree(minLeafNB, motionRanges, repartirionRatio);
            TernaryBallNode root = bt.buildBallTree();
            ballTreeAtEachTimestamp.put(ts, bt);
            ballRootAtEachTimestamp.put(ts, root);
        }
        long t2 = System.currentTimeMillis();
        cTime += (t2 - t1);
    }

    /**
     * conduct BJ-Alg method to obtain all candidate pairs
     * 
     * @return all candidate pairs
     */
    public ArrayList<TimeIntervalMR> getCandidate(int idx, TimeIntervalMR q) {
        TernaryBallTree bt = ballTreeAtEachTimestamp.get(idx);
        TernaryBallNode root = ballRootAtEachTimestamp.get(idx);
        long t1 = System.currentTimeMillis();
        ArrayList<TimeIntervalMR> candidates = bt.searchRange(root, q);
        long t2 = System.currentTimeMillis();
        searchCount += bt.searchCount;
        t2 = System.currentTimeMillis();
        fTime += t2 - t1;
        return candidates;
    }
}
