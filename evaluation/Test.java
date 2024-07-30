package evaluation;

import java.util.ArrayList;
import java.util.PriorityQueue;

import utils.Location;
import utils.NN;
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
        int n = Settings.objectNB;
        double simThreshold = 0.05;
        l.getTrajectoryData(n);
        l.getQueryDB(n / 2);

        BFAlg bf = new BFAlg();
        int matchNB = 0;

        startTime = System.currentTimeMillis();
        // matchNB = bf.getAll(l.queries, l.db, simThreshold);
        matchNB = bf.rangeJoin(l.queries, l.db, simThreshold);
        endTime = System.currentTimeMillis();
        System.out.println(
                String.format("Brute-Force Range Join Match Number: %d Time cost: %d", matchNB, endTime -
                        startTime));

        // startTime = System.currentTimeMillis();
        // bf.nnJoin(l.queries, l.db, 5);
        // endTime = System.currentTimeMillis();
        // System.out.println(
        // String.format("Brute-Force NN Join Time cost: %d", endTime - startTime));

        // startTime = System.currentTimeMillis();
        // matchNB = bf.getIntersection(l.queries, l.db);
        // endTime = System.currentTimeMillis();
        // System.out.println(String.format("BF Pruning Match Number: %d Time cost: %d",
        // matchNB, endTime - startTime));

        // construct ball-tree index then pruning
        BJAlg bj = new BJAlg(l.queries, l.db, Settings.tsNB, Settings.repartitionRatio, Settings.minLeafNB);
        bj.nnJoin(l.queries, l.db, 10);
        System.out.println(String.format("Ball-tree NN-Join CTime: %d FTime: %d", bj.cTime, bj.fTime));

        matchNB = bj.rangeJoin(l.queries, l.db, simThreshold);
        System.out.println(
                String.format("Ball-tree Range-Join MatchNB: %d CTime: %d FTime: %d", matchNB, bj.cTime, bj.fTime));

        // construct M-tree index then pruning
        // startTime = System.currentTimeMillis();
        // MJAlg mj = new MJAlg(Settings.tsNB);
        // mj.constructMulti(l.db);
        // int candidateNB = 0;
        // for (Trajectory trj1 : l.queries) {
        // for (int ts = 0; ts < Settings.tsNB - 1; ts++) {
        // ArrayList<TimeIntervalMR> candidate = mj.getIntersection(ts,
        // trj1.DataSeq.get(ts));
        // candidateNB += candidate.size();
        // }
        // }
        // endTime = System.currentTimeMillis();
        // System.out.println(
        // String.format("M-Tree Pruning Match Number: %d Time cost: %d", candidateNB,
        // endTime - startTime));

    }
}
