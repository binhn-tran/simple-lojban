import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class interpreter {

    // Stores ordinary facts:
    // predicate name -> list of argument lists
    private HashMap<String, ArrayList<ArrayList<Value>>> factDatabase;

    // Stores cmavo definitions:
    // defined predicate name -> definition object
    private HashMap<String, CmavoDefinition> definitionDatabase;

    // Helper class for cmavo definitions
    private static class CmavoDefinition {
        String targetPredicate;
        ArrayList<String> parameterNames;

        CmavoDefinition(String targetPredicate, ArrayList<String> parameterNames) {
            this.targetPredicate = targetPredicate;
            this.parameterNames = parameterNames;
        }
    }

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
        HashMap<String, Value> bindings = new HashMap<>();

        boolean result = evaluateStatement(query, bindings);

        if (!result) {
            System.out.println("Query failed.");
            return;
        }

        if (bindings.isEmpty()) {
            System.out.println("Query succeeded.");
        } else {
            System.out.println("Query succeeded.");
            System.out.println("Variable values:");
            for (Map.Entry<String, Value> entry : bindings.entrySet()) {
                System.out.println(entry.getKey() + " = " + entry.getValue());
            }
        }
    }

    // Processes a statement before the final query
    private void processStatement(Statement statement) {
        String predicate = statement.getPredicate();
        ArrayList<Value> args = statement.getArguments();

        if (statement.isDefinition() || predicate.equals("cmavo")) {
            handleCmavoDefinition(args);
            return;
        }

        // Store non-built-in facts directly
        if (!isBuiltIn(predicate) && !definitionDatabase.containsKey(predicate)) {
            storeFact(predicate, args);
            return;
        }

        // For built-ins, only store if the statement is already true
        HashMap<String, Value> tempBindings = new HashMap<>();
        if (evaluateStatement(statement, tempBindings)) {
            storeFact(predicate, args);
        }
    }

    // Stores a fact
    private void storeFact(String predicate, ArrayList<Value> args) {
        factDatabase.putIfAbsent(predicate, new ArrayList<>());
        factDatabase.get(predicate).add(new ArrayList<>(args));
    }

    // Evaluates a single statement
    private boolean evaluateStatement(Statement statement, HashMap<String, Value> bindings) {
        String predicate = statement.getPredicate();
        ArrayList<Value> args = statement.getArguments();

        // Evaluate predicates defined with cmavo
        if (definitionDatabase.containsKey(predicate)) {
            return evaluateCmavoCall(predicate, args, bindings);
        }

        if (isBuiltIn(predicate)) {
            return evaluateBuiltIn(predicate, args, bindings);
        }

        return checkStoredFact(predicate, args, bindings);
    }

    // Evaluates a predicate defined using cmavo
    private boolean evaluateCmavoCall(String predicate, ArrayList<Value> args, HashMap<String, Value> bindings) {
        CmavoDefinition definition = definitionDatabase.get(predicate);

        if (definition == null) {
            return false;
        }

        // Argument count must match the definition
        if (args.size() != definition.parameterNames.size()) {
            return false;
        }

        HashMap<String, Value> localBindings = new HashMap<>(bindings);

        // Bind actual arguments to the cmavo parameter names
        for (int i = 0; i < args.size(); i++) {
            String parameterName = definition.parameterNames.get(i);
            Value actualArgument = resolveValue(args.get(i), localBindings);
            Value parameterValue = new Value(Value.NAME, parameterName);

            if (!bindOrMatch(parameterValue, actualArgument, localBindings)) {
                return false;
            }
        }

        // Expand into a call to the target predicate using bound arguments
        ArrayList<Value> targetArgs = new ArrayList<>();
        for (String parameterName : definition.parameterNames) {
            if (!localBindings.containsKey(parameterName)) {
                return false;
            }
            targetArgs.add(localBindings.get(parameterName));
        }

        Statement expandedStatement = new Statement(definition.targetPredicate, targetArgs, false);
        boolean result = evaluateStatement(expandedStatement, localBindings);

        if (result) {
            bindings.clear();
            bindings.putAll(localBindings);
        }

        return result;
    }

    // Evaluates built-in predicates
    private boolean evaluateBuiltIn(String predicate, ArrayList<Value> args, HashMap<String, Value> bindings) {
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

    // NAME values without periods are treated like variables
    private boolean isVariable(Value value) {
        if (value == null) {
            return false;
        }

        if (!value.getType().equals(Value.NAME)) {
            return false;
        }

        String text = value.getValue();
        return text != null && !text.matches("\\.[a-z]+\\.");
    }

    // Binds a variable or checks equality with an existing binding
    private boolean bindOrMatch(Value variable, Value value, HashMap<String, Value> bindings) {
        String variableName = variable.getValue();

        if (!bindings.containsKey(variableName)) {
            bindings.put(variableName, value);
            return true;
        }

        return sameValue(bindings.get(variableName), value);
    }

    // Resolves a value if it is already a bound variable
    private Value resolveValue(Value value, HashMap<String, Value> bindings) {
        if (isVariable(value) && bindings.containsKey(value.getValue())) {
            return bindings.get(value.getValue());
        }
        return value;
    }

    // Compares two values structurally
    private boolean sameValue(Value left, Value right) {
        if (left == null || right == null) {
            return left == right;
        }

        if (!left.getType().equals(right.getType())) {
            return false;
        }

        if (left.getType().equals(Value.LIST)) {
            return sameValue(left.getHead(), right.getHead())
                    && sameValue(left.getTail(), right.getTail());
        }

        if (left.getType().equals(Value.EMPTY_LIST)) {
            return true;
        }

        return left.getValue().equals(right.getValue());
    }

    // fatci succeeds if exactly one non-variable argument is given
    private boolean handleFatci(ArrayList<Value> args, HashMap<String, Value> bindings) {
        if (args.size() != 1) {
            return false;
        }

        Value arg = resolveValue(args.get(0), bindings);
        return !isVariable(arg);
    }

    // sumji means first = second + third
    // Supports at most one variable
    private boolean handleSumji(ArrayList<Value> args, HashMap<String, Value> bindings) {
        if (args.size() != 3) {
            return false;
        }

        Value a0 = resolveValue(args.get(0), bindings);
        Value a1 = resolveValue(args.get(1), bindings);
        Value a2 = resolveValue(args.get(2), bindings);

        int variableCount = 0;
        if (isVariable(a0)) variableCount++;
        if (isVariable(a1)) variableCount++;
        if (isVariable(a2)) variableCount++;

        if (variableCount > 1) {
            return false;
        }

        try {
            if (!isVariable(a0) && !isVariable(a1) && !isVariable(a2)) {
                int v0 = Integer.parseInt(a0.getValue());
                int v1 = Integer.parseInt(a1.getValue());
                int v2 = Integer.parseInt(a2.getValue());
                return v0 == v1 + v2;
            }

            if (isVariable(a0)) {
                int value = Integer.parseInt(a1.getValue()) + Integer.parseInt(a2.getValue());
                return bindOrMatch(a0, new Value(Value.NUMBER, String.valueOf(value)), bindings);
            }

            if (isVariable(a1)) {
                int value = Integer.parseInt(a0.getValue()) - Integer.parseInt(a2.getValue());
                return bindOrMatch(a1, new Value(Value.NUMBER, String.valueOf(value)), bindings);
            }

            int value = Integer.parseInt(a0.getValue()) - Integer.parseInt(a1.getValue());
            return bindOrMatch(a2, new Value(Value.NUMBER, String.valueOf(value)), bindings);

        } catch (NumberFormatException e) {
            return false;
        }
    }

    // vujni means first = second - third
    // Supports at most one variable
    private boolean handleVujni(ArrayList<Value> args, HashMap<String, Value> bindings) {
        if (args.size() != 3) {
            return false;
        }

        Value a0 = resolveValue(args.get(0), bindings);
        Value a1 = resolveValue(args.get(1), bindings);
        Value a2 = resolveValue(args.get(2), bindings);

        int variableCount = 0;
        if (isVariable(a0)) variableCount++;
        if (isVariable(a1)) variableCount++;
        if (isVariable(a2)) variableCount++;

        if (variableCount > 1) {
            return false;
        }

        try {
            if (!isVariable(a0) && !isVariable(a1) && !isVariable(a2)) {
                int v0 = Integer.parseInt(a0.getValue());
                int v1 = Integer.parseInt(a1.getValue());
                int v2 = Integer.parseInt(a2.getValue());
                return v0 == v1 - v2;
            }

            if (isVariable(a0)) {
                int value = Integer.parseInt(a1.getValue()) - Integer.parseInt(a2.getValue());
                return bindOrMatch(a0, new Value(Value.NUMBER, String.valueOf(value)), bindings);
            }

            if (isVariable(a1)) {
                int value = Integer.parseInt(a0.getValue()) + Integer.parseInt(a2.getValue());
                return bindOrMatch(a1, new Value(Value.NUMBER, String.valueOf(value)), bindings);
            }

            int value = Integer.parseInt(a1.getValue()) - Integer.parseInt(a0.getValue());
            return bindOrMatch(a2, new Value(Value.NUMBER, String.valueOf(value)), bindings);

        } catch (NumberFormatException e) {
            return false;
        }
    }

    // dunli checks equality and can bind one variable
    private boolean handleDunli(ArrayList<Value> args, HashMap<String, Value> bindings) {
        if (args.size() != 2) {
            return false;
        }

        Value left = resolveValue(args.get(0), bindings);
        Value right = resolveValue(args.get(1), bindings);

        if (isVariable(left) && !isVariable(right)) {
            return bindOrMatch(left, right, bindings);
        }

        if (!isVariable(left) && isVariable(right)) {
            return bindOrMatch(right, left, bindings);
        }

        if (isVariable(left) && isVariable(right)) {
            return bindOrMatch(left, right, bindings);
        }

        return sameValue(left, right);
    }

    // steni represents the empty list
    private boolean handleSteni(ArrayList<Value> args, HashMap<String, Value> bindings) {
        if (args.size() != 1) {
            return false;
        }

        Value arg = resolveValue(args.get(0), bindings);
        Value empty = new Value(Value.EMPTY_LIST, (String) null);

        if (isVariable(arg)) {
            return bindOrMatch(arg, empty, bindings);
        }

        return sameValue(arg, empty);
    }

    // steko represents a list node
    private boolean handleSteko(ArrayList<Value> args, HashMap<String, Value> bindings) {
        if (args.size() != 3) {
            return false;
        }

        Value whole = resolveValue(args.get(0), bindings);
        Value head = resolveValue(args.get(1), bindings);
        Value tail = resolveValue(args.get(2), bindings);

        Value listValue = new Value(Value.LIST, head, tail);

        if (isVariable(whole)) {
            return bindOrMatch(whole, listValue, bindings);
        }

        return sameValue(whole, listValue);
    }

    // cmavo definition:
    // first argument = new predicate name
    // middle arguments = parameter names
    // last argument = target predicate name
    private void handleCmavoDefinition(ArrayList<Value> args) {
        if (args.size() < 2) {
            return;
        }

        Value newPredicateValue = args.get(0);
        Value targetPredicateValue = args.get(args.size() - 1);

        if (newPredicateValue.getValue() == null || targetPredicateValue.getValue() == null) {
            return;
        }

        String newPredicateName = newPredicateValue.getValue();
        String targetPredicateName = targetPredicateValue.getValue();

        ArrayList<String> parameterNames = new ArrayList<>();

        for (int i = 1; i < args.size() - 1; i++) {
            Value param = args.get(i);
            if (param.getValue() != null) {
                parameterNames.add(param.getValue());
            }
        }

        definitionDatabase.put(newPredicateName,
                new CmavoDefinition(targetPredicateName, parameterNames));
    }

    // Checks whether a matching fact was stored earlier
    private boolean checkStoredFact(String predicate, ArrayList<Value> args, HashMap<String, Value> bindings) {
        if (!factDatabase.containsKey(predicate)) {
            return false;
        }

        ArrayList<ArrayList<Value>> knownFacts = factDatabase.get(predicate);

        for (ArrayList<Value> factArgs : knownFacts) {
            if (factArgs.size() != args.size()) {
                continue;
            }

            HashMap<String, Value> tempBindings = new HashMap<>(bindings);
            boolean match = true;

            for (int i = 0; i < args.size(); i++) {
                Value queryArg = resolveValue(args.get(i), tempBindings);
                Value factArg = factArgs.get(i);

                if (isVariable(queryArg)) {
                    if (!bindOrMatch(queryArg, factArg, tempBindings)) {
                        match = false;
                        break;
                    }
                } else if (!sameValue(queryArg, factArg)) {
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