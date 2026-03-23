import java.util.ArrayList;

public class parser {

    // Takes a list of tokens and turns them into statements
    public static ArrayList<statement> parse(ArrayList<token> tokens) {
        ArrayList<statement> statements = new ArrayList<>();

        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("No tokens to parse.");
        }

        int index = 0;

        while (index < tokens.size()) {
            // Every statement must begin with i
            if (!tokens.get(index).getType().equals("STATEMENT_START")) {
                throw new IllegalArgumentException("Each statement must start with i.");
            }

            index++;

            ArrayList<token> currentStatement = new ArrayList<>();

            while (index < tokens.size()
                    && !tokens.get(index).getType().equals("STATEMENT_START")) {
                currentStatement.add(tokens.get(index));
                index++;
            }

            if (currentStatement.isEmpty()) {
                throw new IllegalArgumentException("Empty statement after i.");
            }

            statements.add(parseSingleStatement(currentStatement));
        }

        return statements;
    }

    // Parses one statement
    private static statement parseSingleStatement(ArrayList<token> tokens) {
        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("Empty statement.");
        }

        if (containsPredicate(tokens, "cmavo")) {
            return parseCmavoStatement(tokens);
        }

        return parseRegularStatement(tokens);
    }

    // Parses a normal statement with a main predicate
    private static statement parseRegularStatement(ArrayList<token> tokens) {
        int predicateIndex = findMainPredicate(tokens);

        if (predicateIndex == -1) {
            throw new IllegalArgumentException("No predicate found in statement.");
        }

        if (predicateIndex == 0) {
            throw new IllegalArgumentException("Missing first argument before predicate.");
        }

        String predicate = tokens.get(predicateIndex).getValue();
        ArrayList<value> arguments = new ArrayList<>();

        ParsedArgument firstParsed = parseArgument(tokens, 0, predicateIndex);
        arguments.add(firstParsed.value);

        int index = predicateIndex + 1;
        while (index < tokens.size()) {
            ParsedArgument parsed = parseArgument(tokens, index, tokens.size());
            arguments.add(parsed.value);
            index = parsed.nextIndex;
        }

        boolean seBeforeFirst = startsWithSe(tokens, 0, predicateIndex);
        boolean seBeforeSecond = predicateIndex + 1 < tokens.size()
                && tokens.get(predicateIndex + 1).getValue().equals("se");

        if ((seBeforeFirst || seBeforeSecond) && arguments.size() >= 2) {
            value temp = arguments.get(0);
            arguments.set(0, arguments.get(1));
            arguments.set(1, temp);
        }

        return new statement(predicate, arguments, false);
    }

    // Parses a cmavo definition statement
    private static statement parseCmavoStatement(ArrayList<token> tokens) {
        int cmavoIndex = findPredicate(tokens, "cmavo");

        if (cmavoIndex == -1) {
            throw new IllegalArgumentException("Invalid cmavo statement.");
        }

        ArrayList<value> arguments = new ArrayList<>();

        for (int i = 0; i < tokens.size();) {
            if (i == cmavoIndex) {
                i++;
                continue;
            }

            ParsedArgument parsed = parseArgument(tokens, i, tokens.size());
            arguments.add(parsed.value);
            i = parsed.nextIndex;
        }

        return new statement("cmavo", arguments, true);
    }

    // Finds the main predicate of a regular statement
    private static int findMainPredicate(ArrayList<token> tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).getType().equals("PREDICATE")) {
                String valueText = tokens.get(i).getValue();

                // lo steni and lo steko are list expressions, not the main predicate
                if ((valueText.equals("steni") || valueText.equals("steko"))
                        && i > 0
                        && tokens.get(i - 1).getValue().equals("lo")) {
                    continue;
                }

                if (!valueText.equals("cmavo")) {
                    return i;
                }
            }
        }
        return -1;
    }

    // Finds a specific predicate by name
    private static int findPredicate(ArrayList<token> tokens, String name) {
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).getType().equals("PREDICATE")
                    && tokens.get(i).getValue().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    // Checks whether the statement contains a certain predicate
    private static boolean containsPredicate(ArrayList<token> tokens, String name) {
        return findPredicate(tokens, name) != -1;
    }

    // Checks whether an argument starts with se
    private static boolean startsWithSe(ArrayList<token> tokens, int start, int limit) {
        return start < limit && tokens.get(start).getValue().equals("se");
    }

    // Parses one argument starting at a given position
    private static ParsedArgument parseArgument(ArrayList<token> tokens, int start, int limit) {
        if (start >= limit || start >= tokens.size()) {
            throw new IllegalArgumentException("Invalid argument start.");
        }

        token first = tokens.get(start);

        // se just swaps places later, so skip it while reading the argument
        if (first.getValue().equals("se")) {
            if (start + 1 >= limit) {
                throw new IllegalArgumentException("Incomplete se expression.");
            }
            return parseArgument(tokens, start + 1, limit);
        }

        // lo introduces a description or list structure
        if (first.getValue().equals("lo")) {
            if (start + 1 >= limit) {
                throw new IllegalArgumentException("Incomplete lo expression.");
            }

            token second = tokens.get(start + 1);
            String next = second.getValue();

            // lo steni = empty list
            if (next.equals("steni")) {
                return new ParsedArgument(new value(value.EMPTY_LIST, (String) null), start + 2);
            }

            // lo steko X Y = list node
            if (next.equals("steko")) {
                ParsedArgument headArg = parseArgument(tokens, start + 2, limit);
                ParsedArgument tailArg = parseArgument(tokens, headArg.nextIndex, limit);

                value listValue = new value(value.LIST, headArg.value, tailArg.value);
                return new ParsedArgument(listValue, tailArg.nextIndex);
            }

            // lo followed by a name or predicate word
            if (second.getType().equals("NAME") || second.getType().equals("PREDICATE")) {
                return new ParsedArgument(new value(value.NAME, second.getValue()), start + 2);
            }

            throw new IllegalArgumentException("Invalid lo expression.");
        }

        if (first.getType().equals("NUMBER")) {
            return new ParsedArgument(new value(value.NUMBER, first.getValue()), start + 1);
        }

        if (first.getType().equals("NAME")) {
            return new ParsedArgument(new value(value.NAME, first.getValue()), start + 1);
        }

        if (first.getType().equals("SHORT") || first.getType().equals("PREDICATE")) {
            return new ParsedArgument(new value(value.NAME, first.getValue()), start + 1);
        }

        throw new IllegalArgumentException("Could not parse argument starting at: " + first.getValue());
    }

    // Small helper class for returning both a value and the next position
    private static class ParsedArgument {
        value value;
        int nextIndex;

        ParsedArgument(value value, int nextIndex) {
            this.value = value;
            this.nextIndex = nextIndex;
        }
    }
}