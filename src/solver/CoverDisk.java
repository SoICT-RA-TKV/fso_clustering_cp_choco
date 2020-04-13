package solver;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;

import java.awt.*;
import java.io.*;
import java.util.*;

public class CoverDisk {

    public static void main(String[] args) throws IOException {
        CoverDisk d = new CoverDisk("gfso_413_01");
        d.readInput();
        HashSet<Location> locationSet = d.listLocation();
        d.integerLinearProgramming2(locationSet);
    }

    private String dataName;
    private Integer NFSO;
    private Double[] x;
    private Double[] y;
    private Integer W;
    private Double R;

    public void integerLinearProgramming(HashSet<Location> locationSet) throws IOException {
        int NL = locationSet.size();
        int NT = 0;
        for (Location location : locationSet) {
            for (Integer iT : location.target) {
                NT = Math.max(NT, iT + 1);
            }
        }

        System.out.println(NL + " " + NT);

        int[][] k = new int[NL][NT];
        IntVar[][] c = new IntVar[NL][NT];
        IntVar[] ns = new IntVar[NL];

        Model model = new Model("");

        int iL = 0;
        for (Location location : locationSet) {

            System.out.println(iL + ": " + location);

            for (Integer iT : location.target) {
                k[iL][iT] = 1;
            }

            ns[iL] = model.intVar(0, (int) Math.ceil(1.0 * location.target.size() / this.W));
            for (int iT = 0; iT < NT; iT++) {
                c[iL][iT] = model.intVar(0, 1);
                model.arithm(c[iL][iT], "<=", k[iL][iT]).post();
            }

            IntVar tmp = model.intVar(0, NT);
            model.sum(c[iL], "=", tmp).post();
            model.arithm(ns[iL], "*", model.intVar(W, W), ">=", tmp).post();

            iL++;
        }

        for (int iT = 0; iT < NT; iT++) {
            IntVar[] ct = new IntVar[NL];
            for (iL = 0; iL < NL; iL++) {
                ct[iL] = c[iL][iT];
            }
            model.sum(ct, "=", model.intVar(1, 1)).post();
        }

        IntVar objective = model.intVar(0, NT);
        model.sum(ns, "=", objective).post();
        model.arithm(objective, "<", 11).post();
        model.setObjective(model.MINIMIZE, objective);

        FileWriter writer = new FileWriter("./data/gfso_" + dataName);
        PrintWriter printWriter = new PrintWriter(writer);
        printWriter.println(NL + " " + NT);
        for (iL = 0; iL < NL; iL++) {
            for (int iT = 0; iT < NT; iT++) {
                printWriter.print(k[iL][iT] + " ");
            }
            printWriter.println();
        }

        printWriter.close();

        System.out.println("Solving...");
        while (true) {
            if (!model.getSolver().solve()) {
                System.out.println("No more solution!");
                break;
            }
            System.out.println(objective.getValue());
        }
    }

    public void integerLinearProgramming2(HashSet<Location> locationSet) throws IOException {
        int NL = locationSet.size();
        int NT = 0;
        for (Location location : locationSet) {
            for (Integer iT : location.target) {
                NT = Math.max(NT, iT + 1);
            }
            if (location.target.size() >= 100) {
                System.out.println(location.target.size());
            }
        }

        Model model = new Model("");

        IntVar[] c = new IntVar[NL];
        int[][] k = new int[NT][NL];
        IntVar[][] s = new IntVar[NT][NL];

        int iL = 0;
        for (Location location : locationSet) {
            for (Integer target : location.target) {
                k[target][iL] = 1;
            }
            iL++;
        }

        for (iL = 0; iL < NL; iL++) {
            c[iL] = model.intVar(0, 1);
            for (int iT = 0; iT < NT; iT++) {
                s[iT][iL] = model.intScaleView(c[iL], k[iT][iL]);
            }
        }

        for (int iT = 0; iT < NT; iT++) {
            model.sum(s[iT], ">=", 1).post();
        }

        IntVar objective = model.intVar(0, NL);
        model.sum(c, "=", objective).post();

        model.setObjective(Model.MINIMIZE, objective);

        System.out.println("Solving...");
        while (true) {
            if (!model.getSolver().solve()) {
                System.out.println("No more solution!");
                break;
            }
            System.out.println(objective.getValue());
        }
    }

