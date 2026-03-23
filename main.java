import java.util.ArrayList;
import java.util.Scanner;

public class main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            // Read the full input string from the user
            System.out.println("Enter the input string:");
            String input = scanner.nextLine();

            // Step 1: turn the raw input into tokens
            ArrayList<token> tokens = tokenizer.tokenize(input);
            System.out.println("Tokens:");
            for (token oneToken : tokens) {
                System.out.println(oneToken);
            }

            // Step 2: turn the tokens into statements
            ArrayList<statement> statements = parser.parse(tokens);
            System.out.println("\nStatements:");
            for (statement oneStatement : statements) {
                System.out.println(oneStatement);
            }

            // Step 3: run the statements
            System.out.println("\nInterpreter Output:");
            interpreter interpreterObject = new interpreter();
            interpreterObject.execute(statements);

        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } finally {
            scanner.close();
        }
    }
}