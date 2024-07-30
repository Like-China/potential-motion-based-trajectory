package utils;

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

    // get a given number of point samples within this time-point motion ranges with
    // uniform sampling
    public Point[] getUniformSamplingPoints(int numSamples) {
        double theta = Math.atan2(By - Ay, Bx - Ax);
        double phi = Math.acos((r1 * r1 + distance * distance - r2 * r2) / (2 * r1 * distance));
        double intersectionAngle1 = theta + phi;
        double intersectionAngle2 = theta - phi;
        double angleStep = (intersectionAngle2 - intersectionAngle1) / numSamples;
        Point[] samples = new Point[numSamples];
        for (int i = 0; i < numSamples; i++) {
            double angle = intersectionAngle1 + i * angleStep;
            double radius = r1 + (r2 - r1) * i / numSamples;
            double sampleX = Ax + radius * Math.cos(angle);
            double sampleY = Ay + radius * Math.sin(angle);
            samples[i] = new Point(sampleX, sampleY);
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
