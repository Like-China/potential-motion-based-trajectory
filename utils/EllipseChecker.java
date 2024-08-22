package utils;

public class EllipseChecker {

    public static boolean isPointInEllipse(double px, double py, double x1, double y1, double x2, double y2, double a,
            double b) {
        // Compute rotation angle based on foci
        double angle = Math.atan2(y2 - y1, x2 - x1);

        // Translate point to ellipse's coordinate system
        double translatedX = px - (x1 + x2) / 2;
        double translatedY = py - (y1 + y2) / 2;

        // Rotate point to align ellipse with the axes
        double cosTheta = Math.cos(angle);
        double sinTheta = Math.sin(angle);

        double rotatedX = cosTheta * translatedX + sinTheta * translatedY;
        double rotatedY = -sinTheta * translatedX + cosTheta * translatedY;

        // Apply ellipse equation
        double ellipseEquation = (rotatedX * rotatedX) / (a * a) + (rotatedY * rotatedY) / (b * b);

        return ellipseEquation <= 1;
    }

    public static void main(String[] args) {
        // Example usage
        double px = 1.0;
        double py = 2.0;
        double x1 = -3.0; // Focus 1
        double y1 = 0.0;
        double x2 = 3.0; // Focus 2
        double y2 = 0.0;
        double a = 5.0; // Semi-major axis
        double b = 3.0; // Semi-minor axis

        boolean result = isPointInEllipse(px, py, x1, y1, x2, y2, a, b);
        System.out.println("Point is within ellipse: " + result);
    }
}
