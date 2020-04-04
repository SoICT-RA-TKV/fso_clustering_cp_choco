package solver;

import org.jgrapht.alg.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;

public class Graph {
    ArrayList<Pair<Double, Double>> V;
    HashMap<Pair<Double, Double>, Double> E;

    public Graph() {
        V = new ArrayList<Pair<Double, Double>>();
        E = new HashMap<Pair<Double, Double>, Double>();
    }

    public ArrayList<Pair<Double, Double>> getV() {
        return V;
    }

    public void setV(ArrayList<Pair<Double, Double>> v) {
        V = v;
    }

    public HashMap<Pair<Double, Double>, Double> getE() {
        return E;
    }

    public void setE(HashMap<Pair<Double, Double>, Double> e) {
        E = e;
    }
}
