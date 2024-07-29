package evaluation;

import java.util.ArrayList;
import balltree.BallNode;
import balltree.BallTree;
import utils.ContactPair;
import utils.Data;

public class BJAlgNoRepartition {
    // query, database set at each timestamp, we update them at each timestampe
    public ArrayList<Data> queries = new ArrayList<>();
    public ArrayList<Data> db = new ArrayList<>();
    // index construction time / filtering time
    public long cTime = 0;
    public long fTime = 0;
    // the number of node accesses
    public int searchCount = 0;
    public int minLeafNB = 0;

    public BJAlgNoRepartition(ArrayList<Data> queries, ArrayList<Data> db, int minLeafNB) {
        this.queries = queries;
        this.db = db;
        this.minLeafNB = minLeafNB;
    }

    /**
     * conduct BJ-Alg without reportition strategy to obtain all candidate pairs
     * 
     * @return all candidate pairs
     */
    public ArrayList<ContactPair> getCandidate() {
        long t1 = System.currentTimeMillis();
        BallTree bt = new BallTree(minLeafNB, db);
        BallNode root = bt.buildBallTree();
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