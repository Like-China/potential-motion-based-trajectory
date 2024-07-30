package evaluation;

import java.util.*;

import balltree.TernaryBallNode;
import balltree.TernaryBallTree;
import mtree.MTreeClass;
import utils.*;

public class MJAlg {
    // index construction time / filtering time
    public long cTime = 0;
    public long fTime = 0;
    // the number of node accesses
    public int searchCount = 0;
    public int tsNB = 0;
    // tree structure
    HashMap<Integer, MTreeClass> MTreeAtEachTimestamp = new HashMap<>();

    public MJAlg(int tsNB) {
        this.tsNB = tsNB;
    }

    // construct indexes at all timestamp
    public void constructMulti(ArrayList<Trajectory> db) {
        long t1 = System.currentTimeMillis();
        for (int ts = 0; ts < tsNB; ts++) {
            MTreeClass mtree = new MTreeClass();
            for (Trajectory dbTrj : db) {
                Data data = dbTrj.DataSeq.get(ts);
                mtree.add(data);
            }
            MTreeAtEachTimestamp.put(ts, mtree);
        }
        long t2 = System.currentTimeMillis();
        cTime += (t2 - t1);
    }

    /**
     * conduct MJ-Alg method to obtain all candidate pairs
     * 
     * @return all candidate pairs
     */
    public ArrayList<TimeIntervalMR> getCandidate(int idx, Data qdata) {
        MTreeClass mtree = MTreeAtEachTimestamp.get(idx);
        long t1 = System.currentTimeMillis();
        MTreeClass.Query query = mtree.getNearestByRange(qdata, qdata.radius);
        ArrayList<TimeIntervalMR> candidates = query.rangeQuery();
        long t2 = System.currentTimeMillis();
        fTime += (t2 - t1);
        return candidates;
    }
}