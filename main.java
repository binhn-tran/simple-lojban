import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        // Read the full input string from the user
        System.out.println("Enter the input string:");
        String input = scanner.nextLine();

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

        scanner.close();
    }
}