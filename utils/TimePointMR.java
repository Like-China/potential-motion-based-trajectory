package utils;

import java.util.Random;

public class TimePointMR {
    // The time-point motion range is the intersection region of two circles
    // here, we record the centers and radius of these two circles
    double Ax, Ay;
    double Bx, By;
    double r1, r2;
    double distance;

    public TimePointMR(double Ax, double Ay, double Bx, double By, double r1, double r2) {
        this.Ax = Ax;
        this.Ay = Ay;
        this.Bx = Bx;
        this.By = By;
        this.r1 = r1;
        this.r2 = r2;
        distance = Math.sqrt((Ax - Bx) * (Ax - Bx) + (Ay - By) * (Ay - By));
        if (distance >= r1 + r2) {
            System.out.println(Ax + "/" + Bx + "/" + Ay + "/" + By + "/" + r1 + "/" + r2);
            System.out.println("No Interseection!");
            return;
        }
    }

    // get a given number of point samples within this time-point motion ranges with
    // uniform sampling
    public Point[] getUniformSamplingPoints(int numSamples) {
        Point[] samples = new Point[numSamples];
        int i = 0;
        Random r = new Random(10);
        // The MBR of the time-point motion range
        double mbrMinX = Math.max(Ax - r1, Bx - r2);
        double mbrMinY = Math.max(Ay - r1, By - r2);
        double mbrMaxX = Math.min(Ax + r1, Bx + r2);
        double mbrMaxY = Math.min(Ay + r1, By + r2);
        while (i < 10) {
            double randomX = mbrMinX + ((mbrMaxX - mbrMinX) * r.nextDouble());
            double randomY = mbrMinY + ((mbrMaxY - mbrMinY) * r.nextDouble());
            // check if the point in the time-interval Ellipse
            double d1 = Math.sqrt((randomX - Ax) * (randomX - Ax) + (randomY - Ay) * (randomY - Ay));
            double d2 = Math.sqrt((randomX - Bx) * (randomX - Bx) + (randomY - By) * (randomY - By));
            if (d1 + d2 <= (r1 + r2)) {
                samples[i++] = new Point(randomX, randomY);
            }
        }
        return samples;
    }

    // check if the smaple point is in the time-point motion range
    private boolean isInsideOverlapArea(double x, double y) {
        double distanceA = Math.sqrt((x - Ax) * (x - Ax) + (y - Ay) * (y - Ay));
        double distanceB = Math.sqrt((x - Bx) * (x - Bx) + (y - By) * (y - By));
        return distanceA <= r1 && distanceB <= r2;
    }

}
