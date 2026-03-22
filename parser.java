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

    // Parses a normal statement:
    // first argument before predicate, remaining arguments after
    private static Statement parseRegularStatement(ArrayList<Token> tokens) {
        int predicateIndex = findMainPredicate(tokens);

        if (predicateIndex == -1) {
            throw new IllegalArgumentException("No predicate found in statement");
        }

        if (predicateIndex == 0) {
            throw new IllegalArgumentException("Missing first argument before predicate");
        }

        String predicate = tokens.get(predicateIndex).getValue();
        ArrayList<String> arguments = new ArrayList<>();

        // Parse first argument
        String firstArgument = parseArgument(tokens, 0, predicateIndex);
        arguments.add(firstArgument);

        // Parse arguments after predicate
        int index = predicateIndex + 1;
        while (index < tokens.size()) {
            int nextIndex = findNextArgumentEnd(tokens, index);
            arguments.add(parseArgument(tokens, index, nextIndex));
            index = nextIndex;
        }

        // Only swap if "se" appears before first or second argument
        boolean seBeforeFirst = tokens.get(0).getValue().equals("se");
        boolean seBeforeSecond = predicateIndex + 1 < tokens.size()
                && tokens.get(predicateIndex + 1).getValue().equals("se");

        if ((seBeforeFirst || seBeforeSecond) && arguments.size() >= 2) {
            String temp = arguments.get(0);
            arguments.set(0, arguments.get(1));
            arguments.set(1, temp);
        }

        return new Statement(predicate, arguments, false);
    }

    // Parses a cmavo definition statement
    private static Statement parseCmavoStatement(ArrayList<Token> tokens) {
        int predicateIndex = findPredicate(tokens, "cmavo");

        if (predicateIndex == -1) {
            throw new IllegalArgumentException("Invalid cmavo statement");
        }

        ArrayList<String> arguments = new ArrayList<>();

        for (int i = 0; i < tokens.size(); i++) {
            if (i != predicateIndex) {
                arguments.add(tokens.get(i).getValue());
            }
        }

        return new Statement("cmavo", arguments, true);
    }

    // Finds the main predicate in a regular statement
    private static int findMainPredicate(ArrayList<Token> tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).getType().equals("PREDICATE")) {
                String value = tokens.get(i).getValue();

                // Ignore list words inside lo-expressions
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

    // Parses one argument from [start, end)
    private static String parseArgument(ArrayList<Token> tokens, int start, int end) {
        if (start >= end) {
            throw new IllegalArgumentException("Invalid argument");
        }

        Token first = tokens.get(start);

        // Skip se if it appears directly before an argument
        if (first.getValue().equals("se")) {
            if (start + 1 >= end) {
                throw new IllegalArgumentException("Incomplete se expression");
            }
            return parseArgument(tokens, start + 1, end);
        }

        // Handle lo expressions
        if (first.getValue().equals("lo")) {
            if (start + 1 >= end) {
                throw new IllegalArgumentException("Incomplete lo expression");
            }

            String next = tokens.get(start + 1).getValue();

            if (next.equals("steni")) {
                return "[]";
            }

            if (next.equals("steko")) {
                if (start + 3 >= end) {
                    throw new IllegalArgumentException("Incomplete steko expression");
                }

                String head = tokens.get(start + 2).getValue();
                String tail = tokens.get(start + 3).getValue();
                return "[HEAD:" + head + ",TAIL:" + tail + "]";
            }

            return tokens.get(start + 1).getValue();
        }

        // Normal one-token argument
        return first.getValue();
    }

    // Finds the end of the next argument
    private static int findNextArgumentEnd(ArrayList<Token> tokens, int start) {
        if (start >= tokens.size()) {
            return start;
        }

        if (tokens.get(start).getValue().equals("se")) {
            return Math.min(start + 2, tokens.size());
        }

        if (tokens.get(start).getValue().equals("lo")) {
            if (start + 1 < tokens.size()) {
                String next = tokens.get(start + 1).getValue();

                if (next.equals("steni")) {
                    return Math.min(start + 2, tokens.size());
                }

                if (next.equals("steko")) {
                    return Math.min(start + 4, tokens.size());
                }

                return Math.min(start + 2, tokens.size());
            }
        }

        return start + 1;
    }
}