    public HashSet<Location> listLocation() {
        HashSet<Location> locationSet = new HashSet<>();
        for (int i = 0; i < NFSO; i++) {
            boolean isolated = true;
            for (int j = i + 1; j < NFSO; j++) {
                Point2D[] res = this.findCircleCenter(new Point2D(x[i], y[i]), new Point2D(x[j], y[j]), R);
                if (res == null) {
                    continue;
                }
                isolated = false;
                for (int k = 0; k < res.length; k++) {
                    double tmpR = Math.max(res[k].distance(new Point2D(x[i], y[i])) , res[k].distance(new Point2D(x[j], y[j])));
                    HashSet<Integer> newDisk = new HashSet<>();
                    for (int h = 0; h < NFSO; h++) {
                        if (res[k].distance(new Point2D(x[h], y[h])) <= tmpR) {
                            newDisk.add(h);
                        }
                    }
                    Location location = new Location(res[k], newDisk);
                    customAddLocation(locationSet, location);
                }
            }
            if (isolated) {
                Location location = new Location(new Point2D(x[i], y[i]), new HashSet<Integer>());
                location.target.add(i);
                customAddLocation(locationSet, location);
            }
        }
        return locationSet;
    }

    public Point2D[] findCircleCenter(Point2D p1, Point2D p2, double r) {
        if (p1.distance(p2) > 2 * r) {
            return null;
        }
        Point2D[] res;
        double al = p1.x - p2.x;
        double bl = p1.y - p2.y;
        Point2D mid = new Point2D((p1.x + p2.x) / 2, (p1.y + p2.y) / 2);
        double cl = -(al * mid.x + bl * mid.y);
        double a = Math.pow(bl / al, 2) + 1;
        double b = (2 * ((bl * cl) / Math.pow(al, 2))) + (2 * (bl / al) * p1.x) - (2 * p1.y);
        double c = Math.pow(cl / al, 2) + (2 * (cl / al) * p1.x) + Math.pow(p1.x, 2) + Math.pow(p1.y, 2) - Math.pow(r, 2);
        double eps = 1e-6;
        if (Math.abs(al) <= eps) {
            double y = (-cl / bl);
            double x0 = Math.sqrt(Math.pow(r, 2) - Math.pow(y - p1.y, 2)) + p1.x;
            double x1 = 2 * p1.x - x0;
            if (Math.abs(x0 - x1) <= eps) {
                res = new Point2D[1];
                res[0] = new Point2D(Math.max(x0, x1), y);
            } else {
                res = new Point2D[2];
                res[0] = new Point2D(x0, y);
                res[1] = new Point2D(x1, y);
            }
        } else {
            double delta = Math.pow(b, 2) - (4 * a * c);
            if (delta < 0) {
                return null;
            } else if (Math.abs(delta) <= eps) {
                res = new Point2D[1];
                double y0 = (-b / (2 * a));
                double x0 = (-b / a) * y0 - (c / a);
                res[0] = new Point2D(x0, y0);
            } else {
                res = new Point2D[2];
                double y0 = (-b + Math.sqrt(delta)) / (2 * a);
                double y1 = (-b - Math.sqrt(delta)) / (2 * a);
                double x0 = (-bl / al) * y0 - (cl / al);
                double x1 = (-bl / al) * y1 - (cl / al);
                res[0] = new Point2D(x0, y0);
                res[1] = new Point2D(x1, y1);
            }
        }
        return res;
    }

    public void customAddLocation(HashSet<Location> locationSet, Location location) {
        if (locationSet == null || location == null) {
            return;
        }
        boolean add = true;

        HashSet<Location> removeSet = new HashSet<>();

        for (Location other : locationSet) {
            if (other.target.containsAll(location.target)) {
                add = false;
                continue;
            }
            if (location.target.containsAll(other.target)) {
                removeSet.add(other);
            }
        }
        if (add) {
            locationSet.add(location);
            for (Location other : removeSet) {
                locationSet.remove(other);
            }
        }
    }

    public void readInput() throws FileNotFoundException {
        FileInputStream f = new FileInputStream("./data/" + dataName);
        Scanner s = new Scanner(f);
        while (!s.hasNextInt()) {
            s.next();
        }
        NFSO = s.nextInt();
        x = new Double[NFSO];
        y = new Double[NFSO];
        for (int i = 0; i < NFSO; i++) {
            while (!s.hasNextDouble()) {
                s.next();
            }
            x[i] = s.nextDouble();
            y[i] = s.nextDouble();
            s.nextDouble();
        }
        W = 128;
        R = 7.5;
    }

    public CoverDisk(String dataName) {
        this.dataName = dataName;
    }
}
