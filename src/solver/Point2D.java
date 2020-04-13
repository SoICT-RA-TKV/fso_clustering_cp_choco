package solver;

public class Point2D {
    public double x;
    public double y;
    public Point2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double distance(Point2D other) {
        return Math.sqrt(Math.pow(this.x - other.x, 2) + Math.pow(this.y - other.y, 2));
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
