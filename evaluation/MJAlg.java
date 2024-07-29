package evaluation;

import java.util.ArrayList;

import mtree.MTreeClass;
import utils.ContactPair;
import utils.Data;

public class MJAlg {
    // query, database set at each timestamp, we update them at each timestampe
    public ArrayList<Data> queries = new ArrayList<>();
    public ArrayList<Data> db = new ArrayList<>();
    // index construction time / filtering time
    public long cTime = 0;
    public long fTime = 0;

    public MJAlg(ArrayList<Data> queries, ArrayList<Data> db) {
        this.queries = queries;
        this.db = db;
    }

    /**
     * conduct MJ-Alg method to obtain all candidate pairs
     * 
     * @return all candidate pairs
     */
    public ArrayList<ContactPair> getCandidate() {
        long t1 = System.currentTimeMillis();
        MTreeClass mtree = new MTreeClass();
        for (Data data : db) {
            mtree.add(data);
            // System.out.println(data.radius);
        }
        long t2 = System.currentTimeMillis();
        cTime = (t2 - t1);

        t1 = System.currentTimeMillis();
        ArrayList<ContactPair> candidates = new ArrayList<>();
        for (Data qdata : queries) {
            MTreeClass.Query query = mtree.getNearestByRange(qdata, qdata.radius);
            candidates.addAll(query.rangeQuery());
        }
        t2 = System.currentTimeMillis();
        fTime = (t2 - t1);
        return candidates;
    }
}