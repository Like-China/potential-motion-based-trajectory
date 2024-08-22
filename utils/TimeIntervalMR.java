package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import evaluation.Settings;
import poi.QuadTree;

public class TimeIntervalMR {

    public Location curLocation;
    public Location nextLocation;
    public int objectID;
    public double[] center = new double[2];
    // long radius
    public double a = 0;
    // short radius
    public double b = 0;
    public double speed;
    public double maxSpeed;
    // angle
    public double angle;
    double[] MBR;
    // potential POI in this motion range
    public ArrayList<TimePointMR> timePointMRs = new ArrayList<>();

    public TimeIntervalMR(Location curLocation, Location nextLocation, double maxSpeed) {
        assert curLocation.objectID == nextLocation.objectID;
        this.objectID = curLocation.objectID;
        this.curLocation = curLocation;
        this.nextLocation = nextLocation;
        this.maxSpeed = maxSpeed;
        this.angle = Math.atan2(nextLocation.y - curLocation.y, nextLocation.x - curLocation.x);
        location2ellipse();
        generateTimePointMRs(Settings.intervalNum);
    }

    public ArrayList<Point> POIsWithinThis(QuadTree qTree) {
        double[] mbr = this.getEllipseMBR();
        List<Point> POIsCandidate = qTree.findAll(mbr[0], mbr[2], mbr[1], mbr[3]);
        ArrayList<Point> res = new ArrayList<>();
        for (Point p : POIsCandidate) {
            if (isPointInEllipse(p.x, p.y)) {
                res.add(p);
            }
        }
        System.out.println(POIsCandidate.size() + "/" + res.size());
        return res;
    }

    public ArrayList<TimePointMR> generateTimePointMRs(int intervalNum) {
        for (int i = 1; i < intervalNum + 2 - 1; i++) {
            // get time-point ranges of the two objects at several time points
            double Ax = curLocation.x;
            double Ay = curLocation.y;
            double Bx = nextLocation.x;
            double By = nextLocation.y;
            double r1 = maxSpeed * (nextLocation.timestamp - curLocation.timestamp) * i / intervalNum;
            double r2 = maxSpeed * (nextLocation.timestamp - curLocation.timestamp) * (intervalNum - i) / intervalNum;
            TimePointMR MR = new TimePointMR(Ax, Ay, Bx, By, r1, r2);
            timePointMRs.add(MR);
        }
        return timePointMRs;
    }

    // two locations form an ellipse
    public void location2ellipse() {
        center[0] = (curLocation.x + nextLocation.x) / 2;
        center[1] = (curLocation.y + nextLocation.y) / 2;
        speed = curLocation.distTo(nextLocation) / (nextLocation.timestamp - curLocation.timestamp);
        a = maxSpeed * (nextLocation.timestamp - curLocation.timestamp) / 2;
        b = Math.sqrt(4 * a * a
                - (Math.pow(curLocation.x - nextLocation.x, 2) + Math.pow(curLocation.y - nextLocation.y, 2))) / 2;
        if (a <= 0 || b <= 0 || a <= b) {
            System.out.println(nextLocation.timestamp - curLocation.timestamp);
            System.out.println("Error");
            System.out.println(a + "/" + b + "/" + maxSpeed + "/" + speed);
        }
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return curLocation.toString() + "->\n" + nextLocation.toString() + "\nCenter: " + Arrays.toString(center)
                + String.format("\nspeed: %.3f maxSpeed: %.3f a: %.3f b %.3f", speed, maxSpeed, a, b);
    }

    public double[] getEllipseMBR() {
        double centerX = center[0];
        double centerY = center[1];
        double majorRadius = a;
        double minorRadius = b;
        double cosTheta = Math.cos(angle);
        double sinTheta = Math.sin(angle);

        // Find the coordinates of the four corners of the unrotated bounding box
        double halfWidth = majorRadius;
        double halfHeight = minorRadius;
        double x1 = -halfWidth;
        double y1 = -halfHeight;
        double x2 = halfWidth;
        double y2 = -halfHeight;
        double x3 = halfWidth;
        double y3 = halfHeight;
        double x4 = -halfWidth;
        double y4 = halfHeight;

        // Rotate the coordinates of the four corners
        double rotatedX1 = x1 * cosTheta - y1 * sinTheta;
        double rotatedY1 = x1 * sinTheta + y1 * cosTheta;
        double rotatedX2 = x2 * cosTheta - y2 * sinTheta;
        double rotatedY2 = x2 * sinTheta + y2 * cosTheta;
        double rotatedX3 = x3 * cosTheta - y3 * sinTheta;
        double rotatedY3 = x3 * sinTheta + y3 * cosTheta;
        double rotatedX4 = x4 * cosTheta - y4 * sinTheta;
        double rotatedY4 = x4 * sinTheta + y4 * cosTheta;

        // Find the new coordinates of the rotated bounding box
        double minX = centerX + Math.min(rotatedX1, Math.min(rotatedX2, Math.min(rotatedX3, rotatedX4)));
        double minY = centerY + Math.min(rotatedY1, Math.min(rotatedY2, Math.min(rotatedY3, rotatedY4)));
        double maxX = centerX + Math.max(rotatedX1, Math.max(rotatedX2, Math.max(rotatedX3, rotatedX4)));
        double maxY = centerY + Math.max(rotatedY1, Math.max(rotatedY2, Math.max(rotatedY3, rotatedY4)));
        return new double[] { minX, maxX, minY, maxY };
    }

    public boolean isPointInEllipse(double px, double py) {
        // Translate point to ellipse's coordinate system
        double translatedX = px - (curLocation.x + nextLocation.x) / 2;
        double translatedY = py - (curLocation.y + nextLocation.y) / 2;
        // Rotate point to align ellipse with the axes
        double cosTheta = Math.cos(angle);
        double sinTheta = Math.sin(angle);
        double rotatedX = cosTheta * translatedX + sinTheta * translatedY;
        double rotatedY = -sinTheta * translatedX + cosTheta * translatedY;
        // Apply ellipse equation
        double ellipseEquation = (rotatedX * rotatedX) / (a * a) + (rotatedY * rotatedY) / (b * b);
        return ellipseEquation <= 1;
    }
}
