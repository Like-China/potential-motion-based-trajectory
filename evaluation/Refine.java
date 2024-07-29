package evaluation;

import java.util.ArrayList;

import utils.ContactPair;
import utils.Data;
import utils.Ellipse;
import utils.TimePointMR;

public class Refine {

    // further refine results
    public static ArrayList<ContactPair> monitor(ArrayList<ContactPair> candidates, double similarityThreshold,
            boolean isPrecheck, int sampleNum, int intervalNum) {
        ArrayList<ContactPair> refinedResult = new ArrayList<>();
        for (ContactPair pair : candidates) {
            // check if the intersection similarity of pairs in candidate is at least the
            // given similairty threshold
            if (isContact(pair.query, pair.db, similarityThreshold, isPrecheck, sampleNum, intervalNum)) {
                refinedResult.add(pair);
            }
        }
        // System.out.println("candidate size / match size: " + candidates.size() + "/"
        // + refinedResult.size());
        return refinedResult;
    }

    public static boolean isContact(Data qData, Data dbData, double similarityThreshold, boolean isPrecheck,
            int sampleNum, int intervalNum) {
        Ellipse qE = qData.bead;
        Ellipse dbE = dbData.bead;

        // the maximum intersection similarity among all time-points
        double maxSim = 0;
        for (int i = 1; i < intervalNum - 1; i++) {
            // get time-point ranges of the two objects at several time points
            double Ax = qE.curLocation.x;
            double Ay = qE.curLocation.y;
            double Bx = qE.nextLocation.x;
            double By = qE.nextLocation.y;
            double r1 = qE.maxSpeed * (qE.nextLocation.timestamp - qE.curLocation.timestamp) * i / intervalNum;
            double r2 = qE.maxSpeed * (qE.nextLocation.timestamp - qE.curLocation.timestamp) * (intervalNum - i)
                    / intervalNum;
            TimePointMR qMR = new TimePointMR(Ax, Ay, Bx, By, r1, r2);
            Ax = dbE.curLocation.x;
            Ay = dbE.curLocation.y;
            Bx = dbE.nextLocation.x;
            By = dbE.nextLocation.y;
            r1 = dbE.maxSpeed * (dbE.nextLocation.timestamp - dbE.curLocation.timestamp) * i / intervalNum;
            r2 = dbE.maxSpeed * (dbE.nextLocation.timestamp - dbE.curLocation.timestamp) * (intervalNum - i)
                    / intervalNum;
            TimePointMR dbEMR = new TimePointMR(Ax, Ay, Bx, By, r1, r2);

            // pre-checking
            // motion ranges
            if (isPrecheck) {
                double interArea = qMR.interAreaTo(dbEMR);
                double upper = 2 * interArea / (qMR.getArea() + dbEMR.getArea());
                if (upper < similarityThreshold) {
                    continue;
                }
            }
            // calculate the intersection simlarity using the uniform sampling methods
            double sim = qMR.simTo(dbEMR, sampleNum);
            maxSim = sim > maxSim ? sim : maxSim;
            if (maxSim >= similarityThreshold) {
                return true;
            }

        }

        return false;

    }

}
