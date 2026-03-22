import java.util.ArrayList;

public class Interpreter {

    // runs all the statements in order
    public static void execute(ArrayList<Statement> statements) {
        for (Statement statement : statements) {
            runStatement(statement);
        }
    }

    // decides what to do based on the predicate name
    private static void runStatement(Statement statement) {
        String predicate = statement.getPredicate();
        ArrayList<String> args = statement.getArguments();

        if (predicate.equals("fatci")) {
            handleFatci(args);
        } else if (predicate.equals("sumji")) {
            handleSumji(args);
        } else if (predicate.equals("dunli")) {
            handleDunli(args);
        } else {
            System.out.println("Unknown predicate: " + predicate);
        }
    }

    // fatci checks if there is exactly one argument
    private static void handleFatci(ArrayList<String> args) {
        if (args.size() == 1) {
            System.out.println("fatci is true for: " + args.get(0));
        } else {
            System.out.println("fatci failed: wrong number of arguments");
        }
    }

    // sumji adds two numbers if possible
    private static void handleSumji(ArrayList<String> args) {
        if (args.size() != 2) {
            System.out.println("sumji failed: needs 2 arguments");
            return;
        }

        try {
            int a = Integer.parseInt(args.get(0));
            int b = Integer.parseInt(args.get(1));
            System.out.println("sumji result: " + (a + b));
        } catch (NumberFormatException e) {
            System.out.println("sumji failed: arguments must be numbers");
        }
    }

    // dunli checks if two arguments are equal
    private static void handleDunli(ArrayList<String> args) {
        if (args.size() != 2) {
            System.out.println("dunli failed: needs 2 arguments");
            return;
        }

        if (args.get(0).equals(args.get(1))) {
            System.out.println("dunli is true");
        } else {
            System.out.println("dunli is false");
        }
    }
}