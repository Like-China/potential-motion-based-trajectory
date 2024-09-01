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
    // the number of POI in this time-point interval
    public int POI_NB = 0;

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
        this.POI_NB = POIs.size();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof TimePointMR) {
            TimePointMR that = (TimePointMR) obj;
            if (this.Ax != that.Ax)
                return false;
            if (this.Ay != that.Ay)
                return false;
            if (this.Bx != that.Bx)
                return false;
            if (this.By != that.By)
                return false;
            if (this.r1 != that.r1)
                return false;
            if (this.r2 != that.r2)
                return false;
        }
        return true;
    }

    public ArrayList<Point> POIsWithinThis(QuadTree qTree) {
        ArrayList<Point> POIsCandidate = qTree.query(Ax - r1, Ay - r1, Ax + r1, Ay + r1);
        ArrayList<Point> POIsCandidate1 = qTree.query(Bx - r2, By - r2, Bx + r2, By + r2);
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
