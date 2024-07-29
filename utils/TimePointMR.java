package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
            System.out.println("No Interseection!");
            return;
        }
    }

    // The MBR of the time-point motion range
    public double[] getMBR() {
        double mbrMinX = Math.max(Ax - r1, Bx - r2);
        double mbrMinY = Math.max(Ay - r1, By - r2);
        double mbrMaxX = Math.min(Ax + r1, Bx + r2);
        double mbrMaxY = Math.min(Ay + r1, By + r2);
        return new double[] { mbrMinX, mbrMaxX, mbrMinY, mbrMaxY };
    }

    // check if these two circles intersect or not
    // calculate the area of this time-point motion range
    public double getArea() {
        if (distance >= r1 + r2)
            return 0;
        double angle1 = 2 * Math.acos((r1 * r1 + distance * distance - r2 * r2) / (2
                * r1 * distance));
        double angle2 = 2 * Math.acos((r2 * r2 + distance * distance - r1 * r1) / (2
                * r2 * distance));
        double sectorArea1 = 0.5 * r1 * r1 * angle1;
        double sectorArea2 = 0.5 * r2 * r2 * angle2;
        return sectorArea1 + sectorArea2 - 0.5 * r1 * r1 * Math.sin(angle1) - 0.5 *
                r2 * r2 * Math.sin(angle2);
    }

    // get a given number of point samples within this time-point motion ranges with
    // uniform sampling
    private List<Point> getUniformSamplingPoints(int numSamples) {
        double theta = Math.atan2(By - Ay, Bx - Ax);
        double phi = Math.acos((r1 * r1 + distance * distance - r2 * r2) / (2 * r1 * distance));
        double intersectionAngle1 = theta + phi;
        double intersectionAngle2 = theta - phi;
        double angleStep = (intersectionAngle2 - intersectionAngle1) / numSamples;
        List<Point> samples = new ArrayList<>();
        for (int i = 0; i < numSamples; i++) {
            double angle = intersectionAngle1 + i * angleStep;
            double radius = r1 + (r2 - r1) * i / numSamples;
            double sampleX = Ax + radius * Math.cos(angle);
            double sampleY = Ay + radius * Math.sin(angle);
            samples.add(new Point(sampleX, sampleY));
        }
        return samples;
    }

    // calculate intersection area to another MR's MBR
    public double interAreaTo(TimePointMR that) {
        double[] thisMBR = this.getMBR();
        double[] thatMBR = that.getMBR();
        // Calculate the coordinates of the intersection rectangle
        double x1 = Math.max(thisMBR[0], thatMBR[0]);
        double y1 = Math.max(thisMBR[2], thatMBR[2]);
        double x2 = Math.min(thisMBR[1], thatMBR[1]);
        double y2 = Math.min(thisMBR[3], thatMBR[3]);
        // Calculate the area of the intersection rectangle
        double intersectionArea = (x2 - x1) * (y2 - y1);
        // Check if there is no intersection
        if (intersectionArea < 0) {
            intersectionArea = 0;
        }
        return intersectionArea;
    }

    // check if the smaple point is in the time-point motion range
    private boolean isInsideOverlapArea(double x, double y) {
        double distanceA = Math.sqrt((x - Ax) * (x - Ax) + (y - Ay) * (y - Ay));
        double distanceB = Math.sqrt((x - Bx) * (x - Bx) + (y - By) * (y - By));
        return distanceA <= r1 && distanceB <= r2;
    }

    // calculate the intersection similarity to another
    public double simTo(TimePointMR that, int numSamples) {
        double thisArea = this.getArea();
        double thatArea = that.getArea();
        int sampleNumOfThis = (int) (thisArea * numSamples / (thisArea + thatArea));
        int sampleNumOfThat = (int) (thatArea * numSamples / (thisArea + thatArea));
        List<Point> pointOfThis = this.getUniformSamplingPoints(sampleNumOfThis);
        List<Point> pointOfThat = that.getUniformSamplingPoints(sampleNumOfThat);
        double n1 = 0;
        double n2 = 0;
        for (Point p : pointOfThis) {
            if (that.isInsideOverlapArea(p.x, p.y)) {
                n1++;
            }
        }
        for (Point p : pointOfThat) {
            if (this.isInsideOverlapArea(p.x, p.y)) {
                n2++;
            }
        }
        return (n1 + n2) / numSamples;
    }

    // test
    public static void main(String[] args) {
        int numSamples = 10;
        double Ax = 1.0;
        double Ay = 2.0;
        double r1 = 10.0;

        double Bx = 5.0;
        double By = 8.0;
        double r2 = 5.0;
        TimePointMR tmR = new TimePointMR(Ax, Ay, Bx, By, r1, r2);

        // test MBR
        double[] mbr = tmR.getMBR();
        System.out.println(Arrays.toString(mbr));
        // test area
        double area = tmR.getArea();
        System.out.println(area);
        // test sample
        List<Point> samples = tmR.getUniformSamplingPoints(numSamples);
        System.out.println(samples.size());
        for (Point p : samples) {
            System.out.println(p.x);
        }
    }

}
