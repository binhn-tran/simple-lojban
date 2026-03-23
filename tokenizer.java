import java.util.ArrayList;

public class tokenizer {

    // Consonants and vowels for Lojban word patterns
    private static final String CONSONANT = "[bcdfgjklmnprstvxz]";
    private static final String VOWEL = "[aeiou]";

    // Turns the input string into a list of tokens
    public static ArrayList<token> tokenize(String input) {
        ArrayList<token> tokens = new ArrayList<>();

        // Uppercase and lowercase are treated the same
        input = input.toLowerCase();

        // Only letters, digits, periods, and whitespace are allowed
        if (!input.matches("[a-z0-9.\\s]*")) {
            throw new IllegalArgumentException("Input contains invalid characters.");
        }

        // Remove extra space around the whole input
        input = input.trim();

        // Empty input is invalid for this assignment
        if (input.isEmpty()) {
            throw new IllegalArgumentException("Input cannot be empty.");
        }

        // Split input into whitespace-separated parts
        String[] parts = input.split("\\s+");

        // Check each part and classify it
        for (String part : parts) {
            if (isStatementStart(part)) {
                tokens.add(new token("STATEMENT_START", part));
            } else if (isNumber(part)) {
                tokens.add(new token("NUMBER", part));
            } else if (isName(part)) {
                tokens.add(new token("NAME", part));
            } else if (isShortWord(part)) {
                tokens.add(new token("SHORT", part));
            } else if (isPredicateWord(part)) {
                tokens.add(new token("PREDICATE", part));
            } else {
                throw new IllegalArgumentException("Invalid token: " + part);
            }
        }

        return tokens;
    }

    // Every statement starts with i
    public static boolean isStatementStart(String word) {
        return word.equals("i");
    }

    // Integer with no leading zeros except for 0 itself
    public static boolean isNumber(String word) {
        return word.matches("0|[1-9][0-9]*");
    }

    // Names start and end with a period and have one or more letters inside
    public static boolean isName(String word) {
        return word.matches("\\.[a-z]+\\.");
    }

    // Short words are exactly CV
    public static boolean isShortWord(String word) {
        return word.matches(CONSONANT + VOWEL);
    }

    // Predicate words are exactly CVCCV or CCVCV
    public static boolean isPredicateWord(String word) {
        return word.matches(CONSONANT + VOWEL + CONSONANT + CONSONANT + VOWEL)
            || word.matches(CONSONANT + CONSONANT + VOWEL + CONSONANT + VOWEL);
    }
}