package components;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

public class SpiderChart extends Canvas {

    private int[]    values  = new int[]{50, 50, 50, 50, 50};
    private String[] labels  = {"Vision", "Communication", "Teamplay", "Réflexes", "Tir"};

    private final Color polygonColor       = Color.rgb(31, 179, 210, 0.35);
    private final Color polygonStrokeColor = Color.rgb(31, 179, 210, 1.0);
    private final Color gridColor          = Color.rgb(42, 63, 95, 0.6);
    private final Color labelColor         = Color.rgb(185, 196, 208, 1.0);
    private final Color pointColorNormal   = Color.rgb(34, 193, 226, 1.0);
    private final Color pointColorStrong   = Color.rgb(241, 196, 15, 1.0);  // jaune si > 80
    private final Color valueColor         = Color.WHITE;

    private static final int    MAX_VALUE        = 100;
    private static final int    GRID_LEVELS      = 5;
    private static final double LABEL_OFFSET     = 28;
    private static final double POINT_RADIUS     = 5;
    private static final double STRONG_THRESHOLD = 80;

    public SpiderChart(double width, double height) {
        super(width, height);
        draw();
    }

    public void setValues(int[] values) {
        if (values == null || values.length != 5)
            throw new IllegalArgumentException("Le SpiderChart attend exactement 5 valeurs.");
        this.values = values;
        draw();
    }

    public void setLabels(String[] labels) {
        if (labels == null || labels.length != 5)
            throw new IllegalArgumentException("Le SpiderChart attend exactement 5 labels.");
        this.labels = labels;
        draw();
    }

    private void draw() {
        GraphicsContext gc = getGraphicsContext2D();
        double w = getWidth();
        double h = getHeight();
        gc.clearRect(0, 0, w, h);

        double cx = w / 2;
        double cy = h / 2;
        double radius = Math.min(w, h) / 2.0 - 50;

        int n = values.length;
        double[] angles = new double[n];
        for (int i = 0; i < n; i++) angles[i] = -Math.PI / 2 + (2 * Math.PI * i / n);

        // 1) GRILLE concentrique (5 niveaux)
        gc.setStroke(gridColor);
        gc.setLineWidth(1);
        for (int level = 1; level <= GRID_LEVELS; level++) {
            double r = radius * level / GRID_LEVELS;
            double[] xs = new double[n];
            double[] ys = new double[n];
            for (int i = 0; i < n; i++) {
                xs[i] = cx + r * Math.cos(angles[i]);
                ys[i] = cy + r * Math.sin(angles[i]);
            }
            gc.strokePolygon(xs, ys, n);
        }

        // 2) AXES depuis le centre
        for (int i = 0; i < n; i++) {
            gc.strokeLine(cx, cy,
                    cx + radius * Math.cos(angles[i]),
                    cy + radius * Math.sin(angles[i]));
        }

        // 3) POLYGONE des valeurs du joueur
        double[] xs = new double[n];
        double[] ys = new double[n];
        for (int i = 0; i < n; i++) {
            double r = radius * Math.min(values[i], MAX_VALUE) / (double) MAX_VALUE;
            xs[i] = cx + r * Math.cos(angles[i]);
            ys[i] = cy + r * Math.sin(angles[i]);
        }
        gc.setFill(polygonColor);
        gc.fillPolygon(xs, ys, n);
        gc.setStroke(polygonStrokeColor);
        gc.setLineWidth(2.5);
        gc.strokePolygon(xs, ys, n);

        // 4) POINTS (jaunes si > 80, sinon cyan)
        for (int i = 0; i < n; i++) {
            boolean strong = values[i] > STRONG_THRESHOLD;
            gc.setFill(strong ? pointColorStrong : pointColorNormal);
            double r = strong ? POINT_RADIUS + 1 : POINT_RADIUS;
            gc.fillOval(xs[i] - r, ys[i] - r, r * 2, r * 2);
        }

        // 5) LABELS + valeurs
        gc.setTextAlign(TextAlignment.CENTER);
        for (int i = 0; i < n; i++) {
            double lx = cx + (radius + LABEL_OFFSET) * Math.cos(angles[i]);
            double ly = cy + (radius + LABEL_OFFSET) * Math.sin(angles[i]) + 4;

            gc.setFill(labelColor);
            gc.setFont(Font.font("Segoe UI", FontWeight.NORMAL, 12));
            gc.fillText(labels[i], lx, ly);

            gc.setFill(valueColor);
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 13));
            gc.fillText(String.valueOf(values[i]), lx, ly + 16);
        }
    }
}