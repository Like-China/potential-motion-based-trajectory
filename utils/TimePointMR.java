package utils;

import java.util.ArrayList;

import poi.QuadTree;

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

    public ArrayList<Point> POIsWithinThis(QuadTree qTree) {
        ArrayList<Point> POIsCandidate = qTree.findAll(Ax - r1, Ay - r1, Ax + r1, Ay + r1);
        ArrayList<Point> POIsCandidate1 = qTree.findAll(Bx - r1, By - r1, Bx + r1, By + r1);
        POIsCandidate.retainAll(POIsCandidate1);
        ArrayList<Point> res = new ArrayList<>();
        // if the object is static, then it has no potential POIs
        if (r1 == 0 || r2 == 0) {
            POIsCandidate.add(new Point(Ax, Ay));
            return POIsCandidate;
        }
        for (Point p : POIsCandidate) {
            if (isInsideOverlapArea(p.x, p.y)) {
                res.add(p);
            }
        }
        // System.out.println(POIsCandidate.size() + "/" + POIsCandidate1.size() + "/" +
        // res.size());
        return res;
    }

    // check if the smaple point is in the time-point motion range
    private boolean isInsideOverlapArea(double x, double y) {
        double distanceA = Math.sqrt((x - Ax) * (x - Ax) + (y - Ay) * (y - Ay));
        double distanceB = Math.sqrt((x - Bx) * (x - Bx) + (y - By) * (y - By));
        return distanceA <= r1 && distanceB <= r2;
    }

}
