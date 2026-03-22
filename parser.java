import java.util.ArrayList;

public class Parser {

    // Takes a list of tokens and turns them into a list of statements
    public static ArrayList<Statement> parse(ArrayList<Token> tokens) {
        ArrayList<Statement> statements = new ArrayList<>();
        ArrayList<Token> currentStatement = new ArrayList<>();

        for (Token token : tokens) {
            // Each statement starts with "i"
            if (token.getValue().equals("i")) {
                if (!currentStatement.isEmpty()) {
                    statements.add(parseSingleStatement(currentStatement));
                    currentStatement.clear();
                }
            } else {
                currentStatement.add(token);
            }
        }

        // Add the last statement if there is one
        if (!currentStatement.isEmpty()) {
            statements.add(parseSingleStatement(currentStatement));
        }

        return statements;
    }

    // Parses one statement
    private static Statement parseSingleStatement(ArrayList<Token> tokens) {
        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("Empty statement");
        }

        if (containsPredicate(tokens, "cmavo")) {
            return parseCmavoStatement(tokens);
        }

        return parseRegularStatement(tokens);
    }

    // Parses a regular statement:
    // first argument before predicate, remaining arguments after predicate
    private static Statement parseRegularStatement(ArrayList<Token> tokens) {
        int predicateIndex = findMainPredicate(tokens);

        if (predicateIndex == -1) {
            throw new IllegalArgumentException("No predicate found in statement");
        }

        if (predicateIndex == 0) {
            throw new IllegalArgumentException("Missing first argument before predicate");
        }

        String predicate = tokens.get(predicateIndex).getValue();
        ArrayList<Value> arguments = new ArrayList<>();

        // Parse the first argument
        ParsedArgument firstParsed = parseArgument(tokens, 0);
        if (firstParsed.nextIndex != predicateIndex) {
            throw new IllegalArgumentException("Invalid first argument before predicate");
        }
        arguments.add(firstParsed.value);

        // Parse remaining arguments
        int index = predicateIndex + 1;
        while (index < tokens.size()) {
            ParsedArgument parsed = parseArgument(tokens, index);
            arguments.add(parsed.value);
            index = parsed.nextIndex;
        }

        // Handle se by swapping first two arguments
        boolean seBeforeFirst = startsWithSe(tokens, 0, predicateIndex);
        boolean seBeforeSecond = predicateIndex + 1 < tokens.size()
                && tokens.get(predicateIndex + 1).getValue().equals("se");

        if ((seBeforeFirst || seBeforeSecond) && arguments.size() >= 2) {
            Value temp = arguments.get(0);
            arguments.set(0, arguments.get(1));
            arguments.set(1, temp);
        }

        return new Statement(predicate, arguments, false);
    }

    // Parses a cmavo definition statement
    private static Statement parseCmavoStatement(ArrayList<Token> tokens) {
        int cmavoIndex = findPredicate(tokens, "cmavo");

        if (cmavoIndex == -1) {
            throw new IllegalArgumentException("Invalid cmavo statement");
        }

        ArrayList<Value> arguments = new ArrayList<>();

        // Store everything except the word "cmavo" as parsed values
        for (int i = 0; i < tokens.size(); ) {
            if (i == cmavoIndex) {
                i++;
                continue;
            }

            ParsedArgument parsed = parseArgument(tokens, i);
            arguments.add(parsed.value);
            i = parsed.nextIndex;
        }

        return new Statement("cmavo", arguments, true);
    }

    // Finds the main predicate in a regular statement
    private static int findMainPredicate(ArrayList<Token> tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).getType().equals("PREDICATE")) {
                String value = tokens.get(i).getValue();

                // Ignore steni/steko when they are part of lo-expressions
                if ((value.equals("steni") || value.equals("steko"))
                        && i > 0
                        && tokens.get(i - 1).getValue().equals("lo")) {
                    continue;
                }

                if (!value.equals("cmavo")) {
                    return i;
                }
            }
        }

        return -1;
    }

    // Finds a specific predicate by name
    private static int findPredicate(ArrayList<Token> tokens, String name) {
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).getType().equals("PREDICATE")
                    && tokens.get(i).getValue().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    // Checks whether a statement contains a given predicate
    private static boolean containsPredicate(ArrayList<Token> tokens, String name) {
        return findPredicate(tokens, name) != -1;
    }

    // Checks if the argument starts with se
    private static boolean startsWithSe(ArrayList<Token> tokens, int start, int limit) {
        return start < limit && tokens.get(start).getValue().equals("se");
    }

    // Parses one argument starting at index start
    private static ParsedArgument parseArgument(ArrayList<Token> tokens, int start) {
        if (start >= tokens.size()) {
            throw new IllegalArgumentException("Invalid argument start");
        }

        Token first = tokens.get(start);

        // Handle se before an argument
        if (first.getValue().equals("se")) {
            if (start + 1 >= tokens.size()) {
                throw new IllegalArgumentException("Incomplete se expression");
            }
            return parseArgument(tokens, start + 1);
        }

        // Handle lo expressions
        if (first.getValue().equals("lo")) {
            if (start + 1 >= tokens.size()) {
                throw new IllegalArgumentException("Incomplete lo expression");
            }

            String next = tokens.get(start + 1).getValue();

            // lo steni -> empty list
            if (next.equals("steni")) {
                return new ParsedArgument(new Value(Value.EMPTY_LIST, (String) null), start + 2);
            }

            // lo steko X Y -> list node
            if (next.equals("steko")) {
                ParsedArgument headArg = parseArgument(tokens, start + 2);
                ParsedArgument tailArg = parseArgument(tokens, headArg.nextIndex);

                Value listValue = new Value(Value.LIST, headArg.value, tailArg.value);
                return new ParsedArgument(listValue, tailArg.nextIndex);
            }

            // lo name or lo predicate-word reference
            Token actual = tokens.get(start + 1);

            if (actual.getType().equals("NAME")) {
                return new ParsedArgument(new Value(Value.NAME, actual.getValue()), start + 2);
            }

            if (actual.getType().equals("PREDICATE")) {
                return new ParsedArgument(new Value(Value.NAME, actual.getValue()), start + 2);
            }

            throw new IllegalArgumentException("Invalid lo expression");
        }

        // Normal number
        if (first.getType().equals("NUMBER")) {
            return new ParsedArgument(new Value(Value.NUMBER, first.getValue()), start + 1);
        }

        // Normal name
        if (first.getType().equals("NAME")) {
            return new ParsedArgument(new Value(Value.NAME, first.getValue()), start + 1);
        }

        // Short words or predicate words used as variables/symbols
        if (first.getType().equals("SHORT") || first.getType().equals("PREDICATE")) {
            return new ParsedArgument(new Value(Value.NAME, first.getValue()), start + 1);
        }

        throw new IllegalArgumentException("Could not parse argument starting at: " + first.getValue());
    }

    // Helper class so we can return both a parsed value and the next index
    private static class ParsedArgument {
        Value value;
        int nextIndex;

        ParsedArgument(Value value, int nextIndex) {
            this.value = value;
            this.nextIndex = nextIndex;
        }
    }
}