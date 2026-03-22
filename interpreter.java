import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Interpreter {

    // Stores ordinary facts:
    // predicate name -> list of argument lists
    private HashMap<String, ArrayList<ArrayList<String>>> factDatabase;

    // Stores simple cmavo definitions:
    // defined predicate name -> predicate it maps to
    private HashMap<String, String> definitionDatabase;

    public Interpreter() {
        factDatabase = new HashMap<>();
        definitionDatabase = new HashMap<>();
    }

    // Runs the whole program
    public void execute(ArrayList<Statement> statements) {
        if (statements == null || statements.isEmpty()) {
            System.out.println("No statements to execute.");
            return;
        }

        // Build database from all but the last statement
        for (int i = 0; i < statements.size() - 1; i++) {
            processStatement(statements.get(i));
        }

        // Final statement is the query
        Statement query = statements.get(statements.size() - 1);
        HashMap<String, String> bindings = new HashMap<>();

        boolean result = evaluateStatement(query, bindings);

        if (!result) {
            System.out.println("Query failed.");
            return;
        }

        // Print variable bindings if there are any
        if (bindings.isEmpty()) {
            System.out.println("Query succeeded.");
        } else {
            System.out.println("Query succeeded.");
            System.out.println("Variable values:");
            for (Map.Entry<String, String> entry : bindings.entrySet()) {
                System.out.println(entry.getKey() + " = " + entry.getValue());
            }
        }
    }

    // Processes a statement before the final query
    private void processStatement(Statement statement) {
        String predicate = statement.getPredicate();
        ArrayList<String> args = statement.getArguments();

        // Handle cmavo definitions
        if (statement.isDefinition() || predicate.equals("cmavo")) {
            handleCmavoDefinition(args);
            return;
        }

        // Built-in predicates used before the final query can be treated as facts
        if (!isBuiltIn(predicate)) {
            storeFact(predicate, args);
        } else {
            // For simple built-in facts, only store if true
            HashMap<String, String> tempBindings = new HashMap<>();
            if (evaluateBuiltIn(predicate, args, tempBindings)) {
                storeFact(predicate, args);
            }
        }
    }

    // Stores a fact
    private void storeFact(String predicate, ArrayList<String> args) {
        factDatabase.putIfAbsent(predicate, new ArrayList<>());
        factDatabase.get(predicate).add(new ArrayList<>(args));
    }

    // Evaluates a single statement with variable bindings
    private boolean evaluateStatement(Statement statement, HashMap<String, String> bindings) {
        String predicate = statement.getPredicate();
        ArrayList<String> args = statement.getArguments();

        // Follow simple cmavo definitions if present
        if (definitionDatabase.containsKey(predicate)) {
            predicate = definitionDatabase.get(predicate);
        }

        if (isBuiltIn(predicate)) {
            return evaluateBuiltIn(predicate, args, bindings);
        }

        return checkStoredFact(predicate, args, bindings);
    }

    // Evaluates built-in predicates
    private boolean evaluateBuiltIn(String predicate, ArrayList<String> args, HashMap<String, String> bindings) {
        if (predicate.equals("fatci")) {
            return handleFatci(args, bindings);
        } else if (predicate.equals("sumji")) {
            return handleSumji(args, bindings);
        } else if (predicate.equals("vujni")) {
            return handleVujni(args, bindings);
        } else if (predicate.equals("dunli")) {
            return handleDunli(args, bindings);
        } else if (predicate.equals("steni")) {
            return handleSteni(args, bindings);
        } else if (predicate.equals("steko")) {
            return handleSteko(args, bindings);
        } else if (predicate.equals("cmavo")) {
            return !args.isEmpty();
        }

        return false;
    }

    // Checks if a predicate is built in
    private boolean isBuiltIn(String predicate) {
        return predicate.equals("fatci")
                || predicate.equals("sumji")
                || predicate.equals("vujni")
                || predicate.equals("dunli")
                || predicate.equals("steni")
                || predicate.equals("steko")
                || predicate.equals("cmavo");
    }

    // A very simple variable test:
    // names start and end with periods, numbers are digits,
    // everything else is treated like a variable/pattern symbol
    private boolean isVariable(String value) {
        if (value == null) {
            return false;
        }

        if (value.matches("0|[1-9][0-9]*")) {
            return false;
        }

        if (value.matches("\\.[a-z]+\\.")) {
            return false;
        }

        if (value.equals("[]")) {
            return false;
        }

        if (value.startsWith("[HEAD:")) {
            return false;
        }

        return true;
    }

    // Binds a variable or checks equality with an existing binding
    private boolean bindOrMatch(String variable, String value, HashMap<String, String> bindings) {
        if (!bindings.containsKey(variable)) {
            bindings.put(variable, value);
            return true;
        }
        return bindings.get(variable).equals(value);
    }

    // Resolves a value if it is already a bound variable
    private String resolveValue(String value, HashMap<String, String> bindings) {
        if (isVariable(value) && bindings.containsKey(value)) {
            return bindings.get(value);
        }
        return value;
    }

    // fatci succeeds if exactly one argument is given
    // and can bind a variable to a placeholder fact value if needed
    private boolean handleFatci(ArrayList<String> args, HashMap<String, String> bindings) {
        if (args.size() != 1) {
            return false;
        }

        String arg = args.get(0);

        if (isVariable(arg)) {
            return bindOrMatch(arg, "true", bindings);
        }

        return true;
    }

    // sumji means first = second + third
    // supports one variable among the arguments
    private boolean handleSumji(ArrayList<String> args, HashMap<String, String> bindings) {
        if (args.size() != 3) {
            return false;
        }

        String a0 = resolveValue(args.get(0), bindings);
        String a1 = resolveValue(args.get(1), bindings);
        String a2 = resolveValue(args.get(2), bindings);

        int variableCount = 0;
        if (isVariable(a0)) variableCount++;
        if (isVariable(a1)) variableCount++;
        if (isVariable(a2)) variableCount++;

        if (variableCount > 1) {
            return false;
        }

        try {
            if (!isVariable(a0) && !isVariable(a1) && !isVariable(a2)) {
                return Integer.parseInt(a0) == Integer.parseInt(a1) + Integer.parseInt(a2);
            }

            if (isVariable(a0)) {
                int value = Integer.parseInt(a1) + Integer.parseInt(a2);
                return bindOrMatch(a0, String.valueOf(value), bindings);
            }

            if (isVariable(a1)) {
                int value = Integer.parseInt(a0) - Integer.parseInt(a2);
                return bindOrMatch(a1, String.valueOf(value), bindings);
            }

            int value = Integer.parseInt(a0) - Integer.parseInt(a1);
            return bindOrMatch(a2, String.valueOf(value), bindings);

        } catch (NumberFormatException e) {
            return false;
        }
    }

    // vujni means first = second - third
    // supports one variable among the arguments
    private boolean handleVujni(ArrayList<String> args, HashMap<String, String> bindings) {
        if (args.size() != 3) {
            return false;
        }

        String a0 = resolveValue(args.get(0), bindings);
        String a1 = resolveValue(args.get(1), bindings);
        String a2 = resolveValue(args.get(2), bindings);

        int variableCount = 0;
        if (isVariable(a0)) variableCount++;
        if (isVariable(a1)) variableCount++;
        if (isVariable(a2)) variableCount++;

        if (variableCount > 1) {
            return false;
        }

        try {
            if (!isVariable(a0) && !isVariable(a1) && !isVariable(a2)) {
                return Integer.parseInt(a0) == Integer.parseInt(a1) - Integer.parseInt(a2);
            }

            if (isVariable(a0)) {
                int value = Integer.parseInt(a1) - Integer.parseInt(a2);
                return bindOrMatch(a0, String.valueOf(value), bindings);
            }

            if (isVariable(a1)) {
                int value = Integer.parseInt(a0) + Integer.parseInt(a2);
                return bindOrMatch(a1, String.valueOf(value), bindings);
            }

            int value = Integer.parseInt(a1) - Integer.parseInt(a0);
            return bindOrMatch(a2, String.valueOf(value), bindings);

        } catch (NumberFormatException e) {
            return false;
        }
    }

    // dunli checks equality and can bind one variable
    private boolean handleDunli(ArrayList<String> args, HashMap<String, String> bindings) {
        if (args.size() != 2) {
            return false;
        }

        String left = resolveValue(args.get(0), bindings);
        String right = resolveValue(args.get(1), bindings);

        if (isVariable(left) && !isVariable(right)) {
            return bindOrMatch(left, right, bindings);
        }

        if (!isVariable(left) && isVariable(right)) {
            return bindOrMatch(right, left, bindings);
        }

        if (isVariable(left) && isVariable(right)) {
            return bindOrMatch(left, right, bindings);
        }

        return left.equals(right);
    }

    // steni represents the empty list
    private boolean handleSteni(ArrayList<String> args, HashMap<String, String> bindings) {
        if (args.size() != 1) {
            return false;
        }

        String arg = resolveValue(args.get(0), bindings);

        if (isVariable(arg)) {
            return bindOrMatch(arg, "[]", bindings);
        }

        return arg.equals("[]");
    }

    // steko represents a list node [HEAD:x,TAIL:y]
    private boolean handleSteko(ArrayList<String> args, HashMap<String, String> bindings) {
        if (args.size() != 3) {
            return false;
        }

        String whole = resolveValue(args.get(0), bindings);
        String head = resolveValue(args.get(1), bindings);
        String tail = resolveValue(args.get(2), bindings);

        String listValue = "[HEAD:" + head + ",TAIL:" + tail + "]";

        if (isVariable(whole)) {
            return bindOrMatch(whole, listValue, bindings);
        }

        return whole.equals(listValue);
    }

    // Very simple cmavo handling:
    // first argument = new predicate name
    // second argument = existing predicate name
    private void handleCmavoDefinition(ArrayList<String> args) {
        if (args.size() >= 2) {
            String newPredicate = args.get(0);
            String existingPredicate = args.get(1);
            definitionDatabase.put(newPredicate, existingPredicate);
        }
    }

    // Checks whether a matching fact was stored earlier,
    // allowing variable bindings in the query
    private boolean checkStoredFact(String predicate, ArrayList<String> args, HashMap<String, String> bindings) {
        if (!factDatabase.containsKey(predicate)) {
            return false;
        }

        ArrayList<ArrayList<String>> knownFacts = factDatabase.get(predicate);

        for (ArrayList<String> factArgs : knownFacts) {
            if (factArgs.size() != args.size()) {
                continue;
            }

            HashMap<String, String> tempBindings = new HashMap<>(bindings);
            boolean match = true;

            for (int i = 0; i < args.size(); i++) {
                String queryArg = resolveValue(args.get(i), tempBindings);
                String factArg = factArgs.get(i);

                if (isVariable(queryArg)) {
                    if (!bindOrMatch(queryArg, factArg, tempBindings)) {
                        match = false;
                        break;
                    }
                } else if (!queryArg.equals(factArg)) {
                    match = false;
                    break;
                }
            }

            if (match) {
                bindings.clear();
                bindings.putAll(tempBindings);
                return true;
            }
        }

        return false;
    }
}