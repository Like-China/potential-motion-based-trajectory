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
        Loader l = new Loader();
        int n = Settings.objectNB;
        double simThreshold = 0.05;
        l.getTrajectoryData(n);
        l.getQueryDB(n / 2);

        BFAlg bf = new BFAlg();
        // construct ball-tree index then pruning
        BJAlg bj = new BJAlg(l.queries, l.db, Settings.tsNB, Settings.repartitionRatio, Settings.minLeafNB);

        int bfMatchNB = bf.rangeJoin(l.queries, l.db, simThreshold);
        System.out.println(String.format("BF Range-Join MatchNB: %d Time cost: %d",
                bfMatchNB, bf.rangeJoinTime));

        int bjMatchNB = bj.rangeJoin(l.queries, l.db, simThreshold);
        System.out.println(String.format("BT Range-Join MatchNB: %d CTime: %d  FTime: %d", bjMatchNB, bj.cTime,
                bj.rangeJoinTime));
        assert bfMatchNB == bjMatchNB;

        ArrayList<PriorityQueue<NN>> bfNNRes = bf.nnJoin(l.queries, l.db, 5);
        System.out.println(String.format("Brute-Force NN-Join Time cost: %d", bf.nnJoinTime));

        ArrayList<PriorityQueue<NN>> bjNNRes = bj.nnJoin(l.queries, l.db, 5);
        System.out.println(String.format("Ball-tree   NN-Join CTime: %d FTime: %d", bj.cTime, bj.nnJoinTime));

        assert bfNNRes.size() == bjNNRes.size();
        for (int i = 0; i < bfNNRes.size(); i++) {
            PriorityQueue<NN> bfNNs = bfNNRes.get(i);
            PriorityQueue<NN> bjNNs = bjNNRes.get(i);
            for (int j = 0; j < bfNNs.size(); j++) {
                NN bfNN = bfNNs.poll();
                NN bjNN = bjNNs.poll();
                assert bfNN.sim == bjNN.sim;
            }
        }

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
