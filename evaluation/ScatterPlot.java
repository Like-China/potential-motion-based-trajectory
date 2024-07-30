package evaluation;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ScatterPlot extends JPanel {

    private List<double[]> points;

    public ScatterPlot(List<double[]> points) {
        this.points = points;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw axes
        int width = getWidth();
        int height = getHeight();
        g2d.drawLine(50, height - 50, width - 50, height - 50); // X axis
        g2d.drawLine(50, height - 50, 50, 50); // Y axis

        // Draw points
        g2d.setColor(Color.RED);
        for (double[] point : points) {
            int x = (int) (50 + point[0] * (width - 100));
            int y = (int) (height - 50 - point[1] * (height - 100));
            g2d.fillOval(x - 2, y - 2, 4, 4);
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Scatter Plot Example");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 800);

        List<double[]> points = new ArrayList<>();
        points.add(new double[] { 0.1, 0.2 });
        points.add(new double[] { 0.4, 0.3 });
        points.add(new double[] { 0.7, 0.8 });
        points.add(new double[] { 0.2, 0.6 });
        points.add(new double[] { 0.9, 0.4 });

        ScatterPlot scatterPlot = new ScatterPlot(points);
        frame.add(scatterPlot);
        frame.setVisible(true);
    }
}
