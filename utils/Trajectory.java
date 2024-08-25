package utils;

import java.util.ArrayList;
import java.util.HashMap;

import evaluation.Settings;
import poi.QuadTree;

public class Trajectory {

    public int objectID;
    public double minSpeed;
    public double maxSpeed;
    public int sampleSize;
    public ArrayList<Location> locationSeq;
    // public ArrayList<TimeIntervalMR> EllipseSeq;
    // public ArrayList<Data> DataSeq;
    // the intersected MR at each timestamp
    // public HashMap<Integer, ArrayList<TimeIntervalMR>> ts2candidate = new
    // HashMap<>();

    public Trajectory(int objID, ArrayList<Location> locationSeq) {
        // EllipseSeq = new ArrayList<>();
        // DataSeq = new ArrayList<>();
        this.objectID = objID;
        this.locationSeq = locationSeq;
        this.sampleSize = locationSeq.size();
        // generate time-interval motion ranges and Data class
        trj2EllipseSeq();
    }

    // Given a time period [i,j], generate MR(o,I), I=[i,j]
    public TimeIntervalMR getIntervalMR(int i, int j, QuadTree qTree) {
        Location cur = locationSeq.get(i);
        Location next = locationSeq.get(j);
        TimeIntervalMR bead = new TimeIntervalMR(cur, next, maxSpeed, qTree);
        return bead;
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
        // if (isDelete()) {
        // System.out.println("Exceed Max Speed: " + maxSpeed);
        // return;
        // }
        // for (int i = 0; i < this.sampleSize - 1; i++) {
        // TimeIntervalMR bead = getIntervalMR(i, i+1);
        // Data data = new Data(bead);
        // EllipseSeq.add(bead);
        // DataSeq.add(data);
        // }
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

}
