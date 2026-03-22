import java.util.ArrayList;

public class Main {
    public static void main(String[] args) {

        // Example input string
        String input = "i 5 fatci i 7 sumji 3 4 i 7 dunli 7";

        // Step 1: turn the raw input into tokens
        ArrayList<Token> tokens = Tokenizer.tokenize(input);
        System.out.println("Tokens:");
        for (Token token : tokens) {
            System.out.println(token);
        }

        // Step 2: turn the tokens into statements
        ArrayList<Statement> statements = Parser.parse(tokens);
        System.out.println("\nStatements:");
        for (Statement statement : statements) {
            System.out.println(statement);
        }

        // Step 3: run the statements
        System.out.println("\nInterpreter Output:");
        Interpreter interpreter = new Interpreter();
        interpreter.execute(statements);
    }
}