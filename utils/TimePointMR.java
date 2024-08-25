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
    // pois in this time-point motion range
    public ArrayList<Point> POIs = new ArrayList<>();

    // define a time-point interval based two circles (Ax,Ay, r1) and (Bx,By,r2)
    public TimePointMR(double Ax, double Ay, double Bx, double By, double r1, double r2, QuadTree qTree) {
        this.Ax = Ax;
        this.Ay = Ay;
        this.Bx = Bx;
        this.By = By;
        this.r1 = r1;
        this.r2 = r2;
        distance = Math.sqrt((Ax - Bx) * (Ax - Bx) + (Ay - By) * (Ay - By));
        assert distance < r1 + r2 : "No Interseection";
        POIs = POIsWithinThis(qTree);
    }

    public ArrayList<Point> POIsWithinThis(QuadTree qTree) {
        ArrayList<Point> POIsCandidate = qTree.findAll(Ax - r1, Ay - r1, Ax + r1, Ay + r1);
        ArrayList<Point> POIsCandidate1 = qTree.findAll(Bx - r2, By - r2, Bx + r2, By + r2);
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
