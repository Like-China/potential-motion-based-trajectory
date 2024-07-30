package utils;

import java.util.ArrayList;

import evaluation.Settings;

public class Trajectory {

    public int objectID;
    public double minSpeed;
    public double maxSpeed;
    public int sampleSize;
    public ArrayList<Location> locationSeq;
    public ArrayList<TimeIntervalMR> EllipseSeq;
    public ArrayList<Data> DataSeq;

    public Trajectory(int objID, ArrayList<Location> locationSeq) {
        EllipseSeq = new ArrayList<>();
        DataSeq = new ArrayList<>();
        this.objectID = objID;
        this.locationSeq = locationSeq;
        this.sampleSize = locationSeq.size();
        // generate time-interval notion ranges and Data class
        trj2EllipseSeq();
    }

    public void trj2EllipseSeq() {
        // use locationSeq to form ellpises
        maxSpeed = 0;
        minSpeed = Double.MAX_VALUE;
        // get maximum speed
        for (int i = 0; i < this.sampleSize - 1; i++) {
            Location cur = locationSeq.get(i);
            Location next = locationSeq.get(i + 1);
            double speed = cur.distTo(next) / (next.timestamp - cur.timestamp);
            // System.out.println(cur.distTo(next) + "/"
            // + cur.distance(cur.latitude, cur.longititude, next.latitude,
            // next.longititude));
            minSpeed = minSpeed < speed ? minSpeed : speed;
            maxSpeed = maxSpeed > speed ? maxSpeed : speed;
        }
        // generate ellipse and data
        maxSpeed *= 1.1;
        if (maxSpeed >= 100) {
            // System.out.println("Exceed Max Speed: " + maxSpeed);
            return;
        }
        if (minSpeed <= 0) {
            // System.out.println("Low Min Speed: " + minSpeed);
            return;
        }
        for (int i = 0; i < this.sampleSize - 1; i++) {
            Location cur = locationSeq.get(i);
            Location next = locationSeq.get(i + 1);
            TimeIntervalMR bead = new TimeIntervalMR(cur, next, maxSpeed);
            // System.out.println(bead);
            // System.out.println();
            Data data = new Data(bead);
            EllipseSeq.add(bead);
            DataSeq.add(data);
        }
    }

    // self check
    public boolean isDelete() {
        if (this.sampleSize <= Settings.tsNB) {
            return true;
        }
        // remove static or abnormal objects
        // a == 0 the timestampe does not change
        if (maxSpeed >= 100) {
            // System.out.println("Exceed Max Speed: " + maxSpeed);
            return true;
        }
        if (minSpeed <= 0) {
            // System.out.println("Low Min Speed: " + minSpeed);
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
    public double simTo(Trajectory that) {
        int trjLen = (this.sampleSize > that.sampleSize) ? that.sampleSize : this.sampleSize;
        double similarity = 0;
        for (int i = 0; i < trjLen; i++) {
            Location thisLocation = this.locationSeq.get(i);
            Location thatLocation = that.locationSeq.get(i);
            double dist = thisLocation.distTo(thatLocation);
            similarity += Math.exp(-dist);
        }
        return similarity / trjLen;
    }
}
