package evaluation;

import java.util.ArrayList;
import balltree.TernaryBallNode;
import balltree.TernaryBallTree;
import utils.ContactPair;
import utils.Data;

public class BJAlg {
    // query, database set at each timestamp, we update them at each timestampe
    public ArrayList<Data> queries = new ArrayList<>();
    public ArrayList<Data> db = new ArrayList<>();
    // index construction time / filtering time
    public long cTime = 0;
    public long fTime = 0;
    // the number of node accesses
    public int searchCount = 0;
    // the repartition threshold
    public double repartirionRatio = 0;
    public int minLeafNB = 0;

    public BJAlg(ArrayList<Data> queries, ArrayList<Data> db, double repartirionRatio, int minLeafNB) {
        this.queries = queries;
        this.db = db;
        this.repartirionRatio = repartirionRatio;
        this.minLeafNB = minLeafNB;
    }

    /**
     * conduct BJ-Alg method to obtain all candidate pairs
     * 
     * @return all candidate pairs
     */
    public ArrayList<ContactPair> getCandidate() {
        long t1 = System.currentTimeMillis();
        TernaryBallTree bt = new TernaryBallTree(minLeafNB, db, repartirionRatio);
        TernaryBallNode root = bt.buildBallTree();
        // root.levelOrder(root);
        long t2 = System.currentTimeMillis();
        cTime = t2 - t1;

        ArrayList<ContactPair> candidates = new ArrayList<>();
        t1 = System.currentTimeMillis();
        for (Data qdata : queries) {
            ArrayList<ContactPair> ballRangeResult = bt.searchRange(root, qdata);
            candidates.addAll(ballRangeResult);
        }
        searchCount = bt.searchCount;
        t2 = System.currentTimeMillis();
        fTime = t2 - t1;
        return candidates;
    }

}