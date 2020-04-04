package solver;

import javafx.beans.binding.IntegerExpression;
import org.chocosolver.solver.Model;
import org.chocosolver.solver.constraints.Constraint;
import org.chocosolver.solver.variables.BoolVar;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.variables.IntVar;
import org.jgrapht.alg.util.Pair;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.*;

public class CPModel {

    private Integer NFSO;
    private Integer[] x;
    private Integer[] y;
    private Integer[][] d;
    private Integer W;
    private Integer bw;
    private Integer R;
    private Integer L;

    private IntVar NHAP;
    private IntVar[][] H;
    private IntVar[] X;
    private IntVar[] Y;

    public CPModel(Integer NFSO, Integer[] x, Integer[] y, Integer[][] d, Integer W, Integer bw, Integer R, Integer L) {
        this.setNFSO(NFSO);
        this.setX(x);
        this.setY(y);
        this.setD(d);
        this.setW(W);
        this.setBw(bw);
        this.setR(R);
        this.setL(L);
    }

    public CPModel() {
        try {
            this.readInput();
        } catch (Exception e) {
            try {
                throw e;
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void stateModel() {
        Model model = new Model("FSO Clustering Constraint Model");

        this.NFSO = 50;
        int minNHAP = Math.max(10, 0);
        int maxNHAP = Math.min(10, NFSO);
        NHAP = model.intVar("NHAP", minNHAP, maxNHAP);

        X = new IntVar[maxNHAP];
        Y = new IntVar[maxNHAP];

        Integer minX = Integer.MAX_VALUE;
        Integer minY = Integer.MAX_VALUE;
        Integer maxX = Integer.MIN_VALUE;
        Integer maxY = Integer.MIN_VALUE;

        for (int i = 0; i < NFSO; i++) {
            minX = Math.min(minX, x[i]);
            minY = Math.min(minY, y[i]);
            maxX = Math.max(maxX, x[i]);
            maxY = Math.max(maxY, y[i]);
        }

        System.out.println((maxX - minX) + " " + (maxY - minY));

        for (int i = 0; i < maxNHAP; i++) {
            for (int j = 0; j < maxNHAP; j++) {
                X[i] = model.intVar(minX, maxX);
                Y[i] = model.intVar(minY, maxY);
            }
        }

        H = new IntVar[NFSO][NFSO];
        IntVar[][] tmp_H = new IntVar[NFSO][NFSO];
        for (int i = 0; i < NFSO; i++) {
            IntVar[] filter = new IntVar[maxNHAP];
            for (int u = 0; u < maxNHAP; u++) {
                H[i][u] = model.intVar("H_" + i + "_" + u, new int[] {0, 1});
                tmp_H[u][i] = H[i][u];
                // Rang buoc 3
                IntVar deltaX = model.intVar(minX - maxX, maxX - minX);
                IntVar deltaY = model.intVar(minY - maxY, maxY - minY);
                IntVar sqrDeltaX = model.intVar(0, (maxX - minX) * (maxX - minX));
                IntVar sqrDeltaY = model.intVar(0, (maxY - minY) * (maxY - minY));
                IntVar tmpX = model.intVar(x[i], x[i]);
                IntVar tmpY = model.intVar(y[i], y[i]);
                model.arithm(X[u], "-", tmpX, "=", deltaX).post();
                model.arithm(Y[u], "-", tmpY, "=", deltaY).post();
                model.arithm(deltaX, "*", deltaX, "=", sqrDeltaX).post();
                model.arithm(deltaY, "*", deltaY, "=", sqrDeltaY).post();
                IntVar sqrR = model.intVar(R * R, R * R);
                IntVar tmpU = model.intVar(u, u);
                Constraint tmp_c1 = model.arithm(tmpU, "<", NHAP);
                Constraint tmp_c2 = model.arithm(H[i][u], "=", 1);
                Constraint c1 = model.and(tmp_c1, tmp_c2);
                Constraint c2 = model.arithm(sqrDeltaX, "+", sqrDeltaY, "<=", sqrR);
                model.ifThen(c1, c2);

                // Rang buoc 4
                filter[u] = model.intVar(0, 1);
                model.ifThen(tmp_c1, model.arithm(filter[u], "=", H[i][u]));
                model.ifThen(model.not(tmp_c1), model.arithm(filter[u], "=", 0));
            }
            // Rang buoc 4
            model.sum(filter, "=", 1).post();
        }
        for (int u = 0; u < maxNHAP; u++) {
            IntVar tmpU = model.intVar(u, u);
            Constraint tmp_c1 = model.arithm(tmpU, "<", NHAP);
            Constraint tmp_f1 = model.sum(tmp_H[u], "<=", W);
            model.ifThen(tmp_c1, tmp_f1);
        }

        model.setObjective(Model.MINIMIZE, NHAP);
        int count = 1;
        while (true) {
            if (!model.getSolver().solve()) {
                System.out.println("No more solution!");
                break;
            }
            System.out.println("Solution " + count++ + " found!");
            System.out.println(NHAP);
            for (int i = 0; i < NFSO; i++) {
                for (int u = 0; u < NHAP.getValue(); u++) {
                    System.out.print(H[i][u].getValue() + " ");
                }
                System.out.println();
            }
            System.out.println("Time: " + (System.nanoTime() / 1e9));
//            break;
        }
        System.out.println("Finish Search Time: " + (System.nanoTime() / 1e9));
    }

    public void search() {

    }

    public void printResult() {

    }

    public void readInput() throws FileNotFoundException {
        FileInputStream f = new FileInputStream("./data/input.txt");
        Scanner s = new Scanner(f);
        while (!s.hasNextInt()) {
            s.next();
        }
        NFSO = s.nextInt();
        x = new Integer[NFSO];
        y = new Integer[NFSO];
        d = new Integer[NFSO][NFSO];
        for (int i = 0; i < NFSO; i++) {
            while (!s.hasNextDouble()) {
                s.next();
            }
            Double tmp = null;
            tmp = s.nextDouble();
            x[i] = tmp.intValue();
            tmp = s.nextDouble();
            y[i] = tmp.intValue();
            s.nextDouble();
        }
        while (!s.hasNextInt()) {
            s.next();
        }
        while (s.hasNextInt()) {
            Integer tmp_i, tmp_j, tmp_d;
            tmp_i = s.nextInt();
            tmp_j = s.nextInt();
            tmp_d = s.nextInt();
            d[tmp_i][tmp_j] = tmp_d;
        }
        W = 128;
        bw = 1024;
        R = 8;
        L = 40;
    }

    public static void main(String[] args) {
        long start = System.nanoTime();
        CPModel s = new CPModel();
        s.stateModel();
        long finish = System.nanoTime();

        System.out.println("Running Time: " + ((finish - start) / 1e9));
    }

    public Integer getNFSO() {
        return NFSO;
    }

    public void setNFSO(Integer NFSO) {
        this.NFSO = NFSO;
    }

    public Integer[] getX() {
        return x;
    }

    public void setX(Integer[] x) {
        this.x = x;
    }

    public Integer[] getY() {
        return y;
    }

    public void setY(Integer[] y) {
        this.y = y;
    }

    public Integer[][] getD() {
        return d;
    }

    public void setD(Integer[][] d) {
        this.d = d;
    }

    public Integer getW() {
        return W;
    }

    public void setW(Integer w) {
        W = w;
    }

    public Integer getBw() {
        return bw;
    }

    public void setBw(Integer bw) {
        this.bw = bw;
    }

    public Integer getR() {
        return R;
    }

    public void setR(Integer r) {
        R = r;
    }

    public Integer getL() {
        return L;
    }

    public void setL(Integer l) {
        L = l;
    }
}
