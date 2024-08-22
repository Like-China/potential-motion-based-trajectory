package utils;

import java.util.ArrayList;
import java.util.HashMap;

import evaluation.Settings;

public class Trajectory {

    public int objectID;
    public double minSpeed;
    public double maxSpeed;
    public int sampleSize;
    public ArrayList<Location> locationSeq;
    public ArrayList<TimeIntervalMR> EllipseSeq;
    public ArrayList<Data> DataSeq;
    // the intersected MR at each timestamp
    public HashMap<Integer, ArrayList<TimeIntervalMR>> ts2candidate = new HashMap<>();

    public Trajectory(int objID, ArrayList<Location> locationSeq) {
        EllipseSeq = new ArrayList<>();
        DataSeq = new ArrayList<>();
        this.objectID = objID;
        this.locationSeq = locationSeq;
        this.sampleSize = locationSeq.size();
        // generate time-interval motion ranges and Data class
        trj2EllipseSeq();
    }

    public void trj2EllipseSeq() {
        // use locationSeq to form ellpises
        maxSpeed = 0;
        minSpeed = Double.MAX_VALUE;
        // get minimum/maximum speed
        for (int i = 0; i < this.sampleSize - 1; i++) {
            Location cur = locationSeq.get(i);
            Location next = locationSeq.get(i + 1);

            double speed = cur.distTo(next) / (next.timestamp - cur.timestamp);
            minSpeed = minSpeed < speed ? minSpeed : speed;
            maxSpeed = maxSpeed > speed ? maxSpeed : speed;
        }
        // generate ellipse and data
        maxSpeed *= 1.01;
        if (isDelete()) {
            // System.out.println("Exceed Max Speed: " + maxSpeed);
            return;
        }
        for (int i = 0; i < this.sampleSize - 1; i++) {
            Location cur = locationSeq.get(i);
            Location next = locationSeq.get(i + 1);
            TimeIntervalMR bead = new TimeIntervalMR(cur, next, maxSpeed);
            Data data = new Data(bead);
            EllipseSeq.add(bead);
            DataSeq.add(data);
        }
    }

    // self check
    public boolean isDelete() {
        if (this.sampleSize < Settings.tsNB) {
            return true;
        }
        // remove static or abnormal objects
        // a == 0 the timestampe does not change
        if (maxSpeed >= 0.03) {
            // System.out.println("Exceed Max Speed: " + maxSpeed);
            return true;
        }
        if (minSpeed <= 0) {
            // System.out.println("Low Min Speed: " + minSpeed);
            return true;
        }
        if (Double.isNaN(maxSpeed)) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return String.format("%d@loc size: %d\t min speed: %.3f\t max speed: %.3f", this.objectID,
                this.locationSeq.size(), this.minSpeed, this.maxSpeed);
    }

    // the trajectory similairity from this to another
    public double sampleLocsimTo(Trajectory that) {
        assert this.sampleSize == Settings.tsNB;
        double similarity = 0;
        double dist = 0;
        for (int i = 0; i < Settings.tsNB; i++) {
            Location thisLocation = this.locationSeq.get(i);
            Location thatLocation = that.locationSeq.get(i);
            dist = thisLocation.distTo(thatLocation);
            similarity += Math.exp(-dist);
        }
        return similarity / Settings.tsNB;
    }

    // the trajectory similairity from this to another
    public double simTo(Trajectory that, double sampleSim) {
        int trjLen = Settings.tsNB;
        double similarity = sampleSim * trjLen;
        // Motion ranges' POI similarity
        for (int i = 0; i < trjLen - 1; i++) {
            TimeIntervalMR thisMR = this.EllipseSeq.get(i);
            TimeIntervalMR thatMR = that.EllipseSeq.get(i);
            ArrayList<Point[]> thisPoints = thisMR.POIs;
            ArrayList<Point[]> thatPoints = thatMR.POIs;
            assert thisPoints.size() == Settings.intervalNum || thisPoints.size() == 1;
            for (int j = 0; j < thisPoints.size(); j++) {
                double minDist = Double.MAX_VALUE;
                for (Point A : thisPoints.get(j)) {
                    for (Point B : thatPoints.get(j)) {
                        double dist = Math.sqrt(Math.pow(A.x - B.x, 2) + Math.pow(A.y - B.y, 2));
                        minDist = minDist > dist ? dist : minDist;
                    }
                }
                similarity += Math.exp(-minDist);
            }
        }
        return similarity / (trjLen + (trjLen - 1) * Settings.intervalNum);
    }

    public double upperBoundTo(Trajectory that) {
        int trjLen = Settings.tsNB;
        double similarity = 0;
        // Motion ranges' POI similarity
        double dist = 0;
        for (int ts = 0; ts < trjLen - 1; ts++) {
            TimeIntervalMR thisMR = this.EllipseSeq.get(ts);
            TimeIntervalMR thatMR = that.EllipseSeq.get(ts);
            if (ts2candidate.get(ts).contains(thatMR)) {
                dist = 0;
            } else {
                dist = Math.sqrt(Math.pow(thisMR.center[0] - thatMR.center[0], 2)
                        + Math.pow(thisMR.center[1] - thatMR.center[1], 2)) - thisMR.a - thatMR.a;
                assert dist > 0;
            }
            similarity += (Math.exp(-dist) * (Settings.intervalNum + 1));
        }
        similarity += Math.exp(-dist);
        return similarity / (trjLen + (trjLen - 1) * Settings.intervalNum);
    }
}
