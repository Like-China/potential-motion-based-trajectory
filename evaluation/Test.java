package evaluation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import poi.QuadTree;
import utils.Point;
import utils.TimeIntervalMR;
import utils.TimePointMR;
import utils.Trajectory;

public class Test {

    // Write results log
    public static void writeFile(String setInfo, String otherInfo) {
        try {
            File writeName = new File(Settings.data + "out.txt");
            writeName.createNewFile();
            try (FileWriter writer = new FileWriter(writeName, true);
                    BufferedWriter out = new BufferedWriter(writer)) {
                out.write(setInfo);
                out.newLine();
                out.write(otherInfo);
                out.newLine();
                out.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void testGetPOI(ArrayList<TimeIntervalMR> MR_A, Loader l) {
        // test the POI retrieve
        long t1 = System.currentTimeMillis();
        for (TimeIntervalMR mr : MR_A) {
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
        System.out.println(System.currentTimeMillis() - t1);
    }

    // Given a query interval I=[i,j], we get all MR(o,I) from trajectories.
    public static TimeIntervalMR[] getMRSet(ArrayList<Trajectory> trjs, int start, int end, QuadTree qTree) {
        assert start >= 0;
        assert end <= Settings.tsNB;
        TimeIntervalMR[] mrs = new TimeIntervalMR[trjs.size()];
        for (int i = 0; i < trjs.size(); i++) {
            mrs[i] = trjs.get(i).getIntervalMR(start, end, qTree);
        }
        return mrs;
    }

    public static void main(String[] args) {
        Loader l = new Loader(Settings.dataNB, 100000);
        // query and database trajectories
        ArrayList<Trajectory> A = l.A;
        ArrayList<Trajectory> B = l.B;
        double theta = Settings.simThreshold;
        QuadTree poiTree = l.qStatic;

        int start = 0;
        int end = 1;
        TimeIntervalMR[] MR_A = getMRSet(A, start, end, poiTree);
        TimeIntervalMR[] MR_B = getMRSet(B, start, end, poiTree);
        // teseGetPOI(MR_A, l);

        // Basic Filter-and-refine solution
        BFAlg bf = new BFAlg(poiTree);
        int bfMatchNB = bf.tbJoin(MR_A, MR_B, theta);
        System.out.println(String.format("BFR Range-Join MatchNB: %d Time cost: %d\n",
                bfMatchNB, bf.rangeJoinTime));
        // MTree
        MJAlg mt = new MJAlg(poiTree);
        int mtMatchNB = mt.tbJoin(MR_A, MR_B, theta);
        System.out.println(String.format("MTR RJoin MatchNB: %d CTime: %d FTime: %d\n",
                mtMatchNB, mt.cTime, mt.rangeJoinTime));
        // Ball-tree index then pruning
        BJAlg bj = new BJAlg(MR_A, MR_B, Settings.repartitionRatio, Settings.minLeafNB, poiTree);
        int bjMatchNB = bj.tbJoin(MR_A, MR_B, theta);
        System.out.println(
                String.format("BTR RJoin MatchNB: %d CTime: %d FTime: %d\n", bjMatchNB, bj.cTime, bj.rangeJoinTime));

        // assert bfMatchNB == bjMatchNB;

        // ArrayList<PriorityQueue<NN>> bfNNRes = bf.nnJoin(A, B, Settings.k);
        // System.out.println(String.format("Brute-Force NN-Join Time cost: %d",
        // bf.nnJoinTime));

        // assert bfNNRes.size() == bjNNRes.size();
        // for (int i = 0; i < bfNNRes.size(); i++) {
        // PriorityQueue<NN> bfNNs = bfNNRes.get(i);
        // PriorityQueue<NN> bjNNs = bjNNRes.get(i);
        // for (int j = 0; j < bfNNs.size(); j++) {
        // NN bfNN = bfNNs.poll();
        // NN bjNN = bjNNs.poll();
        // assert bfNN.sim == bjNN.sim;
        // }
        // }

        // ArrayList<PriorityQueue<NN>> bjNNRes = bj.nnJoin(A, B, Settings.k);
        // System.out.println(String.format("Ball-tree NN-Join CTime: %d FTime: %d\n",
        // bj.cTime, bj.nnJoinTime));

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
