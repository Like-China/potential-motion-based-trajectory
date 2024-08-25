package evaluation;

import java.util.*;

import mtree.MTreeClass;
import poi.QuadTree;
import utils.*;

public class MJAlg {
    // index construction time / filtering time
    public long cTime = 0;
    public long nnJoinTime = 0;
    public long rangeJoinTime = 0;
    // the number of node accesses
    public int searchCount = 0;
    public int intersectCount = 0;
    // pruning count
    public double rangePruningCount = 0;
    public double nnPruningCount = 0;

    // POI tree
    public QuadTree qTree = null;

    public MJAlg(QuadTree qTree) {
        this.qTree = qTree;
    }

    public ArrayList<TimeIntervalMR> tbSearch(TimeIntervalMR mra, MTreeClass mtree, double simThreshold) {
        ArrayList<TimeIntervalMR> res = new ArrayList<>();
        // pre-checking to get candidates
        MTreeClass.Query query = mtree.getNearestByRange(mra, mra.a);
        ArrayList<TimeIntervalMR> candidates = query.rangeQuery();

        for (TimeIntervalMR c : candidates) {
            double sim = mra.simTo(c, this.qTree);
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
            if (i % (MR_A.length / 10) == 0) {
                System.out.print(i * 100 / MR_A.length + "%-> ");
            }
            ArrayList<TimeIntervalMR> res = tbSearch(mra, mtree, simThreshold);
            matchNB += res.size();
        }
        long t2 = System.currentTimeMillis();
        rangeJoinTime = t2 - t1;
        System.out.println("\n-- Range Pruning count: " + rangePruningCount + " Pruning Ratio: "
                + (rangePruningCount / (MR_A.length * MR_B.length)));
        return matchNB;
    }

}