package evaluation;

import java.util.ArrayList;
import balltree.TernaryBallNode;
import balltree.TernaryBallTree;
import utils.TimeIntervalMR;

public class BJAlg {
    // time-interval motion ranges for all moving objects in
    public ArrayList<TimeIntervalMR> db = new ArrayList<>();
    // index construction time / filtering time
    public long cTime = 0;
    public long fTime = 0;
    // the number of node accesses
    public int searchCount = 0;
    // the repartition threshold
    public double repartirionRatio = 0;
    public int minLeafNB = 0;
    // tree structure
    TernaryBallTree bt;
    TernaryBallNode root;

    public BJAlg(ArrayList<TimeIntervalMR> db, double repartirionRatio, int minLeafNB) {
        this.db = db;
        this.repartirionRatio = repartirionRatio;
        this.minLeafNB = minLeafNB;
        construct();
    }

    public TernaryBallNode construct() {
        long t1 = System.currentTimeMillis();
        bt = new TernaryBallTree(minLeafNB, db, repartirionRatio);
        root = bt.buildBallTree();
        // root.levelOrder(root);
        long t2 = System.currentTimeMillis();
        cTime = t2 - t1;
        return root;
    }

    /**
     * conduct BJ-Alg method to obtain all candidate pairs
     * 
     * @return all candidate pairs
     */
    public ArrayList<TimeIntervalMR> getCandidate(TimeIntervalMR q) {

        long t1 = System.currentTimeMillis();
        ArrayList<TimeIntervalMR> candidates = bt.searchRange(root, q);
        long t2 = System.currentTimeMillis();
        searchCount = bt.searchCount;
        t2 = System.currentTimeMillis();
        fTime += t2 - t1;
        return candidates;
    }
}
