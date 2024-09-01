package evaluation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import poi.QuadTree;
import utils.NN;
import utils.Point;
import utils.TimeIntervalMR;
import utils.TimePointMR;
import utils.Trajectory;

public class Test {

    public double[] tbJoin_times = new double[3];
    public double[] kJoin_times = new double[3];
    public double[] ctimes = new double[3];
    public double[] refineCounts = new double[2];
    public double MpruneRatios = 0;
    public double BallpruneRatios = 0;
    public double bNodeAccess = 0;
    public double hbNodeAccess = 0;
    public double ubPruneRatio = 0;

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
    public static TimeIntervalMR[] getMRSet(ArrayList<Trajectory> trjs, int start, int end, QuadTree qTree,
            int intervalNum) {
        assert start >= 0;
        assert end <= Settings.tsNB;
        TimeIntervalMR[] mrs = new TimeIntervalMR[trjs.size()];
        for (int i = 0; i < trjs.size(); i++) {
            mrs[i] = trjs.get(i).getIntervalMR(start, end, qTree, intervalNum);
        }
        return mrs;
    }

    public void vary(int expNB, int objNB, int poiNB, double theta, int start, int end, int k, int intervalNum,
            boolean isKnn,
            String varyPara) {
        // M-tree HBall-tree HBall-tree*
        tbJoin_times = new double[3];
        kJoin_times = new double[2];
        ctimes = new double[3];
        refineCounts = new double[2];
        for (int i = 0; i < 2; i++) {
            kJoin_times[i] = 0;
            refineCounts[i] = 0;
        }
        for (int i = 0; i < 3; i++) {
            tbJoin_times[i] = 0;
            ctimes[i] = 0;
        }
        // pruning ratio of M-tree and HBall

        MpruneRatios = 0;
        BallpruneRatios = 0;
        bNodeAccess = 0;
        hbNodeAccess = 0;

        // decresed total number of candidates
        ubPruneRatio = 0;

        for (int i = 0; i < expNB; i++) {
            Loader l = new Loader(objNB, poiNB);
            // query and database trajectories
            ArrayList<Trajectory> A = l.A;
            ArrayList<Trajectory> B = l.B;
            QuadTree poiTree = l.qTree;
            TimeIntervalMR[] MR_A = getMRSet(A, start, end, poiTree, intervalNum);
            TimeIntervalMR[] MR_B = getMRSet(B, start, end, poiTree, intervalNum);
            /* tb-Join */
            // MTree
            MJAlg mt = new MJAlg();
            int mtMatchNB = mt.tbJoin(MR_A, MR_B, theta);
            tbJoin_times[0] += mt.rangeJoinTime;
            ctimes[0] += mt.cTime;
            refineCounts[0] += mt.candidateCount;
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
            refineCounts[1] += hbj.searchCount;
            ubPruneRatio += (mt.candidateCount - hbj.searchCount) / mt.candidateCount;
            BallpruneRatios += hbj.pruneRatio;

            /* kJoin */
            if (isKnn) {
                PriorityQueue<NN> mNNRes = mt.kJoin(MR_A, MR_B, k, true);
                kJoin_times[0] += mt.nnJoinTime;
                PriorityQueue<NN> hbjNNRes = hbj.kJoin(MR_A, MR_B, k, true);
                kJoin_times[1] += hbj.nnJoinTime;
            }
        }
        for (int i = 0; i < 3; i++) {
            tbJoin_times[i] /= expNB;
            ctimes[i] /= expNB;

        }
        for (int i = 0; i < 2; i++) {
            kJoin_times[i] /= expNB;
            refineCounts[i] /= expNB;
        }
        MpruneRatios /= expNB;
        BallpruneRatios /= expNB;
        bNodeAccess /= expNB;
        hbNodeAccess /= expNB;
        ubPruneRatio /= expNB;
        String setInfo = String.format("Vary %s  expNB@%d objNB@%d poiNB@%d theta@%f   start@%d end@%d k@%d", varyPara,
                expNB, objNB, poiNB, theta, start, end, k);
        String info = "tbJoin_times: " + Arrays.toString(tbJoin_times) + "\nkJoin_times: "
                + Arrays.toString(kJoin_times) + "\nctimes: " + Arrays.toString(ctimes) + "\nRefine Count: "
                + Arrays.toString(refineCounts) + "\nPrune ratio: "
                + MpruneRatios + "," + BallpruneRatios + "\nAccess: "
                + bNodeAccess + "," + hbNodeAccess + "\nPruning Enhancement: " + ubPruneRatio + "\n";
        writeFile(setInfo, info);

    }

