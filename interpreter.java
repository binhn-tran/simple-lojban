import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class interpreter {

    // Stores ordinary facts:
    // predicate name -> list of argument lists
    private HashMap<String, ArrayList<ArrayList<value>>> factDatabase;

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

    public interpreter() {
        factDatabase = new HashMap<>();
        definitionDatabase = new HashMap<>();
    }

    // Runs the whole program
    public void execute(ArrayList<statement> statements) {
        if (statements == null || statements.isEmpty()) {
            System.out.println("No statements to execute.");
            return;
        }

        // Build database from all but the last statement
        for (int i = 0; i < statements.size() - 1; i++) {
            processStatement(statements.get(i));
        }

        // Final statement is the query
        statement query = statements.get(statements.size() - 1);
        HashMap<String, value> bindings = new HashMap<>();

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
            for (Map.Entry<String, value> entry : bindings.entrySet()) {
                System.out.println(entry.getKey() + " = " + entry.getValue());
            }
        }
    }

    // Processes a statement before the final query
    private void processStatement(statement statementObject) {
        String predicate = statementObject.getPredicate();
        ArrayList<value> args = statementObject.getArguments();

        if (statementObject.isDefinition() || predicate.equals("cmavo")) {
            handleCmavoDefinition(args);
            return;
        }

        // Store non-built-in facts directly
        if (!isBuiltIn(predicate) && !definitionDatabase.containsKey(predicate)) {
            storeFact(predicate, args);
            return;
        }

        // For built-ins, only store if the statement is already true
        HashMap<String, value> tempBindings = new HashMap<>();
        if (evaluateStatement(statementObject, tempBindings)) {
            storeFact(predicate, args);
        }
    }

    // Stores a fact
    private void storeFact(String predicate, ArrayList<value> args) {
        factDatabase.putIfAbsent(predicate, new ArrayList<>());
        factDatabase.get(predicate).add(new ArrayList<>(args));
    }

    // Evaluates a single statement
    private boolean evaluateStatement(statement statementObject, HashMap<String, value> bindings) {
        String predicate = statementObject.getPredicate();
        ArrayList<value> args = statementObject.getArguments();

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
    private boolean evaluateCmavoCall(String predicate, ArrayList<value> args, HashMap<String, value> bindings) {
        CmavoDefinition definition = definitionDatabase.get(predicate);

        if (definition == null) {
            return false;
        }

        // Argument count must match the definition
        if (args.size() != definition.parameterNames.size()) {
            return false;
        }

        HashMap<String, value> localBindings = new HashMap<>(bindings);

        // Bind actual arguments to the cmavo parameter names
        for (int i = 0; i < args.size(); i++) {
            String parameterName = definition.parameterNames.get(i);
            value actualArgument = resolveValue(args.get(i), localBindings);
            value parameterValue = new value(value.NAME, parameterName);

            if (!bindOrMatch(parameterValue, actualArgument, localBindings)) {
                return false;
            }
        }

        // Expand into a call to the target predicate using bound arguments
        ArrayList<value> targetArgs = new ArrayList<>();
        for (String parameterName : definition.parameterNames) {
            if (!localBindings.containsKey(parameterName)) {
                return false;
            }
            targetArgs.add(localBindings.get(parameterName));
        }

        statement expandedStatement = new statement(definition.targetPredicate, targetArgs, false);
        boolean result = evaluateStatement(expandedStatement, localBindings);

        if (result) {
            bindings.clear();
            bindings.putAll(localBindings);
        }

        return result;
    }

    // Evaluates built-in predicates
    private boolean evaluateBuiltIn(String predicate, ArrayList<value> args, HashMap<String, value> bindings) {
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
    private boolean isVariable(value oneValue) {
        if (oneValue == null) {
            return false;
        }

        if (!oneValue.getType().equals(value.NAME)) {
            return false;
        }

        String text = oneValue.getValue();
        return text != null && !text.matches("\\.[a-z]+\\.");
    }

    // Binds a variable or checks equality with an existing binding
    private boolean bindOrMatch(value variable, value actualValue, HashMap<String, value> bindings) {
        String variableName = variable.getValue();

        if (!bindings.containsKey(variableName)) {
            bindings.put(variableName, actualValue);
            return true;
        }

        return sameValue(bindings.get(variableName), actualValue);
    }

    // Resolves a value if it is already a bound variable
    private value resolveValue(value oneValue, HashMap<String, value> bindings) {
        if (isVariable(oneValue) && bindings.containsKey(oneValue.getValue())) {
            return bindings.get(oneValue.getValue());
        }
        return oneValue;
    }

    // Compares two values structurally
    private boolean sameValue(value left, value right) {
        if (left == null || right == null) {
            return left == right;
        }

        if (!left.getType().equals(right.getType())) {
            return false;
        }

        if (left.getType().equals(value.LIST)) {
            return sameValue(left.getHead(), right.getHead())
                    && sameValue(left.getTail(), right.getTail());
        }

        if (left.getType().equals(value.EMPTY_LIST)) {
            return true;
        }

        return left.getValue().equals(right.getValue());
    }

    // fatci succeeds if exactly one non-variable argument is given
    private boolean handleFatci(ArrayList<value> args, HashMap<String, value> bindings) {
        if (args.size() != 1) {
            return false;
        }

        value arg = resolveValue(args.get(0), bindings);
        return !isVariable(arg);
    }

    // sumji means first = second + third
    // Supports at most one variable
    private boolean handleSumji(ArrayList<value> args, HashMap<String, value> bindings) {
        if (args.size() != 3) {
            return false;
        }

        value a0 = resolveValue(args.get(0), bindings);
        value a1 = resolveValue(args.get(1), bindings);
        value a2 = resolveValue(args.get(2), bindings);

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
                int computedValue = Integer.parseInt(a1.getValue()) + Integer.parseInt(a2.getValue());
                return bindOrMatch(a0, new value(value.NUMBER, String.valueOf(computedValue)), bindings);
            }

            if (isVariable(a1)) {
                int computedValue = Integer.parseInt(a0.getValue()) - Integer.parseInt(a2.getValue());
                return bindOrMatch(a1, new value(value.NUMBER, String.valueOf(computedValue)), bindings);
            }

            int computedValue = Integer.parseInt(a0.getValue()) - Integer.parseInt(a1.getValue());
            return bindOrMatch(a2, new value(value.NUMBER, String.valueOf(computedValue)), bindings);

        } catch (NumberFormatException e) {
            return false;
        }
    }

    // vujni means first = second - third
    // Supports at most one variable
    private boolean handleVujni(ArrayList<value> args, HashMap<String, value> bindings) {
        if (args.size() != 3) {
            return false;
        }

        value a0 = resolveValue(args.get(0), bindings);
        value a1 = resolveValue(args.get(1), bindings);
        value a2 = resolveValue(args.get(2), bindings);

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
                int computedValue = Integer.parseInt(a1.getValue()) - Integer.parseInt(a2.getValue());
                return bindOrMatch(a0, new value(value.NUMBER, String.valueOf(computedValue)), bindings);
            }

            if (isVariable(a1)) {
                int computedValue = Integer.parseInt(a0.getValue()) + Integer.parseInt(a2.getValue());
                return bindOrMatch(a1, new value(value.NUMBER, String.valueOf(computedValue)), bindings);
            }

            int computedValue = Integer.parseInt(a1.getValue()) - Integer.parseInt(a0.getValue());
            return bindOrMatch(a2, new value(value.NUMBER, String.valueOf(computedValue)), bindings);

        } catch (NumberFormatException e) {
            return false;
        }
    }

    // dunli checks equality and can bind one variable
    private boolean handleDunli(ArrayList<value> args, HashMap<String, value> bindings) {
        if (args.size() != 2) {
            return false;
        }

        value left = resolveValue(args.get(0), bindings);
        value right = resolveValue(args.get(1), bindings);

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
    private boolean handleSteni(ArrayList<value> args, HashMap<String, value> bindings) {
        if (args.size() != 1) {
            return false;
        }

        value arg = resolveValue(args.get(0), bindings);
        value empty = new value(value.EMPTY_LIST, (String) null);

        if (isVariable(arg)) {
            return bindOrMatch(arg, empty, bindings);
        }

        return sameValue(arg, empty);
    }

    // steko represents a list node
    private boolean handleSteko(ArrayList<value> args, HashMap<String, value> bindings) {
        if (args.size() != 3) {
            return false;
        }

        value whole = resolveValue(args.get(0), bindings);
        value head = resolveValue(args.get(1), bindings);
        value tail = resolveValue(args.get(2), bindings);

        value listValue = new value(value.LIST, head, tail);

        if (isVariable(whole)) {
            return bindOrMatch(whole, listValue, bindings);
        }

        return sameValue(whole, listValue);
    }

    // cmavo definition:
    // first argument = new predicate name
    // middle arguments = parameter names
    // last argument = target predicate name
    private void handleCmavoDefinition(ArrayList<value> args) {
        if (args.size() < 2) {
            return;
        }

        value newPredicateValue = args.get(0);
        value targetPredicateValue = args.get(args.size() - 1);

        if (newPredicateValue.getValue() == null || targetPredicateValue.getValue() == null) {
            return;
        }

        String newPredicateName = newPredicateValue.getValue();
        String targetPredicateName = targetPredicateValue.getValue();

        ArrayList<String> parameterNames = new ArrayList<>();

        for (int i = 1; i < args.size() - 1; i++) {
            value param = args.get(i);
            if (param.getValue() != null) {
                parameterNames.add(param.getValue());
            }
        }

        definitionDatabase.put(newPredicateName,
                new CmavoDefinition(targetPredicateName, parameterNames));
    }

    // Checks whether a matching fact was stored earlier
    private boolean checkStoredFact(String predicate, ArrayList<value> args, HashMap<String, value> bindings) {
        if (!factDatabase.containsKey(predicate)) {
            return false;
        }

        ArrayList<ArrayList<value>> knownFacts = factDatabase.get(predicate);

        for (ArrayList<value> factArgs : knownFacts) {
            if (factArgs.size() != args.size()) {
                continue;
            }

            HashMap<String, value> tempBindings = new HashMap<>(bindings);
            boolean match = true;

            for (int i = 0; i < args.size(); i++) {
                value queryArg = resolveValue(args.get(i), tempBindings);
                value factArg = factArgs.get(i);

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