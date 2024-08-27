package evaluation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.PriorityQueue;

import poi.QuadTree1;
import poi.QuadTree1;
import utils.NN;
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
            res = l.qTree.query(mbr[0], mbr[2], mbr[1], mbr[3]);

            // mr.POIsWithinThis(l.qStatic);

            System.out.println(mr.timePointMRs.size());
            for (TimePointMR tMR : mr.timePointMRs) {
                tMR.POIsWithinThis(l.qTree);
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
    public static TimeIntervalMR[] getMRSet(ArrayList<Trajectory> trjs, int start, int end, QuadTree1 qTree) {
        assert start >= 0;
        assert end <= Settings.tsNB;
        TimeIntervalMR[] mrs = new TimeIntervalMR[trjs.size()];
        for (int i = 0; i < trjs.size(); i++) {
            mrs[i] = trjs.get(i).getIntervalMR(start, end, qTree);
        }
        return mrs;
    }

    public void vary(int expNB, int objNB, int poiNB, double theta, int start, int end, int k, String varyPara) {
        // M-tree HBall-tree HBall-tree*
        double[] tbJoin_times = new double[3];
        double[] kJoin_times = new double[3];
        double[] ctimes = new double[3];
        for (int i = 0; i < 3; i++) {
            tbJoin_times[i] = 0;
            kJoin_times[i] = 0;
            ctimes[i] = 0;
        }
        // pruning ratio of M-tree and HBall
        double MpruneRatios = 0;
        double BallpruneRatios = 0;
        double bNodeAccess = 0;
        double hbNodeAccess = 0;

        // decresed total number of candidates
        double ubPruneRatio = 0;

        for (int i = 0; i < expNB; i++) {
            Loader l = new Loader(objNB, poiNB);
            // query and database trajectories
            ArrayList<Trajectory> A = l.A;
            ArrayList<Trajectory> B = l.B;
            QuadTree1 poiTree = l.qTree;
            TimeIntervalMR[] MR_A = getMRSet(A, start, end, poiTree);
            TimeIntervalMR[] MR_B = getMRSet(B, start, end, poiTree);
            /* tb-Join */
            // MTree
            MJAlg mt = new MJAlg();
            int mtMatchNB = mt.tbJoin(MR_A, MR_B, theta);
            tbJoin_times[0] += mt.rangeJoinTime;
            ctimes[0] += mt.cTime;
            MpruneRatios += mt.pruneRatio;
            // Ball-tree index then pruning
            BJAlg bj = new BJAlg(MR_A, MR_B, Settings.minLeafNB);
            int bfMatchNB = bj.tbJoin(MR_A, MR_B, theta);
            bNodeAccess += bj.nodeAccess;

            HBJAlg hbj = new HBJAlg(MR_A, MR_B, Settings.repartitionRatio, Settings.minLeafNB);
            int hbjMatchNB = hbj.tbJoin(MR_A, MR_B, theta, false);
            tbJoin_times[1] += hbj.rangeJoinTime;
            ctimes[1] += hbj.cTime;
            hbNodeAccess += hbj.nodeAccess;

            hbj = new HBJAlg(MR_A, MR_B, Settings.repartitionRatio, Settings.minLeafNB);
            hbj.tbJoin(MR_A, MR_B, theta, true);
            tbJoin_times[2] += hbj.rangeJoinTime;
            ctimes[2] += hbj.cTime;
            ubPruneRatio += (mt.candidateCount - hbj.searchCount) / mt.candidateCount;
            BallpruneRatios += hbj.pruneRatio;

            /* kJoin */
            PriorityQueue<NN> mNNRes = mt.kJoin(MR_A, MR_B, k, true);
            kJoin_times[0] += mt.nnJoinTime;
            PriorityQueue<NN> hbjNNRes = hbj.kJoin(MR_A, MR_B, k, true);
            kJoin_times[1] += hbj.nnJoinTime;
        }
        for (int i = 0; i < 3; i++) {
            tbJoin_times[i] /= expNB;
            kJoin_times[i] /= expNB;
            ctimes[i] /= expNB;
        }
        MpruneRatios /= expNB;
        BallpruneRatios /= expNB;
        bNodeAccess /= expNB;
        hbNodeAccess /= expNB;
        ubPruneRatio /= expNB;
        String setInfo = String.format("Vary %s  expNB@%d objNB@%d poiNB@%d theta@%f   start@%d end@%d k@%d", varyPara,
                expNB, objNB, poiNB, theta, start, end, k);
        String info = "tbJoin_times: " + Arrays.toString(tbJoin_times) + "\nkJoin_times: "
                + Arrays.toString(kJoin_times) + "\nctimes: " + Arrays.toString(ctimes) + "\nPrune ratio: "
                + MpruneRatios + "," + BallpruneRatios + "\nAccess: "
                + bNodeAccess + "," + hbNodeAccess + "\nPruning Enhancement: " + ubPruneRatio + "\n";
        writeFile(setInfo, info);

    }

    public void singleTest() {
        Loader l = new Loader(Settings.objNB, 400000);
        // query and database trajectories
        ArrayList<Trajectory> A = l.A;
        ArrayList<Trajectory> B = l.B;
        double theta = Settings.simThreshold;
        QuadTree1 poiTree = l.qTree;

        int start = 0;
        int end = 1;
        TimeIntervalMR[] MR_A = getMRSet(A, start, end, poiTree);
        TimeIntervalMR[] MR_B = getMRSet(B, start, end, poiTree);
        // teseGetPOI(MR_A, l);

        BFAlg bf = new BFAlg();
        MJAlg mt = new MJAlg();
        BJAlg bj = new BJAlg(MR_A, MR_B, Settings.minLeafNB);
        HBJAlg hbj = new HBJAlg(MR_A, MR_B, Settings.repartitionRatio, Settings.minLeafNB);

        int bfMatchNB = bf.tbJoin(MR_A, MR_B, theta);
        System.out.println(String.format("BFR tbJoin MatchNB: %d Time cost:%d\n",
                bfMatchNB, bf.rangeJoinTime));
        int mtMatchNB = mt.tbJoin(MR_A, MR_B, theta);
        System.out.println(
                String.format("MTR tbJoin MatchNB: %d CTime: %d FTime:%d\n",
                        mtMatchNB, mt.cTime, mt.rangeJoinTime));
        int bjMatchNB = bj.tbJoin(MR_A, MR_B, theta);
        System.out.println(
                String.format("BTR tbJoin MatchNB: %d CTime: %d FTime: %d\n", bjMatchNB,
                        bj.cTime, bj.rangeJoinTime));
        int hbjMatchNB = hbj.tbJoin(MR_A, MR_B, theta, true);
        System.out.println(
                String.format("HBTR tbJoin MatchNB: %d CTime: %d FTime: %d\n", hbjMatchNB,
                        hbj.cTime, hbj.rangeJoinTime));
        assert bfMatchNB == hbjMatchNB;

        PriorityQueue<NN> mNNRes = mt.kJoin(MR_A, MR_B, Settings.k, true);
        System.out.println(String.format("M-tree NN-Join CTime: %d FTime: %d\n",
                mt.cTime, mt.nnJoinTime));

        PriorityQueue<NN> hbjNNRes = hbj.kJoin(MR_A, MR_B, Settings.k, true);
        System.out.println(String.format("HBall-tree NN-Join CTime: %d FTime: %d\n",
                hbj.cTime, hbj.nnJoinTime));

        PriorityQueue<NN> bjNNRes = bj.kJoin(MR_A, MR_B, Settings.k, true);
        System.out.println(String.format("Ball-tree NN-Join CTime: %d FTime: %d\n",
                bj.cTime, bj.nnJoinTime));

        assert mNNRes.size() == Settings.k;
        assert bjNNRes.size() == Settings.k;
        assert hbjNNRes.size() == Settings.k;
        while (!bjNNRes.isEmpty()) {
            NN nn1 = hbjNNRes.poll();
            NN nn2 = bjNNRes.poll();
            assert nn1.sim == nn2.sim : nn1 + " neq " + nn2;
        }

    }

    public static void main(String[] args) {
        Test test = new Test();
        // for (int objNB : new int[] { 2000, 4000, 6000, 8000, 10000 }) {
        // System.out.println("Varying objDB = " + objNB);
        // test.vary(Settings.expNB, objNB, Settings.poiNB,
        // Settings.simThreshold, Settings.start,
        // Settings.end, Settings.k, "Cardinality");
        // }

        // for (int objNB : new int[] { 2000, 4000, 6000, 8000, 10000 }) {
        // System.out.println("Varying objDB = " + objNB);
        // test.vary(Settings.expNB, objNB, Settings.poiNB,
        // Settings.simThreshold, Settings.start,
        // Settings.end, Settings.k, "Cardinality");
        // }
        // Settings.end, Settings.k);
        test.singleTest();

    }
}
