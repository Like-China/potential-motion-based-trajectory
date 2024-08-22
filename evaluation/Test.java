package evaluation;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

import utils.Location;
import utils.NN;
import utils.Point;
import utils.TimeIntervalMR;
import utils.TimePointMR;
import utils.Trajectory;

public class Test {

    public static void teseGetPOI(ArrayList<Trajectory> A, ArrayList<Trajectory> B, Loader l) {
        // test the POI retrieve
        long t1 = System.currentTimeMillis();
        for (Trajectory trj : A) {
            for (TimeIntervalMR mr : trj.EllipseSeq) {
                List<Point> res = new ArrayList<>();
                double[] mbr = mr.getEllipseMBR();
                res = l.qStatic.findAll(mbr[0], mbr[2], mbr[1], mbr[3]);

                // mr.POIsWithinThis(l.qStatic);

                System.out.println(mr.timePointMRs.size());
                for (TimePointMR tMR : mr.timePointMRs) {
                    tMR.POIsWithinThis(l.qStatic);
                }

                // int count = 0;
                // for (Point db : l.points) {
                // if (db.x >= mbr[0] && db.x <= mbr[1] && db.y >= mbr[2] && db.y <= mbr[3]) {
                // count++;
                // }
                // }
                // assert res.size() == count;
            }
        }
        System.out.println(System.currentTimeMillis() - t1);
    }

    public static void main(String[] args) {
        Loader l = new Loader(Settings.dataNB, 100000);
        // query and database trajectories
        ArrayList<Trajectory> A = l.A;
        ArrayList<Trajectory> B = l.B;
        double theta = Settings.simThreshold;
        teseGetPOI(A, B, l);

        System.exit(0);
        // construct ball-tree index then pruning
        BJAlg bj = new BJAlg(A, B, Settings.tsNB, Settings.repartitionRatio, Settings.minLeafNB);

        int bjMatchNB = bj.rangeJoin(A, B, theta);
        System.out.println(
                String.format("BT RJoin MatchNB: %d CTime: %d FTime: %d\n", bjMatchNB, bj.cTime, bj.rangeJoinTime));

        ArrayList<PriorityQueue<NN>> bjNNRes = bj.nnJoin(A, B, Settings.k);
        System.out.println(String.format("Ball-tree   NN-Join CTime: %d FTime: %d\n", bj.cTime, bj.nnJoinTime));

        // Brute-Force solution
        BFAlg bf = new BFAlg();

        int bfMatchNB = bf.rangeJoin(A, B, theta);
        System.out.println(String.format("BF Range-Join MatchNB: %d Time cost: %d",
                bfMatchNB, bf.rangeJoinTime));
        assert bfMatchNB == bjMatchNB;

        ArrayList<PriorityQueue<NN>> bfNNRes = bf.nnJoin(A, B, Settings.k);
        System.out.println(String.format("Brute-Force NN-Join Time cost: %d",
                bf.nnJoinTime));

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
        // mj.constructMulti(B);
        // int candidateNB = 0;
        // for (Trajectory trj1 : A) {
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
