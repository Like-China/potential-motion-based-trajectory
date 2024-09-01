package poi;

import java.util.ArrayList;
import java.util.List;

import utils.Point;

public class QuadTree {

    private static final int CAPACITY = 4; // Maximum points in a leaf before splitting
    private final double x1, y1, x2, y2; // Bounding box
    private final List<Point> points; // Points in this quadrant
    private QuadTree[] children; // Subdivided quadrants

    public QuadTree(double x1, double y1, double x2, double y2) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.points = new ArrayList<>();
    }

    public void insert(Point point) {
        if (!contains(point))
            return;

        if (points.size() < CAPACITY || (x2 - x1) <= 1) {
            points.add(point);
        } else {
            if (children == null)
                subdivide();
            for (QuadTree child : children) {
                child.insert(point);
            }
        }
    }

    public ArrayList<Point> query(double x1, double y1, double x2, double y2) {
        ArrayList<Point> result = new ArrayList<>();
        if (!intersects(x1, y1, x2, y2))
            return result;

        for (Point point : points) {
            if (point.x >= x1 && point.y >= y1 && point.x <= x2 && point.y <= y2) {
                result.add(point);
            }
        }

        if (children != null) {
            for (QuadTree child : children) {
                result.addAll(child.query(x1, y1, x2, y2));
            }
        }

        return result;
    }

    private boolean contains(Point point) {
        return point.x >= x1 && point.y >= y1 && point.x <= x2 && point.y <= y2;
    }

    private boolean intersects(double x1, double y1, double x2, double y2) {
        return this.x1 <= x2 && this.y1 <= y2 && this.x2 >= x1 && this.y2 >= y1;
    }

    private void subdivide() {
        double midX = (x1 + x2) / 2;
        double midY = (y1 + y2) / 2;
        children = new QuadTree[4];
        children[0] = new QuadTree(x1, y1, midX, midY); // Top-left
        children[1] = new QuadTree(midX, y1, x2, midY); // Top-right
        children[2] = new QuadTree(x1, midY, midX, y2); // Bottom-left
        children[3] = new QuadTree(midX, midY, x2, y2); // Bottom-right
    }

    // public static void main(String[] args) {
    // QuadTree tree = new QuadTree(0, 0, 100, 100);
    // tree.insert(new Point(10, 10));
    // tree.insert(new Point(20, 20));
    // tree.insert(new Point(30, 30));
    // tree.insert(new Point(40, 40));

    // ArrayList<Point> foundPoints = tree.query(15, 15, 35, 35);
    // for (Point p : foundPoints) {
    // System.out.println("Found point: (" + p.x + ", " + p.y + ")");
    // }

    // }
}
