import java.util.ArrayList;
import java.util.HashMap;

public class Interpreter {

    // Stores facts for user-defined or previously seen predicates
    private HashMap<String, ArrayList<ArrayList<String>>> factDatabase;

    public Interpreter() {
        factDatabase = new HashMap<>();
    }

    // Runs the program:
    // all statements except the last one build the database
    // the last statement is treated as the query
    public void execute(ArrayList<Statement> statements) {
        if (statements == null || statements.isEmpty()) {
            System.out.println("No statements to execute.");
            return;
        }

        // Process all but the last statement as facts or definitions
        for (int i = 0; i < statements.size() - 1; i++) {
            storeStatement(statements.get(i));
        }

        // Evaluate the last statement as the final query
        Statement query = statements.get(statements.size() - 1);
        boolean result = evaluateStatement(query);

        System.out.println("Final query result: " + result);
    }

    // Stores earlier statements in the database
    private void storeStatement(Statement statement) {
        String predicate = statement.getPredicate();
        ArrayList<String> args = statement.getArguments();

        // Built-in predicates do not need to be stored as ordinary facts
        if (isBuiltIn(predicate)) {
            return;
        }

        // Store user-defined or other predicate facts
        factDatabase.putIfAbsent(predicate, new ArrayList<>());
        factDatabase.get(predicate).add(args);
    }

    // Evaluates one statement
    private boolean evaluateStatement(Statement statement) {
        String predicate = statement.getPredicate();
        ArrayList<String> args = statement.getArguments();

        if (predicate.equals("fatci")) {
            return handleFatci(args);
        } else if (predicate.equals("sumji")) {
            return handleSumji(args);
        } else if (predicate.equals("vujni")) {
            return handleVujni(args);
        } else if (predicate.equals("dunli")) {
            return handleDunli(args);
        } else if (predicate.equals("cmavo")) {
            return handleCmavo(args);
        } else {
            return checkStoredFact(predicate, args);
        }
    }

    // Checks if the predicate is one of the built-in predicates
    private boolean isBuiltIn(String predicate) {
        return predicate.equals("fatci")
                || predicate.equals("sumji")
                || predicate.equals("vujni")
                || predicate.equals("dunli")
                || predicate.equals("steni")
                || predicate.equals("steko")
                || predicate.equals("cmavo");
    }

    // fatci is true when there is exactly one argument
    private boolean handleFatci(ArrayList<String> args) {
        return args.size() == 1;
    }

    // sumji means first argument = second argument + third argument
    // Example: i 4 sumji 2 2
    private boolean handleSumji(ArrayList<String> args) {
        if (args.size() != 3) {
            return false;
        }

        try {
            int result = Integer.parseInt(args.get(0));
            int a = Integer.parseInt(args.get(1));
            int b = Integer.parseInt(args.get(2));
            return result == a + b;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // vujni means first argument = second argument - third argument
    private boolean handleVujni(ArrayList<String> args) {
        if (args.size() != 3) {
            return false;
        }

        try {
            int result = Integer.parseInt(args.get(0));
            int a = Integer.parseInt(args.get(1));
            int b = Integer.parseInt(args.get(2));
            return result == a - b;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // dunli checks whether two arguments are equal
    private boolean handleDunli(ArrayList<String> args) {
        if (args.size() != 2) {
            return false;
        }

        return args.get(0).equals(args.get(1));
    }

    // Placeholder for cmavo
    // For now, just accept the statement structure
    private boolean handleCmavo(ArrayList<String> args) {
        return !args.isEmpty();
    }

    // Checks whether a matching fact was stored earlier
    private boolean checkStoredFact(String predicate, ArrayList<String> args) {
        if (!factDatabase.containsKey(predicate)) {
            return false;
        }

        ArrayList<ArrayList<String>> knownFacts = factDatabase.get(predicate);

        for (ArrayList<String> factArgs : knownFacts) {
            if (factArgs.equals(args)) {
                return true;
            }
        }

        return false;
    }
}