import java.util.ArrayList;

public class Tokenizer {

    // takes the input string and turns it into a list of tokens
    public static ArrayList<Token> tokenize(String input) {
        ArrayList<Token> tokens = new ArrayList<>();

        // split the input by one or more spaces
        String[] parts = input.trim().split("\\s+");

        // iterate through each part of the input
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

    // checks if the word is made up of only digits
    public static boolean isNumber(String word) {
        return word.matches("\\d+");
    }

    // checks if the word matches a short word pattern
    public static boolean isShortWord(String word) {
        return word.equals("i") || word.equals("lo") || word.equals("se");

    // checks if the word matches one of the built-in predicate words
    public static boolean isPredicateWord(String word) {
        return word.equals("fatci") || word.equals("sumji") || word.equals("vujni") || word.equals("dunli") || word.equals("steni") || word.equals("steko") || word.equals(
            "cmavo");
    }

    // checks if the word is a valid name
    public static boolean isName(String word) {
        return word.matches("[a-z]+\\.*");
    }
}
            