    // test all methods with the default settings
    public void singleTest() {
        Loader l = new Loader(Settings.objNB, Settings.poiNB);
        // query and database trajectories
        ArrayList<Trajectory> A = l.A;
        ArrayList<Trajectory> B = l.B;
        double theta = Settings.simThreshold;
        QuadTree poiTree = l.qTree;

        int start = Settings.start;
        int end = Settings.end;
        TimeIntervalMR[] MR_A = getMRSet(A, start, end, poiTree, Settings.intervalNum);
        TimeIntervalMR[] MR_B = getMRSet(B, start, end, poiTree, Settings.intervalNum);
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

    // test MBJ-Alg, HBJ-Alg, HBJ-Alg# with varying settings
    public void multiTest() {
        double[][] tbJoin_times_ = new double[5][3];
        double[][] kJoin_times_ = new double[5][2];
        double[][] refineCounts_ = new double[5][2];
        double[][] ctimes_ = new double[5][3];
        double[] MpruneRatios_ = new double[5];
        double[] BallpruneRatios_ = new double[5];
        double[] bNodeAccess_ = new double[5];
        double[] hbNodeAccess_ = new double[5];
        double[] ubPruneRatio_ = new double[5];

        int i = 0;

        for (int objNB : Settings.objNBs) {
            System.out.println("Varying objDB = " + objNB);
            this.vary(Settings.expNB, objNB, Settings.poiNB,
                    Settings.simThreshold, Settings.start,
                    Settings.end, Settings.k, Settings.intervalNum, false, "Cardinality");
            tbJoin_times_[i] = this.tbJoin_times;
            kJoin_times_[i] = this.kJoin_times;
            refineCounts_[i] = this.refineCounts;
            ctimes_[i] = this.ctimes;
            MpruneRatios_[i] = this.MpruneRatios;
            BallpruneRatios_[i] = this.BallpruneRatios;
            bNodeAccess_[i] = this.bNodeAccess;
            hbNodeAccess_[i] = this.hbNodeAccess;
            ubPruneRatio_[i] = this.ubPruneRatio;
            i += 1;
        }
        String info = "tbJoin_times: " + Arrays.deepToString(tbJoin_times_) + "\nkJoin_times: "
                + Arrays.deepToString(kJoin_times_) + "\nctimes: " + Arrays.deepToString(ctimes_) + "\nRefineCount: "
                + Arrays.deepToString(refineCounts_) + "\nPrune ratio: "
                + Arrays.toString(MpruneRatios_) + "," + Arrays.toString(BallpruneRatios_) + "\nAccess: "
                + Arrays.toString(bNodeAccess_) + "," + Arrays.toString(hbNodeAccess_) + "\nPruning Enhancement: "
                + Arrays.toString(ubPruneRatio_) + "\n";
        writeFile("Vary objDB", info);
        i = 0;

        for (int poiNB : Settings.poiNBs) {
            System.out.println("Varying poiNB = " + poiNB);
            this.vary(Settings.expNB, Settings.objNB, poiNB,
                    Settings.simThreshold, Settings.start,
                    Settings.end, Settings.k, Settings.intervalNum, false, "poiNB");
            tbJoin_times_[i] = this.tbJoin_times;
            kJoin_times_[i] = this.kJoin_times;
            refineCounts_[i] = this.refineCounts;
            ctimes_[i] = this.ctimes;
            MpruneRatios_[i] = this.MpruneRatios;
            BallpruneRatios_[i] = this.BallpruneRatios;
            bNodeAccess_[i] = this.bNodeAccess;
            hbNodeAccess_[i] = this.hbNodeAccess;
            ubPruneRatio_[i] = this.ubPruneRatio;
            i += 1;
        }
        info = "tbJoin_times: " + Arrays.deepToString(tbJoin_times_) + "\nkJoin_times: "
                + Arrays.deepToString(kJoin_times_) + "\nctimes: " + Arrays.deepToString(ctimes_) + "\nRefineCount: "
                + Arrays.deepToString(refineCounts_) + "\nPrune ratio: "
                + Arrays.toString(MpruneRatios_) + "," + Arrays.toString(BallpruneRatios_) + "\nAccess: "
                + Arrays.toString(bNodeAccess_) + "," + Arrays.toString(hbNodeAccess_) + "\nPruning Enhancement: "
                + Arrays.toString(ubPruneRatio_) + "\n";
        writeFile("Vary poiNB", info);
        i = 0;

        for (double simThreshold : new double[] { 0.3, 0.4, 0.5, 0.6, 0.7 }) {
            System.out.println("Varying simThreshold = " + simThreshold);
            this.vary(Settings.expNB, Settings.objNB, Settings.poiNB,
                    simThreshold, Settings.start,
                    Settings.end, Settings.k, Settings.intervalNum, false, "simThreshold");
            tbJoin_times_[i] = this.tbJoin_times;
            kJoin_times_[i] = this.kJoin_times;
            ctimes_[i] = this.ctimes;
            MpruneRatios_[i] = this.MpruneRatios;
            BallpruneRatios_[i] = this.BallpruneRatios;
            bNodeAccess_[i] = this.bNodeAccess;
            hbNodeAccess_[i] = this.hbNodeAccess;
            ubPruneRatio_[i] = this.ubPruneRatio;
            i += 1;
        }
        info = "tbJoin_times: " + Arrays.deepToString(tbJoin_times_) + "\nkJoin_times: "
                + Arrays.deepToString(kJoin_times_) + "\nctimes: " + Arrays.deepToString(ctimes_) + "\nRefineCount: "
                + Arrays.deepToString(refineCounts_) + "\nPrune ratio: "
                + Arrays.toString(MpruneRatios_) + "," + Arrays.toString(BallpruneRatios_) + "\nAccess: "
                + Arrays.toString(bNodeAccess_) + "," + Arrays.toString(hbNodeAccess_) + "\nPruning Enhancement: "
                + Arrays.toString(ubPruneRatio_) + "\n";
        writeFile("Vary simThreshold", info);
        i = 0;

        for (int k : new int[] { 200, 400, 600, 800, 1000 }) {
            System.out.println("Varying k = " + k);
            this.vary(Settings.expNB, Settings.objNB, Settings.poiNB,
                    Settings.simThreshold, Settings.start,
                    Settings.end, k, Settings.intervalNum, true, "K");
            tbJoin_times_[i] = this.tbJoin_times;
            kJoin_times_[i] = this.kJoin_times;
            ctimes_[i] = this.ctimes;
            MpruneRatios_[i] = this.MpruneRatios;
            BallpruneRatios_[i] = this.BallpruneRatios;
            bNodeAccess_[i] = this.bNodeAccess;
            hbNodeAccess_[i] = this.hbNodeAccess;
            ubPruneRatio_[i] = this.ubPruneRatio;
            i += 1;
        }
        info = "tbJoin_times: " + Arrays.deepToString(tbJoin_times_) + "\nkJoin_times: "
                + Arrays.deepToString(kJoin_times_) + "\nctimes: " + Arrays.deepToString(ctimes_) + "\nRefineCount: "
                + Arrays.deepToString(refineCounts_) + "\nPrune ratio: "
                + Arrays.toString(MpruneRatios_) + "," + Arrays.toString(BallpruneRatios_) + "\nAccess: "
                + Arrays.toString(bNodeAccess_) + "," + Arrays.toString(hbNodeAccess_) + "\nPruning Enhancement: "
                + Arrays.toString(ubPruneRatio_) + "\n";
        writeFile("Vary K", info);
        i = 0;

        for (int I : Settings.Is) {
            System.out.println("Varying I = " + I);
            Random r = new Random();
            int start = r.nextInt(Settings.tsNB - I);
            this.vary(Settings.expNB, Settings.objNB, Settings.poiNB,
                    Settings.simThreshold, start,
                    start + I, Settings.k, Settings.intervalNum, false, "I");
            tbJoin_times_[i] = this.tbJoin_times;
            kJoin_times_[i] = this.kJoin_times;
            refineCounts_[i] = this.refineCounts;
            ctimes_[i] = this.ctimes;
            MpruneRatios_[i] = this.MpruneRatios;
            BallpruneRatios_[i] = this.BallpruneRatios;
            bNodeAccess_[i] = this.bNodeAccess;
            hbNodeAccess_[i] = this.hbNodeAccess;
            ubPruneRatio_[i] = this.ubPruneRatio;
            i += 1;
        }
        info = "tbJoin_times: " + Arrays.deepToString(tbJoin_times_) + "\nkJoin_times: "
                + Arrays.deepToString(kJoin_times_) + "\nctimes: " + Arrays.deepToString(ctimes_) + "\nRefineCount: "
                + Arrays.deepToString(refineCounts_) + "\nPrune ratio: "
                + Arrays.toString(MpruneRatios_) + "," + Arrays.toString(BallpruneRatios_) + "\nAccess: "
                + Arrays.toString(bNodeAccess_) + "," + Arrays.toString(hbNodeAccess_) + "\nPruning Enhancement: "
                + Arrays.toString(ubPruneRatio_) + "\n";
        writeFile("Vary I", info);
        i = 0;

        for (int intervalNum : Settings.intervalNums) {
            System.out.println("Varying intervals = " + intervalNum);
            this.vary(Settings.expNB, Settings.objNB, Settings.poiNB,
                    Settings.simThreshold, Settings.start,
                    Settings.end, Settings.k, intervalNum, false, "intervals");
            tbJoin_times_[i] = this.tbJoin_times;
            kJoin_times_[i] = this.kJoin_times;
            refineCounts_[i] = this.refineCounts;
            ctimes_[i] = this.ctimes;
            MpruneRatios_[i] = this.MpruneRatios;
            BallpruneRatios_[i] = this.BallpruneRatios;
            bNodeAccess_[i] = this.bNodeAccess;
            hbNodeAccess_[i] = this.hbNodeAccess;
            ubPruneRatio_[i] = this.ubPruneRatio;
            i += 1;
        }
        info = "tbJoin_times: " + Arrays.deepToString(tbJoin_times_) + "\nkJoin_times: "
                + Arrays.deepToString(kJoin_times_) + "\nctimes: " + Arrays.deepToString(ctimes_) + "\nRefineCount: "
                + Arrays.deepToString(refineCounts_) + "\nPrune ratio: "
                + Arrays.toString(MpruneRatios_) + "," + Arrays.toString(BallpruneRatios_) + "\nAccess: "
                + Arrays.toString(bNodeAccess_) + "," + Arrays.toString(hbNodeAccess_) + "\nPruning Enhancement: "
                + Arrays.toString(ubPruneRatio_) + "\n";
        writeFile("Vary intervals", info);
        i = 0;
    }

    public static void main(String[] args) {
        Test test = new Test();
        // test.singleTest();
        test.multiTest();
    }
}
