package evaluation;

import java.util.ArrayList;
import utils.ContactPair;
import utils.Data;

public class BFAlg {
    // query, database set at each timestamp, we update them at each timestampe
    public ArrayList<Data> queries = new ArrayList<>();
    public ArrayList<Data> db = new ArrayList<>();
    public long fTime = 0;

    public BFAlg(ArrayList<Data> queries, ArrayList<Data> db) {
        this.queries = queries;
        this.db = db;
    }

    /**
     * conduct brute-force method to obtain all candidate pairs
     * 
     * @return all candidate pairs
     */
    public ArrayList<ContactPair> getCandidate() {
        long t1 = System.currentTimeMillis();
        ArrayList<ContactPair> candidates = new ArrayList<>();
        // bruteforce
        for (Data qdata : queries) {
            for (Data dbdata : db) {
                // pre-checking
                if (Math.abs(qdata.get(0) - dbdata.get(0)) > (qdata.bead.a + dbdata.bead.a)
                        && Math.abs(qdata.get(1) - dbdata.get(1)) > (qdata.bead.b + dbdata.bead.b)) {
                    continue;
                }
                double centerDist = Math
                        .sqrt(Math.pow(qdata.get(0) - dbdata.get(0), 2) + Math.pow(qdata.get(1) - dbdata.get(1), 2));
                if (centerDist <= (qdata.radius + dbdata.radius)) {
                    candidates.add(new ContactPair(qdata, dbdata, centerDist));
                }
            }
        }
        long t2 = System.currentTimeMillis();
        fTime = t2 - t1;
        return candidates;
    }

}