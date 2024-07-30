package evaluation;

import java.util.ArrayList;
import java.util.HashMap;

import utils.ContactPair;
import utils.TimeIntervalMR;
import utils.Trajectory;

public class Test {
    public static void main(String[] args) {
        long startTime, endTime;
        Loader l = new Loader();
        int n = 5000;
        double simThreshold = 0.01;
        l.getTrajectoryData(n);
        l.getQueryDB(n / 2);
        // for (Trajectory t : l.trjs) {
        // System.out.println(t);
        // }

        int i = 0;
        int matchNB = 0;
        startTime = System.currentTimeMillis();
        for (Trajectory qTrj : l.queries) {
            i++;
            if (i % 1000 == 0) {
                System.out.println(i + "/" + n / 2);
            }
            for (Trajectory dbTrj : l.db) {
                double sim = qTrj.simTo(dbTrj);
                if (sim > simThreshold) {
                    matchNB++;
                }
            }
        }
        endTime = System.currentTimeMillis();
        System.out.println(String.format("Brute-Force Match Number: %d Time cost: %d", matchNB, endTime - startTime));

        matchNB = 0;
        startTime = System.currentTimeMillis();
        for (Trajectory qTrj : l.queries) {
            i++;
            for (Trajectory dbTrj : l.db) {
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
        endTime = System.currentTimeMillis();
        System.out.println(String.format("BF Pruning Match Number: %d Time cost: %d", matchNB, endTime - startTime));

        // construct ball-tree index then pruning
        startTime = System.currentTimeMillis();
        BJAlg bj = new BJAlg(Settings.tsNB, Settings.repartitionRatio, Settings.minLeafNB);
        bj.constructMulti(l.db);
        int candidateNB = 0;
        for (Trajectory trj1 : l.queries) {
            for (int ts = 0; ts < Settings.tsNB; ts++) {
                ArrayList<TimeIntervalMR> candidate = bj.getCandidate(ts, trj1.EllipseSeq.get(ts));
                candidateNB += candidate.size();
            }
        }
        endTime = System.currentTimeMillis();
        System.out.println(
                String.format("Ball-tree Pruning Match Number: %d Time cost: %d", candidateNB, endTime - startTime));

        // construct M-tree index then pruning
        startTime = System.currentTimeMillis();
        MJAlg mj = new MJAlg(Settings.tsNB);
        mj.constructMulti(l.db);
        candidateNB = 0;
        for (Trajectory trj1 : l.queries) {
            for (int ts = 0; ts < Settings.tsNB; ts++) {
                ArrayList<ContactPair> candidate = mj.getCandidate(ts, trj1.DataSeq.get(ts));
                candidateNB += candidate.size();
            }
        }
        endTime = System.currentTimeMillis();
        System.out.println(
                String.format("M-Tree Pruning Match Number: %d Time cost: %d", candidateNB, endTime - startTime));

    }
}
