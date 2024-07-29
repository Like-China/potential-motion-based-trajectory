package utils;

import java.util.Arrays;

public class Ellipse {

    public Location curLocation;
    public Location nextLocation;
    public int objectID;
    public double[] center = new double[2];
    // long radius
    public double a = 0;
    // short radius
    public double b = 0;
    public double meanSpeed;
    public double maxSpeed;
    // angle
    public double angle;
    double[] MBR;

    public Ellipse(Location curLocation, Location nextLocation, double maxSpeed) {
        assert curLocation.objectID == nextLocation.objectID;
        this.objectID = curLocation.objectID;
        this.curLocation = curLocation;
        this.nextLocation = nextLocation;
        this.maxSpeed = maxSpeed;
        this.angle = Math.atan2(nextLocation.y - curLocation.y, nextLocation.x - curLocation.x);
        location2ellipse();
    }

    public double getSpeed() {
        double dist = betweenDist();
        return dist / (nextLocation.timestamp - curLocation.timestamp);
    }

    // meters
    public double betweenDist() {
        return curLocation.distTo(nextLocation);
    }

    // two locations form an ellipse
    public void location2ellipse() {
        center[0] = (curLocation.x + nextLocation.x) / 2;
        center[1] = (curLocation.y + nextLocation.y) / 2;
        meanSpeed = getSpeed();
        this.maxSpeed = meanSpeed * maxSpeed;
        a = maxSpeed * (nextLocation.timestamp - curLocation.timestamp) / 2;
        b = Math.sqrt(a * a
                - (Math.pow(curLocation.x - nextLocation.x, 2) + Math.pow(curLocation.y - nextLocation.y, 2)) / 4);
        // System.out.println(a + "/" + meanSpeed + "/" + maxSpeed);
        assert a >= 0;
        assert b >= 0;
        assert a >= b;
    }

    @Override
    public String toString() {
        // TODO Auto-generated method stub
        return curLocation.toString() + "->\n" + nextLocation.toString() + "\nCenter: " + Arrays.toString(center)
                + " meanSpeed: " + meanSpeed + " maxSpeed: " + maxSpeed;
    }

    // get the area of the ellipse
    public double getArea() {
        return Math.PI * a * b;
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

    // calculate intersection area to another ellipse's MBR
    public double interAreaTo(Ellipse that) {
        if (this.MBR == null) {
            this.MBR = this.getEllipseMBR();
        }
        if (that.MBR == null) {
            that.MBR = that.getEllipseMBR();
        }
        // Calculate the coordinates of the intersection rectangle
        double x1 = Math.max(this.MBR[0], that.MBR[0]);
        double y1 = Math.max(this.MBR[2], that.MBR[2]);
        double x2 = Math.min(this.MBR[1], that.MBR[1]);
        double y2 = Math.min(this.MBR[3], that.MBR[3]);
        // Calculate the area of the intersection rectangle
        double intersectionArea = (x2 - x1) * (y2 - y1);
        // Check if there is no intersection
        if (intersectionArea < 0) {
            intersectionArea = 0;
        }
        return intersectionArea;
    }
}
