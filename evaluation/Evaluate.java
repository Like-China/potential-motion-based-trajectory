package evaluation;

import java.util.ArrayList;
import java.util.Arrays;
import utils.ContactPair;
import utils.Utils;

// evaulate the algorithm performance with varied parameters: the maximum speed, cardinality, similarity threshold, etc.
public class Evaluate {

        /**
         * conduct an evaluation with given parameter
         * 
         * @param maxSpeed         the times of the maximum speed to the averaged speed
         * @param cardinality      the size of query/database
         * @param minLeafNB        the node size in the balltree
         * @param interRatio       the intersection similairity threshold
         * @param repartitionRatio the repartition threshold
         * @param sampleNum        the number of sampling points during refining
         *                         precedure
         * @param useBF            evaulate the performance of BFAlg or not
         * @param useMJ            evaulate the performance of MJAlg or not
         * @param useBall          evaulate the performance of BJAlgNoRepartition or not
         * @param intervalNum      the number of evaluated time intervals
         * @return the detailed time cost of the four algorithms in the form of
         *         Long[6][4]
         *         long[0][]: { bruteCTime, mCTime, ballCTime, ternaryCTime }
         *         long[1][]: { bruteFTime, mFTime, ballFTime, ternaryFTime }
         *         long[2][]: { bruteCTime + bruteFTime, mFTime + mCTime, ballFTime +
         *         ballCTime,
         *         ternaryFTime + ternaryCTime }
         *         long[3][]: { basicRTime, basicRTime, advanceRTime, advanceRTime }
         *         long[4][]: {bruteCTime + bruteFTime + basicRTime, mCTime + mFTime +
         *         basicRTime,
         *         ballCTime + ballFTime + advanceRTime, ternaryCTime + ternaryFTime +
         *         advanceRTime)}
         *         long[5][]: {searchCountSumOfBall, searchCountSumOfTernary, 0, 0}
         */
        public static Long[][] evaluate(double maxSpeed, int cardinality, int minLeafNB, double interRatio,
                        double repartitionRatio, int sampleNum, boolean useBF, boolean useMJ, boolean useBall,
                        int intervalNum) {

                Long[][] res = new Long[6][4];
                Loader l = new Loader();
                long t1 = 0, t2 = 0;
                long bruteCTime = 0, ballCTime = 0, mCTime = 0, ternaryCTime = 0;
                long bruteFTime = 0, ballFTime = 0, mFTime = 0, ternaryFTime = 0;
                // advanceRTime the the refinement time with pre-checking
                long basicRTime = 0, advanceRTime = 0;
                // the number of independent experiments
                int expNum = 20;
                // the number of node accesses
                int searchCountSumOfBall = 0;
                int searchCountSumOfTernary = 0;
                l.getAllData(Settings.objectNB, maxSpeed);
                for (int i = 0; i < expNum; i++) {
                        long loopStart = System.currentTimeMillis();
                        System.out.println("\t Round: " + (i + 1));
                        l.getBatch(cardinality);
                        // For some experiments, we do not evaluate the performance of BF-Alg, thus a
                        // boolean useBF is used
                        if (useBF) {
                                BFAlg brute = new BFAlg(l.queries, l.db);
                                brute.getCandidate();
                                bruteFTime += brute.fTime;
                        }
                        if (useMJ) {
                                MJAlg m = new MJAlg(l.queries, l.db);
                                m.getCandidate();
                                mCTime += m.cTime;
                                mFTime += m.fTime;
                        }

                        ArrayList<ContactPair> ballCandidate = new ArrayList<>();
                        if (useBall) {
                                BJAlgNoRepartition ball = new BJAlgNoRepartition(l.queries, l.db, minLeafNB);
                                ballCandidate = ball.getCandidate();
                                searchCountSumOfBall += ball.searchCount;
                                ballCTime += ball.cTime;
                                ballFTime += ball.fTime;
                                ball = null;
                                ballCandidate = null;
                                System.gc();
                        }

                        BJAlg ternaryBall = new BJAlg(l.queries, l.db,
                                        repartitionRatio, minLeafNB);
                        ballCandidate = ternaryBall.getCandidate();
                        searchCountSumOfTernary += ternaryBall.searchCount;
                        ternaryCTime += ternaryBall.cTime;
                        ternaryFTime += ternaryBall.fTime;
                        ternaryBall = null;

                        t1 = System.currentTimeMillis();
                        Refine.monitor(ballCandidate, interRatio, false, sampleNum, intervalNum);
                        t2 = System.currentTimeMillis();
                        basicRTime += (t2 - t1);
                        t1 = System.currentTimeMillis();
                        Refine.monitor(ballCandidate, interRatio, true, sampleNum, intervalNum);
                        t2 = System.currentTimeMillis();
                        advanceRTime += (t2 - t1);
                        long loopEnd = System.currentTimeMillis();
                        System.out.println("\t Round Time Cost: " + (loopEnd - loopStart));
                }
                searchCountSumOfBall /= expNum;
                searchCountSumOfTernary /= expNum;
                bruteFTime /= expNum;
                mCTime /= expNum;
                mFTime /= expNum;
                ballCTime /= expNum;
                ballFTime /= expNum;
                ternaryCTime /= expNum;
                ternaryFTime /= expNum;
                basicRTime /= expNum;
                advanceRTime /= expNum;
                String conTime = String.format(
                                "***Construction Time***\nBrute: %8d MTree: %8d Ball: %8d Ternary: %8d\n", bruteCTime,
                                mCTime, ballCTime, ternaryCTime);
                res[0] = new Long[] { bruteCTime, mCTime, ballCTime, ternaryCTime };
                System.out.println(conTime);
                String filterTime = String.format("***Filter Time***\nBrute: %8d MTree: %8d Ball: %8d Ternary: %8d\n",
                                bruteFTime, mFTime, ballFTime, ternaryFTime);
                res[1] = new Long[] { bruteFTime, mFTime, ballFTime, ternaryFTime };
                System.out.println(filterTime);
                String conTimePlusfilterTime = String.format(
                                "***Construction && Filter Time***\nBrute: %8d MTree: %8d Ball: %8d Ternary: %8d\n",
                                bruteCTime + bruteFTime, mFTime + mCTime, ballFTime + ballCTime,
                                ternaryFTime + ternaryCTime);
                res[2] = new Long[] { bruteCTime + bruteFTime, mFTime + mCTime, ballFTime + ballCTime,
                                ternaryFTime + ternaryCTime };
                System.out.println(conTimePlusfilterTime);
                String refineTime = String.format("***Refine Time***\nBrute: %8d MTree: %8d Ball: %8d Ternary: %8d\n",
                                basicRTime, basicRTime, advanceRTime, advanceRTime);
                res[3] = new Long[] { basicRTime, basicRTime, advanceRTime, advanceRTime };
                System.out.println(refineTime);
                String totalTime = String.format("***Total Time***\nBrute: %8d MTree: %8d Ball: %8d Ternary: %8d\n",
                                bruteCTime + bruteFTime + basicRTime, mCTime + mFTime + basicRTime,
                                ballCTime + ballFTime + advanceRTime, ternaryCTime + ternaryFTime + advanceRTime);
                res[4] = new Long[] { bruteCTime + bruteFTime + basicRTime, mCTime + mFTime + basicRTime,
                                ballCTime + ballFTime + advanceRTime, ternaryCTime + ternaryFTime + advanceRTime };
                System.out.println(totalTime);
                String nodeAccess = String.format("***Node Access***\nBall: %8d Ternary: %8d\n", searchCountSumOfBall,
                                searchCountSumOfTernary);
                res[5] = new Long[] { (long) searchCountSumOfBall, (long) searchCountSumOfTernary, 0l, 0l };
                System.out.println(nodeAccess);
                String otherInfo = conTime + filterTime + conTimePlusfilterTime + refineTime + totalTime + nodeAccess;
                String setInfo = String.format(
                                "maxSpeed=%f, cardinality=%d, minLeafNB=%d, interRatio=%f, repartitionRatio=%f",
                                maxSpeed, cardinality, minLeafNB, interRatio, repartitionRatio);
                Utils.writeFile(setInfo, otherInfo);
                return res;
        }

