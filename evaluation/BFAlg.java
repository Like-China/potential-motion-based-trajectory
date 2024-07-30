package evaluation;

import java.util.ArrayList;
import utils.TimeIntervalMR;
import utils.Trajectory;

public class BFAlg {
    public long fTime = 0;

    /**
     * conduct brute-force method to obtain all candidate pairs
     * 
     * @return all candidate pairs
     */
    public int getCandidate(ArrayList<Trajectory> queries,
            ArrayList<Trajectory> db) {
        long t1 = System.currentTimeMillis();
        int matchNB = 0;
        ArrayList<TimeIntervalMR> candidates = new ArrayList<>();
        for (Trajectory qTrj : queries) {
            for (Trajectory dbTrj : db) {
                for (int ts = 0; ts < Settings.tsNB; ts++) {
                    TimeIntervalMR mr1 = qTrj.EllipseSeq.get(ts);
                    TimeIntervalMR mr2 = dbTrj.EllipseSeq.get(ts);
                    double[] A = mr1.center;
                    double[] B = mr2.center;
                    double dist = Math.sqrt(Math.pow(A[0] - B[0], 2) + Math.pow(A[1] - B[1], 2));
                    if (dist <= mr1.a + mr2.a) {
                        matchNB++;
                    }
                }
            }
        }
        long t2 = System.currentTimeMillis();
        fTime = t2 - t1;
        return matchNB;
    }

    public int getAll(ArrayList<Trajectory> queries,
            ArrayList<Trajectory> db, double simThreshold) {
        long t1 = System.currentTimeMillis();
        int matchNB = 0;
        int i = 0;
        ArrayList<TimeIntervalMR> candidates = new ArrayList<>();
        for (Trajectory qTrj : queries) {
            i++;
            if (i % 1000 == 0) {
                System.out.println(i + "/" + queries.size());
            }
            for (Trajectory dbTrj : db) {
                double sim = qTrj.simTo(dbTrj);
                if (sim > simThreshold) {
                    matchNB++;
                }
            }
        }
        long t2 = System.currentTimeMillis();
        fTime = t2 - t1;
        return matchNB;
    }

}