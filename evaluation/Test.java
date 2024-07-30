package evaluation;

import java.util.ArrayList;

import utils.Location;
import utils.Point;
import utils.TimeIntervalMR;
import utils.Trajectory;

public class Test {

    // get all location of a trajectory including its poential POIs
    public ArrayList<double[]> getPoints(Trajectory trj) {
        ArrayList<double[]> locations = new ArrayList<>();
        for (Location loc : trj.locationSeq) {
            locations.add(new double[] { loc.x, loc.y });
        }
        ArrayList<double[]> POIS = new ArrayList<>();
        for (TimeIntervalMR mr : trj.EllipseSeq) {
            for (Point[] ps : mr.POIs) {
                for (Point p : ps) {
                    POIS.add(new double[] { p.x, p.y });
                }
            }
        }
        return POIS;
    }

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

        BFAlg bf = new BFAlg();
        startTime = System.currentTimeMillis();
        int matchNB = bf.getAll(l.queries, l.db, simThreshold);
        endTime = System.currentTimeMillis();
        System.out.println(String.format("Brute-Force Match Number: %d Time cost: %d", matchNB, endTime - startTime));

        startTime = System.currentTimeMillis();
        matchNB = bf.getCandidate(l.queries, l.db);
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
                ArrayList<TimeIntervalMR> candidate = mj.getCandidate(ts, trj1.DataSeq.get(ts));
                candidateNB += candidate.size();
            }
        }
        endTime = System.currentTimeMillis();
        System.out.println(
                String.format("M-Tree Pruning Match Number: %d Time cost: %d", candidateNB, endTime - startTime));

    }
}