        public static void varyTest() {
                long t1 = System.currentTimeMillis();
                ArrayList<Long[][]> allRes = new ArrayList<>();
                Long[][] res;

                // vary speed

                Utils.writeFile("", "Varying speed");
                for (double maxSpeed : Settings.maxSpeeds) {
                        System.out.println("Vary speed");
                        res = evaluate(maxSpeed, Settings.cardinality, Settings.minLeafNB,
                                        Settings.interRatio,
                                        Settings.repartitionRatio, Settings.sampleNum, false, true, true,
                                        Settings.intervalNum);
                        allRes.add(res);
                }
                for (Long[][] item : allRes) {
                        Utils.writeFile("speed", Arrays.deepToString(item));
                }
                allRes = new ArrayList<>();

                // vary cardinality

                Utils.writeFile("", "Varying cardinality");
                // for (int cardinality: Settings.cardinalities) {
                for (int cardinality : new int[] { 23000, 25000, 30000, 35000, 41000 }) {
                        System.out.println("Vary cardinality");
                        res = evaluate(Settings.maxSpeed, cardinality, Settings.minLeafNB,
                                        Settings.interRatio, Settings.repartitionRatio, Settings.sampleNum, true, true,
                                        true,
                                        Settings.intervalNum);
                        allRes.add(res);
                }
                for (Long[][] item : allRes) {
                        Utils.writeFile("cardinality", Arrays.deepToString(item));
                }
                allRes = new ArrayList<>();

                // vary interRatio

                Utils.writeFile("", "Varying interRatio");
                for (double interRatio : Settings.interRatios) {
                        System.out.println("Vary interRatio");
                        res = evaluate(Settings.maxSpeed, Settings.cardinality, Settings.minLeafNB,
                                        interRatio,
                                        Settings.repartitionRatio, Settings.sampleNum, false, false,
                                        false, Settings.intervalNum);
                        allRes.add(res);
                }
                for (Long[][] item : allRes) {
                        Utils.writeFile("interRatio", Arrays.deepToString(item));
                }
                allRes = new ArrayList<>();

                // vary repartition ratio (intersection similairity)

                Utils.writeFile("", "Varying repartition ratio");
                for (double repartitionRatio : Settings.repartitionRatios) {
                        System.out.println("Vary repartition ratio");
                        res = evaluate(Settings.maxSpeed, Settings.cardinality, Settings.minLeafNB,
                                        Settings.interRatio, repartitionRatio, Settings.sampleNum, false, false,
                                        false, Settings.intervalNum);
                        allRes.add(res);
                }
                for (Long[][] item : allRes) {
                        Utils.writeFile("repartition ratio", Arrays.deepToString(item));
                }
                allRes = new ArrayList<>();

                // // vary minLeaf

                Utils.writeFile("", "Varying minLeaf");
                for (int minLeafNB : new int[] { 20, 30, 40, 50, 60 }) {
                        System.out.println("Vary minLeaf");
                        res = evaluate(Settings.maxSpeed, Settings.cardinality, minLeafNB,
                                        Settings.interRatio, Settings.repartitionRatio, Settings.sampleNum, false,
                                        false, true, Settings.intervalNum);
                        allRes.add(res);
                }
                for (Long[][] item : allRes) {
                        Utils.writeFile("minLeaf", Arrays.deepToString(item));
                }
                allRes = new ArrayList<>();

                long t2 = System.currentTimeMillis();
                System.out.println("All Evaluation Time Cost: " + (t2 - t1) / 1000);
        }

        public static void main(String[] args) {
                varyTest();
        }
}
