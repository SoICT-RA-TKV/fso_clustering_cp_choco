import org.chocosolver.solver.Model;
import org.chocosolver.solver.variables.IntVar;
import org.chocosolver.solver.constraints.Constraint;

public class Main {
    public static void main(String[] args) {
        Model model = new Model("Choco Solver Hello World");
        IntVar a = model.intVar("a", new int[] {0, 1, 2, 3, 5});
        IntVar b = model.intVar("b", 0, 2);

        model.arithm(a, "*", a, "<=", 9).post();

        model.ifThen(model.arithm(a, "+", b, "=", 2), model.arithm(a,"!=", 1));
        int i = 1;
        while (model.getSolver().solve()) {
            System.out.println("Solution " + i++ + " found: " + a + ", " + b);
        }
    }
}
