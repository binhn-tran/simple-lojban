import java.util.ArrayList;

public class Parser {

    // takes a list of tokens and turns them into a list of statements
    public static ArrayList<Statement> parse(ArrayList<Token> tokens) {
        ArrayList<Statement> statements = new ArrayList<>();
        ArrayList<Token> currentStatement = new ArrayList<>();

        for (Token token : tokens) {
            // the start of a new statement
            if (token.getValue().equals("i")) {
                // if there is already a statement being built, finish it first
                if (!currentStatement.isEmpty()) {
                    statements.add(parseSingleStatement(currentStatement));
                    currentStatement.clear();
                }
            } else {
                currentStatement.add(token);
            }
        }

        // add the last statement if there is one
        if (!currentStatement.isEmpty()) {
            statements.add(parseSingleStatement(currentStatement));
        }

        return statements;
    }

    // turns one group of tokens into a statement object
    private static Statement parseSingleStatement(ArrayList<Token> tokens) {
        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("Empty statement");
        }

        // the first token should be the predicate
        String predicate = tokens.get(0).getValue();

        // the rest of the tokens are arguments
        ArrayList<String> arguments = new ArrayList<>();
        for (int i = 1; i < tokens.size(); i++) {
            arguments.add(tokens.get(i).getValue());
        }

        return new Statement(predicate, arguments);
    }
}