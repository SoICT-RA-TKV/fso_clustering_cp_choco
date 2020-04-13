package solver;

import java.util.HashSet;
import java.util.Objects;

public class Location {
    Point2D center;
    HashSet<Integer> target;
    public Location(Point2D center, HashSet<Integer> target) {
        this.center = center;
        this.target = target;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return Objects.equals(target, location.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(target);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Integer t : target) {
            sb.append(" " + t);
        }
        return center + " " + sb.toString();
    }
}
