import java.util.ArrayList;

public class Tokenizer {

    // Consonants and vowels used for short words and predicate words
    private static final String CONSONANT = "[bcdfgjklmnprstvxz]";
    private static final String VOWEL = "[aeiou]";

    // Takes the input string and turns it into a list of tokens
    public static ArrayList<Token> tokenize(String input) {
        ArrayList<Token> tokens = new ArrayList<>();

        // Uppercase and lowercase are treated the same
        input = input.toLowerCase();

        // Make sure the input contains only allowed characters
        if (!input.matches("[a-z0-9.\\s]*")) {
            throw new IllegalArgumentException("Input contains invalid characters.");
        }

        // Remove extra space at the start and end
        input = input.trim();

        // Empty input gives an empty token list
        if (input.isEmpty()) {
            return tokens;
        }

        // Split the input into words using whitespace
        String[] parts = input.split("\\s+");

        // Check each word and classify it
        for (String part : parts) {
            if (isNumber(part)) {
                tokens.add(new Token("NUMBER", part));
            } else if (isName(part)) {
                tokens.add(new Token("NAME", part));
            } else if (isShortWord(part)) {
                tokens.add(new Token("SHORT", part));
            } else if (isPredicateWord(part)) {
                tokens.add(new Token("PREDICATE", part));
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

    // Checks if the word is a valid name:
    // starts with a period, ends with a period, and has letters in between
    public static boolean isName(String word) {
        return word.matches("\\.[a-z]+\\.");
    }

    // Checks if the word is a short word in CV form
    public static boolean isShortWord(String word) {
        return word.matches(CONSONANT + VOWEL);
    }

    // Checks if the word is a predicate word in CVCCV or CCVCV form
    public static boolean isPredicateWord(String word) {
        return word.matches(CONSONANT + VOWEL + CONSONANT + CONSONANT + VOWEL)
                || word.matches(CONSONANT + CONSONANT + VOWEL + CONSONANT + VOWEL);
    }
}