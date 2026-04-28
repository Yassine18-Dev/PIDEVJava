package components;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.LinkedHashMap;
import java.util.Map;

public class RadarChart extends Canvas {

    private Map<String, Double> data = new LinkedHashMap<>();
    private double maxValue = 100.0;

    private Color axisColor   = Color.web("#2a3f5f");
    private Color webColor    = Color.web("#1fb3d2", 0.20);
    private Color strokeColor = Color.web("#1fb3d2");
    private Color pointColor  = Color.web("#22c1e2");
    private Color labelColor  = Color.web("#b9c4d0");

    public RadarChart(double w, double h) { super(w, h); }

    public void setData(Map<String, Double> data) {
        this.data = data;
        draw();
    }

    public void setStrokeColor(Color c) {
        this.strokeColor = c;
        this.webColor    = c.deriveColor(0, 1, 1, 0.20);
        this.pointColor  = c.brighter();
        draw();
    }

    private void draw() {
        GraphicsContext g = getGraphicsContext2D();
        double w = getWidth(), h = getHeight();
        g.clearRect(0, 0, w, h);
        if (data == null || data.isEmpty()) return;

        int n = data.size();
        double cx = w / 2, cy = h / 2;
        double radius = Math.min(w, h) / 2.0 - 50;
        double[] angles = new double[n];
        for (int i = 0; i < n; i++) angles[i] = -Math.PI/2 + 2*Math.PI*i/n;

        g.setStroke(axisColor); g.setLineWidth(1);
        for (int level = 1; level <= 5; level++) {
            double r = radius * level / 5.0;
            double[] xs = new double[n], ys = new double[n];
            for (int i = 0; i < n; i++) {
                xs[i] = cx + r * Math.cos(angles[i]);
                ys[i] = cy + r * Math.sin(angles[i]);
            }
            g.strokePolygon(xs, ys, n);
        }

        for (int i = 0; i < n; i++)
            g.strokeLine(cx, cy,
                    cx + radius * Math.cos(angles[i]),
                    cy + radius * Math.sin(angles[i]));

        double[] xs = new double[n], ys = new double[n];
        int i = 0;
        for (Double v : data.values()) {
            double r = radius * Math.min(v, maxValue) / maxValue;
            xs[i] = cx + r * Math.cos(angles[i]);
            ys[i] = cy + r * Math.sin(angles[i]);
            i++;
        }
        g.setFill(webColor); g.fillPolygon(xs, ys, n);
        g.setStroke(strokeColor); g.setLineWidth(2.5); g.strokePolygon(xs, ys, n);

        g.setFill(pointColor);
        for (int j = 0; j < n; j++) g.fillOval(xs[j]-4, ys[j]-4, 8, 8);

        g.setFill(labelColor);
        g.setFont(Font.font("Segoe UI", 13));
        g.setTextAlign(TextAlignment.CENTER);
        i = 0;
        for (String label : data.keySet()) {
            double lx = cx + (radius + 28) * Math.cos(angles[i]);
            double ly = cy + (radius + 28) * Math.sin(angles[i]) + 5;
            g.fillText(label, lx, ly);
            i++;
        }
    }
}