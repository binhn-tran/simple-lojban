import java.util.ArrayList;

public class Parser {

    // Takes a list of tokens and turns them into a list of statements
    public static ArrayList<Statement> parse(ArrayList<Token> tokens) {
        ArrayList<Statement> statements = new ArrayList<>();
        ArrayList<Token> currentStatement = new ArrayList<>();

        for (Token token : tokens) {
            // "i" marks the start of a new statement
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

    // Parses one statement based on the assignment rules
    private static Statement parseSingleStatement(ArrayList<Token> tokens) {
        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("Empty statement");
        }

        // Handle cmavo separately because it is a definition form
        if (containsPredicate(tokens, "cmavo")) {
            return parseCmavoStatement(tokens);
        }

        return parseRegularStatement(tokens);
    }

    // Parses a normal statement where:
    // first argument comes before the predicate
    // remaining arguments come after the predicate
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

        // First argument comes before the predicate
        String firstArgument = parseArgument(tokens, 0, predicateIndex);
        arguments.add(firstArgument);

        // Remaining arguments come after the predicate
        int i = predicateIndex + 1;
        while (i < tokens.size()) {
            int nextIndex = findNextArgumentEnd(tokens, i);
            arguments.add(parseArgument(tokens, i, nextIndex));
            i = nextIndex;
        }

        // Handle "se" by swapping the first two arguments
        // This is a simple version: if "se" appears anywhere in the statement,
        // swap argument 1 and argument 2 if both exist
        if (containsShortWord(tokens, "se") && arguments.size() >= 2) {
            String temp = arguments.get(0);
            arguments.set(0, arguments.get(1));
            arguments.set(1, temp);
        }

        return new Statement(predicate, arguments);
    }

    // Parses a cmavo statement in a simple way
    // For now, store everything after cmavo as arguments
    private static Statement parseCmavoStatement(ArrayList<Token> tokens) {
        int predicateIndex = findPredicate(tokens, "cmavo");

        if (predicateIndex == -1) {
            throw new IllegalArgumentException("Invalid cmavo statement");
        }

        ArrayList<String> arguments = new ArrayList<>();

        // Store everything except the word "cmavo" as arguments
        for (int i = 0; i < tokens.size(); i++) {
            if (i != predicateIndex) {
                arguments.add(tokens.get(i).getValue());
            }
        }

        return new Statement("cmavo", arguments);
    }

    // Finds the main predicate in a regular statement
    // Ignores predicate words that are part of "lo steni" or "lo steko"
    private static int findMainPredicate(ArrayList<Token> tokens) {
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).getType().equals("Predicate")) {
                String value = tokens.get(i).getValue();

                // steni and steko can appear inside lo-expressions, so skip those if needed
                if (value.equals("steni") || value.equals("steko")) {
                    if (i > 0 && tokens.get(i - 1).getValue().equals("lo")) {
                        continue;
                    }
                }

                // skip cmavo here because it is handled separately
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
            if (tokens.get(i).getType().equals("Predicate")
                    && tokens.get(i).getValue().equals(name)) {
                return i;
            }
        }
        return -1;
    }

    // Checks whether a statement contains a certain predicate
    private static boolean containsPredicate(ArrayList<Token> tokens, String name) {
        return findPredicate(tokens, name) != -1;
    }

    // Checks whether a statement contains a certain short word
    private static boolean containsShortWord(ArrayList<Token> tokens, String name) {
        for (Token token : tokens) {
            if (token.getType().equals("Short") && token.getValue().equals(name)) {
                return true;
            }
        }
        return false;
    }

    // Parses one argument from token positions [start, end)
    private static String parseArgument(ArrayList<Token> tokens, int start, int end) {
        if (start >= end) {
            throw new IllegalArgumentException("Invalid argument");
        }

        Token first = tokens.get(start);

        // Handle "lo ..."
        if (first.getValue().equals("lo")) {
            if (start + 1 >= end) {
                throw new IllegalArgumentException("Incomplete lo expression");
            }

            String next = tokens.get(start + 1).getValue();

            // lo steni -> empty list
            if (next.equals("steni")) {
                return "[]";
            }

            // lo steko X Y -> list node
            if (next.equals("steko")) {
                if (start + 3 >= end) {
                    throw new IllegalArgumentException("Incomplete steko expression");
                }

                String head = tokens.get(start + 2).getValue();
                String tail = tokens.get(start + 3).getValue();
                return "[HEAD:" + head + ",TAIL:" + tail + "]";
            }

            // lo name or lo predicate-word
            return tokens.get(start + 1).getValue();
        }

        // Handle "se" before an argument by skipping it here
        if (first.getValue().equals("se")) {
            if (start + 1 >= end) {
                throw new IllegalArgumentException("Incomplete se expression");
            }
            return parseArgument(tokens, start + 1, end);
        }

        // Normal single-token argument
        return first.getValue();
    }

    // Finds where the next argument ends
    private static int findNextArgumentEnd(ArrayList<Token> tokens, int start) {
        if (start >= tokens.size()) {
            return start;
        }

        // lo steni -> 2 tokens
        if (tokens.get(start).getValue().equals("lo")) {
            if (start + 1 < tokens.size()) {
                String next = tokens.get(start + 1).getValue();

                if (next.equals("steni")) {
                    return Math.min(start + 2, tokens.size());
                }

                // lo steko X Y -> 4 tokens
                if (next.equals("steko")) {
                    return Math.min(start + 4, tokens.size());
                }

                // lo something -> 2 tokens
                return Math.min(start + 2, tokens.size());
            }
        }

        // se X -> treat as one argument expression of length 2
        if (tokens.get(start).getValue().equals("se")) {
            return Math.min(start + 2, tokens.size());
        }

        // Normal one-token argument
        return start + 1;
    }
}