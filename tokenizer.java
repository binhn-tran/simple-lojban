import java.util.ArrayList;

public class Tokenizer {

    // Takes the input string and turns it into a list of tokens
    public static ArrayList<Token> tokenize(String input) {
        ArrayList<Token> tokens = new ArrayList<>();

        // Make sure the input only contains valid characters
        if (!input.matches("[a-z0-9.\\s]+")) {
            throw new IllegalArgumentException("Input contains invalid characters.");
        }

        // Split the input by one or more spaces
        String[] parts = input.trim().split("\\s+");

        // Go through each part of the input
        for (String part : parts) {
            if (isNumber(part)) {
                tokens.add(new Token("Number", part));
            } else if (isShortWord(part)) {
                tokens.add(new Token("Short", part));
            } else if (isPredicateWord(part)) {
                tokens.add(new Token("Predicate", part));
            } else if (isName(part)) {
                tokens.add(new Token("Name", part));
            } else {
                throw new IllegalArgumentException("Invalid token: " + part);
            }
        }

        return tokens;
    }

    // Checks if the word is a valid integer without leading zeros
    public static boolean isNumber(String word) {
        return word.matches("0|[1-9][0-9]*");
    }

    // Checks if the word is one of the built-in short words
    public static boolean isShortWord(String word) {
        return word.equals("i") || word.equals("lo") || word.equals("se");
    }

    // Checks if the word is one of the built-in predicate words
    public static boolean isPredicateWord(String word) {
        return word.equals("fatci") || word.equals("sumji") || word.equals("vujni")
                || word.equals("dunli") || word.equals("steni")
                || word.equals("steko") || word.equals("cmavo");
    }

    // Checks if the word is a valid name with periods on both sides
    public static boolean isName(String word) {
        return word.matches("\\.[a-z]+\\.");
    }
}