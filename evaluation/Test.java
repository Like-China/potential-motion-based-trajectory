package evaluation;

import java.util.ArrayList;
import java.util.HashMap;

import utils.TimeIntervalMR;
import utils.Trajectory;

public class Test {
    public static void main(String[] args) {
        Loader l = new Loader();
        int n = 20000;
        double simThreshold = 0.01;
        l.getTrajectoryData(n);
        l.getQueryDB(n / 2);
        // for (Trajectory t : l.trjs) {
        // System.out.println(t);
        // }

        int i = 0;
        int matchNB = 0;
        for (Trajectory t1 : l.queries) {
            i++;
            if (i % 500 == 0) {
                System.out.println(i + "/" + n / 2);
            }
            for (Trajectory t2 : l.db) {
                double sim = t1.simTo(t2);
                if (sim > simThreshold) {
                    matchNB++;
                }
            }
        }
        System.out.println("Match Number: " + matchNB);

        // construct index then pruning
        HashMap<Integer, ArrayList<TimeIntervalMR>> time2TimeIntervalMR = new HashMap<>();
        for (i = 0; i < 10; i++) {
            ArrayList<TimeIntervalMR> mr = new ArrayList<>();
            for (Trajectory t2 : l.db) {
                mr.add(t2.EllipseSeq.get(i));
            }
            
        }
        BJAlg bj = new BJAlg(, n, i)

    }
}
